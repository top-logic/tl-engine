/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * {@link Operation} that operates on a single operand.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UnaryOperation extends Operation {
	
	/**
	 * @see #getExpr()
	 */
	private Expression expr;
	
	/*package protected*/ UnaryOperation(Operator symbol, Expression expr) {
		super(symbol);
		
		assert symbol.getExpectedArguments() == 1 : "Operator mismatch.";
		
		this.expr = expr;
	}
	
	/**
	 * The only argument to this operation.
	 */
	public Expression getExpr() {
		return expr;
	}
	
	public void setExpr(Expression expr) {
		this.expr = expr;
	}
	
	@Override
	public int getArgumentCount() {
		return 1;
	}
	
	@Override
	public Expression getArgument(int index) {
		switch (index) {
		case 0: return expr;
		default: throw new IndexOutOfBoundsException("Index greater than 1: " + index);
		}
	}
	
	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitUnaryOperation(this, arg);
	}
}
