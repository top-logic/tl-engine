/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.iterator;

import static com.top_logic.basic.col.iterator.IteratorUtil.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.top_logic.basic.col.iterator.AppendIterator;
import com.top_logic.basic.col.iterator.IteratorUtil;

/**
 * Tests for: {@link IteratorUtil}
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestIteratorUtil extends TestCase {

	private static final List<String> EXAMPLE_LIST = Collections.unmodifiableList(Arrays.asList("One", "Two", "Three"));

	public void testEmptyIteratorConstant() {
		assertFalse(EMPTY_ITERATOR.hasNext());
	}

	public void testEmptyIterator() {
		assertFalse(emptyIterator().hasNext());
	}
	
	public void testToEnumeration() {
		Iterator<String> sourceIterator = createExampleIterator();
		Enumeration<String> resultEnumeration = toEnumeration(sourceIterator);
		assertTrue(resultEnumeration.hasMoreElements());
		assertEquals(EXAMPLE_LIST.get(0), resultEnumeration.nextElement());
		assertTrue(resultEnumeration.hasMoreElements());
		assertEquals(EXAMPLE_LIST.get(1), resultEnumeration.nextElement());
		assertTrue(resultEnumeration.hasMoreElements());
		assertEquals(EXAMPLE_LIST.get(2), resultEnumeration.nextElement());
		assertFalse(resultEnumeration.hasMoreElements());
		try {
			resultEnumeration.nextElement();
			fail(); // Should not be reached
		} catch (NoSuchElementException expectedException) {
			// Correct
		}
	}
	
	public void testFromEnumeration() {
		Iterator<String> sourceIterator = createExampleIterator();
		Enumeration<String> sourceEnumeration = toEnumeration(sourceIterator);
		Iterator<String> resultIterator = fromEnumeration(sourceEnumeration);
		assertEqualsExampleIteratorContent(resultIterator);
	}

	public void testToIterable() {
		reusableTestToIterable();
	}
	
	public static void reusableTestToIterable() {
		Iterable<String> iterable = toIterable(createExampleIterator());
		assertEquals(toList(iterable.iterator()), EXAMPLE_LIST);
		try {
			iterable.iterator();
			fail(); // Should not be reached
		} catch (IllegalStateException expectedException) {
			// Correct
		}
	}
	
	public void testGetIterator() {
		reusableTestGetIterator();
	}
	
	public static void reusableTestGetIterator() {
		Iterator<?> iteratorFromNull = getIterator(null);
		assertFalse(iteratorFromNull.hasNext());
		
		Iterable<String> exampleIterable = EXAMPLE_LIST;
		Iterator<?> iteratorFromIterable = getIterator(exampleIterable);
		assertEqualsExampleIteratorContent(iteratorFromIterable);
		
		Iterator<?> iteratorFromArray = getIterator(EXAMPLE_LIST.toArray());
		assertEqualsExampleIteratorContent(iteratorFromArray);
		
		Iterator<?> iteratorFromIterator = getIterator(createExampleIterator());
		assertEqualsExampleIteratorContent(iteratorFromIterator);
		
		try {
			// Something else:
			getIterator(new Object());
			fail();
		} catch (IllegalArgumentException expected) {
			// Correct
		}
	}
	
	public void testCreateAppendingIterator() {
		AppendIterator<String> appendIterator = createAppendIterator();
		assertFalse(appendIterator.hasNext());
	}
	
	public void testDynamicCastView() {
		Iterable<?> exampleIterable = EXAMPLE_LIST;
		Iterable<String> resultIterableExactTypeMatch = dynamicCastView(String.class, exampleIterable);
		assertEqualsExampleIteratorContent(resultIterableExactTypeMatch.iterator());
		resultIterableExactTypeMatch.iterator(); // Check we can request multiple iterators (and not only one)
		
		Iterable<CharSequence> resultIterable = dynamicCastView(CharSequence.class, exampleIterable);
		assertEqualsExampleIteratorContent(resultIterable.iterator());
		
		try {
			Iterable<Integer> resultIterableWrongTypes = dynamicCastView(Integer.class, exampleIterable);
			assertEqualsExampleIteratorContent(resultIterableWrongTypes.iterator());
			fail();
		} catch (ClassCastException expectedException) {
			// Correct
		}
	}
	
	public void testToListIterable() {
		Iterable<String> exampleIterable = EXAMPLE_LIST;
		assertEquals(new ArrayList<>(EXAMPLE_LIST), toListIterable(exampleIterable));
	}
	
	public void testToList() {
		assertEquals(new ArrayList<>(EXAMPLE_LIST), toList(createExampleIterator()));
	}
	
	public void testToSetIterable() {
		Iterable<String> exampleIterable = EXAMPLE_LIST;
		assertEquals(new HashSet<>(EXAMPLE_LIST), toSetIterable(exampleIterable));
	}
	
	public void testToSet() {
		assertEquals(new HashSet<>(EXAMPLE_LIST), toSet(createExampleIterator()));
	}
	
	public void testMerge() {
		Comparator<Integer> cmp = (i1, i2) -> i1.compareTo(i2);

		List<Integer> first = list(0, 3, 5, 8);
		List<Integer> second = list(1, 4, 6);
		List<Integer> third = list(2, 7);

		ArrayList<Object> emptyMerge = toList(IteratorUtil.mergeIterators(cmp, list()));
		assertEquals(list(), emptyMerge);

		ArrayList<Object> singleMerge = toList(IteratorUtil.mergeIterators(cmp, list(first.iterator())));
		assertEquals(first, singleMerge);
		singleMerge = toList(IteratorUtil.mergeIterators(cmp, list(second.iterator())));
		assertEquals(second, singleMerge);

		ArrayList<Object> twoMerge =
			toList(IteratorUtil.mergeIterators(cmp, list(first.iterator(), second.iterator())));
		assertEquals(list(0, 1, 3, 4, 5, 6, 8), twoMerge);
		twoMerge =
			toList(IteratorUtil.mergeIterators(cmp, list(first.iterator(), third.iterator())));
		assertEquals(list(0, 2, 3, 5, 7, 8), twoMerge);

		ArrayList<Object> threeMerge =
			toList(IteratorUtil.mergeIterators(cmp, list(first.iterator(), second.iterator(), third.iterator())));
		assertEquals(list(0, 1, 2, 3, 4, 5, 6, 7, 8), threeMerge);

	}

	private static Iterator<String> createExampleIterator() {
		return EXAMPLE_LIST.iterator();
	}
	
	private static void assertEqualsExampleIteratorContent(Iterator<?> resultIterator) {
		assertTrue(resultIterator.hasNext());
		assertEquals(EXAMPLE_LIST.get(0), resultIterator.next());
		assertTrue(resultIterator.hasNext());
		assertEquals(EXAMPLE_LIST.get(1), resultIterator.next());
		assertTrue(resultIterator.hasNext());
		assertEquals(EXAMPLE_LIST.get(2), resultIterator.next());
		assertFalse(resultIterator.hasNext());
		try {
			resultIterator.next();
			fail(); // Should not be reached
		} catch (NoSuchElementException expectedException) {
			// Correct
		}
	}
	
}
