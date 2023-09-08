/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.base.office.excel.ExcelValue;

/**
 * The ExcelCellFormatter configures the style of {@link ExcelValue}s.
 * 
 * <p>
 * NOTE: All {@link ExcelCellFormatter}s must be {@link Comparable}! All sub classes must be
 * implement the {@link Object#equals(Object)} and {@link Object#hashCode()} methods because the
 * formatter is used as key for the cell style cache of the {@link ExcelExportSupport}.
 * </p>
 * 
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface ExcelCellFormatter {

	/**
	 * This method sets the cell style to the given excel value.
	 * 
	 * @param excelValue
	 *            The excel value must NOT be <code>null</code>.
	 */
	public void formatCell(ExcelValue excelValue);
	
}
