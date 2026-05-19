/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import java.util.function.Function;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.transform.FilterCompiler.Parameters;

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
	public Value processEquals(SearchExpression orig, Value other) {
		if (!other.hasInterpretedPart()) {
			return new CompiledExpression(MOPrimitive.BOOLEAN,
				params -> ExpressionFactory.isNull(other.compiled().apply(params)));
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAccess(SearchExpression orig, TLStructuredTypePart part) {
		// Access on null always leads to null
		return this;
	}

	@Override
	public Value processNot(SearchExpression orig) {
		// Strange situation: not(literal(null))
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processOr(SearchExpression orig, Value other) {
		// Strange situation: or(xxx, literal(null))
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAnd(SearchExpression orig, Value other) {
		// Strange situation: and(xxx, literal(null))
		return new InterpretedExpression(orig);
	}

	@Override
	public boolean hasCompiledPart() {
		// literal null can not be delegated to the KB. It must be replaced.
		return false;
	}

	@Override
	public Function<Parameters, Expression> compiled() {
		throw new UnsupportedOperationException("Not compiled: " + this);
	}

	@Override
	public MetaObject compiledType() {
		throw new UnsupportedOperationException("Not compiled: " + this);
	}

	@Override
	public boolean notifyExpectedCompiledType(MetaObject type) {
		throw new UnsupportedOperationException("Not compiled: " + this);
	}

	@Override
	public boolean hasInterpretedPart() {
		return true;
	}

	@Override
	public SearchExpression interpreted() {
		return _nullLiteral;
	}

}

