/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.Map;

/**
 * Represents the insertion / output of an expression into the result of the template expansion.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class AssignStatement extends FunctionStatement {

	private final Expression expr;
	private final Map<String, String> attributes;

	/**
	 * Creates a new {@link AssignStatement}.
	 */
	public AssignStatement(Expression expr, Map<String, String> attributes) {
		this.expr = expr;
		this.attributes = attributes;
	}
	
	@Override
	public Map<String, String> getAttributes() {
		return this.attributes;
	}

	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitAssignStatement(this, arg);
	}

	/**
	 * Getter for the {@link Expression} to be written out.
	 */
	public final Expression getExpression() {
		return expr;
	}
	
}