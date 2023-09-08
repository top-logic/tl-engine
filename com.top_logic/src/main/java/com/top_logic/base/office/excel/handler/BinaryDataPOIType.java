/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Embed a given binary data object into an excel file (assumed that it is an image).
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class BinaryDataPOIType extends AbstractImagePOIType {

	@Override
	public Class<?> getHandlerClass() {
		return BinaryData.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		InputStream theStream = null;
		BinaryData theData = BinaryData.cast(aValue);
		String      theName   = theData.getName();

		if (theName == null) {
			theName = theData.toString();
		}

		try {
			theStream = theData.getStream();

			this.exportImage(aCell, theStream, this.getImageType(theData), aSupport.getTemplate(aCell), aSupport.getDrawingManager());
		}
		catch (IOException ex) {
			Logger.error("Failed to get data from '" + theName + "', will not be exported", ex, this);
		}
		finally {
			FileUtilities.close(theStream);
		}
		return 0;
	}

	/** 
	 * Return the content type of the given binary data object in an excel flavor.
	 * 
	 * @param    someData    The binary data to get the image type from.
	 * @return   The requested image type.
	 */
	protected int getImageType(BinaryData someData) {
		String theType   = someData.getContentType();
        int    theIndex  = theType.lastIndexOf('/') + 1;
        String theSuffix = theType.substring(theIndex);

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
            throw new IllegalArgumentException("Unsupported format for content type '" + theType + "'!");
        }
	}
}

