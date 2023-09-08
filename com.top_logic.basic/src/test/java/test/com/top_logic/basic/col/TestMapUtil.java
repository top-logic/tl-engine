/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static com.top_logic.basic.col.MapUtil.*;
import static com.top_logic.basic.col.factory.CollectionFactory.*;
import static com.top_logic.basic.col.factory.CollectionFactory.set;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static test.com.top_logic.basic.BasicTestCase.list;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections4.BidiMap;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.MapUtil;

/**
 * Tests methods in {@link MapUtil}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
@SuppressWarnings("javadoc")
public class TestMapUtil extends TestCase {

    /** 
     * Create a new TestMapUtil for given name.
     */
    public TestMapUtil(String name) {
        super(name);
    }

    /**
     * Test method for {@link com.top_logic.basic.col.MapUtil#addObject(java.util.Map, java.lang.Object, java.lang.Object)}.
     */
    public void testAddObjectAndRemoveObject() {
        Map<Object, List<Number>> theMap = new HashMap<>();

		Integer eins = Integer.valueOf(1);
		Integer zwei = Integer.valueOf(2);
		Integer drei = Integer.valueOf(3);

        MapUtil.addObject(theMap, "aaa", eins);
        MapUtil.addObject(theMap, "aaa", zwei);
        MapUtil.addObject(theMap, "ZZZ", drei);

        List<?> theList = theMap.get("aaa");
        assertEquals(eins, theList.get(0));
        assertEquals(zwei, theList.get(1));

        theList = theMap.get("ZZZ");
        assertEquals(drei, theList.get(0));

        MapUtil.removeObject(theMap, "aaa", eins);
        theList = theMap.get("aaa");
        assertEquals(zwei, theList.get(0));

        MapUtil.removeObject(theMap, "aaa", zwei);
        theList = theMap.get("aaa");
        assertTrue(theList.isEmpty());
    }

    public void testIndexMap() {
    	List<String> list = list("A", "B", "C");
    	assertTrue("Wanted to test random access list", list instanceof RandomAccess);
		assertEquals(2, createIndexMap(list).get("C").intValue());
		
		LinkedList<String> list2= new LinkedList<>(list);
		assertFalse("Wanted to test non random access list", list2 instanceof RandomAccess);
		assertEquals(2, createIndexMap(list2).get("C").intValue());
    }
    
	// Ignore the "unchecked" warning, as the worst that can happen is that this test fails.
	@SuppressWarnings("unchecked")
	public void testCreateMap() {
        List<String> theList = new ArrayList<>();
        theList.add("A");
        theList.add("B");
        theList.add("C");
        Map<Object, String> theMap = MapUtil.createValueMap(theList, StringServices.LOWER_CASE_MAPPING);
        assertEquals("A", theMap.get("a"));
        assertEquals("B", theMap.get("b"));
        assertEquals("C", theMap.get("c"));
        assertNull(theMap.get("A"));
        assertNull(theMap.get("B"));
        assertNull(theMap.get("C"));
        theMap = MapUtil.createKeyMap(theList, StringServices.LOWER_CASE_MAPPING);
        assertEquals("a", theMap.get("A"));
        assertEquals("b", theMap.get("B"));
        assertEquals("c", theMap.get("C"));
        assertNull(theMap.get("a"));
        assertNull(theMap.get("b"));
        assertNull(theMap.get("c"));

        List<String> theList2 = new ArrayList<>();
        theList2.add("X");
        theList2.add("Y");
        theMap = MapUtil.<Object,String, String, String>createMap(theList, theList2);
        assertEquals("X", theMap.get("A"));
        assertEquals("Y", theMap.get("B"));
        assertEquals(null, theMap.get("C"));
        assertNull(theMap.get("X"));
        assertNull(theMap.get("Y"));
        theMap = MapUtil.<Object,String, String, String>createMap(theList2, theList);
        assertEquals("A", theMap.get("X"));
        assertEquals("B", theMap.get("Y"));
        assertNull(theMap.get("A"));
        assertNull(theMap.get("B"));
        assertNull(theMap.get("C"));
        
        Object[] array1 = {"A", "B", "C"}; String[] array2 = {"X", "Y"};
        Map<Object, String> map1 = MapUtil.createMap(array1, array2);
        assertEquals("X", map1.get("A"));
        assertEquals("Y", map1.get("B"));
        assertEquals(null, map1.get("C"));
        assertNull(map1.get("X"));
        assertNull(map1.get("Y"));
        Map<String, Object> map2 = MapUtil.createMap(array2, array1);
        assertEquals("A", map2.get("X"));
        assertEquals("B", map2.get("Y"));
        assertNull(map2.get("A"));
        assertNull(map2.get("B"));
        assertNull(map2.get("C"));
    }

    /**
     * Test method for {@link com.top_logic.basic.col.MapUtil#addObjectToSortedSet(java.util.Map, java.lang.Object, java.lang.Object, java.util.Comparator)}.
     */
    public void testAddObjectToSet() {
        Map<Object, SortedSet<Object>> theMap = new HashMap<>();

		Integer eins = Integer.valueOf(1);
		Integer zwei = Integer.valueOf(2);
		Integer drei = Integer.valueOf(3);

        MapUtil.addObjectToSortedSet(theMap, "brbr"     , eins, null);
        MapUtil.addObjectToSortedSet(theMap, "brbr"     , zwei, null);
        MapUtil.addObjectToSortedSet(theMap, "UksaDunks", drei, ComparableComparator.INSTANCE);

        Set<Object> theSet = theMap.get("brbr");
        assertTrue(theSet.contains(eins));
        assertTrue(theSet.contains(zwei));

        theSet = theMap.get("UksaDunks");
        assertTrue(theSet.contains(drei));

        MapUtil.removeObject(theMap, "brbr", eins);
        theSet = theMap.get("brbr");
        assertFalse(theSet.contains(eins));
        assertTrue(theSet.contains(zwei));

        MapUtil.removeObject(theMap, "brbr", zwei);
        theSet = theMap.get("brbr");
        assertTrue(theSet.isEmpty());
    }
    
    public void testShareObject() {
        Map<String, String> theSharedObjects = new HashMap<>();
        
        // Create s1 and s2 which are equal but not identical.
        StringBuffer sb = new StringBuffer();
        String s;
        sb.append('A');
        
        String s1 = "A";
        String s2 = sb.toString();
        
        assertTrue (s1.equals(s2));
        assertFalse(s2 == s1);

        s = MapUtil.shareObject(theSharedObjects, s1);
        assertTrue (s1 == s);
        assertFalse(s2 == s);
        assertTrue(s1.equals(s));
        assertTrue(s2.equals(s));
        s = MapUtil.shareObject(theSharedObjects, s2);
        assertTrue (s1 == s);
        assertFalse(s2 == s);
        assertTrue (s1.equals(s));
        assertTrue (s2.equals(s));
        s = MapUtil.shareObject(theSharedObjects, null);
        assertTrue(null == s);
    }

	public void testGetAll() {
		List<String> emptyListOfValues = Collections.emptyList();
		assertEquals(emptyListOfValues, getFromAll(null, null));
		assertEquals(emptyListOfValues, getFromAll(null, "firstKey"));
		List<Map<String, String>> emptyListOfMaps = Collections.emptyList();
		assertEquals(emptyListOfValues, getFromAll(emptyListOfMaps, null));
		assertEquals(emptyListOfValues, getFromAll(emptyListOfMaps, "firstKey"));

		Map<String, String> mapOne = new MapBuilder<String, String>()
			.put(null, null)
			.put("one", "A")
			.put("two", "B")
			.put("three", null)
			.toMap();
		Map<String, String> mapTwo = new MapBuilder<String, String>()
			.put(null, "null")
			.put("one", "1")
			.put("two", null)
			.put("three", null)
			.put("four", "four")
			.toMap();
		@SuppressWarnings("unchecked")
		List<? extends Map<?, ?>> listOfMaps = CollectionUtil.createList(mapOne, null, mapTwo);
		List<String> listOfValuesForNull = CollectionUtil.createList(null, null, "null");
		List<String> listOfValuesForOne = CollectionUtil.createList("A", null, "1");
		List<String> listOfValuesForTwo = CollectionUtil.createList("B", null, null);
		List<String> listOfValuesForThree = CollectionUtil.createList(null, null, null);
		List<String> listOfValuesForFour = CollectionUtil.createList(null, null, "four");
		assertEquals(listOfValuesForNull, getFromAll(listOfMaps, null));
		assertEquals(listOfValuesForOne, getFromAll(listOfMaps, "one"));
		assertEquals(listOfValuesForTwo, getFromAll(listOfMaps, "two"));
		assertEquals(listOfValuesForThree, getFromAll(listOfMaps, "three"));
		assertEquals(listOfValuesForFour, getFromAll(listOfMaps, "four"));
		
	}

	/** Test for {@link MapUtil#parse(String, String, String)} when the input is the empty String. */
	public void testParseEmptyString() throws ParseException {
		Map<String, String> expected = Collections.emptyMap();
		Map<String, String> actual = parse("", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when the input represents a single
	 * entry.
	 */
	public void testParseOneEntry() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "A");
		Map<String, String> actual = parse("a:A", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when the input represents two entries.
	 */
	public void testParseTwoEntries() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "A");
		expected.put("b", "B");
		Map<String, String> actual = parse("a:A,b:B", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when the input represents three
	 * entries.
	 */
	public void testParseThreeEntries() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "A");
		expected.put("b", "B");
		expected.put("c", "C");
		Map<String, String> actual = parse("a:A,b:B,c:C", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when the key-value and the entry
	 * separator are more than one character long.
	 */
	public void testParseMultiCharSeparators() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "A");
		expected.put("b", "B");
		expected.put("c", "C");
		Map<String, String> actual = parse("a=>A&&b=>B&&c=>C", "=>", "&&");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when the input contains only spaces.
	 */
	public void testParseTrimEmptyString() throws ParseException {
		Map<String, String> expected = Collections.emptyMap();
		Map<String, String> actual = parse("  ", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when the input represents a single
	 * entry and all tokens are separated by spaces.
	 */
	public void testParseTrimOneEntry() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "A");
		Map<String, String> actual = parse(" a : A ", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when the input represents two entries
	 * and all tokens are separated by spaces.
	 */
	public void testParseTrimTwoEntries() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "A");
		expected.put("b", "B");
		Map<String, String> actual = parse(" a : A , b : B ", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when the input represents three
	 * entries and all tokens are separated by spaces.
	 */
	public void testParseEmptyKey() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "A");
		expected.put("", "B");
		expected.put("c", "C");
		Map<String, String> actual = parse("a:A,:B,c:C", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when one of the values is the empty
	 * String.
	 */
	public void testParseEmptyValue() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "A");
		expected.put("b", "");
		expected.put("c", "C");
		Map<String, String> actual = parse("a:A,b:,c:C", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when one of the keys is the empty
	 * String.
	 */
	public void testParseEmptyKeyValue() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "A");
		expected.put("", "");
		expected.put("c", "C");
		Map<String, String> actual = parse("a:A,:,c:C", ":", ",");
		assertEquals(expected, actual);
	}

	/**
	 * Test for {@link MapUtil#parse(String, String, String)} when the only entry has the empty
	 * String as key and value.
	 */
	public void testParseOnlyEmptyKeyValue() throws ParseException {
		Map<String, String> expected = new HashMap<>();
		expected.put("", "");
		Map<String, String> actual = parse(":", ":", ",");
		assertEquals(expected, actual);
	}

	public void testJoin() {
		assertEquals(emptyMap(), join(emptyMap(), emptyMap()));
		Map<Integer, Character> nonEmptyMap = createNonEmptyMap();
		assertEquals(nonEmptyMap, join(nonEmptyMap, emptyMap()));
		assertEquals(nonEmptyMap, join(emptyMap(), nonEmptyMap));
		assertEquals(nonEmptyMap, join(nonEmptyMap, nonEmptyMap));
		Map<Integer, Character> conflictingMap = createConflictingMap();
		Map<Integer, Character> mergedMap = createMergedMap();
		assertEquals(mergedMap, join(nonEmptyMap, conflictingMap));
	}

	public void testJoinInto() {
		LinkedHashMap<Integer, Character> nonEmptyMap = createNonEmptyMap();
		LinkedHashMap<Integer, Character> conflictingMap = createConflictingMap();
		LinkedHashMap<Integer, Character> mergedMap = createMergedMap();
		LinkedHashMap<Integer, Character> actualResult = linkedMap();
		LinkedHashMap<Integer, Character> returnValue = joinInto(actualResult, asList(nonEmptyMap, conflictingMap));
		assertEquals(mergedMap, actualResult);
		assertSame(actualResult, returnValue);
	}

	private LinkedHashMap<Integer, Character> createNonEmptyMap() {
		return linkedMap(mapEntry(1, 'a'), mapEntry(2, 'b'));
	}

	private LinkedHashMap<Integer, Character> createConflictingMap() {
		return linkedMap(mapEntry(1, 'z'), mapEntry(3, 'x'));
	}

	private LinkedHashMap<Integer, Character> createMergedMap() {
		return linkedMap(mapEntry(1, 'z'), mapEntry(2, 'b'), mapEntry(3, 'x'));
	}

	public void testCreateSubMap() {
		Map<Integer, String> source = map(mapEntry(1, "a"), mapEntry(2, "b"), mapEntry(3, "b"), mapEntry(4, "b"));
		Set<Integer> keys = set(1, 3);
		Map<Integer, String> actualSubMap = createSubMap(source, keys);
		assertEquals(map(mapEntry(1, "a"), mapEntry(3, "b")), actualSubMap);
	}

	public void testCreateSubMapMissingKey() {
		Map<Integer, String> source = map(mapEntry(1, "a"), mapEntry(2, "b"));
		Set<Integer> keys = set(5);
		try {
			createSubMap(source, keys);
		} catch (NoSuchElementException ex) {
			// good
			return;
		}
		fail("Requesting a key that does not exist has to fail,"
			+ " just like ArrayList.subList(...) fails for a non-existing index.");
	}

	public void testCreateSubMapNoKeys() {
		Map<Integer, String> source = map(mapEntry(1, "a"), mapEntry(2, "b"), mapEntry(3, "b"), mapEntry(4, "b"));
		Set<Integer> keys = set();
		Map<Integer, String> actualSubMap = createSubMap(source, keys);
		assertEquals(map(), actualSubMap);
	}

	public void testCreateSubMapEmptySource() {
		Map<Integer, String> source = map();
		Set<Integer> keys = set();
		Map<Integer, String> actualSubMap = createSubMap(source, keys);
		assertEquals(map(), actualSubMap);
		actualSubMap.put(1, "a"); // check that the sub-map is not immutable
	}

	public void testCreateSubMapSpecialResultType() {
		Map<Integer, String> source = map(mapEntry(1, "a"), mapEntry(2, "b"));
		Set<Integer> keys = set(1);
		LinkedHashMap<Number, CharSequence> result = linkedMap();
		/* Explicitly state a specialized return value type to make sure future changes don't break
		 * that the return value can be specialized on the Map, key, and value type. */
		LinkedHashMap<Number, CharSequence> returnValue = createSubMap(source, keys, result);
		assertEquals(map(mapEntry(1, "a")), returnValue);
		assertSame(result, returnValue);
	}

	public void testEmptyBidiMap() {
		BidiMap<Object, Object> emptyBidiMap = emptyBidiMap();
		BidiMap<Object, Object> secondEmptyMap = emptyBidiMap();
		assertSame("There must only be one empty BidiMap", emptyBidiMap, secondEmptyMap);
		assertTrue("EmptyBidiMap must be empty.", emptyBidiMap.isEmpty());
		try {
			emptyBidiMap.put("key", "value");
			fail("Empty map instance must be unmodifiable.");
		} catch (Exception ex) {
			// expected.
		}
		try {
			emptyBidiMap.clear();
			emptyBidiMap.remove("key");
		} catch (Exception ex) {
			BasicTestCase.fail("Change operations that actually do nothing must not fail.", ex);
		}
	}

	public static Test suite() {
        return new TestSuite(TestMapUtil.class);
    }

}
