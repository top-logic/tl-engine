/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link SQLExpression} for the `not()` function.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLNot extends AbstractSQLExpression {

	private SQLExpression expr;

	SQLNot(SQLExpression expr) {
		assert expr != null;
		this.expr = expr;
	}
	
	/**
	 * The expression that is negated by this {@link SQLNot}.
	 */
	public SQLExpression getExpr() {
		return expr;
	}

	/** @see #getExpr() */
	public void setExpr(SQLExpression expr) {
		assert expr != null;
		this.expr = expr;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLNot(this, arg);
	}

}
