/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
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
	 * Creates an {@link Expression} that is true exactly when {@link #eval(TLObject, EvalContext)}
	 * yields a value that is {@link SearchExpression#isTrue(Object) true} in TL-Script, and false
	 * otherwise.
	 *
	 * <p>
	 * In contrast to {@link #buildExpression(EvalContext)}, the result is never SQL
	 * <code>UNKNOWN</code>. As top-level filter, an <code>UNKNOWN</code> condition drops the row
	 * just like <code>false</code>, and <code>and</code> and <code>or</code> preserve this
	 * equivalence. A negation does not: TL-Script negates a <code>null</code> value to
	 * <code>true</code>, whereas SQL negates <code>UNKNOWN</code> to <code>UNKNOWN</code>.
	 * Therefore, a negation must be built from this condition.
	 * </p>
	 *
	 * <p>
	 * The default implementation supports boolean values only and guards the value with
	 * {@link #buildIsNull(EvalContext)}.
	 * </p>
	 *
	 * @param context
	 *        See {@link #buildExpression(EvalContext)}.
	 * @throws CompiledValue.IncompatibleTypes
	 *         See {@link #buildExpression(EvalContext)}.
	 */
	public Expression buildCondition(EvalContext context) throws CompiledValue.IncompatibleTypes {
		if (compiledType() != MOPrimitive.BOOLEAN) {
			throw new CompiledValue.IncompatibleTypes();
		}
		Expression isNull = buildIsNull(context);
		if (ExpressionFactory.isLiteralTrue(isNull)) {
			return ExpressionFactory.literal(Boolean.FALSE);
		}
		return ExpressionFactory.and(notNull(isNull), buildExpression(context));
	}

	/**
	 * Creates an {@link Expression} that is true exactly when {@link #eval(TLObject, EvalContext)}
	 * yields <code>null</code>, and false otherwise (never SQL <code>UNKNOWN</code>).
	 *
	 * @param context
	 *        See {@link #buildExpression(EvalContext)}.
	 * @throws CompiledValue.IncompatibleTypes
	 *         See {@link #buildExpression(EvalContext)}.
	 */
	public Expression buildIsNull(EvalContext context) throws CompiledValue.IncompatibleTypes {
		return ExpressionFactory.isNull(buildExpression(context));
	}

	/**
	 * Creates an {@link Expression} to be used as operand of a comparison in the database.
	 *
	 * <p>
	 * The resulting expression represents the value of {@link #eval(TLObject, EvalContext)}
	 * whenever this value is not <code>null</code>. When it is <code>null</code>, the result is
	 * undefined; a caller must check {@link #buildIsNull(EvalContext)} for this case.
	 * </p>
	 *
	 * @param context
	 *        See {@link #buildExpression(EvalContext)}.
	 * @throws CompiledValue.IncompatibleTypes
	 *         See {@link #buildExpression(EvalContext)}.
	 */
	public Expression buildValue(EvalContext context) throws CompiledValue.IncompatibleTypes {
		return buildExpression(context);
	}

	/**
	 * The negation of the given result of {@link #buildIsNull(EvalContext)}.
	 *
	 * <p>
	 * A boolean literal is negated directly, so that the result can be simplified by
	 * {@link ExpressionFactory#and(Expression, Expression)} and
	 * {@link ExpressionFactory#or(Expression, Expression)}.
	 * </p>
	 */
	protected static Expression notNull(Expression isNull) {
		if (ExpressionFactory.isLiteralTrue(isNull)) {
			return ExpressionFactory.literal(Boolean.FALSE);
		}
		if (ExpressionFactory.isLiteralFalse(isNull)) {
			return ExpressionFactory.literal(Boolean.TRUE);
		}
		return ExpressionFactory.not(isNull);
	}

	/**
	 * Method to execute when {@link #buildExpression(EvalContext)} fails.
	 * 
	 * @param item
	 *        The item from the source would be filtered by the expression.
	 */
	public abstract Object eval(TLObject item, EvalContext context);

	/**
	 * Whether the given value is an object that is not stored in the database.
	 *
	 * <p>
	 * Such an object cannot take part in a database query: no stored row can be identical to it, and
	 * it has no table that could be used as the type of a query literal. A comparison with it must
	 * therefore be evaluated in memory.
	 * </p>
	 *
	 * @param value
	 *        The value to check, may be <code>null</code>.
	 *
	 * @see TLObject#tTransient()
	 */
	public static boolean isUnstored(Object value) {
		return value instanceof TLObject object && object.tTransient();
	}

}

