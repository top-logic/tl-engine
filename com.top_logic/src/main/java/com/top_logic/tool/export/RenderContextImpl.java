/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.tool.export.ExcelCellRenderer.RenderContext;

/**
 * {@link RenderContext} for a given fixed column, and a modifiable object holding row informations.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RenderContextImpl implements RenderContext {

	private final RowContext _rowContext;

	private final Column _modelColumn;

	private final int _excelColumn;

	private final Object _customContext;
	
	/**
	 * Create a new {@link RenderContextImpl}.
	 */
	public RenderContextImpl(RowContext rowContext, Object customContext, Column modelColumn, int excelColumn) {
		_rowContext = rowContext;
		_customContext = customContext;
		_modelColumn = modelColumn;
		_excelColumn = excelColumn;
	}

	@Override
	public TableModel model() {
		return _rowContext.model();
	}

	@Override
	public Column modelColumn() {
		return _modelColumn;
	}

	@Override
	public int excelColumn() {
		return _excelColumn;
	}

	@Override
	public int modelRow() {
		return _rowContext.modelRow();
	}
	
	@Override
	public int excelRow() {
		return _rowContext.excelRow();
	}

	@Override
	public Object getCustomContext() {
		return _customContext;
	}

	@Override
	public Object getCellValue() {
		return model().getValueAt(modelRow(), modelColumn().getIndex());
	}

}
