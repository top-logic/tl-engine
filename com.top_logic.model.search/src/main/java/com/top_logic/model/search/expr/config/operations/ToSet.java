/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link GenericMethod} transforming the input to a {@link Set}.
 * 
 * <p>
 * If the input is a collection, it is transformed into a {@link Set}. Here the original is
 * returned, when is it already a {@link Set}. <code>null</code> is transformed to an empty
 * {@link Set}. Other objects are wrapped into a {@link Set} with the object as single entry.
 * </p>
 * 
 * @see ToList
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ToSet extends SimpleGenericMethod {

	/**
	 * Creates a new {@link ToSet}.
	 */
	public ToSet(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ToSet(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public Object eval(Object[] arguments) {
		Object base = arguments[0];
		Set<?> result;
		if (base == null) {
			result = Collections.emptySet();
		} else if (base instanceof Collection<?>) {
			result = CollectionUtil.toSet((Collection<?>) base);
		} else {
			result = Collections.singleton(base);
		}
		return result;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link ToSet}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ToSet> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ToSet build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new ToSet(getConfig().getName(), args);
		}

	}

}

