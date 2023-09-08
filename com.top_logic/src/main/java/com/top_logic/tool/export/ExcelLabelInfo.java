/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * This class can be used with the {@link ExcelExportSupport}. 
 * The value that is returnd by the method {@link #getValue()} is translated 
 * with the {@link MetaResourceProvider}. 
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class ExcelLabelInfo extends ExcelInfo {

	/** 
	 * Creates a new info object with a column and value.
	 * 
	 * @param column A column (0..n).
	 * @param value A value.  
	 */
	public ExcelLabelInfo(int column, Object value) {
		super(column, value);
	}
	
	/** 
	 * Creates a new info object with a column, value and cell formatter.
	 * 
	 * @param column A column (0..n).
	 * @param value A value.  
	 * @param cellFormatter A cell formatter to format the {@link ExcelValue}.
	 */
	public ExcelLabelInfo(int column, Object value, ExcelCellFormatter cellFormatter) {
		super(column, value, cellFormatter);
	}
	
	@Override
	public Object getValue() {
		return MetaResourceProvider.INSTANCE.getLabel(super.getValue());
	}
	
}
