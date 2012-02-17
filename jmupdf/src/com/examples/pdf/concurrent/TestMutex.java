package com.examples.pdf.concurrent;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.JmuPdf;
import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.pdf.PdfDocument;

public class TestMutex {

	public static void main(String[] args) {

		// Limit each opened doc to a max of 20mb
		final int maxMemory = 20;

//		PdfDocument pdfDoc = null;
//		try {
//			pdfDoc = new PdfDocument("c:\\tmp\\test-5.pdf", maxMemory);
//		} catch (DocException e) {
//		} catch (DocSecurityException e) {
//			
//		}
//
//		PageRenderer r1 = new PageRenderer(3f, Page.PAGE_ROTATE_AUTO, PageRenderer.IMAGE_TYPE_RGB);
//		PageRenderer r2 = new PageRenderer(3f, Page.PAGE_ROTATE_AUTO, PageRenderer.IMAGE_TYPE_GRAY);
//		PageRenderer r3 = new PageRenderer(3f, Page.PAGE_ROTATE_AUTO, PageRenderer.IMAGE_TYPE_RGB);
//
//		r1.setPage(pdfDoc.getPage(1));
//		r2.setPage(pdfDoc.getPage(8));
//		r3.setPage(pdfDoc.getPage(9));
//
//		r1.render(false);
//		r2.render(false);
//		r3.render(false);
//
//		while (!r1.isPageRendered());
//		while (!r2.isPageRendered());
//		while (!r3.isPageRendered());
//		
//		r1.dispose();
//		r2.dispose();
//		r3.dispose();
//		
//		pdfDoc.dispose();

		/* #1 */
		Runnable r1 = new Runnable() {
			public void run() {
				PdfDocument pdfDoc;
				try {
					pdfDoc = new PdfDocument("c:\\tmp\\test-0.pdf", maxMemory); 
					for(int i=0; i < pdfDoc.getPageCount(); i++) {
						pdfDoc.saveAsTif((i+1),							// Page number 
								"c:\\tmp\\test-0.tif", 					// TIF file
								3f, 									// Zoom
								ImageType.IMAGE_TYPE_RGB,				// Color space
								PdfDocument.TIF_COMPRESSION_LZW,		// Compression
								PdfDocument.TIF_DATA_APPEND,			// File mode
								75);									// Quality. Only for JPEG & ZLIB compressions
					}
					pdfDoc.dispose();
				} catch (DocException e) {
				} catch (DocSecurityException e) {
				}
			}
		};
		
		/* #2 */
		Runnable r2 = new Runnable() {
			public void run() {
				PdfDocument pdfDoc;
				try {
					pdfDoc = new PdfDocument("c:\\tmp\\test-1.pdf", maxMemory);
					for(int i=0; i < pdfDoc.getPageCount(); i++) {
						pdfDoc.saveAsTif((i+1),							// Page number 
								"c:\\tmp\\test-1.tif", 					// TIF file
								3f, 									// Zoom
								ImageType.IMAGE_TYPE_RGB,				// Color space
								PdfDocument.TIF_COMPRESSION_LZW,		// Compression
								PdfDocument.TIF_DATA_APPEND,			// File mode
								75);									// Quality. Only for JPEG & ZLIB compressions						
					}
					pdfDoc.dispose();
				} catch (DocException e) {
				} catch (DocSecurityException e) {
				}
			}
		};
		
		/* #3 */
		Runnable r3 = new Runnable() {
			public void run() {
				PdfDocument pdfDoc;
				try {
					pdfDoc = new PdfDocument("c:\\tmp\\test-6.pdf", maxMemory);
					for(int i=0; i < pdfDoc.getPageCount(); i++) {
						pdfDoc.saveAsTif((i+1),							// Page number 
								"c:\\tmp\\test-6.tif", 					// TIF file
								3f, 									// Zoom
								ImageType.IMAGE_TYPE_RGB,				// Color space
								PdfDocument.TIF_COMPRESSION_LZW,		// Compression
								PdfDocument.TIF_DATA_APPEND,			// File mode
								75);									// Quality. Only for JPEG & ZLIB compressions
					}
					pdfDoc.dispose();
				} catch (DocException e) {
				} catch (DocSecurityException e) {
				}
			}
		};
		
		Thread t1 = new Thread(r1);
		Thread t2 = new Thread(r2);
		Thread t3 = new Thread(r3);
		
		t1.start();
		t2.start();
		t3.start();
	}
}
