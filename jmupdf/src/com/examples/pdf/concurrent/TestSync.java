package com.examples.pdf.concurrent;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.pdf.PdfDocument;

public class TestSync {

	public static void main(String[] args) {

		/* Set memory limit to 60mb per document*/
		int maxMemory = 60;

		/* Open doc */
		PdfDocument pdfDoc = null;
		try {
			pdfDoc = new PdfDocument("c:\\tmp\\test-5.pdf", maxMemory);
		} catch (DocException e) {
			System.out.println("oops!!");
			return;
		} catch (DocSecurityException e) { 
			System.out.println("oops!!");
			return;
		}

		/* Create renderer */
		PageRenderer r1 = new PageRenderer(2f, Page.PAGE_ROTATE_AUTO, ImageType.IMAGE_TYPE_RGB);
		PageRenderer r2 = new PageRenderer(2f, Page.PAGE_ROTATE_AUTO, ImageType.IMAGE_TYPE_GRAY);
		PageRenderer r3 = new PageRenderer(2f, Page.PAGE_ROTATE_AUTO, ImageType.IMAGE_TYPE_RGB);

		/* Set pages to render */
		r1.setPage(pdfDoc.getPage(1));
		r2.setPage(pdfDoc.getPage(8));
		r3.setPage(pdfDoc.getPage(9));

		/* Submit with no wait */
		/* This is to test object sycronization. The library has been
		   fine tuned to always sync on the Document instance. For example
		   the rendering always goes through the document object which 
		   syncs calls to JNI rendering engine forcing pages to render one at 
		   a time within the same document.
		   To obtain true concurrent processing different Document objects must
		   be instantiated. */
		r1.render(false);
		r2.render(false);
		r3.render(false);

		while (!r1.isPageRendered());
		while (!r2.isPageRendered());
		while (!r3.isPageRendered());
		
		r1.dispose();
		r2.dispose();
		r3.dispose();
		
		pdfDoc.dispose();
	}
}
