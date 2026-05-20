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
 * {@link CompiledExpression} negating another {@link CompiledValue}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledNot extends CompiledExpression {

	private CompiledValue _base;

	/**
	 * Creates a new {@link CompiledNot}.
	 */
	public CompiledNot(CompiledValue base) {
		super(MOPrimitive.BOOLEAN);
		_base = base;
	}

	@Override
	public Expression buildExpression(EvalContext context) throws CompiledValue.IncompatibleTypes {
		return not(_base.buildExpression(context));
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return !SearchExpression.asBoolean(_base.eval(item, context));
	}

	@Override
	public boolean needsEvalContext() {
		return _base.needsEvalContext();
	}

}

