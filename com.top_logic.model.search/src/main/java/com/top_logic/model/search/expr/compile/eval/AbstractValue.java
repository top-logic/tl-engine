/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * Common base of {@link Value} implementations assuming that the result of all operations is an
 * {@link InterpretedExpression}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractValue extends Value {

	@Override
	public Value processEquals(SearchExpression orig, Value other) {
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAccess(SearchExpression orig, TLStructuredTypePart part) {
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processNot(SearchExpression orig) {
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processOr(SearchExpression orig, Value other) {
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAnd(SearchExpression orig, Value other) {
		return new InterpretedExpression(orig);
	}

}
