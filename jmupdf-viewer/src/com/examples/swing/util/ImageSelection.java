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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Image Selection Class
 * 
 * @author Pedro J Rivera
 *
 */
public class ImageSelection implements Transferable {
    private DataFlavor imageFlavor;
    private DataFlavor bmpFlavor;
    private DataFlavor jpgFlavor;
	private BufferedImage image;

    /**
     * Create new image selection object for copying data to clip board
     * @param image
     */
    public ImageSelection(BufferedImage image) {    	
		imageFlavor = DataFlavor.imageFlavor;
		bmpFlavor = new DataFlavor("image/bmp; class=java.io.ByteArrayInputStream", "Image");
		jpgFlavor = new DataFlavor("image/jpeg; class=java.io.ByteArrayInputStream", "Image");
		this.image = image;
    }
    
    /**
     * Returns supported flavors
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{imageFlavor, bmpFlavor, jpgFlavor};
    }

    /**
     * Returns true if flavor is supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
    	if (flavor.equals(imageFlavor) || 
    		flavor.equals(bmpFlavor)   || 
    		flavor.equals(jpgFlavor))  {
    		return true;
    	}
        return false;
    }

    /**
     * Returns image
     */
    public Object getTransferData(DataFlavor flavor) {
    
    	if (flavor.equals(imageFlavor)) {
        	return image;            
        } 
    	
    	else if (flavor.equals(bmpFlavor)) {
    		return getImageAsBitmap();
    	}
        
       	else if (flavor.equals(jpgFlavor)) {
    		return getImageAsJPeg();
    	}
    	
    	return null;
    }

    /**
     * Get an input stream that represents a bitmap image
     * @return
     */
    private ByteArrayInputStream getImageAsBitmap() {
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	try {
			ImageIO.write(image, "bmp", stream);
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	return null;
    }

    /**
     * Get an input stream that represents a jpeg image
     * @return
     */
    private ByteArrayInputStream getImageAsJPeg() {
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	try {
			ImageIO.write(image, "jpeg", stream);
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	return null;
    }
    
    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }
}