/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of {@link Iterator} that handles {@link #hasNext()} and {@link #next()}.
 * Implementor must only care about computation of next element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractIterator<E> implements Iterator<E> {

	private E _next;

	@Override
	public boolean hasNext() {
		if (_next != null) {
			return true;
		}
		_next = computeNext();
		return _next != null;
	}

	/**
	 * Computes the next result of the iterator.
	 * 
	 * @return The next value or <code>null</code> when no next value exists.
	 */
	protected abstract E computeNext();

	@Override
	public E next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		E next = _next;
		_next = null;
		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}

}

