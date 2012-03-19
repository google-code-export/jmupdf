/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.pdf;

import com.jmupdf.document.DocumentImp;
import com.jmupdf.enums.DictionaryType;
import com.jmupdf.enums.DocumentType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;

/**
 * PDF Document class
 * 
 * @author Pedro J Rivera
 *
 */
public final class PdfDocument extends DocumentImp {
	private static final DocumentType DOC_TYPE = DocumentType.DOC_PDF;
	private PdfInformation pdfInformation;
	private PdfEncrypt pdfEncrypt;
	
	/**
	 * Create a new document object
	 * @param document
	 * @param password
	 * @param maxStore
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public PdfDocument(String document, String password, int maxStore) throws DocException, DocSecurityException {
		open(document, password, DOC_TYPE, maxStore);
	}

	/**
	 * Create a new document object
	 * @param document
	 * @param password
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public PdfDocument(String document, String password) throws DocException, DocSecurityException {
		this(document, password, 0);
	}
	
	/**
	 * Create a new document object
	 * @param document
	 * @param maxStore
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public PdfDocument(String document, int maxStore) throws DocException, DocSecurityException {
		this(document, null, maxStore);
	}

	/**
	 * Create a new document object
	 * @param document
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public PdfDocument(String document) throws DocException, DocSecurityException {
		this(document, null, 0);
	}

	/**
	 * Create a new document object
	 * @param document
	 * @param password
	 * @param maxStore
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public PdfDocument(byte[] document, String password, int maxStore) throws DocException, DocSecurityException {
		open(document, password, DOC_TYPE, maxStore);
	}

	/**
	 * Create a new document object from byte array
	 * @param document
	 * @param password
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public PdfDocument(byte[] document, String password) throws DocException, DocSecurityException {
		this(document, password, 0);
	}
	
	/**
	 * Create a new document object from byte array
	 * @param document
	 * @param maxStore
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public PdfDocument(byte[] document, int maxStore) throws DocException, DocSecurityException {
		this(document, null, maxStore);
	}
	
	/**
	 * Create a new document object from byte array
	 * @param document
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	public PdfDocument(byte[] document) throws DocException, DocSecurityException {
		this(document, null, 0);
	}

	/**
	 * Get document information from info dictionary
	 * @param key
	 * @return Null if no value could be retrieved 
	 */
	public String getInfo(DictionaryType key) {
		if (getHandle() > 0) {
			return pdfInfo(getHandle(), key.getStrValue());
		}
		return null;
	}

	/**
	 * Get encryption information
	 * @return 
	 */
	public PdfEncrypt getEncryptInfo() {
		if (getHandle() > 0) {
			if (pdfEncrypt == null) {
				int[] data = pdfEncryptInfo(getHandle());
				pdfEncrypt = new PdfEncrypt(data);
			}
			return pdfEncrypt;
		}
		return null;
	}

	/**
	 * Get document information
	 * @return
	 */
	public PdfInformation getInformation() {
		if (getHandle() > 0) {
			if (pdfInformation == null) {
				pdfInformation = new PdfInformation(this);
			}
			return pdfInformation;
		}
		return null;
	}
	
	/**
	 * Clone current document.
	 * This will create a new handle to document. </br>
	 * If document could not be cloned a null value will be returned. 
	 */
	public PdfDocument clone() {
		PdfDocument doc = null;
		if (getHandle() > 0) {
			try {
				doc = new PdfDocument(getDocumentName(), getPassWord());
			} catch (DocException e) {
				doc = null;
			} catch (DocSecurityException e) {
				doc = null;
			}			
		}
		return doc;
	}

}