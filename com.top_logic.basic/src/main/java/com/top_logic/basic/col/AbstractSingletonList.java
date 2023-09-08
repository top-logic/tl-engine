/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link List} that contains exactly one non <code>null</code> element.
 * 
 * <p>
 * This class is only useful for memory optimization.
 * </p>
 * 
 * @see Collections#singletonList(Object) The preferable modular variant.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSingletonList<T> extends AbstractList<T> {

	@Override
	public boolean add(T o) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean contains(Object o) {
		return internalGet().equals(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		switch (c.size()) {
			case 0:
				return true;
			case 1:
				return contains(c.iterator().next());
			default:
				return false;
		}
	}

	@Override
	public T get(int index) {
		if (index != 0) {
			throw new IndexOutOfBoundsException("Singleton list.");
		}
		return internalGet();
	}

	/**
	 * Returns the singleton content of this list
	 * 
	 * @return Never <code>null</code>.
	 */
	protected abstract T internalGet();

	@Override
	public int indexOf(Object o) {
		if (internalGet().equals(o)) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public int lastIndexOf(Object o) {
		return indexOf(o);
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		if ((fromIndex == 0) && (toIndex == 1)) {
			return this;
		} else {
			return Collections.emptyList();
		}
	}

}
