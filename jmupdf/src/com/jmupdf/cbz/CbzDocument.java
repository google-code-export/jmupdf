/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.cbz;

import com.jmupdf.document.DocumentImp;
import com.jmupdf.enums.DocumentType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;

/**
 * CBZ (Comic Book) Document class
 * 
 * @author Pedro J Rivera
 *
 */
public final class CbzDocument extends DocumentImp  {
	private static final DocumentType DOC_TYPE = DocumentType.DOC_CBZ;
	
	/**
	 * Create a new document object
	 * @param document
	 * @param maxStore
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public CbzDocument(String document, int maxStore) throws DocException, DocSecurityException {
		open(document, null, DOC_TYPE, maxStore);
	}
	
	/**
	 * Create a new document object
	 * @param document
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public CbzDocument(String document) throws DocException, DocSecurityException {
		this(document, 0);
	}

	/**
	 * Create a new document object
	 * @param document
	 * @param maxStore
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public CbzDocument(byte[] document, int maxStore) throws DocException, DocSecurityException {
		open(document, null, DOC_TYPE, maxStore);
	}
	
	/**
	 * Create a new document object from byte array
	 * @param document
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public CbzDocument(byte[] document) throws DocException, DocSecurityException {
		this(document, 0);
	}

	/**
	 * Clone current document.
	 * This will create a new handle to document. </br>
	 * If document could not be cloned a null value will be returned. 
	 */
	public CbzDocument clone() {
		CbzDocument doc = null;
		if (getHandle() > 0) {
			try {
				doc = new CbzDocument(getDocumentName());
			} catch (DocException e) {
				doc = null;
			} catch (DocSecurityException e) {
				doc = null;
			}			
		}
		return doc;
	}

}