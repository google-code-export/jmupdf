package com.examples.xps;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;
import com.jmupdf.xps.XpsDocument;

/**
 * Create a multi page tif file
 *
 */
public class Xps_2_Tif_MultiPage {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			XpsDocument xps = new XpsDocument("d:\\HyperV_Deploy.xps");

			/*
			 * Loop through all pages and write to tif file
			 */
			for(int i=0; i < xps.getPageCount(); i++) {
				xps.saveAsTif((i+1),							// Page number 
						"d:\\test1.tif", 						// TIF file
						1f, 									// Zoom
						ImageType.IMAGE_TYPE_RGB,				// Color space
						PdfDocument.TIF_COMPRESSION_LZW,		// Compression
						PdfDocument.TIF_DATA_APPEND,			// File mode
						0);										// Quality. Only for JPEG & ZLIB compressions
			}

			/*
			 * Dispose
			 */
			xps.dispose();
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		}
		
	}

}
