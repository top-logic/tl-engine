/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import java.util.function.Function;

import com.top_logic.basic.NamedConstant;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.transform.FilterCompiler.Parameters;

/**
 * Value whose {@link #compiled()} is filled during execution with an argument from the
 * {@link EvalContext}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Variable extends Value {

	private MetaObject _type = MOPrimitive.INVALID_TYPE;

	private final NamedConstant _key;

	/**
	 * Creates a new {@link Variable}.
	 * 
	 * @param key
	 *        {@link NamedConstant} to fetch the actual value as database argument from the
	 *        {@link EvalContext}.
	 */
	public Variable(NamedConstant key) {
		_key = key;
	}

	@Override
	public Value processEquals(SearchExpression orig, Value other) {
		if (!other.hasInterpretedPart()) {
			if (!notifyExpectedCompiledType(other.compiledType())) {
				return new InterpretedExpression(orig);
			}
			return new CompiledExpression(MOPrimitive.BOOLEAN,
				params -> ExpressionFactory.eqBinary(compiled().apply(params), other.compiled().apply(params)));
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAccess(SearchExpression orig, TLStructuredTypePart part) {
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processNot(SearchExpression orig) {
		if (!notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
			return new InterpretedExpression(orig);
		}
		return new CompiledExpression(MOPrimitive.BOOLEAN, params -> ExpressionFactory.not(compiled().apply(params)));
	}

	@Override
	public Value processOr(SearchExpression orig, Value other) {
		if (!notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
			return new InterpretedExpression(orig);
		}
		if (!other.hasInterpretedPart()) {
			if (!other.notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
				return new InterpretedExpression(orig);
			}
			return new CompiledExpression(MOPrimitive.BOOLEAN,
				params -> ExpressionFactory.or(compiled().apply(params), other.compiled().apply(params)));
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAnd(SearchExpression orig, Value other) {
		if (!notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
			return new InterpretedExpression(orig);
		}
		Function<Parameters, Expression> compiledAnd;
		if (other.hasCompiledPart()) {
			if (!other.notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
				return new InterpretedExpression(orig);
			}
			compiledAnd = params -> ExpressionFactory.and(compiled().apply(params), other.compiled().apply(params));
		} else {
			compiledAnd = compiled();
		}

		if (other.hasInterpretedPart()) {
			return new CombinedAndValue(compiledAnd, other.interpreted());
		} else {
			return new CompiledExpression(MOPrimitive.BOOLEAN, compiledAnd);
		}
	}

	@Override
	public boolean hasCompiledPart() {
		return true;
	}

	@Override
	public Function<Parameters, Expression> compiled() {
		return params -> {
			MetaObject compiledType = compiledType();
			if (compiledType == MOPrimitive.INVALID_TYPE) {
				throw new IllegalStateException("Correct type for variable not yet set. Key: " + _key);
			}
			return ExpressionFactory.param(params.getParameterName(compiledType, _key));
		};
	}

	@Override
	public MetaObject compiledType() {
		return _type;
	}

	@Override
	public boolean hasInterpretedPart() {
		return false;
	}

	@Override
	public SearchExpression interpreted() {
		return null;
	}

	@Override
	public boolean notifyExpectedCompiledType(MetaObject type) {
		if (_type == MOPrimitive.INVALID_TYPE) {
			_type = type;
			return true;
		}
		return _type.isSubtypeOf(type);
	}

}

