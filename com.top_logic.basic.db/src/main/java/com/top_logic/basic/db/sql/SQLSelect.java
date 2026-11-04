/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.List;

import com.top_logic.basic.sql.DBHelper;

/**
 * SQL SELECT statement.
 * 
 * @see SQLQuery
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public class SQLSelect extends AbstractSQLStatement implements SQLSelectionStatement {

	private boolean distinct;
	
	/** @see #getNoBlockHint() */
	private boolean _noBlockHint;

	private SQLTableReference tableReference;

	private List<SQLColumnDefinition> columns;

	private SQLExpression where;

	private List<SQLOrder> orderBy;

	private SQLLimit _limit;

	private boolean _forUpdate;

	SQLSelect(boolean distinct, List<SQLColumnDefinition> columns, SQLTableReference tableReference, SQLExpression where, List<SQLOrder> orderBy, SQLLimit limit) {
		assert columns != null;
		assert tableReference != null;
		assert where != null;
		assert orderBy != null;
		
		this.distinct = distinct;
		this.columns = columns;
		this.tableReference = tableReference;
		this.where = where;
		this.orderBy = orderBy;
		this._limit = limit;
	}

	/**
	 * Whether duplicate result rows are eliminated.
	 */
	public boolean isDistinct() {
		return distinct;
	}
	
	/** @see #isDistinct() */
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	/**
	 * The table to select from.
	 */
	public SQLTableReference getTableReference() {
		return tableReference;
	}
	
	@Override
	public SQLTable getInitialTable() {
		return getTableReference().getInitialTable();
	}

	/**
	 * Setter for {@link #getTableReference()}.
	 */
	public void setTableReference(SQLTableReference tableReference) {
		assert tableReference != null;
		this.tableReference = tableReference;
	}
	
	/**
	 * The condition the selected rows must match.
	 */
	public SQLExpression getWhere() {
		return where;
	}

	/**
	 * Setter for {@link #getWhere()}.
	 */
	public void setWhere(SQLExpression where) {
		assert where != null;
		this.where = where;
	}

	/**
	 * The order of the result rows.
	 */
	public List<SQLOrder> getOrderBy() {
		return orderBy;
	}

	/** @see #getOrderBy() */
	public void setOrderBy(List<SQLOrder> orderBy) {
		assert orderBy != null;
		this.orderBy = orderBy;
	}

	/**
	 * Returns the limitation of the result of this select.
	 */
	public SQLLimit getLimit() {
		return _limit;
	}

	/**
	 * Sets {@link #getLimit()}
	 */
	public void setLimit(SQLLimit limit) {
		this._limit = limit;
	}

	/**
	 * The columns of the result.
	 */
	public List<SQLColumnDefinition> getColumns() {
		return columns;
	}
	
	/** @see #getColumns() */
	public void setColumns(List<SQLColumnDefinition> columns) {
		assert columns != null;
		this.columns = columns;
	}

	/**
	 * Hint for the database whether the execution of this select shall not block.
	 * 
	 * @see DBHelper#selectNoBlockHint()
	 */
	public boolean getNoBlockHint() {
		return _noBlockHint;
	}

	/**
	 * Setter for {@link #getNoBlockHint()}.
	 */
	public void setNoBlockHint(boolean hint) {
		_noBlockHint = hint;
	}

	/**
	 * Whether the accessed rows should be locked for future updates.
	 */
	public boolean isForUpdate() {
		return _forUpdate;
	}

	/**
	 * @see #isForUpdate()
	 */
	public void setForUpdate(boolean value) {
		_forUpdate = value;
	}

	/**
	 * Call-chaining variant of {@link #setForUpdate(boolean)}
	 */
	public SQLStatement forUpdate() {
		setForUpdate(true);
		return this;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R,A> v, A arg) {
		return v.visitSQLSelect(this, arg);
	}

}
