/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} sorting {@link List}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Sort extends SearchExpression {

	private SearchExpression _list;

	private SearchExpression _comparator;

	/**
	 * Creates a {@link Sort}.
	 */
	public Sort(SearchExpression list, SearchExpression comparator) {
		_list = list;
		_comparator = comparator;
	}

	/**
	 * The expression evaluating to a {@link Collection} to be sorted.
	 */
	public SearchExpression getList() {
		return _list;
	}

	/**
	 * @see #getList()
	 */
	public void setList(SearchExpression list) {
		_list = list;
	}

	/**
	 * Function expression building a sort key for a list element.
	 * 
	 * <p>
	 * The {@link #getList()} is sorted according to the natural compare order of the values
	 * computed by the {@link #getComparator()} for the respective elements.
	 * </p>
	 */
	public SearchExpression getComparator() {
		return _comparator;
	}

	/**
	 * @see #getComparator()
	 */
	public void setComparator(SearchExpression comparator) {
		_comparator = comparator;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object result = _list.evalWith(definitions, args);
		if (result instanceof Collection<?>) {
			ArrayList<?> list = new ArrayList<>((Collection<?>) result);
			list.sort(cmp(definitions));
			return list;
		} else {
			return result;
		}
	}

	private Comparator<Object> cmp(EvalContext definitions) {
		SearchExpression comparator = _comparator;
		return new Comparator<>() {
			@Override
			public int compare(Object o1, Object o2) {
				Object result = comparator.eval(definitions, o1, o2);
				if (result == null) {
					return 0;
				}
				return ((Number) result).intValue();
			}
		};
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitSort(this, arg);
	}

}
