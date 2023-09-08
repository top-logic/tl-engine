/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * Invocation of a function expression passing an argument.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Call extends SearchExpression {

	private SearchExpression _function;

	private SearchExpression _argument;

	/**
	 * Creates a {@link Call}.
	 * 
	 * @param function
	 *        See {@link #getFunction()}.
	 * @param argument
	 *        Seee {@link #getArgument()}.
	 */
	Call(SearchExpression function, SearchExpression argument) {
		_function = function;
		_argument = argument;
	}

	/**
	 * The function expression that is invoked.
	 */
	public SearchExpression getFunction() {
		return _function;
	}

	/**
	 * @see #getFunction()
	 */
	public void setFunction(SearchExpression function) {
		_function = function;
	}

	/**
	 * The argument that is passed to the {@link #getFunction()}.
	 */
	public SearchExpression getArgument() {
		return _argument;
	}

	/**
	 * @see #getArgument()
	 */
	public void setArgument(SearchExpression argument) {
		_argument = argument;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object funResult = getFunction().eval(definitions);
		if (!(funResult instanceof SearchExpression)) {
			// A plain value is interpreted as "constant function" returning this value.
			return funResult;
		}
		SearchExpression fun = (SearchExpression) funResult;
		Object argument = getArgument().evalWith(definitions, Args.none());
		return fun.evalWith(definitions, Args.cons(argument, args));
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitCall(this, arg);
	}

}
