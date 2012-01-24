package com.examples.xps;

import com.jmupdf.document.Outline;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.xps.XpsDocument;

public class Xps_Outline {
	
	public static void main(String[] args) {
		//String f = "E:\\development\\indigo\\workspace_jmupdf\\pdf_docs\\pdf_reference_1-7.pdf";
		String f = "C:\\Users\\Pedro\\Documents\\Windows_Vista_Product_Guide.xps";
		Outline o;
		try {
			XpsDocument doc = new XpsDocument(f);
			o = doc.getOutline();	
			debug_outline(o, 0);
			doc.dispose();
		} catch (DocException e) {
			log("error 1");
		} catch (DocSecurityException e) {
			log("error 2");
		}		
	}

	/* Print out outline data structure */
	static void debug_outline(Outline o, int level) {
		String t = "";
		
		for (int i=0; i<level; i++)
			t += " ";
		
		while (o != null) {
			log(t + o.getTitle() + " " + o.getPage());
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
