/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.Map;

/**
 * A {@link FunctionStatement} that represents the definition of a variable.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class DefineStatement extends FunctionStatement {

	private final String              variable;
	private final Expression          expression;
	private final Map<String, String> attributes;

	/**
	 * Creates a new {@link DefineStatement}.
	 */
	public DefineStatement(String varName, Expression expression, Map < String, String > attributes) {
		this.variable   = varName;
		this.expression = expression;
		this.attributes = attributes;
	}
	
	@Override
	public Map<String, String> getAttributes() {
		return this.attributes;
	}

	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitDefineStatement(this, arg);
	}


	/**
	 * the name of the variable that will be used for the assignments.
	 */
	public final String getVariable() {
		return variable;
	}

	/**
	 * Returns the {@link Expression} used for this ForeachStm.
	 */
	public Expression getExpression() {
		return expression;
	}
}