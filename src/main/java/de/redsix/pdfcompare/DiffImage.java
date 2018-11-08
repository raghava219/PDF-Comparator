package de.redsix.pdfcompare;

import static de.redsix.pdfcompare.PdfComparator.MARKER_WIDTH;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffImage {

    private static final Logger LOG = LoggerFactory.getLogger(DiffImage.class);
    //static final int MARKER_RGB = color(230, 0, 230);
    static final int MARKER_RGB = color(0, 0, 204);
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
    private HighlightingText highObj = new HighlightingText();
    private int[] highObjArray = new int[4];
    private Map<Integer, int[]> mapOfArrMarkObj = new HashMap<>();
    private Map<Integer, List<HighlightingText>> mapOfMarkObj = new HashMap<>(); 
    private List<HighlightingText> listOfMarkObj = new LinkedList<>();
    

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
        
        exeGraphics = expectedImage.bufferedImage.createGraphics();
        exeGraphics.setColor(new Color(255, 255, 0, 127));
        
        actGraphics = actualImage.bufferedImage.createGraphics();
        actGraphics.setColor(new Color(255, 255, 0, 127));
        

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
                        LOG.info("Difference found on page: {} at x: {}, y: {}, resulWidth: {}, resulHeight: {} ", page + 1, x, y, resultImageWidth, resultImageHeight);
                        diffCalculator.diffFound();
                        
                        listOfMarkObj = extendDiffArea(x, y, resultImageWidth, highObj);
                        mapOfMarkObj.put(resultImageWidth, listOfMarkObj);    
                        //actGraphics.fill3DRect(x, y, resultImageWidth, resultImageHeight, true);
                        //exeGraphics.fill3DRect(x, y, resultImageWidth, resultImageHeight, true);
                        //mark(resultBuffer, x, y, resultImageWidth, MARKER_RGB);
                        //highlightDiffArea(x, y, resultImageWidth, resultImageHeight);
                        
                        
                        //mark(actualBuffer, x, y, resultImageWidth, MARKER_RGB);
                        //mark(actualBuffer, x, y, resultImageWidth, resultImageHeight, MARKER_RGB);
                        //mark(expectBuffer, x, y, resultImageWidth, MARKER_RGB);
                        //mark(expectBuffer, x, y, resultImageWidth, resultImageHeight, MARKER_RGB);
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
        
        Iterator<Entry<Integer, List<HighlightingText>>> itr = mapOfMarkObj.entrySet().iterator();
        
        
        /*mapOfMarkObj.forEach((k, v) -> {
            System.out.print(k + " = ");
            //v.stream().forEach(w -> System.out.println(w.getMarkAreaX() + "," + w.getMarkAreaY() + "," + w.getMarkAreaWidth() + "," + w.getMarkAreaWidth() + ","));
            v.stream().map(w->w.getMarkAreaX()).min(null);
            System.out.println();
        });*/
        
        while(itr.hasNext()) 
        { 
             Map.Entry<Integer, List<HighlightingText>> entry = itr.next(); 
             System.out.println("Key = " + Integer.valueOf(entry.getKey()) +  ", Value = " + entry.getValue());
             
             List<HighlightingText> listOfMarkObj = entry.getValue();
             
             for(HighlightingText hobj : listOfMarkObj){
            	 System.out.println(hobj.getMarkAreaX() +" "+ hobj.getMarkAreaY() +" "+ hobj.getMarkAreaWidth() +" "+ hobj.getMarkAreaHeight());
             }
             
             HighlightingText lastHighObj = listOfMarkObj.get(listOfMarkObj.size()-1);
             HighlightingText firstHighObj = listOfMarkObj.get(0);
             
             
             int fexeX = firstHighObj.getMarkAreaX();
             int fexeY = firstHighObj.getMarkAreaY();
             int fmarkWid = firstHighObj.getMarkAreaWidth();
             int fmarkHei = firstHighObj.getMarkAreaHeight();
             int fexeWidth = fmarkWid - fexeX;
             int fexeHeight = fmarkHei - fexeY;

             
             int lexeX = lastHighObj.getMarkAreaX();
             int lexeY = lastHighObj.getMarkAreaY();
             int lmarkWid = lastHighObj.getMarkAreaWidth();
             int lmarkHei = lastHighObj.getMarkAreaHeight();
             int lexeWidth = lmarkWid - lexeX;
             int lexeHeight = lmarkHei - lexeY;
             
             //actGraphics.fill3DRect(fexeX, fexeY, fexeWidth, fexeHeight, true);
             actGraphics.fill3DRect(fexeX, fexeY, fexeWidth, fexeHeight, true);
             //exeGraphics.fill3DRect(lexeX, lexeY, lexeWidth, lexeHeight, true);   
             exeGraphics.fill3DRect(lexeX, lexeY, lexeWidth, lexeHeight, true);
             
             System.out.println("Values of first Object " + firstHighObj.getMarkAreaX() +" "+ firstHighObj.getMarkAreaY() +" "+ firstHighObj.getMarkAreaWidth() +" "+ firstHighObj.getMarkAreaHeight());
             System.out.println("Values of Last Object " + lastHighObj.getMarkAreaX() +" "+ lastHighObj.getMarkAreaY() +" "+ lastHighObj.getMarkAreaWidth() +" "+ lastHighObj.getMarkAreaHeight());
             
              
        }
        

        //compareResult.addPage(diffCalculator, page, new ImageWithDimension(resultExpectBuffImage, maxWidth, maxHeight), new ImageWithDimension(resultActualBuffImage, maxWidth, maxHeight));
        //compareResult.addPage(diffCalculator, page, new ImageWithDimension(resultExpectBuffImage, maxWidth, maxHeight), new ImageWithDimension(resultActualBuffImage, maxWidth, maxHeight), new ImageWithDimension(resultImage, maxWidth, maxHeight));
        compareResult.addPage(diffCalculator, page, expectedImage, actualImage, new ImageWithDimension(resultImage, maxWidth, maxHeight));
        
        
    }
    
    // Goal is mark the exact text, not the top and bottom
    
    //mark(resultBuffer, x+10, y+10, resultImageWidth, MARKER_RGB); --> Tried adding 10 to both x & y. But the mark on Y-axis is gone.
    
    //mark(actualBuffer, x+100, y, resultImageWidth, MARKER_RGB); --> Tried adding 100 to x. Mark moved 100 pixels on X-axis.
    
    //mark(expectBuffer, y, x, resultImageWidth, MARKER_RGB);  --> Tried exchanging x and y parameters, no use. 
    
    //private static void mark(final DataBuffer image, final int x, final int y, final int imageWidth, final int imageHeight, final int markerRGB) {
       //|--> Tried above no use, see the output in diffOutputFile_12.PDF
    	   
   	private static void mark(final DataBuffer image, final int x, final int y, final int imageWidth, final int markerRGB) {	
        
   		final int yOffset = y * imageWidth;
   		final int xOffset = y;
   		LOG.info(" Values in Mark x: "+x+" y: "+y+" imageWidth: "+imageWidth+" yOffset: "+yOffset);
        
        for (int i = 0; i < MARKER_WIDTH; i++) {
        	int xValue = x + i * imageWidth;
        	image.setElem(xValue, markerRGB);
        	//image.setElem(y + i * imageWidth, markerRGB); --> Tried adding y, same as x. But the mark on Y-axis is gone.
        	//image.setElem(i, markerRGB); //--> Tried removed yOffset. But the mark on Y-axis is gone.
        	int yValue = i + yOffset;
            image.setElem(yValue, markerRGB);
            
        	LOG.info(" Values in loop x= "+i+" (x + i * imageWidth)= "+xValue+" (i + yOffset)= "+yValue);
            
        	//image.setElem(y + i * imageHeight, markerRGB);
        }
    }
    
    /*private static void mark(final DataBuffer image, final int x, final int y, final int imageWidth, final int markerRGB) {
        final int yOffset = y * imageWidth;
        for (int i = 0; i < MARKER_WIDTH; i++) {
        	image.setElem(x + i * imageWidth, markerRGB);
        	image.setElem(i + yOffset, markerRGB);
        }
    }*/

    
    private List<HighlightingText> extendDiffArea(final int x, final int y, final int resultImgWidth, HighlightingText highObj) {
    	
    	/*if (diffCalculator.differencesFound()) {
            diffAreaX1 = x;
            diffAreaY1 = y;
        }*/
        //diffAreaX1 = Math.min(diffAreaX1, x);
    	if(diffAreaX1 == 0 || diffAreaX1 > x) {
    		diffAreaX1 = x;
    	}
    	highObj.setMarkAreaX(diffAreaX1);
        
    	diffAreaX2 = Math.max(diffAreaX2, x);
        highObj.setMarkAreaWidth(diffAreaX2);
        //diffAreaY1 = Math.min(diffAreaY1, y);
        
        if(diffAreaY1 == 0 || diffAreaY1 > y) {
        	diffAreaY1 = y;
        }
        highObj.setMarkAreaY(diffAreaY1);
        
        diffAreaY2 = Math.max(diffAreaY2, y);
        highObj.setMarkAreaHeight(diffAreaY2);
        
        //highObj.setResultImageWdth(resultImgWidth);
    	
        
        listOfMarkObj.add(highObj);
        LOG.info("Differences found at MarkAreaX, MarkAreaWdth, MarkAreaY, MarkAreaHeight { diffAreaX1: {}, diffAreaX2: {}, diffAreaY1: {}, diffAreaY2: {} }", diffAreaX1, diffAreaX2, diffAreaY1, diffAreaY2);
        return listOfMarkObj;
    }
    
    /*private void highlightDiffArea(final int x, final int y, final int imageWidth, final int imageHeight) {
    	if (!diffCalculator.differencesFound()) {
    		markAreaX = x;
    		markAreaY = y;
        }
        markAreaX = Math.min(markAreaX, x);
        markAreaY = Math.min(markAreaY, y);
        markAreaWidth = Math.max(markAreaWidth, imageWidth);
        markAreaHeight = Math.max(markAreaHeight, imageHeight);
        LOG.info("Values of markAreaX: {}, markAreaY: {}, markAreaWidth: {}, markAreaHeight: {} ",markAreaX,markAreaY,markAreaWidth,markAreaHeight);
    }*/

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
