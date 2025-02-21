/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.ConcatenatedClosableIterator;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.basic.shared.collection.map.MappedIterator;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.model.TLObject;

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

	private Comparator<Object> _order;

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
		this(pool, sources, ExpressionFactory.NO_ORDER);
	}

	/**
	 * Creates a {@link ConcatenatedCompiledQuery} based on the given {@link CompiledQuery}.
	 * 
	 * @param pool
	 *        the {@link ConnectionPool} to execute service methods. Must be the same as in source
	 *        {@link CompiledQuery}s.
	 * @param sources
	 *        the {@link CompiledQuery compiled queries} this {@link ConcatenatedCompiledQuery}
	 *        based on.
	 * @param order
	 *        Order in which the <code>sources</code> returns their values.
	 */
	public ConcatenatedCompiledQuery(ConnectionPool pool, Iterable<? extends CompiledQuery<? extends E>> sources,
			Order order) {
		super(pool);
		_sources = sources;
		if (order == ExpressionFactory.NO_ORDER) {
			_order = null;
		} else {
			_order = new UnwrappingComparator(OrderComparator.createComparator(order));
		}
	}

	private static class UnwrappingComparator implements Comparator<Object> {

		private Comparator<DataObject> _impl;

		public UnwrappingComparator(Comparator<DataObject> impl) {
			_impl = impl;
		}

		@Override
		public int compare(Object o1, Object o2) {
			if (o1 == o2) {
				return 0;
			}
			return _impl.compare(toDataObject(o1), toDataObject(o2));
		}

		private DataObject toDataObject(Object object) {
			DataObject dataObject;
			if (object instanceof TLObject) {
				dataObject = ((TLObject) object).tHandle();
			} else {
				dataObject = (DataObject) object;
			}
			return dataObject;
		}

	}

	@Override
	public CloseableIterator<E> searchStream(PooledConnection connection, RevisionQueryArguments arguments) {
		if (_order == null) {
			Iterator<CloseableIterator<? extends E>> iterators =
				new CompiledQueryIterator<>(_sources.iterator(), connection, arguments);
			return new ConcatenatedClosableIterator<>(iterators);
		} else {
			return new SortedConcatenatedClosableIterator<>(_order, _sources, connection, arguments);
		}
	}

	@Override
	public List<E> search(PooledConnection connection, RevisionQueryArguments arguments) {
		ArrayList<E> result = new ArrayList<>();
		for (CompiledQuery<? extends E> innerQuery : _sources) {
			List<? extends E> innerResult = innerQuery.search(connection, arguments);
			result.addAll(innerResult);
		}
		if (_order != null) {
			result.sort(_order);
		}
		return result;
	}

	/**
	 * Iterator that merges the results of a sequence of {@link CompiledQuery} into an sorted
	 * iterator.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class SortedConcatenatedClosableIterator<E> implements CloseableIterator<E> {

		private List<CloseableIterator<? extends E>> _iterators = new ArrayList<>();

		private final Iterator<? extends E> _mergedIterator;

		SortedConcatenatedClosableIterator(Comparator<? super E> comparator,
				Iterable<? extends CompiledQuery<? extends E>> sources, PooledConnection connection,
				RevisionQueryArguments arguments) {
			try {
				for (CompiledQuery<? extends E> query : sources) {
					_iterators.add(query.searchStream(connection, arguments));
				}
				_mergedIterator = IteratorUtil.mergeIterators(comparator, _iterators);
			} catch (Throwable ex) {
				// close already opened iterators
				close();
				throw ex;
			}
		}
		@Override
		public boolean hasNext() {
			return _mergedIterator.hasNext();
		}

		@Override
		public E next() {
			return _mergedIterator.next();
		}

		@Override
		public void close() {
			for (CloseableIterator<?> it : _iterators) {
				it.close();
			}

		}
		
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

