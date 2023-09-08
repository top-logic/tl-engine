/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

/**
 * Type-safe {@link Iterable} that returns only those elements from a source {@link Iterable} that
 * are of a certain type.
 * 
 * @see TypeMatchingIterator
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class TypeFilteredIterable<T> implements Iterable<T> {

	private final Class<T> _type;

	private final Iterable<?> _source;

	/**
	 * @see FilterUtil#filterIterable(Class, Iterable)
	 */
	TypeFilteredIterable(Class<T> type, Iterable<?> source) {
		_source = source;
		_type = type;
	}

	@Override
	public Iterator<T> iterator() {
		return FilterUtil.filterIterator(_type, _source.iterator());
	}

}
