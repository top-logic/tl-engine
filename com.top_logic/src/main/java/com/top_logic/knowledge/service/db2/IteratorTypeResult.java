/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Iterator;

/**
 * Adaptor to translate an {@link Iterator} of Strings to an {@link TypeResult}.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
class IteratorTypeResult implements TypeResult {

	/**
	 * The data source contacted in {@link #next()}
	 */
	private final Iterator<String> _typeNames;

	/**
	 * @see #getType()
	 */
	private String _currentType = null;

	/**
	 * Creates a new {@link IteratorTypeResult} from the given type names
	 * 
	 * @param typeNames
	 *        the data contacted in {@link #next()} and {@link #getType()}. The iterator must return
	 *        the types in the correct order.
	 * 
	 * @see #getType()
	 */
	public IteratorTypeResult(Iterator<String> typeNames) {
		_typeNames = typeNames;
	}

	/**
	 * Actually calls {@link Iterator#hasNext() hasNext} on the data {@link Iterator} and caches
	 * the value for {@link #getType()}.
	 * 
	 * In contrast to {@link Iterator#hasNext()} this method can just be called once.
	 * 
	 * @see com.top_logic.knowledge.service.db2.QueryResult#next()
	 */
	@Override
	public boolean next() {
		final boolean hasNext = _typeNames.hasNext();
		if (hasNext) {
			_currentType = _typeNames.next();
		} else {
			_currentType = null;
		}
		return hasNext;
	}

	@Override
	public void close() {
		// nothing to release here
	}

	/**
	 * Actually returns the value cached in {@link #next()}.
	 * 
	 * In contrast to {@link Iterator#next()} this method can be called more than once.
	 * 
	 * @see TypeResult#getType()
	 */
	@Override
	public String getType() {
		return _currentType;
	}

}