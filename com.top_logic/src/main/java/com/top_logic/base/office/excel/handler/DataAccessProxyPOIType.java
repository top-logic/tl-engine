/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.dsa.DataAccessProxy;

/**
 * Embed the stream from a given data access proxy into an excel file (assumed that it is an image).
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DataAccessProxyPOIType extends AbstractImagePOIType {

	@Override
	public Class<?> getHandlerClass() {
		return DataAccessProxy.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		DataAccessProxy theFile   = (DataAccessProxy) aValue;
		InputStream     theStream = null;

		try {
			theStream = theFile.getEntry();

			this.exportImage(aCell, theStream, this.getImageType(theFile.getName()), aSupport.getTemplate(aCell), aSupport.getDrawingManager());
		}
		catch (FileNotFoundException ex) {
			Logger.error("Failed to get '" + theFile + "', will not be exported", ex, DataAccessProxyPOIType.class);
		} 
		catch (IOException ex) {
			Logger.error("Failed to get data from '" + theFile + "', will not be exported", ex, DataAccessProxyPOIType.class);
		}
		finally {
			FileUtilities.close(theStream);
		}
		return 0;
	}
}

