package de.redsix.pdfcompare;

public interface ResultCollector {

    void addPage(PageDiffCalculator diffCalculator, int pageIndex,
            ImageWithDimension expectedImage, ImageWithDimension actualImage, ImageWithDimension diffImage);

    /*void addPage(PageDiffCalculator diffCalculator, int pageIndex,
            ImageWithDimension expectedImage, ImageWithDimension actualImage);*/

    
    void noPagesFound();

    default void done() {}
}
