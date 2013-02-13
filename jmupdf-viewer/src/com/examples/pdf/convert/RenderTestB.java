package com.examples.pdf.convert;

import java.io.FileOutputStream;
import java.io.IOException;

import com.jmupdf.enums.ImageFormat;
import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PageRendererOptions;
import com.jmupdf.pdf.PdfDocument;

/**
 * Create a PNG file
 *
 */
public class RenderTestB {

	public static void main(String[] args) {
		PdfDocument doc = null;
		Page page = null;
		
		try {

			/* Open document */
			doc = new PdfDocument("f:/tmp/test1.pdf");

			/* Get page */
			page = doc.getPage(1);
			
			/* Set file */
			String file = "f:/tmp/test-png-1.png";
			
			/* Set rendering options */
			PageRendererOptions options = page.getRenderingOptions();
			options.setRotate(Page.PAGE_ROTATE_NONE);
			options.setZoom(2f);
			options.setImageFormat(ImageFormat.FORMAT_PNG);
			options.setImageType(ImageType.IMAGE_TYPE_RGB);
			options.setGamma(1f);
			options.setAntiAlias(8);
			
			/* Create png file */
			page.saveAsImage(file, options);
			
			byte[] buffer = page.saveAsImage(options);
			
			FileOutputStream fos = new FileOutputStream(file + ".buffer");
			fos.write(buffer);
			fos.close();
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (PageException e) {
			e.printStackTrace();
		}
        catch (IOException e)
        {
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
