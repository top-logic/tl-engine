/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Collection} view that filters values from a source collection based on
 * their dynamic types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeFilteredCollection<T> extends AbstractCollection<T> {

	final Class<? extends T> dynamicType;
	private final Collection<? super T> base;

	/**
	 * Creates a {@link TypeFilteredCollection}.
	 * 
	 * @param base
	 *        The base collection.
	 * @param dynamicType
	 *        The type of elements that should be visible from the base
	 *        collection in this collection.
	 */
	public TypeFilteredCollection(Collection<? super T> base, Class<? extends T> dynamicType) {
		this.base = base;
		this.dynamicType = dynamicType;
	}
	
	@Override
	public boolean add(T obj) {
		return base.add(obj);
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<? super T> baseIterator = base.iterator();
		return new Iterator<>() {

			/**
			 * The next element to return, or <code>null</code>, if the current
			 * element was consumed by a call to {@link #next()}, or this
			 * iterator is exhausted.
			 */
			private T nextResult;

			{
				// Make iterator be initially aware of whether there is a next element.
				findNext();
			}
			
			private void findNext() {
				while (baseIterator.hasNext()) {
					Object nextBase = baseIterator.next();
					if (dynamicType.isInstance(nextBase)) {
						nextResult = dynamicType.cast(nextBase);
						return;
					}
				}
			}
			
			@Override
			public boolean hasNext() {
				if (nextResult != null) {
					return true;
				}
				
				findNext();
				return nextResult != null;
			}

			@Override
			public T next() {
				if (! hasNext()) {
					throw new NoSuchElementException();
				}
				T result = nextResult;

				// Re-establish the invariant.
				nextResult = null;
				
				return result;
			}

			@Override
			public void remove() {
				baseIterator.remove();
			}

		};
	}

	@Override
	public int size() {
		int size = 0;
		for (Iterator<?> it = iterator(); it.hasNext(); it.next()) {
			size++;
		}
		return size;
	}

}