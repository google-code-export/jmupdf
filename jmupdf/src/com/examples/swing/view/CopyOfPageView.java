/*
 * Copyright (C) 2010-2011 Pedro J Rivera
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.examples.swing.view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JComponent;

import com.jmupdf.page.Page;
import com.jmupdf.page.PageRenderer;

/**
 * Single Page View
 * 
 * @author Pedro J Rivera
 *
 */
public class CopyOfPageView extends JComponent {
	private static final long serialVersionUID = 1L;	
	private static final int shadowSize = 3;	
	private PageRenderer renderer;
    private int x;
    private int y;
	private int width;
    private int height;
    private Rectangle rect;
    private Image scaledImage;

    /**
     * Constructor
     */
    public CopyOfPageView() {
    	x = 0;
    	y = 0;
    	width = 0;
    	height = 0;
    	this.setDoubleBuffered(false);
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
    public Image getScaledImage() {
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
    public void setPage(PageRenderer renderer) {
		unSetRect();
		this.renderer = renderer;
    	if (renderer.isPageRendered()) {
    		repaint();
    	} else {
    		renderer.setComponent(this);
    		renderer.render(false);
    		synchronized (this) {
	    		try {
	    			if (getScaledImage() == null) {
	    				wait(200);
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
		
		String msg = null;
        String msg2 = null;        
        
		if (pageRendering) {
        	if (getScaledImage() == null) {
        		msg = "Rendering page, please wait. . .";
        	} else {
        		width = getScaledImage().getWidth(null);
        		height = getScaledImage().getHeight(null);
        	}
        } else {
        	if (isPageRendered()) {
				width = getRenderer().getWidth();
				height = getRenderer().getHeight();
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

        if (pageRendering) {
        	if (getScaledImage() != null) {
        		g2.drawImage(getScaledImage(), x, y, this);
        	} else {
        		g2.setColor(Color.BLACK);        	
        		g2.drawString(msg, x+10, y+((height+10)/2));        		
        	}
        } else {
        	if (isPageRendered()) {
        		g2.drawImage(renderer.getImage(), x, y, this);
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

        g2.dispose();
        
        setPreferredSize(getPreferredSize());
        revalidate();
    }

    /**
     * Get preferred size
     */
    public Dimension getPreferredSize() {
    	return new Dimension(width+shadowSize, height+shadowSize);
    }

    /**
     * Dispose of any objects
     */
    public void dispose() {
    	if (getScaledImage() != null) {
    		getScaledImage().flush();
    		scaledImage = null;
    	}
    }

    /**
     * Scale current image
     * @param zoom
     */
    public void scaleImage(final float zoom) {
    	if (getRenderer() != null) {    		
    		int w = (int)(getRenderer().getWidth() / getRenderer().getZoom() * zoom);
    		int h = (int)(getRenderer().getHeight() / getRenderer().getZoom() * zoom);
    		if (scaledImage != null) {
    			scaledImage.flush();
    			scaledImage = null;
    		}
    		if (getRenderer().getImage() != null) {
    			scaledImage = getRenderer().getImage().getScaledInstance(w, h, 0);	
    		}
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
