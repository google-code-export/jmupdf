package com.examples.pdf.concurrent;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;

public class TestMutexB implements Runnable {
	PdfDocument doc;
	int n;
	
	public TestMutexB(PdfDocument doc, int n) {		
		this.doc = doc;
		this.n = n;
	}
	
	public void run() {				
		try {
			Page p = new Page(doc, n);
			p.saveAsPng("d:\\tmp\\images\\" + doc.getHandle() + "_page_"+n+".png", Page.PAGE_ROTATE_NONE, 3f, ImageType.IMAGE_TYPE_ARGB, 1f);
		} catch (PageException e) {
			e.printStackTrace();
		}
	}		

}
