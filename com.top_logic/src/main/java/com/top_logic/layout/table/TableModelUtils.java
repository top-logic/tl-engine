/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.VisiblePaneRequest;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;

/**
 * Utilities for accessing {@link TableModel}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableModelUtils {

	/**
	 * Column names of the given {@link TableModel} as set.
	 */
	public static Set<String> getColumnSet(TableModel model) {
		return addAllColumnNames(CollectionUtil.<String> newLinkedSet(model.getColumnCount()), model);
	}

	private static <C extends Collection<String>> C addAllColumnNames(C list, TableModel model) {
		for (int n = 0, size = model.getColumnCount(); n < size; n++) {
			list.add(model.getColumnName(n));
		}
		return list;
	}

	/**
	 * Column names that are declared as visible in the configuration of the given
	 * {@link TableModel} as set.
	 */
	public static Set<String> getDefaultVisibleColumnSet(TableModel model) {
		return addAllDefaultVisibleColumnNames(CollectionUtil.<String> newLinkedSet(model.getColumnCount()), model);
	}

	private static <C extends Collection<String>> C addAllDefaultVisibleColumnNames(C list, TableModel model) {
		for (int n = 0, size = model.getColumnCount(); n < size; n++) {
			ColumnConfiguration description = model.getColumnDescription(n);
			if (description.isVisible()) {
				list.add(model.getColumnName(n));
			}
		}
		return list;
	}

	/**
	 * The amount of fixed columns, defined by {@link TableConfiguration#getFixedColumnCount()},
	 * enhanced by some heuristics to mark technical columns (e.g. _select) as frozen additionally.
	 */
	public static int getConfiguredFixedColumns(TableConfiguration configuration, List<Column> visibleColumns) {
		int fixedColumnConfiguration = configuration.getFixedColumnCount();
		if (fixedColumnConfiguration > 0) {
			// Heuristics to adapt the configured column count to potentially added technical
			// default columns.
			// The idea is to calculate the maximum count of fixed columns, based on the configured
			// fixed column count and the column configurations of the technical columns. Then
			// iterate over the calculated maximum amount of fixed columns and every time you find a
			// technical column, increase the additional fixed column count.
			Collection<ColumnConfiguration> visibleDefaultColumns = getVisibleDefaultColumns();
			int maxFixedColumns =
				Math.min(fixedColumnConfiguration + visibleDefaultColumns.size(), visibleColumns.size());
			int additionalFixedColumns = 0;
			for (int i = 0; i < maxFixedColumns; i++) {
				Column column = visibleColumns.get(i);
				for (ColumnConfiguration defaultColumn : visibleDefaultColumns) {
					if (!column.getName().equals(defaultColumn.getName())) {
						continue;
					}
					additionalFixedColumns++;
				}
			}
			return fixedColumnConfiguration + additionalFixedColumns;
		} else {
			return fixedColumnConfiguration;
		}
	}

	private static Collection<ColumnConfiguration> getVisibleDefaultColumns() {
		Collection<? extends ColumnConfiguration> defaultColumns =
			TableConfiguration.defaultTable().getDeclaredColumns();
		Collection<ColumnConfiguration> visibleDefaultColumns =
			new ArrayList<>(defaultColumns.size());
		for (ColumnConfiguration defaultColumn : defaultColumns) {
			if (defaultColumn.isVisible()) {
				visibleDefaultColumns.add(defaultColumn);
			}
		}
		return visibleDefaultColumns;
	}

	/**
	 * Sets the given column names which are known by the application model.
	 * 
	 * <p>
	 * This method checks which column in the given list exists and sets a sublist of names
	 * containing the known columns.
	 * </p>
	 * 
	 * <p>
	 * Vetos are ignored.
	 * </p>
	 * 
	 * <p>
	 * If the final list of columns to set is empty, nothing is done, because empty columns are not
	 * allowed.
	 * </p>
	 * 
	 * @param model
	 *        The model to set column names to.
	 * @param columnNames
	 *        The column names to set. May be <code>null</code> in which case nothing happens.
	 */
	public static void setKnownColumns(TableViewModel model, List<String> columnNames) {
		if (columnNames == null) {
			return;
		}
		List<String> knownColumns;
		switch (columnNames.size()) {
			case 0:
				knownColumns = Collections.<String> emptyList();
				break;
			case 1:
				String column = columnNames.get(0);
				if (model.getApplicationModel().getColumnIndex(column) != TableModel.NO_ROW) {
					// Column is contained in table
					knownColumns = Collections.singletonList(column);
				} else {
					// Column is not contained in table
					knownColumns = Collections.<String> emptyList();
				}
				break;
			default:
				knownColumns = new ArrayList<>(columnNames);
				knownColumns.retainAll(model.getApplicationModel().getColumnNames());
				break;
		}
		if (knownColumns.isEmpty()) {
			// empty column list is not allowed
			return;
		}
		try {
			model.setColumns(knownColumns);
		} catch (VetoException ex) {
			// ignore
		}
	}

	/**
	 * Scrolls to the position of the first selected row in {@link VisiblePaneRequest} and sets the
	 * range of visible rows.
	 * 
	 * @param tableData
	 *        {@link TableData} with the selected rows.
	 */
	public static void scrollToSelectedRow(TableData tableData) {
		TableViewModel viewModel = tableData.getViewModel();
		viewModel.validate(DefaultDisplayContext.getDisplayContext());
		IndexRange showRowRange = IndexRange.undefined();
		List<Integer> selectedRows = new ArrayList<>();
		Set<?> selectedRowsObjects = tableData.getSelectionModel().getSelection();
		for (Object object : selectedRowsObjects) {
			int rowIndex = viewModel.getApplicationModel().getRowOfObject(object);
			if (rowIndex >= 0) {
				selectedRows.add(rowIndex);
			}
		}
		int min = Integer.MAX_VALUE;
		int max = 0;
		for (int rowIndex : selectedRows) {
			max = (rowIndex > max) ? rowIndex : max;
			min = (rowIndex < min) ? rowIndex : min;
		}
		// Scroll to the currently selected row to make it visible again.
		viewModel.getPagingModel().showRow(min);

		// Set row range
		if (selectedRows.size() > 0) {
			showRowRange = (min == max) ? IndexRange.singleIndex(min) : IndexRange.multiIndex(min, max);
		}
		viewModel.getClientDisplayData().getVisiblePaneRequest()
			.setRowRange(showRowRange);
	}

}
