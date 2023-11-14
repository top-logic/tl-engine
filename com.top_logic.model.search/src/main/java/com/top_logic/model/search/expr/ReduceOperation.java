/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.ThreeArgsMethodBuilder;

/**
 * A reduce operation combining a sequence of input elements.
 * 
 * <p>
 * The method is invoked on a collection-typed expression and produces a value that is the recursive
 * combination of all elements using an associative combinator function.
 * </p>
 * 
 * <p>
 * <code>list(v1, v2, ..., vn).reduce(i, fun)</code> is equivalent to
 * <code>fun(... fun(fun(i, v1), v2)...), vn)</code>.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReduceOperation extends GenericMethod {

	private static final String REDUCE = "reduce";

	/**
	 * Creates a {@link ReduceOperation}.
	 * @param arguments
	 *        The identity element and an associative function combining two elements.
	 */
	ReduceOperation(SearchExpression[] arguments) {
		super(REDUCE, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return SearchExpressionFactory.reduce(arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object result = arguments[1];
		SearchExpression fun = asSearchExpression(arguments[2]);
		for (Object input : asCollection(arguments[0])) {
			result = fun.eval(definitions, result, input);
		}
		return result;
	}

	/**
	 * Builder creating a {@link CreateObject} expression.
	 */
	public static class Builder extends ThreeArgsMethodBuilder<ReduceOperation> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		protected ReduceOperation internalBuild(Expr expr, SearchExpression arg0, SearchExpression arg1,
				SearchExpression arg2, SearchExpression[] allArgs) throws ConfigurationException {
			return SearchExpressionFactory.reduce(allArgs);
		}
	}

}
