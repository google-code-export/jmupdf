package com.examples.pdf.convert;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a multi page tif file
 *
 */
public class Pdf_2_Tif_MultiPage {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("E:\\development\\indigo\\workspace_jmupdf\\pdf_docs\\itextinaction.pdf", "");

			/*
			 * Loop through all pages and write to tif file
			 */
			for(int i=0; i < pdfDoc.getPageCount(); i++) {
				pdfDoc.saveAsTif((i+1),							// Page number 
						"e:\\tmp\\test4.tif", 					// TIF file
						1f, 									// Zoom
						ImageType.IMAGE_TYPE_RGB,		    	// Color space
						PdfDocument.TIF_COMPRESSION_LZW,		// Compression
						PdfDocument.TIF_DATA_APPEND,			// File mode
						75);									// Quality. Only for JPEG & ZLIB compressions
			}

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
