/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.top_logic.basic.col.ArrayIterator;

/**
 * The class {@link TestArrayIterator} tests {@link ArrayIterator}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestArrayIterator extends TestCase {

	public void testNext() {
		String[] storage = new String[] { "0", "1", "2" };
		ArrayIterator<String> it = new ArrayIterator<>(storage, 0);
		assertTrue(it.hasNext());
		assertEquals("0", it.next());
		assertTrue(it.hasNext());
		assertEquals("1", it.next());
		assertTrue(it.hasNext());
		assertTrue("Call to has next must not move iterator forward.", it.hasNext());
		assertEquals("2", it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("No further element.");
		} catch (NoSuchElementException ex) {
			// expected
		}
	}

	public void testUnmodifiable() {
		String[] storage = new String[] { "0" };
		ArrayIterator<String> it = new ArrayIterator<>(storage, 0);
		assertTrue(it.hasNext());
		assertEquals("0", it.next());
		try {
			it.remove();
			fail("Unmodifiable.");
		} catch (UnsupportedOperationException ex) {
			// expected
		}
	}

	@SuppressWarnings("unused")
	public void testIllegalArguments() {
		String[] storage = new String[] { "0", "1" };
		try {
			new ArrayIterator<>(storage, -1);
			fail("Illegal index: -1");
		} catch (Exception ex) {
			// expected
		}
		try {
			new ArrayIterator<>(storage, storage.length);
			fail("Illegal index:" + storage.length);
		} catch (Exception ex) {
			// expected
		}
	}

}

