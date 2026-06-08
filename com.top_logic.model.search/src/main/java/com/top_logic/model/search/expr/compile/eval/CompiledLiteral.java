/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.EvalContext;

/**
 * {@link CompiledExpression} representing a {@link ExpressionFactory#literal(Object)}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledLiteral extends CompiledExpression {

	private Object _literal;

	/**
	 * Creates a new {@link CompiledLiteral}.
	 * 
	 * @param type
	 *        Type of the literal.
	 * @param literal
	 *        The literal value.
	 */
	public CompiledLiteral(MetaObject type, Object literal) {
		super(type);
		_literal = literal;
	}

	@Override
	public Expression buildExpression(EvalContext context) {
		return ExpressionFactory.literal(_literal);
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return _literal;
	}

	@Override
	public boolean needsEvalContext() {
		return false;
	}

}

