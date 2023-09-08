/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.MapBuilder;

/**
 * Test case for {@link MapBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestMapBuilder extends TestCase {

	public void testUnmodifiable() {
		MapBuilder<Integer, Integer> builder = new MapBuilder<>();
		builder.put(1, 1);
		Map<Integer, Integer> unmodifiableMap = builder.toUnmodifiableMap();
		assertEquals(Collections.singletonMap(1, 1), unmodifiableMap);
		try {
			unmodifiableMap.put(2, 1);
			fail("Map can not be modified.");
		} catch (RuntimeException ex) {
			// expected.
		}
		builder.put(3, 2);
		assertEquals("Formerly created unmodifiable map did not changed.", Collections.singletonMap(1, 1),
			unmodifiableMap);
	}

	public void testLinkedMap() {
		List<Integer> list = BasicTestCase.list(1, 2, 3, 4, 5, 6, 7, 8, 9);

		Random rnd = new Random(123456789L);
		for (int i = 0; i < 100; i++) {
			Collections.shuffle(list, rnd);
			MapBuilder<Integer, Integer> mapBuilder = new MapBuilder<>(true);
			int initialMapSize = list.size() / 2;
			for (int j = 0; j < initialMapSize; j++) {
				mapBuilder.put(list.get(j), list.get(j));
			}
			assertEquals("Insert order was not prepared.", list.subList(0, initialMapSize),
				BasicTestCase.toList(mapBuilder.toMap().keySet()));
			assertEquals("Insert order was not prepared.", list.subList(0, initialMapSize),
				BasicTestCase.toList(mapBuilder.toUnmodifiableMap().keySet()));

			// Fill map in two steps to force copy of buffer.
			for (int j = initialMapSize; j < list.size(); j++) {
				mapBuilder.put(list.get(j), list.get(j));
			}
			assertEquals("Insert order was not prepared.", list, BasicTestCase.toList(mapBuilder.toMap().keySet()));
			assertEquals("Insert order was not prepared.", list,
				BasicTestCase.toList(mapBuilder.toUnmodifiableMap().keySet()));
		}
	}

	public void testCreate() {
		HashMap<String, String> map1 = new HashMap<>();
		map1.put("a", "1");
		map1.put("b", "2");
		assertEquals(map1, new MapBuilder<String, String>().put("a", "1").put("b", "2").toMap());

        HashMap<String, String> map2 = new HashMap<>();
        map2.put("a", "1");
        map2.put("b", "2");
        map2.put("c", "3");
        map2.put("d", "4");
        
		MapBuilder<String, String> theBuilder =
			new MapBuilder<String, String>().putAll(map1).put("c", "3").put("d", "4");
        Map<String, String> map4 = theBuilder.toMap();
        assertEquals(map2, map4);
	}

	public void testPutAllOnce() {
		Map<String, String> sourceA = new HashMap<>();
		sourceA.put("a", "1");
		sourceA.put("b", "2");
		Map<String, String> actualResult = new MapBuilder<String, String>().putAll(sourceA).toMap();
		Map<String, String> expectedResult = new HashMap<>(sourceA);
		assertEquals(expectedResult, actualResult);
	}

	public void testPutAllTwice() {
		Map<String, String> sourceA = new HashMap<>();
		sourceA.put("a", "1");
		sourceA.put("b", "2");
		Map<String, String> sourceB = new HashMap<>();
		sourceB.put("c", "3");
		sourceB.put("d", "4");
		Map<String, String> actualResult = new MapBuilder<String, String>().putAll(sourceA).putAll(sourceB).toMap();
		Map<String, String> expectedResult = new HashMap<>(sourceA);
		expectedResult.putAll(sourceB);
		assertEquals(expectedResult, actualResult);
	}

	public void testPutAllWithDuplicateKeys() {
		Map<String, String> sourceA = new HashMap<>();
		sourceA.put("a", "1");
		sourceA.put("b", "2");
		Map<String, String> sourceB = new HashMap<>();
		sourceB.put("c", "3");
		sourceB.put("d", "4");
		Map<String, String> duplicateKeySource = new HashMap<>();
		duplicateKeySource.put("b", "2");
		duplicateKeySource.put("e", "5");
		MapBuilder<String, String> actualResult = new MapBuilder<String, String>().putAll(sourceA).putAll(sourceB);
		try {
			actualResult.putAll(duplicateKeySource);
			fail("Must report duplicate key exception");
		} catch (IllegalArgumentException expectedException) {
			// Expected
		}
	}

	public void testPutAllWithSharing() {
		Map<String, String> sourceA = new HashMap<>();
		sourceA.put("a", "1");
		sourceA.put("b", "2");
		Map<String, String> sourceB = new HashMap<>();
		sourceB.put("c", "3");
		sourceB.put("d", "4");
		MapBuilder<String, String> mapBuilder = new MapBuilder<String, String>().putAll(sourceA);
		Map<String, String> firstActualResult = mapBuilder.toMap();
		Map<String, String> firstExpectedResult = new HashMap<>(sourceA);
		assertEquals(firstExpectedResult, firstActualResult);
		
		Map<String, String> secondActualResult = mapBuilder.putAll(sourceB).toMap();
		Map<String, String> secondExpectedResult = new HashMap<>(sourceA);
		secondExpectedResult.putAll(sourceB);
		assertEquals(firstExpectedResult, firstActualResult);
		assertEquals(secondExpectedResult, secondActualResult);
	}

	public void testDuplicateKey() {
		try {
			new MapBuilder<String, String>().put("a", "1").put("a", "2");
			fail("Must report duplicate key exception");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}
	
	public void testSharing() {
		MapBuilder<String, String> builder = new MapBuilder<>();

		HashMap<String, String> map1 = new HashMap<>();
		map1.put("a", "1");

		HashMap<String, String> map2 = new HashMap<>();
		map2.put("a", "1");
		map2.put("b", "2");
		
		Map<String, String> builtMap1 = builder.put("a", "1").toMap();
		Map<String, String> builtMap2 = builder.put("b", "2").toMap();

		// Check that the first built map is not overwritten by the following
		// add.
		assertEquals(map1, builtMap1);
		assertEquals(map2, builtMap2);
	}
	
	public void testEquality() {
		MapBuilder<String, String> builder = new MapBuilder<String, String>().put("a", "1");
		Map<String, String> builtMap1 = builder.toMap();
		Map<String, String> builtMap2 = builder.toMap();
		
		// Assert that both built maps are equal but not identical.
		assertEquals(builtMap1, builtMap2);
		assertNotSame(builtMap1, builtMap2);
		
		Map<String, String> builtMap3 = builder.put("b", "2").toMap();
		assertNotSame(builtMap1, builtMap3);
		assertNotSame(builtMap2, builtMap3);
	}
	
    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        return new TestSuite(TestMapBuilder.class);
    }

}
