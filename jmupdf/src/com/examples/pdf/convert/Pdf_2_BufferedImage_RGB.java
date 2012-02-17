package com.examples.pdf.convert;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a BufferedImage from a PdfPage object
 * 
 */
public class Pdf_2_BufferedImage_RGB {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("E:\\development\\indigo\\workspace_jmupdf-old\\pdf_docs\\itextinaction.pdf", "");
			
			/*
			 * Get page object
			 */
			Page page = pdfDoc.getPage(1);

			/*
			 * Get rendering object
			 */
			PageRenderer render = new PageRenderer(page, 1f, Page.PAGE_ROTATE_AUTO, ImageType.IMAGE_TYPE_RGB);
			render.render(true);	
			
			/*
			 * Save to disk for review
			 */
			ImageIO.write(render.getImage(), "JPEG", new File("e:\\tmp\\test-1.jpg"));
			
			/*
			 * Dispose
			 */
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
