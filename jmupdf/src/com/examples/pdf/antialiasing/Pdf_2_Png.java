package com.examples.pdf.antialiasing;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a PNG file
 *
 */
public class Pdf_2_Png {

	public static void main(String[] args) {

		try {

			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("c:\\tmp\\test1.pdf");
			
			/*
			 * Set anti alias level
			 */
			pdfDoc.setAntiAliasLevel(0);
			
			/*
			 * Create JPeg file
			 */
			pdfDoc.saveAsPng(
					1,									// Page number
					"c:\\tmp\\test1.png",				// PNG file
					6f,									// Zoom								
					PdfDocument.IMAGE_TYPE_ARGB);		// Color space. Only RGB, ARGB & Gray.
			
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
