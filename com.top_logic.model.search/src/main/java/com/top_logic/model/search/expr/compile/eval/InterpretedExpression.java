/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;

/**
 * {@link Value} that must be interpreted in memory.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InterpretedExpression extends AbstractValue {

	private SearchExpression _orig;

	/**
	 * Creates a {@link InterpretedExpression}.
	 *
	 * @param orig
	 *        See {@link #interpreted()}.
	 */
	public InterpretedExpression(SearchExpression orig) {
		_orig = orig;
	}

	@Override
	public CompiledValue compiled() {
		return null;
	}

	@Override
	public SearchExpression interpreted() {
		return _orig;
	}

	@Override
	public Value processAnd(SearchExpression orig, Value other) {
		SearchExpression interpretedAnd;
		if (other.hasInterpretedPart()) {
			interpretedAnd = SearchExpressionFactory.and(interpreted(), other.interpreted());
		} else {
			interpretedAnd = interpreted();
		}

		CompiledValue otherCompiled = other.compiled();
		if (otherCompiled != null) {
			return new CombinedAndValue(otherCompiled, interpretedAnd);
		} else {
			return new InterpretedExpression(interpretedAnd);
		}
	}

}
