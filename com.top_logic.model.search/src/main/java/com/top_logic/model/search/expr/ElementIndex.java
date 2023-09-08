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
import com.top_logic.basic.util.Utils;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Expression retrieving the element at a given index of a list-like value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementIndex extends AbstractListAccess {

	/**
	 * Creates a {@link ElementIndex}.
	 */
	protected ElementIndex(SearchExpression self, SearchExpression... arguments) {
		super("elementIndex", self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new ElementIndex(self, arguments);
	}

	@Override
	protected Object evalOnEmpty() {
		return -1;
	}

	@Override
	protected Object evalOnSingleton(Object self, Object[] arguments) {
		if (Utils.equals(self, element(arguments))) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	protected Object evalOnIterator(Iterator<?> iterator, Object[] arguments) {
		Object element = element(arguments);

		int index = 0;
		while (true) {
			Object value = iterator.next();
			if (Utils.equals(element, value)) {
				return index;
			}
			index++;

			if (!iterator.hasNext()) {
				return -1;
			}
		}
	}

	@Override
	protected Object evalOnList(List<?> list, Object[] arguments) {
		return list.indexOf(element(arguments));
	}

	private Object element(Object[] arguments) {
		return arguments[0];
	}

	/**
	 * {@link MethodBuilder} creating {@link ElementIndex}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ElementIndex> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ElementIndex build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return SearchExpressionFactory.elementIndex(self, args[0]);
		}

	}

}
