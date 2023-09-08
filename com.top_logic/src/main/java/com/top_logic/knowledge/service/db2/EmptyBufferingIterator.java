/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * {@link BufferingCloseableIterator} that has no entries.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class EmptyBufferingIterator<T> implements BufferingCloseableIterator<T> {

	/**
	 * Singleton {@link EmptyBufferingIterator} instance.
	 */
	public static final EmptyBufferingIterator<Object> INSTANCE = new EmptyBufferingIterator<>();

	private EmptyBufferingIterator() {
		// Singleton constructor.
	}

	@Override
	public void close() {
		// Ignore.
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> getAll() {
		return new ArrayList<>();
	}

	/**
	 * Singleton {@link EmptyBufferingIterator} instance.
	 */
	@SuppressWarnings("unchecked")
	public static <T> BufferingCloseableIterator<T> getInstance() {
		return (BufferingCloseableIterator<T>) INSTANCE;
	}

}
