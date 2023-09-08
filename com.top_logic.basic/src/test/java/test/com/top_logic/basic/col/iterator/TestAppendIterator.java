/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.iterator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.col.iterator.AppendIterator;

/**
 * Tests for: {@link AppendIterator}
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestAppendIterator extends TestCase {
	
	public void testInitialization() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		assertFalse(appendIterator.hasNext());
	}
	
	public void testSingleAppend() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		appendIterator.append("One");
		assertTrue(appendIterator.hasNext());
		assertEquals("One", appendIterator.next());
		assertFalse(appendIterator.hasNext());
	}
	
	public void testMultipleAppends() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		appendIterator.append("Two").append("Three");
		assertTrue(appendIterator.hasNext());
		assertEquals("Two", appendIterator.next());
		assertTrue(appendIterator.hasNext());
		assertEquals("Three", appendIterator.next());
		assertFalse(appendIterator.hasNext());
	}
	
	public void testCopyUnderlyingCollection() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		appendIterator.append("Two").append("Three");
		appendIterator.next();
		appendIterator.next();
		assertEquals(Arrays.asList("Two", "Three"), appendIterator.copyUnderlyingCollection());
	}
	
	public void testCopyUnderlyingCollectionWithoutNext() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		appendIterator.append("Two").append("Three");
		assertEquals(Arrays.asList("Two", "Three"), appendIterator.copyUnderlyingCollection());
	}
	
	public void testRemove() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		appendIterator.append("One").append("Two").append("Three").append("Four");
		appendIterator.next();
		appendIterator.next();
		appendIterator.next();
		appendIterator.next();
		assertFalse(appendIterator.hasNext());
		appendIterator.remove();
		assertFalse(appendIterator.hasNext());
		appendIterator.append("Five").append("Six");
		appendIterator.next();
		assertTrue(appendIterator.hasNext());
		appendIterator.remove();
		assertEquals(Arrays.asList("One", "Two", "Three", "Six"), appendIterator.copyUnderlyingCollection());
	}
	
	public void testRemoveWithoutNext() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		appendIterator.append("One");
		try {
			appendIterator.remove();
		} catch (IllegalStateException expectedException) {
			// Correct
		}
		// Works still correct?
		assertTrue(appendIterator.hasNext());
		assertEquals("One", appendIterator.next());
		assertFalse(appendIterator.hasNext());
		assertEquals(Arrays.asList("One"), appendIterator.copyUnderlyingCollection());
	}
	
	public void testRemoveTwice() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		appendIterator.append("One").append("Two");
		appendIterator.next();
		appendIterator.remove();
		try {
			appendIterator.remove();
		} catch (IllegalStateException expectedException) {
			// Correct
		}
		// Works still correct?
		assertTrue(appendIterator.hasNext());
		assertEquals("Two", appendIterator.next());
		assertFalse(appendIterator.hasNext());
		assertEquals(Arrays.asList("Two"), appendIterator.copyUnderlyingCollection());
	}
	
	public void testAppendAll() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		appendIterator.appendAll(Arrays.asList("Seven", "Eight"));
		assertTrue(appendIterator.hasNext());
		assertEquals("Seven", appendIterator.next());
		assertTrue(appendIterator.hasNext());
		assertEquals("Eight", appendIterator.next());
		assertFalse(appendIterator.hasNext());
	}
	
	public void testAppendAllEmptyList() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		appendIterator.appendAll(Collections.<String>emptyList());
		assertFalse(appendIterator.hasNext());
	}
	
	public void testAppendAllChangeCollectionSiteEffectProtection() {
		AppendIterator<String> appendIterator = new AppendIterator<>();
		List<String> appendedList = Arrays.asList("Nine", "Ten");
		appendIterator.appendAll(appendedList);
		appendedList.set(0, "Eleven");
		
		assertTrue(appendIterator.hasNext());
		assertEquals("Nine", appendIterator.next());
		assertTrue(appendIterator.hasNext());
		assertEquals("Ten", appendIterator.next());
		assertFalse(appendIterator.hasNext());
	}
	
}
