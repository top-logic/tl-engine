/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.iterator;

import java.util.Iterator;

/**
 * An unmodifiable {@link Iterator}.
 * <p>
 * Forwards all calls to the inner {@link Iterator} but throws an
 * {@link UnsupportedOperationException} when {@link #remove()} is called.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class UnmodifiableIterator<E> implements Iterator<E> {

	private final Iterator<? extends E> _inner;

	/** Creates an {@link UnmodifiableIterator}. */
	public UnmodifiableIterator(Iterator<? extends E> inner) {
		_inner = inner;
	}

	@Override
	public boolean hasNext() {
		return _inner.hasNext();
	}

	@Override
	public E next() {
		return _inner.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("This is an unmodifiable collection.");
	}

}
