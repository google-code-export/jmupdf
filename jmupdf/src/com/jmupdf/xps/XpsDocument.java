/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.xps;

import com.jmupdf.document.Document;
import com.jmupdf.enums.DocumentType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;

/**
 * XPS Document class
 * 
 * @author Pedro J Rivera
 *
 */
public class XpsDocument extends Document  {
	private static final DocumentType DOC_TYPE = DocumentType.DOC_XPS; 
	
	/**
	 * Create a new document object
	 * @param document
	 * @param maxStore
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public XpsDocument(String document, int maxStore) throws DocException, DocSecurityException {
		open(document, null, DOC_TYPE, maxStore);
		validate();
	}
	
	/**
	 * Create a new document object
	 * @param document
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public XpsDocument(String document) throws DocException, DocSecurityException {
		this(document, 0);
	}

	/**
	 * Create a new document object
	 * @param document
	 * @param maxStore
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public XpsDocument(byte[] document, int maxStore) throws DocException, DocSecurityException {
		open(document, null, DOC_TYPE, maxStore);
		validate();
	}
	
	/**
	 * Create a new document object from byte array
	 * @param document
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public XpsDocument(byte[] document) throws DocException, DocSecurityException {
		this(document, 0);
	}

	/**
	 * Validate handle value
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	private void validate() throws DocException, DocSecurityException {
		if (getHandle() > 0) {
			return;
		}
		if (getHandle() == -1 || getHandle() == -2) {
			throw new DocException("Error " + getHandle() + ": Document " + getDocumentName() + " could not be opened.");
		}			
		else {
			throw new DocException("Unexpected error opening document.");
		}			
	}
	
	/**
	 * Close document and free native resources
	 * 
	 */
	public void dispose() {
		close();
	}

	/**
	 * Clone current document.
	 * This will create a new handle to document. </br>
	 * If document could not be cloned a null value will be returned.
	 * 
	 */
	public XpsDocument clone() {
		XpsDocument doc = null;
		if (getHandle() > 0) {
			try {
				doc = new XpsDocument(getDocumentName());
			} catch (DocException e) {
				doc = null;
			} catch (DocSecurityException e) {
				doc = null;
			}			
		}
		return doc;
	}

}