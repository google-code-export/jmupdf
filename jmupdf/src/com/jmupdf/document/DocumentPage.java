/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.document;

import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.Page;

/* Package private */

/**
 * Private package class to create a page object
 * 
 * @author Pedro J Rivera
 *
 */
class DocumentPage extends Page{
	
	/**
	 * Create a new page object
	 * 
	 * @param doc
	 * @param page
	 * @throws PageException
	 */
	public DocumentPage(Document doc, int page) throws PageException {
		document = doc;
		handle = newPage(doc.getHandle(), page);
		if (getHandle() > 0) {
			loadPageInfo();
			if (getHandle() <= 0) {
				throw new PageException("Error: Page could not be loaded.");
			}
		} else {
			throw new PageException("Error: Page could not be created.");
		}
	}
}
