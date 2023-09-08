/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.iterator;

import static com.top_logic.basic.col.iterator.IteratorUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
