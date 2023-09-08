/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link SQLExpression} that test for values being `NULL`.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLIsNull extends AbstractSQLExpression {

	private SQLExpression expr;

	SQLIsNull(SQLExpression expr) {
		this.expr = expr;
	}
	
	/**
	 * The expression to test for being `NULL`.
	 */
	public SQLExpression getExpr() {
		return expr;
	}

	/** @see #getExpr() */
	public void setExpr(SQLExpression expr) {
		this.expr = expr;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLIsNull(this, arg);
	}

}
