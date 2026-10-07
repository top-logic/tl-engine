/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

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
public class CompiledEquals extends CompiledPredicate {

	private final CompiledValue _left;

	private final CompiledValue _right;

	/**
	 * Creates a new {@link CompiledEquals}.
	 */
	public CompiledEquals(CompiledValue left, CompiledValue right) {
		_left = left;
		_right = right;
	}

	@Override
	public boolean needsEvalContext() {
		return _left.needsEvalContext() || _right.needsEvalContext();
	}

	/**
	 * Builds a two-valued equality test.
	 *
	 * <p>
	 * A <code>null</code> operand is tested with {@link CompiledValue#buildIsNull(EvalContext)}.
	 * Otherwise, the {@link CompiledValue#buildValue(EvalContext) values} of the operands are
	 * compared with the equality of the knowledge base, which treats <code>NULL</code> columns
	 * like TL-Script: <code>null</code> is equal to <code>null</code> only.
	 * </p>
	 */
	@Override
	public Expression buildExpression(EvalContext context) throws CompiledValue.IncompatibleTypes {
		if (_left instanceof Variable leftParam && _right instanceof Variable rightParam) {
			Object leftArg = context.getVarOrNull(leftParam.key());
			Object rightArg = context.getVarOrNull(rightParam.key());
			return ExpressionFactory.literal(isEqual(leftArg, rightArg));
		}
		Expression leftIsNull = _left.buildIsNull(context);
		if (ExpressionFactory.isLiteralTrue(leftIsNull)) {
			return _right.buildIsNull(context);
		}
		Expression rightIsNull = _right.buildIsNull(context);
		if (ExpressionFactory.isLiteralTrue(rightIsNull)) {
			return leftIsNull;
		}
		return eqBinary(_left.buildValue(context), _right.buildValue(context));
	}

	@Override
	public Expression buildCondition(EvalContext context) throws CompiledValue.IncompatibleTypes {
		return buildExpression(context);
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return isEqual(_left.eval(item, context), _right.eval(item, context));
	}

	private static boolean isEqual(Object leftVal, Object rightVal) {
		return IsEqual.equals(leftVal, rightVal);
	}

}
