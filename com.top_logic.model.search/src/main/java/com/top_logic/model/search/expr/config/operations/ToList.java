/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link GenericMethod} transforming the input to a {@link List}.
 * 
 * <p>
 * If the input is a collection, it is transformed into a {@link List}. Here the original is
 * returned, when is it already a {@link List}. <code>null</code> is transformed to an empty
 * {@link List}. Other objects are wrapped into a {@link List} with the object as single entry.
 * </p>
 * 
 * @see ToSet
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ToList extends SimpleGenericMethod {

	/**
	 * Creates a new {@link ToSet}.
	 */
	public ToList(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new ToList(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		List<?> result;
		if (self == null) {
			result = Collections.emptyList();
		} else if (self instanceof Collection<?>) {
			result = CollectionUtil.toList((Collection<?>) self);
		} else {
			result = Collections.singletonList(self);
		}
		return result;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link ToList}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ToList> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ToList build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkNoArguments(expr, self, args);
			return new ToList(getConfig().getName(), self, args);
		}
	}

}

