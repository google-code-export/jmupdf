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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

import com.examples.swing.resources.Images;
import com.jmupdf.JmuPdf;
import com.jmupdf.enums.ImageType;
import com.jmupdf.interfaces.Page;

/**
 * Main View
 * 
 * @author Pedro J Rivera
 * 
 */
public class MainView {
	private JFrame mainFrame;
	private JSplitPane splitPane;
	private JScrollPane scrollPane;

	private JMenuBar menuBar;
	
	private JMenu menuFile;	
    private JMenuItem itemExit;
    private JMenuItem itemOpen;
    private JMenuItem itemClose;
    private JMenuItem itemPrint;
    private JMenuItem itemDocInfo;
    
    private JPopupMenu popupMenu;
    private JMenuItem itemCopyText;
    private JMenuItem itemCopyImage;
    private JMenuItem itemCopyCancel;

    private JComboBox comboRotate;
    private JComboBox comboColor;
    private JComboBox comboZoom;
    private JComboBox comboAntiAlias;
    private JComboBox comboGamma;
    private JSpinner pageNumber;
    private JLabel pageCount;
    private JButton open;
    private JButton print;
    private JButton prvPage;
    private JButton nxtPage;
    private JButton fstPage;
    private JButton lstPage;
    
    private PageView pageView;

    private final String[] colors = {"RGB", "ARGB", "ARGB_PRE", "BGR", "Gray Scale", "Black & White", "Black & White w/Dither"};
    private final String[] rotate = {"Auto", "90 CW", "90 CCW", "180"};
    private final String[] zoomLevels = {"5", "10", "25", "50", "75", "100", "125", "150", "200", "250", "300", "350", "400", "450", "500", "550", "600", "1200", "2400", "4800", "6400"};
    private final String[] antiAliasLevels = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
    private final String[] gamma = {"0.5", "0.7", "1.0", "1.5", "1.7", "2"};
    private final int defaultZoomIndex = 5;

	/**
	 * Class Constructor
	 */
	public MainView() {
		createUI();
	}
	
	/**
	 * Create User Interface
	 */
	private void createUI() {
		setLookAndFeel();
		buildMainFrame();
		buildSplitPane();
		buildMenuBar();
		buildToolBar();
		buildPopupMenu();
	}
	
	/**
	 * Set default look and feel  
	 */
	private void setLookAndFeel() {		
		try {
			String os = System.getProperty("os.name");
			if (os.equals("Linux")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());	
			}
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());				
			} catch (Exception e2) {
				// Well, we did the best we could!
			}
		}
	}

	/**
	 * Build main window frame
	 */
	private void buildMainFrame() {
		this.mainFrame = new JFrame();
		this.mainFrame.setSize(800, 700);
		this.mainFrame.setLocationRelativeTo(null);
		this.mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	/**
	 * Build split pane component
	 */
	private void buildSplitPane() {
		this.splitPane = new JSplitPane();
		this.scrollPane = new JScrollPane();
		this.splitPane.setRightComponent(this.scrollPane);		
		this.scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		
		this.splitPane.getLeftComponent().setVisible(false);
		this.splitPane.setDividerSize(0);
		
		//this.mainFrame.getContentPane().add(this.splitPane, BorderLayout.CENTER);
		this.mainFrame.getContentPane().add(this.splitPane);
	}

	/**
	 * Build main menu bar
	 */
	private void buildMenuBar() {
		this.menuBar = new JMenuBar();
		this.menuFile = new JMenu("File");
		//this.menuFile = new JMenu("View");

	    itemOpen = new JMenuItem("Open");	    
	    itemPrint = new JMenuItem("Print");
	    itemDocInfo = new JMenuItem("Properties...");
	    itemClose = new JMenuItem("Close");
	    itemExit = new JMenuItem("Exit");

	    itemOpen.setIcon(Images.getImageIcon("open.gif"));
	    itemPrint.setIcon(Images.getImageIcon("print.gif"));

	    this.menuFile.add(itemOpen);	    
	    this.menuFile.add(new JSeparator());
	    this.menuFile.add(itemDocInfo);
	    this.menuFile.add(new JSeparator());
	    this.menuFile.add(itemPrint);
	    this.menuFile.add(new JSeparator());
	    this.menuFile.add(itemClose);
	    this.menuFile.add(new JSeparator());
	    this.menuFile.add(itemExit);

	    this.menuBar.add(this.menuFile);	    
	    this.mainFrame.setJMenuBar(this.menuBar);
	    
	    itemClose.setEnabled(false);
	}
	
	/**
	 * Build main tool bar
	 */
	private void buildToolBar() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JToolBar toolBar1 = new JToolBar();				
		JToolBar toolBar2 = new JToolBar();
		
		Insets margins = new Insets(0, 0, 0, 0);
		
		/*
		 * Open button 
		 */
		this.open = new JButton(Images.getImageIcon("open.gif"));		
		this.open.setMargin(margins);
		toolBar1.add(open);
		
		/*
		 * Print button 
		 */
		this.print = new JButton(Images.getImageIcon("print.gif"));		
		this.print.setMargin(margins);
		toolBar1.add(print);

		/*
		 * Page navigation buttons |< << >> >|
		 */
		toolBar1.addSeparator();		
		this.fstPage = new JButton(Images.getImageIcon("first.gif"));
		this.prvPage = new JButton(Images.getImageIcon("prev.gif"));
		this.nxtPage = new JButton(Images.getImageIcon("next.gif"));		
		this.lstPage = new JButton(Images.getImageIcon("last.gif"));		
		this.fstPage.setMargin(margins);
		this.prvPage.setMargin(margins);
		this.nxtPage.setMargin(margins);		
		this.lstPage.setMargin(margins);
		toolBar1.add(fstPage);
		toolBar1.add(prvPage);
		toolBar1.add(nxtPage);		
		toolBar1.add(lstPage);

		/*
		 * Page Number Spinner
		 */
		toolBar1.addSeparator();
		this.pageCount = new JLabel();
		this.pageNumber = new JSpinner();
		Dimension d = new Dimension(this.pageNumber.getPreferredSize());
		d.setSize(60, d.getHeight());
		this.pageNumber.setMaximumSize(d);
		toolBar1.add(new JLabel("Page: "));		
		toolBar1.add(this.pageNumber);
		toolBar1.add(new JLabel(" of "));
		toolBar1.add(this.pageCount);
		
		/*
		 * Zoom level combo box
		 */
		toolBar1.addSeparator();		
		JLabel zoomText = new JLabel("Zoom:");
		this.comboZoom = new JComboBox(zoomLevels);
		d = new Dimension(comboZoom.getPreferredSize());
		d.setSize(60, d.getHeight());
		this.comboZoom.setMaximumSize(d);
		toolBar1.add(zoomText);
		toolBar1.add(comboZoom);
		
		/*
		 * Page rotate combo box
		 */
		toolBar1.addSeparator();		
		JLabel rotateText = new JLabel("Rotate:");
		this.comboRotate = new JComboBox(rotate);
		d = new Dimension(comboRotate.getPreferredSize());
		d.setSize(65, d.getHeight());
		this.comboRotate.setMaximumSize(d);
		toolBar1.add(rotateText);
		toolBar1.add(comboRotate);
		
		/*
		 * Anti Alias Level
		 */
		JLabel antiAliasText = new JLabel("Antialias Level:");
		this.comboAntiAlias = new JComboBox(antiAliasLevels);
		d = new Dimension(comboAntiAlias.getPreferredSize());
		d.setSize(40, d.getHeight());
		this.comboAntiAlias.setMaximumSize(d);
		toolBar2.add(antiAliasText);
		toolBar2.add(comboAntiAlias);
		
		/*
		 * Gamma Level
		 */
		toolBar2.addSeparator();
		JLabel gammaText = new JLabel("Gamma Level:");
		this.comboGamma = new JComboBox(gamma);
		d = new Dimension(comboGamma.getPreferredSize());
		d.setSize(60, d.getHeight());
		this.comboGamma.setMaximumSize(d);
		toolBar2.add(gammaText);
		toolBar2.add(comboGamma);

		/*
		 * Color space combo box
		 */
		toolBar2.addSeparator();				
		JLabel colorText = new JLabel("Color:");
		this.comboColor = new JComboBox(colors);
		d = new Dimension(this.comboColor.getPreferredSize());
		d.setSize(130, d.getHeight());
		this.comboColor.setMaximumSize(d);
		toolBar2.add(colorText);
		toolBar2.add(this.comboColor);

		// Set alignment
		toolBar1.setAlignmentX(0);
		toolBar2.setAlignmentX(0);
		
		// Add toolbars to panel
		panel.add(toolBar1);
		panel.add(toolBar2);

		// Add panel to frame
		this.mainFrame.getContentPane().add(panel, BorderLayout.NORTH);
		
	}

	/**
	 * Build Popup Menu
	 */
	private void buildPopupMenu() {
		popupMenu = new JPopupMenu();
	    itemCopyText = new JMenuItem("Copy As Text");
	    itemCopyImage = new JMenuItem("Copy As Image");
	    itemCopyCancel = new JMenuItem("Cancel");
		popupMenu.add(itemCopyText);
		popupMenu.add(itemCopyImage);
		popupMenu.add(new JSeparator());
		popupMenu.add(itemCopyCancel);
	}

	// ----------------------------------------------------
	// Frame
	// ----------------------------------------------------
	
	
	/**
	 * Change window title
	 * @param title
	 */
	public void setTitle(String title) {
		if (title == null || title.trim().length() == 0) {
			mainFrame.setTitle("JMuPDF " + JmuPdf.getLibVersion());
		} else {
			mainFrame.setTitle("JMuPDF " + JmuPdf.getLibVersion() + " - " + title);
		}
	}

	/**
	 * Close window
	 */
	public void close() {
		Toolkit.getDefaultToolkit().getSystemEventQueue()
		       .postEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * Display window
	 */
	public void show() {
		this.mainFrame.setVisible(true);
	}	
	
	/**
	 * Get JFrame associated with main window
	 * @return
	 */
	public JFrame getMainFrame() {
		return this.mainFrame;
	}
	
	// ----------------------------------------------------
	// Listener(s)
	// ----------------------------------------------------
	
	
	/**
	 * setWindowListener()
	 * @param wl
	 */
	public void setWindowListener(WindowListener wl) {
		this.mainFrame.addWindowListener(wl);
	}
	
	/**
	 * setActionListeners()
	 * @param al
	 */
	public void setActionListeners(ActionListener al) {
		this.itemExit.addActionListener(al);
		this.itemOpen.addActionListener(al);		
		this.itemClose.addActionListener(al);
		this.itemPrint.addActionListener(al);
		this.itemDocInfo.addActionListener(al);
		this.open.addActionListener(al);
		this.print.addActionListener(al);
		this.prvPage.addActionListener(al);
		this.nxtPage.addActionListener(al);
		this.fstPage.addActionListener(al);
		this.lstPage.addActionListener(al);
		this.comboZoom.addActionListener(al);
		this.comboColor.addActionListener(al);	  
		this.comboRotate.addActionListener(al);
		this.comboAntiAlias.addActionListener(al);
		this.comboGamma.addActionListener(al);
	}	

	/**
	 * setChangeListeners()
	 * @param cl
	 */
	public void setChangeListener(ChangeListener cl) {
		this.pageNumber.addChangeListener(cl);
	}
	
	/**
	 * Set Mouse listener(s) for canvas panning
	 * @param el
	 */
	public void setPanningListener(EventListener el) {
		this.scrollPane.getViewport().addMouseMotionListener((MouseMotionListener)el);
		this.scrollPane.getViewport().addMouseListener((MouseListener)el);
		this.popupMenu.addMouseListener((MouseListener)el);
		this.itemCopyText.addActionListener((ActionListener)el);
		this.itemCopyImage.addActionListener((ActionListener)el);
		this.itemCopyCancel.addActionListener((ActionListener)el);
		this.scrollPane.getHorizontalScrollBar().addAdjustmentListener((AdjustmentListener)el);
		this.scrollPane.getVerticalScrollBar().addAdjustmentListener((AdjustmentListener)el);
	}
	
	/**
	 * Remove Mouse listener(s) from canvas
	 * @param el
	 */
	public void removePanningListener(EventListener el) {
		this.scrollPane.getViewport().removeMouseMotionListener((MouseMotionListener)el);
		this.scrollPane.getViewport().removeMouseListener((MouseListener)el);
		this.popupMenu.removeMouseListener((MouseListener)el);
		this.itemCopyText.removeActionListener((ActionListener)el);
		this.itemCopyImage.removeActionListener((ActionListener)el);
		this.itemCopyCancel.removeActionListener((ActionListener)el);
		this.scrollPane.getHorizontalScrollBar().removeAdjustmentListener((AdjustmentListener)el);
		this.scrollPane.getVerticalScrollBar().removeAdjustmentListener((AdjustmentListener)el);
	}
	
	// ----------------------------------------------------
	// Page Canvas
	// ----------------------------------------------------
	
	
	/**
	 * Clear page canvas area
	 */
	public void clearPageCanvas() {
		if (this.scrollPane.getViewport() != null) {
			this.scrollPane.getViewport().removeAll();
			this.scrollPane.setViewport(null);
			if (this.pageView != null) {
				this.pageView.dispose();
				this.pageView = null;
			}
		}
		this.splitPane.setVisible(false);		
		this.itemClose.setEnabled(false);		
		this.itemPrint.setEnabled(false);
		this.itemDocInfo.setEnabled(false);
		this.comboZoom.setEnabled(false);
		this.comboColor.setEnabled(false);
		this.comboRotate.setEnabled(false);
		this.comboAntiAlias.setEnabled(false);
		this.comboGamma.setEnabled(false);
		this.open.setEnabled(true);
		this.print.setEnabled(false);
		this.prvPage.setEnabled(false);
		this.nxtPage.setEnabled(false);
		this.fstPage.setEnabled(false);
		this.lstPage.setEnabled(false);
		this.pageNumber.setEnabled(false);		
		this.pageNumber.setValue(Integer.valueOf("0"));
		this.pageCount.setText("0");
		this.comboColor.setSelectedIndex(0);
		this.comboZoom.setSelectedIndex(defaultZoomIndex);
		this.comboRotate.setSelectedIndex(0);
		this.comboAntiAlias.setSelectedIndex(8);
		this.comboGamma.setSelectedIndex(2);
		this.setTitle("");
		this.mainFrame.validate();
	}

	/**
	 * Set page canvas area
	 * @param page
	 */
	public void setPageCanvas(Page page, float zoom, int rotate, ImageType color, int antiAliasLevel, float gamma) {
		if (this.pageView == null) {
			this.pageView = new PageView();
			this.scrollPane.setViewportView(pageView);
		}
		this.pageView.setPage(page, zoom, rotate, color, antiAliasLevel, gamma);
		this.splitPane.setVisible(true);		
		this.itemClose.setEnabled(true);
		this.itemPrint.setEnabled(true);
		this.itemDocInfo.setEnabled(true);
		this.comboZoom.setEnabled(true);
		this.comboColor.setEnabled(true);
		this.comboRotate.setEnabled(true);
		this.comboAntiAlias.setEnabled(true);
		this.comboGamma.setEnabled(true);
		this.open.setEnabled(true);
		this.print.setEnabled(true);
		this.prvPage.setEnabled(true);
		this.nxtPage.setEnabled(true);
		this.fstPage.setEnabled(true);
		this.lstPage.setEnabled(true);
		this.pageNumber.setEnabled(true);
		this.pageNumber.setValue(Integer.valueOf(page.getPageNumber()));
		this.pageCount.setText(""+page.getDocument().getPageCount());
		this.comboZoom.setSelectedIndex(defaultZoomIndex);
		this.comboRotate.setSelectedIndex(0);
		this.comboAntiAlias.setSelectedIndex(8);
		this.comboGamma.setSelectedIndex(2);
		this.setTitle(page.getDocument().getDocumentName());
		this.mainFrame.validate();
	}
	
	/**
	 * Get page canvas
	 * @return
	 */
	public PageView getPageCanvas() {
		return pageView;
	}

	
	// ----------------------------------------------------
	// Menu Items
	// ----------------------------------------------------
	
	
	/**
	 * Exit Application
	 * @return
	 */
	public JMenuItem getItemExit() {
		return itemExit;
	}

	/**
	 * Open a PDF document
	 * @return
	 */
	public JMenuItem getItemOpen() {
		return itemOpen;
	}

	/**
	 * Close PDF document
	 * @return
	 */
	public JMenuItem getItemClose() {
		return itemClose;
	}

	/**
	 * Print PDF document
	 * @return
	 */
	public JMenuItem getItemPrint() {
		return itemPrint;
	}
	
	/**
	 * Document Information 
	 * @return
	 */
	public JMenuItem getItemDocInfo() {
		return itemDocInfo;
	}
	
	/**
	 * Get Zoom Combo Box
	 * @return
	 */
	public JComboBox getComboZoom() {
		return comboZoom;
	}

	/**
	 * Get Color Combo Box
	 * @return
	 */
	public JComboBox getComboColor() {
		return comboColor;
	}
	
	/**
	 * Get Rotate Combo Box
	 * @return
	 */
	public JComboBox getComboRotate() {
		return comboRotate;
	}
	
	/**
	 * Get Anti Alias Level Combo Box
	 * @return
	 */
	public JComboBox getComboAntiAlias() {
		return comboAntiAlias;
	}
	
	/**
	 * Get Gamma Level Combo Box
	 * @return
	 */
	public JComboBox getComboGamma() {
		return comboGamma;
	}
	
	/**
	 * Get Open 
	 * @return
	 */
	public JButton getOpen() {
		return open;
	}
	
	/**
	 * Get Print 
	 * @return
	 */
	public JButton getPrint() {
		return print;
	}
	
	/**
	 * Get Previous Page
	 * @return
	 */
	public JButton getPrvPage() {
		return prvPage;
	}
	
	/**
	 * Get Next Page
	 * @return
	 */
	public JButton getNxtPage() {
		return nxtPage;
	}

	/**
	 * Get First Page
	 * @return
	 */
	public JButton getFstPage() {
		return fstPage;
	}
	
	/**
	 * Get Last Page
	 * @return
	 */
	public JButton getLstPage() {
		return lstPage;
	}
	
	/**
	 * Get page number text field
	 * @return
	 */
	public JSpinner getPageNumber() {
		return pageNumber;
	}

	/**
	 * Get pop up menu
	 * @return
	 */
	public JPopupMenu getPopoupMenu() {
		return popupMenu;
	}

	/**
	 * Copy as text
	 * @return
	 */
	public JMenuItem getItemCopyText() {
		return itemCopyText;
	}
	
	/**
	 * Copy as image 
	 * @return
	 */
	public JMenuItem getItemCopyImage() {
		return itemCopyImage;
	}
	
	/**
	 * Copy cancel 
	 * @return
	 */
	public JMenuItem getItemCopyCancel() {
		return itemCopyCancel;
	}
	
}
