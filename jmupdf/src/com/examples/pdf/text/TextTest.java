package com.examples.pdf.text;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;

/**
 * Sample get page text
 * 
 * @author Pedro J Rivera
 *
 */
public class TextTest {

	public static void main(String[] args) {

		try {
			
			/* Open document */ 
			PdfDocument doc = new PdfDocument("d:\\tmp\\iTextinAction.pdf");

			/* Get page */
			Page page = new Page(doc, 5);
			
			/* Get text, if any */
			String text = page.getText();
			
			/* Display out data */
			System.out.println(text);
			
			/* Dispose */
			page.dispose();
			doc.dispose();
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		}
		
	}
	
}
