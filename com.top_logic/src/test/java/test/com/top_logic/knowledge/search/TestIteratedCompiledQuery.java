/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.search;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.SimpleCloseableIteratorAdapter;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.ConcatenatedCompiledQuery;
import com.top_logic.knowledge.search.EmptyCompiledQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.search.StreamingCompiledQuery;

/**
 * Tests of {@link ConcatenatedCompiledQuery}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestIteratedCompiledQuery extends TestCase {

	private RevisionQueryArguments _arguments;

	private PooledConnection _connection;

	private CloseableIterator<?> _iterator;

	@Override
	protected void tearDown() throws Exception {
		if (_iterator != null) {
			_iterator.close();
		}
		super.tearDown();
	}

	/**
	 * Tests that {@link ConcatenatedCompiledQuery} works with empty source
	 */
	public void testNoSource() {
		List<CompiledQuery<Object>> sources = Collections.emptyList();
		ConcatenatedCompiledQuery<Object> iteratedCompiledQuery = iteratedCompiledQuery(sources);
		_iterator = iteratedCompiledQuery.searchStream(_connection, _arguments);
		assertFalse(_iterator.hasNext());
		assertFalse("hasNext() should be stable ", _iterator.hasNext());
		try {
			_iterator.next();
			fail("Iterator must be empty");
		} catch (NoSuchElementException ex) {
			// expected
		}
	}

	private <T> ConcatenatedCompiledQuery<T> iteratedCompiledQuery(List<CompiledQuery<T>> sources) {
		return new ConcatenatedCompiledQuery<>(null, sources);
	}

	/**
	 * Tests that some wrapped {@link CompiledQuery} can produce "empty" iterators
	 */
	public void testEmptySources() {
		List<CompiledQuery<Object>> sources = new ArrayList<>();
		sources.add(EmptyCompiledQuery.getInstance());
		sources.add(EmptyCompiledQuery.getInstance());
		ConcatenatedCompiledQuery<Object> iteratedCompiledQuery = iteratedCompiledQuery(sources);
		_iterator = iteratedCompiledQuery.searchStream(_connection, _arguments);
		assertFalse(_iterator.hasNext());
		assertFalse("hasNext() should be stable ", _iterator.hasNext());
		assertEmpty(_iterator);
	}

	private static void assertEmpty(Iterator<?> iterator) {
		assertFalse(iterator.hasNext());
		try {
			iterator.next();
			fail("Iterator must be empty");
		} catch (NoSuchElementException ex) {
			// expected
		}
	}

	/**
	 * Ordinary test that the {@link ConcatenatedCompiledQuery} dispatches to sources in correct order.
	 */
	public void testConcatenated() {
		List<CompiledQuery<Integer>> sources = new ArrayList<>();
		sources.add(EmptyCompiledQuery.<Integer>getInstance());
		sources.add(new ConstantCompiledQuery<>(list(1, 2, 3)));
		sources.add(EmptyCompiledQuery.<Integer> getInstance());
		sources.add(new ConstantCompiledQuery<>(list(4, 5, 6)));
		ConcatenatedCompiledQuery<Integer> iteratedCompiledQuery = iteratedCompiledQuery(sources);
		try (CloseableIterator<Integer> firstIterator = iteratedCompiledQuery.searchStream(_connection, _arguments)) {
			ArrayList<Integer> result = new ArrayList<>();
			while (firstIterator.hasNext()) {
				result.add(firstIterator.next());
			}
			assertEmpty(firstIterator);
			assertEquals(list(1, 2, 3, 4, 5, 6), result);
		}
		try (CloseableIterator<Integer> secondIterator = iteratedCompiledQuery.searchStream(_connection, _arguments)) {
			ArrayList<Integer> result = new ArrayList<>();
			while (secondIterator.hasNext()) {
				result.add(secondIterator.next());
			}
			assertEmpty(secondIterator);
			assertEquals(list(1, 2, 3, 4, 5, 6), result);
		}

	}

	/**
	 * Test that iterated {@link Iterator#next()} works without calling {@link Iterator#hasNext()}
	 */
	public void testNoHasNext() {
		List<CompiledQuery<Integer>> sources = new ArrayList<>();
		sources.add(EmptyCompiledQuery.<Integer> getInstance());
		sources.add(new ConstantCompiledQuery<>(list(1, 2, 3)));
		sources.add(EmptyCompiledQuery.<Integer> getInstance());
		sources.add(new ConstantCompiledQuery<>(list(4, 5, 6)));
		ConcatenatedCompiledQuery<Integer> iteratedCompiledQuery = iteratedCompiledQuery(sources);
		ArrayList<Integer> allElements;
		try (CloseableIterator<Integer> firstIterator = iteratedCompiledQuery.searchStream(_connection, _arguments)) {
			allElements = new ArrayList<>();
			while (firstIterator.hasNext()) {
				allElements.add(firstIterator.next());
			}
			assertEmpty(firstIterator);
			assertEquals(list(1, 2, 3, 4, 5, 6), allElements);
		}
		try (CloseableIterator<Integer> secondIterator = iteratedCompiledQuery.searchStream(_connection, _arguments)) {
			for (int i = 0; i < allElements.size(); i++) {
				// access without hasNext()
				Integer next = secondIterator.next();
				assertEquals(allElements.get(i), next);
			}
			assertEmpty(secondIterator);
		}
	}

	private static class ConstantCompiledQuery<E> extends StreamingCompiledQuery<E> {

		private final Iterable<E> _sources;

		public ConstantCompiledQuery(Iterable<E> sources) {
			super(null);
			_sources = sources;
		}

		@Override
		public CloseableIterator<E> searchStream(PooledConnection connection, RevisionQueryArguments arguments) {
			return new SimpleCloseableIteratorAdapter<>(_sources.iterator());
		}

	}

}

