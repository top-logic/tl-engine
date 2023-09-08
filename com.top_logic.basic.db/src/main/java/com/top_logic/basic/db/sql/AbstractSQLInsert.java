/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for insertion statements.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSQLInsert extends AbstractSQLStatement {

	private SQLTable table;

	private List<String> columnNames;

	AbstractSQLInsert(SQLTable table, List<String> columnNames) {
		assert table != null;
		assert columnNames != null;

		this.table = table;
		this.columnNames = new ArrayList<>(columnNames);
	}

	/**
	 * The table specification to insert into.
	 */
	public SQLTable getTable() {
		return table;
	}

	/**
	 * @see #getTable()
	 */
	public void setTable(SQLTable table) {
		assert table != null;
		this.table = table;
	}

	/**
	 * The names of the columns to fill.
	 */
	public List<String> getColumnNames() {
		return columnNames;
	}

	/** @see #getColumnNames() */
	public void setColumnNames(List<String> columnNames) {
		assert columnNames != null;
		this.columnNames = columnNames;
	}

}
