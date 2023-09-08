/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * Describe a "DELETE FROM {@link #getTable() 'TABLE'} WHERE {@link #getCondition() 'CONDITION'}"
 * statement.
 * 
 * @see SQLFactory#delete(SQLTable, SQLExpression)
 * @see SQLQuery
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLDelete extends AbstractSQLTableStatement {

	private SQLExpression _condition;

	/**
	 * Creates a new {@link SQLDelete}.
	 * 
	 * @param table
	 *        See {@link #getTable()}.
	 * @param condition
	 *        See {@link #getCondition()}.
	 */
	SQLDelete(SQLTable table, SQLExpression condition) {
		super(table);
		setCondition(condition);
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLDelete(this, arg);
	}

	/**
	 * {@link SQLExpression} that defines the rows to delete.
	 */
	public SQLExpression getCondition() {
		return _condition;
	}

	/**
	 * Setter for {@link #getCondition()}.
	 */
	public void setCondition(SQLExpression condition) {
		assert condition != null;
		_condition = condition;
	}

}

