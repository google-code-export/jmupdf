package com.examples.pdf.concurrent;

import com.jmupdf.document.Document;
import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.Page;

public class TestMutexB implements Runnable {
	Document doc;
	int n;
	
	public TestMutexB(Document doc, int n) {		
		this.doc = doc;
		this.n = n;
	}
	
	public void run() {
		Page p = null;
		try {
			p = new Page(doc, n);
			p.saveAsPng("d:\\tmp\\images\\" + doc.getHandle() + "_page_"+n+".png", Page.PAGE_ROTATE_NONE, 3f, ImageType.IMAGE_TYPE_ARGB, 1f);			
		} catch (PageException e) {
			e.printStackTrace();
		} finally {
			if (p != null) {
				p.dispose();
			}
		}
	}		

}
