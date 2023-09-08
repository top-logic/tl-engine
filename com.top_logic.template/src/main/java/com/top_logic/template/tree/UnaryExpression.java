/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

/**
 * Represents a unary {@link Expression} consisting of an {@link Expression.Operator Operator} and
 * an inner {@link Expression}.
 * 
 * @see #getOperator()
 * @see #getExpression()
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class UnaryExpression extends Expression {
	
	private final Operator operator;
	private final Expression expr;

	/**
	 * Creates a new {@link UnaryExpression} with the given {@link Expression.Operator Operator} and
	 * inner {@link Expression}.
	 */
	public UnaryExpression(String operator, Expression expr) {
		this.operator = this.parseOperator(operator);
		this.expr = expr;
	}

	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitUnaryExpression(this, arg);
	}

	/**
	 * Getter for the {@link Expression.Operator Operator}.
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Getter for the inner {@link Expression}.
	 */
	public Expression getExpression() {
		return expr;
	}

}
