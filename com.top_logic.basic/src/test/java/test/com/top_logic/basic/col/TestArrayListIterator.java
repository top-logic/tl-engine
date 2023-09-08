/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ListIterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.top_logic.basic.col.ArrayListIterator;

/**
 * The class {@link TestArrayListIterator} tests {@link ArrayListIterator}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestArrayListIterator extends TestCase {

	public void testNextPrevious() {
		String[] storage = new String[] { "0", "1", "2" };
		ListIterator<String> it = new ArrayListIterator<>(storage, 1);
		String next = it.next();
		it.previous();
		assertEquals("Call next(), previous() resets the iterator.", next, it.next());
		String previous = it.previous();
		it.next();
		assertEquals("Call previous(), next() resets the iterator.", previous, it.previous());
	}

	public void testMain() {
		String[] storage = new String[] { "0", "1" };
		ListIterator<String> it = new ArrayListIterator<>(storage, 1);
		assertSame(0, it.previousIndex());
		assertTrue(it.hasPrevious());
		assertEquals(storage[0], it.previous());
		assertSame(-1, it.previousIndex());
		assertFalse(it.hasPrevious());
		try {
			it.previous();
			fail("No further element.");
		} catch (NoSuchElementException ex) {
			// expected
		}
		assertSame(0, it.nextIndex());
		assertEquals(storage[0], it.next());
		assertEquals(storage[1], it.next());
		assertSame(2, it.nextIndex());
		try {
			it.next();
			fail("No further element.");
		} catch (NoSuchElementException ex) {
			// expected
		}
	}

	public void testUnmodifiable() {
		String[] storage = new String[] { "0", "1", "2" };
		ListIterator<String> it = new ArrayListIterator<>(storage, 1);
		it.next();
		try {
			it.set("5");
		} catch (UnsupportedOperationException ex) {
			// expected
		}
		it.previous();
		assertEquals("1", it.next());

		try {
			it.add("5");
		} catch (UnsupportedOperationException ex) {
			// expected
		}
		assertEquals("2", it.next());
	}

}

