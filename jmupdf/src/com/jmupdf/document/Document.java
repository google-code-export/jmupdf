/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.jmupdf.JmuPdf;
import com.jmupdf.enums.DocumentType;
import com.jmupdf.enums.ImageType;
import com.jmupdf.enums.TifCompression;
import com.jmupdf.enums.TifMode;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageLinks;
import com.jmupdf.page.PageText;

/**
 * Document class
 * 
 * @author Pedro J Rivera
 *
 */
public abstract class Document extends JmuPdf {
	private String document;
	private String password;
	private long handle;
	private int pageCount;		
	private DocumentType documentType;
	private int antiAliasLevel;
	private int maxStore;
	private boolean isCached;		
	private Outline outline;

	/**
	 * Open a document
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
			this.antiAliasLevel = getAntiAliasLevel(getHandle());
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
	public void close() {
		if (handle > 0) {
			close(handle);
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
	 * @return
	 */
	public long getHandle() {
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
	 * Get max memory used to store information.
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
		if (handle > 0) {
			return getVersion(handle);
		}
		return 0;
	}

	/**
	 * Get total pages in document
	 * @return 
	 */
	public int getPageCount() {
		if (handle > 0) {
			return pageCount;
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
	 * Get a page object.  
	 * @param page
	 * @return
	 */
	public Page getPage(int page) {
		if (handle > 0) {
			return new Page(this, getPageInfo(page), page);
		}
		return null;
	}

	/**
	 * Get page info
	 * @param page
	 * @return
	 */
	public float[] getPageInfo(int page) {
		if (handle > 0) {
			return loadPage(handle, page);
		}
		return null;
	}

	/**
	 * Get page links
	 * @param page
	 * @return
	 */
	public PageLinks[] getPageLinks(int page) {
		if (handle > 0) {
			return getPageLinks(handle, page);
		}
		return null;
	}
	
	/**
	 * Get document outline
	 * @return
	 */
	public Outline getOutline() {
		if (handle > 0) {
			if (outline == null) {
				outline = getOutline(handle);	
			}
			return outline;
		}
		return null;
	}
	
	/**
	 * Get page text
	 * @param page
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public PageText[] getPageText(int page, int x0, int y0, int x1, int y1) {
		if (handle > 0) {
			return getPageText(handle, page, x0, y0, x1, y1);
		}
		return null;
	}
	
	/**
	 * Get a page as a byte buffer
	 * @param page
	 * @param zoom
	 * @param rotate
	 * @param color
	 * @param gamma
	 * @param bbox
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public ByteBuffer getPageByteBuffer(int page, float zoom, int rotate, ImageType color, float gamma, int[] bbox, float x0, float y0, float x1, float y1) {
		if (handle > 0) {
			return getByteBuffer(handle, page, zoom, rotate, color.getIntValue(), gamma, bbox, x0, y0, x1, y1);
		}
		return null;
	}
	
	/**
	 * Free a byte buffer resource
	 * @param buffer
	 */
	public void freeByteBuffer(ByteBuffer buffer) {
		if (handle > 0) {
			if (buffer != null) {
				if (buffer.isDirect()) {
					buffer.clear();
					freeByteBuffer(handle, buffer);
				}
			}
		}
	}
	
	/**
	 * Set default anti-alias level to be used when rendering pages
	 * @param antiAliasLevel
	 */
	public void setAntiAliasLevel(int antiAliasLevel) {
		if (handle > 0) {
			antiAliasLevel = validateAntiAliasLevel(antiAliasLevel);
			if (antiAliasLevel != this.antiAliasLevel) {
				this.antiAliasLevel = antiAliasLevel;
				setAntiAliasLevel(handle, antiAliasLevel);
			}
		}
	}

	/**
	 * Get default anti-alias level used when rendering pages
	 * @return
	 */
	public int getAntiAliasLevel() {
		if (handle > 0) {
			return antiAliasLevel;
		}
		return -1;
	}
	
	/**
	 * Save a page to a file in PBM format</br>
	 * PBM is the portable bitmap format, a lowest common denominator monochrome file format. 
	 * @param page
	 * @param file
	 * @param zoom
	 * @param gamma
	 * @return
	 */
	public boolean saveAsPbm(int page, String file, float zoom, float gamma) {
		if (handle > 0) {
			return writePbm(handle, page, zoom, gamma, file.getBytes()) == 0;
		}
		return false;
	}
	
	/**
	 * Save a page to a file in PBM format</br>
	 * PBM is the portable bitmap format, a lowest common denominator monochrome file format. 
	 * @param page
	 * @param file
	 * @param zoom
	 * @return
	 */
	public boolean saveAsPbm(int page, String file, float zoom) {
		return saveAsPbm(page, file, zoom, 1f);
	}
	
	/**
	 * Save a page to a file in PNM format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	public boolean saveAsPnm(int page, String file, float zoom, ImageType color, float gamma) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB || 
				color == ImageType.IMAGE_TYPE_GRAY) {
				return writePnm(handle, page, zoom, color.getIntValue(), gamma, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a file in PNM format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @return
	 */
	public boolean saveAsPnm(int page, String file, float zoom, ImageType color) {
		return saveAsPnm(page, file, zoom, color, 1f);
	}

	/**
	 * Save a page to a file in JPEG format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @param quality
	 * <blockquote>quality levels are in the range 0-100 with a default value of 75.</blockquote>
	 * @return
	 */
	public boolean saveAsJPeg(int page, String file, float zoom, ImageType color, float gamma, int quality) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB || 
				color == ImageType.IMAGE_TYPE_GRAY) {
				if (!(quality >= 0 && quality <= 100)) {
					quality = 75;
				}
				return writeJPeg(handle, page, zoom, color.getIntValue(), gamma, file.getBytes(), quality) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a file in JPEG format 
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param quality
	 * @return
	 */
	public boolean saveAsJPeg(int page, String file, float zoom, ImageType color, int quality) {
		return saveAsJPeg(page, file, zoom, color, 1f, quality);
	}
	
	/**
	 * Save a page to a BMP format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	public boolean saveAsBmp(int page, String file, float zoom, ImageType color, float gamma) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB    || 
				color == ImageType.IMAGE_TYPE_GRAY   ||
				color == ImageType.IMAGE_TYPE_BINARY ||
				color == ImageType.IMAGE_TYPE_BINARY_DITHER) {
				return writeBmp(handle, page, zoom, color.getIntValue(), gamma, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a BMP format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @return
	 */
	public boolean saveAsBmp(int page, String file, float zoom, ImageType color) {
		return saveAsBmp(page, file, zoom, color, 1f);
	}
	
	/**
	 * Save a page to a file in PNG format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	public boolean saveAsPng(int page, String file, float zoom, ImageType color, float gamma) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB  	 || 
				color == ImageType.IMAGE_TYPE_ARGB 	 ||
				color == ImageType.IMAGE_TYPE_ARGB_PRE ||
				color == ImageType.IMAGE_TYPE_GRAY) {
				return writePng(handle, page, zoom, color.getIntValue(), gamma, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a file in PNG format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @return
	 */
	public boolean saveAsPng(int page, String file, float zoom, ImageType color) {
		return saveAsPng(page, file, zoom, color, 1f);
	}
	
	/**
	 * Save a page to a file in PAM format</br>
	 * The name "PAM" is an acronym derived from "Portable Arbitrary Map"
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	public boolean saveAsPam(int page, String file, float zoom, ImageType color, float gamma) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB  	 || 
				color == ImageType.IMAGE_TYPE_ARGB 	 || 
				color == ImageType.IMAGE_TYPE_ARGB_PRE ||
				color == ImageType.IMAGE_TYPE_GRAY) {
				return writePam(handle, page, zoom, color.getIntValue(), gamma, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a file in PAM format</br>
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @return
	 */
	public boolean saveAsPam(int page, String file, float zoom, ImageType color) {
		return saveAsPam(page, file, zoom, color, 1f);
	}
	
	/**
	 * Save a page to a TIF format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @param compression
	 * @param mode
	 * @param quality <br/><br/>
	 *        
	 *        <blockquote>
	 *        <strong>When compression == TIF_COMPRESSION_ZLIB <br/></strong> 
	 *          Control  the  compression  technique used by the Deflate codec.  <br/> 
	 *          Quality levels are in the range 1-9 with larger numbers yielding <br/> 
	 *          better compression at the cost of more computation. The default  <br/> 
	 *          quality level is 6 which yields a good time-space tradeoff.      <br/><br/>
	 *          
	 *        <strong>When compression == TIF_COMPRESSION_JPEG <br/></strong>
	 *          Control the compression quality level used in the baseline algo- <br/>
	 *          rithm. Note that quality levels are in the range 0-100 with a    <br/>
	 *          default value of 75. <br/><br/>
	 *        </blockquote>  
	 * @return
	 */
	public boolean saveAsTif(int page, String file, float zoom, ImageType color, float gamma, TifCompression compression, TifMode mode, int quality) {
		if (handle > 0) {
			
			if (!(color == ImageType.IMAGE_TYPE_RGB      || 
				  color == ImageType.IMAGE_TYPE_ARGB     ||
				  color == ImageType.IMAGE_TYPE_ARGB_PRE ||
				  color == ImageType.IMAGE_TYPE_GRAY     ||
				  color == ImageType.IMAGE_TYPE_BINARY   || 
				  color == ImageType.IMAGE_TYPE_BINARY_DITHER)) {
				log("Invalid color type specified.");
				return false;
			}
			
			if (!(mode == TifMode.TIF_DATA_APPEND || 
				  mode == TifMode.TIF_DATA_DISCARD)) {
				log("Invalid mode value specified.");
				return false;
			}

			if (compression == TifCompression.TIF_COMPRESSION_CCITT_RLE || 
				compression == TifCompression.TIF_COMPRESSION_CCITT_T_4 ||
				compression == TifCompression.TIF_COMPRESSION_CCITT_T_6) {
				if (!(color == ImageType.IMAGE_TYPE_BINARY || 
					  color == ImageType.IMAGE_TYPE_BINARY_DITHER)) {
					log("When using CCITT compression, color must be type binary");
					return false;
				}
				if (color == ImageType.IMAGE_TYPE_ARGB || 
					color == ImageType.IMAGE_TYPE_ARGB_PRE) {
					log("When using CCITT compression, color cannot be type of ARGB");
					return false;
				}
			}

			if (compression == TifCompression.TIF_COMPRESSION_JPEG) {
				if (!(quality >= 1 && quality <= 100)) {
					quality = 75;
				}
			}

			if (compression == TifCompression.TIF_COMPRESSION_ZLIB) {
				if (!(quality >= 1 && quality <= 9)) {
					quality = 6;
				}
			}

			return writeTif(handle, page, zoom, color.getIntValue(), gamma, file.getBytes(), compression.getIntValue(), mode.getIntValue(), quality) == 0;
		}

		return false;
	}
	
	/**
	 * Save a page to a TIF format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param compression
	 * @param mode
	 * @param quality
	 * @return
	 */
	public boolean saveAsTif(int page, String file, float zoom, ImageType color, TifCompression compression, TifMode mode, int quality) {
		return saveAsTif(page, file, zoom, color, 1f, compression, mode, quality);
	}
	
	/**
	 * Validate Anti-alias level
	 */
	private static int validateAntiAliasLevel(int level) {
		if (level < 0) {
			return 0;
		}
		else if (level > 8) {
			return 8;
		}
		return level;
	}

	/**
	 * Release all references to outline objects
	 * @param o
	 * 
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
