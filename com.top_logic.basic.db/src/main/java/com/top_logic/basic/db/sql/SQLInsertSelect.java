/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.List;

/**
 * Bulk INSERT statement that inserts the result of another selection.
 * 
 * @see SQLQuery
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLInsertSelect extends AbstractSQLInsert {

	private SQLSelect _select;

	SQLInsertSelect(SQLTable table, List<String> columnNames, SQLSelect select) {
		super(table, columnNames);
		_select = select;
	}

	/**
	 * The selection to insert.
	 */
	public SQLSelect getSelect() {
		return _select;
	}

	/**
	 * @see #getSelect()
	 */
	public void setSelect(SQLSelect select) {
		_select = select;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLInsertSelect(this, arg);
	}

}
