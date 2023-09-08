/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

/**
 * Represents a binary {@link Expression} consisting of a left {@link Expression} an
 * {@link Expression.Operator Operator} in the middle and a right {@link Expression}.
 * 
 * @see #getLeftExpression()
 * @see #getOperator()
 * @see #getRightExpression()
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class BinaryExpression extends Expression {

	private final Expression leftExpr;
	private final Expression rightExpr;
	private final Operator   operator;

	/**
	 * Creates a new {@link BinaryExpression}.
	 * <p>
	 * The operator {@link String} is parsed before it's stored as {@link Expression.Operator
	 * Operator}.
	 * </p>
	 */
	public BinaryExpression(Expression leftExpr, String operator, Expression rightExpr) {
		this.leftExpr  = leftExpr;
		this.rightExpr = rightExpr;
		this.operator  = this.parseOperator(operator);
	}
	
	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitBinaryExpression(this, arg);
	}

	/**
	 * Getter for the left {@link Expression}.
	 */
	public final Expression getLeftExpression() {
		return this.leftExpr;
	}

	/**
	 * Getter for the {@link Expression.Operator Operator}.
	 */
	public final Operator getOperator() {
		return this.operator;
	}

	/**
	 * Getter for the right {@link Expression}.
	 */
	public final Expression getRightExpression() {
		return this.rightExpr;
	}
	
}