package com.examples.pdf.concurrent;

import java.io.File;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Document;
import com.jmupdf.interfaces.Page;

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
			s = "c:\\tmp\\images\\" + doc.getHandle() + "_page_"+n+".png";			
			p.saveAsPng(s, Page.PAGE_ROTATE_NONE, 3f, ImageType.IMAGE_TYPE_ARGB, 1f);
			new File(s).delete();
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
