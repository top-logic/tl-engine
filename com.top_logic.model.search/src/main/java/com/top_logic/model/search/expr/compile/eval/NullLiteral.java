/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.CompareOp;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * Representation of "literal(null)".
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NullLiteral extends Value {

	private SearchExpression _nullLiteral;

	/**
	 * Creates a new {@link NullLiteral}.
	 */
	public NullLiteral(SearchExpression nullLiteral) {
		_nullLiteral = nullLiteral;
	}

	@Override
	public Value processEquals(IsEqual orig, Value other) {
		if (!other.hasInterpretedPart()) {
			return new CompiledIsNull(other.compiled());
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processCompareOp(CompareOp orig, Value other) {
		// Comparison with null always leads to null
		return this;
	}

	@Override
	public Value processAccess(Access orig, TLStructuredTypePart part) {
		// Access on null always leads to null
		return this;
	}

	@Override
	public Value processNot(Not orig) {
		// Strange situation: not(literal(null))
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processOr(Or orig, Value other) {
		// Strange situation: or(xxx, literal(null))
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAnd(And orig, Value other) {
		// Strange situation: and(xxx, literal(null))
		return new InterpretedExpression(orig);
	}

	@Override
	public CompiledValue compiled() {
		// literal null can not be delegated to the KB. It must be replaced.
		return null;
	}

	@Override
	public SearchExpression interpreted() {
		return _nullLiteral;
	}

}

