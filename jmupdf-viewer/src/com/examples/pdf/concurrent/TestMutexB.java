package com.examples.pdf.concurrent;

import java.io.File;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Document;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PageRendererOptions;

public class TestMutexB implements Runnable {
	Document doc;
	int n;
	
	public TestMutexB(Document doc, int n) {		
		this.doc = doc;
		this.n = n;
	}
	
	public void run() {
		Page p = null;
		String s;
		try {
			log("Processing page " + n + " of " + doc.getDocumentName());
			p = doc.getPage(n);
			s = "f:\\tmp\\images\\" + doc.getHandle() + "_page_" + n + ".png";
			
			PageRendererOptions options = p.getRenderingOptions();
			options.setRotate(Page.PAGE_ROTATE_NONE);
			options.setZoom(0.5f);
			options.setImageType(ImageType.IMAGE_TYPE_ARGB);
			options.setGamma(1f);
			options.setAntiAlias(8);
			
			p.saveAsImage(s, options);			
			new File(s).delete();
			//byte[] b = p.saveAsImage(options);
		} catch (PageException e) {
			e.printStackTrace();
		} finally {
			if (p != null) {
				p.dispose();
			}
		}
	}		

    protected static void log(String text) {
    	System.out.println(text);
    }
}
