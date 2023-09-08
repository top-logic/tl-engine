/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.EmptyClosableIterator;
import com.top_logic.basic.sql.PooledConnection;

/**
 * {@link CompiledQuery} which creates empty {@link CloseableIterator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EmptyCompiledQuery<E> implements CompiledQuery<E> {

	/**
	 * Singleton {@link EmptyCompiledQuery} instance.
	 */
	public static final EmptyCompiledQuery<Object> INSTANCE = new EmptyCompiledQuery<>();

	private EmptyCompiledQuery() {
		// singleton instance
	}

	@Override
	public CloseableIterator<E> searchStream(PooledConnection connection, RevisionQueryArguments arguments) {
		return EmptyClosableIterator.getInstance();
	}

	@Override
	public List<E> search(PooledConnection connection, RevisionQueryArguments arguments) {
		return Collections.emptyList();
	}

	@Override
	public CloseableIterator<E> searchStream(RevisionQueryArguments arguments) {
		return EmptyClosableIterator.getInstance();
	}

	@Override
	public List<E> search(RevisionQueryArguments arguments) {
		return Collections.emptyList();
	}

	@Override
	public CloseableIterator<E> searchStream() {
		return EmptyClosableIterator.getInstance();
	}

	@Override
	public List<E> search() {
		return Collections.emptyList();
	}

	/**
	 * Type-safe access to {@link #INSTANCE}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> CompiledQuery<T> getInstance() {
		return (CompiledQuery<T>) INSTANCE;
	}

}

