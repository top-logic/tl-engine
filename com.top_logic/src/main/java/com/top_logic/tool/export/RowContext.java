/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.layout.table.TableModel;
import com.top_logic.tool.export.ExcelCellRenderer.RenderContext;

/**
 * Modifiable object holding information about the row to export.
 * 
 * @see RenderContextImpl
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RowContext {

	private final TableModel _model;

	private int _modelRow;

	private int _excelRow;

	/**
	 * Create a new {@link RowContext}
	 */
	public RowContext(TableModel model, int modelRow, int excelRow) {
		_model = model;
		updateRows(modelRow, excelRow);
	}

	/**
	 * @see RenderContext#modelRow()
	 */
	int modelRow() {
		return _modelRow;
	}

	/**
	 * @see RenderContext#excelRow()
	 */
	int excelRow() {
		return _excelRow;
	}

	/**
	 * @see RenderContext#model()
	 */
	TableModel model() {
		return _model;
	}

	/**
	 * Updates {@link #modelRow()} and {@link #excelRow()}.
	 * 
	 * @param modelRow
	 *        New value of {@link #modelRow()}.
	 * @param excelRow
	 *        New value of {@link #excelRow()}.
	 */
	public void updateRows(int modelRow, int excelRow) {
		_modelRow = modelRow;
		_excelRow = excelRow;
	}

}

