/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link SQLExpression} is a conditional statement providing if/then/else logic for any ordinary
 * SQL command, such as {@link SQLSelect} or {@link SQLUpdate}. It then provides when-then-else
 * functionality (WHEN this condition is met THEN do_this ELSE do_that).
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLCase extends AbstractSQLExpression {
	
	/** @see #getCondition() */
	private SQLExpression _condition;

	/** @see #getThen() */
	private SQLExpression _then;

	/** @see #getElse() */
	private SQLExpression _else;

	/**
	 * Creates a new {@link SQLCase}.
	 * 
	 * @param conditionExpr
	 *        Value of {@link #getCondition()}.
	 * @param thenExpr
	 *        Value of {@link #getThen()}.
	 * @param elseExpr
	 *        Value of {@link #getElse()}.
	 */
	SQLCase(SQLExpression conditionExpr, SQLExpression thenExpr, SQLExpression elseExpr) {
		assert conditionExpr != null;
		assert thenExpr != null;
		assert elseExpr != null;
		_condition = conditionExpr;
		_then = thenExpr;
		_else = elseExpr;
	}

	/**
	 * Condition that decides whether {@link #getThen()} or {@link #getElse()} is executed.
	 */
	public SQLExpression getCondition() {
		return _condition;
	}

	/** @see #setCondition(SQLExpression) */
	public void setCondition(SQLExpression condition) {
		assert condition != null;
		_condition = condition;
	}

	/**
	 * Condition to execute if {@link #getCondition()} is <code>true</code>.
	 */
	public SQLExpression getThen() {
		return _then;
	}

	/** @see #getThen() */
	public void setThen(SQLExpression thenExpr) {
		assert thenExpr != null;
		_then = thenExpr;
	}

	/**
	 * Condition to execute if {@link #getCondition()} is <code>false</code>.
	 */
	public SQLExpression getElse() {
		return _else;
	}

	/** @see #getElse() */
	public void setElse(SQLExpression elseExpr) {
		assert elseExpr != null;
		_else = elseExpr;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLCase(this, arg);
	}

}

