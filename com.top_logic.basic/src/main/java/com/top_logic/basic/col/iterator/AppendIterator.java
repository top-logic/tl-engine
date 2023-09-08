/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} that allows to append items via
 * {@link #append(Object)} and {@link #appendAll(Collection)}.
 * Supports {@link #remove()}
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AppendIterator<T> implements Iterator<T> {

	private final List<T> items;
	private int index = -1;
	/** Was {@link #remove()} called since the last call to {@link #next()}? */
	boolean removedLastItem = false;

	/**
	 * Creates an empty {@link AppendIterator}.
	 */
	public AppendIterator() {
		items = new ArrayList<>();
	}

	@Override
	public boolean hasNext() {
		return (index + 1) < items.size();
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		removedLastItem = false;
		index += 1;
		return items.get(index);
	}

	@Override
	public void remove() {
		if (index == -1) {
			throw new IllegalStateException("You called 'remove()' but never called 'next()'!");
		}
		if (removedLastItem) {
			throw new IllegalStateException("You already called 'remove()' since the last call to 'next()'!");
		}
		items.remove(index);
		index -= 1;
		removedLastItem = true;
	}

	/**
	 * Appends the item to the end of the underlying list.
	 * Changes the underlying collection.
	 * 
	 * @return Itself (for easier method chaining)
	 */
	public AppendIterator<T> append(T item) {
		items.add(item);
		return this;
	}

	/**
	 * Appends the items to the end of the underlying list.
	 * Changes the underlying collection.
	 * 
	 * @return Itself (for easier method chaining)
	 */
	public AppendIterator<T> appendAll(Collection<? extends T> newItems) {
		items.addAll(newItems);
		return this;
	}

	/**
	 * Returns a copy of the current underlying collection.
	 * Future changes of the underlying collection via this {@link AppendIterator}
	 * won't change that copy.
	 */
	public List<T> copyUnderlyingCollection() {
		return new ArrayList<>(items);
	}

}
