/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Arrays;
import java.util.Iterator;

/**
 * The concatenation of a given number of {@link Iterable}s.
 * 
 * @see ConcatenatedIterator
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConcatenatedIterable<T> implements Iterable<T> {

	/**
	 * Concatenate all given {@link Iterable}s to a new {@link Iterable} that
	 * lazily requests iterators.
	 * 
	 * @param <T>
	 *        The element type of the {@link Iterable}s.
	 * @param entries
	 *        The iterables to concatenate.
	 * @return A concatenated {@link Iterable} that returns all elements of the
	 *         given {@link Iterable}s.
	 */
	public static <T> Iterable<T> concat(Iterable<? extends Iterable<? extends T>> entries) {
		return new ConcatenatedIterable<>(entries);
	}

	/**
	 * Concatenate all given {@link Iterable}s to a new {@link Iterable} that lazily requests
	 * iterators.
	 * 
	 * @param <T>
	 *        The element type of the {@link Iterable}s.
	 * @param entries
	 *        The iterables to concatenate.
	 * @return A concatenated {@link Iterable} that returns all elements of the given
	 *         {@link Iterable}s.
	 */
	@SafeVarargs
	public static <T> Iterable<T> concat(Iterable<? extends T>... entries) {
		return concat(Arrays.asList(entries));
	}

	private Iterable<? extends Iterable<? extends T>> _entries;

	private ConcatenatedIterable(Iterable<? extends Iterable<? extends T>> entries) {
		_entries = entries;
	}

	@Override
	public Iterator<T> iterator() {
		return new ConcatenatedIterator<>(_entries.iterator());
	}

}
