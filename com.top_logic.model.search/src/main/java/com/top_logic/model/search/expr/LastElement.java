/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Expression retrieving the last element of a list-like value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LastElement extends AbstractListAccess {

	/**
	 * Creates a {@link LastElement}.
	 */
	protected LastElement(SearchExpression self, SearchExpression[] arguments) {
		super("lastElement", self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new LastElement(self, arguments);
	}

	@Override
	protected Object evalOnEmpty() {
		return null;
	}

	@Override
	protected Object evalOnSingleton(Object self, Object[] arguments) {
		return self;
	}

	@Override
	protected Object evalOnIterator(Iterator<?> iterator, Object[] arguments) {
		while (true) {
			Object value = iterator.next();
			if (!iterator.hasNext()) {
				return value;
			}
		}
	}

	@Override
	protected Object evalOnList(List<?> list, Object[] arguments) {
		return list.get(list.size() - 1);
	}

	/**
	 * {@link MethodBuilder} creating {@link LastElement}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<LastElement> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public LastElement build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkNoArguments(expr, self, args);
			return SearchExpressionFactory.lastElement(self);
		}

	}

}
