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
import com.jmupdf.interfaces.Document;
import com.jmupdf.interfaces.Page;
import com.jmupdf.page.PageImp;
import com.jmupdf.page.PageRect;

/**
 * Document Implementation Class
 * 
 * @author Pedro J Rivera
 *
 */
public abstract class DocumentImp extends JmuPdf implements Document {
	private String document;
	private String fileName;
	private String path;
	private String password;
	private DocumentType type;
	private long handle;
	private int pageCount;	
	private int maxStore;
	private boolean isCached;		
	private DocumentOutline outline;
	
	/**
	 * Open a document
	 * 
	 * @param document
	 * @param password
	 * @param type
	 * @param maxStore
	 * @throws DocException
	 * @throws DocSecurityException
	 */
	protected void open(String document, String password, DocumentType type, int maxStore) throws DocException, DocSecurityException  {
		this.document = document;
		this.password = password;
		this.type = type;
		this.maxStore = maxStore << 20;
		this.handle = 0;
		this.pageCount = 0;
		this.isCached = false;

		File file = new File(getDocumentName());

		if (!file.exists()) {
			throw new DocException("Document " + document + " does not exist.");
		} 

		handle = open(getType().getIntValue(), getDocumentName().getBytes(), getPassWord().getBytes(), getMaxStore());

		if (getHandle() > 0) {
			pageCount = getPageCount(getHandle());
			fileName = file.getName();
			path = file.getParent();
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
	 * @throws DocSecurityException
	 */
	protected void open(byte[] document, String password, DocumentType type, int maxStore) throws DocException, DocSecurityException  {
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

	/* */
	public synchronized void dispose() {
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

	/* */
	public synchronized long getHandle() {
		return handle;
	}
	
	/* */
	public DocumentType getType() {
		return type;
	}
	
	/* */
	public int getMaxStore() {
		if (maxStore <= 0) {
			maxStore = 60 << 20;
		}
		return maxStore;
	}
	
	/* */
	public int getVersion() {
		if (getHandle() > 0) {
			return getVersion(getHandle());
		}
		return 0;
	}

	/* */
	public String getDocumentName() {
		if (document == null) {
			document = "";
		}
		return document;
	}

	/* */
	public String getFileName() {
		if (fileName == null) {
			fileName = "";
		}
		return fileName;
	}

	/* */
	public String getPath() {
		if (path == null) {
			path = "";
		}
		return path;
	}
	
	/* */
	public String getPassWord() {
		if (password == null) {
			password = "";
		}
		return password;
	}

	/* */
	public synchronized DocumentOutline getOutline() {
		if (getHandle() > 0) {
			if (outline == null) {
				outline = getOutline(getHandle());	
			}
			return outline;
		}
		return null;
	}
	
	/* */
	public synchronized int getPageCount() {
		if (getHandle() > 0) {
			return pageCount;
		}
		return 0;
	}

	/* */
	public synchronized Page getPage(int page) throws PageException {
		if (getHandle() > 0) {
			return new DocumentPageFactory(this, page);
		}
		return null;
	}

	/**
	 * Release all references to outline objects
	 * 
	 * @param o 
	 */
	private static void disposeOutline(DocumentOutline o) {
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

	/**
	 * DocumentPageFactory class
	 */
	class DocumentPageFactory extends PageImp {		
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

}
