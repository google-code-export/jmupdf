package com.examples.xps;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.xps.XpsDocument;

/**
 * Create a BufferedImage from a PdfPage object
 * 
 */
public class Xps_2_BufferedImage {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open XPS document
			 */
			XpsDocument xpsDoc = new XpsDocument("d:\\test01.xps");
			Page page = new Page(xpsDoc, 1);

			/*
			 * Get rendering object
			 */
			PageRenderer render = new PageRenderer(page, 1f, Page.PAGE_ROTATE_NONE, ImageType.IMAGE_TYPE_ARGB);
			render.render(true);	
			
			/*
			 * Save to disk for review
			 */
			ImageIO.write(render.getImage(), "PNG", new File("d:\\test_xps.png"));

			render.dispose();
			xpsDoc.dispose();
			
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
