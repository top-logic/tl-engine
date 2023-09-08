/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.col.InlineMap;

/**
 * Test for {@link InlineMap}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestInlineMap extends TestCase {

	public void testInlineMap() {
		InlineMap<String, String> map = InlineMap.empty();
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());
		assertFalse(map.hasValue("key"));
		assertNull(map.getValue("key"));
		map = map.removeValue("key");
		assertEquals(0, map.size());
		assertEquals(Collections.emptyMap(), map.toMap());

		map = map.putValue("key", "value");
		assertEquals(1, map.size());
		assertTrue(map.hasValue("key"));
		assertEquals("value", map.getValue("key"));
		assertNull(map.getValue("key1"));
		assertEquals(Collections.singletonMap("key", "value"), map.toMap());

		map = map.removeValue("key");
		assertEquals(0, map.size());

		map = map.putValue("key", "value");
		map = map.putValue("key", "newValue");
		assertEquals(1, map.size());
		assertEquals("newValue", map.getValue("key"));
		assertTrue(map.hasValue("key"));
		assertFalse(map.hasValue("key1"));

		map = map.removeValue("key1");
		assertEquals(1, map.size());

		map = map.putValue("key1", "value1");
		assertEquals(2, map.size());

		map = map.putValue("key2", "value2");
		assertEquals(3, map.size());
		assertNull(map.getValue("key3"));
		assertFalse(map.hasValue("key3"));
		assertTrue(map.hasValue("key2"));

		map = map.removeValue("key2");
		assertEquals(2, map.size());
		map = map.removeValue("key1");
		assertEquals(1, map.size());
		map = map.removeValue("key");
		assertEquals(0, map.size());
		map = map.removeValue("key");
		assertEquals(0, map.size());
	}

	public void testNullValue() {
		InlineMap<String, String> map = InlineMap.empty();
		assertFalse(map.hasValue("key"));
		map = map.putValue("key", null);
		assertNull(map.getValue("key"));
		assertTrue(map.hasValue("key"));
		map = map.putValue("key2", null);
		assertNull(map.getValue("key2"));
		assertTrue(map.hasValue("key2"));
	}

	public void testPutAllValues() {
		InlineMap<String, String> map = InlineMap.empty();
		map = map.putAllValues(Collections.emptyMap());
		assertEquals(0, map.size());
		map = map.putAllValues(Collections.singletonMap("key", "value"));
		assertEquals(1, map.size());
		assertEquals("value", map.getValue("key"));
		
		Map<String, String> m = new HashMap<>();
		m.put("key", "value");
		m.put("key1", "value1");
		map = InlineMap.empty();
		map = map.putAllValues(m);
		assertEquals(m, map.toMap());

		map = InlineMap.empty();
		map = map.putValue("key", "otherValue");
		map = map.putAllValues(m);
		assertEquals(m, map.toMap());

		m.put("key2", "value2");
		map = map.putAllValues(m);
		assertEquals(3, map.size());
	}

	public void testPutAllEmpty() {
		assertTrue(InlineMap.empty().putAllValues(InlineMap.empty()).isEmpty());
	}

	public void testHashedPutAllValuesWithInlineArgument() {
		InlineMap<String, String> map = map5();
		assertEquals(5, map.size());
		
		InlineMap<String, String> other = InlineMap.<String, String> empty().putValue("foo", "1").putValue("x", "2");
		map = map.putAllValues(other);
		assertEquals(6, map.size());
		assertEquals("2", map.getValue("x"));
	}

	public void testHashedPutAllValuesWithMap() {
		InlineMap<String, String> map = map5();

		Map<String, String> other2 = new HashMap<>();
		other2.put("a", "3");
		other2.put("b", "4");
		other2.put("c", "5");
		map = map.putAllValues(other2);

		assertEquals(8, map.size());
	}

	public void testHashedRemoveValue() {
		InlineMap<String, String> map = map5().removeValue("z");

		assertEquals(4, map.size());
		assertFalse(map.hasValue("z"));
		assertTrue(map.hasValue("x"));
	}

	private InlineMap<String, String> map5() {
		InlineMap<String, String> map = InlineMap.<String, String> empty()
			.putValue("x", "0")
			.putValue("y", "0")
			.putValue("z", "0")
			.putValue("z1", "0")
			.putValue("z2", "0");
		return map;
	}

}

