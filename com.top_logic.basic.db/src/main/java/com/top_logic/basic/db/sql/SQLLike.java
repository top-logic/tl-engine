/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * SQL Expression <code>EXPR like 'PATTERN'</code>.
 */
public class SQLLike extends AbstractSQLExpression {

	private SQLExpression _expr;

	private String _pattern;
	
	SQLLike(SQLExpression expr, String pattern) {
		assert expr != null;
		assert pattern != null;
		
		_pattern = pattern;
		_expr = expr;
	}

	/**
	 * The expression part of this "like" construct.
	 */
	public SQLExpression getExpr() {
		return _expr;
	}

	/** @see #getExpr() */
	public void setExpr(SQLExpression expr) {
		assert expr != null;
		_expr = expr;
	}

	/**
	 * The pattern part of this "like" construct. The pattern part of this binary expression.
	 */
	public String getPattern() {
		return _pattern;
	}

	/** @see #getPattern() */
	public void setRightExpr(String pattern) {
		assert pattern != null;
		_pattern = pattern;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLLike(this, arg);
	}

}
