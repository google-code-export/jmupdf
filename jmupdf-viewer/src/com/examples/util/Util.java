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

package com.examples.util;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.ImageIcon;

/**
 * Utility class
 * 
 * @author Pedro J Rivera
 *
 */
public class Util {
	
	/**
	 * Returns true if the specified image has transparent pixels
	 * 
	 * Adapted from: http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
	 *  
	 * @param image
	 * @return
	 */
	public static boolean hasAlpha(Image image) {
		
	    /*
	     *  If buffered image, the color model is readily available
	     */
	    if (image instanceof BufferedImage) {
	        BufferedImage bimage = (BufferedImage)image;
	        return bimage.getColorModel().hasAlpha();
	    }

	    /*
	     * Use a pixel grabber to retrieve the image's color model;
	     * grabbing a single pixel is usually sufficient
	     */
	    PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
	    try {
	        pg.grabPixels();
	    } catch (InterruptedException e) {
	    }

	    /*
	     *  Get the image's color model
	     */
	    ColorModel cm = pg.getColorModel();
	    return cm.hasAlpha();
	}

	/**
	 * Returns a buffered image with the contents of an image
	 * 
	 * Adapted from: http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage toBufferedImage(Image image) {
	    if (image instanceof BufferedImage) {
	        return (BufferedImage)image;
	    }

	    /*
	     * This code ensures that all the pixels in the image are loaded
	     */
	    image = new ImageIcon(image).getImage();

	    /*
	     * Determine if the image has transparent pixels; for this method's
	     * implementation, see Determining If an Image Has Transparent Pixels
	     */
	    boolean hasAlpha = hasAlpha(image);

	    /*
	     *  Create a buffered image with a format that's compatible with the screen
	     */
	    BufferedImage bimage = null;
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    try {
	    	
	        /*
	         *  Determine the type of transparency of the new buffered image
	         */
	        int transparency = Transparency.OPAQUE;
	        if (hasAlpha) {
	            transparency = Transparency.BITMASK;
	        }

	        /*
	         *  Create the buffered image
	         */
	        GraphicsDevice gs = ge.getDefaultScreenDevice();
	        GraphicsConfiguration gc = gs.getDefaultConfiguration();
	        bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
	    } catch (HeadlessException e) {
	        // The system does not have a screen
	    }

	    if (bimage == null) {
	        /*
	         * Create a buffered image using the default color model
	         */
	        int type = BufferedImage.TYPE_INT_RGB;
	        if (hasAlpha) {
	            type = BufferedImage.TYPE_INT_ARGB;
	        }
	        bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
	    }

	    /*
	     *  Copy image to buffered image
	     */
	    Graphics g = bimage.createGraphics();

	    /*
	     *  Paint the image onto the buffered image
	     */
	    g.drawImage(image, 0, 0, null);
	    g.dispose();

	    return bimage;
	}

	/**
	 * Create a unique directory within a directory 'root'
	 * 
	 * @param rootDir
	 * @param seed
	 * @return
	 * @throws IOException
	 */
	public static synchronized File createUniqueDirectory(File rootDir, String seed) throws IOException {
		int index = seed.lastIndexOf('.');
		if (index > 0) {
			seed = seed.substring(0, index);
		}
		File result = null;
		int count = 0;
		while (result == null) {
			String name = seed + "." + count + ".tmp";
			File file = new File(rootDir, name);
			if (!file.exists()) {
				file.mkdirs();
				result = file;
			}
			count++;
		}
		return result;
	}

//	/**
//	 * Create a black and white image from a gray scale image
//	 * with optional dithering.
//	 * @param inputImage
//	 * @param dither
//	 * @return
//	 */
//	private BufferedImage grayToBlackWhite(BufferedImage inputImage, boolean dither) {
//		
//		int w = inputImage.getWidth();
//		int h = inputImage.getHeight();
//		BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
//
//		// Work on a copy of input image because it is modified by diffusion
//		WritableRaster input = inputImage.copyData(null);
//		WritableRaster output = outputImage.getRaster();
//
//		final int threshold = 128;
//		float value, qerror;
//
//		for (int y = 0; y < h; ++y) {
//			for (int x = 0; x < w; ++x) {
//				value = input.getSample(x, y, 0);
//
//				// Threshold value and compute quantization error
//				if (value < threshold) {
//					output.setSample(x, y, 0, 0);
//					qerror = value;
//				} else {
//					output.setSample(x, y, 0, 1);
//					qerror = value - 255;
//				}
//
//				// Spread error amongst neighboring pixels
//				// Based on Floyd-Steinberg Dithering
//				// http://en.wikipedia.org/wiki/Floyd-Steinberg_dithering
//				if (dither) {
//					if((x > 0) && (y > 0) && (x < (w-1)) && (y < (h-1))) {
//						// 7/16
//						value = input.getSample(x+1, y, 0);
//						input.setSample(x+1, y, 0, clamp(value + 0.4375f * qerror));
//						// 3/16
//						value = input.getSample(x-1, y+1, 0);
//						input.setSample(x-1, y+1, 0, clamp(value + 0.1875f * qerror));
//						// 5/16
//						value = input.getSample(x, y+1, 0);
//						input.setSample(x, y+1, 0, clamp(value + 0.3125f * qerror));
//						// 1/16
//						value = input.getSample(x+1, y+1, 0);
//						input.setSample(x+1, y+1, 0, clamp(value + 0.0625f * qerror));
//					}
//				}
//			}
//		}
//		return outputImage;
//	}
	
//	/**
//	 * Forces a value to a 0-255 integer range
//	 * @param value
//	 * @return
//	 */
//	private int clamp(float value) {
//		return Math.min(Math.max(Math.round(value), 0), 255);
//	}
	
	
	//
	// File Utilities
	//
	
	  public final static byte[] load(String fileName)
	  {
	    try { 
	      FileInputStream fin=new FileInputStream(fileName);
	      return load(fin);
	    }
	    catch (Exception e) {
	 
	      return new byte[0];
	    }
	  }

	  public final static byte[] load(File file)
	  {
	    try { 
	      FileInputStream fin=new FileInputStream(file);
	      return load(fin);
	    }
	    catch (Exception e) {
	     
	      return new byte[0];
	    }
	  }

	  public final static byte[] load(FileInputStream fin)
	  {
	    byte readBuf[] = new byte[512*1024];
	  
	    try { 
	      ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    
	      int readCnt = fin.read(readBuf);
	      while (0 < readCnt) {
	        bout.write(readBuf, 0, readCnt);
	        readCnt = fin.read(readBuf);
	      }
	      
	      fin.close();
	      
	      return bout.toByteArray();
	    }
	    catch (Exception e) {
	     
	      return new byte[0];
	    }
	  }	


		
}
