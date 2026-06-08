/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.EvalContext;

/**
 * {@link CompiledExpression} representing the context of an {@link Expression}.
 * 
 * @see ExpressionFactory#context()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledContext extends CompiledExpression {

	/**
	 * Creates a new {@link CompiledContext}.
	 */
	public CompiledContext(MetaObject type) {
		super(type);
	}

	@Override
	public Expression buildExpression(EvalContext context) {
		return context();
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return item;
	}

	@Override
	public boolean needsEvalContext() {
		return false;
	}

}

