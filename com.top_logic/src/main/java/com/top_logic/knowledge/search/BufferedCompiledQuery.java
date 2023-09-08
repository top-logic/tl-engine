/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.SimpleCloseableIteratorAdapter;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;

/**
 * {@link CompiledQuery} that retrieves the full result at once.
 * 
 * <p>
 * The implementation of {@link #searchStream(PooledConnection, RevisionQueryArguments)} bases on
 * the implementation of {@link #search(PooledConnection, RevisionQueryArguments)} and iterates over
 * the internal result buffer.
 * </p>
 * 
 * @see StreamingCompiledQuery
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class BufferedCompiledQuery<E> extends AbstractCompiledQuery<E> {

	/**
	 * Creates a new {@link BufferedCompiledQuery}.
	 * 
	 * @param pool
	 *        see {@link AbstractCompiledQuery#AbstractCompiledQuery(ConnectionPool)}
	 */
	protected BufferedCompiledQuery(ConnectionPool pool) {
		super(pool);
	}

	@Override
	public CloseableIterator<E> searchStream(PooledConnection connection, RevisionQueryArguments arguments) {
		List<E> result = search(connection, arguments);
		return new SimpleCloseableIteratorAdapter<>(result.iterator());
	}

}

