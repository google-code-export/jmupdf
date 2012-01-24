package com.examples.pdf.links;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageLinks;
import com.jmupdf.pdf.PdfDocument;

/**
 * Sample get page links
 * 
 * @author Pedro J Rivera
 *
 */
public class Links {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("E:\\development\\indigo\\workspace_jmupdf\\pdf_docs\\itextinaction.pdf", "");
		
			/*
			 * Get page
			 */
			Page page = pdfDoc.getPage(8);
			
			/*
			 * Get links, if any
			 */
			PageLinks[] links = page.getLinks(null);
			
			/*
			 * List out data
			 */
			for (int i=0; i<links.length; i++) {
				if (links[i].getType() == 0)
					System.out.println("Page #: " + links[i].getDestination());
				else
					System.out.println("URL: " + links[i].getDestination());
			}

			/*
			 * Dispose
			 */
			pdfDoc.dispose();
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		}
		
	}
	
}
