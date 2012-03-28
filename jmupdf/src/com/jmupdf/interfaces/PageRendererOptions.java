/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.interfaces;

import com.jmupdf.enums.ImageFormat;
import com.jmupdf.enums.ImageType;
import com.jmupdf.enums.TifCompression;
import com.jmupdf.enums.TifMode;
import com.jmupdf.page.PageRect;

/**
 * Page rendering options Interface </br></br>
 * 
 * The PageRendererOption object is implemented as a singleton within the </br> 
 * Page object via the Page.getRenderingOptions() factory method. </br></br>
 * So basically each Page object has its own PageRendererOption object. </br></br> 
 * It is necessary that the object be accessed in a synchronized manner preferably </br>
 * within the same thread that the page is being instantiated and rendered.
 * 
 * @author Francois Barre
 * @author Pedro J Rivera
 * 
 */
public interface PageRendererOptions {
	
    /**
     * Get image format
     * @return
     */
    public ImageFormat getImageFormat();

    /**
     * Set image format
     * @param imageFormat
     */
    public void setImageFormat(ImageFormat imageFormat);

    /**
     * Get image type
     * @return
     */
    public ImageType getImageType();

    /**
     * Set image type
     * @param imageType
     */
    public void setImageType(ImageType imageType);

    /**
     * Get rotate value
     * @return
     */
    public int getRotate();

    /**
     * Set rotate value
     * @param rotate
     */
    public void setRotate(int rotate);

    /**
     * Get zoom factor
     * @return
     */
    public float getZoom();

    /**
     * Set zoom factor
     * @param zoom
     */
    public void setZoom(float zoom);

	/**
	 * Get resolution
	 * @return
	 */
    public float getResolution();
    
    /**
     * Get gamma correction
     * @return
     */
    public float getGamma();

    /**
     * Set gamma correction </br>
	 * Gamma correct the output image. </br>
	 * Some typical values are 0.7 or 1.4 to thin or darken text rendering. </br>
	 * Default value is 1f. 
     * @param gamma
     */
    public void setGamma(float gamma);

    /**
     * Get quality level
     * @return
     */
    public int getQuality();

    /**
     * Set quality level </br></br>
     *  
     * <strong>When ImageType == FORMAT_TIF <br/></strong>
     * 
     * <blockquote>
     * <strong>When compression == TIF_COMPRESSION_ZLIB <br/></strong> 
     * Control  the  compression  technique used by the Deflate codec.  <br/> 
     * Quality levels are in the range 1-9 with larger numbers yielding <br/> 
     * better compression at the cost of more computation. The default  <br/> 
     * quality level is 6 which yields a good time-space tradeoff.      <br/><br/>
     *          
     * <strong>When compression == TIF_COMPRESSION_JPEG <br/></strong>
     * Control the compression quality level used in the baseline algo- <br/>
     * rithm. Note that quality levels are in the range 0-100 with a    <br/>
     * default value of 75. <br/><br/>
     * </blockquote>
     * 
     * <strong>When ImageType == FORMAT_JPG <br/></strong>
     * <blockquote>
     * Control the compression quality level used in the baseline algo- <br/>
     * rithm. Note that quality levels are in the range 0-100 with a    <br/>
     * default value of 75. <br/><br/>
     * </blockquote>
     * 
     * @param quality
     */
    public void setQuality(int quality);

    /**
     * Get TIFF compression level
     * @return
     */
    public TifCompression getCompression();

    /**
     * Set TIFF compression level 
     * @param compression
     */
    public void setCompression(TifCompression compression);

    /**
     * Get TIFF mode
     * @return
     */
    public TifMode getMode();

    /**
     * Set TIFF mode
     * @param mode
     */
    public void setMode(TifMode mode);

    /**
     * Get bounding box
     * @return
     */
    public PageRect getBoundBox();

    /**
     * Set bounding box.
     * Set the rectangular area to render. 
     * @param bbox
     */
    public void setBoundBox(PageRect bbox);

    /**
     * Get anti alias level
     * @return
     */
    public int getAntiAlias();

    /**
     * Set anti alias level </br>
     * This value is used to determine what bit level is used when </br> 
	 * applying anti-aliasing while rendering page images.</br>
	 * A value of zero turns off anti-aliasing. Maximum value is 8. </br>
     * @param antiAlias
     */
    public void setAntiAlias(int antiAlias);

    /**
     * Validate rendering options
     * @return
     */
    public boolean isValid();
}
