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
 * Expression retrieving the element at a given index of a list-like value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementAt extends AbstractListAccess {

	/**
	 * Creates a {@link ElementAt}.
	 */
	protected ElementAt(SearchExpression... arguments) {
		super("elementAt", arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ElementAt(arguments);
	}

	@Override
	protected Object evalOnEmpty() {
		return null;
	}

	@Override
	protected Object evalOnSingleton(Object self, Object[] arguments) {
		int index = index(arguments);
		if (index == 0) {
			return self;
		} else {
			return null;
		}
	}

	@Override
	protected Object evalOnIterator(Iterator<?> iterator, Object[] arguments) {
		int index = index(arguments);
		if (index < 0) {
			return null;
		}
		while (true) {
			Object value = iterator.next();
			if (index == 0) {
				return value;
			}
			index--;

			if (!iterator.hasNext()) {
				return null;
			}
		}
	}

	@Override
	protected Object evalOnList(List<?> list, Object[] arguments) {
		int index = index(arguments);
		if (index < 0) {
			return null;
		}
		if (index >= list.size()) {
			return null;
		}
		return list.get(index);
	}

	private int index(Object[] arguments) {
		Object firstArg = arguments[1];
		int index;
		if (firstArg instanceof Number) {
			index = ((Number) firstArg).intValue();
		} else {
			index = -1;
		}
		return index;
	}

	/**
	 * {@link MethodBuilder} creating {@link ElementAt}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ElementAt> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ElementAt build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkTwoArgs(expr, args);
			return SearchExpressionFactory.elementAt(args);
		}

	}

}
