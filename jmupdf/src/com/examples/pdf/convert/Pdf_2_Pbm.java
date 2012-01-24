package com.examples.pdf.convert;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a PAM file
 *
 */
public class Pdf_2_Pbm {

	public static void main(String[] args) {

		try {

			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("c:\\tmp\\test1.pdf");

			/*
			 * Create JPeg file
			 */
			pdfDoc.saveAsPbm(
					1,									// Page number
					"c:\\tmp\\test1.pbm",				// PBM file
					1f);								// Zoom

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
