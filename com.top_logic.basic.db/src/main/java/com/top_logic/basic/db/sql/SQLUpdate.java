/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.List;

/**
 * SQL update statement.
 * 
 * @see SQLQuery
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLUpdate extends AbstractSQLTableStatement {

	private SQLExpression where;

	private List<String> columns;

	private List<SQLExpression> values;

	
	SQLUpdate(SQLTable table, SQLExpression where, List<String> columns,
			List<SQLExpression> values) {
		super(table);
		assert where != null;
		assert columns != null;
		assert values != null;
		assert columns.size() == values.size();
		
		this.where = where;
		this.columns = columns;
		this.values = values;
	}

	public SQLExpression getWhere() {
		return where;
	}

	/**@see #getWhere()*/
	public void setWhere(SQLExpression where) {
		assert where != null;
		this.where = where;
	}

	public List<String> getColumns() {
		return columns;
	}
	
	/** @see #getColumns() */
	public void setColumns(List<String> columns) {
		assert columns != null;
		this.columns = columns;
	}

	/** @see #getValues() */
	public List<SQLExpression> getValues() {
		return values;
	}

	/** @see #getValues() */
	public void setValues(List<SQLExpression> values) {
		assert values != null;
		this.values = values;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R,A> v, A arg) {
		return v.visitSQLUpdate(this, arg);
	}

}
