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

/**
 * {@link CompiledExpression} checking a {@link CompiledValue} for being null.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledIsNull extends CompiledExpression {

	private CompiledValue _base;

	/**
	 * Creates a new {@link CompiledIsNull}.
	 */
	public CompiledIsNull(CompiledValue base) {
		super(MOPrimitive.BOOLEAN);
		_base = base;
	}

	@Override
	public Expression buildExpression(EvalContext context) throws CompiledValue.IncompatibleTypes {
		return isNull(_base.buildExpression(context));
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return _base.eval(item, context) == null;
	}

	@Override
	public boolean needsEvalContext() {
		return _base.needsEvalContext();
	}

}

