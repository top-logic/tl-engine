/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import junit.framework.TestCase;

import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.col.NameValueBuilder;

/**
 * Test case for {@link KeyValueBuffer} and its {@link NameValueBuffer} type binding.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestKeyValueBuffer extends TestCase {

	public void testEmpty() {
		assertFalse(new NameValueBuffer().iterator().hasNext());
	}

	public void testSingle() {
		KeyValueBuffer<String, Object> buffer = new NameValueBuffer().put("key", 42);
		Iterator<Entry<String, Object>> iterator = buffer.iterator();
		assertTrue(iterator.hasNext());
		assertTrue(iterator.hasNext());
		Entry<String, Object> entry = iterator.next();
		assertEquals("key", entry.getKey());
		assertEquals(42, entry.getValue());
		assertFalse(iterator.hasNext());
	}

	public void testClear() {
		NameValueBuffer buffer = new NameValueBuffer();
		// Use the builder interface for building.
		NameValueBuilder builder = buffer.setValue("k1", 42).setValue("k2", 10);

		// Use the buffer interface for iterating and reusing.
		buffer.iterator().next();
		buffer.clear();

		// Again use the builder interface.
		builder.setValue("k3", 13);

		// And the buffer interface for iterating again.
		assertEquals("k3", buffer.iterator().next().getKey());
	}

	public void testNoIterateAccessAfterIterator() {
		KeyValueBuffer<Object, Object> buffer = new KeyValueBuffer<>();
		buffer.iterator();
		try {
			buffer.iterator();
			fail("One time iteration only.");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

	public void testNoHasNextAccessBeforeIterator() {
		KeyValueBuffer<Object, Object> buffer = new KeyValueBuffer<>();
		try {
			buffer.hasNext();
			fail("Access without iterator()");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

	public void testNoNextAccessBeforeIterator() {
		KeyValueBuffer<Object, Object> buffer = new KeyValueBuffer<>();
		try {
			buffer.next();
			fail("Access without iterator()");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

	public void testNoValueAccessBeforeIterator() {
		KeyValueBuffer<Object, Object> buffer = new KeyValueBuffer<>();
		try {
			buffer.getValue();
			fail("Access without iterator()");
		} catch (IllegalStateException ex) {
			// Expected.
		}

	}

	public void testNoKeyAccessBeforeNext() {
		KeyValueBuffer<Object, Object> buffer = new KeyValueBuffer<>();
		buffer.iterator();
		try {
			buffer.getKey();
			fail("Access without next()");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

	public void testNoValueAccessBeforeNext() {
		KeyValueBuffer<Object, Object> buffer = new KeyValueBuffer<>();
		buffer.iterator();
		try {
			buffer.getValue();
			fail("Access without next()");
		} catch (IllegalStateException ex) {
			// Expected.
		}

	}

	public void testNoBuildAccessAfterIterator() {
		KeyValueBuffer<Object, Object> buffer = new KeyValueBuffer<>();
		buffer.iterator();
		try {
			buffer.put("x", "y");
			fail("Build after iterator()");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

	public void testMulti() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("k1", 42);
		map.put("k2", 13);
		map.put("kx", 10);
		map.put(null, 10);
		map.put("ky", null);
		assertEqualEntries(map.entrySet(), new NameValueBuffer().put("k1", 42).put("k2", 13).put("kx", 10)
			.put(null, 10).put("ky", null));
	}

	private static <K, V> void assertEqualEntries(Iterable<Entry<K, V>> values1, Iterable<Entry<K, V>> values2) {
		Iterator<Entry<K, V>> it1 = values1.iterator();
		Iterator<Entry<K, V>> it2 = values2.iterator();
		while (it1.hasNext()) {
			assertTrue(it2.hasNext());
			Entry<K, V> entry1 = it1.next();
			Entry<K, V> entry2 = it2.next();
			assertEquals(entry2, entry1);
			assertEquals(entry1, entry2);
			assertEquals(entry1.hashCode(), entry2.hashCode());
		}
		assertFalse(it2.hasNext());
	}
}
