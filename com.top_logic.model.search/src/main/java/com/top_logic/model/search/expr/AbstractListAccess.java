/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Iterator;
import java.util.List;

import com.top_logic.model.TLType;

/**
 * Base class for expression accessing list-like values.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractListAccess extends SimpleGenericMethod {

	/**
	 * Creates a {@link AbstractListAccess}.
	 */
	protected AbstractListAccess(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return selfType;
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		Object base = arguments[0];
		if (base == null) {
			return evalOnEmpty();
		}
		if (base instanceof List) {
			List<?> list = (List<?>) base;
			if (list.isEmpty()) {
				return evalOnEmpty();
			}
			return evalOnList(list, arguments);
		} else if (base instanceof Iterable<?>) {
			Iterator<?> iterator = ((Iterable<?>) base).iterator();
			if (!iterator.hasNext()) {
				return evalOnEmpty();
			}
			return evalOnIterator(iterator, arguments);
		} else {
			return evalOnSingleton(base, arguments);
		}
	}

	/**
	 * Result for evaluation on an empty list.
	 */
	protected abstract Object evalOnEmpty();

	/**
	 * Result for evaluation on a singleton element (not a list at all).
	 */
	protected abstract Object evalOnSingleton(Object self, Object[] arguments);

	/**
	 * Result for evaluation on an iterable value.
	 */
	protected abstract Object evalOnIterator(Iterator<?> iterator, Object[] arguments);

	/**
	 * Result for evaluation on an true list value.
	 */
	protected abstract Object evalOnList(List<?> list, Object[] arguments);

}
