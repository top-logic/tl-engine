/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;


/**
 * SQL test, if an expression result is within the results of a sub-query.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLInSetSelect extends AbstractSQLExpression {

	private SQLExpression _expr;

	private SQLSelect _select;

	/**
	 * Creates a {@link SQLInSetSelect}.
	 * 
	 * @param expr
	 *        See {@link #getExpr()}.
	 * @param select
	 *        See {@link #getSelect()}.
	 */
	SQLInSetSelect(SQLExpression expr, SQLSelect select) {
		_expr = expr;
		_select = select;
	}

	/**
	 * The expression to test.
	 */
	public SQLExpression getExpr() {
		return _expr;
	}

	/** @see #getExpr() */
	public void setExpr(SQLExpression expr) {
		_expr = expr;
	}

	/**
	 * The sub-query that must match the test value.
	 */
	public SQLSelect getSelect() {
		return _select;
	}

	/** @see #getSelect() */
	public void setSelect(SQLSelect select) {
		_select = select;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLInSetSelect(this, arg);
	}

}
