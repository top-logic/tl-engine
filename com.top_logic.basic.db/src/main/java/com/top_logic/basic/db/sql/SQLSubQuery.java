/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link SQLPart} representing a query as part of a {@link SQLStatement}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLSubQuery extends AbstractSQLTableReference {

	private SQLSelectionStatement _select;

	private String _tableAlias;

	/**
	 * Creates a {@link SQLSubQuery}.
	 * 
	 * @param select
	 *        See {@link #getSelect()}.
	 * @param tableAlias
	 *        See {@link #getTableAlias()}.
	 */
	public SQLSubQuery(SQLSelectionStatement select, String tableAlias) {
		this._select = select;
		this._tableAlias = tableAlias;
	}

	/**
	 * The virtual table created by can inner {@link SQLSelect}.
	 */
	public SQLSelectionStatement getSelect() {
		return _select;
	}

	/** @see #getSelect() */
	public void setSelect(SQLSelectionStatement select) {
		_select = select;
	}

	/**
	 * The alias name under which the virtual table {@link #getSelect()} can be accessed.
	 */
	public String getTableAlias() {
		return _tableAlias;
	}

	/** @see #getTableAlias() */
	public void setTableAlias(String tableAlias) {
		_tableAlias = tableAlias;
	}

	@Override
	public SQLTable getInitialTable() {
		return _select.getInitialTable();
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLSubQuery(this, arg);
	}

}
