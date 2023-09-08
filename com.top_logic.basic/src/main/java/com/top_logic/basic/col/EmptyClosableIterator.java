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
 * {@link Closeable} {@link Iterator} that has no elements.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class EmptyClosableIterator<T> implements CloseableIterator<T> {

	/**
	 * Singleton {@link EmptyClosableIterator} instance.
	 */
	public static final EmptyClosableIterator<Object> INSTANCE = new EmptyClosableIterator<>();

	private EmptyClosableIterator() {
		// Singleton constructor.
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
	public void close() {
		// Ignore.
	}

	/**
	 * Type-safe access to {@link #INSTANCE}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> CloseableIterator<T> getInstance() {
		return (CloseableIterator<T>) INSTANCE;
	}

}
