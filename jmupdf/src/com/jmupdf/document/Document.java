/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.jmupdf.JmuPdf;
import com.jmupdf.enums.DocumentType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.Page;

/**
 * Document class
 * 
 * @author Pedro J Rivera
 *
 */
public abstract class Document extends JmuPdf {
	private String document;
	private String password;
	private DocumentType documentType;
	private long handle;
	private int pageCount;	
	private int maxStore;
	private boolean isCached;		
	private Outline outline;
	
	/**
	 * Open a document
	 * 
	 * @param document
	 * @param password
	 * @param type
	 * @param maxStore
	 * @throws DocException
	 */
	public void open(String document, String password, DocumentType type, int maxStore) throws DocException, DocSecurityException  {
		this.document = document;
		this.password = password;
		this.documentType = type;
		this.maxStore = maxStore << 20;
		this.handle = 0;
		this.pageCount = 0;
		this.isCached = false;

		File file = new File(getDocumentName());

		if (!file.exists()) {
			throw new DocException("Document " + document + " does not exist.");
		}

		this.handle = open(getDocumentType().getIntValue(), getDocumentName().getBytes(), getPassWord().getBytes(), getMaxStore());

		if (getHandle() > 0) {
			this.pageCount = getPageCount(getHandle());			
		} else {
			if (getHandle() == -3) {
				throw new DocSecurityException("Error " + getHandle() + ": Document requires authentication");
			} else {
				throw new DocException("Error " + getHandle() + ": Document " + getDocumentName() + " could not be opened.");
			}		
		}
	}

	/**
	 * Open a document
	 * 
	 * @param document
	 * @param password
	 * @param type
	 * @param maxStore
	 * @throws DocException
	 */
	public void open(byte[] document, String password, DocumentType type, int maxStore) throws DocException, DocSecurityException  {
		try {
			File tmp = File.createTempFile("jmupdf" + getClass().hashCode(), ".tmp");
			tmp.deleteOnExit();

			FileOutputStream fos = new FileOutputStream(tmp.getAbsolutePath(), true);
            fos.write(document, 0, document.length);
            fos.flush();
            fos.close();

            open(tmp.getAbsolutePath(), password, type, maxStore);
    		isCached = true;
		} catch (IOException e) {
			throw new DocException("Error: byte[] document could not be opened.");
		}
	}

	/**
	 * Close document
	 */
	public synchronized void close() {
		if (getHandle() > 0) {
			close(getHandle());
			if (isCached) {
				File file = new File(document);
				if (file.exists()) {
					file.delete();
				}
			}
			if (outline != null) {
				disposeOutline(outline);
			}
			handle = 0;
		}
	}

	/**
	 * Get document handle
	 * 
	 * @return
	 */
	public synchronized long getHandle() {
		return handle;
	}
	
	/**
	 * Get document type
	 * @return
	 */
	public DocumentType getDocumentType() {
		return documentType;
	}
	
	/**
	 * Get max memory used to store information.</br>
	 * The default value will be 20mb
	 * @return
	 */
	public int getMaxStore() {
		if (maxStore <= 0) {
			maxStore = 60 << 20;
		}
		return maxStore;
	}
	
	/**
	 * Get document version
	 * @return
	 */
	public int getVersion() {
		if (getHandle() > 0) {
			return getVersion(getHandle());
		}
		return 0;
	}

	/**
	 * Get document full path plus name
	 * @return
	 */
	public String getDocumentName() {
		if (document == null) {
			document = "";
		}
		return document;
	}

	/**
	 * Get document password
	 * @return
	 */
	public String getPassWord() {
		if (password == null) {
			password = "";
		}
		return password;
	}

	/**
	 * Get document outline
	 * @return
	 */
	public synchronized Outline getOutline() {
		if (getHandle() > 0) {
			if (outline == null) {
				outline = getOutline(getHandle());	
			}
			return outline;
		}
		return null;
	}
	
	/**
	 * Get total pages in document
	 * @return 
	 */
	public synchronized int getPageCount() {
		if (getHandle() > 0) {
			return pageCount;
		}
		return 0;
	}

	/**
	 * Create a new page object.  
	 * 
	 * @param page
	 * @return
	 */
	public synchronized Page getPage(int page) throws PageException {
		if (getHandle() > 0) {
			return new DocumentPage(this, page);
		}
		return null;
	}

	/**
	 * Release all references to outline objects
	 * 
	 * @param o 
	 */
	private static void disposeOutline(Outline o) {
		while (o != null) {
			if (o.getChild() != null) {
				disposeOutline(o.getChild());
			}
			if (o.getNext() != null) {
				disposeOutline(o.getNext());
			}
			o = null;
		}
	}

}
