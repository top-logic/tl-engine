/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * {@link ListIterator} implementation based on an array.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ArrayListIterator<E> extends ArrayIterator<E> implements ListIterator<E> {

	/**
	 * Creates a {@link ArrayListIterator}.
	 * 
	 * @see ArrayIterator#ArrayIterator(Object[], int)
	 */
	public ArrayListIterator(E[] storage, int index) {
		super(storage, index);
	}

	@Override
	public boolean hasPrevious() {
		return _cursor > 0;
	}

	@Override
	public E previous() {
		int index = _cursor - 1;
		if (index < 0) {
			throw new NoSuchElementException();
		}
		_cursor = index;
		return _storage[index];
	}

	@Override
	public int nextIndex() {
		return _cursor;
	}

	@Override
	public int previousIndex() {
		return _cursor - 1;
	}


	@Override
	public void set(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(E e) {
		throw new UnsupportedOperationException();
	}

}
