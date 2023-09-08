/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} that executes a sequential sequence of subexpressions with potential
 * side-effects.
 * 
 * @see Update
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Block extends SearchExpression {

	private SearchExpression[] _contents;

	/**
	 * @see SearchExpressionFactory#block(SearchExpression...)
	 */
	Block(SearchExpression[] contents) {
		_contents = contents;
	}

	/**
	 * Sub-expressions to execute in sequence.
	 * 
	 * <p>
	 * The result of this {@link SearchExpression} is the result of the last expression in the list.
	 * </p>
	 */
	public SearchExpression[] getContents() {
		return _contents;
	}

	/**
	 * @see #getContents()
	 */
	public void setContents(SearchExpression[] contents) {
		_contents = contents;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object lastResult = null;
		for (SearchExpression content : _contents) {
			lastResult = content.eval(definitions);
		}
		return lastResult;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitBlock(this, arg);
	}

}
