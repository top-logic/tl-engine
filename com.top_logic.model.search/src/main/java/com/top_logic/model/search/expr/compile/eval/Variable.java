/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.basic.NamedConstant;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.db2.expr.visit.PolymorphicTypeComputation;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * Value whose {@link #buildExpression(EvalContext) expression} is a literal with an argument from
 * the {@link EvalContext}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Variable extends CompiledValue {

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
			CompiledValue otherCompiled = other.compiled();
			if (!notifyExpectedCompiledType(otherCompiled.compiledType())) {
				return new InterpretedExpression(orig);
			}
			return new CompiledEquals(this, otherCompiled);
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
		return new CompiledNot(this);
	}

	@Override
	public Value processOr(SearchExpression orig, Value other) {
		if (!notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
			return new InterpretedExpression(orig);
		}
		if (!other.hasInterpretedPart()) {
			CompiledValue otherCompiled = other.compiled();
			if (!otherCompiled.notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
				return new InterpretedExpression(orig);
			}
			return new CompiledOr(this, otherCompiled);
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAnd(SearchExpression orig, Value other) {
		if (!notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
			return new InterpretedExpression(orig);
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

		if (other.hasInterpretedPart()) {
			return new CombinedAndValue(compiledAnd, other.interpreted());
		} else {
			return compiledAnd;
		}
	}

	@Override
	public MetaObject compiledType() {
		return _type;
	}

	@Override
	public boolean notifyExpectedCompiledType(MetaObject type) {
		if (_type == MOPrimitive.INVALID_TYPE) {
			_type = type;
			return true;
		}
		return _type.isSubtypeOf(type);
	}

	@Override
	public Expression buildExpression(EvalContext context) throws CompiledValue.IncompatibleTypes {
		Object argument = value(context);
		MetaObject argumentType = PolymorphicTypeComputation.getLiteralType(argument);
		if (!KBUtils.typeSystem(context.getKnowledgeBase()).hasCommonInstances(compiledType(), argumentType)) {
			throw new CompiledValue.IncompatibleTypes();
		}
		return ExpressionFactory.literal(argument);
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return value(context);
	}

	private Object value(EvalContext context) {
		return context.getVar(key());
	}

	@Override
	public boolean needsEvalContext() {
		return true;
	}

	/**
	 * The key to get the literal value from the {@link EvalContext}.
	 */
	public NamedConstant key() {
		return _key;
	}

}

