/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.sql.ResultSet;
import java.util.NoSuchElementException;

/**
 * {@link AbstractCloseableIterator} for adapters sources that returns the next element by calling a
 * specific method, and the result tells the caller whether the "next element" can be used.
 * 
 * <p>
 * An example for such a source is a {@link ResultSet} which allows access to
 * {@link ResultSet#next()} but the result is not the "next" of the iterator.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CloseableIteratorBase<T> extends AbstractCloseableIterator<T> {

	private T _next;

	@Override
	public boolean hasNext() {
		if (lookupNext() != null) {
			return true;
		}
	
		assert !isClosed() : "Already closed.";
		return findNext();
	}

	/**
	 * Computes the next element for the iterator.
	 * 
	 * <p>
	 * The next element must be set using {@link #setNext(Object)};
	 * </p>
	 * 
	 * @return <code>true</code> iff there is a next element.
	 * 
	 * @see #setNext(Object)
	 */
	protected abstract boolean findNext();

	/**
	 * Installs the next element to return by {@link #next()}
	 * 
	 * @param newNext
	 *        The next value returned by {@link #next()}
	 */
	protected final void setNext(T newNext) {
		assert newNext != null : "Not able to set null as next, since null is used as marker for not having called hasNext()";
		_next = newNext;
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
	
		T current = lookupNext();
		resetNext();
		return current;
	}

	/**
	 * The last result produced by {@link #findNext()}.
	 */
	protected final T lookupNext() {
		return _next;
	}

	/**
	 * Clears the last result of {@link #findNext()}.
	 */
	protected final void resetNext() {
		_next = null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

