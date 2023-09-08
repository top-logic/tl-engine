/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.util.ArrayList;

/**
 * In memory representation of the contents of a database table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DatabaseContent {
	
	private final String _tableName;

	private final DBType[] _columnTypes;

	private final ArrayList<Object[]> _rows = new ArrayList<>();

	/**
	 * Creates a new {@link DatabaseContent}.
	 * 
	 * @param tableName
	 *        see {@link #getTableName()}.
	 * @param columnTypes
	 *        see {@link #getColumnTypes()}.
	 */
	public DatabaseContent(String tableName, DBType[] columnTypes) {
		_tableName = tableName;
		_columnTypes = columnTypes;
	}
	
	/**
	 * Adds the given row to {@link #getRows()}.
	 */
	public void addRow(Object[] row) {
		if (row.length != getColumnTypes().length) {
			throw new IllegalArgumentException();
		}
		getRows().add(row);
	}

	/**
	 * The name of the database table.
	 * 
	 * @return The name of the table as known in the database.
	 */
	public String getTableName() {
		return _tableName;
	}

	/**
	 * The types of the columns in the table.
	 */
	public DBType[] getColumnTypes() {
		return _columnTypes;
	}

	/**
	 * The rows in the table in the {@link #addRow(Object[]) insert order}.
	 * 
	 * <p>
	 * Each entry in the result list represents a row in the database.
	 * </p>
	 * <p>
	 * Each entry has the same length as {@link #getColumnTypes()}. The first entry in the row has
	 * the first entry of {@link #getColumnTypes()} as {@link DBType}, etc.
	 * </p>
	 * 
	 * <p>
	 * Note: This list is fully modifiable, therefore the restrictions may be violated.
	 * </p>
	 */
	public ArrayList<Object[]> getRows() {
		return _rows;
	}

}
