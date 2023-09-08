/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.base.office.excel.ExcelValue;

/**
 * This class can be used with the {@link ExcelExportSupport}.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class ExcelInfo {

	private int column;
	private Object value;
	private ExcelCellFormatter formatter;

	/** 
	 * Creates a new info object with a column and value.
	 * 
	 * @param column A column (0..n).
	 * @param value A value.  
	 */
	public ExcelInfo(int column, Object value) {
		this.column = column;
		this.value = value;
	}
	
	/** 
	 * Creates a new info object with a column, value and cell formatter.
	 * 
	 * @param column A column (0..n).
	 * @param value A value.  
	 * @param cellFormatter A cell formatter to format the {@link ExcelValue}.
	 */
	public ExcelInfo(int column, Object value, ExcelCellFormatter cellFormatter) {
		this.column = column;
		this.value = value;
		this.formatter = cellFormatter;
	}
	
	/**
	 * Returns the column.
	 */
	public int getColumn() {
		return this.column;
	}

	/**
	 * Returns the value.
	 */
	public Object getValue() {
		return this.value;
	}

	/**
	 * Returns the formatter.
	 */
	public ExcelCellFormatter getFormatter() {
		return this.formatter;
	}
	
}
