/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.IsEqual;

/**
 * {@link CompiledExpression} representing the equality of two {@link CompiledValue}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledEquals extends CompiledExpression {

	private final CompiledValue _left;

	private final CompiledValue _right;

	/**
	 * Creates a new {@link CompiledEquals}.
	 */
	public CompiledEquals(CompiledValue left, CompiledValue right) {
		super(MOPrimitive.BOOLEAN);
		_left = left;
		_right = right;
	}

	@Override
	public boolean needsEvalContext() {
		return _left.needsEvalContext() || _right.needsEvalContext();
	}

	@Override
	public Expression buildExpression(EvalContext context) throws CompiledValue.IncompatibleTypes {
		if (_left instanceof Variable leftParam) {
			Object leftArg = context.getVarOrNull(leftParam.key());
			if (_right instanceof Variable rightParam) {
				Object rightArg = context.getVarOrNull(rightParam.key());
				return ExpressionFactory.literal(isEqual(leftArg, rightArg));
			} else if (leftArg == null) {
				return isNull(_right.buildExpression(context));
			}
		} else if (_right instanceof Variable rightParam) {
			Object rightArg = context.getVarOrNull(rightParam.key());
			if (rightArg == null) {
				return isNull(_left.buildExpression(context));
			}
		}
		return eqBinary(_left.buildExpression(context), _right.buildExpression(context));
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return isEqual(_left.eval(item, context), _right.eval(item, context));
	}

	private static boolean isEqual(Object leftVal, Object rightVal) {
		return IsEqual.equals(leftVal, rightVal);
	}

}
