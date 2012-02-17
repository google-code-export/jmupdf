package com.examples.pdf.antialiasing;

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
public class Pdf_2_BufferedImage {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("c:\\tmp\\test1.pdf", "");
			
			/*
			 * Get page object
			 */
			Page page = pdfDoc.getPage(5);

			/*
			 * Get rendering object
			 */
			PageRenderer render = new PageRenderer(page, 4f, Page.PAGE_ROTATE_AUTO, ImageType.IMAGE_TYPE_RGB);
			
			/*
			 * turn off anti-aliasing
			 */
			render.setAntiAliasLevel(0);
			
			/*
			 * Render 
			 */
			render.render(true);	
			
			/*
			 * Save to disk for review
			 */
			ImageIO.write(render.getImage(), "PNG", new File("c:\\tmp\\test1.png"));
			
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
