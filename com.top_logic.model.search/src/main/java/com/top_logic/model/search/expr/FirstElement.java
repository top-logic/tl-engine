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
 * Expression retrieving the first element of a list-like value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FirstElement extends AbstractListAccess {

	/**
	 * Creates a {@link FirstElement}.
	 */
	protected FirstElement(SearchExpression self, SearchExpression[] arguments) {
		super("firstElement", self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new FirstElement(self, arguments);
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
		return iterator.next();
	}

	@Override
	protected Object evalOnList(List<?> list, Object[] arguments) {
		return list.get(0);
	}

	/**
	 * {@link MethodBuilder} creating {@link FirstElement}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<FirstElement> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public FirstElement build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return SearchExpressionFactory.firstElement(self);
		}

	}

}
