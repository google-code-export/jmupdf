/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.pdf;

import com.jmupdf.enums.DictionaryType;

/**
 * Document Information
 * 
 * @author Pedro Rivera
 *
 */
class PdfInformation {
	private String title;
	private String author;
	private String producer;
	private String creator;
	private String subject;
	private String keywords;
	private String createdDate;
	private String modifiedDate;
	private String version;
	private String trapped;
	
	/**
	 * Create new PDFDocument object
	 * @param pdfDoc
	 */
	public PdfInformation(PdfDocument pdfDoc) {
		title = pdfDoc.getInfo(DictionaryType.INFO_TITLE);
		author = pdfDoc.getInfo(DictionaryType.INFO_AUTHOR);
		producer = pdfDoc.getInfo(DictionaryType.INFO_PRODUCER);
		creator = pdfDoc.getInfo(DictionaryType.INFO_CREATOR);
		subject = pdfDoc.getInfo(DictionaryType.INFO_SUBJECT);
		keywords = pdfDoc.getInfo(DictionaryType.INFO_KEYWORDS);
		createdDate = convertDate(pdfDoc.getInfo(DictionaryType.INFO_CREATION_DATE));
		modifiedDate = convertDate(pdfDoc.getInfo(DictionaryType.INFO_MODIFIED_DATE));
		trapped = pdfDoc.getInfo(DictionaryType.INFO_TRAPPED);
		
		int v = pdfDoc.getVersion();
		version = (v/10) + "." +  (v%10);
	}

	/**
	 * Get title
	 * @return
	 */
	public String getTitle() {
		if (title == null) {
			title = "";
		}
		return title;
	}

	/**
	 * Get author
	 * @return
	 */
	public String getAuthor() {
		if (author == null) {
			author = "";
		}
		return author;
	}

	/**
	 * Get producer
	 * @return
	 */
	public String getProducer() {
		if (producer == null) {
			producer = "";
		}
		return producer;
	}

	/**
	 * Get creator
	 * @return
	 */
	public String getCreator() {
		if (creator == null) {
			creator = "";
		}
		return creator;
	}

	/**
	 * Get subject
	 * @return
	 */
	public String getSubject() {
		if (subject == null) {
			subject = "";
		}
		return subject;
	}

	/**
	 * Get keywords
	 * @return
	 */
	public String getKeywords() {
		if (keywords == null) {
			keywords = "";
		}
		return keywords;
	}

	/**
	 * Ger date created
	 * @return
	 */
	public String getCreatedDate() {
		if (createdDate == null) {
			createdDate = "";
		}
		return createdDate;
	}

	/**
	 * Get date modified
	 * @return
	 */
	public String getModifiedDate() {
		if (modifiedDate == null) {
			modifiedDate = "";
		}
		return modifiedDate;
	}

	/**
	 * Get version
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Is document trapped
	 * @return
	 */
	public String isTrapped() {
		if (trapped == null) {
			trapped = "";
		}
		return trapped;
	}
	 
	/**
	 * Convert a PDF date format to a readable date format
	 * 
	 * Example: "D:20091222171933-05'00'"
	 * Format:  "D:YYYYMMDDHHMMSSxxxxxxx"
	 * 
	 * @param pdfDate
	 * @return
	 */
	private static String convertDate(String pdfDate) {
		String value = null;
		
		if (pdfDate != null && pdfDate.trim().length() > 0) {
			String yy = pdfDate.substring(2, 6);
			String mm = pdfDate.substring(6, 8);
			String dd = pdfDate.substring(8, 10);
			
			int hh = Integer.valueOf(pdfDate.substring(10, 12)).intValue();
			String mn = pdfDate.substring(12, 14);
			String ss = pdfDate.substring(14, 16);
			
			String ap = "AM";
			
			if (hh >= 12) {
				ap = "PM";
				if (hh > 12) {
					hh -= 12;
				}
			}
			
			value = mm + "/" + dd + "/" + yy + " " + hh + ":" + mn + ":" + ss + " " + ap; 
		}
		return value;
	}

}
