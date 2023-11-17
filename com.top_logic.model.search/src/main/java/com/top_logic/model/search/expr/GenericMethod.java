/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * Base class for custom expression implementations.
 * 
 * <p>
 * Note: Whenever possible, consider implementing {@link SimpleGenericMethod} instead to enable the
 * constant folding optimization.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GenericMethod extends SearchExpression {

	private final String _name;

	private SearchExpression[] _args;

	/**
	 * Creates a {@link GenericMethod}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param arguments
	 *        See {@link #getArguments()}
	 */
	protected GenericMethod(String name, SearchExpression[] arguments) {
		_name = name;
		_args = arguments;
	}

	/**
	 * Creates a {@link SearchExpression} implementing the same generic method but invoked with
	 * other arguments.
	 * 
	 * @param arguments
	 *        Copies of the argument expressions of the method.
	 */
	public abstract GenericMethod copy(SearchExpression[] arguments);

	/**
	 * The name of the method.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * The arguments to the method.
	 */
	public SearchExpression[] getArguments() {
		return _args;
	}

	/**
	 * @see #getArguments()
	 */
	public void setArguments(SearchExpression[] args) {
		_args = args;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitGenericMethod(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		SearchExpression[] argExprs = getArguments();
		Object[] arguments = new Object[argExprs.length];
		for (int n = 0, cnt = arguments.length; n < cnt; n++) {
			arguments[n] = eval(argExprs[n], definitions);
		}

		return eval(arguments, definitions);
	}

	private Object eval(SearchExpression expr, EvalContext definitions) {
		return expr == null ? null : expr.eval(definitions);
	}

	/**
	 * Compute the type of the method result.
	 * @param argumentTypes
	 *        The types of the arguments.
	 *
	 * @return The type of the result.
	 */
	public abstract TLType getType(List<TLType> argumentTypes);

	/**
	 * Whether the {@link #eval(Object[], EvalContext)} implementation does not modify
	 * state.
	 * 
	 * <p>
	 * A side-effect-free function can be freely re-ordered during expression compilation.
	 * </p>
	 */
	public boolean isSideEffectFree() {
		return true;
	}

	/**
	 * Whether the {@link #eval(Object[], EvalContext)} can be evaluated during expression
	 * compilation.
	 * 
	 * @implSpec An {@link GenericMethod} can be evaluated at compile time if it does not access
	 *           data that are not specified in the arguments, e.g. "calculate the length of a
	 *           {@link String}" can be evaluated at compile time because the string always has the
	 *           same length, whereas "current timestamp" cannot be evaluated at compile time
	 *           because if the expression is evaluated now , a different value will be returned
	 *           returned, than if it is evaluated tomorrow.
	 *           <p>
	 *           <b>Note:</b> Implementations returning <code>true</code> must not access the
	 *           {@link EvalContext context} in {@link #eval(Object[], EvalContext)} because there
	 *           is no such context at compile time.
	 *           </p>
	 * 
	 * @param arguments
	 *        Arguments that would be given to {@link #eval(Object[], EvalContext)}
	 */
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return true;
	}

	/**
	 * Performs the evaluation on concrete values computed from sub expressions.
	 * 
	 * @param arguments
	 *        The arguments to the method.
	 * @param definitions
	 *        See {@link SearchExpression#evalWith(EvalContext, Args)}.
	 *
	 * @return The result of the invocation.
	 */
	protected abstract Object eval(Object[] arguments, EvalContext definitions);

}
