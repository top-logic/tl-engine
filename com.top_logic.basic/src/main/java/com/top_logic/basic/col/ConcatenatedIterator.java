/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Iterator} that returns the entries of a sequence of {@link Iterator} in the given order.
 * 
 * @see ConcatenatedIterable
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConcatenatedIterator<T> implements Iterator<T> {

	private final Iterator<? extends Iterable<? extends T>> _entries;

	private Iterator<? extends T> _currentIt;

	/**
	 * Creates a new {@link ConcatenatedIterator}.
	 * 
	 * @param entries
	 *        The {@link Iterator}s to iterate through.
	 */
	public ConcatenatedIterator(Iterator<? extends Iterable<? extends T>> entries) {
		_entries = entries;
		_currentIt = entries.hasNext() ? entries.next().iterator() : Collections.emptyIterator();
	}

	@Override
	public boolean hasNext() {
		while (true) {
			if (_currentIt.hasNext()) {
				return true;
			}
			
			if (_entries.hasNext()) {
				_currentIt = _entries.next().iterator();
			} else {
				return false;
			}
		}
	}

	@Override
	public T next() {
		if (! hasNext()) {
			throw new NoSuchElementException("No more elements.");
		}
		
		return _currentIt.next();
	}

	@Override
	public void remove() {
		_currentIt.remove();
	}
}
