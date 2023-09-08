/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * Evaluate an {@link Expression} in the context of the result of another
 * expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Eval extends ContextExpression {

	private Expression expr;

	/**
	 * Creates a {@link Eval}.
	 * 
	 * @param context
	 *        See {@link #getContext()}
	 * @param expr
	 *        See {@link #getExpr()}
	 */
	Eval(Expression context, Expression expr) {
		super(context);
		this.expr = expr;
	}
	
	/**
	 * The expression that is evaluated in the context of the result of
	 * {@link #getContext()}.
	 */
	public Expression getExpr() {
		return expr;
	}
	
	public void setExpr(Expression expr) {
		this.expr = expr;
	}
	
	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitEval(this, arg);
	}

}
