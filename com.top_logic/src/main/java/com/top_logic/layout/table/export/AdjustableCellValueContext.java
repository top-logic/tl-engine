/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import com.top_logic.layout.table.model.Column;
import com.top_logic.tool.export.ExcelCellRenderer.RenderContext;
import com.top_logic.tool.export.RenderContextImpl;
import com.top_logic.tool.export.RowContext;

/**
 * {@link RenderContext}, whose cell value can be changed on demand.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class AdjustableCellValueContext extends RenderContextImpl {

	private Object _cellValue;

	/**
	 * Creates a {@link AdjustableCellValueContext}.
	 */
	public AdjustableCellValueContext(RowContext rowContext, Object customContext, Column modelColumn,
			int excelColumn) {
		super(rowContext, customContext, modelColumn, excelColumn);
	}

	@Override
	public Object getCellValue() {
		return _cellValue;
	}

	/**
	 * Adjust cell value of this context.
	 */
	public void setCellValue(Object cellValue) {
		_cellValue = cellValue;
	}
}
