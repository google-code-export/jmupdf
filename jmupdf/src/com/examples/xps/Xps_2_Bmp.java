package com.examples.xps;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;
import com.jmupdf.xps.XpsDocument;

/**
 * Create a BMP file
 *
 */
public class Xps_2_Bmp {

	public static void main(String[] args) {

		try {

			/*
			 * Open document
			 */
			XpsDocument pdfDoc = new XpsDocument("c:\\tmp\\test1.xps");

			/*
			 * Create JPeg file
			 */
			pdfDoc.saveAsBmp(
					1,									// Page number
					"c:\\tmp\\xps-test1.bmp",			// bmp file
					2f,									// Zoom	
					PdfDocument.IMAGE_TYPE_BINARY);		// Color space. Only RGB & Gray.

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
