/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.POIUtil;

/**
 * Super class for description of an POI excel sheet.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class POISheetDescription {

	private final String _sheetName;

	/**
	 * Constructor creates a new {@link POISheetDescription}.
	 */
	public POISheetDescription(String sheetName) {
		_sheetName = sheetName;
	}

	/**
	 * Returns the name of the described sheet.
	 */
	public String getSheetName() {
		return _sheetName;
	}

	/**
	 * Returns the represented sheet of the given workbook.
	 */
	public Sheet getSheet(Workbook workbook) {
		String theSheet = getSheetName();

		if (theSheet == null) {
			theSheet = workbook.getSheetName(0); // DEFAULT_SHEETNAME;
		}
		return POIUtil.createIfNull(theSheet, workbook);
	}

}
