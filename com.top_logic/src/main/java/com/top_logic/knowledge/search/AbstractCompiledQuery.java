/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.CloseableIteratorAdapter;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Abstract implementation of {@link CompiledQuery} which offers some helper functions.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractCompiledQuery<E> implements CompiledQuery<E> {

	final ConnectionPool _pool;

	/**
	 * Creates an {@link AbstractCompiledQuery} with the given {@link ConnectionPool}.
	 */
	protected AbstractCompiledQuery(ConnectionPool pool) {
		_pool = pool;
	}

	@Override
	public final CloseableIterator<E> searchStream() {
		return searchStream(ExpressionFactory.revisionArgs());
	}

	@Override
	public final CloseableIterator<E> searchStream(RevisionQueryArguments arguments) {
		boolean success = false;
		final PooledConnection con = _pool.borrowReadConnection();
		try {
			final CloseableIterator<E> resultStream = searchStream(con, arguments);
			CloseableIterator<E> iterator = new CloseableIteratorAdapter<>(resultStream) {

				@Override
				protected void internalClose() {
					try {
						resultStream.close();
					} finally {
						_pool.releaseReadConnection(con);
					}
				}
			};
			success = true;
			return iterator;
		} finally {
			if (!success) {
				_pool.releaseReadConnection(con);
			}
		}
	}

	@Override
	public final List<E> search() {
		return search(ExpressionFactory.revisionArgs());
	}

	@Override
	public final List<E> search(RevisionQueryArguments arguments) {
		final PooledConnection con = _pool.borrowReadConnection();
		try {
			return search(con, arguments);
		} finally {
			_pool.releaseReadConnection(con);
		}
	}

	@Override
	public abstract CloseableIterator<E> searchStream(PooledConnection connection, RevisionQueryArguments arguments);

	@Override
	public abstract List<E> search(PooledConnection connection, RevisionQueryArguments arguments);

}

