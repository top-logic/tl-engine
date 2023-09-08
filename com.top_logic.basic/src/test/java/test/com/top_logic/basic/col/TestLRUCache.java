/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.LRUCache;

/**
 * Test for{@link LRUCache}. 
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestLRUCache extends TestCase {

	private static class CacheUpcomingDeletionsLRUCache<K, V> extends LRUCache<K, V> {

		Map<Object, V> _removed = new HashMap<>();

		public CacheUpcomingDeletionsLRUCache(int aMaxCut) {
			super(aMaxCut);
		}

		@Override
		protected void handleUpcomingRemove(Object key, V value) {
			super.handleUpcomingRemove(key, value);
			_removed.put(key, value);
		}

	}

	/**
	 * Constructor to conduct a special test.
	 *
	 * @param name name of the test to execute.
	 */
	public TestLRUCache(String name) {
		super(name);
	}

	// Test methodes

	/**
	 * Tests call of callback method for removed objects.
	 */
	public void testRemovedImplicit() {
		CacheUpcomingDeletionsLRUCache<Integer, Integer> cache = new CacheUpcomingDeletionsLRUCache<>(2);
		cache.put(1, 1);
		assertTrue(cache._removed.isEmpty());
		cache.put(2, 2);
		assertTrue(cache._removed.isEmpty());
		cache.put(3, 3);
		Map<Integer, Integer> expectedRemoved = new HashMap<>();
		expectedRemoved.put(1, 1);
		assertEquals(expectedRemoved, cache._removed);
	}

	/**
	 * Tests call of callback method for removed objects.
	 */
	public void testRemovedExplicit() {
		CacheUpcomingDeletionsLRUCache<Integer, Integer> cache = new CacheUpcomingDeletionsLRUCache<>(500);
		cache.put(1, 1);
		cache.put(2, 2);
		cache.put(3, 3);

		// Test remove object
		cache.remove(3);
		Map<Integer, Integer> expectedRemoved = new HashMap<>();
		expectedRemoved.put(3, 3);
		assertEquals(expectedRemoved, cache._removed);

		// Test override value
		cache.put(3, 2);
		assertEquals(expectedRemoved, cache._removed);
		cache.put(3, 5);
		expectedRemoved.put(3, 2);
		assertEquals(expectedRemoved, cache._removed);

		// Test remove non existence
		cache.remove(15);
		assertFalse(expectedRemoved.containsKey(15));

		// Test clear
		cache.clear();
		expectedRemoved.put(1, 1);
		expectedRemoved.put(2, 2);
		expectedRemoved.put(3, 5);
		assertEquals(expectedRemoved, cache._removed);
	}

	/** 
     * Test removal of overflowing entries.
	 */
	public void testSpace() {
		// Test size costraints on LRUMap
		Map<String, String> map = new LRUCache<>(5, 0.99f);
		assertEquals("Map has wrong size", 0,  map.size());
        assertTrue(map.isEmpty());
		map.put("1", "val1");
		map.put("2", "val2");
		assertTrue(map.size() == 2);
		map.put("3", "val3");
		map.put("4", "val4");
		map.put("5", "val5");
		map.put("6", "val6");
		map.put("7", "val7");
		map.put("8", "val8");
		// five elements left in the map
		assertTrue("'8' not in map", map.containsKey("8"));
		assertTrue("'7' not in map", map.containsKey("7"));
		assertTrue("'6' not in map", map.containsKey("6"));
		assertTrue("'5' not in map", map.containsKey("5"));
		assertTrue("'4' not in map", map.containsKey("4"));
		// the rest removed
		assertTrue("'3' in map", !map.containsKey("3"));
		assertTrue("'2' in map", !map.containsKey("2"));
		// just a single remove
		map.remove("8");
		assertTrue("'8' in map", !map.containsKey("8"));
		// add a new fifth element
		map.put("9", "val9");
		// still all in the map
		assertTrue("'4' not in map", map.containsKey("4"));
		// access last
		map.get("4");
		map.put("10", "val10");
		map.put("11", "val11");
		// entry 4 remains
		assertTrue("'4' not in map after get", map.containsKey("4"));
		// entry 5 is removed
		assertTrue("'5' in map", !map.containsKey("5"));
	}

	/**
	 * Test overlapping access / ConcurrentModification.
	 */
	public void testAccess() {
		// test Iterator plus put
		Map<String, String> map = new LRUCache<>(5);
		try {
			map.put("1", "val1");
			map.put("2", "val2");
			Iterator<String> it = map.values().iterator();
			map.put("3", "val3");
			map.put("4", "val4");
			map.put("5", "val5");
			map.put("6", "val6");
			map.put("7", "val7");
			it.next();
			fail("ConcurrentModificationException not thrown");
		} catch (ConcurrentModificationException e) {/* expected */	}
	}
	
	/**
	 * Test multi-threading
	 */
	public void testMultiThread() throws Exception {
        
		/**
		 * An additional Thread performing concurrent access to a LRUMap
		 */
		class ConcurrentAccessor extends Thread {
			public String type;

			public Map<String, String> map;
			public boolean halt;

			ConcurrentAccessor(String aType, Map<String, String> aMap) {
				type = aType;
				map  = aMap;
				halt = false;
			} 
			@Override
			public void run() {
				if (type.equals("InOut")) {
					// permanently puts and removes entries to/from the map
					while (!halt) {
						map.put("conKey1",  "conVal1" );
                        map.put("conKey2",  "conVal2" );
                        map.put("conKey3",  "conVal3" );
                        map.get("conKey2");
                        map.get("conKey3");
                        map.get("conKey1");
                        map.remove("conKey3");
                        map.remove("conKey");
                        map.remove("conKey2");
					}
				}
			}
		}
        Properties prop = new Properties();
        prop.setProperty("test.LRUCount"  , "6"); // +3 for ConcurrentAccessor
        
		Map<String, String> map = Collections.synchronizedMap(new LRUCache<>(prop, "test.", 10));
		// start a concurrent accessor
		ConcurrentAccessor con = new ConcurrentAccessor("InOut", map);
		con.start();
		// make some access 
        try {
            for (int i=0; i <100; i++) {
        		map.put("1","val1");
        		map.put("2","val2");
        		map.put("3","val3");
        		map.put("4","val4");
        		map.put("5","val5");
                map.put("6","val6");
                map.put("7","val7");
        		// Due to the ConcurrentAccessor we con only tell soemthing about
                // "1" and "7"
        		assertTrue ("'7' should be in map"     , map.containsKey("7"));
        		if (map.containsKey("1"))
        		   fail("'1' should not be in map" + map);
                
                assertEquals("val7", map.remove("7"));
                assertNull  (        map.remove("1"));
                map.remove("2");
                map.remove("3");
                map.remove("4");
                map.remove("5");
                map.remove("6");

            }
        } finally {
            con.halt = true;
            con.join();
        }
	}
		
	/**
	 * Test KeySet
	 */
	public void testKeySet() {
		// test operation on LRUCache via KeySet
		Map<String, String> map = new LRUCache<>(5);
		Set<String> keySet = map.keySet();
        assertEquals(0, keySet.size());
        assertTrue  (keySet.isEmpty());
		map.put("1", "val1");
		map.put("2", "val2");
		Collection<String> keyCol = new Vector<>();
		keyCol.add("1");
		keyCol.add("2");
		assertTrue("KeySet must contain key '1' and '2'", keySet.containsAll(keyCol));
		assertEquals("KeySet size not correct", keySet.size(), 2);
        Object array[] = keySet.toArray();
        Arrays.sort(array);
        assertEquals("1", array[0]);
        assertEquals("2", array[1]);
        
		String sarray[] = keySet.toArray(new String[2]);
        Arrays.sort(sarray);
        assertEquals("1", sarray[0]);
        assertEquals("2", sarray[1]);

		Iterator<String> keySetIt = keySet.iterator();
		Object key = keySetIt.next();
		keySetIt.remove();
		assertTrue("Removed Element still in Mmap", !map.containsKey(key));
		map.put("3", "valZZZ3");
		assertEquals("valZZZ3", map.put("3", "val3"));
		map.put("4", "val4");
		map.put("5", "val5");
		map.put("6", "val6");
		map.put("7", "val6");
		map.remove("3");
		assertTrue("Map contains key '1'", !map.containsKey("1"));
		assertTrue("KeySet contains key '1'", !keySet.contains("1"));
		assertTrue("KeySet contains key '1' and '2'", !keySet.containsAll(keyCol));	
	}
	
	/**
	 * Test ValuesCollection
	 */
	public void testValuesCollection() {
		// test operation on LRUMap via KeySet
		Map<String, String> map = new LRUCache<>(5);
		Collection<String> values = map.values();
        assertTrue(values.isEmpty());
		map.put("1", "val1");
		map.put("2", "val2");
        assertFalse(values.isEmpty());
		Collection<String> valCol = new Vector<>();
		valCol.add("val1");
		valCol.add("val2");
		assertTrue("ValuesCollection must contain val 'val1' and 'val2'", values.containsAll(valCol));
		assertEquals("KeySet size not correct", values.size(), 2);
		Iterator<String> valuesIt = values.iterator();
		Object val = valuesIt.next();
		valuesIt.remove();
		assertTrue("Removed Element still in Mmap", !map.containsValue(val));
		map.put("3", "val3");
		map.put("4", "val4");
		map.put("5", "valXXX5");
		assertEquals("valXXX5", map.put("5", "val5"));
		map.put("6", "val6");
		map.put("7", "val6");
		map.remove("3");
		assertTrue("Map contains key '1'", !map.containsKey("1"));
		assertTrue("KeySet contains key '1'", !values.contains("1"));
		assertTrue("KeySet contains key '1' and '2'", !values.containsAll(valCol));	
	}
	
    /** 
     * Test some other variant of CTor.
     */
	public void testConstructor() {
		Map<String, String> initialMap = new HashMap<>(3);
        
        initialMap.put("1", "val1");
        initialMap.put("2", "val2");

		Map<String, String> map = new LRUCache<>(5, initialMap);

        initialMap.clear();
        initialMap.put("3", "val3");
        initialMap.put("4", "val4");
        map.putAll(initialMap);

		Set<String> keySet = map.keySet();
		Collection<String> keyCol = new ArrayList<>(4);
        keyCol.add("1");
        keyCol.add("2");
        keyCol.add("3");
        keyCol.add("4");
        assertEquals("KeySet size not correct", keyCol.size(), keySet.size());
        assertTrue  ("KeySet must contain keys 1,2,3,4", keySet.containsAll(keyCol));
		Iterator<String> keySetIt = keySet.iterator();
        Object key = keySetIt.next();
        keySetIt.remove();
        assertTrue("Removed Element still in Map", !map.containsKey(key));
        map.put("3", "valZZZ3");
        assertEquals("valZZZ3", map.put("3", "val3"));
        map.put("4", "val4");
        map.put("5", "val5");
        map.put("6", "val6");
        map.put("7", "val6");
        map.remove("3");
        assertTrue("Map contains key '1'", !map.containsKey("1"));
        assertTrue("KeySet contains key '1'", !keySet.contains("1"));
        assertTrue("KeySet contains key '1' and '2'", !keySet.containsAll(keyCol)); 
        
		initialMap = new HashMap<>(3);
        
        initialMap.put("1", "val1");
        initialMap.put("2", "val2");
		map = new LRUCache<>(5, initialMap);

        initialMap.clear();
        initialMap.put("3", "val3");
        initialMap.put("4", "val4");
        map.putAll(initialMap);

        keySet = map.keySet();      
        assertEquals("KeySet size not correct", keyCol.size(), keySet.size());
        assertTrue  ("KeySet must contain keys 1,2,3,4", keySet.containsAll(keyCol));
        keySetIt    = keySet.iterator();
        key         = keySetIt.next();
        keySetIt.remove();
        assertTrue("Removed Element still in Map", !map.containsKey(key));
        map.put("3", "valZZZ3");
        assertEquals("valZZZ3", map.put("3", "val3"));
        map.put("4", "val4");
        map.put("5", "val5");
        map.put("6", "val6");
        map.put("7", "val6");
        map.remove("3");
        assertTrue("Map contains key '1'", !map.containsKey("1"));
        assertTrue("KeySet contains key '1'", !keySet.contains("1"));
        assertTrue("KeySet contains key '1' and '2'", !keySet.containsAll(keyCol)); 
        
		initialMap = new HashMap<>();
        
        initialMap.put("1", "val1");
        initialMap.put("2", "val2");
        initialMap.put("3", "val3");
        initialMap.put("4", "val4");
        initialMap.put("5", "val5");
		map = new LRUCache<>(3, initialMap); // Provoke Warning
   }

    /** 
     * Test using empty properties resulting in defaults.
     */
	public void testEmptyProperties() {
        Properties prop = new Properties();
        prop.setProperty("No such Count"  , "6"); 
        
		LRUCache<String, String> map = new LRUCache<>(prop, null, 333);
        assertEquals( 333 , map.getMaxCut());
   }
    
    /**
     * The equals() was inherited from AbstractMap, better check it.
     */
    public void testEquals() {
		Map<String, String> map1 = new LRUCache<>(10);
		Map<String, String> map2 = new LRUCache<>(10);
        
        assertEquals(map1, map1);
        assertEquals(map2, map2);
        map1.equals(map2);
        map2.equals(map1);

        map1.put("1", "val1");
        map2.put("2", "val2");
        assertEquals(map1, map1);
        assertEquals(map2, map2);
        assertFalse(map1.equals(map2));
        assertFalse(map2.equals(map1));
    }

    /**
     * The clear() method was inherited from AbstractMap, better check it.
     */
	public void testClear() {
		LRUCache<String, String> map1 = new LRUCache<>(10);
		LRUCache<String, String> map2 = new LRUCache<>(10);
        
        assertEquals(0, map1.size());
        assertEquals(0, map2.size());

        map1.put("1", "val1");
        map1.put("2", "val2");
        assertEquals(2, map1.size());

        map2.put("1", "val1");
        map2.put("2", "val2");
        assertEquals(2, map2.size());

        map1.clear();
        map2.clear();

        assertEquals(0, map1.size());
        assertEquals(0, map2.size());
    }

    /**
	 * make initial Map
	 */
	public Map<Integer, Integer> makeInitialMap(int size) {
		Map<Integer, Integer> m = new HashMap<>();
		for (int i = 0; i < size; i++) {
			m.put(Integer.valueOf(i), Integer.valueOf(i * i));
		}
		assertEquals("initial Map has wrong size", m.size(), size);
		return m;
	}

	/** Return the suite of tests to execute.
	 */
	public static Test suite() {
        return new TestSuite(TestLRUCache.class);
		// return new TestLRUCache("testMultiThread");
	}

	/** main function for direct testing.
	 */
	public static void main(String[] args) {
        
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        
		junit.textui.TestRunner.run(suite());
	}

}
