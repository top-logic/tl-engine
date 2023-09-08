/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.base.office.excel.ExcelValue;

/**
 * No op formatter.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class DoNothingCellFormatter implements ExcelCellFormatter {

	public static final DoNothingCellFormatter INSTANCE = new DoNothingCellFormatter();
	
	/** 
	 * @see com.top_logic.tool.export.ExcelCellFormatter#formatCell(com.top_logic.base.office.excel.ExcelValue)
	 */
	@Override
	public void formatCell(ExcelValue excelValue) {
	}

}
