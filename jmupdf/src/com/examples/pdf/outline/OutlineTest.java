package com.examples.pdf.outline;

import com.jmupdf.document.Outline;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;

public class OutlineTest {
	
	public static void main(String[] args) {
		try {
			PdfDocument doc = new PdfDocument("c:\\tmp\\Introduction.pdf");
			Outline o = doc.getOutline();
			debug_outline(o, 0);			
			doc.dispose();
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		}		
	}

	/* Print out outline data structure */
	static void debug_outline(Outline o, int level) {
		String t = "";
		
		for (int i=0; i<level; i++)
			t += " ";
		
		while (o != null) {
			log(t + o.getTitle() + " \tPage:" + o.getPage() + " (" + o.getDestination() + ") \trect: " + o.getX0() + "," + o.getY0() + "," + o.getX1() + "," + o.getY1());
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
