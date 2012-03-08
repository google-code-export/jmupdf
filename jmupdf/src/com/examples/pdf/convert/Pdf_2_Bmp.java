package com.examples.pdf.convert;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a BMP file
 *
 */
public class Pdf_2_Bmp {

	public static void main(String[] args) {

		try {

			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("c:\\tmp\\test-1.pdf");

			/*
			 * Create JPeg file
			 */
			pdfDoc.saveAsBmp(
					1,									// Page number
					"c:\\tmp\\pdf-test1.bmp",			// bmp file
					Page.PAGE_ROTATE_90, 				// rotate
					3f,									// Zoom	
					ImageType.IMAGE_TYPE_BINARY,		// Color space. Only RGB & Gray.
					1f);								// Gamma		

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
