/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * Application of a binary {@link Operator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BinaryOperation extends Operation {
	
	/**
	 * @see #getLeft()
	 */
	private Expression left;
	
	/**
	 * @see #getRight()
	 */
	private Expression right;

	/*package protected*/ BinaryOperation(Operator symbol, Expression left, Expression right) {
		super(symbol);
		
		assert symbol.getExpectedArguments() == 2 : "Operator mismatch.";
		
		this.left = left;
		this.right = right;
	}
	
	/**
	 * Left subexpression.
	 */
	public Expression getLeft() {
		return left;
	}

	public void setLeft(Expression left) {
		this.left = left;
	}
	
	/**
	 * Right subexpression.
	 */
	public Expression getRight() {
		return right;
	}
	
	public void setRight(Expression right) {
		this.right = right;
	}
	
	@Override
	public int getArgumentCount() {
		return 2;
	}
	
	@Override
	public Expression getArgument(int index) {
		switch (index) {
		case 0: return left;
		case 1: return right;
		default: throw new IndexOutOfBoundsException("Index greater than 2: " + index);
		}
	}

	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitBinaryOperation(this, arg);
	}
}
