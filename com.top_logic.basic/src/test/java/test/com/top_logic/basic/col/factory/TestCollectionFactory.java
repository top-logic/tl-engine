/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.factory;

import static com.top_logic.basic.col.factory.CollectionFactory.*;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.TestCase;

import com.top_logic.basic.col.factory.CollectionFactory;

/**
 * {@link TestCase} for the {@link CollectionFactory}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestCollectionFactory extends TestCase {

	private static final long SEED = 1234567890987654321L;

	private static final Comparator<Object> COMPARATOR = Collections.reverseOrder();

	public void testList() {
		Collection<Integer> empty = new ArrayList<>();
		assertEquals(empty, list());
		assertEquals(empty, list(empty));
		assertEquals(empty, list(empty.toArray()));

		Collection<Integer> singleton = new ArrayList<>();
		singleton.add(1);
		assertEquals(singleton, list(1));
		assertEquals(singleton, list(singleton));
		assertEquals(singleton, list(singleton.toArray()));

		Collection<Integer> multi = new ArrayList<>();
		multi.add(1);
		multi.add(2);
		multi.add(3);
		assertEquals(multi, list(1, 2, 3));
		assertEquals(multi, list(multi));
		assertEquals(multi, list(multi.toArray()));
	}

	public void testLinkedList() {
		Collection<Integer> empty = new LinkedList<>();
		assertEquals(empty, linkedList());
		assertEquals(empty, linkedList(empty));
		assertEquals(empty, linkedList(empty.toArray()));

		Collection<Integer> singleton = new LinkedList<>();
		singleton.add(1);
		assertEquals(singleton, linkedList(1));
		assertEquals(singleton, linkedList(singleton));
		assertEquals(singleton, linkedList(singleton.toArray()));

		Collection<Integer> multi = new LinkedList<>();
		multi.add(1);
		multi.add(2);
		multi.add(3);
		assertEquals(multi, linkedList(1, 2, 3));
		assertEquals(multi, linkedList(multi));
		assertEquals(multi, linkedList(multi.toArray()));
	}

	public void testSet() {
		Collection<Integer> empty = new HashSet<>();
		assertEquals(empty, set());
		assertEquals(empty, set(empty));
		assertEquals(empty, set(empty.toArray()));

		Collection<Integer> singleton = new HashSet<>();
		singleton.add(1);
		assertEquals(singleton, set(1));
		assertEquals(singleton, set(singleton));
		assertEquals(singleton, set(singleton.toArray()));

		Collection<Integer> multi = new HashSet<>();
		multi.add(1);
		multi.add(2);
		multi.add(3);
		assertEquals(multi, set(1, 2, 3));
		assertEquals(multi, set(multi));
		assertEquals(multi, set(multi.toArray()));
	}

	public void testLinkedSet() {
		Collection<Integer> empty = new LinkedHashSet<>();
		assertEquals(empty, linkedSet());
		assertEquals(empty, linkedSet(empty));
		assertEquals(empty, linkedSet(empty.toArray()));

		Collection<Integer> singleton = new LinkedHashSet<>();
		singleton.add(1);
		assertEquals(singleton, linkedSet(1));
		assertEquals(singleton, linkedSet(singleton));
		assertEquals(singleton, linkedSet(singleton.toArray()));

		Collection<Integer> multi = new LinkedHashSet<>();
		multi.add(1);
		multi.add(2);
		multi.add(3);
		assertEquals(multi, linkedSet(1, 2, 3));
		assertEquals(multi, linkedSet(multi));
		assertEquals(multi, linkedSet(multi.toArray()));
	}

	/**
	 * This should not test the implementation of the {@link LinkedHashSet}, but test that the
	 * {@link CollectionFactory} does not accidentally lose the order.
	 */
	public void testLinkedSetKeepsOrder() {
		Random random = new Random(SEED);
		List<Integer> expected = list();
		for (int i = 0; i < 1000; i++) {
			expected.add(random.nextInt());
		}
		assertEquals(expected, list(linkedSet(expected)));
		assertEquals(expected, list(linkedSet(expected.toArray())));
	}

	public void testTreeSetWithoutComparator() {
		Collection<Integer> empty = new TreeSet<>();
		assertEquals(empty, treeSet());
		assertEquals(empty, treeSet(empty));
		assertEquals(empty, treeSet(empty.toArray(new Integer[0])));

		Collection<Integer> singleton = new TreeSet<>();
		singleton.add(1);
		assertEquals(singleton, treeSet(1));
		assertEquals(singleton, treeSet(singleton));
		assertEquals(singleton, treeSet(singleton.toArray(new Integer[0])));

		Collection<Integer> multi = new TreeSet<>();
		multi.add(1);
		multi.add(2);
		multi.add(3);
		assertEquals(multi, treeSet(1, 2, 3));
		assertEquals(multi, treeSet(multi));
		assertEquals(multi, treeSet(multi.toArray(new Integer[0])));
	}

	public void testTreeSetWithComparator() {
		Collection<Integer> empty = new TreeSet<>();
		assertEquals(empty, treeSet(COMPARATOR));
		assertEquals(empty, treeSet(COMPARATOR, empty));
		assertEquals(empty, treeSet(COMPARATOR, empty.toArray(new Integer[0])));

		Collection<Integer> singleton = new TreeSet<>();
		singleton.add(1);
		assertEquals(singleton, treeSet(COMPARATOR, 1));
		assertEquals(singleton, treeSet(COMPARATOR, singleton));
		assertEquals(singleton, treeSet(COMPARATOR, singleton.toArray(new Integer[0])));

		Collection<Integer> multi = new TreeSet<>();
		multi.add(1);
		multi.add(2);
		multi.add(3);
		assertEquals(multi, treeSet(COMPARATOR, 1, 2, 3));
		assertEquals(multi, treeSet(COMPARATOR, multi));
		assertEquals(multi, treeSet(COMPARATOR, multi.toArray(new Integer[0])));
	}

	/**
	 * This should not test the implementation of the {@link TreeSet}, but test that the
	 * {@link CollectionFactory} has set the given {@link Comparator} on it.
	 */
	public void testTreeSetHasComparator() {
		Collection<Integer> source = list(1, 2);

		assertEquals(list(1, 2), list(treeSet(source)));
		assertEquals(list(2, 1), list(treeSet(COMPARATOR, source)));

		assertEquals(list(1, 2), list(treeSet(source.toArray(new Integer[0]))));
		assertEquals(list(2, 1), list(treeSet(COMPARATOR, source.toArray(new Integer[0]))));
	}

	@SuppressWarnings("unchecked")
	public void testMap() {
		Map<Integer, String> empty = new HashMap<>();
		assertEquals(empty, map());
		assertEquals(empty, map(empty));
		assertEquals(empty, map(empty.entrySet()));
		assertEquals(empty, map(empty.entrySet().toArray(new Entry[0])));

		Map<Integer, String> singleton = new HashMap<>();
		singleton.put(1, "1");
		assertEquals(singleton, map(singleton));
		assertEquals(singleton, map(singleton.entrySet()));
		assertEquals(singleton, map(singleton.entrySet().toArray(new Entry[0])));

		Map<Integer, String> multi = new HashMap<>();
		multi.put(1, "1");
		multi.put(2, "2");
		multi.put(3, "3");
		assertEquals(multi, map(multi));
		assertEquals(multi, map(multi.entrySet()));
		assertEquals(multi, map(multi.entrySet().toArray(new Entry[0])));
	}

	@SuppressWarnings("unchecked")
	public void testLinkedMap() {
		Map<Integer, String> empty = new LinkedHashMap<>();
		assertEquals(empty, linkedMap());
		assertEquals(empty, linkedMap(empty));
		assertEquals(empty, linkedMap(empty.entrySet()));
		assertEquals(empty, linkedMap(empty.entrySet().toArray(new Entry[0])));

		Map<Integer, String> singleton = new LinkedHashMap<>();
		singleton.put(1, "1");
		assertEquals(singleton, linkedMap(singleton));
		assertEquals(singleton, linkedMap(singleton.entrySet()));
		assertEquals(singleton, linkedMap(singleton.entrySet().toArray(new Entry[0])));

		Map<Integer, String> multi = new LinkedHashMap<>();
		multi.put(1, "1");
		multi.put(2, "2");
		multi.put(3, "3");
		assertEquals(multi, linkedMap(multi));
		assertEquals(multi, linkedMap(multi.entrySet()));
		assertEquals(multi, linkedMap(multi.entrySet().toArray(new Entry[0])));
	}

	/**
	 * This should not test the implementation of the {@link LinkedHashMap}, but test that the
	 * {@link CollectionFactory} does not accidentally lose the order.
	 */
	@SuppressWarnings("unchecked")
	public void testLinkedMapKeepsOrder() {
		Random random = new Random(SEED);
		List<Map.Entry<Integer, String>> expected = list();
		for (int i = 0; i < 1000; i++) {
			int number = random.nextInt();
			expected.add(new SimpleImmutableEntry<>(number, Integer.toString(number)));
		}
		assertEquals(expected, list(linkedMap(expected).entrySet()));
		/* The first assert checks whether "linkedMap(Collection)" keeps the order. After that is
		 * checked, we can use that method to construct a Map to test whether "linkedMap(Map)" keeps
		 * the order. */
		assertEquals(expected, list(linkedMap(linkedMap(expected)).entrySet()));
		assertEquals(expected, list(linkedMap(expected.toArray(new Entry[0])).entrySet()));
	}

	@SuppressWarnings("unchecked")
	public void testTreeMapWithoutComparator() {
		Map<Integer, String> empty = new TreeMap<>();
		assertEquals(empty, treeMap());
		assertEquals(empty, treeMap(empty));
		assertEquals(empty, treeMap(empty.entrySet()));
		assertEquals(empty, treeMap(empty.entrySet().toArray(new Entry[0])));

		Map<Integer, String> singleton = new TreeMap<>();
		singleton.put(1, "1");
		assertEquals(singleton, treeMap(singleton));
		assertEquals(singleton, treeMap(singleton.entrySet()));
		assertEquals(singleton, treeMap(singleton.entrySet().toArray(new Entry[0])));

		Map<Integer, String> multi = new TreeMap<>();
		multi.put(1, "1");
		multi.put(2, "2");
		multi.put(3, "3");
		assertEquals(multi, treeMap(multi));
		assertEquals(multi, treeMap(multi.entrySet()));
		assertEquals(multi, treeMap(multi.entrySet().toArray(new Entry[0])));
	}

	@SuppressWarnings("unchecked")
	public void testTreeMapWithComparator() {
		Map<Integer, String> empty = new TreeMap<>();
		assertEquals(empty, treeMap(COMPARATOR));
		assertEquals(empty, treeMap(COMPARATOR, empty));
		assertEquals(empty, treeMap(COMPARATOR, empty.entrySet()));
		assertEquals(empty, treeMap(COMPARATOR, empty.entrySet().toArray(new Entry[0])));

		Map<Integer, String> singleton = new TreeMap<>();
		singleton.put(1, "1");
		assertEquals(singleton, treeMap(COMPARATOR, singleton));
		assertEquals(singleton, treeMap(COMPARATOR, singleton.entrySet()));
		assertEquals(singleton, treeMap(COMPARATOR, singleton.entrySet().toArray(new Entry[0])));

		Map<Integer, String> multi = new TreeMap<>();
		multi.put(1, "1");
		multi.put(2, "2");
		multi.put(3, "3");
		assertEquals(multi, treeMap(COMPARATOR, multi));
		assertEquals(multi, treeMap(COMPARATOR, multi.entrySet()));
		assertEquals(multi, treeMap(COMPARATOR, multi.entrySet().toArray(new Entry[0])));
	}

	/**
	 * This should not test the implementation of the {@link TreeMap}, but test that the
	 * {@link CollectionFactory} has set the given {@link Comparator} on it.
	 */
	@SuppressWarnings("unchecked")
	public void testTreeMapHasComparator() {
		Map<Integer, String> source = map(mapEntry(1, "1"), mapEntry(2, "2"));

		assertEquals(list(1, 2), list(treeMap(source).keySet()));
		assertEquals(list(2, 1), list(treeMap(COMPARATOR, source).keySet()));

		assertEquals(list(1, 2), list(treeMap(source.entrySet()).keySet()));
		assertEquals(list(2, 1), list(treeMap(COMPARATOR, source.entrySet()).keySet()));

		assertEquals(list(1, 2), list(treeMap(source.entrySet().toArray(new Entry[0])).keySet()));
		assertEquals(list(2, 1), list(treeMap(COMPARATOR, source.entrySet().toArray(new Entry[0])).keySet()));
	}

	public void testMapEntry() {
		int key = 123;
		String value = "Hello World!";
		SimpleImmutableEntry<Integer, String> expected = new SimpleImmutableEntry<>(key, value);
		assertEquals(expected, mapEntry(key, value));
	}

	public void testAddAllVarargs() {
		/* Check the type parameters: It has to be possible to give a collection of a supertype of
		 * the elements to add. */
		List<Number> numbers = CollectionFactory.<Number> list(1, 2, 3);
		Integer[] ints = { 5, 2, 0 };
		/* Check the type parameters: The result has to be of the type of the first parameter. */
		List<Number> result = addAllTo(numbers, ints);
		assertSame(numbers, result);
		assertEquals(list(1, 2, 3, 5, 2, 0), numbers);
	}

	public void testAddAllCollection() {
		/* Check the type parameters: It has to be possible to give a collection of a supertype of
		 * the elements to add. */
		List<Number> oldEntries = CollectionFactory.<Number> list(1, 2, 3);
		Collection<Integer> newEntries = Arrays.asList(5, 2, 0);
		/* Check the type parameters: The result has to be of the type of the first parameter: Both
		 * its type itself ("List") and its type parameters. */
		List<Number> result = addAllTo(oldEntries, newEntries);
		assertSame(oldEntries, result);
		ArrayList<Integer> expected = list(1, 2, 3, 5, 2, 0);
		assertEquals(expected, oldEntries);
	}

	public void testPutAllVarargs() {
		/* Check the type parameters: It has to be possible to give a collection of a supertype of
		 * the elements to add. */
		HashMap<Number, CharSequence> oldEntries = CollectionFactory.<Number, CharSequence> map(
			mapEntry(1, "1"),
			mapEntry(2, "2"),
			mapEntry(3, "3"));
		Map<Integer, String> newEntries = map(
			mapEntry(5, "5"),
			mapEntry(2, "2"),
			mapEntry(0, "0"));
		/* Check the type parameters: The result has to be of the type of the first parameter: Both
		 * its type itself ("HashMap") and its type parameters. */
		@SuppressWarnings("unchecked")
		HashMap<Number, CharSequence> result = putAll(oldEntries, newEntries.entrySet().toArray(new Entry[0]));
		assertSame(oldEntries, result);
		Object expected = map(
			mapEntry(1, "1"),
			mapEntry(2, "2"),
			mapEntry(3, "3"),
			mapEntry(5, "5"),
			mapEntry(2, "2"),
			mapEntry(0, "0"));
		assertEquals(expected, oldEntries);
	}

	public void testPutAllCollection() {
		/* Check the type parameters: It has to be possible to give a collection of a supertype of
		 * the elements to add. */
		HashMap<Number, CharSequence> oldEntries = CollectionFactory.<Number, CharSequence> map(
			mapEntry(1, "1"),
			mapEntry(2, "2"),
			mapEntry(3, "3"));
		Map<Integer, String> newEntries = map(
			mapEntry(5, "5"),
			mapEntry(2, "2"),
			mapEntry(0, "0"));
		/* Check the type parameters: The result has to be of the type of the first parameter: Both
		 * its type itself ("HashMap") and its type parameters. */
		HashMap<Number, CharSequence> result = putAll(oldEntries, newEntries.entrySet());
		assertSame(oldEntries, result);
		Object expected = map(
			mapEntry(1, "1"),
			mapEntry(2, "2"),
			mapEntry(3, "3"),
			mapEntry(5, "5"),
			mapEntry(2, "2"),
			mapEntry(0, "0"));
		assertEquals(expected, oldEntries);
	}

	public void testPutAllMap() {
		/* Check the type parameters: It has to be possible to give a collection of a supertype of
		 * the elements to add. */
		HashMap<Number, CharSequence> oldEntries = CollectionFactory.<Number, CharSequence> map(
			mapEntry(1, "1"),
			mapEntry(2, "2"),
			mapEntry(3, "3"));
		Map<Integer, String> newEntries = map(
			mapEntry(5, "5"),
			mapEntry(2, "2"),
			mapEntry(0, "0"));
		/* Check the type parameters: The result has to be of the type of the first parameter: Both
		 * its type itself ("HashMap") and its type parameters. */
		HashMap<Number, CharSequence> result = putAll(oldEntries, newEntries);
		assertSame(oldEntries, result);
		Object expected = map(
			mapEntry(1, "1"),
			mapEntry(2, "2"),
			mapEntry(3, "3"),
			mapEntry(5, "5"),
			mapEntry(2, "2"),
			mapEntry(0, "0"));
		assertEquals(expected, oldEntries);
	}

	public void testNullSafety() {
		assertEquals(list(), list((Object[]) null));
		assertEquals(list(), list((Collection<?>) null));

		assertEquals(linkedList(), linkedList((Object[]) null));
		assertEquals(linkedList(), linkedList((Collection<?>) null));

		assertEquals(set(), set((Object[]) null));
		assertEquals(set(), set((Collection<?>) null));

		assertEquals(linkedSet(), linkedSet((Object[]) null));
		assertEquals(linkedSet(), linkedSet((Collection<?>) null));

		assertEquals(treeSet(), treeSet((String[]) null));
		assertEquals(treeSet(), treeSet((Collection<String>) null));
		assertEquals(treeSet(), treeSet(COMPARATOR, (Object[]) null));
		assertEquals(treeSet(), treeSet(COMPARATOR, (Collection<Object>) null));

		assertEquals(map(), map((Entry<?, ?>[]) null));
		assertEquals(map(), map((Collection<Entry<?, ?>>) null));
		assertEquals(map(), map((Map<?, ?>) null));

		assertEquals(linkedMap(), linkedMap((Entry<?, ?>[]) null));
		assertEquals(linkedMap(), linkedMap((Collection<Entry<?, ?>>) null));
		assertEquals(linkedMap(), linkedMap((Map<?, ?>) null));

		assertEquals(treeMap(), treeMap((Entry<String, ?>[]) null));
		assertEquals(treeMap(), treeMap((Collection<Entry<String, ?>>) null));
		assertEquals(treeMap(), treeMap((Map<String, ?>) null));
		assertEquals(treeMap(), treeMap(COMPARATOR, (Entry<?, ?>[]) null));
		assertEquals(treeMap(), treeMap(COMPARATOR, (Collection<Entry<?, ?>>) null));
		assertEquals(treeMap(), treeMap(COMPARATOR, (Map<?, ?>) null));
	}

	public void testCalcRequiredCapacity() {
		try {
			calcRequiredCapacity(-1);
		} catch (IllegalArgumentException ex) {
			return; // Expected
		}
		fail("A negative size has to cause an IllegalArgumentException.");
	}

}
