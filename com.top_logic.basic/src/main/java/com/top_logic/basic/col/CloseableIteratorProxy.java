/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Proxy for another {@link CloseableIterator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CloseableIteratorProxy<T> implements CloseableIterator<T> {

	private final CloseableIterator<? extends T> _impl;

	/**
	 * Creates a new {@link CloseableIteratorProxy}.
	 * 
	 * @param impl
	 *        The {@link CloseableIterator} to dispatch to.
	 */
	public CloseableIteratorProxy(CloseableIterator<? extends T> impl) {
		_impl = impl;
	}

	@Override
	public boolean hasNext() {
		return getImpl().hasNext();
	}

	@Override
	public T next() {
		return getImpl().next();
	}

	@Override
	public void remove() {
		getImpl().remove();
	}

	@Override
	public void close() {
		getImpl().close();
	}

	/**
	 * Actual {@link CloseableIterator} to dispatch to.
	 */
	protected CloseableIterator<? extends T> getImpl() {
		return _impl;
	}

}

