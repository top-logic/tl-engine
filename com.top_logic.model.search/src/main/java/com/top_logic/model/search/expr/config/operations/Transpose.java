/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} that transposes a collection of lists, combining elements at corresponding
 * positions.
 * 
 * <p>
 * Takes a collection containing up to 2 lists and combines their elements at corresponding
 * positions. If a transpose function is provided, it's used to combine the elements. Otherwise,
 * returns tuples of the combined elements.
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
		
		if (collection.size() > 2) {
			throw new TopLogicException(I18NConstants.ERROR_TOO_MANY_LISTS__FUN_TRANSPOSE.fill(collection.size()));
		}
		
		List<?> list1 = null;
		List<?> list2 = null;
		
		int index = 0;
		for (Object item : collection) {
			if (item instanceof Collection<?>) {
				if (index == 0) {
					list1 = asList(item);
				} else if (index == 1) {
					list2 = asList(item);
				}
				index++;
			}
		}
		
		if (list1 == null) {
			return List.of();
		}
		
		if (list2 == null) {
			list2 = List.of();
		}
		
		int maxSize = Math.max(list1.size(), list2.size());
		List<Object> result = new ArrayList<>(maxSize);
		
		if (arguments.length > 1 && arguments[1] != null) {
			SearchExpression transposeFunction = asSearchExpression(arguments[1]);
			
			for (int i = 0; i < maxSize; i++) {
				Object elem1 = i < list1.size() ? list1.get(i) : null;
				Object elem2 = i < list2.size() ? list2.get(i) : null;
				
				Object combined = transposeFunction.eval(definitions, elem1, elem2);
				result.add(combined);
			}
		} else {
			for (int i = 0; i < maxSize; i++) {
				Object elem1 = i < list1.size() ? list1.get(i) : null;
				Object elem2 = i < list2.size() ? list2.get(i) : null;
				Map<String, Object> tuple = new LinkedHashMap<>();
				tuple.put("0", elem1);
				tuple.put("1", elem2);

				result.add(tuple);
			}
		}
		
		return result;
	}


	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link Transpose}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Transpose> {

		/** Description of parameters for a {@link Transpose}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("collection")
			.optional("func")
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