package com.quest.custom.compare;

import java.awt.Color;
import java.io.File;
  
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class sample {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		String path = "C:\\Raghava\\Ultimatix\\SafariBooks\\Development\\Laboratory2\\PDFBoxExamples\\output\\actual.pdf";
		
		  File fileObj = new File(path);
		
	      PDDocument document = PDDocument.load(fileObj);
	        
	      //Retrieving a page of the PDF Document
	      PDPage page = document.getPage(0);
	      
	      PDImageXObject pdImage = PDImageXObject.createFromFile("C:\\Raghava\\Ultimatix\\SafariBooks\\Development\\Laboratory2\\pdfcompare-master\\pdf_files\\Java.png",document);
	      
	      //Instantiating the PDPageContentStream class
	      PDPageContentStream contentStream = new PDPageContentStream(document, page);
	       
	      //Setting the non stroking color
	      //contentStream.setStrokingColor(Color.DARK_GRAY);

	      //Drawing a rectangle 
	      //contentStream.lineTo(250, 250);
	      
	      //contentStream.lineTo(50, 150);
	      
	      //contentStream.lineTo(5000, 15000);
	      
	    //Drawing the image in the PDF document
	      contentStream.drawImage(pdImage, 70, 250);
	      
	      int x = (int)page.getCropBox().getWidth()/2;
	      System.out.println("value of x "+x);
	      int y = (int)page.getCropBox().getHeight();
	      System.out.println("value of y "+y);
	      
	        contentStream.setLineWidth(0.5F); 
	      	contentStream.moveTo(x, 2);
	        contentStream.lineTo(x, y);
	        contentStream.stroke();
	        
	        //contentStream.moveTo(x, 2);
	        //contentStream.drawImage(pdImage, 300, 722); 
	        
	        contentStream.drawImage(pdImage, x+50, y/2); 

	      //Drawing a rectangle
	      //contentStream.fill();

	      System.out.println("line added");

	      //Closing the ContentStream object
	      contentStream.close();

	      //Saving the document
	      //File file1 = new File("C:/PdfBox_Examples/colorbox.pdf");
	      document.save(fileObj);

	      //Closing the document
	      document.close();

		
	}

}

