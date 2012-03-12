package com.examples.pdf.links;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageLinks;
import com.jmupdf.pdf.PdfDocument;

/**
 * Sample get page links
 * 
 * @author Pedro J Rivera
 *
 */
public class LinksTest {

	public static void main(String[] args) {
		PdfDocument doc = null;
		Page page = null;
		
		try {
			
			/* Open document */ 
			doc = new PdfDocument("d:\\tmp\\sources.pdf");

			/* Get page */
			page = new Page(doc, 1);
			
			/* Get links, if any */
			PageLinks[] links = page.getLinks(null);
			
			/* Print the data */
			for (int i=0; i<links.length; i++) {
				switch (links[i].getType()) {
				case LINK_GOTO:
					System.out.println("Page #: " + links[i].getDestination());
					break;
				case LINK_URL:
					System.out.println("URL: " + links[i].getDestination());
					break;
				case LINK_GOTOR:
					System.out.println("GOTOR: " + links[i].getDestination());
					break;
				case LINK_LAUNCH:
					System.out.println("LAUNCH: " + links[i].getDestination());
					break;
				case LINK_NAMED:
					System.out.println("NAMED: " + links[i].getDestination());
					break;
				default:
					System.out.println("Undefined");
					break;
				}					
			}

			/* Dispose */
			page.dispose();
			doc.dispose();
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (PageException e) {
			e.printStackTrace();
		} finally {
			if (page != null)
				page.dispose();
			if (doc != null)
				doc.dispose();
		}
		
	}
	
}
