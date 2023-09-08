/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link AbstractSQLStatement} that modifies a {@link SQLTable}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSQLTableStatement extends AbstractSQLStatement {

	private SQLTable _table;

	/**
	 * Creates a new {@link AbstractSQLTableStatement}.
	 * 
	 * @param table
	 *        See {@link #getTable()}
	 */
	protected AbstractSQLTableStatement(SQLTable table) {
		setTable(table);
	}

	/**
	 * The {@link SQLTable} to be processed.
	 */
	public SQLTable getTable() {
		return _table;
	}

	/**
	 * Setter for {@link #getTable()}.
	 */
	public void setTable(SQLTable table) {
		assert table != null;
		if (table.getTableAlias() != null) {
			throw new IllegalArgumentException("Table alias in SQL " + getClass().getName() + " not allowed: " + table);
		}
		_table = table;
	}

}

