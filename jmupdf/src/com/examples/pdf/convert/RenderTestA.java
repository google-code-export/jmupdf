package com.examples.pdf.convert;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PagePixels;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a BufferedImage from a PdfPage object
 * 
 */
public class RenderTestA {

	public static void main(String[] args) {
		PdfDocument doc = null;
		Page page = null;
		PagePixels pix = null;
		
		try {
			
			/* Open document */
			doc = new PdfDocument("C:\\Users\\Pedro\\Downloads\\test-2.pdf", "");
			
			/* Get page object */
			page = new Page(doc, 1);
			
			/* Get pixel object */
			pix = new PagePixels(page);
			pix.setRotation(Page.PAGE_ROTATE_NONE);			
			pix.drawPage(null, pix.getX0(),pix.getY0(), pix.getX1(), pix.getY1());

			/* Save to disk for review */
			ImageIO.write(pix.getImage(), "PNG", new File("C:\\Users\\Pedro\\Downloads\\test-1.jpg"));

		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (PageException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pix != null)
				pix.dispose();
			if (page != null)
				page.dispose();
			if (doc != null)
				doc.dispose();
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
