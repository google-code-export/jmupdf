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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.jmupdf.pdf.PdfDocument;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Show document information dialog
 * 
 * @author Pedro J Rivera
 *
 */
public class DocInfoView extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JDialog view;
	private PdfDocument pdfDoc;
	
	private JTabbedPane tabbedPane;
	private JPanel panelMain;
	private JPanel panelProperties;
	private JPanel panelSecurity;
	private JLabel lblTitle;
	private JLabel lblAuthor;
	private JLabel lblProducer;
	private JLabel lblCreator;
	private JLabel lblKeywords;
	private JLabel lblSubject;
	private JLabel lblCreationDate;
	private JLabel lblModifiedDate;
	private JLabel lblFile;
	private JLabel lblLocation;	
	private JLabel lblVersion;
	private JLabel textModifiedDate;	
	private JLabel textCreateDate;
	private JLabel textVersion;
	private JLabel textFile;
	private JTextField textLocation;
	private JTextField textTitle;
	private JTextField textAuthor;
	private JTextField textProducer;
	private JTextField textCreator;
	private JTextField textKeywords;
	private JTextField textSubject;	
	
	private JLabel lblSecurityMethod;
	private JLabel lblPrinting;
	private JLabel lblDocumentAssembly;
	private JLabel lblContentCopying;
	private JLabel lblContentAccessibility;
	private JLabel lblFormFieldFillin;
	private JLabel lblAuthoringComments;
	private JLabel lblEncryptionLevel;
	private JLabel lblChangingTheDocument;
	private JLabel textSecurity;
	private JLabel textEncryptionLevel;
	private JLabel textPrinting;
	private JLabel textAssembly;
	private JLabel textCopying;
	private JLabel textAccessibility;
	private JLabel textFillForm;
	private JLabel textComments;		
	private JLabel textChange;
	
	private JButton btnCancel;
	private JButton btnOk;	
	
	public DocInfoView(PdfDocument pdfDoc) {
		this.pdfDoc = pdfDoc;
		this.view = this;
		createGUI();
	}
	
	private void createGUI() {
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setSize(500, 400);
		setLocationRelativeTo(null);		
		setTitle("Document Information");		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{322, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		this.panelMain = new JPanel();
		GridBagConstraints gbc_panelMain = new GridBagConstraints();
		gbc_panelMain.fill = GridBagConstraints.BOTH;
		gbc_panelMain.gridx = 0;
		gbc_panelMain.gridy = 0;
		gbc_panelMain.insets = new Insets(8, 8, 8, 8);
		getContentPane().add(this.panelMain, gbc_panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panelMain.rowHeights = new int[]{0, 0, 0};
		gbl_panelMain.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelMain.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		this.panelMain.setLayout(gbl_panelMain);
		
		this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.gridwidth = 3;
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		this.panelMain.add(this.tabbedPane, gbc_tabbedPane);
		
		//
		// First Tab - Properties
		//
		
		this.panelProperties = new JPanel();
		this.tabbedPane.addTab("Properties", null, this.panelProperties, null);
		GridBagLayout gbl_panelProperties = new GridBagLayout();
		gbl_panelProperties.columnWidths = new int[]{15, 95, 0, 15, 0};
		gbl_panelProperties.rowHeights = new int[]{15, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 0};
		gbl_panelProperties.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panelProperties.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		this.panelProperties.setLayout(gbl_panelProperties);
		
		this.lblFile = new JLabel("File:");
		this.lblFile.setFont(this.lblFile.getFont().deriveFont(this.lblFile.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblFile = new GridBagConstraints();
		gbc_lblFile.anchor = GridBagConstraints.EAST;
		gbc_lblFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblFile.gridx = 1;
		gbc_lblFile.gridy = 1;
		this.panelProperties.add(this.lblFile, gbc_lblFile);
				
		this.textFile = new JLabel(pdfDoc.getFileName());
		GridBagConstraints gbc_textFile = new GridBagConstraints();
		gbc_textFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFile.insets = new Insets(0, 0, 5, 5);
		gbc_textFile.gridx = 2;
		gbc_textFile.gridy = 1;
		this.panelProperties.add(this.textFile, gbc_textFile);
		
		this.lblLocation = new JLabel("Location:");
		this.lblLocation.setFont(this.lblLocation.getFont().deriveFont(this.lblLocation.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblLocation = new GridBagConstraints();
		gbc_lblLocation.anchor = GridBagConstraints.EAST;
		gbc_lblLocation.insets = new Insets(0, 0, 5, 5);
		gbc_lblLocation.gridx = 1;
		gbc_lblLocation.gridy = 2;
		this.panelProperties.add(this.lblLocation, gbc_lblLocation);
		
		this.textLocation = new JTextField(pdfDoc.getPath());
		this.textLocation.setEditable(false);
		this.textLocation.setCaretPosition(0);
		GridBagConstraints gbc_textLocation = new GridBagConstraints();
		gbc_textLocation.fill = GridBagConstraints.HORIZONTAL;
		gbc_textLocation.insets = new Insets(0, 0, 5, 5);
		gbc_textLocation.gridx = 2;
		gbc_textLocation.gridy = 2;
		this.panelProperties.add(this.textLocation, gbc_textLocation);
		
		this.lblTitle = new JLabel("Title:");
		this.lblTitle.setFont(this.lblTitle.getFont().deriveFont(this.lblTitle.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.anchor = GridBagConstraints.EAST;
		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle.gridx = 1;
		gbc_lblTitle.gridy = 3;
		this.panelProperties.add(this.lblTitle, gbc_lblTitle);
		
		this.textTitle = new JTextField(pdfDoc.getTitle());
		this.textTitle.setEditable(false);
		this.textTitle.setCaretPosition(0);
		GridBagConstraints gbc_textTitle = new GridBagConstraints();
		gbc_textTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_textTitle.insets = new Insets(0, 0, 5, 5);
		gbc_textTitle.gridx = 2;
		gbc_textTitle.gridy = 3;
		this.panelProperties.add(this.textTitle, gbc_textTitle);
		
		this.lblAuthor = new JLabel("Author:");
		this.lblAuthor.setFont(this.lblAuthor.getFont().deriveFont(this.lblAuthor.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblAuthor = new GridBagConstraints();
		gbc_lblAuthor.anchor = GridBagConstraints.EAST;
		gbc_lblAuthor.insets = new Insets(0, 0, 5, 5);
		gbc_lblAuthor.gridx = 1;
		gbc_lblAuthor.gridy = 4;
		this.panelProperties.add(this.lblAuthor, gbc_lblAuthor);
		
		this.textAuthor = new JTextField(pdfDoc.getAuthor());
		this.textAuthor.setEditable(false);
		this.textAuthor.setCaretPosition(0);
		GridBagConstraints gbc_textAuthor = new GridBagConstraints();
		gbc_textAuthor.fill = GridBagConstraints.HORIZONTAL;
		gbc_textAuthor.insets = new Insets(0, 0, 5, 5);
		gbc_textAuthor.gridx = 2;
		gbc_textAuthor.gridy = 4;
		this.panelProperties.add(this.textAuthor, gbc_textAuthor);
		
		this.lblProducer = new JLabel("Producer:");
		this.lblProducer.setFont(this.lblProducer.getFont().deriveFont(this.lblProducer.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblProducer = new GridBagConstraints();
		gbc_lblProducer.anchor = GridBagConstraints.EAST;
		gbc_lblProducer.insets = new Insets(0, 0, 5, 5);
		gbc_lblProducer.gridx = 1;
		gbc_lblProducer.gridy = 5;
		this.panelProperties.add(this.lblProducer, gbc_lblProducer);
		
		this.textProducer = new JTextField(pdfDoc.getProducer());
		this.textProducer.setEditable(false);
		this.textProducer.setCaretPosition(0);
		GridBagConstraints gbc_textProducer = new GridBagConstraints();
		gbc_textProducer.fill = GridBagConstraints.HORIZONTAL;
		gbc_textProducer.insets = new Insets(0, 0, 5, 5);
		gbc_textProducer.gridx = 2;
		gbc_textProducer.gridy = 5;
		this.panelProperties.add(this.textProducer, gbc_textProducer);
		
		this.lblCreator = new JLabel("Application:");
		this.lblCreator.setFont(this.lblCreator.getFont().deriveFont(this.lblCreator.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblCreator = new GridBagConstraints();
		gbc_lblCreator.anchor = GridBagConstraints.EAST;
		gbc_lblCreator.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreator.gridx = 1;
		gbc_lblCreator.gridy = 6;
		this.panelProperties.add(this.lblCreator, gbc_lblCreator);
		
		this.textCreator = new JTextField(pdfDoc.getCreator());
		this.textCreator.setEditable(false);
		this.textCreator.setCaretPosition(0);
		GridBagConstraints gbc_textCreator = new GridBagConstraints();
		gbc_textCreator.fill = GridBagConstraints.HORIZONTAL;
		gbc_textCreator.insets = new Insets(0, 0, 5, 5);
		gbc_textCreator.gridx = 2;
		gbc_textCreator.gridy = 6;
		this.panelProperties.add(this.textCreator, gbc_textCreator);
		
		this.lblKeywords = new JLabel("Keywords:");
		this.lblKeywords.setFont(this.lblKeywords.getFont().deriveFont(this.lblKeywords.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblKeywords = new GridBagConstraints();
		gbc_lblKeywords.anchor = GridBagConstraints.EAST;
		gbc_lblKeywords.insets = new Insets(0, 0, 5, 5);
		gbc_lblKeywords.gridx = 1;
		gbc_lblKeywords.gridy = 7;
		this.panelProperties.add(this.lblKeywords, gbc_lblKeywords);
		
		this.textKeywords = new JTextField(pdfDoc.getKeywords());
		this.textKeywords.setEditable(false);
		this.textKeywords.setCaretPosition(0);
		GridBagConstraints gbc_textKeywords = new GridBagConstraints();
		gbc_textKeywords.fill = GridBagConstraints.HORIZONTAL;
		gbc_textKeywords.insets = new Insets(0, 0, 5, 5);
		gbc_textKeywords.gridx = 2;
		gbc_textKeywords.gridy = 7;
		this.panelProperties.add(this.textKeywords, gbc_textKeywords);
		
		this.lblSubject = new JLabel("Subject:");
		this.lblSubject.setFont(this.lblSubject.getFont().deriveFont(this.lblSubject.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblSubject = new GridBagConstraints();
		gbc_lblSubject.anchor = GridBagConstraints.EAST;
		gbc_lblSubject.insets = new Insets(0, 0, 5, 5);
		gbc_lblSubject.gridx = 1;
		gbc_lblSubject.gridy = 8;
		this.panelProperties.add(this.lblSubject, gbc_lblSubject);
		
		this.textSubject = new JTextField(pdfDoc.getSubject());
		this.textSubject.setEditable(false);
		this.textSubject.setCaretPosition(0);
		GridBagConstraints gbc_textSubject = new GridBagConstraints();
		gbc_textSubject.fill = GridBagConstraints.HORIZONTAL;
		gbc_textSubject.insets = new Insets(0, 0, 5, 5);
		gbc_textSubject.gridx = 2;
		gbc_textSubject.gridy = 8;
		this.panelProperties.add(this.textSubject, gbc_textSubject);
		
		this.lblCreationDate = new JLabel("Creation Date:");
		this.lblCreationDate.setFont(this.lblCreationDate.getFont().deriveFont(this.lblCreationDate.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblCreationDate = new GridBagConstraints();
		gbc_lblCreationDate.anchor = GridBagConstraints.EAST;
		gbc_lblCreationDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreationDate.gridx = 1;
		gbc_lblCreationDate.gridy = 9;
		this.panelProperties.add(this.lblCreationDate, gbc_lblCreationDate);
		
		this.textCreateDate = new JLabel(pdfDoc.getCreatedDate());
		GridBagConstraints gbc_textCreateDate = new GridBagConstraints();
		gbc_textCreateDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_textCreateDate.insets = new Insets(0, 0, 5, 5);
		gbc_textCreateDate.gridx = 2;
		gbc_textCreateDate.gridy = 9;
		this.panelProperties.add(this.textCreateDate, gbc_textCreateDate);
		
		this.lblModifiedDate = new JLabel("Modified Date:");
		this.lblModifiedDate.setFont(this.lblModifiedDate.getFont().deriveFont(this.lblModifiedDate.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblModifiedDate = new GridBagConstraints();
		gbc_lblModifiedDate.anchor = GridBagConstraints.EAST;
		gbc_lblModifiedDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblModifiedDate.gridx = 1;
		gbc_lblModifiedDate.gridy = 10;
		this.panelProperties.add(this.lblModifiedDate, gbc_lblModifiedDate);
		
		this.textModifiedDate = new JLabel(pdfDoc.getModifiedDate());
		GridBagConstraints gbc_textModifiedDate = new GridBagConstraints();
		gbc_textModifiedDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_textModifiedDate.insets = new Insets(0, 0, 5, 5);
		gbc_textModifiedDate.gridx = 2;
		gbc_textModifiedDate.gridy = 10;
		this.panelProperties.add(this.textModifiedDate, gbc_textModifiedDate);

		this.lblVersion = new JLabel(" Version:");
		this.lblVersion.setFont(this.lblVersion.getFont().deriveFont(this.lblVersion.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblVersion = new GridBagConstraints();
		gbc_lblVersion.anchor = GridBagConstraints.EAST;
		gbc_lblVersion.insets = new Insets(0, 0, 0, 5);
		gbc_lblVersion.gridx = 1;
		gbc_lblVersion.gridy = 11;
		this.panelProperties.add(this.lblVersion, gbc_lblVersion);
		
		this.textVersion = new JLabel(pdfDoc.getPdfVersion());
		GridBagConstraints gbc_textVersion = new GridBagConstraints();
		gbc_textVersion.fill = GridBagConstraints.HORIZONTAL;
		gbc_textVersion.insets = new Insets(0, 0, 0, 5);
		gbc_textVersion.gridx = 2;
		gbc_textVersion.gridy = 11;
		this.panelProperties.add(this.textVersion, gbc_textVersion);
		
		//
		// Second Tab - Security
		//
		
		this.panelSecurity = new JPanel();
		this.tabbedPane.addTab("Security", null, this.panelSecurity, null);
		GridBagLayout gbl_panelSecurity = new GridBagLayout();
		gbl_panelSecurity.columnWidths = new int[]{15, 0, 0, 0};
		gbl_panelSecurity.rowHeights = new int[]{15, 25, 25, 25, 0, 25, 25, 25, 25, 25, 0};
		gbl_panelSecurity.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panelSecurity.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		this.panelSecurity.setLayout(gbl_panelSecurity);
		
		this.lblSecurityMethod = new JLabel("Encryption:");
		this.lblSecurityMethod.setFont(this.lblSecurityMethod.getFont().deriveFont(this.lblSecurityMethod.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblSecurityMethod = new GridBagConstraints();
		gbc_lblSecurityMethod.anchor = GridBagConstraints.EAST;
		gbc_lblSecurityMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblSecurityMethod.gridx = 1;
		gbc_lblSecurityMethod.gridy = 1;
		this.panelSecurity.add(this.lblSecurityMethod, gbc_lblSecurityMethod);
		
		if(pdfDoc.isEncrypted()) {
			this.textSecurity = new JLabel("Encrypted");
		} else {
			this.textSecurity = new JLabel("None");
		}
		GridBagConstraints gbc_textSecurity = new GridBagConstraints();
		gbc_textSecurity.anchor = GridBagConstraints.WEST;
		gbc_textSecurity.insets = new Insets(0, 0, 5, 0);
		gbc_textSecurity.gridx = 2;
		gbc_textSecurity.gridy = 1;
		this.panelSecurity.add(this.textSecurity, gbc_textSecurity);
		
		this.lblEncryptionLevel = new JLabel("Encryption Level:");
		this.lblEncryptionLevel.setFont(this.lblEncryptionLevel.getFont().deriveFont(this.lblEncryptionLevel.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblEncryptionLevel = new GridBagConstraints();
		gbc_lblEncryptionLevel.anchor = GridBagConstraints.EAST;
		gbc_lblEncryptionLevel.insets = new Insets(0, 0, 5, 5);
		gbc_lblEncryptionLevel.gridx = 1;
		gbc_lblEncryptionLevel.gridy = 2;
		this.panelSecurity.add(this.lblEncryptionLevel, gbc_lblEncryptionLevel);
		
		if (pdfDoc.getLength() > 0) {
			this.textEncryptionLevel = new JLabel(pdfDoc.getLength() + "-bit " + pdfDoc.getMethod());
		} else {
			this.textEncryptionLevel = new JLabel("None");
		}		
		GridBagConstraints gbc_textEncryptionLevel = new GridBagConstraints();
		gbc_textEncryptionLevel.anchor = GridBagConstraints.WEST;
		gbc_textEncryptionLevel.insets = new Insets(0, 0, 5, 0);
		gbc_textEncryptionLevel.gridx = 2;
		gbc_textEncryptionLevel.gridy = 2;
		this.panelSecurity.add(this.textEncryptionLevel, gbc_textEncryptionLevel);
		
		this.lblPrinting = new JLabel("Printing:");
		this.lblPrinting.setFont(this.lblPrinting.getFont().deriveFont(this.lblPrinting.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblPrinting = new GridBagConstraints();
		gbc_lblPrinting.anchor = GridBagConstraints.EAST;
		gbc_lblPrinting.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrinting.gridx = 1;
		gbc_lblPrinting.gridy = 3;
		this.panelSecurity.add(this.lblPrinting, gbc_lblPrinting);
		
		if (pdfDoc.getCanPrint()){
			if(pdfDoc.getCanPrintQuality()) {
				this.textPrinting = new JLabel("Fully Allowed");
			} else {
				this.textPrinting = new JLabel("Low Resolution");
			}
		} else {
			this.textPrinting = new JLabel("Not Allowed");	
		}		
		GridBagConstraints gbc_textPrinting = new GridBagConstraints();
		gbc_textPrinting.anchor = GridBagConstraints.WEST;
		gbc_textPrinting.insets = new Insets(0, 0, 5, 0);
		gbc_textPrinting.gridx = 2;
		gbc_textPrinting.gridy = 3;
		this.panelSecurity.add(this.textPrinting, gbc_textPrinting);
		
		this.lblChangingTheDocument = new JLabel("Changing the Document:");
		this.lblChangingTheDocument.setFont(this.lblChangingTheDocument.getFont().deriveFont(this.lblChangingTheDocument.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblChangingTheDocument = new GridBagConstraints();
		gbc_lblChangingTheDocument.anchor = GridBagConstraints.EAST;
		gbc_lblChangingTheDocument.insets = new Insets(0, 0, 5, 5);
		gbc_lblChangingTheDocument.gridx = 1;
		gbc_lblChangingTheDocument.gridy = 4;
		this.panelSecurity.add(this.lblChangingTheDocument, gbc_lblChangingTheDocument);
		
		this.textChange = new JLabel(pdfDoc.getCanModify()?"Allow":"Not Allowed");
		GridBagConstraints gbc_textChange = new GridBagConstraints();
		gbc_textChange.anchor = GridBagConstraints.WEST;
		gbc_textChange.insets = new Insets(0, 0, 5, 0);
		gbc_textChange.gridx = 2;
		gbc_textChange.gridy = 4;
		this.panelSecurity.add(this.textChange, gbc_textChange);
		
		this.lblDocumentAssembly = new JLabel("Document Assembly:");
		this.lblDocumentAssembly.setFont(this.lblDocumentAssembly.getFont().deriveFont(this.lblDocumentAssembly.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblDocumentAssembly = new GridBagConstraints();
		gbc_lblDocumentAssembly.anchor = GridBagConstraints.EAST;
		gbc_lblDocumentAssembly.insets = new Insets(0, 0, 5, 5);
		gbc_lblDocumentAssembly.gridx = 1;
		gbc_lblDocumentAssembly.gridy = 5;
		this.panelSecurity.add(this.lblDocumentAssembly, gbc_lblDocumentAssembly);
		
		this.textAssembly = new JLabel(pdfDoc.getCanAssemble()?"Allow":"Not Allowed");
		GridBagConstraints gbc_textAssembly = new GridBagConstraints();
		gbc_textAssembly.anchor = GridBagConstraints.WEST;
		gbc_textAssembly.insets = new Insets(0, 0, 5, 0);
		gbc_textAssembly.gridx = 2;
		gbc_textAssembly.gridy = 5;
		this.panelSecurity.add(this.textAssembly, gbc_textAssembly);
		
		this.lblContentCopying = new JLabel("Content Copying or Extraction:");
		this.lblContentCopying.setFont(this.lblContentCopying.getFont().deriveFont(this.lblContentCopying.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblContentCopying = new GridBagConstraints();
		gbc_lblContentCopying.anchor = GridBagConstraints.EAST;
		gbc_lblContentCopying.insets = new Insets(0, 0, 5, 5);
		gbc_lblContentCopying.gridx = 1;
		gbc_lblContentCopying.gridy = 6;
		this.panelSecurity.add(this.lblContentCopying, gbc_lblContentCopying);
		
		this.textCopying = new JLabel(pdfDoc.getCanCopy()?"Allow":"Not Allowed");
		GridBagConstraints gbc_textCopying = new GridBagConstraints();
		gbc_textCopying.anchor = GridBagConstraints.WEST;
		gbc_textCopying.insets = new Insets(0, 0, 5, 0);
		gbc_textCopying.gridx = 2;
		gbc_textCopying.gridy = 6;
		this.panelSecurity.add(this.textCopying, gbc_textCopying);
		
		this.lblContentAccessibility = new JLabel("Content Accessibility:");
		this.lblContentAccessibility.setFont(this.lblContentAccessibility.getFont().deriveFont(this.lblContentAccessibility.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblContentAccessibility = new GridBagConstraints();
		gbc_lblContentAccessibility.anchor = GridBagConstraints.EAST;
		gbc_lblContentAccessibility.insets = new Insets(0, 0, 5, 5);
		gbc_lblContentAccessibility.gridx = 1;
		gbc_lblContentAccessibility.gridy = 7;
		this.panelSecurity.add(this.lblContentAccessibility, gbc_lblContentAccessibility);
		
		this.textAccessibility = new JLabel(pdfDoc.getCanAccessibility()?"Allow":"Not Allowed");
		GridBagConstraints gbc_textAccessibility = new GridBagConstraints();
		gbc_textAccessibility.anchor = GridBagConstraints.WEST;
		gbc_textAccessibility.insets = new Insets(0, 0, 5, 0);
		gbc_textAccessibility.gridx = 2;
		gbc_textAccessibility.gridy = 7;
		this.panelSecurity.add(this.textAccessibility, gbc_textAccessibility);
		
		this.lblFormFieldFillin = new JLabel("Form Field Fill-in or Signing:");
		this.lblFormFieldFillin.setFont(this.lblFormFieldFillin.getFont().deriveFont(this.lblFormFieldFillin.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblFormFieldFillin = new GridBagConstraints();
		gbc_lblFormFieldFillin.anchor = GridBagConstraints.EAST;
		gbc_lblFormFieldFillin.insets = new Insets(0, 0, 5, 5);
		gbc_lblFormFieldFillin.gridx = 1;
		gbc_lblFormFieldFillin.gridy = 8;
		this.panelSecurity.add(this.lblFormFieldFillin, gbc_lblFormFieldFillin);
		
		this.textFillForm = new JLabel(pdfDoc.getCanFillForm()?"Allow":"Not Allowed");
		GridBagConstraints gbc_textFillForm = new GridBagConstraints();
		gbc_textFillForm.anchor = GridBagConstraints.WEST;
		gbc_textFillForm.insets = new Insets(0, 0, 5, 0);
		gbc_textFillForm.gridx = 2;
		gbc_textFillForm.gridy = 8;
		this.panelSecurity.add(this.textFillForm, gbc_textFillForm);
		
		this.lblAuthoringComments = new JLabel("Authoring Comments and Form Fields");
		this.lblAuthoringComments.setFont(this.lblAuthoringComments.getFont().deriveFont(this.lblAuthoringComments.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblAuthoringComments = new GridBagConstraints();
		gbc_lblAuthoringComments.insets = new Insets(0, 0, 0, 5);
		gbc_lblAuthoringComments.anchor = GridBagConstraints.EAST;
		gbc_lblAuthoringComments.gridx = 1;
		gbc_lblAuthoringComments.gridy = 9;
		this.panelSecurity.add(this.lblAuthoringComments, gbc_lblAuthoringComments);
		
		this.textComments = new JLabel(pdfDoc.getCanNotes()?"Allow":"Not Allowed");
		GridBagConstraints gbc_textComments = new GridBagConstraints();
		gbc_textComments.anchor = GridBagConstraints.WEST;
		gbc_textComments.gridx = 2;
		gbc_textComments.gridy = 9;
		this.panelSecurity.add(this.textComments, gbc_textComments);
		
		//
		// Buttons
		//
		
		this.btnOk = new JButton("Ok");
		this.btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view.setVisible(false);
				view.dispose();
			}
		});
		GridBagConstraints gbc_btnOk = new GridBagConstraints();
		gbc_btnOk.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnOk.insets = new Insets(0, 0, 0, 5);
		gbc_btnOk.gridx = 1;
		gbc_btnOk.gridy = 1;
		this.panelMain.add(this.btnOk, gbc_btnOk);

		this.btnCancel = new JButton("Cancel");
		this.btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view.setVisible(false);
				view.dispose();
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.SOUTHEAST;		
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 1;
		this.panelMain.add(this.btnCancel, gbc_btnCancel);
		
	}

}
