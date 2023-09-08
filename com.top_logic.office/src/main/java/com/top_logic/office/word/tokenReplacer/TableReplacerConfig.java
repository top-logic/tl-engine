/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.tokenReplacer;

/**
 * Holds values for decision whether to add/remove columns/rows in a table.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class TableReplacerConfig {
	/**
	 * flag to decide whether columns should be added or not if table in template does contain
	 * less columns as the given fill
	 */
	private boolean _addColumns;

	/**
	 * flag to decide whether columns should be removed or not if table in template does contain
	 * more columns as the given fill
	 */
	private boolean _removeColumns;

	/**
	 * flag to decide whether rows should be added or not if table in template does contain less
	 * rows as the given fill
	 */
	private boolean _addRows;

	/**
	 * flag to decide whether rows should be removed or not if table in template does contain
	 * more rows as the given fill
	 */
	private boolean _removeRows;

	/**
	 * the values the table should be filled with
	 */
	private Object[][] _tableValues;

	/**
	 * Creates a new {@link TableReplacerConfig}.
	 * 
	 * <p>
	 * By default, tables will add new rows on demand, but will not remove odd rows given by the
	 * template.
	 * </p>
	 * 
	 * <p>
	 * Columns will not be added or removed by default.
	 * </p>
	 */
	public TableReplacerConfig(Object[][] values) {
		this(false, false, true, false, values);
	}

	/**
	 * Creates a new {@link TableReplacerConfig}
	 */
	public TableReplacerConfig(boolean addColumns, boolean removeColumns, boolean addRows, boolean removeRows,
			Object[][] values) {
		_addColumns = addColumns;
		_removeColumns = removeColumns;
		_addRows = addRows;
		_removeRows = removeRows;
		_tableValues = values;
	}

	public boolean isAddColumns() {
		return (_addColumns);
	}

	public boolean isAddRows() {
		return (_addRows);
	}

	public boolean isRemoveColumns() {
		return (_removeColumns);
	}

	public boolean isRemoveRows() {
		return (_removeRows);
	}

	public Object[][] getTableValues() {
		return (_tableValues);
	}
}