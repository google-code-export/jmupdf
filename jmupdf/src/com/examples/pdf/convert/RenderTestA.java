package com.examples.pdf.convert;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PagePixels;
import com.jmupdf.page.PageRect;
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
			doc = new PdfDocument("f:\\tmp\\test1.pdf", "");
			
			/* Get page object */
			page = doc.getPage(1);
			
			/* Get pixel object */
			PageRect bb = page.getBoundBox();
			pix = page.getPagePixels();
			pix.getOptions().setRotate(Page.PAGE_ROTATE_NONE);			
			pix.drawPage(null, bb.getX0(), bb.getY0(), bb.getX1(), bb.getY1());

			/* Save to disk for review */
			ImageIO.write(pix.getImage(), "PNG", new File("f:\\tmp\\test1.png"));

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
