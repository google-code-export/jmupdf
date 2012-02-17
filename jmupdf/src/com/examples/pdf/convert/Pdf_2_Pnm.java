package com.examples.pdf.convert;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a PAM file
 *
 */
public class Pdf_2_Pnm {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("c:\\temp\\tiff6.pdf");

			/*
			 * Create JPeg file
			 */
			pdfDoc.saveAsPnm(
					1,									// Page number
					"c:\\temp\\test1.pnm",				// PNM file
					1f,									// Zoom		
					ImageType.IMAGE_TYPE_RGB);		// Color space. Only RGB & Gray.

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
