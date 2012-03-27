package com.jmupdf.page;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jmupdf.JmuPdf;
import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PagePixels;
import com.jmupdf.interfaces.PageRendererOptions;

/**
 * PagePixels Class
 * 
 * This class represents pixel/image data for a given page.
 * 
 * @author Pedro J Rivera
 *
 */
abstract class PagePixelsImp extends JmuPdf implements PagePixels {
	protected Page page = null;
	protected PageRendererOptions options = null;
	protected BufferedImage image = null;
	protected ByteBuffer buffer = null;
	protected Object pixels = null;

	/* */
	public Page getPage() {
		return page;
	}

	/* */
	public PageRendererOptions getOptions() {
		return options;
	}

	/* */
	public synchronized BufferedImage getImage() {
		if (image == null) {
			createBufferedImage();
		}
		return image;
	}

	/* */
	public Object getPixels() {
		if (pixels != null) { 
			return pixels;
		}
		return null;
	}

	/* */
	public synchronized void drawPage(PageRendererOptions options, float x0, float y0, float x1, float y1) {
		
		if (options != null) {
			/* zero rotate and 1f zoom */
			float zoom = options.getZoom();
			PageRect rect = new PageRect(x0/zoom, y0/zoom, x1/zoom, y1/zoom);
			rect = rect.rotate(getPage().getBoundBox(), options.getRotate(), Page.PAGE_ROTATE_NONE);
			getOptions().setBoundBox(rect);
		} else {
			getOptions().setBoundBox(new PageRect(x0, y0, x1, y1));
		}
		
		int[] bbox = new int[4];

		buffer = getByteBuffer(bbox);

		if (buffer != null) {
			if (isByteData()) {
				pixels = new byte[buffer.order(ByteOrder.nativeOrder()).capacity()];
				buffer.order(ByteOrder.nativeOrder()).get((byte[])pixels);
			} else {
				pixels = new int[buffer.order(ByteOrder.nativeOrder()).asIntBuffer().capacity()];
				buffer.order(ByteOrder.nativeOrder()).asIntBuffer().get((int[])pixels);
			}
			freeByteBuffer();
			getOptions().getBoundBox().setRect(bbox[0], bbox[1], bbox[2], bbox[3]);
		} else {
			System.gc();
		}
	}

	/**
	 * Create a buffered image from packed pixel data
	 * @param pixels
	 */
	private void createBufferedImage() {
		Object p;
		try {
			p = getPixels();
			PageRect bb = getOptions().getBoundBox();
			if (p != null) {
				image = new BufferedImage(bb.getWidth(), bb.getHeight(), getBufferedImageType());
			    if (image != null) {
			    	WritableRaster raster = image.getRaster();
			    	raster.setDataElements(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight(), p);
			    }			    
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			if (image != null) {
				image.flush();
				image = null;	
			}
    		System.gc();
		}
	}

	/**
	 * Get buffered image type
	 * @return
	 */
	private int getBufferedImageType() {
		int type;
		switch (getOptions().getImageType()) {
			case IMAGE_TYPE_BINARY:
			case IMAGE_TYPE_BINARY_DITHER:
				type = BufferedImage.TYPE_BYTE_BINARY;
				break;
			case IMAGE_TYPE_GRAY:
				type = BufferedImage.TYPE_BYTE_GRAY;
				break;
			case IMAGE_TYPE_RGB:
				type = BufferedImage.TYPE_INT_RGB;
				break;
			case IMAGE_TYPE_ARGB:
				type = BufferedImage.TYPE_INT_ARGB;
				break;
			case IMAGE_TYPE_ARGB_PRE:
				type = BufferedImage.TYPE_INT_ARGB_PRE;
				break;
			case IMAGE_TYPE_BGR:
				type = BufferedImage.TYPE_INT_BGR;
				break;
			default:
				type = BufferedImage.TYPE_INT_RGB;
				break;
		}
		return type;
	}

	/**
	 * Determine if color type is a byte type.
	 * @return
	 */
	private boolean isByteData() {
		return (getOptions().getImageType() == ImageType.IMAGE_TYPE_BINARY        || 
				getOptions().getImageType() == ImageType.IMAGE_TYPE_BINARY_DITHER ||
				getOptions().getImageType() == ImageType.IMAGE_TYPE_GRAY);
	}

	/**
	 * Get a page as a byte buffer
	 * 
	 * @param bbox
	 * @return
	 */
	private ByteBuffer getByteBuffer(int[] bbox) {
		if (getPage().getHandle() > 0) {
			if (buffer == null) {
				if (getOptions().isValid()) {
					return getByteBuffer(getPage().getHandle(), bbox);
				}
			} else {
				return buffer;
			}
		}
		return null;
	}

	/**
	 * Free a byte buffer resource
	 * 
	 * @param buffer
	 */
	private void freeByteBuffer() {
		if (getPage().getHandle() > 0) {
			if (buffer != null) {
				if (buffer.isDirect()) {
					buffer.clear();
					freeByteBuffer(getPage().getHandle(), buffer);
					buffer = null;
				}
			}
		}
	}	

	/* */
	public PagePixels clone() {
		PagePixels pix = null;
		try {
			Page p = getPage().getDocument().getPage(getPage().getPageNumber());
			pix = p.getPagePixels();
			pix.getOptions().setZoom(getOptions().getZoom());
			pix.getOptions().setRotate(getOptions().getRotate());
			pix.getOptions().setAntiAlias(getOptions().getAntiAlias());
			pix.getOptions().setGamma(getOptions().getGamma());
			pix.getOptions().setImageType(getOptions().getImageType());
		} catch (PageException e) {
			e.printStackTrace();
		}
		return pix;
	}

	/**
	 * Dispose of resources
	 */
	public void dispose() {
		if (image != null) {
			image.flush();
		}
		pixels = null;
		buffer = null;
	}
	
}
