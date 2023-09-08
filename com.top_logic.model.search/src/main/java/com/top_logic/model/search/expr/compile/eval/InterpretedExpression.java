/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.knowledge.search.Expression;
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
	public boolean hasCompiledPart() {
		return false;
	}

	@Override
	public boolean hasInterpretedPart() {
		return true;
	}

	@Override
	public Expression compiled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SearchExpression interpreted() {
		return _orig;
	}

	@Override
	public Value processAnd(SearchExpression orig, Value other) {
		if (other.hasCompiledPart()) {
			SearchExpression interpreted;
			if (other.hasInterpretedPart()) {
				interpreted = SearchExpressionFactory.and(_orig, other.interpreted());
			} else {
				interpreted = _orig;
			}
			return new CombinedAndValue(other.compiled(), interpreted);
		}
		return super.processAnd(orig, other);
	}

}
