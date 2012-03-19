/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.document;

import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Document;
import com.jmupdf.page.PageImp;
import com.jmupdf.page.PageRect;

/**
 * Class to create a page object
 * 
 * @author Pedro J Rivera
 *
 */
class DocumentPageFactory extends PageImp {
	
	/**
	 * Create a new page object
	 * 
	 * @param doc
	 * @param page
	 * @throws PageException
	 */
	public DocumentPageFactory(Document doc, int page) throws PageException {
		float[] info = new float[5];
		document = doc;
		pageNumber = page;
		handle = newPage(doc.getHandle(), page, info);
		if (handle > 0) {
			boundBox = new PageRect(info[0], info[1], info[2], info[3]);
			rotation = (int)info[4];
		} else {
			throw new PageException("Error: Page could not be created.");
		}
	}
	
}
