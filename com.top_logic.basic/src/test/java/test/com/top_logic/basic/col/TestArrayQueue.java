/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import junit.framework.TestCase;

import com.top_logic.basic.col.ArrayQueue;

/**
 * Tests for {@link ArrayQueue}
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestArrayQueue extends TestCase {
	
	public void testPush() {
		Queue<String> queue = new ArrayQueue<>(3);
		
		queue.offer("s1");
		queue.offer("s2");
		queue.offer("s3");
		assertEquals(3, queue.size());
		
		assertEquals("s1", queue.peek());
		assertEquals("s1", queue.poll());
		assertEquals("s2", queue.peek());
		assertEquals("s2", queue.poll());
		assertEquals("s3", queue.peek());
		assertEquals("s3", queue.poll());
		
		assertTrue(queue.isEmpty());
	}
	
	public void testIterator() {
		Queue<String> queue = new ArrayQueue<>(7);
		queue.offer("s1");
		queue.offer("s2");
		queue.offer("s3");
		
		Iterator<String> iterator = queue.iterator();
		assertTrue(iterator.hasNext());
		assertEquals("s1", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("s2", iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals("s3", iterator.next());
		assertFalse(iterator.hasNext());
	}

	public void testRemove1() {
		Queue<String> queue = new ArrayQueue<>(3);
		queue.offer("s1");
		queue.offer("s2");
		queue.offer("s3");
		queue.poll();
		queue.offer("s4");
		
		Iterator<String> iterator = queue.iterator();
		iterator.remove();
		assertEquals("s3", queue.poll());
		assertEquals("s4", queue.poll());
		assertEquals(null, queue.poll());
	}
	
	public void testRemove2() {
		Queue<String> queue = new ArrayQueue<>(4);
		queue.offer("s1");
		queue.offer("s2");
		queue.offer("s3");
		queue.offer("s4");
		queue.poll();
		queue.poll();
		queue.offer("s5");
		
		Iterator<String> iterator = queue.iterator();
		assertEquals("s3", iterator.next());
		iterator.remove();
		assertEquals("s5", iterator.next());
		assertFalse(iterator.hasNext());
	}
	
	public void testRemove3() {
		Queue<String> queue = new ArrayQueue<>(5);
		queue.offer("s1");
		queue.offer("s2");
		queue.offer("s3");
		queue.offer("s4");
		queue.offer("s5");
		queue.poll();
		
		Iterator<String> iterator = queue.iterator();
		assertEquals("s2", iterator.next());
		iterator.remove();
		assertEquals("s4", iterator.next());
		assertEquals("s5", iterator.next());
		assertFalse(iterator.hasNext());
	}
	
	public void testRemove4() {
		Queue<String> queue = new ArrayQueue<>(5);
		queue.offer("s1");
		queue.offer("s2");
		queue.offer("s3");
		queue.offer("s4");
		queue.offer("s5");
		queue.poll();
		
		Iterator<String> iterator = queue.iterator();
		assertEquals("s2", iterator.next());
		assertEquals("s3", iterator.next());
		assertEquals("s4", iterator.next());
		iterator.remove();
		assertFalse(iterator.hasNext());
	}
	
	public void testRemove5() {
		Queue<String> queue = new ArrayQueue<>(5);
		queue.offer("s1");
		queue.offer("s2");
		queue.offer("s3");
		queue.offer("s4");
		queue.offer("s5");
		queue.poll();
		queue.poll();
		queue.poll();
		queue.offer("s6");
		queue.offer("s7");
		
		Iterator<String> iterator = queue.iterator();
		assertEquals("s4", iterator.next());
		assertEquals("s5", iterator.next());
		assertEquals("s6", iterator.next());
		assertEquals("s7", iterator.next());
		iterator.remove();
		assertFalse(iterator.hasNext());
	}
	
	public void testRemove6() {
		Queue<String> queue = new ArrayQueue<>(5);
		queue.offer("s1");
		queue.offer("s2");
		queue.offer("s3");
		queue.offer("s4");
		queue.offer("s5");
		queue.poll();
		queue.poll();
		queue.poll();
		queue.offer("s6");
		queue.offer("s7");
		
		Iterator<String> iterator = queue.iterator();
		assertEquals("s4", iterator.next());
		iterator.remove();
		assertEquals("s6", iterator.next());
		assertEquals("s7", iterator.next());
		assertFalse(iterator.hasNext());
		try {
			iterator.next();
			fail("iterator is empty");
		} catch (NoSuchElementException ex) {
			// expected
		}
	}

	public void testConcurrentModification() {
		Queue<String> queue = new ArrayQueue<>(5);
		queue.offer("s1");
		queue.offer("s2");
		Iterator<String> iterator = queue.iterator();
		assertEquals("s1", iterator.next());
		queue.add("s3");
		try {
			iterator.next();
			fail("queue was modified concurrently");
		} catch (ConcurrentModificationException ex) {
			// expected
		}
	}
	
	public void testResize() {
		Queue<Integer> queue = new ArrayQueue<>(2);
		queue.offer(1);
		queue.offer(2);
		// array must be copied here
		queue.offer(3);
		assertEquals(Integer.valueOf(1), queue.poll());
		assertEquals(Integer.valueOf(2), queue.poll());
		assertEquals(Integer.valueOf(3), queue.poll());
		assertEquals(null, queue.poll());
		// must also be null on repeated call
		assertEquals(null, queue.poll());
		assertEquals(null, queue.peek());
	}

}
