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

/**
 * Creates an index by reducing all list entries with the same index value to a single element.
 * 
 * <p>
 * In contrast to {@link IndexBy}, an unit element is given as additional element and the reduce
 * function is called for each list element starting with the unit element.
 * </p>
 * 
 * @see IndexBy
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IndexReduce extends GenericMethod {

	/**
	 * Creates a {@link IndexReduce}.
	 */
	protected IndexReduce(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new IndexReduce(getName(), self, arguments);
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		Collection<?> source = asCollection(arguments[0]);
		SearchExpression fun = asSearchExpression(arguments[1]);
		Object unit = arguments[2];
		SearchExpression reduce = asSearchExpression(arguments[3]);

		Map<Object, Object> result = new LinkedHashMap<>();
		for (Object obj : source) {
			Object key = fun.eval(definitions, obj);

			Object existing;
			if (result.containsKey(key)) {
				existing = result.get(key);
			} else {
				existing = unit;
			}

			result.put(key, reduce.eval(definitions, existing, obj));
		}
		return result;
	}

	/**
	 * {@link MethodBuilder} creating {@link IndexReduce}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<IndexReduce> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public IndexReduce build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkArgs(expr, args, 4, 4);
			return new IndexReduce(getConfig().getName(), self, args);
		}

	}
}
