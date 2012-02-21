package com.examples.pdf.outline;

import com.jmupdf.document.Outline;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;

public class Pdf_Outline {
	
	public static void main(String[] args) {
		//String f = "E:\\development\\indigo\\workspace_jmupdf\\pdf_docs\\pdf_reference_1-7.pdf";
		String f = "E:\\development\\indigo\\workspace_jmupdf-old\\pdf_docs\\itextinaction.pdf";
		Outline o;
		try {
			PdfDocument doc = new PdfDocument(f);
			o = doc.getOutline();	
			debug_outline(o, 0);
			doc.dispose();
		} catch (DocException e) {
		} catch (DocSecurityException e) {
		}		
	}

	/* Print out outline data structure */
	static void debug_outline(Outline o, int level) {
		String t = "";
		
		for (int i=0; i<level; i++)
			t += " ";
		
		while (o != null) {
			log(t + o.getTitle() + " " + o.getPage() + " (" + o.getDestination() + ")");
			if (o.getChild() != null) {
				debug_outline(o.getChild(), level + 2);
			}
			o = o.getNext();
		}
		
	}
	
	static void log(String s) {
		System.out.println(s);
	}

}
