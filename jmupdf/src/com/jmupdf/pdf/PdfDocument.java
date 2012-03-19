/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.pdf;

import com.jmupdf.document.DocumentImp;
import com.jmupdf.enums.DictionaryType;
import com.jmupdf.enums.DocumentType;
import com.jmupdf.enums.EncryptType;
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

	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/* Begin encrypt info methods                        */
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/* TODO: Document each method!                       */ 
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	
	public boolean isEncrypted() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().isEncrypted();
		}
		return false;
	}
	
	public int getRevision() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getRevision();
		}
		return 0;
	}
	
	public int getLength() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getLength();
		}
		return 0;
	}

	public EncryptType getMethod() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getMethod();
		}
		return null;
	}

	public boolean getCanPrint() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getCanPrint();
		}
		return false;
	}

	public boolean getCanModify() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getCanModify();
		}
		return false;
	}

	public boolean getCanNotes() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getCanNotes();
		}
		return false;
	}

	public boolean getCanCopy() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getCanCopy();
		}
		return false;
	}

	public boolean getCanFillForm() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getCanFillForm();
		}
		return false;
	}

	public boolean getCanAccessibility() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getCanAccessibility();
		}
		return false;
	}

	public boolean getCanAssemble() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getCanAssemble();
		}
		return false;
	}

	public boolean getCanPrintQuality() {
		if (getEncryptInfo() != null) {
			return getEncryptInfo().getCanPrintQuality();
		}
		return false;
	}	
	
	/**
	 * Get encryption information
	 * @return 
	 */
	private PdfEncrypt getEncryptInfo() {
		if (getHandle() > 0) {
			if (pdfEncrypt == null) {
				int[] data = pdfEncryptInfo(getHandle());
				pdfEncrypt = new PdfEncrypt(data);
			}
			return pdfEncrypt;
		}
		return null;
	}

	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/* Begin document information methods                */
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	/**
	 * Get title
	 * @return
	 */
	public String getTitle() {
		if (getPdfInfo() != null) {
			return getPdfInfo().getTitle();
		}
		return "";
	}

	/**
	 * Get author
	 * @return
	 */
	public String getAuthor() {
		if (getPdfInfo() != null) {
			return getPdfInfo().getAuthor();
		}
		return "";
	}

	/**
	 * Get producer
	 * @return
	 */
	public String getProducer() {
		if (getPdfInfo() != null) {
			return getPdfInfo().getProducer();
		}
		return "";
	}

	/**
	 * Get creator
	 * @return
	 */
	public String getCreator() {
		if (getPdfInfo() != null) {
			return getPdfInfo().getCreator();
		}
		return "";
	}

	/**
	 * Get subject
	 * @return
	 */
	public String getSubject() {
		if (getPdfInfo() != null) {
			return getPdfInfo().getSubject();
		}
		return "";
	}

	/**
	 * Get keywords
	 * @return
	 */
	public String getKeywords() {
		if (getPdfInfo() != null) {
			return getPdfInfo().getKeywords();
		}
		return "";
	}

	/**
	 * Ger date created
	 * @return
	 */
	public String getCreatedDate() {
		if (getPdfInfo() != null) {
			return getPdfInfo().getCreatedDate();
		}
		return "";
	}

	/**
	 * Get date modified
	 * @return
	 */
	public String getModifiedDate() {
		if (getPdfInfo() != null) {
			return getPdfInfo().getModifiedDate();
		}
		return "";
	}

	/**
	 * Get version
	 * @return
	 */
	public String getPdfVersion() {
		if (getPdfInfo() != null) {
			return getPdfInfo().getVersion();
		}
		return "";
	}

	/**
	 * Is document trapped
	 * @return
	 */
	public String isTrapped() {
		if (getPdfInfo() != null) {
			return getPdfInfo().isTrapped();
		}
		return "";
	}
	
	/**
	 * Get document information
	 * @return
	 */
	private PdfInformation getPdfInfo() {
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