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
import com.top_logic.model.search.expr.config.operations.TwoArgMethodBuilder;

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
	 *
	 * @param self
	 *        An expression producing a collection.
	 * @param arguments
	 *        The identity element and an associative function combining two elements.
	 */
	ReduceOperation(SearchExpression self, SearchExpression[] arguments) {
		super(REDUCE, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return SearchExpressionFactory.reduce(self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		Object result = arguments[0];
		SearchExpression fun = asSearchExpression(arguments[1]);
		for (Object input : asCollection(self)) {
			result = fun.eval(definitions, result, input);
		}
		return result;
	}

	/**
	 * Builder creating a {@link CreateObject} expression.
	 */
	public static class Builder extends TwoArgMethodBuilder<ReduceOperation> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		protected ReduceOperation internalBuild(Expr expr, SearchExpression self, SearchExpression arg1,
				SearchExpression arg2) throws ConfigurationException {
			return SearchExpressionFactory.reduce(self, arg1, arg2);
		}
	}

}
