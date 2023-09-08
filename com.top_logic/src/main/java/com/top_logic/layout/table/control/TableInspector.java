/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.util.Map;

import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.TreeTableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.util.Utils;

/**
 * {@link GuiInspectorCommand} inspecting {@link TableControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableInspector extends GuiInspectorCommand<TableControl, TableCell> {

	/**
	 * Singleton {@link TableInspector} instance.
	 */
	public static final TableInspector INSTANCE = new TableInspector();

	private TableInspector() {
		// Singleton constructor.
	}

	@Override
	protected TableCell findInspectedGuiElement(TableControl control, Map<String, Object> arguments)
			throws AssertionError {
		if (Utils.isTrue((Boolean) arguments.get("isTableCell"))) {
			return findInspectedTableCell(control, arguments);
		} else {
			return new TableCell(null, control.getTableData(), null, null);
		}
	}

	private TableCell findInspectedTableCell(TableControl tableControl, Map<String, Object> arguments) {
		int rowIndex = calcRowIndex(arguments);
		int columnIndex = calcColumnIndex(tableControl, arguments);

		TableViewModel viewModel = tableControl.getViewModel();
		Object rowObject = viewModel.getRowObject(rowIndex);
		String columnName = viewModel.getColumnName(columnIndex);
		Object value = getValue(tableControl, rowObject, columnName);
		if (Utils.isTrue((Boolean) arguments.get("isTreeNode"))) {
			return new TreeTableCell<>(value, tableControl.getTableData(), rowObject, columnName);
		} else {
			return new TableCell(value, tableControl.getTableData(), rowObject, columnName);
		}
	}

	private Object getValue(TableControl tableControl, Object rowObject, String columnName) {
		return tableControl.getViewModel().getValueAt(rowObject, columnName);
	}

	private int calcColumnIndex(TableControl tableControl, Map<String, Object> arguments) {
		int columnIndexIgnoringFixedColumns = (Integer) arguments.get("columnIndex");
		boolean isFixedColumn = (Boolean) arguments.get("isFixedColumn");
		int columnIndex = columnIndexIgnoringFixedColumns;

		if (!isFixedColumn) {
			columnIndex += findFixedColumnsCount(tableControl);
		}
		return columnIndex;
	}

	private int calcRowIndex(Map<String, Object> arguments) {
		return (Integer) arguments.get("rowIndex");
	}

	private int findFixedColumnsCount(TableControl tableControl) {
		int fixedColumnCount = Math.max(0, tableControl.getViewModel().getFixedColumnCount());
		if (fixedColumnCount == TableViewModel.NO_FIXED_COLUMN_PERSONALIZATION) {
			return 0;
		} else {
			assert fixedColumnCount >= 0 : "Unexpected value for the number of fixed columns: '" + fixedColumnCount
				+ "'! Can only interprete '-1' (no fixed columns) and values >= 0.";
			return fixedColumnCount;
		}
	}

	@Override
	protected void buildInspector(InspectorModel inspector, TableCell model) {
		GuiInspectorPluginFactory.createTableCellAssertions(inspector, model);
	}
}
