/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * BinaryPart of an SQL Expression like <code>LEFT oper RIGHT</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLBinaryExpression extends AbstractSQLExpression {

	private SQLOp op;

	private SQLExpression leftExpr;

	private SQLExpression rightExpr;
	
	SQLBinaryExpression(SQLOp op, SQLExpression leftExpr, SQLExpression rightExpr) {
		assert op != null;
		assert leftExpr != null;
		assert rightExpr != null;
		
		this.op = op;
		this.leftExpr = leftExpr;
		this.rightExpr = rightExpr;
	}

	/**
	 * Operator used to connect {@link #getLeftExpr()} and {@link #getRightExpr()}.
	 */
	public SQLOp getOp() {
		return op;
	}

	/** @see #getOp() */
	public void setOp(SQLOp op) {
		assert op != null;
		this.op = op;
	}

	/**
	 * The left part of this binary expression.
	 */
	public SQLExpression getLeftExpr() {
		return leftExpr;
	}

	/** @see #getLeftExpr() */
	public void setLeftExpr(SQLExpression leftExpr) {
		assert leftExpr != null;
		this.leftExpr = leftExpr;
	}

	/**
	 * The right part of this binary expression.
	 */
	public SQLExpression getRightExpr() {
		return rightExpr;
	}

	/** @see #getRightExpr() */
	public void setRightExpr(SQLExpression rightExpr) {
		assert rightExpr != null;
		this.rightExpr = rightExpr;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLBinaryExpression(this, arg);
	}

}
