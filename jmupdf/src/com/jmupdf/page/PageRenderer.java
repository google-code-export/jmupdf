/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import com.jmupdf.enums.ImageType;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PagePixels;

/**
 * PdfRenderer class.</br></br>
 * 
 * This is a helper class to render pages either in current thread or in the background. </br></br>
 * 
 * Implements Runnable 
 * 
 * @author Pedro J Rivera
 *
 */
public class PageRenderer implements Runnable {
	private PagePixels pagePixels;
	private PageRect boundBox;
	private PageRendererWorker worker;
	private JComponent component;
	private boolean isPageRendered;
	private boolean isPageRendering;
	
	/**
	 * Create renderer instance with default values. 
	 */
	public PageRenderer() {
		this(null, 1f, Page.PAGE_ROTATE_AUTO, ImageType.IMAGE_TYPE_RGB);
	}
	
	/**
	 * Create renderer instance.
	 * @param zoom
	 * @param rotate
	 * @param color
	 */
	public PageRenderer(float zoom, int rotate, ImageType color) {
		this(null, zoom, rotate, color);
	}

	/**
	 * Create renderer instance.
	 * @param page
	 * @param zoom
	 * @param rotate
	 * @param color
	 */
	public PageRenderer(Page page, float zoom, int rotate, ImageType color) {
		this.boundBox = new PageRect();
		setZoom(zoom);
		setRotation(rotate);
		setColorType(color);
		setGamma(1f);
		setPage(page);
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
		setCroppingArea((float)x, (float)y, (float)x+w, (float)y+h);
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
		if (!isPageRendering()) {
			this.boundBox.setRect(x0, y0, x1, y1);
			this.isPageRendering = false;
			needsRendering();
		}
	}

	/**
	 * Set the component to paint after rendering is complete. </br>
	 * The components repaint() method is invoked once rendering is complete.</br>
	 * If component is null no action is taken. 
	 * @param component : Can be null
	 */
	public void setComponent(JComponent component) {
		if (!isPageRendering()) {
			this.component = component;
		}
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
		return boundBox.getX();
	}
	
	/**
	 * Get Y coordinate
	 * @return
	 */
	public int getY() {
		return boundBox.getY();
	}
	
	/**
	 * Get width
	 * @return
	 */
	public int getWidth() {
		return boundBox.getWidth();
	}

	/**
	 * Get height
	 * @return
	 */
	public int getHeight() {
		return boundBox.getHeight();
	}
	
    /**
     * Get point one x
     * @return
     */
    public float getX0() {
    	return boundBox.getX0();
    }
    
    /**
     * Get point one y
     * @return
     */
    public float getY0() {
    	return boundBox.getY0();
    }
    
    /**
     * Get point two x
     * @return
     */
    public float getX1() {
    	return boundBox.getX1();
    }

    /**
     * Get point two y
     * @return
     */
    public float getY1() {
    	return boundBox.getY1();
    }

	/**
	 * Get image rotation.
	 * @return
	 */
	public int getRotation() {
		if (getPagePixels() == null) {
			return 0;
		}
		return getPagePixels().getOptions().getRotate();
	}
	
	/**
	 * Set image rotation.
	 * @param rotate
	 */
	public void setRotation(int rotate) {
		if (getRotation() == rotate) {
			return;
		}
		if (!isPageRendering()) {			
			if (getPagePixels() != null) {
				getPagePixels().getOptions().setRotate(rotate);
			}
			needsRendering();
		}
	}

	/**
	 * Get zoom level
	 * @return
	 */
	public float getZoom() {
		if (getPagePixels() == null) {
			return 1f;
		}
		return getPagePixels().getOptions().getZoom();
	}

	/**
	 * Set zoom level
	 * @param zoom
	 */
	public void setZoom(float zoom) {
		if (getZoom() == zoom) {
			return;
		}
		if (!isPageRendering()) {
			if (getPagePixels() != null) {
				getPagePixels().getOptions().setZoom(zoom);
			}
			needsRendering();
		}
	}

	/**
	 * Get color type
	 * @return
	 */
	public ImageType getColorType() {	
		if (getPagePixels() == null) {
			return ImageType.IMAGE_TYPE_RGB;
		}
		return getPagePixels().getOptions().getImageType();
	}

	/**
	 * Set color type
	 * @param color
	 */
	public void setColorType(ImageType color) {
		if (getColorType() == color) {
			return;
		}
		if (!isPageRendering()) {
			if (getPagePixels() != null) {
				getPagePixels().getOptions().setImageType(color);
			}
			needsRendering();
		}
	}
	
	/**
	 * Get gamma correction
	 * @return
	 */
	public float getGamma() {
		if (getPagePixels() == null) {
			return 1f;
		}
		return getPagePixels().getOptions().getGamma();
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
		if (!isPageRendering()) {			
			if (getPagePixels() != null) {
				getPagePixels().getOptions().setGamma(gamma);
			}
			needsRendering();
		}
	}

	/**
	 * Get anti-alias level.
	 * @return
	 */
	public int getAntiAliasLevel() {
		if (getPagePixels() == null) {
			return 0;
		}
		return getPagePixels().getOptions().getAntiAlias();
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
		if (!isPageRendering()) {
			if (getPagePixels() != null) {
				getPagePixels().getOptions().setAntiAlias(level);
			}
			needsRendering();
		}
	}

	/**
	 * Get resolution
	 * @return
	 */
	public float getResolution() {
		if (getPagePixels() == null) {
			return 0;
		}
		return getPagePixels().getOptions().getResolution();
	}

	/**
	 * Get page object
	 * @return
	 */
	public Page getPage() {
		if (getPagePixels() == null) {
			return null;
		}
		return getPagePixels().getPage();
	}

	/**
	 * Set page object to render
	 * @param page
	 */
	public void setPage(Page page) {
		if (!isPageRendering()) {
			setPagePixels(page);
			if (page == null) {
				setCroppingArea(0, 0, 0, 0);
			} else {
				setCroppingArea(page.getX(), page.getY(), page.getWidth(), page.getHeight());
			}
			needsRendering();
		}
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
		return getPagePixels().getImage();
	}

	/**
	 * Get page pixels object
	 * @return
	 */
	public PagePixels getPagePixels() {
		return pagePixels;
	}
	
	/**
	 * Set page pixels object
	 */
	public void setPagePixels(Page page) {
		if (!isPageRendering()) {
			if (pagePixels != null) {
				pagePixels.dispose();
			}
			if (page != null) {
				pagePixels = page.getPagePixels();
				pagePixels.getOptions().setAntiAlias(getAntiAliasLevel());
				pagePixels.getOptions().setImageType(getColorType());
				pagePixels.getOptions().setGamma(getGamma());
				pagePixels.getOptions().setRotate(getRotation());
				pagePixels.getOptions().setZoom(getZoom());
			}
			needsRendering();
		}
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
		if (!isPageRendering()) {
			if (wait) {
				run();
			} else {
				getWorker().renderPage(this);
			}
		}
	}

	/**
	 * Start rendering page in current or separate thread
	 */
	public void run() {
		try {
			if (isPageRendering() || 
				isPageRendered()) {
				return;
			}

			isPageRendering = true;
			needsRendering();			

			getPagePixels().drawPage(null, getX0(), getY0(), getX1(), getY1());
			
			PageRect bb = getPagePixels().getOptions().getBoundBox();
			boundBox.setRect(bb.getX0(), bb.getY0(), bb.getX1(), bb.getY1());
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
    		System.gc();
		} finally {
			isPageRendering = false;
			isPageRendered = true;
			if (getComponent() != null) {				
				synchronized (component) {
					getComponent().notify();
					getComponent().repaint();
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
		if (worker != null) {
			worker.shutdown();
			worker = null;
		}
		if (pagePixels != null) {
			pagePixels.dispose();
			pagePixels = null;
		}
		isPageRendering = false;
		needsRendering();
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
