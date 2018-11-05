package de.redsix.pdfcompare;

import static de.redsix.pdfcompare.PdfComparator.MARKER_WIDTH;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.ImageObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffImage {

    private static final Logger LOG = LoggerFactory.getLogger(DiffImage.class);
    /*package*/ static final int MARKER_RGB = color(230, 0, 230);
    private final ImageWithDimension expectedImage;
    private final ImageWithDimension actualImage;
    private final int page;
    private final Exclusions exclusions;
    private DataBuffer expectedBuffer;
    private DataBuffer actualBuffer;
    private int expectedImageWidth;
    private int expectedImageHeight;
    private int actualImageWidth;
    private int actualImageHeight;
    private int resultImageWidth;
    private int resultImageHeight;
    private BufferedImage resultImage;
    private BufferedImage resultExpectBuffImage;
    private BufferedImage resultActualBuffImage;
    private int diffAreaX1, diffAreaY1, diffAreaX2, diffAreaY2;
    private final ResultCollector compareResult;
    private PageDiffCalculator diffCalculator;
    Graphics2D exeGraphics;
    Graphics2D actGraphics;
    int normalElement;
    int actualElement;
    int expectedElement;
    

    public DiffImage(final ImageWithDimension expectedImage, final ImageWithDimension actualImage, final int page,
            final Exclusions exclusions, final ResultCollector compareResult) {
        this.expectedImage = expectedImage;
        this.actualImage = actualImage;
        this.page = page;
        this.exclusions = exclusions;
        this.compareResult = compareResult;
    }

    public BufferedImage getImage() {
        return resultImage;
    }
    
    public BufferedImage getExpectedImage() {
        return resultExpectBuffImage;
    }
    
    public BufferedImage getActualImage() {
        return resultActualBuffImage;
    }

    public void diffImages() {
        BufferedImage expectBuffImage = this.expectedImage.bufferedImage;
        BufferedImage actualBuffImage = this.actualImage.bufferedImage;
        expectedBuffer = expectBuffImage.getRaster().getDataBuffer();
        actualBuffer = actualBuffImage.getRaster().getDataBuffer();

        expectedImageWidth = expectBuffImage.getWidth();
        expectedImageHeight = expectBuffImage.getHeight();
        actualImageWidth = actualBuffImage.getWidth();
        actualImageHeight = actualBuffImage.getHeight();

        resultImageWidth = Math.max(expectedImageWidth, actualImageWidth);
        resultImageHeight = Math.max(expectedImageHeight, actualImageHeight);
        resultImage = new BufferedImage(resultImageWidth, resultImageHeight, actualBuffImage.getType());
        resultExpectBuffImage = new BufferedImage(resultImageWidth, resultImageHeight, actualBuffImage.getType());
        resultActualBuffImage = new BufferedImage(resultImageWidth, resultImageHeight, actualBuffImage.getType());
        
        DataBuffer resultBuffer = resultImage.getRaster().getDataBuffer();
        DataBuffer actualBuffer = resultActualBuffImage.getRaster().getDataBuffer();
        DataBuffer expectBuffer = resultExpectBuffImage.getRaster().getDataBuffer();
        
        diffCalculator = new PageDiffCalculator(resultImageWidth * resultImageHeight, Environment.getAllowedDiffInPercent());

        int localExpectedElement;
        int localActualElement;
        final PageExclusions pageExclusions = exclusions.forPage(page + 1);

        for (int y = 0; y < resultImageHeight; y++) {
            final int expectedLineOffset = y * expectedImageWidth;
            final int actualLineOffset = y * actualImageWidth;
            final int resultLineOffset = y * resultImageWidth;
            for (int x = 0; x < resultImageWidth; x++) {
            	localExpectedElement = getExpectedElement(x, y, expectedLineOffset);
            	localActualElement = getActualElement(x, y, actualLineOffset);
                int element = getElement(localExpectedElement, localActualElement);
                // Page Exclusions contains x and y, dont perform any thing
                if (pageExclusions.contains(x, y)) {
                    element = ImageTools.fadeExclusion(element);
                    if (expectedElement != actualElement) {
                        diffCalculator.diffFoundInExclusion();
                    }
                } else {
                    if (expectedElement != actualElement) {
                        extendDiffArea(x, y);
                        diffCalculator.diffFound();
                        LOG.trace("Difference found on page: {} at x: {}, y: {}", page + 1, x, y);
                        mark(resultBuffer, x, y, resultImageWidth, MARKER_RGB);
                    }
                }
                resultBuffer.setElem(x + resultLineOffset, element);
                actualBuffer.setElem(x + actualLineOffset, this.getActualElement());
                expectBuffer.setElem(x + expectedLineOffset, this.getExpectedElement());
                //resultBuffer.setElem(x, element);
            }
        }
        
        
        if (diffCalculator.differencesFound()) {
        	LOG.info("Differences found at { page: {}, x1: {}, y1: {}, x2: {}, y2: {} }", page + 1, diffAreaX1, diffAreaY1, diffAreaX2,
                    diffAreaY2);
        }
        final float maxWidth = Math.max(expectedImage.width, actualImage.width);
        final float maxHeight = Math.max(expectedImage.height, actualImage.height);
        
        exeGraphics = expectedImage.bufferedImage.createGraphics();
        exeGraphics.setColor(new Color(255, 255, 0, 127));
        int exeWidth = diffAreaX2 - diffAreaX1;
        int exeHeight = diffAreaY2 - diffAreaY1;
        exeGraphics.fill3DRect(diffAreaX1, diffAreaY1, exeWidth, exeHeight, true);
        
        actGraphics = actualImage.bufferedImage.createGraphics();
        actGraphics.setColor(new Color(255, 255, 0, 127));
        actGraphics.fill3DRect(diffAreaX1, diffAreaY1, exeWidth, exeHeight, true);
        
        //compareResult.addPage(diffCalculator, page, new ImageWithDimension(resultExpectBuffImage, maxWidth, maxHeight), new ImageWithDimension(resultActualBuffImage, maxWidth, maxHeight));
        compareResult.addPage(diffCalculator, page, new ImageWithDimension(resultExpectBuffImage, maxWidth, maxHeight), new ImageWithDimension(resultActualBuffImage, maxWidth, maxHeight), new ImageWithDimension(resultImage, maxWidth, maxHeight));
        //compareResult.addPage(diffCalculator, page, expectedImage, actualImage, new ImageWithDimension(resultImage, maxWidth, maxHeight));
        
        
    }

    private void extendDiffArea(final int x, final int y) {
    	/*System.out.print(x +", ");
    	System.out.print(y +", ");*/
        if (!diffCalculator.differencesFound()) {
            diffAreaX1 = x;
            diffAreaY1 = y;
        }
        diffAreaX1 = Math.min(diffAreaX1, x);
        diffAreaX2 = Math.max(diffAreaX2, x);
        diffAreaY1 = Math.min(diffAreaY1, y);
        diffAreaY2 = Math.max(diffAreaY2, y);
    }

    private int getElement(final int expectedElement, final int actualElement) {
        if (expectedElement != actualElement) {
            int expectedDarkness = calcCombinedIntensity(expectedElement);
            int actualDarkness = calcCombinedIntensity(actualElement);
            if (expectedDarkness > actualDarkness) {
                this.setActualElement(color(levelIntensity(expectedDarkness, 210), 0, 0));
                return color(levelIntensity(expectedDarkness, 210), 0, 0);
            } else {
                this.setExpectedElement(color(0, levelIntensity(actualDarkness, 180), 0));
                return color(0, levelIntensity(actualDarkness, 180), 0);
            }
        } 
         else {
        	this.setActualElement(actualElement);
        	this.setExpectedElement(expectedElement); 
            this.setNormalElement(ImageTools.normalElement(expectedElement));
            return ImageTools.normalElement(expectedElement);
        }
    }

    private int getExpectedElement(final int x, final int y, final int expectedLineOffset) {
        if (x < expectedImageWidth && y < expectedImageHeight) {
            return expectedBuffer.getElem(x + expectedLineOffset);
        }
        return 0;
    }

    private int getActualElement(final int x, final int y, final int actualLineOffset) {
        if (x < actualImageWidth && y < actualImageHeight) {
            return actualBuffer.getElem(x + actualLineOffset);
        }
        return 0;
    }

    /**
     * Levels the color intensity to at least 50 and at most maxIntensity.
     *
     * @param darkness     color component to level
     * @param maxIntensity highest possible intensity cut off
     * @return A value that is at least 50 and at most maxIntensity
     */
    private static int levelIntensity(final int darkness, final int maxIntensity) {
        return Math.min(maxIntensity, Math.max(50, darkness));
    }

    /**
     * Calculate the combined intensity of a pixel and normalizes it to a value of at most 255.
     *
     * @param element
     * @return
     */
    private static int calcCombinedIntensity(final int element) {
        final Color color = new Color(element);
        return Math.min(255, (color.getRed() + color.getGreen() + color.getRed()) / 3);
    }

    private static void mark(final DataBuffer image, final int x, final int y, final int imageWidth, final int markerRGB) {
        final int yOffset = y * imageWidth;
        for (int i = 0; i < MARKER_WIDTH; i++) {
        	//image.setElem(x + i * imageWidth, markerRGB);
        	image.setElem(x + i * imageWidth, markerRGB);
            image.setElem(i + yOffset, markerRGB);
        }
    }

    public static int color(final int r, final int g, final int b) {
    	LOG.trace("rgb :"+r+ ","+g+","+b);
        return new Color(r, g, b).getRGB();
    }

    @Override
    public String toString() {
        return "DiffImage{" +
                "page=" + page +
                '}';
    }

	public int getNormalElement() {
		return normalElement;
	}

	public void setNormalElement(int normalElement) {
		this.normalElement = normalElement;
	}

	public int getActualElement() {
		return actualElement;
	}

	public void setActualElement(int actualElement) {
		this.actualElement = actualElement;
	}

	public int getExpectedElement() {
		return expectedElement;
	}

	public void setExpectedElement(int expectedElement) {
		this.expectedElement = expectedElement;
	}
    
    
    
}
