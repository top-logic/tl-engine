/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export.tableview;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.LabelResourceProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.ExcelCellRenderer.RenderContext;

/**
 * {@link RenderContext} of a cell of a {@link com.top_logic.table.TableView} export.
 *
 * <p>
 * Such an export runs over no {@link TableModel}: {@link #model()} and {@link #modelColumn()} are
 * {@code null}, and an {@link ExcelCellRenderer} renders from the {@link #getCellValue() cell
 * value} alone.
 * </p>
 */
final class ViewRenderContext implements RenderContext {

	private final Object _value;

	private final int _modelRow;

	private final int _excelRow;

	private final int _excelColumn;

	private final Object _customContext;

	ViewRenderContext(Object value, int modelRow, int excelRow, int excelColumn, Object customContext) {
		_value = value;
		_modelRow = modelRow;
		_excelRow = excelRow;
		_excelColumn = excelColumn;
		_customContext = customContext;
	}

	@Override
	public TableModel model() {
		return null;
	}

	@Override
	public Column modelColumn() {
		return null;
	}

	@Override
	public int excelColumn() {
		return _excelColumn;
	}

	@Override
	public int modelRow() {
		return _modelRow;
	}

	@Override
	public int excelRow() {
		return _excelRow;
	}

	@Override
	public Object getCustomContext() {
		return _customContext;
	}

	@Override
	public Object getCellValue() {
		return _value;
	}

	@Override
	public ResourceProvider resourceProvider() {
		// Labels the elements of a collection value as a single value is labelled.
		return LabelResourceProvider.toResourceProvider(MetaLabelProvider.INSTANCE);
	}

}
