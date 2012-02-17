package com.examples.pdf.convert;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PagePixels;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a BufferedImage from a PdfPage object
 * 
 */
public class Pdf_2_BufferedImage_Pixels {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("E:\\development\\indigo\\workspace_jmupdf-old\\pdf_tests\\test05.pdf", "");
			//PdfDocument pdfDoc = new PdfDocument("E:\\development\\indigo\\workspace_jmupdf-old\\pdf_tests\\iTextAction.pdf", "");
			
			/*
			 * Get page object
			 */
			Page page = pdfDoc.getPage(1);

			/*
			 * Get pixel object
			 */
			PagePixels pp = new PagePixels(page);
			//pp.setColor(PagePixels.IMAGE_TYPE_RGB);
			pp.setRotate(Page.PAGE_ROTATE_AUTO);			
			pp.drawPage(509f,569f, 564f, 756f);
			//pp.drawPage(175, 385, 200, 500);
			//pp.drawPage(385, 465, 500, 500);
			
			/*
			 * Get rendering object
			 */
			//PageRenderer render = new PageRenderer(page, 1f, Page.PAGE_ROTATE_AUTO, PageRenderer.IMAGE_TYPE_RGB);
			//render.render(true);	
			
			/*
			 * Save to disk for review
			 */
			ImageIO.write(pp.getImage(), "PNG", new File("e:\\tmp\\test-1.jpg"));
			
			/*
			 * Dispose
			 */
			//render.dispose();
			pp.dispose();
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
