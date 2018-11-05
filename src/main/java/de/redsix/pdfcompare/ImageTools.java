package de.redsix.pdfcompare;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageTools {

	private static final Logger LOG = LoggerFactory.getLogger(ImageTools.class);
    public static final int EXCLUDED_BACKGROUND_RGB = new Color(255, 255, 100).getRGB();

    public static BufferedImage blankImage(final BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(Color.white);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        return image;
    }

    public static int normalElement(final int i) {
        final Color color = new Color(i);
        int normalColor = new Color(color.getRed(), color.getGreen(), color.getBlue()).getRGB();
        //LOG.info("normalColor :"+color.getRed() +","+ color.getGreen() +","+ color.getBlue());
        return normalColor;
    }

    public static int fadeExclusion(final int i) {
        final Color color = new Color(i);
        if (color.getRed() > 245 && color.getGreen() > 245 && color.getBlue() > 245) {
            return EXCLUDED_BACKGROUND_RGB;
        }
        return normalElement(i);
    }

    private static int fade(final int i) {
        return i + ((255 - i) * 3 / 5);
    }

    public static BufferedImage deepCopy(BufferedImage image) {
    	BufferedImage bufferdImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
        LOG.info("Deep Copy is completed");   	
        return bufferdImage;
    }
}
