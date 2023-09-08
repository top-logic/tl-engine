/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import com.top_logic.basic.col.LRUMap;
import com.top_logic.basic.col.LRUWatcher;

/**
 * Test class test {@link com.top_logic.basic.col.LRUMap}.
 * 
 * TODO KHA/BHU reproduce rare case of deadlock when synching with LRUMapWatcher
 *
 * @author  <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class TestLRUMap extends TestCase {

	/**
	 * Constructor to conduct a special test.
	 *
	 * @param name name of the test to execute.
	 */
	public TestLRUMap(String name) {
		super(name);
	}

	// Test methodes

	/** 
     * Test timing out of values.
	 */
	public void testTime() throws Exception {
		// Cache Object for 300milliseocnds only
		Map map = new LRUMap(300, 0);
		assertEquals("Map has wrong size", map.size(), 0);
        assertTrue(map.isEmpty());
		map.put("2", "val2");
		assertEquals("Map has wrong size", map.size(), 1);
        assertFalse(map.isEmpty());
		map.put("3", "val3");
		Thread.sleep(100);
		map.put("4", "val4");
		Thread.sleep(100);
		map.put("5", "val5");
		Thread.sleep(100);
		map.put("6", "val6");
		Thread.sleep(100);
		map.put("7", "val7");
		Thread.sleep(100);
		map.put("8", "val8");
		// Element 2, 7 and 8 are at most 200 ms in map
		assertTrue("'8' not in map", map.containsKey("8"));
		assertTrue("'7' not in map", map.containsKey("7"));
		assertTrue("'6' not in map", map.containsKey("6"));
		// also test values
		assertTrue("'val8' not in map", map.containsValue("val8"));
		assertTrue("'val7' not in map", map.containsValue("val7"));
		assertTrue("'val6' not in map", map.containsValue("val6"));
		//assertTrue("'5' in map", !map.containsKey("5"));
		// Elements 2, 3 and 4 are at least 400 ms not used
		assertTrue("'4' in map", !map.containsKey("4"));
		assertTrue("'3' in map", !map.containsKey("3"));
		assertTrue("'2' in map", !map.containsKey("2"));
		// also test values
		assertTrue("'val4' in map", !map.containsValue("val4"));
		assertTrue("'val3' in map", !map.containsValue("val3"));
		assertTrue("'val2' in map", !map.containsValue("val2"));
		// remove an element
		map.remove("8");
		assertTrue("'8' in map", !map.containsKey("8"));

		// test all entries expired
		Thread.sleep(400);
		assertEquals(map.size(), 0);

		// insert and update
		map.put("1", "val1");
		Thread.sleep(200);
		map.put("2", "val2");
		map.get("1");
		Thread.sleep(200);
		map.get("1");
		map.put("3", "val3");
		Thread.sleep(200);
		map.put("4", "val4");
		map.get("1");
		Thread.sleep(200);
		map.put("5", "val5");
		map.get("1");
		Thread.sleep(200);
		// Element 1 is at most 200 ms not used
		assertTrue ("'1' not in map"  , map.containsKey("1"));
		// Element 2 is at least 800 ms not used
		assertFalse("'2' in map again", map.containsKey("2"));
		
		// test two maps being watched

		Map map1 = new LRUMap(300, 0);
		Map map2 = new LRUMap(300, 0);
		map1.put("p1","val1");
		Thread.sleep(200); 
		map1.put("p2","val2");
		Thread.sleep(100); 
		map1.put("p3","val3");
		Thread.sleep(50);
		map2.put("p4","val4");
		Thread.sleep(50); 
		map2.get("p3");
		Thread.sleep(50);
		map1.get("p4");
		assertTrue("p3 not in map1", map1.containsKey("p3"));
		assertTrue("p4 not in map2", map2.containsKey("p4"));
	}

	/** 
     * Test removal of overflowing entries.
	 */
	public void testSpace() throws Exception {
		// Test size costraints on LRUMap
		Map map = new LRUMap(0, 5);
		assertEquals("Map has wrong size", map.size(), 0);
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
	public void testAccess() throws Exception {
		// test Iterator plus put
		Map map = new LRUMap(0, 5);
		try {
			map.put("1", "val1");
			map.put("2", "val2");
			Iterator it = map.values().iterator();
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
			public LRUMap map;
			public boolean halt;
			ConcurrentAccessor (String aType, LRUMap aMap) {
				type = aType;
				map  = aMap;
				halt = false;
			} 
			@Override
			public void run() {
				if (type.equals("InOut")) {
					// permanently puts end removes entries to/from the map
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
        prop.setProperty("test.LRUSeconds", "0");
        prop.setProperty("test.LRUCount"  , "6"); // +3 for ConcurrentAccessor
        
		LRUMap map = new LRUMap(prop, "test.", 10, 444); // 
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
        		// Due to the ConcurrentAccessor we con oly tell soemthing about
                // "1" and "7"
        		assertTrue ("'7' should be in map"     , map.containsKey("7"));
        		assertFalse("'1' should not be in map" , map.containsKey("1"));
                
                assertEquals("val7", map.remove("7"));

                assertTrue(map.checkLists());
                
                map.remove("1");
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
	public void testKeySet() throws Exception {
		// test operation on LRUMap via KeySet
		Map map = new LRUMap(0, 5);
		Set keySet = map.keySet();		
        assertEquals(0, keySet.size());
        assertTrue  (keySet.isEmpty());
		map.put("1", "val1");
		map.put("2", "val2");
		Collection keyCol = new Vector();
		keyCol.add("1");
		keyCol.add("2");
		assertTrue("KeySet must contain key '1' and '2'", keySet.containsAll(keyCol));
		assertEquals("KeySet size not correct", keySet.size(), 2);
        Object array[] = keySet.toArray();
        Arrays.sort(array);
        assertEquals("1", array[0]);
        assertEquals("2", array[1]);
        
        String sarray[] = (String[]) keySet.toArray(new String[2]);
        Arrays.sort(sarray);
        assertEquals("1", sarray[0]);
        assertEquals("2", sarray[1]);

        Iterator keySetIt = keySet.iterator();
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
	public void testValuesCollection() throws Exception {
		// test operation on LRUMap via KeySet
		Map map = new LRUMap(0, 5);
		Collection values = map.values();
        assertTrue(values.isEmpty());
		map.put("1", "val1");
		map.put("2", "val2");
        assertFalse(values.isEmpty());
		Collection valCol = new Vector();
		valCol.add("val1");
		valCol.add("val2");
		assertTrue("ValuesCollectio must contain val 'val1' and 'val2'", values.containsAll(valCol));
		assertEquals("KeySet size not correct", values.size(), 2);
		Iterator valuesIt = values.iterator();
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
    public void testConstructor() throws Exception {
        LRUWatcher theWatcher = new LRUWatcher();
        Map        initialMap = new HashMap(3);
        
        initialMap.put("1", "val1");
        initialMap.put("2", "val2");

        Map map = new LRUMap(1000, 5, initialMap, theWatcher);

        initialMap.clear();
        initialMap.put("3", "val3");
        initialMap.put("4", "val4");
        map.putAll(initialMap);

        Set keySet = map.keySet();      
        Collection keyCol = new ArrayList(4);
        keyCol.add("1");
        keyCol.add("2");
        keyCol.add("3");
        keyCol.add("4");
        assertEquals("KeySet size not correct", keyCol.size(), keySet.size());
        assertTrue  ("KeySet must contain keys 1,2,3,4", keySet.containsAll(keyCol));
        Iterator keySetIt = keySet.iterator();
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
        
        
        initialMap = new HashMap(3);
        
        initialMap.put("1", "val1");
        initialMap.put("2", "val2");
        map = new LRUMap(1000, 5, initialMap, null);

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
        
        map = new LRUMap(0, 5, null, null);

        initialMap = new HashMap(8);
        
        initialMap.put("1", "val1");
        initialMap.put("2", "val2");      
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
   }

    /** 
     * Test using empty properties resulting in defaults.
     */
    public void testEmptyProperties() throws Exception {
        Properties prop = new Properties();
        prop.setProperty("test.BlaBlub"        , "0");
        prop.setProperty("test.No such Count"  , "6"); 
        
        LRUMap map = new LRUMap(prop, null, 111, 333);
        assertEquals( 333L , map.getMaxCut());
        assertEquals( 111L , map.getMinTime());
   }
    
    /**
     * Check Unsupported Operations.
     * 
     * (More a check against missing Tests actually,
     *  and well, it pushes coverage ...)
     */
    public void testUnsupported() {
        Map map = new LRUMap(10, 10);
        
        try { map.entrySet();
              fail("Expected UnsupportedOperation"); }
        catch (UnsupportedOperationException usox) { /* expected */ }

        Collection values = map.values();

        try {
            values.add(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        try {
            values.addAll(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        try {
            values.remove(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        try {
            values.removeAll(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        
        try {
            values.retainAll(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        
        Set keySet = map.keySet();

        try {
            keySet.add(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        try {
            keySet.addAll(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        try {
            keySet.clear();
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        try {
            keySet.remove(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        try {
            keySet.removeAll(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
        
        try {
            keySet.retainAll(null);
            fail("Expected UnsupportedOperation");
        } catch (UnsupportedOperationException usox) { /* expected */ }
    }
    
    /**
     * The equals() was inherited from AbstractMap, better check it.
     */
    public void testEquals() {
        Map map1 = new LRUMap(10, 10);
        Map map2 = new LRUMap(10, 10);
        
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
    public void testClear() throws InterruptedException {
        LRUMap map1 = new LRUMap(10, 10);
        LRUMap map2 = new LRUMap(10, 0);
        LRUMap map3 = new LRUMap(0 , 10);
        
        assertEquals(0, map1.size());
        assertEquals(0, map2.size());
        assertEquals(0, map3.size());

        map1.put("1", "val1");
        map1.put("2", "val2");
        assertEquals(2, map1.size());

        map2.put("1", "val1");
        map2.put("2", "val2");
        assertEquals(2, map2.size());

        map3.put("1", "val1");
        map3.put("2", "val2");
        assertEquals(2, map3.size());

        map1.clear();
        map2.clear();
        map3.clear();

        Thread.sleep(50);
        assertEquals(0, map1.removeExpired()); // Should be a noop
        assertEquals(0, map2.removeExpired()); // Should be a noop
        try {
        	assertEquals(0, map3.removeExpired()); // Should be a noop
        	fail("Must not call removeExpired(), if no exiration was specified.");
        } catch (IllegalStateException ex) {
        	// Expected.
        }

        assertEquals(0, map1.size());
        assertEquals(0, map2.size());
        assertEquals(0, map3.size());
    }

	/** Return the suite of tests to execute.
	 */
	public static Test suite() {
        return new TestSuite(TestLRUMap.class);
		// return new TestLRUMap("testEntrySet");
	}

	/** main function for direct testing.
	 */
	public static void main(String[] args) {
        
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        
		junit.textui.TestRunner.run(suite());
	}

}
