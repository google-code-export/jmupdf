package com.examples.pdf.convert;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a BufferedImage from a PdfPage object
 * 
 */
public class RenderTestC {

	public static void main(String[] args) {

		try {
			
			/* Open document */
			PdfDocument doc = new PdfDocument("C:\\tmp\\test-aa.pdf", "");			
			
			/* Get page object */
			Page page = new Page(doc, 1);
			page.setAntiAliasLevel(0);
			
			/* Set parameter values */
			String file = "c:\\tmp\\test-aa-0.png";
			int rotate = Page.PAGE_ROTATE_NONE;
			float zoom = 3f;
			ImageType type = ImageType.IMAGE_TYPE_ARGB;
			float gamma = 1f;
			
			/* Create png file */
			page.saveAsPng(file, rotate, zoom, type, gamma);

			/* dispose native resources */
			page.dispose();
			doc.dispose();
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
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
