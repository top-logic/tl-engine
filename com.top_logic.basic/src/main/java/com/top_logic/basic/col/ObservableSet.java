/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link Set} that informs internal {@link #beforeAdd(Object)} and
 * {@link #afterRemove(Object)} hooks.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObservableSet<T> extends HashSet<T> {

	@Override
	public boolean add(T element) {
		beforeAdd(element);
		return super.add(element);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object element) {
		boolean success = super.remove(element);
		if (success) {
			afterRemove((T) element);
		}
		return success;
	}

	@Override
	public void clear() {
		Object[] buffer = toArray();

		super.clear();

		for (Object element : buffer) {
			@SuppressWarnings("unchecked")
			final T removedElement = (T) element;
			afterRemove(removedElement);
		}
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<T> baseIterator = super.iterator();
		return new Iterator<>() {

			private T _lastValue;

			@Override
			public boolean hasNext() {
				return baseIterator.hasNext();
			}

			@Override
			public T next() {
				_lastValue = baseIterator.next();
				return _lastValue;
			}

			@Override
			public void remove() {
				baseIterator.remove();
				ObservableSet.this.afterRemove(_lastValue);
			}

		};
	}

	/**
	 * Hook that is called before each add operation.
	 * 
	 * @param element
	 *        The element being added to this set.
	 */
	protected void beforeAdd(T element) {
		// Hook for subclasses.
	}

	/**
	 * Hook that is called after each remove operation.
	 * 
	 * @param element
	 *        The element that was removed from this set.
	 */
	protected void afterRemove(T element) {
		// Hook for subclasses.
	}
	
}
