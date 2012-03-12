package com.examples.pdf.text;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
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
		PdfDocument doc = null;
		Page page = null;
		
		try {
			
			/* Open document */ 
			doc = new PdfDocument("d:\\tmp\\iTextinAction.pdf");

			/* Get page */
			page = new Page(doc, 5);
			
			/* Get text, if any */
			String text = page.getText();
			
			/* Display out data */
			System.out.println(text);
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (PageException e) {
			e.printStackTrace();
		} finally {
			if (page != null)
				page.dispose();
			if (doc != null)
				doc.dispose();
		}
		
	}
	
}
