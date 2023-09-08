/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} constructing a list for a given finite number of elements.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListExpr extends SearchExpression {

	private SearchExpression[] _elements;

	/**
	 * Creates a {@link ListExpr}.
	 */
	public ListExpr(SearchExpression[] elements) {
		_elements = elements;
	}

	/**
	 * The elements to build a list from.
	 */
	public SearchExpression[] getElements() {
		return _elements;
	}

	/**
	 * @see #getElements()
	 */
	public void setElements(SearchExpression[] elements) {
		_elements = elements;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		List<Object> result = new ArrayList<>(_elements.length);
		for (SearchExpression expession : _elements) {
			result.add(expession.evalWith(definitions, args));
		}
		return result;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitList(this, arg);
	}

}
