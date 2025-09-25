/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} that transposes a collection of lists, combining elements at corresponding
 * positions.
 * 
 * <p>
 * Takes a collection containing any number of lists and combines their elements at corresponding
 * positions. If a transpose function is provided, it's used to combine the elements. Otherwise,
 * returns tuples (as lists) of the combined elements.
 * </p>
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class Transpose extends GenericMethod {

	/**
	 * Creates a {@link Transpose}.
	 */
	protected Transpose(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Transpose(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Collection<?> collection = asCollection(arguments[0]);

		// Convert all elements in the collection to lists
		List<List<?>> innerLists = new ArrayList<>();
		for (Object item : collection) {
			innerLists.add(asList(item));
		}

		if (innerLists.isEmpty()) {
			return Collections.emptyList();
		}

		// Find the maximum length among all inner lists
		int maxSize = 0;
		for (List<?> innerList : innerLists) {
			if (innerList.size() > maxSize) {
				maxSize = innerList.size();
			}
		}

		// Handle with transpose function
		if (arguments.length > 1 && arguments[1] != null) {
			SearchExpression transposeFunction = asSearchExpression(arguments[1]);
			List<Object> result = new ArrayList<>(maxSize);

			for (int i = 0; i < maxSize; i++) {
				Object[] args = new Object[innerLists.size()];
				for (int j = 0; j < innerLists.size(); j++) {
					List<?> innerList = innerLists.get(j);
					args[j] = i < innerList.size() ? innerList.get(i) : null;
				}
				Object combined = transposeFunction.eval(definitions, args);
				result.add(combined);
			}

			return result;
		}
		// Handle without transpose function (return tuples as lists)
		else {
			List<List<Object>> result = new ArrayList<>(maxSize);

			for (int i = 0; i < maxSize; i++) {
				List<Object> tuple = new ArrayList<>(innerLists.size());
				for (List<?> innerList : innerLists) {
					tuple.add(i < innerList.size() ? innerList.get(i) : null);
				}
				result.add(tuple);
			}

			return result;
		}
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link Transpose}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Transpose> {

		/** Description of parameters for a {@link Transpose}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("collection")
			.optional("fun")
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
		public Transpose build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Transpose(getConfig().getName(), args);
		}
	}
}