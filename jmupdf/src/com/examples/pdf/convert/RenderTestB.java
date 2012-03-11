package com.examples.pdf.convert;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a PNG file
 *
 */
public class RenderTestB {

	public static void main(String[] args) {

		try {

			/* Open document */
			PdfDocument doc = new PdfDocument("c:\\tmp\\test-1.pdf");

			/* Get page */
			Page page = new Page(doc, 1);
			
			/* Set parameter values */
			String file = "c:\\tmp\\test-1.png";
			int rotate = Page.PAGE_ROTATE_90;
			float zoom = 2f;
			ImageType type = ImageType.IMAGE_TYPE_RGB;
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

}
