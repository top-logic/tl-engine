/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} applying a {@link #getFunction()} to each element of a
 * {@link #getBase()} collection.
 * 
 * <p>
 * The result is the collection of evaluation results of the {@link #getFunction()} on each element
 * of the {@link #getBase()} collection.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Foreach extends SearchExpression {

	private SearchExpression _base;

	private SearchExpression _function;

	/**
	 * Creates a {@link Foreach}.
	 * 
	 * @param base
	 *        See {@link #getBase()}.
	 * @param function
	 *        See {@link #getFunction()}.
	 */
	Foreach(SearchExpression base, SearchExpression function) {
		_base = base;
		_function = function;
	}

	/**
	 * The input collection.
	 */
	public SearchExpression getBase() {
		return _base;
	}

	/**
	 * @see #getBase()
	 */
	public void setBase(SearchExpression base) {
		_base = base;
	}

	/**
	 * The function to apply to each element of {@link #getBase()}.
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

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitForeach(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Collection<?> base = asCollection(getBase().evalWith(definitions, args));
		List<Object> result = new ArrayList<>(base.size());
		for (Object element : base) {
			result.add(getFunction().evalWith(definitions, Args.some(element)));
		}
		return result;
	}

}