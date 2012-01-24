package com.examples.pdf.convert;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a BufferedImage from a PdfPage object
 * 
 */
public class Pdf_2_BufferedImage_ARGB {

	public static void main(String[] args) {

		try {

			/* Open document */
			PdfDocument pdfDoc = new PdfDocument("E:\\development\\indigo\\workspace_jmupdf\\pdf_docs\\itextinaction.pdf", "");
			
			/* Create a reusable renderer and set pixel output to ARGB */
			PageRenderer render = new PageRenderer(2f, Page.PAGE_ROTATE_AUTO, PageRenderer.IMAGE_TYPE_ARGB);
			
			/* Render all pages */
			for (int i=0; i<pdfDoc.getPageCount(); i++) {
				render.setPage(pdfDoc.getPage(i+1));
				render.render(true);
				ImageIO.write(render.getImage(), "PNG", new File("e:\\tmp\\test-1-" + i + ".png"));
			}

			/* Dispose */
			render.dispose();
			pdfDoc.dispose();

		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
    /**
     * Print test messages
     * @param text
     */
    protected static void log(String text) {
    	System.out.println(text);
    }
    
}
