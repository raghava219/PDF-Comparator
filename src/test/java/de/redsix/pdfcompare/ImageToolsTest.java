package de.redsix.pdfcompare;

import static de.redsix.pdfcompare.ImageTools.EXCLUDED_BACKGROUND_RGB;
import static de.redsix.pdfcompare.ImageTools.normalElement;
import static de.redsix.pdfcompare.ImageTools.fadeExclusion;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.*;

import org.junit.jupiter.api.Test;

public class ImageToolsTest {

    @Test
    public void normalElementMakesDarkerPixelsLighter() {
        int actual = normalElement(new Color(0, 0, 0).getRGB());
        assertThat(actual, is(new Color(153, 153, 153).getRGB()));
        actual = normalElement(new Color(255, 255, 255).getRGB());
        assertThat(actual, is(new Color(255, 255, 255).getRGB()));
        actual = normalElement(new Color(0, 0, 255).getRGB());
        assertThat(actual, is(new Color(153, 153, 255).getRGB()));
        actual = normalElement(new Color(180, 60, 130).getRGB());
        assertThat(actual, is(new Color(225, 177, 205).getRGB()));
    }

    @Test
    public void normalExclusionOfDarkPixlesIsARegularnormal() {
        int actual = fadeExclusion(new Color(0, 0, 0).getRGB());
        assertThat(actual, is(new Color(153, 153, 153).getRGB()));
    }

    @Test
    public void normalExclusionOfLightPixlesMakesItYellow() {
        int actual = fadeExclusion(new Color(250, 250, 250).getRGB());
        assertThat(actual, is(EXCLUDED_BACKGROUND_RGB));
    }
}