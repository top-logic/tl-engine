/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.CloseableIteratorAdapter;
import com.top_logic.basic.col.ConcatenatedClosableIterator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.MappingIterator;
import com.top_logic.basic.col.SimpleCloseableIteratorAdapter;
import com.top_logic.basic.shared.collection.map.MappedIterator;

/**
 * Test case for {@link ConcatenatedClosableIterator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestConcatenatedClosableIterator extends TestCase {

	public void testExceptionInInnerIterator() {
		class ExceptionInNext extends RuntimeException {
			public ExceptionInNext() {
				// nothing special here
			}
		}
		Mapping<Integer, CloseableIterator<Integer>> mapping = new Mapping<>() {

			@Override
			public CloseableIterator<Integer> map(Integer input) {
				switch(input) {
					case 1:
					case 2:
						return new SimpleCloseableIteratorAdapter<>(Arrays.asList(input).iterator());
					case 3:
						throw new ExceptionInNext();
				}
				return null;
			}
		};
		MappingIterator<Integer, CloseableIterator<Integer>> iterators =
			new MappingIterator<>(mapping, Arrays.asList(1, 2, 3).iterator());

		ConcatenatedClosableIterator<Integer> concatenatedClosableIterator =
			new ConcatenatedClosableIterator<>(iterators);
		try {
			try {
				assertEquals(Integer.valueOf(1), concatenatedClosableIterator.next());
				assertEquals(Integer.valueOf(2), concatenatedClosableIterator.next());
				Integer unexpected = concatenatedClosableIterator.next();
				fail("Expected inner iterator throws exception but returns " + unexpected);
			} finally {
				concatenatedClosableIterator.close();
			}
		} catch (ExceptionInNext ex) {
			// expected
		} catch (RuntimeException ex) {
			BasicTestCase.fail("Ticket #11318: Expected exception thrown by inner iterator.", ex);
		}
	}

	public void testConcat() {
		assertEquals(list("A", "B", "C", "D"), concatComplete(list("A", "B"), list("C", "D")));
	}
	
	public void testConcatSingle() {
		assertEquals(list("A", "B", "C", "D"), concatComplete(list("A", "B", "C", "D")));
	}
	
	public void testConcatEmpty() {
		assertEquals(list(), concatComplete(list()));
		assertEquals(list(), concatComplete(list(), list()));
	}

	public void testConcatIncludingEmpty() {
		List<String> empty = list();
		assertEquals(list("A", "B", "C", "D"), concatComplete(empty, list("A"), empty, list("B", "C", "D"), empty, empty));
	}

	public void testPartialConcat() {
		List<String> empty = list();
		
		for (int limit = 0; limit < 5; limit++) {
			assertEquals(list("A", "B", "C", "D").subList(0, limit), concat(limit, empty, list("A"), empty, list("B", "C", "D"), empty, empty));
		}
	}
	
	private static <T> List<T> concatComplete(List<T> ...lists) {
		return concat(Integer.MAX_VALUE, lists);
	}
	
	private static <T> List<T> concat(int limit, List<T> ...lists) {
		CheckedConcat<T> checkedConcat = new CheckedConcat<>(lists);
		
		List<T> result;
		try (CloseableIterator<T> stream = checkedConcat.getConcatenated()) {
			result = consume(stream, limit);
		}
		
		checkedConcat.check();
		return result;
	}

	private static <T> List<T> consume(CloseableIterator<T> stream, int limit) {
		List<T> result;
		result = new ArrayList<>();
		int n = 0;
		while (stream.hasNext() && n < limit) {
			result.add(stream.next());
			n++;
		}
		return result;
	}

	/**
	 * Construction of a {@link ConcatenatedClosableIterator} with functionality to ensure that all
	 * requested delegate iterators are closed after closing the concatenation.
	 */
	static class CheckedConcat<T> {
		Set<Object> openIterators = new HashSet<>();
		
		private ConcatenatedClosableIterator<T> concatenated;

		public CheckedConcat(List<T>... lists) {
			Iterator<List<T>> listsIterator = Arrays.asList(lists).iterator();
			
			Iterator<CloseableIterator<T>> closeableIterators = new MappedIterator<>(listsIterator) {
				@Override
				protected CloseableIterator<T> map(List<T> input) {
					CloseableIteratorAdapter<T> result = new CloseableIteratorAdapter<>(input.iterator()) {
						@Override
						protected void internalClose() {
							openIterators.remove(this);
						}
					};
					
					openIterators.add(result);
					return result;
				}
			};
			
			concatenated = new ConcatenatedClosableIterator<>(closeableIterators);
		}
		
		public ConcatenatedClosableIterator<T> getConcatenated() {
			return concatenated;
		}

		public void check() {
			assertTrue("Not all requested iterators have been closed.", openIterators.isEmpty());
		}
	}

}
