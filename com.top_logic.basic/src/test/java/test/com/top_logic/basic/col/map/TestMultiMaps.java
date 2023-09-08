/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.map;

import static com.top_logic.basic.col.map.MultiMaps.*;
import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.TestCase;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.map.MultiMaps;

/**
 * Tests for {@link MultiMaps}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestMultiMaps extends TestCase {

	/**
	 * Tests {@link MultiMaps#add(Map, Object, Object, Supplier)}
	 */
	public void testAddArbitraryCollectionFactory() {
		Map<String, List<String>> multiMap = new HashMap<>();

		assertTrue(add(multiMap, "a", "a", ArrayList::new));
		assertTrue(add(multiMap, "a", "b", ArrayList::new));
		assertTrue(add(multiMap, "a", "c", ArrayList::new));
		assertTrue(add(multiMap, "a", "a", ArrayList::new));
		assertTrue(add(multiMap, "a", "b", ArrayList::new));
		assertTrue(add(multiMap, "a", "c", ArrayList::new));

		assertEquals(list("a", "b", "c", "a", "b", "c"), multiMap.get("a"));
		assertNull(multiMap.get("b"));
		assertNull(multiMap.get("c"));
	}

	/**
	 * Tests {@link MultiMaps#add(Map, Object, Object)}
	 */
	public void testadd() {
		Map<String, Set<String>> multiMap = new HashMap<>();

		assertTrue(add(multiMap, "a", "a"));
		assertTrue(add(multiMap, "a", "b"));
		assertTrue(add(multiMap, "a", "c"));
		assertFalse(add(multiMap, "a", "a"));
		assertFalse(add(multiMap, "a", "b"));
		assertFalse(add(multiMap, "a", "c"));

		assertEquals(set("a", "b", "c"), multiMap.get("a"));
		assertEquals(set("a", "b", "c"), get(multiMap, "a"));

		assertNull(multiMap.get("b"));
		assertEquals(set(), get(multiMap, "b"));

		assertNull(multiMap.get("c"));
		assertEquals(set(), get(multiMap, "c"));
	}

	/**
	 * Tests {@link MultiMaps#add(Map, Map)}
	 */
	public void testadd2() {
		Map<String, Set<String>> multiMap = new HashMap<>();

		assertTrue(add(multiMap, "a", "a"));
		assertTrue(add(multiMap, "a", "b"));
		assertTrue(add(multiMap, "a", "c"));

		assertFalse(add(multiMap, new MapBuilder<String, String>().put("a", "b").toMap()));

		assertTrue(add(multiMap, new MapBuilder<String, String>().put("a", "a").put("b", "b").put("c", "c")
			.toMap()));
		assertEquals(set("b"), get(multiMap, "b"));
		assertEquals(set("c"), get(multiMap, "c"));

		assertTrue(add(multiMap, new MapBuilder<String, String>().put("b", "x").put("c", "x").toMap()));
		assertEquals(set("b", "x"), get(multiMap, "b"));
		assertEquals(set("c", "x"), get(multiMap, "c"));
	}

	/**
	 * Tests {@link MultiMaps#addAll(Map, Object, Collection)}
	 */
	public void testaddNull() {
		Map<String, Set<String>> multiMap = new HashMap<>();

		assertTrue(add(multiMap, "a", "a"));
		assertTrue(add(multiMap, "a", null));
		assertFalse(add(multiMap, "a", null));

		assertEquals(set("a", null), multiMap.get("a"));
	}

	/**
	 * Tests {@link MultiMaps#remove(Map, Object, Object)}
	 */
	public void testRemoveMultiMap() {
		Map<String, Set<String>> multiMap = new HashMap<>();

		add(multiMap, "a", "a");
		add(multiMap, "a", "b");
		add(multiMap, "a", "c");
		assertTrue(remove(multiMap, "a", "b"));
		assertFalse(remove(multiMap, "a", "d"));

		assertFalse(remove(multiMap, "b", "b"));

		assertEquals(set("a", "c"), multiMap.get("a"));
		assertNull(multiMap.get("b"));
	}

	/**
	 * Tests {@link MultiMaps#remove(Map, Object, Collection)}
	 */
	public void testRemoveAll() {
		Map<String, Set<String>> multiMap = new HashMap<>();

		add(multiMap, "a", "a");
		add(multiMap, "a", "b");
		add(multiMap, "a", "c");
		assertTrue(remove(multiMap, "a", list("b", "c", "d")));
		assertFalse(remove(multiMap, "b", list("b", "c", "d")));

		assertEquals(set("a"), multiMap.get("a"));
		assertNull(multiMap.get("b"));

		Map<String, ? extends Collection<String>> alias = multiMap;
		Map<String, Set<String>> clone = MultiMaps.clone(alias);

		assertEquals(multiMap, clone);
	}

	/**
	 * Tests {@link MultiMaps#addAll(Map, Object, Collection)}
	 */
	public void testAddAll() {
		Map<String, Set<String>> multiMap = new HashMap<>();

		assertTrue(addAll(multiMap, "a", list("a")));
		assertTrue(addAll(multiMap, "a", list("b", "c", "b", "c")));
		assertFalse(addAll(multiMap, "a", list("a")));

		assertTrue(addAll(multiMap, "b", list("a")));

		assertFalse(addAll(multiMap, "c", Collections.<String> emptyList()));

		assertFalse(addAll(multiMap, "d", null));

		assertEquals(set("a", "b", "c"), multiMap.get("a"));
		assertEquals(set("a"), multiMap.get("b"));

		assertNull(multiMap.get("c"));
		assertNull(multiMap.get("d"));
	}

	/**
	 * Tests {@link MultiMaps#addAll(Map, Map)}
	 */
	public void testAddAll2() {
		Map<String, Set<String>> multiMap = new HashMap<>();
		assertTrue(addAll(multiMap, "a", list("a", "x")));
		assertTrue(addAll(multiMap, "b", list("b", "x")));

		assertFalse(addAll(multiMap, multiMap));

		Map<String, Set<String>> add = new HashMap<>();
		assertTrue(addAll(add, "a", list("x")));
		assertTrue(addAll(add, "b", list("x")));

		assertFalse(addAll(multiMap, add));

		assertTrue(addAll(add, "a", list("y", "z")));
		assertTrue(addAll(add, "b", list("a", "c", "d")));

		assertTrue(addAll(multiMap, add));

		assertEquals(set("a", "y", "x", "z"), multiMap.get("a"));
		assertEquals(set("a", "b", "c", "d", "x"), multiMap.get("b"));
	}

	// The standard order for the following tests is 1, 3, 2 and not 1, 2, 3 as the MultiMap should
	// not be sorted, but keep its initial order.

	/** Test for {@link MultiMaps#add(Map, Object, Object)} */
	public void testStableOrderAdd1() {
		Map<Integer, Set<Integer>> multiMap = new LinkedHashMap<>();
		add(multiMap, 1, 1);
		add(multiMap, 1, 3);
		add(multiMap, 1, 2);
		add(multiMap, 3, 1);
		add(multiMap, 3, 3);
		add(multiMap, 3, 2);
		add(multiMap, 2, 1);
		add(multiMap, 2, 3);
		add(multiMap, 2, 2);
		assertOrder1(multiMap);
	}

	/** Test for {@link MultiMaps#add(Map, Map)} */
	public void testStableOrderAdd2() {
		Map<Integer, Set<Integer>> multiMap = new LinkedHashMap<>();
		LinkedHashMap<Integer, Integer> partMap1 = new LinkedHashMap<>();
		partMap1.put(1, 1);
		partMap1.put(3, 1);
		partMap1.put(2, 1);
		add(multiMap, partMap1);
		LinkedHashMap<Integer, Integer> partMap3 = new LinkedHashMap<>();
		partMap3.put(1, 3);
		partMap3.put(3, 3);
		partMap3.put(2, 3);
		add(multiMap, partMap3);
		LinkedHashMap<Integer, Integer> partMap2 = new LinkedHashMap<>();
		partMap2.put(1, 2);
		partMap2.put(3, 2);
		partMap2.put(2, 2);
		add(multiMap, partMap2);
		assertOrder1(multiMap);
	}

	/** Test for {@link MultiMaps#addAll(Map, Map)} */
	public void testStableOrderAddAll1() {
		Map<Integer, Set<Integer>> multiMap = new LinkedHashMap<>();
		addAll(multiMap, 1, linkedSet(1, 3, 2));
		addAll(multiMap, 3, linkedSet(1, 3));
		addAll(multiMap, 2, set(1));
		addAll(multiMap, 3, set(2));
		addAll(multiMap, 2, set(3));
		addAll(multiMap, 2, set(2));
		assertOrder1(multiMap);
	}

	/** Test for {@link MultiMaps#addAll(Map, Object, Collection)} */
	public void testStableOrderAddAll2() {
		Map<Integer, Set<Integer>> multiMap = new LinkedHashMap<>();
		LinkedHashMap<Integer, Set<Integer>> partMultiMap1 = new LinkedHashMap<>();
		partMultiMap1.put(1, linkedSet(1, 3));
		partMultiMap1.put(3, set(1));
		partMultiMap1.put(2, linkedSet(1, 3, 2));
		addAll(multiMap, partMultiMap1);
		LinkedHashMap<Integer, Set<Integer>> partMultiMap2 = new LinkedHashMap<>();
		partMultiMap2.put(1, set(2));
		partMultiMap2.put(3, linkedSet(3, 2));
		addAll(multiMap, partMultiMap2);
		assertOrder1(multiMap);
	}

	/** Test for {@link MultiMaps#get(Map, Object)} */
	public void testStableOrderGet() {
		Map<Integer, Set<Integer>> multiMap = buildMultiMap1();
		assertSameOrder(linkedSet(1, 3, 2), multiMap.get(1));
		assertSameOrder(linkedSet(1, 3, 2), multiMap.get(2));
		assertSameOrder(linkedSet(1, 3, 2), multiMap.get(3));
	}

	/** Test for {@link MultiMaps#clone(Map)} */
	public void testClone() {
		Map<Integer, Set<Integer>> original = buildMultiMap1();
		Map<Integer, Set<Integer>> clone = MultiMaps.clone(original);
		assertSameOrder(original, clone);
		Map<Integer, Set<Integer>> cloneOfClone = MultiMaps.clone(clone);
		assertSameOrder(original, cloneOfClone);
	}

	private Map<Integer, Set<Integer>> buildMultiMap1() {
		Map<Integer, Set<Integer>> multiMap = new LinkedHashMap<>();
		addAll(multiMap, 1, linkedSet(1, 3, 2));
		addAll(multiMap, 2, linkedSet(1, 3, 2));
		addAll(multiMap, 3, linkedSet(1, 3, 2));
		return multiMap;
	}

	private void assertOrder1(Map<Integer, Set<Integer>> multiMap) {
		Map<Integer, Set<Integer>> expected = new LinkedHashMap<>();
		addAll(expected, 1, linkedSet(1, 3, 2));
		addAll(expected, 3, linkedSet(1, 3, 2));
		addAll(expected, 2, linkedSet(1, 3, 2));
		assertSameOrder(expected, multiMap);
	}

	private <T> LinkedHashSet<T> linkedSet(T... entries) {
		return new LinkedHashSet<>(Arrays.asList(entries));
	}

	private void assertSameOrder(Map<?, ? extends Set<?>> expected, Map<?, ? extends Set<?>> actual) {
		assertEquals(expected.size(), actual.size());
		Iterator<Entry<?, ? extends Set<?>>> iteratorExpected = (Iterator) expected.entrySet().iterator();
		Iterator<Entry<?, ? extends Set<?>>> iteratorActual = (Iterator) actual.entrySet().iterator();
		while (iteratorExpected.hasNext()) {
			Entry<?, ? extends Set<?>> mapEntryExpected = iteratorExpected.next();
			Entry<?, ? extends Set<?>> mapEntryActual = iteratorActual.next();
			assertEquals(mapEntryExpected.getKey(), mapEntryActual.getKey());
			assertSameOrder(mapEntryExpected.getValue(), mapEntryActual.getValue());
		}
	}

	private void assertSameOrder(Set<?> expected, Set<?> actual) {
		assertEquals(expected.size(), actual.size());
		Iterator<?> iteratorExpected = expected.iterator();
		Iterator<?> iteratorActual = actual.iterator();
		while (iteratorExpected.hasNext()) {
			assertEquals(iteratorExpected.next(), iteratorActual.next());
		}
	}

}
