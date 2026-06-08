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
import com.top_logic.model.search.expr.SearchExpression;

/**
 * Value which can create an {@link Expression} to execute in the database.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CompiledValue extends Value {

	/**
	 * Indicates that creating an {@link Expression} is not possible because of incompatible types.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class IncompatibleTypes extends Exception {
		// marker class
	}

	@Override
	public CompiledValue compiled() {
		return this;
	}

	@Override
	public SearchExpression interpreted() {
		return null;
	}

	/**
	 * The database type of the {@link #compiled() compilation result}.
	 */
	public abstract MetaObject compiledType();

	/**
	 * Checks that the {@link #compiledType()} is compatible with the given {@link MetaObject} if
	 * possible.
	 * 
	 * <p>
	 * The value may adapt its own {@link #compiledType()} with respect to the argument type.
	 * </p>
	 * 
	 * @param type
	 *        The expected super type of {@link #compiledType()} for this {@link Value}.
	 * @return <code>true</code> if {@link #compiledType()} is compatible with the given type.
	 */
	public abstract boolean notifyExpectedCompiledType(MetaObject type);

	/**
	 * Whether this {@link CompiledValue} needs an {@link EvalContext evaluation context} to create
	 * the {@link Expression}
	 * 
	 * @see #buildExpression(EvalContext)
	 * 
	 * @return Whether the method {@link #buildExpression(EvalContext)} can not be called without
	 *         context.
	 */
	public abstract boolean needsEvalContext();

	/**
	 * Creates the {@link Expression} to use in the database as
	 * {@link ExpressionFactory#filter(com.top_logic.knowledge.search.SetExpression, Expression)
	 * filter}.
	 * 
	 * @param context
	 *        {@link EvalContext} to get necessary informations to create the result
	 *        {@link Expression}. The context must not be accessed when {@link #needsEvalContext()}
	 *        is <code>false</code>.
	 * @throws CompiledValue.IncompatibleTypes
	 *         iif the {@link Expression} could not be created due to incompatible types. In this
	 *         case {@link #eval(TLObject, EvalContext)} will be called.
	 */
	public abstract Expression buildExpression(EvalContext context) throws CompiledValue.IncompatibleTypes;

	/**
	 * Method to execute when {@link #buildExpression(EvalContext)} fails.
	 * 
	 * @param item
	 *        The item from the source would be filtered by the expression.
	 */
	public abstract Object eval(TLObject item, EvalContext context);

}

