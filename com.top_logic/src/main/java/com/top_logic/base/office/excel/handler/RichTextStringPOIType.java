/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.basic.StringServices;

/**
 * Set rich text string values to excel cells via POI.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class RichTextStringPOIType implements PoiTypeHandler {

	@Override
	public Class<?> getHandlerClass() {
		return RichTextString.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		RichTextString theString = (RichTextString) aValue;

		if (aCell instanceof HSSFCell) {
			aCell.setCellType(CellType.STRING);
		}

		aCell.setCellValue(theString);
		return length(theString);
	}

	private int length(RichTextString rts) {
		return rts == null ? 0 : StringServices.nonNull(rts.getString()).length();
	}
}

