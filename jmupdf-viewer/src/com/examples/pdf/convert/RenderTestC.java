package com.examples.pdf.convert;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PageRendererOptions;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a BufferedImage from a PdfPage object
 * 
 */
public class RenderTestC {

	public static void main(String[] args) {
		PdfDocument doc = null;
		Page page = null;

		try {
			
			/* Open document */
			doc = new PdfDocument("D:\\tmp\\test1.pdf", "");			
			
			/* Get page object */
			page = doc.getPage(2);
			
			/* Set file */			
			String file = "D:\\tmp\\test1.png";
			
			/* Set rendering options */
            PageRendererOptions options = page.getRenderingOptions();
            options.setRotate(Page.PAGE_ROTATE_NONE);
            options.setZoom(3f);
            options.setImageType(ImageType.IMAGE_TYPE_ARGB);
            options.setGamma(1f);
            options.setAntiAlias(8);

			/* Create png file */
			page.saveAsImage(file, options);

		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (PageException e) {
			e.printStackTrace();
		} finally {
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
