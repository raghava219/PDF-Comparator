/*
 * Copyright 2016 Malte Finsterwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.redsix.pdfcompare;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * A CompareResult tracks the differences, that result from a comparison.
 * The CompareResult only stores the diffImages, for lower memory consumption.
 * If you also need the expected and actual Image, please use the Subclass
 * {@link CompareResultWithExpectedAndActual}
 */
public class CompareResult implements ResultCollector {

    protected final Map<Integer, ImageWithDimension> diffImages = new TreeMap<>();
    protected final Map<Integer, ImageWithDimension> actualImages = new TreeMap<>();
    protected final Map<Integer, ImageWithDimension> expectedImages = new TreeMap<>();
    protected boolean isEqual = true;
    protected boolean hasDifferenceInExclusion = false;
    private boolean expectedOnly;
    private boolean actualOnly;

    /**
     * Write the result Pdf to a file. Warning: This will remove the diffImages from memory!
     * Writing can only be done once.
     *
     * @param filename without pdf-Extension
     * @return a boolean indicating, whether the comparison is equal. When true, the files are equal.
     */
    public boolean writeTo(String filename) {
        if (!hasImages()) {
            return isEqual;
        }
        try (PDDocument document = new PDDocument()) {
            addImagesToDocument(document);
            document.save(filename + ".pdf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return isEqual;
    }

    protected boolean hasImages() {
        return !diffImages.isEmpty();
    }

    protected void addImagesToDocument(final PDDocument document) throws IOException {
        addImagesToDocument(document, diffImages);
    }

    protected void addImagesToDocument(final PDDocument document, final Map<Integer, ImageWithDimension> images) throws IOException {
        final Iterator<Entry<Integer, ImageWithDimension>> iterator = images.entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<Integer, ImageWithDimension> entry = iterator.next();
            if (!keepImages()) {
                iterator.remove();
            }
            Integer index = entry.getKey();
            //addPageToDocument(document, entry.getValue(), actualImages.get(index), expectedImages.get(index));
            //addPageToDocument(document, entry.getValue(), expectedImages.get(index));
            addPageToDocument(document, actualImages.get(index), expectedImages.get(index));
            //addPageToDocument(document, entry.getValue());
        }
    }

    
    protected void addPageToDocument(final PDDocument document, final ImageWithDimension diffImage) throws IOException {
        PDPage page = new PDPage(new PDRectangle(diffImage.width, diffImage.height));
        document.addPage(page);
        final PDImageXObject diffXImage = LosslessFactory.createFromImage(document, diffImage.bufferedImage);
/*      final PDImageXObject actualXObject = LosslessFactory.createFromImage(document, actualImage.bufferedImage);
        final PDImageXObject expectedXObject = LosslessFactory.createFromImage(document, expectedImage.bufferedImage);
*/        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        	int x = (int)page.getCropBox().getWidth();
        	//System.out.println("X value "+x);
  	      	int y = (int)page.getCropBox().getHeight();
  	        //System.out.println("Y value "+y);
  	        contentStream.drawImage(diffXImage, 0, 0, x, y);
/*  	    contentStream.setLineWidth(0.5F); 
 	      	contentStream.moveTo(x, 2);
 	        contentStream.lineTo(x, y);
  	        contentStream.drawImage(expectedXObject, x+2, 0, x, y);
			contentStream.moveTo(x+x, 2);
	        contentStream.lineTo(x+x, y+y);
	        contentStream.drawImage(expectedXObject, x+x+2, 0, x, y);
 	        contentStream.stroke();*/
        }
    }
    
    protected void addPageToDocument(final PDDocument document, final ImageWithDimension actualImage, final ImageWithDimension expectedImage) throws IOException {
        PDPage page = new PDPage(new PDRectangle(actualImage.width, actualImage.height));
        document.addPage(page);
        //final PDImageXObject diffXImage = LosslessFactory.createFromImage(document, diffImage.bufferedImage);
        final PDImageXObject actualXObject = LosslessFactory.createFromImage(document, actualImage.bufferedImage);
        final PDImageXObject expectedXObject = LosslessFactory.createFromImage(document, expectedImage.bufferedImage);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        	int x = (int)page.getCropBox().getWidth()/2;
        	//System.out.println("X value "+x);
  	      	int y = (int)page.getCropBox().getHeight();
  	        //System.out.println("Y value "+y);
  	        contentStream.drawImage(actualXObject, 0, 0, x, y);
  	        contentStream.setLineWidth(0.5F); 
 	      	contentStream.moveTo(x, 2);
 	        contentStream.lineTo(x, y);
  	        contentStream.drawImage(expectedXObject, x+2, 0, x, y);
  	        /*contentStream.moveTo(x+x, 2);
	        contentStream.lineTo(x+x, y+y);
	        contentStream.drawImage(expectedXObject, x+x+2, 0, x, y);*/
 	        contentStream.stroke();
        }
    }

    
    protected void addPageToDocument(final PDDocument document, final ImageWithDimension diffImage, final ImageWithDimension actualImage, final ImageWithDimension expectedImage) throws IOException {
        PDPage page = new PDPage(new PDRectangle(diffImage.width, diffImage.height));
        document.addPage(page);
        final PDImageXObject diffXImage = LosslessFactory.createFromImage(document, diffImage.bufferedImage);
        final PDImageXObject actualXObject = LosslessFactory.createFromImage(document, actualImage.bufferedImage);
        final PDImageXObject expectedXObject = LosslessFactory.createFromImage(document, expectedImage.bufferedImage);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        	int x = (int)page.getCropBox().getWidth()/3;
        	System.out.println("X value "+x);
  	      	int y = (int)page.getCropBox().getHeight();
  	        System.out.println("Y value "+y);
  	        contentStream.drawImage(diffXImage, 0, 0, x, y);
  	        contentStream.setLineWidth(0.5F); 
 	      	contentStream.moveTo(x, 2);
 	        contentStream.lineTo(x, y);
  	        contentStream.drawImage(actualXObject, x+2, 0, x, y);
  	        contentStream.moveTo(x+x, 2);
	        contentStream.lineTo(x+x, y+y);
	        contentStream.drawImage(expectedXObject, x+x+2, 0, x, y);
 	        contentStream.stroke();
        }
    }


    
    protected boolean keepImages() {
        return false;
    }

    /*@Override
    public synchronized void addPage(final PageDiffCalculator diffCalculator, final int pageIndex,
            final ImageWithDimension expectedImage, final ImageWithDimension actualImage) {
        Objects.requireNonNull(expectedImage, "expectedImage is null");
        Objects.requireNonNull(actualImage, "actualImage is null");
        //Objects.requireNonNull(diffImage, "diffImage is null");
        this.hasDifferenceInExclusion |= diffCalculator.differencesFoundInExclusion();
        if (diffCalculator.differencesFound()) {
            isEqual = false;
        }
        //diffImages.put(pageIndex, diffImage);
        actualImages.put(pageIndex, actualImage);
        expectedImages.put(pageIndex, expectedImage);
    }*/
    
    @Override
    public synchronized void addPage(final PageDiffCalculator diffCalculator, final int pageIndex,
            final ImageWithDimension expectedImage, final ImageWithDimension actualImage, final ImageWithDimension diffImage) {
        Objects.requireNonNull(expectedImage, "expectedImage is null");
        Objects.requireNonNull(actualImage, "actualImage is null");
        Objects.requireNonNull(diffImage, "diffImage is null");
        this.hasDifferenceInExclusion |= diffCalculator.differencesFoundInExclusion();
        if (diffCalculator.differencesFound()) {
            isEqual = false;
        }
        diffImages.put(pageIndex, diffImage);
        actualImages.put(pageIndex, actualImage);
        expectedImages.put(pageIndex, expectedImage);
    }

    @Override
    public void noPagesFound() {
        isEqual = false;
    }

    public boolean isEqual() {
        return isEqual;
    }

    public boolean isNotEqual() {
        return !isEqual;
    }

    public boolean hasDifferenceInExclusion() {
        return hasDifferenceInExclusion;
    }

    public boolean hasOnlyExpected() {
        return expectedOnly;
    }

    public boolean hasOnlyActual() {
        return actualOnly;
    }

    public boolean hasOnlyOneDoc() {
        return expectedOnly || actualOnly;
    }

    public synchronized int getNumberOfPages() {
        if (!hasImages()) {
            return 0;
        }
        return Collections.max(diffImages.keySet());
    }

    public void expectedOnly() {
        this.expectedOnly = true;
    }

    public void actualOnly() {
        this.actualOnly = true;
    }
}
