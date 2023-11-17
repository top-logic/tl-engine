/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

/**
 * {@link GenericMethod} that can {@link #eval(Object[]) be evaluated} without any context.
 * 
 * <p>
 * A {@link SimpleGenericMethod} take part in the constant folding optimization of the script
 * compiler.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SimpleGenericMethod extends GenericMethod {

	/**
	 * Creates a {@link SimpleGenericMethod}.
	 */
	protected SimpleGenericMethod(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	protected final Object eval(Object[] arguments, EvalContext definitions) {
		return eval(arguments);
	}

	/**
	 * Whether the {@link #eval(Object[])} can be evaluated during expression compilation.
	 * 
	 * @implSpec An {@link SimpleGenericMethod} can be evaluated at compile time if it does not
	 *           access data that are not specified in the arguments, e.g. "calculate the length of
	 *           a {@link String}" can be evaluated at compile time because the string always has
	 *           the same length, whereas "current timestamp" cannot be evaluated at compile time
	 *           because if the expression is evaluated now , a different value will be returned
	 *           returned, than if it is evaluated tomorrow.
	 * 
	 * @param arguments
	 *        Arguments that would be given to {@link #eval(Object[])}
	 */
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return true;
	}

	/**
	 * Implementation of {@link #eval(Object[], EvalContext)} without {@link EvalContext}.
	 */
	public abstract Object eval(Object[] arguments);

}
