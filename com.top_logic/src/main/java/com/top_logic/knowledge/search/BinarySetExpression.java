/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Base class for binary expressions of {@link SetExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class BinarySetExpression extends SetExpression {

	private SetExpression leftExpr;
	private SetExpression rightExpr;

	/**
	 * Creates a {@link BinarySetExpression}.
	 * 
	 * @param expr1
	 *        See {@link #getLeftExpr()}
	 * @param expr2
	 *        See {@link #getRightExpr()}
	 */
	public BinarySetExpression(SetExpression expr1, SetExpression expr2) {
		this.leftExpr = expr1;
		this.rightExpr = expr2;
	}
	
	/**
	 * The left hand side of this {@link BinarySetExpression}.
	 */
	public SetExpression getLeftExpr() {
		return leftExpr;
	}
	
	public void setLeftExpr(SetExpression leftExpr) {
		this.leftExpr = leftExpr;
	}
	
	/**
	 * The right hand side of this {@link BinarySetExpression}.
	 */
	public SetExpression getRightExpr() {
		return rightExpr;
	}
	
	public void setRightExpr(SetExpression rightExpr) {
		this.rightExpr = rightExpr;
	}

}
