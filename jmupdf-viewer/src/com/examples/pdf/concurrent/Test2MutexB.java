package com.examples.pdf.concurrent;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Page;
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
			log("Processing " + doc);
			for (int i=0; i<d.getPageCount(); i++) {
				p = d.getPage(i+1);
				p.dispose();
			}
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (PageException e) {
			e.printStackTrace();
		} finally {
			if (d != null) {
				log("closing doc " + doc);
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
