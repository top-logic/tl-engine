/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Groups a list with key lookup function.
 * 
 * <p>
 * The result is a {@link Map} with keys computed be the key lookup function and lists as values
 * containing objects with the same key.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GroupBy extends GenericMethod {

	/**
	 * Creates a {@link GroupBy}.
	 */
	protected GroupBy(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new GroupBy(getName(), arguments);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Collection<?> source = asCollection(arguments[0]);
		SearchExpression fun = asSearchExpression(arguments[1]);

		Map<Object, List<Object>> result = new LinkedHashMap<>();
		for (Object obj : source) {
			Object key = fun.eval(definitions, obj);
			List<Object> values = result.get(key);
			if (values == null) {
				values = new ArrayList<>();
				result.put(key, values);
			}
			values.add(obj);
		}

		if (arguments.length >= 3) {
			SearchExpression mapping = asSearchExpression(arguments[2]);

			LinkedHashMap<Object, Object> mappedResult = new LinkedHashMap<>();
			for (Entry<Object, List<Object>> entry : result.entrySet()) {
				mappedResult.put(entry.getKey(), mapping.eval(definitions, entry.getValue()));
			}
			return mappedResult;
		}

		return result;
	}

	/**
	 * {@link MethodBuilder} creating {@link GroupBy}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<GroupBy> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public GroupBy build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkArgs(expr, args, 2, 3);
			return new GroupBy(getConfig().getName(), args);
		}

	}
}
