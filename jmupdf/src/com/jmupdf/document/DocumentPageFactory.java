/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.document;

import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Document;
import com.jmupdf.page.Page;

/**
 * Class to create a page object
 * 
 * @author Pedro J Rivera
 *
 */
class DocumentPageFactory extends Page {
	
	/**
	 * Create a new page object
	 * 
	 * @param doc
	 * @param page
	 * @throws PageException
	 */
	public DocumentPageFactory(Document doc, int page) throws PageException {
		document = doc;
		pageNumber = page;
		handle = newPage(doc.getHandle(), page);
		if (getHandle() > 0) {
			loadPageInfo();
		} else {
			throw new PageException("Error: Page could not be created.");
		}
	}
	
}
