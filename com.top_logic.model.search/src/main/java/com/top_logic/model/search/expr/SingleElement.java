/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link UnaryOperation} decomposing a singleton set into its only element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SingleElement extends UnaryOperation {

	/**
	 * Creates a {@link SingleElement}.
	 * 
	 * @param arg
	 *        See {@link #getArgument()}.
	 */
	SingleElement(SearchExpression arg) {
		super(arg);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitSingleElement(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		return compute(getArgument().evalWith(definitions, args));
	}

	/**
	 * Computes the result based on concrete values.
	 */
	public final Object compute(Object value) {
		return asSingleElement(value);
	}

}