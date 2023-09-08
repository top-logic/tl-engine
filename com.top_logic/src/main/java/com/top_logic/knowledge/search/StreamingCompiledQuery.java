/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;

/**
 * {@link CompiledQuery} that reads its result in streaming mode.
 * 
 * <p>
 * The implementation of {@link #search(PooledConnection, RevisionQueryArguments)} is based on
 * {@link #searchStream(PooledConnection, RevisionQueryArguments)} and buffers the complete result
 * by iterating the internal streaming based result.
 * </p>
 * 
 * @see BufferedCompiledQuery
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class StreamingCompiledQuery<E> extends AbstractCompiledQuery<E> {

	/**
	 * Creates a new {@link StreamingCompiledQuery}.
	 * 
	 * @param pool
	 *        see {@link AbstractCompiledQuery#AbstractCompiledQuery(ConnectionPool)}
	 */
	protected StreamingCompiledQuery(ConnectionPool pool) {
		super(pool);
	}

	@Override
	public List<E> search(PooledConnection connection, RevisionQueryArguments arguments) {
		try (CloseableIterator<E> stream = searchStream(connection, arguments)) {
			ArrayList<E> result = new ArrayList<>();
			while (stream.hasNext()) {
				result.add(stream.next());
			}
			return result;
		}
	}

}

