/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} that creates a result set by recursively applying a function to a start
 * collection.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Recursion extends SearchExpression {

	private SearchExpression _start;

	private SearchExpression _fun;

	private SearchExpression _minDepth;

	private SearchExpression _maxDepth;

	/**
	 * @see SearchExpressions#recursion(SearchExpression, SearchExpression, SearchExpression,
	 *      SearchExpression)
	 */
	Recursion(SearchExpression start, SearchExpression fun, SearchExpression minDepth, SearchExpression maxDepth) {
		_start = start;
		_fun = fun;
		_minDepth = minDepth;
		_maxDepth = maxDepth;
	}

	/**
	 * Expression producing the start value(s) of the recursion.
	 */
	public SearchExpression getStart() {
		return _start;
	}

	/**
	 * @see #getStart()
	 */
	public void setStart(SearchExpression start) {
		_start = start;
	}

	/**
	 * Function that is applied recursively.
	 */
	public SearchExpression getFunction() {
		return _fun;
	}

	/**
	 * @see #getFunction()
	 */
	public void setFunction(SearchExpression fun) {
		_fun = fun;
	}

	/**
	 * Minimum application of the {@link #getFunction()}.
	 */
	public SearchExpression getMinDepth() {
		return _minDepth;
	}

	/**
	 * @see #getMinDepth()
	 */
	public void setMinDepth(SearchExpression minDepth) {
		_minDepth = minDepth;
	}

	/**
	 * Maximum applications of the {@link #getFunction()}.
	 */
	public SearchExpression getMaxDepth() {
		return _maxDepth;
	}

	/**
	 * @see #getMaxDepth()
	 */
	public void setMaxDepth(SearchExpression maxDepth) {
		_maxDepth = maxDepth;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		int minDepth = toNumber(_minDepth.evalWith(definitions, args)).intValue();
		int maxDepth = toNumber(_maxDepth.evalWith(definitions, args)).intValue();
		Collection<?> startCollection = toCollection(_start.evalWith(definitions, args));

		List<Object> result = new ArrayList<>();
		Set<Object> all = new HashSet<>();
		fill(result, all, args, definitions, startCollection, 0, minDepth, maxDepth);
		return result;
	}

	private void fill(Collection<Object> result, Set<Object> all, Args args, EvalContext definitions,
			Collection<?> values, int depth, int minDepth, int maxDepth) {
		boolean produce = depth >= minDepth;

		int nextDepth = depth + 1;
		boolean proceed = maxDepth < 0 || nextDepth <= maxDepth;

		for (Object value : values) {
			if (!all.add(value)) {
				// Duplicate.
				continue;
			}
			if (produce) {
				result.add(value);
			}
			if (proceed) {
				Collection<?> next = toCollection(_fun.evalWith(definitions, Args.cons(value, args)));
				fill(result, all, args, definitions, next, nextDepth, minDepth, maxDepth);
			}
		}
	}

	private static Collection<?> toCollection(Object result) {
		if (result instanceof Collection<?>) {
			return (Collection<?>) result;
		}
		return CollectionUtilShared.singletonOrEmptyList(result);
	}

	private static Number toNumber(Object result) {
		if (result == null) {
			return Integer.valueOf(0);
		}
		return (Number) result;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitRecursion(this, arg);
	}

}
