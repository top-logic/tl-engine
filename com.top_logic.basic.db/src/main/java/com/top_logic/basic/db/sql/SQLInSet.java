/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;


/**
 * SQL test, if an expression result is within a literal set of values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLInSet extends AbstractSQLExpression {

	private SQLExpression expr;

	private SQLExpression values;

	/**
	 * Creates a {@link SQLInSet}.
	 * 
	 * @param expr
	 *        See {@link #getExpr()}.
	 * @param values
	 *        See {@link #getValues()}.
	 */
	SQLInSet(SQLExpression expr, SQLExpression values) {
		this.expr = expr;
		this.values = values;
	}

	/**
	 * The expression to test.
	 */
	public SQLExpression getExpr() {
		return expr;
	}

	/** @see #getExpr() */
	public void setExpr(SQLExpression expr) {
		this.expr = expr;
	}

	/**
	 * The literal values that the {@link #getExpr()} is tested to be a member
	 * of.
	 */
	public SQLExpression getValues() {
		return values;
	}

	/** @see #getValues() */
	public void setValues(SQLExpression values) {
		this.values = values;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLInSet(this, arg);
	}

}
