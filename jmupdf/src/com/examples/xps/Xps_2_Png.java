package com.examples.xps;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.xps.XpsDocument;

/**
 * Create a PNG file
 *
 */
public class Xps_2_Png {

	public static void main(String[] args) {

		try {

			/*
			 * Open document
			 */
			XpsDocument pdfDoc = new XpsDocument("c:\\tmp\\test1.xps");

			/*
			 * Create JPeg file
			 */
			pdfDoc.saveAsPng(
					1,									// Page number
					"c:\\tmp\\xps-test1.png",			// PNG file
					1f,									// Zoom	
					ImageType.IMAGE_TYPE_RGB);		// Color space. Only RGB, ARGB & Gray.

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
