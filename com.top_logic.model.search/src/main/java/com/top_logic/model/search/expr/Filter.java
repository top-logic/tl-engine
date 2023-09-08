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
 * {@link SearchExpression} filtering a {@link #getBase() collection} based on a boolean
 * {@link #getFunction() decision function}.
 * 
 * <p>
 * The result of a {@link Filter} is the collection that only contains those elements from the
 * {@link #getBase()} collection that match the filter {@link #getFunction()}. Conceptually, the
 * filter {@link #getFunction()} is evaluated for each element of the {@link #getBase()} collection
 * by passing the element as argument. The element is added to the result set, if the function
 * {@link #getFunction()} evaluates to <code>true</code>.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Filter extends SearchExpression {
	private SearchExpression _base;

	private SearchExpression _function;

	/**
	 * Creates a {@link Filter}.
	 * 
	 * @param base
	 *        See {@link #getBase()}.
	 * @param function
	 *        See {@link #getFunction()}.
	 */
	Filter(SearchExpression base, SearchExpression function) {
		_base = base;
		_function = function;
	}

	/**
	 * The base collection whose elements are filtered.
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
	 * A {@link Boolean} {@link Lambda function} expression that is evaluated for each element of
	 * the {@link #getBase()} collection by passing it as argument.
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
		return visitor.visitFilter(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Collection<?> base = asCollection(_base.evalWith(definitions, args));
		List<Object> result = new ArrayList<>();
		for (Object value : base) {
			boolean decision = isTrue(_function.evalWith(definitions, Args.some(value)));
			if (decision) {
				result.add(value);
			}
		}
		return result;
	}
}