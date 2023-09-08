/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections4.BidiMap;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.BidiHashMap;

/**
 * Test the {@link BidiHashMap}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBidiHashMap extends AbstractTestBidiMap {

	@Override
	public BidiMap makeEmptyBidiMap() {
		return new BidiHashMap();

	}

	/**
	 * Tests <code>null</code> as key or value in the {@link BidiMap}.
	 */
	public void testNull() {
		Object o = new Object();
		BidiMap m = makeEmptyBidiMap();
		m.put(null, o);
		assertEquals(o, m.get(null));
		assertEquals(null, m.getKey(o));

		m = makeEmptyBidiMap();
		m.put(o, null);
		assertEquals(null, m.get(o));
		assertEquals(o, m.getKey(null));

		m = makeEmptyBidiMap();
		m.put(null, null);
		assertEquals(null, m.get(null));
		assertEquals(null, m.getKey(null));
	}

	/**
	 * Tests map integrity after removing elements from {@link BidiHashMap#entrySet()}.
	 */
	public void testRemoveFromIterator() {
		// These values are special values to trigger situation as in #13908.

		int expectedSize = 20; // causes the map to have min size 12
		int internalTableSize = 32; // causes all entries are located in same slot
		BidiHashMap testMap = new BidiHashMap(expectedSize);
		for (int i = 0; i < 12; i++) {
			testMap.put(internalTableSize * i, internalTableSize * i);
		}
		Iterator iterator = testMap.keySet().iterator();
		while (iterator.hasNext()) {
			// fetch and allocate to be able to remove.
			Object objectToRemove = iterator.next();
			assertNotNull(objectToRemove);
			iterator.remove();
		}
		for (int i = 0; i < 12; i++) {
			int key = internalTableSize * i;
			assertFalse("Ticket #13908: Removal of key '" + key + "'.", testMap.containsKey(key));
			assertNull("Ticket #13908: Removal of key '" + key + "'.", testMap.get(key));

			int value = internalTableSize * i;
			assertFalse("Ticket #13908: Removal of value '" + value + "'.", testMap.containsValue(value));
			assertNull("Ticket #13908: Removal of value '" + value + "'.", testMap.getKey(value));
		}
	}

	/**
	 * Puts a mapping such that for the key as well for the destination an mapping already exists.
	 */
	public void testPutSourceDestAlreadyExists() {
		// These values are special values to trigger situation as in #13908.

		int expectedSize = 20; // causes the map to have min size 12
		BidiHashMap testMap = new BidiHashMap(expectedSize);
		for (int i = 1; i < 12; i++) {
			testMap.put(i, i);
		}
		testMap.put(17, 17); // size == minSize
		/* removing old entry for entry 1 causes the map to rehash, because after removing size <
		 * minSize */
		testMap.put(17, 1);

		assertEquals("Ticket #13908: put(17,1) causes 17 to be key of 1.", Integer.valueOf(17), testMap.getKey(1));
		assertEquals("Ticket #13908: put(17,1) causes 1 to be value of of 17.", 1, testMap.get(17));
	}

	/**
	 * Puts a mapping such that for the the destination an mapping already exists (but not for the
	 * key).
	 */
	public void testPutDestAlreadyExists() {
		// These values are special values to trigger situation as in #13908.

		int expectedSize = 20; // causes the map to have min size 12
		BidiHashMap testMap = new BidiHashMap(expectedSize);
		for (int i = 1; i < 12; i++) {
			testMap.put(i, i);
		}
		testMap.put(17, 17); // size == minSize
		assertFalse(testMap.containsKey(31));
		try {
			/* 31 has index 62 before rehash, after rehash the array has size 32 -> an
			 * ArrayIndexOutOfBoundsException for source entry is thrown */
			testMap.put(31, 1);
			assertEquals(Integer.valueOf(31), testMap.getKey(1));
			assertEquals(1, testMap.get(31));
		} catch (ArrayIndexOutOfBoundsException ex) {
			BasicTestCase.fail("Ticket #13908: Rehash during put(Object,Object).", ex);
		}
	}

	/**
	 * Puts a mapping such that for the the destination an mapping already exists (but not for the
	 * key).
	 */
	public void testPutDestAlreadyExists2() {
		// These values are special values to trigger situation as in #13908.

		int expectedSize = 20; // causes the map to have min size 12
		BidiHashMap testMap = new BidiHashMap(expectedSize);
		for (int i = 1; i < 12; i++) {
			testMap.put(i, i);
		}
		testMap.put(17, 17); // size == minSize
		assertFalse(testMap.containsKey(32));
		try {
			/* 17 has index 35 before rehash, after rehash the array has size 32 -> an
			 * ArrayIndexOutOfBoundsException is for destination entry is thrown */
			testMap.put(32, 17);
			assertEquals(Integer.valueOf(32), testMap.getKey(17));
			assertEquals(17, testMap.get(32));
		} catch (ArrayIndexOutOfBoundsException ex) {
			BasicTestCase.fail("Ticket #13908: Rehash during put(Object,Object).", ex);
		}
	}

	public void testConcurrentModificationDetectionEntrySet() {
		resetFull();
		
		Iterator it = map.entrySet().iterator();
		Entry first = (Entry) it.next();
		
		map.remove(first.getKey());
		
		try {
			it.next();
			fail("Ticket #2512: Concurrent modification exception expected.");
		} catch (ConcurrentModificationException ex) {
			// Expected.
		}
	}
	
	public void testConcurrentModificationDetectionKeySet() {
		resetFull();
		
		Iterator it = map.keySet().iterator();
		Object first = it.next();
		
		map.remove(first);
		
		try {
			it.next();
			fail("Ticket #2512: Concurrent modification exception expected.");
		} catch (ConcurrentModificationException ex) {
			// Expected.
		}
	}
	
	public void testConcurrentModificationDetectionValueSet() {
		resetFull();
		
		Iterator it = map.values().iterator();
		Object first = it.next();
		
		((BidiMap) map).removeValue(first);
		
		try {
			it.next();
			fail("Ticket #2512: Concurrent modification exception expected.");
		} catch (ConcurrentModificationException ex) {
			// Expected.
		}
	}
	
	public void testSimpleHashCode() {
        resetEmpty();

        assertEquals("Hash codes are the same for an empty map", confirmed.hashCode(), map.hashCode());
		verify();
		for (int n = 0; n < 1000; n++) {
			Integer k = Integer.valueOf(n);
			Integer v = Integer.valueOf(n + 1000);
			map.put(k, v);
			confirmed.put(k, v);
			
			if (n % 100 == 0) {
				verify();
			}
			assertEquals("Hash codes are the same, n=" + n, confirmed.hashCode(), map.hashCode());
		}
		verify();
		
		for (int n = 0; n < 1000; n++) {
			Integer k = Integer.valueOf(n);
			
			map.remove(k);
			confirmed.remove(k);
			
			if (n % 100 == 0) {
				verify();
			}
			assertEquals("Hash codes are the same, n=" + n, confirmed.hashCode(), map.hashCode());
		}
		verify();
	}
	
	@Override
	public void testBidiModifyEntrySet() {
		// Not supported.
		// super.testBidiModifyEntrySet();
	}
	
	public void testEntryIteratorRemoveAll() {
		doTestEntryIteratorRemoveAll(false);
	}

	public void testEntryIteratorRemoveAllWithInterleavingHasNext() {
		doTestEntryIteratorRemoveAll(true);
	}
	
	private void doTestEntryIteratorRemoveAll(boolean hasNextBeforeRemove) {
		resetFull();
		
		int removed = 0;
		int expectedRemoved = confirmed.size();
		for (Iterator it = entrySet.iterator(); it.hasNext(); ) {
			Entry entry = (Entry) it.next();
			
			if (hasNextBeforeRemove) {
				it.hasNext();
			}
			
			it.remove();
			
			Object removedKey = entry.getKey();
			Object removedValue = entry.getValue();
			
			Object expectedRemovedValue = confirmed.remove(removedKey);
			
			assertEquals(expectedRemovedValue, removedValue);
			
			removed++;
		}
		
		assertEquals(expectedRemoved, removed);
		
		verifyMap();
	}
	
	public void testEntryIteratorRemove() {
		resetFull();

		int n = 0;
		int removed = 0;
		
		int maxRemove = 5;
		for (Iterator it = entrySet.iterator(); it.hasNext(); ) {
			Entry entry = (Entry) it.next();

			// Remove first entry, last entry and ever third entry, but at most maxRemove entries. 
			boolean hasAnother = it.hasNext();
			if (((removed < maxRemove - 1) && (n % 3 == 0)) || (! hasAnother)) {
				it.remove();
				Object removedKey = entry.getKey();
				Object removedValue = entry.getValue();
				
				Object expectedRemovedValue = confirmed.remove(removedKey);
				
				assertEquals(expectedRemovedValue, removedValue);
				
				removed++;
			}
		}
		
		// Make sure, test setup has enough elements.
		assertEquals(maxRemove, removed);
		
		verifyMap();
	}	

    public static Test suite() {
        return new TestSuite(TestBidiHashMap.class);
    }
	
    @Override
	public void verifyMap() {
    	// Re-enable test disabled in Apache suite.
        assertEquals("Map should still equal HashMap", confirmed, map);
    	
    	assertEquals(map.size(), new HashSet(this.map.keySet()).size());
    	assertEquals(map.size(), new HashSet(this.map.values()).size());
    	assertEquals(map.size(), new HashSet(this.map.entrySet()).size());
    	
    	super.verifyMap();
    }
}
