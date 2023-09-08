/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.Column;

/**
 * {@link ExcelCellRenderer} that only needs the cell value of the table model to export it to
 * Excel.
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractExcelCellRenderer implements ExcelCellRenderer {

	@Override
	public ExcelValue renderCell(RenderContext context) {
		return renderValue(context, context.getCellValue(), context.excelRow(), context.excelColumn());
	}

	/**
	 * By default an {@link AbstractExcelCellRenderer} needs only one column.
	 * 
	 * @return 1
	 * 
	 * @see ExcelCellRenderer#colSpan(TableModel, Column)
	 */
	@Override
	public int colSpan(TableModel model, Column modelColumn) {
		return 1;
	}

	@Override
	public Object newCustomContext(TableModel model, Column modelColumn) {
		return null;
	}

	/**
	 * Returns an {@link ExcelValue} for the given value.
	 * 
	 * @param context
	 *        The context in which exporting occurs.
	 * @param cellValue
	 *        The value to create {@link ExcelValue} for
	 * @param excelRow
	 *        The row in excel to store value.
	 * @param excelColumn
	 *        The column in excel to store value.
	 * 
	 * @see AbstractExcelCellRenderer#renderCell(RenderContext)
	 */
	protected abstract ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn);

}

