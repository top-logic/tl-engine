/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

/**
 * Adapter of {@link Iterator} to {@link CloseableIterator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CloseableIteratorAdapter<T> extends AbstractCloseableIterator<T> {

	private final Iterator<? extends T> _base;

	/**
	 * Creates a {@link CloseableIteratorAdapter}.
	 * 
	 * @param base
	 *        The {@link Iterator} to dispatch all {@link Iterator} methods to.
	 */
	public CloseableIteratorAdapter(Iterator<? extends T> base) {
		_base = base;
	}

	@Override
	public boolean hasNext() {
		return _base.hasNext();
	}

	@Override
	public T next() {
		return _base.next();
	}

	@Override
	public void remove() {
		_base.remove();
	}
}
