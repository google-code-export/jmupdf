package com.examples.pdf.concurrent;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;

public class Test2MutexB implements Runnable {
	String doc;
	
	public Test2MutexB(String doc) {		
		this.doc = doc;
	}

	public void run() {
		PdfDocument d = null;
		Page p = null;
		
		try {
			d = new PdfDocument(doc);
			//for (int i=0; i<d.getPageCount(); i++) {
				//log("p = " + (i+1));
				p = new Page(d, 24);
				p.dispose();
			//}
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (PageException e) {
			e.printStackTrace();
		} finally {
			if (d != null) {
				d.dispose();
			}
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
