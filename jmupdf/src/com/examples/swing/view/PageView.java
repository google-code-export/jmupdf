/*
 * 
 * See copyright file
 *  
 */
package com.examples.swing.view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JComponent;

import com.jmupdf.enums.ImageType;
import com.jmupdf.interfaces.Page;
import com.jmupdf.page.PageRenderer;

/**
 * Single Page View
 * 
 * @author Pedro J Rivera
 *
 */
public class PageView extends JComponent {
	private static final long serialVersionUID = 1L;	
	private static final int shadowSize = 3;	
    private int x;
    private int y;
	private int width;
    private int height;
    private Rectangle rect;
    private BufferedImage scaledImage;
    private PageRenderer renderer;
    
    /**
     * Constructor
     */
    public PageView() {
    	x = 0;
    	y = 0;
    	width = 0;
    	height = 0;
    	//this.setDoubleBuffered(true);
    }

    /**
     * Remove rectangle
     */
    public void unSetRect() {
    	rect = null;
    }

    /**
     * Set rectangle
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public void setRect(int x, int y, int w, int h) {
    	if (rect == null) {
    		rect = new Rectangle();
    	}
    	rect.setRect(x, y, w, h);
    }

    /**
     * Get PdfPage object
     * @return
     */
    public Page getPage() {
    	return renderer.getPage();
    }
    
    /**
     * Get PdfRenderer object
     * @return
     */
    public PageRenderer getRenderer() {
    	return renderer;
    }
    
    /**
     * Get scaled image
     * @return
     */
    public BufferedImage getScaledImage() {
    	return scaledImage;
    }
    
    /**
     * Is page rendered?
     * @return
     */
    public boolean isPageRendered() {
    	return renderer.isPageRendered();
    }
    
    /**
     * Is page being rendering?
     * @return
     */
    public boolean isPageRendering() {
    	return renderer.isPageRendering();
    }

    /**
     * Set page object to view
     * @param page
     */
    public void setPage(Page page, float zoom, int rotate, ImageType color, int antiAliasLevel, float gamma) {
    	if (renderer == null) {        	
    		renderer = new PageRenderer(page, zoom, rotate, color);
        	renderer.setComponent(this);
    	} else {
    		renderer.setPage(page);
    		renderer.setZoom(zoom);
    		renderer.setRotation(rotate);
    		renderer.setColorType(color);
    	}
		
    	renderer.setAntiAliasLevel(antiAliasLevel);
		renderer.setGamma(gamma);
		
    	unSetRect();
    	
    	if (renderer.isPageRendered()) {
    		repaint();
    	} else {
    		renderer.render(false);
    		synchronized (this) {
	    		try {
	    			if (getScaledImage() == null) {
	    				wait(250);
	    			}
				} catch (InterruptedException e) {}
    		}
			if (!renderer.isPageRendered()) {
				repaint();					
			}
    	}
    }

    /**
     * Paint component
     */
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);

    	Graphics2D g2 = (Graphics2D) g;

		boolean pageRendering = isPageRendering();
		
		//Rectangle r = g.getClipBounds();
		
		String msg = null;
        String msg2 = null;
        
		if (pageRendering) {
        	if (getScaledImage() == null) {
        		msg = "Rendering page, please wait. . .";
        	}
        } else {
        	if (isPageRendered()) {
				width = getRenderer().getWidth();
				height = getRenderer().getHeight();
				//log("page: " + width+", "+height);
        	} else {
            	msg = "Error, could not render page.";
            	msg2 = "Please try a lower zoom level.";
        	}
        	if (getScaledImage() != null) { 
        		scaledImage.flush();
        		scaledImage = null;
        	}
        }

		if (msg != null) {
			FontMetrics fm = g2.getFontMetrics();
			width = fm.stringWidth(msg) + 20;
			height = 65;
		}

		x = (getParent().getWidth()-width) / 2;
        if (x<0)
        	x = 0;

        y = (getParent().getHeight()-height) / 2;
        if (y<0)
        	y = 0;
        
        // paper shadow
        g2.setColor(Color.GRAY);
        g2.fillRect(shadowSize+x, height+y, width, shadowSize);
        g2.fillRect(width+x, shadowSize+y, shadowSize, height);

        // paper
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, width, height);
        //g2.fillRect(x+r.x, y+r.y, width-(r.x), height-(r.y)); 

        try {
        	
            if (pageRendering) {
            	if (getScaledImage() != null) {
            		g2.drawImage(getScaledImage(), x, y, width, height, this);
            		//g2.drawImage(getScaledImage().
            		//		getSubimage(r.x, r.y, width-r.x, height-r.y), 
	            	//		x+r.x, y+r.y, this);
            	} else {
            		g2.setColor(Color.BLACK);        	
            		g2.drawString(msg, x+10, y+((height+10)/2));        		
            	}
            } else {
            	if (isPageRendered()) {
            		g2.drawImage(renderer.getImage(), x, y, this);
            		//g2.drawImage(renderer.getImage().
            		//		getSubimage(r.x, r.y, width-r.x, height-r.y), 
	            	//		x+r.x, y+r.y, this);
            	} else {
                	g2.setColor(Color.RED);
                	g2.drawString(msg, x+10, y+((height-10)/2));
                	g2.drawString(msg2, x+10, y+((height+30)/2));
            	}        	
            }
            
            // paper border
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, width, height);

            // Set rectangle for text selection
            if (rect != null) {
               	g2.setColor(Color.BLUE);        	
            	g2.drawRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
            	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            	g2.fillRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());        	
            }
            
		} catch (OutOfMemoryError e) {
			renderer.dispose();
	   		System.gc();
	   		repaint();
		} catch (Exception e) {
			renderer.dispose();
			repaint();
		} finally {
	        g2.dispose();
	        setPreferredSize(getPreferredSize());
	        revalidate();			
		}

    }

    /**
     * Scale current image
     * @param zoom
     */
    public void scaleImage(float zoom) throws OutOfMemoryError {
		
    	if (getRenderer().getImage() == null) {
			if (scaledImage != null) {
				scaledImage.flush();
				scaledImage = null;				
			}
			return;
		}

    	float z = getRenderer().getZoom();
    	BufferedImage tmp = new BufferedImage(getRenderer().getWidth(), getRenderer().getHeight(), getRenderer().getImage().getType());
    	WritableRaster src = getRenderer().getImage().getRaster();
    	WritableRaster dst = tmp.getRaster();

    	dst.setDataElements(0, 0, src);

    	width = (int)(tmp.getWidth() / z * zoom);
    	height = (int)(tmp.getHeight() / z * zoom);

		scaledImage = new BufferedImage(width, height, tmp.getType());

		Graphics2D g = (Graphics2D)scaledImage.getGraphics();
    	g.drawImage(tmp, 0, 0, width, height, null);
		g.dispose();

    }
    
    /**
     * Get preferred size
     */
    public Dimension getPreferredSize() {
    	return new Dimension(width+shadowSize, height+shadowSize);
    }

    /**
     * Get image width
     */
    public int getImageWidth() {
    	return getRenderer().getWidth();
    }
    
    /**
     * Get image height
     */
    public int getImageHeight() {
    	return getRenderer().getHeight();
    }
    
    /**
     * Dispose of any objects
     */
    public void dispose() {
    	if (getScaledImage() != null) {
    		getScaledImage().flush();
    		scaledImage = null;
    	}
    	if (getRenderer() != null) {
    		getRenderer().dispose();
    		renderer = null;
    	}
    }

    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }
    
}
