/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * {@link CompiledExpression} representing the conjunction of two {@link CompiledValue}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledAnd extends CompiledExpression {

	private CompiledValue _left;

	private CompiledValue _right;

	/**
	 * Creates a new {@link CompiledAnd}.
	 * 
	 * @param left
	 *        See {@link #left()}.
	 * @param right
	 *        See {@link #right()}.
	 */
	public CompiledAnd(CompiledValue left, CompiledValue right) {
		super(MOPrimitive.BOOLEAN);
		_left = left;
		_right = right;
	}

	@Override
	public Expression buildExpression(EvalContext context) throws CompiledValue.IncompatibleTypes {
		return and(_left.buildExpression(context), _right.buildExpression(context));
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return SearchExpression.asBoolean(_left.eval(item, context))
			&& SearchExpression.asBoolean(_right.eval(item, context));
	}

	@Override
	public boolean needsEvalContext() {
		return _left.needsEvalContext() || _right.needsEvalContext();
	}

	/**
	 * Left part of the conjunction.
	 */
	public CompiledValue left() {
		return _left;
	}

	/**
	 * Right part of the conjunction.
	 */
	public CompiledValue right() {
		return _right;
	}

}

