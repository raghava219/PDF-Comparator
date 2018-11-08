package com.quest.custom.compare;

import de.redsix.pdfcompare.PdfComparator;

public class CustomPDFCompare {
	

	public static void main(String[] args) {
		
		try {
			
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");	
			
		/*String actualFile = "./pdf_files/client/Automation_025.pdf";
		String expectedFile = "./pdf_files/client/Automation_027.PDF";
		String diffOutputFile = "./pdf_files/client/clientDiffOutputFile_6.PDF";*/
		
		String actualFile = "./pdf_files/actual.pdf";
		String expectedFile = "./pdf_files/expected.PDF";
		String diffOutputFile = "./pdf_files/diffOutputFile_15.PDF";
		
		

		PdfComparator pdfCompObj =	new PdfComparator(expectedFile, actualFile);
		
		boolean outputTrue = pdfCompObj.compare().writeTo(diffOutputFile);
		
/*		PDDocument document = PDDocument.load(new File(actualPDFFile));
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		for (int page = 0; page < document.getNumberOfPages(); ++page)
		{ 
		    //BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
		    
	        BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, DPI);
	        PDRectangle mediaBox = document.getPage(page).getMediaBox();


		    // suffix in filename will be used as the file format
	        return new ImageWithDimension(bufferedImage, mediaBox.getWidth(), mediaBox.getHeight());
		}
		
		document.close();
				
*/		
        
		
		if(!outputTrue) {
			System.out.println("PDF is created");
		} else {
			System.out.println("PDF is not created");
		}
		
		} catch(Exception exe) {
			exe.printStackTrace();
			//System.gc();
		}
				
	}
	
	
/*    public static ImageWithDimension renderPageAsImage(final PDDocument document, final PDFRenderer expectedPdfRenderer, final int pageIndex)
            throws IOException {
        final BufferedImage bufferedImage = expectedPdfRenderer.renderImageWithDPI(pageIndex, DPI);
        final PDRectangle mediaBox = document.getPage(pageIndex).getMediaBox();
        return new ImageWithDimension(bufferedImage, mediaBox.getWidth(), mediaBox.getHeight());
    }
*/
	
	
	
}
