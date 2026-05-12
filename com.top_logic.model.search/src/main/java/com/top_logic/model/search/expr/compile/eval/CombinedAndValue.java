/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;

/**
 * Special {@link Value} that represents an {@link And} operation where one part can be
 * {@link #compiled()} and the other must be {@link #interpreted()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CombinedAndValue extends AbstractValue {

	private Expression _compiled;

	private SearchExpression _interpreted;

	/**
	 * Creates a {@link CombinedAndValue}.
	 *
	 * @param compiled
	 *        See {@link #compiled()}.
	 * @param interpreted
	 *        See {@link #interpreted()}.
	 */
	public CombinedAndValue(Expression compiled, SearchExpression interpreted) {
		_compiled = compiled;
		_interpreted = interpreted;
	}

	@Override
	public boolean hasCompiledPart() {
		return true;
	}

	@Override
	public boolean hasInterpretedPart() {
		return true;
	}

	@Override
	public Expression compiled() {
		return _compiled;
	}

	@Override
	public SearchExpression interpreted() {
		return _interpreted;
	}

	@Override
	public Value processAnd(SearchExpression orig, Value other) {
		SearchExpression interpretedAnd;
		if (other.hasInterpretedPart()) {
			interpretedAnd = SearchExpressionFactory.and(interpreted(), other.interpreted());
		} else {
			interpretedAnd = interpreted();
		}

		Expression compiledAnd;
		if (other.hasCompiledPart()) {
			compiledAnd = ExpressionFactory.and(compiled(), other.compiled());
		} else {
			compiledAnd = compiled();
		}
		return new CombinedAndValue(compiledAnd, interpretedAnd);
	}
}
