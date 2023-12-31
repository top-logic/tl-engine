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
public abstract class AbstractListAccess extends GenericMethod {

	/**
	 * Creates a {@link AbstractListAccess}.
	 */
	protected AbstractListAccess(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object base = arguments[0];
		if (base == null) {
			return evalOnEmpty(arguments);
		}
		if (base instanceof List) {
			List<?> list = (List<?>) base;
			if (list.isEmpty()) {
				return evalOnEmpty(arguments);
			}
			return evalOnList(list, arguments);
		} else if (base instanceof Iterable<?>) {
			Iterator<?> iterator = ((Iterable<?>) base).iterator();
			if (!iterator.hasNext()) {
				return evalOnEmpty(arguments);
			}
			return evalOnIterator(iterator, arguments);
		} else {
			return evalOnSingleton(base, arguments);
		}
	}

	/**
	 * Result for evaluation on an empty list.
	 * 
	 * @param arguments
	 *        All arguments given in {@link #eval(Object[], EvalContext)}. The first element of the
	 *        arguments is the "list like" element.
	 */
	protected abstract Object evalOnEmpty(Object[] arguments);

	/**
	 * Result for evaluation on a singleton element (not a list at all).
	 * 
	 * @param arguments
	 *        All arguments given in {@link #eval(Object[], EvalContext)}. The first element of the
	 *        arguments is the "list like" element.
	 */
	protected abstract Object evalOnSingleton(Object self, Object[] arguments);

	/**
	 * Result for evaluation on an iterable value.
	 * 
	 * @param arguments
	 *        All arguments given in {@link #eval(Object[], EvalContext)}. The first element of the
	 *        arguments is the "list like" element.
	 */
	protected abstract Object evalOnIterator(Iterator<?> iterator, Object[] arguments);

	/**
	 * Result for evaluation on an true list value.
	 * 
	 * @param arguments
	 *        All arguments given in {@link #eval(Object[], EvalContext)}. The first element of the
	 *        arguments is the "list like" element.
	 */
	protected abstract Object evalOnList(List<?> list, Object[] arguments);

}
