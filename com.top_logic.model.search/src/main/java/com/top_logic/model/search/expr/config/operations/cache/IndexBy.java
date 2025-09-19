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
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;


/**
 * Indexes the elements of a list with key lookup function.
 * 
 * <p>
 * In contrast to {@link GroupBy}, the resulting map only stores a single value for a certain key.
 * If no reduce function is given as third argument, the keys retrieved from the objects in the
 * source list must be unique. Otherwise, multiple objects with the same key are passed to the
 * reduce function and only the result is stored.
 * </p>
 * 
 * <p>
 * An optional mapping function can be provided as fourth argument to transform values before
 * storing them in the result map. If no mapping function is given, the original objects are stored.
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
		SearchExpression keyFun = asSearchExpression(arguments[1]);

		SearchExpression clashFun = null;
		if (arguments.length >= 3 && arguments[2] != null) {
			clashFun = asSearchExpression(arguments[2]);
		}

		SearchExpression mapFun = null;
		if (arguments.length >= 4 && arguments[3] != null) {
			mapFun = asSearchExpression(arguments[3]);
		}

		Map<Object, Object> result = new LinkedHashMap<>();

		for (Object obj : source) {
			Object key = keyFun.eval(definitions, obj);

			// Apply mapping function if provided
			Object valueToStore = (mapFun != null) ? mapFun.eval(definitions, obj) : obj;

			if (result.containsKey(key)) {
				if (clashFun != null) {
					// Resolve clash using clashFun
					Object existingValue = result.get(key);
					Object resolvedValue = clashFun.eval(definitions, existingValue, valueToStore);
					result.put(key, resolvedValue);
				} else {
					// No clash resolution provided - throw error
					throw new TopLogicException(
						I18NConstants.ERROR_MULTIPLE_VALUES_WITH_SAME_KEY__KEY_V1_V2_EXPR.fill(key, result.get(key),
							valueToStore, this));
				}
			} else {
				// No clash - store the value
				result.put(key, valueToStore);
			}
		}

		return result;
	}

	/**
	 * {@link MethodBuilder} creating {@link IndexBy} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<IndexBy> {

		/**
		 * Argument descriptor for the indexBy() function: - mandatory "collection": The input
		 * collection to index - mandatory "keyFun": The function to extract keys from elements -
		 * optional "clashFun": Function to resolve key conflicts (2 parameters) - optional
		 * "mapFun": Function to transform values before storing (1 parameter)
		 */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("collection")
			.mandatory("keyFun")
			.optional("clashFun")
			.optional("mapFun")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public IndexBy build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			checkArgs(expr, args, 2, 4);
			return new IndexBy(getConfig().getName(), args);
		}

	}

}