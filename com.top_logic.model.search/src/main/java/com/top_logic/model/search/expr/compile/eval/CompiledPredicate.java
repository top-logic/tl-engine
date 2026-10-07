/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.search.expr.EvalContext;

/**
 * {@link CompiledExpression} computing a boolean from other {@link CompiledValue}s in the
 * database, such as a logical operator or a comparison.
 *
 * <p>
 * A predicate is a condition in the database query, not a stored value. By default, its TL-Script
 * result is never <code>null</code>, and it is used as operand of a comparison through its
 * {@link #buildCondition(EvalContext) two-valued condition}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CompiledPredicate extends CompiledExpression {

	/**
	 * Creates a {@link CompiledPredicate}.
	 */
	public CompiledPredicate() {
		super(MOPrimitive.BOOLEAN);
	}

	@Override
	public abstract Expression buildCondition(EvalContext context) throws CompiledValue.IncompatibleTypes;

	@Override
	public Expression buildIsNull(EvalContext context) throws CompiledValue.IncompatibleTypes {
		return ExpressionFactory.literal(Boolean.FALSE);
	}

	@Override
	public Expression buildValue(EvalContext context) throws CompiledValue.IncompatibleTypes {
		return buildCondition(context);
	}

}
