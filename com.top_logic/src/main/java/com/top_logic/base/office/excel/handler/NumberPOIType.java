/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.basic.util.NumberUtil;

/**
 * Set number values to excel cells via POI (which are always Double).
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class NumberPOIType implements PoiTypeHandler {

	@Override
	public Class<?> getHandlerClass() {
		return Number.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		Double theNumber = NumberUtil.getDouble((Number) aValue);

		if (aCell instanceof HSSFCell) {
			try {
				aCell.setCellType(CellType.NUMERIC);
			}
			catch (RuntimeException ex) {
	        	// Workaround for unknown exception in HSSFCell with CellType.NUMERIC
			}
		}

		aCell.setCellValue(theNumber);
		return theNumber.toString().length();
	}
}

