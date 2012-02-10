/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.JComponent;

import com.jmupdf.interfaces.DocumentTypes;
import com.jmupdf.interfaces.ImageTypes;

/**
 * PdfRenderer class.</br>
 * 
 * This class is responsible for rendering a page to a {@code BufferedImage}. </br></br>
 * 
 * The object is reusable. </br></br>
 * 
 * Implements Runnable 
 * 
 * @author Pedro J Rivera
 *
 */
public class PageRenderer implements Runnable, ImageTypes, DocumentTypes {
	private Page page;
	private PageRect origin;
	private PageRendererWorker worker;
	
	private JComponent component;
	private BufferedImage image;	
	private ByteBuffer buffer;
	private Object pixels;
	
	private float zoom;
	private float resolution;
	private int rotate;
	private int color;
	private float gamma;

	private int x;
	private int y;
	private int w;
	private int h;
	
	private float x0;
	private float y0;
	private float x1;
	private float y1;
	
	private boolean isPageRendered;
	private boolean isPageRendering;
	
	private static int default_resolution = 72;
	
	private boolean useDirectByteBuffer;
	
	/**
	 * Create renderer instance with default values. 
	 */
	public PageRenderer() {
		this(null, 1f, Page.PAGE_ROTATE_AUTO, IMAGE_TYPE_RGB);
	}
	
	/**
	 * Create renderer instance.
	 * @param zoom
	 * @param rotate
	 * @param color
	 */
	public PageRenderer(float zoom, int rotate, int color) {
		this(null, zoom, rotate, color);
	}

	/**
	 * Create renderer instance.
	 * @param page
	 * @param zoom
	 * @param rotate
	 * @param color
	 */
	public PageRenderer(Page page, float zoom, int rotate, int color) {
		this.page = page;
		this.zoom = zoom;
		this.rotate = rotate;
		this.color = color;
		this.gamma = 1f;
		this.useDirectByteBuffer = true;
		if (page == null) {
			setCroppingArea(0, 0, 0, 0);
		} else {
			setCroppingArea(page.getX(), page.getY(), page.getWidth(), page.getHeight());
		}
	}

	/**
	 * Set cropping region. Coordinates are in Java2D space.</br> 
	 * Coordinates are assumed to be in 1f zoom level. 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void setCroppingArea(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.x0 = x;
		this.y0 = y;
		this.x1 = x+w;
		this.y1 = y+h;
		this.origin = new PageRect(x0, y0, x1, y1);
		this.isPageRendering = false;
		needsRendering();
	}

	/**
	 * Set cropping region. Coordinates are in Java2D space.</br> 
	 * Coordinates are assumed to be in 1f zoom level. 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public void setCroppingArea(float x0, float y0, float x1, float y1) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		this.x = (int)x0;
		this.y = (int)y0;
		this.w = (int)(x1-x0);
		this.h = (int)(y1-y0);
		this.origin = new PageRect(x0, y0, x1, y1);
		this.isPageRendering = false;
		needsRendering();
	}

	/**
	 * Set the component to paint after rendering is complete. </br>
	 * The components repaint() method is invoked once rendering is complete.</br>
	 * If component is null no action is taken. 
	 * @param component : Can be null
	 */
	public void setComponent(JComponent component) {
		this.component = component;
	}
	
	/**
	 * Get the component this renderer paints to. 
	 * @return
	 */
	public JComponent getComponent() {
		return component;
	}
	
	/**
	 * Get X coordinate
	 * @return
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Get Y coordinate
	 * @return
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Get width
	 * @return
	 */
	public int getWidth() {
		return w;
	}

	/**
	 * Get height
	 * @return
	 */
	public int getHeight() {
		return h;
	}
	
    /**
     * Get point one x
     * @return
     */
    public float getX0() {
    	return x0;
    }
    
    /**
     * Get point one y
     * @return
     */
    public float getY0() {
    	return y0;
    }
    
    /**
     * Get point two x
     * @return
     */
    public float getX1() {
    	return x1;
    }

    /**
     * Get point two y
     * @return
     */
    public float getY1() {
    	return y1;
    }
    
	/**
	 * Get normalized rotation. </br>
	 * This will return whatever rotation is being requested 
	 * plus any default page rotations stored in the page.
	 * @return rotation
	 */
	public int getNormalizedRotation() {
		int rotate = getRotation();			
		
		if (rotate == Page.PAGE_ROTATE_AUTO) {
			rotate = getPage().getRotation();
		} else {
			if (rotate != Page.PAGE_ROTATE_NONE) {
				rotate += getPage().getRotation();
			}
		}
	    
		rotate = rotate % 360;
	    
		if (rotate < 0) { 
	    	rotate = rotate + 360;
		}
		
		return rotate;
	}

	/**
	 * Get image rotation.
	 * @return
	 */
	public int getRotation() {
		return rotate;
	}
	
	/**
	 * Set image rotation.
	 * @param rotate
	 */
	public void setRotation(int rotate) {
		if (getRotation() == rotate) {
			return;
		}
		this.rotate = rotate;
		needsRendering();
	}

	/**
	 * Get zoom level
	 * @return
	 */
	public float getZoom() {
		return zoom;
	}

	/**
	 * Set zoom level
	 * @param zoom
	 */
	public void setZoom(float zoom) {
		if (getZoom() == zoom) {
			return;
		}
		this.zoom = zoom;
		needsRendering();
	}

	/**
	 * Get color type
	 * @return
	 */
	public int getColorType() {
		return color;
	}

	/**
	 * Set color type
	 * @param color
	 */
	public void setColorType(int color) {
		if (getColorType() == color) {
			return;
		}
		this.color = color;
		needsRendering();
	}
	
	/**
	 * Get gamma correction
	 * @return
	 */
	public float getGamma() {
		return gamma;
	}

	/**
	 * Set gamma correction </br></br>
	 * 
	 * Gamma correct the output image. </br>
	 * Some typical values are 0.7 or 1.4 to thin or darken text rendering.
	 * 
	 * @param gamma
	 */
	public void setGamma(float gamma) {
		if (getGamma() == gamma) {
			return;
		}
		if (gamma <= 0) {
			gamma = 1f;
		}
		this.gamma = gamma;
		needsRendering();
	}

	/**
	 * Get anti-alias level.
	 * @return
	 */
	public int getAntiAliasLevel() {
		return getPage().getDocument().getAntiAliasLevel();
	}
	
	/**
	 * Set anti-alias level.</br>
	 * This value is used to determine what bit level is used when 
	 * applying anti-aliasing while rendering page images.</br>
	 * A value of zero turns off anti-aliasing. Maximum value is 8.
	 * @param level
	 */
	public void setAntiAliasLevel(int level) {
		if (getAntiAliasLevel() == level) {
			return;
		}
		getPage().getDocument().setAntiAliasLevel(level);
		needsRendering();
	}

	/**
	 * Get resolution
	 * @return
	 */
	public float getResolution() {
		return resolution;
	}

	/**
	 * Get page object
	 * @return
	 */
	public Page getPage() {
		return page;
	}

	/**
	 * Set page object to render
	 * @param page
	 */
	public void setPage(Page page) {
		if (getPage() != null && 
			getPage().getPageNumber() == page.getPageNumber() &&
			getPage().getDocument().getHandle() == page.getDocument().getHandle() &&
			getPage() == page) {
			return;
		}
		this.page = page;
		needsRendering();
		setCroppingArea(page.getX(), page.getY(), page.getWidth(), page.getHeight());
	}

	/**
	 * Returns true if pixel data is stored in a direct byte buffer. </br>
	 * Returns false if pixel data is stored in a java primitive type array.
	 * @return
	 */
	public boolean isDirectByteBuffer() {
		return useDirectByteBuffer;
	}
	
	/**
	 * Set whether renderer should store data in native memory for direct access </br>
	 * or use a java object instead managed by the JVM.
	 * @param b
	 */
	public void setDirectByteBuffer(boolean b) {
		this.useDirectByteBuffer = b;
	}
	
	/**
	 * Get buffered image
	 * @return
	 */
	public BufferedImage getImage() {
		if (!isPageRendered() || 
			isPageRendering()) {
			return null;
		}
		return image;
	}

	/**
	 * Determine if page is fully rendered
	 * @return
	 */
	public boolean isPageRendered() {
		return isPageRendered;
	}

	/**
	 * Determine if page is still rendering
	 * @return
	 */
	public boolean isPageRendering() {
		return isPageRendering;
	}

	/**
	 * Force rendering of page even if no values have changed
	 */
	public void needsRendering() {
		isPageRendered = false;
	}

	/**
	 * Render page in current thread or in a separate thread. </br></br>
	 * 
	 * If wait is set to FALSE then the rendering will occur in a separate thread and </br>
	 * process will return immediately. A PageRendererWorker object is created so that all </br>
	 * rendering happens in the same thread. This way a new thread isn't created on subsequent </br>
	 * calls. This is only true if the PageRenderer instance is reused. The isPageRendering() </br>
	 * and the isPageRendered() method should be used to query rendering status. </br> </br>
	 * 
	 * If wait is set to TRUE then rendering will occur in the current thread. </br></br>
	 * 
	 * PageRenderer class also implements Runnable so a different implementation can be used </br>
	 * for rendering.
	 * 
	 * @param wait
	 */
	public void render(boolean wait) {
		if (getPage() == null || isPageRendering()) {
			return;
		}
		if (wait) {
			run();
		} else {
			getWorker().renderPage(this);
		}
	}

	/**
	 * Start rendering page in current or separate thread
	 */
	public void run() {
		try {

			if (getPage() == null ||
				isPageRendering() || 
				isPageRendered()) {
				return;
			}

			isPageRendering = true;
			needsRendering();			

			if (image != null) {
				image.flush();
			}

			// Zero rotate
			PageRect c = zeroRotate(origin.getX0(), origin.getY0(), origin.getX1(), origin.getY1(), getPage().getRotation());
			
			int[] bbox = new int[4];

			// Render page 
			if (isDirectByteBuffer()) {
				buffer = getPage().getDocument().getPageByteBuffer(
						 getPage().getPageNumber(), 
						 getZoom(), 
						 getNormalizedRotation(), 
						 getColorType(),
						 getGamma(),
						 bbox, 
						 c.getX0(), 
						 c.getY0(), 
						 c.getX1(), 
						 c.getY1());
			} else {
				pixels = getPage().getDocument().getPagePixels(
						 getPage().getPageNumber(), 
						 getZoom(), 
						 getNormalizedRotation(), 
						 getColorType(),
						 getGamma(),
						 bbox, 
						 c.getX0(), 
						 c.getY0(), 
						 c.getX1(), 
						 c.getY1());
			}
			
			// Create buffered image
			if (bbox != null) {
				this.x = bbox[0];
				this.y = bbox[1];
				this.w = bbox[2];
				this.h = bbox[3];
				this.x0 = x;
				this.y0 = y;
				this.x1 = x+w;
				this.y1 = y+h;
				this.resolution = getZoom() * default_resolution;
				createBufferedImage();
				if (image != null) {
					isPageRendered = true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
	    	image = null;
    		System.gc();
		} finally {
			isPageRendering = false;			
			if (component != null) {				
				synchronized (component) {
					component.notify();
					component.repaint();
				}
			}
		}
	}

	/**
	 * Dispose of image resources and reset
	 * rendering flags. </br>
	 * Rendering object is reusable. 
	 */
	public void dispose() {
		if (image != null) {
			image.flush();
			image = null;
		}
		if (pixels != null) {
			pixels = null;
		}
		if (buffer != null) {
			buffer = null;
		}
		if (worker != null) {
			worker.shutdown();
			worker = null;
		}
		isPageRendering = false;
		needsRendering();
	}

	/**
	 * Change coordinates to zero page rotation.</br>
	 * This assumes that coordinates are based on the rotate value passed in.
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param rotate
	 * @return
	 */
	private PageRect zeroRotate(float x0, float y0, float x1, float y1, int rotate) {

		// Rotate media box to rotate value
		PageRect mb = getPage().getMediaBox().rotate(getPage().getMediaBox(), rotate);

		// Rotate coordinates back to zero rotation
		PageRect c = new PageRect(x0, y0, x1, y1);

		return c.rotate(mb, -rotate);
	}

	/**
	 * Create a buffered image
	 * @param pixels
	 */
	private void createBufferedImage() {

		// Get pixel data from buffer
		try {
			if (buffer != null) {
				if (getColorType() == IMAGE_TYPE_BINARY || 
					getColorType() == IMAGE_TYPE_BINARY_DITHER) {
					pixels = new byte[buffer.order(ByteOrder.nativeOrder()).capacity()];
					buffer.order(ByteOrder.nativeOrder()).get((byte[])pixels);
				} else {
					pixels = new int[buffer.order(ByteOrder.nativeOrder()).asIntBuffer().capacity()];
					buffer.order(ByteOrder.nativeOrder()).asIntBuffer().get((int[])pixels);
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			getPage().getDocument().freeByteBuffer(buffer);
		}
		
		// Create image buffer
		if (getColorType() == IMAGE_TYPE_BINARY || 
			getColorType() == IMAGE_TYPE_BINARY_DITHER) {
			image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_BINARY);
		} else if (getColorType() == IMAGE_TYPE_ARGB) {
			image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		} else {
			image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		}
		
		// Set data to image
	    if (image != null) {
	    	WritableRaster raster = image.getRaster();
	    	raster.setDataElements(getX(), getY(), getWidth(), getHeight(), pixels);
	    }
	    
	}
	
	/**
	 * Get a page renderer worker
	 * @return
	 */
	private PageRendererWorker getWorker() {
		if (worker == null || !worker.isWorkerActive()) {
			worker = new PageRendererWorker();
		}
		return worker;
	}

    /**
     * Print test messages
     * @param text
     */
    protected static void log(String text) {
    	System.out.println(text);
    }

}
