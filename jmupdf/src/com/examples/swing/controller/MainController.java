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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.examples.swing.util.FileChooser;
import com.examples.swing.view.DocInfoView;
import com.examples.swing.view.MainView;
import com.jmupdf.cbz.CbzDocument;
import com.jmupdf.document.Document;
import com.jmupdf.enums.DocumentType;
import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;
import com.jmupdf.print.PrintServices;
import com.jmupdf.xps.XpsDocument;

/**
 * MainController Class
 * 
 * @author Pedro J Rivera
 *
 */
public class MainController implements ActionListener, ChangeListener, WindowListener {
	private MainView view;
	private MousePanController mousePanController;
	private Document document;
	private Page page;
	private PrintServices printService;
	private float zoom = 1f;
	private int antiAliasLevel = 8;
	private float gammaLevel = 1f;
	private ImageType color; // = Document.IMAGE_TYPE_ARGB;
	private boolean isOpened = false;
	private boolean isZooming = false;
	private int maxStore = 60; 
	
	/**
	 * Main controller
	 * @param view
	 */
	public MainController(MainView view) {
		this.view = view;		
		view.setActionListeners(this);
		view.setWindowListener(this);		
		view.setChangeListener(this);
		view.setTitle(null);
		view.clearPageCanvas();
		view.show();
	}

	// ----------------------------------------------------
	// WindowListener Interface
	// ----------------------------------------------------

	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	
	public void windowClosing(WindowEvent e) {
		if (printService != null && !printService.isPrintJobDone()) { 
			printService.cancel();
			printService.waitForPrintJobDone();
		}
		System.exit(0);
	}

	// ----------------------------------------------------
	// ChangedListener Interface
	// ----------------------------------------------------

	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		
		if (source == null || !isOpened) {
        	return;
        }
	
		if (source.equals(view.getPageNumber())) {
			int p = getPageNumber();
			if ( p < 1 ) {
				view.getPageNumber().setValue(Integer.valueOf("1"));
			} 
			else if ( p > document.getPageCount() ) {
				view.getPageNumber().setValue(Integer.valueOf(document.getPageCount()));
			} else {
				if (p != page.getPageNumber()) {
					if (!view.getPageCanvas().isPageRendering()) {
						setPage();
					} else {
						view.getPageNumber().setValue(Integer.valueOf(page.getPageNumber()));
					}
				}
			}
		}
		
	}

	// ----------------------------------------------------
	// ActionListener Interface
	// ----------------------------------------------------

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
   
		if (source == null) {
        	return;
        }

		if (source.equals(view.getItemExit())) {
			closeDoc();
			view.close();
		}
		
		else if (source.equals(view.getItemDocInfo())) {			
			showDocInfo();
		}
		
		else if (source.equals(view.getItemClose())) {			
			closeDoc();
		}

		else if (source.equals(view.getItemOpen()) || source.equals(view.getOpen())) {
			openDoc();
		}
		
		else if (source.equals(view.getItemPrint()) || source.equals(view.getPrint())) {
			printDoc();
		}

		else if (source.equals(view.getFstPage())) {
			view.getPageNumber().setValue(Integer.valueOf(1));
		}
		
		else if (source.equals(view.getLstPage())) {
			view.getPageNumber().setValue(Integer.valueOf(document.getPageCount()));
		}

		else if (source.equals(view.getNxtPage())) {
			if (page.getPageNumber() < document.getPageCount()) {
				int p = page.getPageNumber() + 1;
				view.getPageNumber().setValue(Integer.valueOf(p));
			}
		}

		else if (source.equals(view.getPrvPage())) {
			if (page.getPageNumber() != 1) {
				int p = page.getPageNumber() - 1;
				view.getPageNumber().setValue(Integer.valueOf(p));
			}
		}

		else if (source.equals(view.getComboZoom())) {
			zoom = getZoomLevel();
			setPage();
		}

		else if (source.equals(view.getComboAntiAlias())) {
			antiAliasLevel = getAntiAliasLevel();
			setPage();
		}
		
		else if (source.equals(view.getComboGamma())) {
			gammaLevel = getGammaLevel();
			setPage();
		}
		
		else if (source.equals(view.getComboColor())) { 
			if (view.getComboColor().getSelectedIndex() == 0) {
				color = ImageType.IMAGE_TYPE_RGB;
			} else if (view.getComboColor().getSelectedIndex() == 1) {
				color = ImageType.IMAGE_TYPE_ARGB;
			} else if (view.getComboColor().getSelectedIndex() == 2) {
				color = ImageType.IMAGE_TYPE_ARGB_PRE;
			} else if (view.getComboColor().getSelectedIndex() == 3) {
				color = ImageType.IMAGE_TYPE_BGR;
			} else if (view.getComboColor().getSelectedIndex() == 4) {
				color = ImageType.IMAGE_TYPE_GRAY;
			} else if (view.getComboColor().getSelectedIndex() == 5) {
				color = ImageType.IMAGE_TYPE_BINARY;				
			} else {
				color = ImageType.IMAGE_TYPE_BINARY_DITHER;
			}
			setPage();
		}
		
		else if (source.equals(view.getComboRotate())) {
			setPage();
		}
		
	}

	
	// ----------------------------------------------------
	// ------------------
	// ----------------------------------------------------
	
	
	/**
	 * Set page view
	 */
	private void setPage() {
		if (isOpened) {			
			page = null;
			page = document.getPage(getPageNumber());
			if (page != null) {
				try {
					if (isZooming) {
						view.getPageCanvas().scaleImage(zoom);
					}
					view.getPageCanvas().setPage(page, zoom, getRotation(), color, antiAliasLevel, gammaLevel);
				} catch (OutOfMemoryError e) {
					view.getPageCanvas().getRenderer().dispose();				
					view.getPageCanvas().repaint();
					System.gc();
				} catch (Exception e) {
				}
			}
		}
		isZooming = false;
	}
	
	/**
	 * Open Document
	 */
	private void openDoc() {
		File file = FileChooser.getPdfDocument(view.getMainFrame());
		if (file != null) {
			closeDoc();
			String pass = "";
			while (true) {
				try {
					if (FileChooser.getExtension(file).equals("pdf")) {
						document = new PdfDocument(file.toString(), pass, maxStore);	
					} else if (FileChooser.getExtension(file).equals("xps")) {
						document = new XpsDocument(file.toString(), maxStore);
					} else if (FileChooser.getExtension(file).equals("cbz")) {
						document = new CbzDocument(file.toString(), maxStore);
					}
					if (document != null && document.getHandle() > 0) {
						zoom = 1f;
						antiAliasLevel = 8;
						page = document.getPage(1);								
						view.setPageCanvas(page, zoom, Page.PAGE_ROTATE_AUTO, color, antiAliasLevel, gammaLevel);
						mousePanController = new MousePanController(view);
						view.setPanningListener(mousePanController);
						view.getPageNumber().setValue(Integer.valueOf(1));
						isOpened = true;
						break;
					}
				} catch (DocException e1) {
					e1.printStackTrace();
					break;
				} catch (DocSecurityException e1) {
					pass = JOptionPane.showInputDialog(null, "Document requires authentication:");
					if (pass == null) {
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Close Document
	 */
	private void closeDoc() {
		isOpened = false;
		view.getPageNumber().requestFocusInWindow();		
		if (document != null) {
			document.close();
			if (mousePanController != null) {
				view.removePanningListener(mousePanController);
				mousePanController = null;
			}
			view.clearPageCanvas();
			document = null;			
		}		
	}

	/**
	 * Print document
	 */
	private void printDoc() {		
		try {
			if (printService == null || printService.isPrintJobDone()) {
				printService = new PrintServices(document);
				printService.print("JMuPdf Print", "", true);
			}
		} catch (DocException e) {}
	}

	/**
	 * Show document information
	 */
	private void showDocInfo() {
		if (document.getDocumentType() == DocumentType.DOC_PDF) { 
			DocInfoView div = new DocInfoView((PdfDocument)document);
			div.setVisible(true);
		} else if (document.getDocumentType() == DocumentType.DOC_XPS) {

		}
	}
	
	/**
	 * Get page number from JSpinner
	 * @return
	 */
	private int getPageNumber() {
		return ((Integer)view.getPageNumber().getValue()).intValue();
	}
	
	/**
	 * Get proper rotation value based on current page
	 * @return
	 */
	private int getRotation() {
		int rotate = 0;
		if (view.getComboRotate().getSelectedIndex() == 0) {
			rotate = Page.PAGE_ROTATE_AUTO;
		}
		else if (view.getComboRotate().getSelectedIndex() == 1) {
			rotate = Page.PAGE_ROTATE_90_CW;
		}
		else if (view.getComboRotate().getSelectedIndex() == 2) {
			rotate = Page.PAGE_ROTATE_90_CCW;
		}
		else if (view.getComboRotate().getSelectedIndex() == 3) {
			rotate = Page.PAGE_ROTATE_180;
		}
		return rotate;
	}

	/**
	 * Get zoom level as float value
	 * @return
	 */
	private float getZoomLevel() {
		String s = (String)view.getComboZoom().getSelectedItem();
		s = s.replace("%", "");
		float f = Float.valueOf(s.trim());
		f = f / 100;
		isZooming = true;
		return f;
	}

	/**
	 * Get anti alias level as int value
	 * @return
	 */
	private int getAntiAliasLevel() {
		String s = (String)view.getComboAntiAlias().getSelectedItem();
		s = s.replace("%", "");
		int i = Integer.valueOf(s.trim());
		return i;
	}
	
	/**
	 * Get gamma level as float value
	 * @return
	 */
	private float getGammaLevel() {
		String s = (String)view.getComboGamma().getSelectedItem();
		s = s.replace("%", "");
		float f = Float.valueOf(s.trim());
		return f;
	}
	
    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }
}
	