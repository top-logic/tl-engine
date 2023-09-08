/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.excel.POITypeSupporter;

/**
 * Set string values to excel cells via POI.
 * 
 * <p>When a given string starts with "=" it'll be interpreted as formula.</p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class StringAndFormularPOIType extends StringPOIType {

	@Override
	public Class<?> getHandlerClass() {
		return String.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		String theString = (String) aValue;

		if (theString.startsWith("=")) {
			aCell.setCellFormula(theString.substring(1, theString.length()));

			return 0;
		}
		else { 
			return super.setValue(aCell, aWorkbook, aValue, aSupport);
		}
	}
}

