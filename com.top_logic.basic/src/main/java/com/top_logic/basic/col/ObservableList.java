/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link List} that reports add and remove operations to internal
 * {@link #beforeAdd(Object)} and {@link #afterRemove(Object)} hooks.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObservableList<T> extends ArrayList<T> {

	@Override
	public void add(int index, T element) {
		beforeAdd(element);
		super.add(index, element);
	}
	
	@Override
	public boolean add(T element) {
		beforeAdd(element);
		return super.add(element);
	}
	
	@Override
	public boolean addAll(Collection<? extends T> collection) {
		beforeAdd(collection);
		return super.addAll(collection);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> collection) {
		beforeAdd(collection);
		return super.addAll(index, collection);
	}

	private void beforeAdd(Collection<? extends T> collection) {
		for (T element : collection) {
			beforeAdd(element);
		}
	}

	@Override
	public T remove(int index) {
		T removedElement = super.remove(index);
		afterRemove(removedElement);
		return removedElement;
	}
	
	@Override
	public boolean remove(Object element) {
		boolean success = super.remove(element);
		if (success) {
			@SuppressWarnings("unchecked")
			final T removedElement = (T) element;
			afterRemove(removedElement);
		}
		return success;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return removeElements(c, true);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return removeElements(c, false);
	}

	/**
	 * Deletes from this elements contained in the the given collection.
	 * 
	 * @param presentElements
	 *        Whether elements should be removed which are present in the given collection or
	 *        absent.
	 */
	private boolean removeElements(Collection<?> c, boolean presentElements) {
		boolean modified = false;
		int index = 0;
		while (index < size()) {
			T element = get(index);
			if (c.contains(element) == presentElements) {
				remove(index);
				modified = true;
			} else {
				// check next item
				index++;
			}
		}
		return modified;
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

	/**
	 * Hook that is called before each add to this list.
	 * 
	 * @param element
	 *        The added element.
	 */
	protected void beforeAdd(T element) {
		// Hook for subclasses.
	}

	/**
	 * Hook that is called after each remove from this list.
	 * 
	 * @param element
	 *        The element that was removed.
	 */
	protected void afterRemove(T element) {
		// Hook for subclasses.
	}
	
}
