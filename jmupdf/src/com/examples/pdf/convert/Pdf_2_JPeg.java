package com.examples.pdf.convert;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a JPEG file
 *
 */
public class Pdf_2_JPeg {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("E:\\development\\indigo\\workspace_jmupdf\\pdf_docs\\itextinaction.pdf", "");

			/*
			 * Create JPeg file
			 */
			pdfDoc.saveAsJPeg(
					1,								// Page number 
					"e:\\tmp\\test1.jpg",			// JPEG file
					1f, 							// Zoom
					PdfDocument.IMAGE_TYPE_RGB,		// Color space. Only RGB & Gray.
					75);							// Quality.

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
