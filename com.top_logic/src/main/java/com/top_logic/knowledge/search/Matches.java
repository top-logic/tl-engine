/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link Expression} that matches a regular expression on a certain {@link #getExpr() value}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Matches extends Expression {

	private final String regex;
	private Expression expr;

	Matches(String regex, Expression expr) {
		this.regex = regex;
		this.expr = expr;
	}
	
	public String getRegex() {
		return regex;
	}
	
	public Expression getExpr() {
		return expr;
	}
	
	public void setExpr(Expression expr) {
		this.expr = expr;
	}
	
	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitMatches(this, arg);
	}

}
