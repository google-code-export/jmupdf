package com.examples.pdf.text;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Page;
import com.jmupdf.page.PageText;
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
			page = doc.getPage(5);
			
			/* Get text, if any */
			String text = PageText.getStringFromArray(page.getTextSpan(page.getBoundBox()));
			
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
