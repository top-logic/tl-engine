/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.dob.attr.MOPrimitive;
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

	private CompiledValue _compiled;

	private SearchExpression _interpreted;

	/**
	 * Creates a {@link CombinedAndValue}.
	 *
	 * @param compiled
	 *        See {@link #compiled()}.
	 * @param interpreted
	 *        See {@link #interpreted()}.
	 */
	public CombinedAndValue(CompiledValue compiled, SearchExpression interpreted) {
		_compiled = compiled;
		_interpreted = interpreted;
	}

	@Override
	public CompiledValue compiled() {
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

		CompiledValue compiledAnd;
		CompiledValue otherCompiled = other.compiled();
		if (otherCompiled != null) {
			if (!otherCompiled.notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
				return new InterpretedExpression(orig);
			}
			compiledAnd = new CompiledAnd(compiled(), otherCompiled);
		} else {
			compiledAnd = compiled();
		}
		return new CombinedAndValue(compiledAnd, interpretedAnd);
	}
}
