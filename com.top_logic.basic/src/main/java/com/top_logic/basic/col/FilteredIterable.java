/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

import com.top_logic.basic.tools.NameBuilder;

/**
 * {@link Iterable} that filters the iterator of some {@link Iterable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FilteredIterable<T> implements Iterable<T> {

	private final Filter<? super T> filter;
	private final Iterable<? extends T> source;

	/**
	 * Creates a {@link FilteredIterable}.
	 * 
	 * @param filter
	 *        The filter to apply to all elements of the given source
	 *        {@link Iterable}.
	 * @param source
	 *        The source of elements to be filtered.
	 */
	public FilteredIterable(Filter<? super T> filter, Iterable<? extends T> source) {
		this.filter = filter;
		this.source = source;
	}

	@Override
	public Iterator<T> iterator() {
		return new FilterIterator<>(source.iterator(), filter);
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("filter", filter)
			.add("source", source)
			.build();
	}

}
