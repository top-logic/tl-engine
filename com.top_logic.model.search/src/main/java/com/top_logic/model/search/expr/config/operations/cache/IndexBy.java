/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.cache;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Indexes the elements of a list with key lookup function.
 * 
 * <p>
 * In contrast to {@link GroupBy}, the resulting map only stores a single value for a certain key.
 * If no reduce function is given as second argument, the keys retrieved from the objects in the
 * source list must be unique. Otherwise, multiple objects with the same key are passed to the
 * reduce function and only the result is stored.
 * </p>
 * 
 * @see GroupBy
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IndexBy extends GenericMethod {

	/**
	 * Creates a {@link IndexBy}.
	 */
	protected IndexBy(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new IndexBy(getName(), arguments);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Collection<?> source = asCollection(arguments[0]);
		SearchExpression fun = asSearchExpression(arguments[1]);

		Map<Object, Object> result = new LinkedHashMap<>();
		if (arguments.length >= 3) {
			SearchExpression reduce = asSearchExpression(arguments[2]);

			for (Object obj : source) {
				Object key = fun.eval(definitions, obj);

				Object value;
				if (result.containsKey(key)) {
					value = reduce.eval(definitions, result.get(key), obj);
				} else {
					value = obj;
				}

				result.put(key, value);
			}
		} else {
			for (Object obj : source) {
				Object key = fun.eval(definitions, obj);

				if (result.containsKey(key)) {
					Object clash = result.get(key);

					throw new TopLogicException(
						I18NConstants.ERROR_MULTIPLE_VALUES_WITH_SAME_KEY__KEY_V1_V2_EXPR.fill(key, clash, obj, this));
				}

				result.put(key, obj);
			}
		}
		return result;
	}

	/**
	 * {@link MethodBuilder} creating {@link IndexBy}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<IndexBy> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public IndexBy build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkArgs(expr, args, 2, 3);
			return new IndexBy(getConfig().getName(), args);
		}

	}
}
