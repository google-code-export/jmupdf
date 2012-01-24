package com.examples.pdf.convert;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a PAM file
 *
 */
public class Pdf_2_Pam {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("c:\\temp\\tiff6.pdf");

			/*
			 * Create JPeg file
			 */
			pdfDoc.saveAsPam(
					1,									// Page number
					"c:\\temp\\test1.pam",				// PNG file
					1f,									// Zoom			
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
