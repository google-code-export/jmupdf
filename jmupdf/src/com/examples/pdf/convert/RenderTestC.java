package com.examples.pdf.convert;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a BufferedImage from a PdfPage object
 * 
 */
public class RenderTestC {

	public static void main(String[] args) {
		PdfDocument doc = null;
		Page page = null;

		try {
			
			/* Open document */
			doc = new PdfDocument("C:\\tmp\\test-aa.pdf", "");			
			
			/* Get page object */
			page = doc.getPage(1);
			page.setAntiAliasLevel(0);
			
			/* Set parameter values */
			String file = "c:\\tmp\\test-aa-0.png";
			int rotate = Page.PAGE_ROTATE_NONE;
			float zoom = 3f;
			ImageType type = ImageType.IMAGE_TYPE_ARGB;
			float gamma = 1f;
			
			/* Create png file */
			page.saveAsPng(file, rotate, zoom, type, gamma);

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
	
    /**
     * Print test messages
     * @param text
     */
    protected static void log(String text) {
    	System.out.println(text);
    }
    
}
