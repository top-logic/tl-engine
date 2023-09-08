/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;

import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.POIDrawingManager;
import com.top_logic.base.office.excel.POIExcelTemplate.HorizontalAlign;
import com.top_logic.base.office.excel.POIExcelTemplate.POITemplateEntry;
import com.top_logic.base.office.excel.POIExcelTemplate.VerticalAlign;

/**
 * Base class for images to be embedded into an excel file.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractImagePOIType implements PoiTypeHandler {

    /**
     * Export the image defined by the given input stream according to the specified template.
     * 
     * <p>
     * <strong>Note: </strong>the specified input stream is NOT automatically
     * closed upon successful export.
     * </p>
     * 
     * @param   aCell          the cell the image will be placed at
     * @param   anImage        the image to be exported.
     * @param   anImageType    the image type as defined by the {@link Workbook Workbook.PICTURE_TYPE_*} constants.
     * @param   aTemplate      the template to be used for image export.
     * @param   aDrawingMgr    the drawing manager to be used for resolving the proper drawing patriarchs to insert the specified image into.
     * @throws  IOException   if an error occurred while accessing the specified input stream.
     */
	protected void exportImage(Cell aCell, InputStream anImage, int anImageType, POITemplateEntry aTemplate, POIDrawingManager aDrawingMgr) throws IOException {
		this.exportImage(aCell, IOUtils.toByteArray(anImage), anImageType, aTemplate, aDrawingMgr);
	}

    /**
     * Export the image defined by the given byte array according to the specified template.
     * 
     * @param   aCell          the cell the image will be placed at
     * @param   anImage        the image to be exported.
     * @param   anImageType    the image type as defined by the {@link Workbook Workbook.PICTURE_TYPE_*} constants.
     * @param   aTemplate      the template to be used for image export.
     * @param   aDrawingMgr    the drawing manager to be used for resolving the proper drawing patriarchs to insert the specified image into.
     * @throws  IOException   if an error occurred while accessing the specified input stream.
     */
	protected void exportImage(Cell aCell, byte[] anImage, int anImageType, POITemplateEntry aTemplate, POIDrawingManager aDrawingMgr) throws IOException {
		// we will need these guys for convenience :)
        final Sheet    theSheet = aCell.getSheet();
        final Workbook theBook  = theSheet.getWorkbook();        

        // get the configured cell span
        final int theColSpan = this.getTemplateValue(aTemplate, POITemplateEntry.ATTRIBUTE_WIDTH, 1);
        final int theRowSpan = this.getTemplateValue(aTemplate, POITemplateEntry.ATTRIBUTE_HEIGHT, 1);
        
        // the pixel offset within the picture region
        Point theD1 = new Point(this.getTemplateValue(aTemplate, POITemplateEntry.ATTRIBUTE_H_INDENT, 0), this.getTemplateValue(aTemplate, POITemplateEntry.ATTRIBUTE_V_INDENT, 0));
        Point theD2 = new Point(theD1.x, theD1.y);

        // Image alignment is not supported yet due to the horrible units in POI/Excel.
//        this.alignImage(theD1, anImage, aTemplate);

        // retrieve the drawing patriarch first...
        Drawing theDrawing = aDrawingMgr.getDrawing(theSheet);
        
        // extract the contents of the image
		final int    theIdx = theBook.addPicture(anImage, anImageType);

        // we can create a new anchor and set the appropriate
        // location coordinates for it.
        final ClientAnchor theAnchor = theBook.getCreationHelper().createClientAnchor();

        // set top-left corner of the picture,
        theAnchor.setRow1(aCell.getRowIndex());
        theAnchor.setCol1(aCell.getColumnIndex());
        theAnchor.setRow2(aCell.getRowIndex() + theRowSpan);
        theAnchor.setCol2(aCell.getColumnIndex() + theColSpan);

        // adjust the picture pixel-wise
        theAnchor.setDx1(theD1.x);
        theAnchor.setDy1(theD1.y);
        theAnchor.setDx2(theD2.x);
        theAnchor.setDy2(theD2.y);

        Picture thePicture = theDrawing.createPicture(theAnchor, theIdx);
        boolean autofit    = this.getTemplateValue(aTemplate, POITemplateEntry.ATTRIBUTE_AUTOFIT, true);

        if (!autofit) {
            thePicture.resize(1);
        }
    }

	/**
     * Returns the type of the image represented by the specified file name.
     * The computation is based on the file name extension.
     * 
     * @param    anImage    The image to retrieve the type for.
     * @return   The image type as defined by the {@link Workbook Workbook.PICTURE_TYPE_*} constants.
     */
    protected int getImageType(String anImage) {
        int    theIndex  = anImage.lastIndexOf('.') + 1;
        String theSuffix = anImage.substring(theIndex);

        if (theSuffix.equalsIgnoreCase("jpg") || theSuffix.equalsIgnoreCase("jpeg")) {
            return Workbook.PICTURE_TYPE_JPEG;
        }
        else if (theSuffix.equalsIgnoreCase("png")) {
            return Workbook.PICTURE_TYPE_PNG;
        }
        else if (theSuffix.equalsIgnoreCase("emf")) {
            return Workbook.PICTURE_TYPE_EMF;
        }
        else if (theSuffix.equalsIgnoreCase("dib")) {
            return Workbook.PICTURE_TYPE_DIB;
        }
        else if (theSuffix.equalsIgnoreCase("wmf")) {
            return Workbook.PICTURE_TYPE_WMF;
        }
        else {
            throw new IllegalArgumentException("Unsupported format for image '" + anImage + "' (suffix: '" + theSuffix + "')!");
        }
    }

	/** 
	 * Align the given point values for the given image.
	 * 
	 * @param   aPoint       the point (top-left position) to be changed.
	 * @param   anImage      the image to be exported.
	 * @param   aTemplate    the template to be used for image export.
	 */
	protected void alignImage(Point aPoint, byte[] anImage, POITemplateEntry aTemplate) {
		// get the configured alignment
        final VerticalAlign   theVAlign = aTemplate.getEnum(POITemplateEntry.ATTRIBUTE_V_ALIGN, VerticalAlign.class,   VerticalAlign.TOP);
        final HorizontalAlign theHAlign = aTemplate.getEnum(POITemplateEntry.ATTRIBUTE_H_ALIGN, HorizontalAlign.class, HorizontalAlign.LEFT);

        // compute the image dimension.
        // We might want to optimize this one by computing the dimensions
        // for non-top and non-left alignments only...
        final Dimension theAnchorDim = this.computeAnchorDimension(aTemplate);
        final Dimension theImgDim    = this.getImageDimension(anImage);

        // now we have to compute the offsets according to the defined horizontal alignment.
        switch (theHAlign) {
	        case CENTER:
	            aPoint.x += theAnchorDim.width - theImgDim.width;
	            break;
	        case RIGHT:
	        	aPoint.x += (theAnchorDim.width - theImgDim.width) / 2;
	            break;
	        default:
	            break;
        }

        // now we have to compute the offsets according to the defined vertical alignment.
        switch (theVAlign) {
	        case BOTTOM:
	        	aPoint.y += theAnchorDim.height - theImgDim.height;
	            break;
	        case CENTER:
	        	aPoint.y += (theAnchorDim.height - theImgDim.height) / 2;
	            break;
	        default:
	            break;
        }
	}

	/** 
	 * Return the dimension of the image provided by the given input stream.
	 * 
	 * @param   anImage       the image to be exported.
	 * @return  The calculated dimension of the given image.
	 */
	protected Dimension getImageDimension(byte[] anImage) {
		// TODO WTA: Implement me.
		throw new UnsupportedOperationException("WTA: Implement me...");
	}

    /**
     * Compute the range dimension for the specified template entry. The
     * computed values are based on the template configuration of the specified
     * template entry. The range width and height (in columns and rows
     * respectively) are taken in order to compute the necessary pixel values.
     * 
     * @param aTemplate
     *            the template entry to compute the range dimension for
     * @return the range dimension of the specified template entry
     */
    protected Dimension computeAnchorDimension(final POITemplateEntry aTemplate) {
        // retrieve the column and row span from the template entry.
        final int theColSpan = aTemplate.getInteger(POITemplateEntry.ATTRIBUTE_WIDTH,  1);
        final int theRowSpan = aTemplate.getInteger(POITemplateEntry.ATTRIBUTE_HEIGHT, 1);
        
        // this is the reference cell
        final Cell  theCell     = aTemplate.getCell();
        final Sheet theSheet    = theCell.getSheet();
        final int   theRowIndex = theCell.getRowIndex();
        final int   theColIndex = theCell.getColumnIndex();
        
        // the column width is returned as 1/256th of a character's width.
        // We will have to recompute it again...
        int theWidth  = 0;
        
        // the height is in points...
        int theHeight = 0;
        
        // compute both, the width and height in one sweep.
        for(int i = 0; i < theRowSpan; i++) {
            Row theRow = theSheet.getRow(theRowIndex + i);
            if(theRow == null) {
                theRow = theSheet.createRow(theRowIndex + i);
            }
            theHeight += theRow.getHeightInPoints();
            
            for(int j = 0; j < theColSpan; j++) {
                theWidth += theSheet.getColumnWidth(theColIndex + j);
            }
        }
        
        return new Dimension(theWidth / 256, theHeight);
    }

	private int getTemplateValue(POITemplateEntry aTemplate, String aKey, int aDefault) {
		return (aTemplate != null) ? aTemplate.getInteger(aKey, aDefault) : aDefault;
	}

	private boolean getTemplateValue(POITemplateEntry aTemplate, String aKey, boolean aDefault) {
		return (aTemplate != null) ? aTemplate.getBoolean(aKey, aDefault) : aDefault;
	}
}

