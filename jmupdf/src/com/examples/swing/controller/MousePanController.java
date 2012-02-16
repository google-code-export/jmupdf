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

package com.examples.swing.controller;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JViewport;

import com.examples.swing.util.BareBonesBrowserLaunch;
import com.examples.swing.util.ImageSelection;
import com.examples.swing.view.MainView;
import com.examples.swing.view.PageView;
import com.jmupdf.page.PageLinks;
import com.jmupdf.page.PagePixels;
import com.jmupdf.page.PageRect;

/**
 * Mouse Panning Controller for View Port
 * 
 * @author Pedro J Rivera
 *
 */
public class MousePanController implements MouseListener, MouseMotionListener, ActionListener, AdjustmentListener {	
	private MainView view;
	private JViewport viewPort;
	private JComponent component;
	private Point pointStr;
	private Point pointEnd;
	private Point pointMove;
	private Rectangle rectViewPort;
	private PageView pageView;
	private PageLinks link;

	private boolean leftButtonPressed = false;
	private boolean rightButtonPressed = false;

	private final Cursor cursorDefault = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private final Cursor cursorHand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	private final Rectangle rectNewPos = new Rectangle();
	
	/**
	 * Listener for document panning inside of scroll pane
	 */
	public MousePanController(MainView view) {
		this.view = view;
		this.pageView = view.getPageCanvas();
		pointMove = new Point();
	}

	//
	// MouseListener
	//
	
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			pointStr = new Point(e.getX(), e.getY());
			component.setCursor(cursorHand);
			leftButtonPressed = true;
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			pointStr = new Point(e.getX(), e.getY());
			pointEnd = new Point(e.getX(), e.getY());
			rightButtonPressed = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (leftButtonPressed) {
			if (link != null) {
				if (link.getType() == PageLinks.LINK_URL) {
					BareBonesBrowserLaunch.openURL(link.getDestination());
				} else if (link.getType() == PageLinks.LINK_GOTO) {
					view.getPageNumber().setValue(Integer.valueOf(link.getDestination())+1);
				}
				link = null;
			}
			component.setCursor(cursorDefault);
			leftButtonPressed = false;
		}
		if (rightButtonPressed) {
			Point p = normalizePoint(e.getX(), e.getY());
			view.getPopoupMenu().show(component, p.x, p.y);
			rightButtonPressed = false;
		}	
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	//
	// MouseMotionListener
	//
	
	public void mouseDragged(MouseEvent e) {		
		if (leftButtonPressed) {
			link = null;
			processPanning(e);
		}
		if (rightButtonPressed) {			
			processRectSelect(e);
		}
	}

	public void mouseMoved(MouseEvent e) {				
		if (component == null || viewPort == null) {
			viewPort = (JViewport)e.getComponent();
			component = (JComponent)viewPort.getComponent(0);			
		}
		if (view.getPageCanvas().isPageRendered()) {
			processLinks(e);
		}
	}

	//
	// ActionListener 
	//
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
        
		if (source == null) {
        	return;
        }

		if (source.equals(view.getItemCopyText())) {
			processCopy(false);
		}
		
		else if (source.equals(view.getItemCopyImage())) {
			processCopy(true);
		}
		
		else if (source.equals(view.getItemCopyCancel())) {
			pageView.unSetRect();
			pageView.repaint();
		}

	}

	//
	// AdjustmentListener
	//
	
	public void adjustmentValueChanged(AdjustmentEvent e) {		
		//rectViewPort = viewPort.getViewRect();
		//pageView.setPaintRect(rectViewPort.x, rectViewPort.y, rectViewPort.width, rectViewPort.height);
		
		//Point p1 = normalizePointInImage(rectViewPort.x, rectViewPort.y);
		//Point p2 = normalizePointInImage(rectViewPort.x+rectViewPort.width, rectViewPort.y+rectViewPort.height);
		
		//log(rectViewPort.x + ", " +  rectViewPort.y + ", " + rectViewPort.width + ", " + rectViewPort.height);
		//log(p1.x + ", " +  p1.y + ", " + p2.x + ", " + p2.y);
		
	}
	
	//
	// ---------------------
	//

	/**
	 * Process mouse panning
	 * @param e
	 */
	private void processPanning(MouseEvent e) {
		pointMove.setLocation(e.getX() - pointStr.x, e.getY() - pointStr.y);
		pointStr.setLocation(new Point(e.getX(), e.getY()));
		rectViewPort = viewPort.getViewRect();
		rectNewPos.setRect(rectViewPort.x - pointMove.x, 
						   rectViewPort.y - pointMove.y, 
						   rectViewPort.width, 
						   rectViewPort.height);
		component.scrollRectToVisible(rectNewPos);
	}

	/**
	 * Create a rectangle while mouse drags while right button pressed in viewer
	 * @param e
	 */
	private void processRectSelect(MouseEvent e) {
		pointEnd = new Point(e.getX(), e.getY());
		int x = Math.min(pointStr.x, pointEnd.x);
		int y = Math.min(pointStr.y, pointEnd.y);
		int w = Math.max(pointEnd.x, pointStr.x) - x;
		int h = Math.max(pointEnd.y, pointStr.y) - y;
		Point p = normalizePoint(x, y);
		pageView.setRect(p.x, p.y, w, h);
		pageView.repaint();
	}

	/**
	 * Make any available links accessible 
	 * @param e
	 */
	private void processLinks(MouseEvent e) {
		PageLinks[] links = pageView.getPage().getLinks(pageView.getRenderer().getPagePixels());
		
		if (links == null || links.length == 0) {
			return;
		}
		
		boolean found = false;
		
		Point p = normalizePointInImage(e.getX(), e.getY());
		link = null;	
		for(int i=0; i < links.length; i++) {
			if (p.getX() >= links[i].getX0() && p.getX() <= links[i].getX1() && 
				p.getY() >= links[i].getY0() && p.getY() <= links[i].getY1() ) {					
				link = links[i];
				found = true;
				break;
			}
		}

		if (found && link != null) {
			if (!component.getCursor().equals(cursorHand)) {
				component.setCursor(cursorHand);
			}
		} else {
			if (!component.getCursor().equals(cursorDefault)) {
				component.setCursor(cursorDefault);
			}			
		}		
	}
	
	/**
	 * Copy text or image from selected region
	 * @param e
	 */
	private void processCopy(boolean copyAsImage) {
		
		// Normalize points
		Point p1 = normalizePointInImage(pointStr.getX(), pointStr.getY());
		Point p2 = normalizePointInImage(pointEnd.getX(), pointEnd.getY());

		// Make sure we are not outside image area
		p1.setLocation(
				Math.min(Math.max(p1.x, 0), pageView.getImageWidth()), 
				Math.min(Math.max(p1.y, 0), pageView.getImageHeight()) );
		p2.setLocation(
				Math.min(Math.max(p2.x, 0), pageView.getImageWidth()), 
			    Math.min(Math.max(p2.y, 0), pageView.getImageHeight()) );
		
		int x = Math.min(p1.x, p2.x);
		int y = Math.min(p1.y, p2.y);
		int w = Math.max(p1.x, p2.x) - x;
		int h = Math.max(p1.y, p2.y) - y;
		
		// Create clip board object
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();

		if (copyAsImage) {
			float zoom = pageView.getRenderer().getZoom();
			PageRect rect = new PageRect(x/zoom, y/zoom, (x+w)/zoom, (y+h)/zoom);
			rect = rect.rotate(
					pageView.getPage().getBoundBox(), 
					pageView.getRenderer().getPagePixels().getRotation(), 
					pageView.getPage().getRotation());

			// Render region
			PagePixels r = new PagePixels(pageView.getPage());
			r.setAntiAliasLevel(pageView.getRenderer().getAntiAliasLevel());
			r.setGamma(pageView.getRenderer().getGamma());
			r.setColor(pageView.getRenderer().getColorType());
			r.setRotate(pageView.getRenderer().getRotation());			
			r.setZoom(zoom);
			r.drawPage(rect.getX0(), rect.getY0(), rect.getX1(), rect.getY1());

			// Copy to clip board
			ImageSelection is = new ImageSelection(r.getImage());			
			cb.setContents(is, null);
			r.dispose();
		} else {
			//log("cor: " + x + "," + y + "," +  w + "," +  h);
			String text = pageView.getPage().getText(pageView.getRenderer().getPagePixels(), x, y, w, h);
			StringSelection ss = new StringSelection(text);
			cb.setContents(ss, null);
		}

		pageView.unSetRect();
		pageView.repaint();
	}

	/**
	 * Adjust x,y points within image area.
	 * This will force x,y to fall within image area making the upper left x,y of the
	 * image to be 0,0 within the view port area.  
	 * @param x
	 * @param y
	 * @return
	 */
	private Point normalizePointInImage(double x, double y) {
		double x1 = (component.getParent().getWidth() - pageView.getImageWidth()) / 2;
		double y1 = (component.getParent().getHeight() - pageView.getImageHeight()) / 2;

		x1 = x - Math.max(x1, 0);
		y1 = y - Math.max(y1, 0);
		
		return normalizePoint(x1, y1);
	}

	/**
	 * Adjust x,y points to account for panning
	 * @param x
	 * @param y
	 * @return
	 */
	private Point normalizePoint(double x, double y) {
		return new Point(
				(int)(x + viewPort.getViewPosition().getX()), 
				(int)(y + viewPort.getViewPosition().getY()));
	}

    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }

}
