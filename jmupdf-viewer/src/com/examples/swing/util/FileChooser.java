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
package com.examples.swing.util;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import java.awt.Component;
import java.io.File;

/**
 * File Chooser Utility
 * 
 * @author Pedro J Rivera
 *
 */
public class FileChooser {
    public final static String[] pdfExtension = {"pdf", "xps", "cbz"};
    public final static String[] pdfDescription = {"Adobe PDF Files (*.pdf)", "XPS Document (*.xps)", "Comic Book (*.cbz)"};

    /**
     * Open a file chooser dialog box for PDF documents
     * @param parent
     * @return
     */
    public static File getPdfDocument(Component parent) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);		
		fc.addChoosableFileFilter(new XPSFileFilter(pdfDescription[2], pdfExtension[2]));
		fc.addChoosableFileFilter(new XPSFileFilter(pdfDescription[1], pdfExtension[1]));
		fc.addChoosableFileFilter(new PDFFileFilter(pdfDescription[0], pdfExtension[0]));
		fc.setDialogTitle("Open a Document");
		fc.setFileHidingEnabled(true);
		int res = fc.showOpenDialog(parent);
		if (res == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}
		return null;	
    }
    
    public static String getExtension(File f) {
        return getExtension(f.getName());
    }

    public static String getExtension(String s) {
        String ext = null;
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * PDF file filter
     * 
     * @author Pedro J Rivera
     */
    private static class PDFFileFilter extends FileFilter {        
        private String extension;
        private String description;

        PDFFileFilter(String description, String extension) {
            this.description = description;
            this.extension = extension;
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String ext = FileChooser.getExtension(f);
            
            if (ext != null && ext.equals(extension)) {
                return true;
            }
            
            return false;
        }

        public String getDescription() {
            return description;
        }
    }
    
    /**
     * XPS file filter
     * 
     * @author Pedro J Rivera
     */
    private static class XPSFileFilter extends FileFilter {        
        private String extension;
        private String description;

        XPSFileFilter(String description, String extension) {
            this.description = description;
            this.extension = extension;
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String ext = FileChooser.getExtension(f);
            
            if (ext != null && ext.equals(extension)) {
                return true;
            }
            
            return false;
        }

        public String getDescription() {
            return description;
        }
    }
}
