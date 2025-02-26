/*
 * Copyright (c) 2025 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.visit.Copy;

/**
 * {@link GenericMethod} calling a given {@link SearchExpression}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SearchExpressionCall extends GenericMethod {

	private SearchExpression _function;

	/**
	 * Creates a new {@link SearchExpressionCall}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param function
	 *        {@link SearchExpression} to execute with the given arguments.
	 * @param arguments
	 *        See {@link #getArguments()}.
	 */
	public SearchExpressionCall(String name, SearchExpression function, SearchExpression[] arguments) {
		super(name, arguments);
		_function = function;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new SearchExpressionCall(getName(), _function.visit(Copy.INSTANCE, null), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return _function.eval(definitions, arguments);
	}

	/**
	 * Can not be evaluated at compile time because the function to execute can not be inspected.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * Not side-effect free because the function to execute can not be inspected.
	 */
	@Override
	public boolean isSideEffectFree() {
		return false;
	}

}