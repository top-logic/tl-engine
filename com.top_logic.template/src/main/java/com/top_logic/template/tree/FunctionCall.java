/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.List;

/**
 * An {@link Expression} representing a function call.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class FunctionCall extends Expression {

	private final String name;
	private final List<Expression> arguments;

	/**
	 * Creates a new {@link FunctionCall}.
	 */
	public FunctionCall(String name, List<Expression> arg) {
		this.name = name;
		this.arguments = arg;
	}
	
	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitFunctionCall(this, arg);
	}

	/**
	 * Getter for the name of the function.
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * Getter for the arguments {@link Expression}s of the function.
	 */
	public final List<Expression> getArguments() {
		return this.arguments;
	}
	
}