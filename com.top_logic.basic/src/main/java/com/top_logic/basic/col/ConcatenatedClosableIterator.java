/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Iterator} that delivers the concatenation of a given sequence of
 * {@link CloseableIterator}s.
 * 
 * <p>
 * This {@link Iterator} is itself {@link Closeable} to ensure that the
 * currently active {@link CloseableIterator} delegate is closed, if this
 * {@link Iterator} is closed.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConcatenatedClosableIterator<T> extends AbstractCloseableIterator<T> {
	private final Iterator<? extends CloseableIterator<? extends T>> _iterators;

	private CloseableIterator<? extends T> _current;

	/**
	 * Creates a {@link ConcatenatedClosableIterator}.
	 * 
	 * @param iterators
	 *        Sequence of {@link CloseableIterator}s to concatenate. It is
	 *        ensured that each {@link CloseableIterator} obtained from this
	 *        sequence through a call to {@link Iterator#next()} by this
	 *        instance is {@link CloseableIterator#close() closed}, if this
	 *        {@link Iterator} is {@link #close() closed}. It is neither ensured
	 *        that the given sequence is completely consumed.
	 */
	public ConcatenatedClosableIterator(Iterator<? extends CloseableIterator<? extends T>> iterators) {
		_iterators = iterators;
		
		_current = nextIterator();
	}

	private CloseableIterator<? extends T> nextIterator() {
		if (_iterators.hasNext()) {
			return _iterators.next();
		} else {
			return EmptyClosableIterator.getInstance();
		}
	}

	@Override
	public boolean hasNext() {
		assert !isClosed() : "Already closed.";

		while (true) {
			boolean hasCurrentNext = _current.hasNext();
			if (hasCurrentNext) {
				return true;
			}

			if (_iterators.hasNext()) {
				closeCurrent();
				_current = nextIterator();
			} else {
				return false;
			}
		}
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		return _current.next();
	}

	/**
	 * Hook for subclasses to add custom functionality that is executed on {@link #close()}.
	 */
	@Override
	protected void internalClose() {
		closeCurrent();
	}

	private void closeCurrent() {
		if (_current == null) {
			/* may happen when inner iterator throws exception in next(), and this iterator is
			 * closed in finally-block. (see #11318) */
			return;
		}
		// Make sure that this is closed, even if close() returns exceptionally.
		CloseableIterator<? extends T> old = _current;
		_current = null;
		old.close();
	}

	@Override
	public void remove() {
		assert !isClosed() : "Already closed.";

		_current.remove();
	}
}