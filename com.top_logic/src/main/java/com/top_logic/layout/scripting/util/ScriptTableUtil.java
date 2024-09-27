/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.util;

import static com.top_logic.basic.shared.string.StringServicesShared.isEmpty;
import static com.top_logic.basic.util.Utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.layout.scripting.recorder.ref.value.TableColumnRef;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * Utilities for working with tables ({@link TableModel}, {@link TableData}, ...) in the scripting
 * framework.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptTableUtil {

	/**
	 * The label of the column with the given technical name.
	 * <p>
	 * If not column with the given name exists, a {@link RuntimeException} is thrown.
	 * </p>
	 * 
	 * @param columnName
	 *        Is not allowed to be null.
	 * @param tableData
	 *        Is not allowed to be null.
	 * @return Never null. Is allowed to be the empty String.
	 */
	public static String getColumnLabel(String columnName, TableData tableData) {
		return Resources.getInstance().getString(getColumn(columnName, tableData).getLabel(tableData));
	}

	/**
	 * The {@link Column} with the given technical name.
	 * <p>
	 * If not column with the given name exists, a {@link RuntimeException} is thrown.
	 * </P>
	 * 
	 * @param columnName
	 *        Is not allowed to be null.
	 * @param tableData
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	private static Column getColumn(String columnName, TableData tableData) {
		requireNonNull(columnName, "Column name is not allowed to be null.");
		Column column = tableData.getTableModel().getHeader().getColumn(columnName);
		if (column == null) {
			throw new NoSuchElementException("There is no column named '" + columnName + "'."
				+ " Columns: " + getColumnNames(tableData));
		}
		return column;
	}

	/**
	 * The names of all columns in the given table.
	 * 
	 * @param tableData
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static List<String> getColumnNames(TableData tableData) {
		return tableData.getTableModel().getColumnNames();
	}

	/**
	 * The labels of columns in the given table.
	 * 
	 * @param tableData
	 *        Is not allowed to be null.
	 * @param allColumns
	 *        Whether all columns are demanded or just the displayed.
	 * @return Never null.
	 */
	public static List<String> getColumnLabels(TableData tableData, boolean allColumns) {
		List<String> columnNames;
		if (allColumns) {
			columnNames = getColumnNames(tableData);
		} else {
			columnNames = tableData.getViewModel().getColumnNames();
		}
		return toColumnLabels(columnNames, tableData);
	}

	/**
	 * Get the column referenced by the given {@link TableColumnRef}.
	 * 
	 * @param columnRef
	 *        Is not allowed to be null.
	 * @param tableData
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static String getColumnName(TableColumnRef columnRef, TableData tableData) {
		boolean isNameEmpty = isEmpty(columnRef.getColumnName());
		boolean isLabelEmpty = isEmpty(columnRef.getColumnLabel());
		if (isNameEmpty && isLabelEmpty) {
			String message = "Either the name or the label of the column has to be set. But both are empty.";
			throw new RuntimeException(
				message);
		}
		if ((!isNameEmpty) && !isLabelEmpty) {
			String message = "Either the name or the label of the column has to be set. But both are set.";
			throw new RuntimeException(message);
		}
		if (!isNameEmpty) {
			return columnRef.getColumnName();
		}
		return getColumnName(columnRef.getColumnLabel(), tableData);
	}

	/**
	 * Get the column with the given label.
	 * <p>
	 * If not column with the given label exists, a {@link RuntimeException} is thrown.
	 * </p>
	 * 
	 * @param searchedLabel
	 *        Is not allowed to be null.
	 * @param tableData
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static String getColumnName(String searchedLabel, TableData tableData) {
		return getColumnName(searchedLabel, tableData, true);
	}

	/**
	 * Get the column with the given label.
	 * <p>
	 * If not column with the given label exists, a {@link RuntimeException} is thrown.
	 * </p>
	 * 
	 * @param searchedLabel
	 *        Is not allowed to be null.
	 * @param tableData
	 *        Is not allowed to be null.
	 * @param onlyVisible
	 *        Whether invisible columns should also be considered.
	 * @return Never null.
	 */
	public static String getColumnName(String searchedLabel, TableData tableData, boolean onlyVisible) {
		requireNonNull(searchedLabel, "Column label is not allowed to be null.");
		SearchResult<Column> searchResult = new SearchResult<>();
		for (Column column : getColumns(tableData)) {
			if (onlyVisible && !column.isVisible()) {
				continue;
			}
			String actualLabel = Resources.getInstance().getString(column.getLabel(tableData));
			searchResult.addCandidate(actualLabel);
			if (Utils.equals(actualLabel, searchedLabel)) {
				searchResult.add(column);
			}
		}
		String errorPrefix = "Search for a single column with the label '" + searchedLabel + "' failed.";
		return searchResult.getSingleResult(errorPrefix).getName();
	}

	private static List<Column> getColumns(TableData tableData) {
		return tableData.getViewModel().getHeader().getAllElementaryColumns();
	}

	/**
	 * Get the names of the columns with the given labels.
	 * 
	 * @param columnLabels
	 *        Is not allowed to be null.
	 * @param tableData
	 *        Is not allowed to be null.
	 * @param onlyVisible
	 *        Whether to exclude invisible columns.
	 * @return Never null. A new, mutable and resizable list.
	 */
	public static List<String> toColumnNames(Iterable<String> columnLabels, TableData tableData, boolean onlyVisible) {
		List<String> list = new ArrayList<>();
		for (String columnLabel : columnLabels) {
			list.add(getColumnName(columnLabel, tableData, onlyVisible));
		}
		return list;
	}

	/**
	 * Get the labels of the columns with the given names.
	 * 
	 * @param columnNames
	 *        Is not allowed to be null.
	 * @param tableData
	 *        Is not allowed to be null.
	 * @return Never null. A new, mutable and resizable list.
	 */
	public static List<String> toColumnLabels(Iterable<String> columnNames, TableData tableData) {
		List<String> list = new ArrayList<>();
		for (String columnName : columnNames) {
			list.add(getColumnLabel(columnName, tableData));
		}
		return list;
	}

	/** Whether the given column can be filtered. */
	public static boolean canBeFiltered(TableData tableData, String columnName) {
		return tableData.getViewModel().getFilter(columnName) != null;
	}

	/** Whether the given column can be sorted. */
	public static boolean canBeSorted(TableData tableData, String columnName) {
		int columnIndex = tableData.getViewModel().getColumnIndex(columnName);
		return tableData.getViewModel().isSortable(columnIndex);
	}

}
