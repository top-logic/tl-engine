/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.TypeMatchingIterator;

/**
 * Test case for {@link TypeMatchingIterator}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTypeMatchingIterator extends TestCase {

	public void testCorrectValues() {
		Date date1 = new Date();
		Date date2 = new Date();
		List<? extends Object> source = list(1, "hello", date1, date2, "world", 5.5);
		assertEquals(list(1), toList(FilterUtil.filterIterable(Integer.class, source)));
		assertEquals(list("hello", "world"), toList(FilterUtil.filterIterable(String.class, source)));
		assertEquals(list(date1, date2), toList(FilterUtil.filterIterable(Date.class, source)));
		assertEquals(list(5.5), toList(FilterUtil.filterIterable(Double.class, source)));
		assertEquals(list(1, 5.5), toList(FilterUtil.filterIterable(Number.class, source)));
	}

	public void testNullValues() {
		List<? extends Object> source = list(1, "hello", null, "world", 5.5);
		assertEquals(list(1, null), toList(FilterUtil.filterIterable(Integer.class, source)));
		assertEquals(list("hello", null, "world"),
			toList(FilterUtil.filterIterable(String.class, source)));
		assertEquals(list(null, 5.5), toList(FilterUtil.filterIterable(Double.class, source)));
		assertEquals(list(1, null, 5.5), toList(FilterUtil.filterIterable(Number.class, source)));
	}

	public void testFilterIterable() {
		Iterable<String> filtered = FilterUtil.filterIterable(String.class, Arrays.<Object> asList("a", 1, "b", 2));
		Iterator<String> iterator = filtered.iterator();

		assertTrue(iterator.hasNext());
		assertEquals("a", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("b", iterator.next());
		assertFalse(iterator.hasNext());
	}

	public void testFilterIterableNoMatch() {
		Iterable<String> filtered = FilterUtil.filterIterable(String.class, Arrays.<Object> asList(1, 2, 3));
		Iterator<String> iterator = filtered.iterator();

		assertFalse(iterator.hasNext());
	}

	public void testFilterIterableEmpty() {
		Iterable<String> filtered = FilterUtil.filterIterable(String.class, Arrays.<Object> asList());
		Iterator<String> iterator = filtered.iterator();

		assertFalse(iterator.hasNext());
	}

}

