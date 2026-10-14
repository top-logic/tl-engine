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
 * {@link List} that reports add and remove operations to internal hooks.
 *
 * <p>
 * An added element is reported to {@link #beforeAdd(Object)} before it is inserted, and to
 * {@link #afterAdd(Object)} after it is inserted. A removed element is reported to
 * {@link #afterRemove(Object)} after it has been removed.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObservableList<T> extends ArrayList<T> {

	@Override
	public void add(int index, T element) {
		beforeAdd(element);
		super.add(index, element);
		afterAdd(element);
	}
	
	@Override
	public boolean add(T element) {
		beforeAdd(element);
		boolean result = super.add(element);
		afterAdd(element);
		return result;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> collection) {
		List<T> added = new ArrayList<>(collection);
		beforeAdd(added);
		boolean result = super.addAll(added);
		afterAdd(added);
		return result;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> collection) {
		List<T> added = new ArrayList<>(collection);
		beforeAdd(added);
		boolean result = super.addAll(index, added);
		afterAdd(added);
		return result;
	}

	private void beforeAdd(List<T> added) {
		for (T element : added) {
			beforeAdd(element);
		}
	}

	private void afterAdd(List<T> added) {
		for (T element : added) {
			afterAdd(element);
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
	 * Hook that is called after each add to this list.
	 *
	 * <p>
	 * When a collection of elements is added, this hook is called for each element in insertion
	 * order after all elements have been inserted.
	 * </p>
	 * 
	 * @param element
	 *        The added element.
	 */
	protected void afterAdd(T element) {
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
