/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Iterator} based on a given array.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ArrayIterator<E> implements Iterator<E> {

	final E[] _storage;
	int _cursor = 0;

	/**
	 * Creates a {@link ArrayIterator}.
	 * 
	 * @param storage
	 *        Base array to resolve elements.
	 * @param index
	 *        index of the element in the storage that is returned during first call of
	 *        {@link #next()}.
	 */
	public ArrayIterator(E[] storage, int index) {
		if (index < 0 || index >= storage.length) {
			throw new IndexOutOfBoundsException(index + " must be >= 0 and < " + storage.length);
		}
		_storage = storage;
		_cursor = index;
	}

	@Override
	public boolean hasNext() {
		return _cursor < _storage.length;
	}

	@Override
	public E next() {
		int index = _cursor;
		if (index >= _storage.length) {
			throw new NoSuchElementException();
		}
		_cursor = index + 1;
		return _storage[index];
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
