/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Proxy for another {@link CloseableIterator} that only returns elements that matches a given
 * {@link Filter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FilteredCloseableIterator<T> extends CloseableIteratorBase<T> {

	private CloseableIterator<? extends T> _baseIterator;

	private Filter<? super T> _filter;

	/**
	 * Creates a new {@link FilteredCloseableIterator}.
	 * 
	 * @param baseIterator
	 *        The base {@link CloseableIterator} to dispatch to.
	 * @param filter
	 *        Only elements matching this filter are returned.
	 */
	public FilteredCloseableIterator(CloseableIterator<? extends T> baseIterator, Filter<? super T> filter) {
		_baseIterator = baseIterator;
		_filter = filter;
	}

	@Override
	protected boolean findNext() {
		while (_baseIterator.hasNext()) {
			T entry = _baseIterator.next();
			if (_filter.accept(entry) && entry != null) {
				setNext(entry);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void internalClose() {
		_baseIterator.close();
	}

}

