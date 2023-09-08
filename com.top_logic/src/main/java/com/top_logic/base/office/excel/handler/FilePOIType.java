/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.FileUtilities;

/**
 * Embed a given file into an excel file (assumed that it is an image).
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FilePOIType extends AbstractImagePOIType {

	@Override
	public Class<?> getHandlerClass() {
		return File.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		InputStream theStream = null;
		File        theFile   = (File) aValue;

		try {
			theStream = new FileInputStream(theFile);

			this.exportImage(aCell, theStream, this.getImageType(theFile.getName()), aSupport.getTemplate(aCell), aSupport.getDrawingManager());
		}
		catch (FileNotFoundException ex) {
			Logger.error("Failed to get '" + theFile + "', will not be exported", ex, this);
		} 
		catch (IOException ex) {
			Logger.error("Failed to get data from '" + theFile + "', will not be exported", ex, this);
		}
		finally {
			FileUtilities.close(theStream);
		}
		return 0;
	}
}

