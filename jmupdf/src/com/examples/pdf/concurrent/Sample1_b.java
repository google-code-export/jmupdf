package com.examples.pdf.concurrent;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;

public class Sample1_b implements Runnable {
	String doc;
	int n;
	
	public Sample1_b(String doc, int n) {		
		this.doc = doc;
		this.n = n;
	}
	
	public void run() {		
		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdf = new PdfDocument(doc + n + ".pdf");

			for (int i = 0; i<pdf.getPageCount(); i++) {
				if (!pdf.saveAsTif(i+1, 
						"c:\\tmp\\images\\test-" + n + ".tif", 
						2f, 			// Zoom
						ImageType.IMAGE_TYPE_RGB, 
						PdfDocument.TIF_COMPRESSION_DEFLATE, 
						PdfDocument.TIF_DATA_APPEND, 
						0)) {
					System.out.println("Could not save page.");
				}
			}

			/*
			 * Dispose
			 */
			pdf.dispose();
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		}			
	}		
}
