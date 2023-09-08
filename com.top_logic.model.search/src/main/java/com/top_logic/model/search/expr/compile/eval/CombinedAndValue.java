/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.knowledge.search.Expression;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.SearchExpression;

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

}
