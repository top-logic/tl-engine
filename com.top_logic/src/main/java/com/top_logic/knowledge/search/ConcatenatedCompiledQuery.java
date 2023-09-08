/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.ConcatenatedClosableIterator;
import com.top_logic.basic.shared.collection.map.MappedIterator;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;

/**
 * {@link CompiledQuery} that executes a sequence of other {@link CompiledQuery}s.
 * 
 * <p>
 * The resulting {@link CloseableIterator} is a concatenation of the resulting
 * {@link CloseableIterator} of the wrapped {@link CompiledQuery}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConcatenatedCompiledQuery<E> extends AbstractCompiledQuery<E> {
	
	private final Iterable<? extends CompiledQuery<? extends E>> _sources;

	/**
	 * Creates a {@link ConcatenatedCompiledQuery} based on the given
	 * {@link CompiledQuery}.
	 * 
	 * @param pool
	 *        the {@link ConnectionPool} to execute service methods. Must be the same as in source
	 *        {@link CompiledQuery}s.
	 * @param sources
	 *        the {@link CompiledQuery compiled queries} this {@link ConcatenatedCompiledQuery} based
	 *        on.
	 */
	public ConcatenatedCompiledQuery(ConnectionPool pool, Iterable<? extends CompiledQuery<? extends E>> sources) {
		super(pool);
		_sources = sources;
	}

	@Override
	public CloseableIterator<E> searchStream(PooledConnection connection, RevisionQueryArguments arguments) {
		Iterator<CloseableIterator<? extends E>> iterators =
			new CompiledQueryIterator<>(_sources.iterator(), connection, arguments);
		return new ConcatenatedClosableIterator<>(iterators);
	}

	@Override
	public List<E> search(PooledConnection connection, RevisionQueryArguments arguments) {
		ArrayList<E> result = new ArrayList<>();
		for (CompiledQuery<? extends E> innerQuery : _sources) {
			List<? extends E> innerResult = innerQuery.search(connection, arguments);
			result.addAll(innerResult);
		}
		return result;
	}

	/**
	 * Iterator that transforms an iterator of {@link CompiledQuery} into an iterator of
	 * corresponding {@link CloseableIterator}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class CompiledQueryIterator<E> extends
			MappedIterator<CompiledQuery<? extends E>, CloseableIterator<? extends E>> {

		private final PooledConnection _connection;

		private final RevisionQueryArguments _arguments;

		CompiledQueryIterator(Iterator<? extends CompiledQuery<? extends E>> source,
				PooledConnection connection, RevisionQueryArguments arguments) {
			super(source);
			_connection = connection;
			_arguments = arguments;
		}

		@Override
		protected CloseableIterator<? extends E> map(CompiledQuery<? extends E> input) {
			return input.searchStream(_connection, _arguments);
		}
	}

}

