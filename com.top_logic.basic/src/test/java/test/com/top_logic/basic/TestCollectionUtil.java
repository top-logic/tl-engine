/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static com.top_logic.basic.CollectionUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections4.comparators.ComparableComparator;

import test.com.top_logic.basic.col.iterator.TestIteratorUtil;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * Test the {@link CollectionUtil}
 * 
 * @author     SKO
 */
@SuppressWarnings("javadoc")
public class TestCollectionUtil extends BasicTestCase {

    /** 
     * Creates a {@link TestCollectionUtil}.
     */
    public TestCollectionUtil(String aName) {
        super(aName);
    }
    
	public void testSizeIsEmpty() {
		assertTrue(CollectionUtil.isEmpty(Collections.emptyList()));
		assertEquals(0, CollectionUtil.size(Collections.emptyList()));
		assertFalse(CollectionUtil.isEmpty(Collections.singletonList("")));
		assertEquals(1, CollectionUtil.size(Collections.singletonList("")));
		
		assertTrue(CollectionUtil.isEmpty(CollectionUtil.toIterable(CollectionUtil.emptyIterator())));
		assertEquals(0, CollectionUtil.size(CollectionUtil.toIterable(CollectionUtil.emptyIterator())));
		assertFalse(CollectionUtil.isEmpty(CollectionUtil.toIterable(list("s", "t").iterator())));
		assertEquals(2, CollectionUtil.size(CollectionUtil.toIterable(list("s", "t").iterator())));
	}

	public void testAddAll() {
		Collection<Integer> c = new HashSet<>();

		boolean firstAdd = CollectionUtil.addAll(c, CollectionUtil.toIterable(list(1, 2).iterator()));
		assertTrue(firstAdd);
		assertEquals(set(1, 2), c);

		boolean secondAdd = CollectionUtil.addAll(c, CollectionUtil.toIterable(list(1, 2).iterator()));
		assertFalse(secondAdd);
		assertEquals(set(1, 2), c);

		boolean emptyAdd =
			CollectionUtil.addAll(c, CollectionUtil.toIterable(BasicTestCase.<Integer> list().iterator()));
		assertFalse(emptyAdd);

		boolean partiallyAdd = CollectionUtil.addAll(c, CollectionUtil.toIterable(list(1, 3).iterator()));
		assertTrue(partiallyAdd);
		assertEquals(set(1, 2, 3), c);
	}

	public void testRemoveAll() {
		Collection<Integer> c = new HashSet<>();
		c.addAll(list(1, 2, 3));

		boolean firstRemove = CollectionUtil.removeAll(c, CollectionUtil.toIterable(list(1, 2).iterator()));
		assertTrue(firstRemove);
		assertEquals(set(3), c);

		boolean secondRemove = CollectionUtil.removeAll(c, CollectionUtil.toIterable(list(1, 2).iterator()));
		assertFalse(secondRemove);
		assertEquals(set(3), c);

		boolean emptyRemove =
			CollectionUtil.removeAll(c, CollectionUtil.toIterable(BasicTestCase.<Integer> list().iterator()));
		assertFalse(emptyRemove);

		boolean partiallyRemove = CollectionUtil.removeAll(c, CollectionUtil.toIterable(list(1, 3).iterator()));
		assertTrue(partiallyRemove);
		assertEquals(set(), c);
	}

	public void testAddAllIterator() {
		Collection<Integer> c = new HashSet<>();

		boolean firstAdd = CollectionUtilShared.addAll(c, list(1, 2).iterator());
		assertTrue(firstAdd);
		assertEquals(set(1, 2), c);

		boolean secondAdd = CollectionUtilShared.addAll(c, list(1, 2).iterator());
		assertFalse(secondAdd);
		assertEquals(set(1, 2), c);

		boolean emptyAdd = CollectionUtilShared.addAll(c, BasicTestCase.<Integer> list().iterator());
		assertFalse(emptyAdd);

		boolean partiallyAdd = CollectionUtilShared.addAll(c, list(1, 3).iterator());
		assertTrue(partiallyAdd);
		assertEquals(set(1, 2, 3), c);
	}

	public void testRemoveAllIterator() {
		Collection<Integer> c = new HashSet<>();
		c.addAll(list(1, 2, 3));

		boolean firstRemove = CollectionUtilShared.removeAll(c, list(1, 2).iterator());
		assertTrue(firstRemove);
		assertEquals(set(3), c);

		boolean secondRemove = CollectionUtilShared.removeAll(c, list(1, 2).iterator());
		assertFalse(secondRemove);
		assertEquals(set(3), c);

		boolean emptyRemove = CollectionUtilShared.removeAll(c, BasicTestCase.<Integer> list().iterator());
		assertFalse(emptyRemove);

		boolean partiallyRemove = CollectionUtilShared.removeAll(c, list(1, 3).iterator());
		assertTrue(partiallyRemove);
		assertEquals(set(), c);
	}

    public void testIfAbsent() {
    	ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    	String sharedKey = "a";
		String initialValue = "initial";
		String containedValue = MapUtil.putIfAbsent(map, sharedKey, initialValue);
    	assertSame(initialValue, containedValue);
		String result = MapUtil.putIfAbsent(map, sharedKey, "new");
    	assertSame("Expected nothing has changed has key already associated to another value", initialValue, result);
		assertSame(initialValue, map.get(sharedKey));
    }
    
    /**
     * Test case for {@link CollectionUtil#chunk(int, Iterator)}.
     */
    public void testChunk() {
    	assertFalse(CollectionUtil.chunk(2, list().iterator()).hasNext());
    	assertEquals(list("A", "B"), CollectionUtil.chunk(2, list("A", "B", "C").iterator()).next());
		assertEquals(list("A", "B", "C"), concat(CollectionUtil.chunk(2, list("A", "B", "C").iterator())));

		List<Integer> list = new ArrayList<>();
    	for (int n = 0; n < 50; n++) {
    		for (int chunkSize = 1; chunkSize <= n + 1; chunkSize++) {
    			assertEquals(list, concat(CollectionUtil.chunk(chunkSize, list.iterator())));
    		}
    		
    		list.add(n);
    	}
    }
    
	private <T> List<T> concat(Iterator<? extends Collection<? extends T>> lists) {
		ArrayList<T> result = new ArrayList<>();
		while (lists.hasNext()) {
			result.addAll(lists.next());
		}
		return result;
	}

    public void testNonNull() {
    	assertTrue(nonNull((Collection<?>) null).isEmpty());
    	assertTrue(nonNull((Set<?>) null).isEmpty());
    	assertTrue(nonNull((List<?>) null).isEmpty());
    	assertTrue(nonNull((Map<?,?>) null).isEmpty());
    	
    	assertFalse(nonNull((Collection<?>) Collections.singletonList("x")).isEmpty());
    	assertFalse(nonNull(Collections.singleton("x")).isEmpty());
    	assertFalse(nonNull(Collections.singletonList("x")).isEmpty());
    	assertFalse(nonNull(Collections.singletonMap("x", "y")).isEmpty());
    }
    
    public void testCastView() {
    	assertEquals(set("x"), dynamicCastView(String.class, set("x")));
    	assertEquals(list("x"), dynamicCastView(String.class, list("x")));
    	assertEquals(list("x"), new ArrayList<>(dynamicCastView(String.class, (Collection<String>) list("x"))));
    	assertEquals(list("x"), toList(dynamicCastView(String.class, (Iterable<String>) list("x")).iterator()));

    	try {
			dynamicCastView(String.class, set(1));
    		fail("Cast problem not detected.");
		} catch (ClassCastException ex) {
			// Expected.
		}
		
		try {
			dynamicCastView(String.class, list(1));
			fail("Cast problem not detected.");
		} catch (ClassCastException ex) {
			// Expected.
		}
		
		try {
			dynamicCastView(String.class, (Collection<Integer>) list(1));
			fail("Cast problem not detected.");
		} catch (ClassCastException ex) {
			// Expected.
		}
		
		try {
			toList(dynamicCastView(String.class, (Iterable<Integer>) list(1)).iterator());
			fail("Cast problem not detected.");
		} catch (ClassCastException ex) {
			// Expected.
		}
    }
    
    public void testShrink() {
    	assertEquals(list(), CollectionUtil.shrinkOptimized(list(), 0));
    	assertEquals(list(0), CollectionUtil.shrinkOptimized(list(0), 1));
    	assertEquals(list(0, 1), CollectionUtil.shrinkOptimized(list(0, 1), 2));
    	assertEquals(list(0, 1, 2), CollectionUtil.shrinkOptimized(list(0, 1, 2), 3));
    	assertEquals(list(0, 1), CollectionUtil.shrinkOptimized(list(0, 1, 2), 2));
    	assertEquals(list(0), CollectionUtil.shrinkOptimized(list(0, 1, 2), 1));
    	assertEquals(list(), CollectionUtil.shrinkOptimized(list(0, 1, 2), 0));
    	
    	assertEquals(list(), CollectionUtil.shrinkOptimized(longList(64), 0));
    	assertEquals(list(0), CollectionUtil.shrinkOptimized(longList(64), 1));
    	assertEquals(list(0, 1), CollectionUtil.shrinkOptimized(longList(64), 2));
    	assertEquals(list(0, 1, 2), CollectionUtil.shrinkOptimized(longList(64), 3));
    	
    	assertEquals(longList(64), CollectionUtil.shrinkOptimized(longList(64), 64));
    	assertEquals(longList(63), CollectionUtil.shrinkOptimized(longList(64), 63));
    	assertEquals(longList(62), CollectionUtil.shrinkOptimized(longList(64), 62));
    	
    	assertEquals(longList(2), CollectionUtil.shrinkOptimized(longList(64), 2));
    	assertEquals(longList(1), CollectionUtil.shrinkOptimized(longList(64), 1));
    	assertEquals(longList(0), CollectionUtil.shrinkOptimized(longList(64), 0));
    	
    	assertEquals(longList(64), CollectionUtil.shrinkOptimized(Arrays.asList(longList(64).toArray()), 64));
    	assertEquals(longList(63), CollectionUtil.shrinkOptimized(Arrays.asList(longList(64).toArray()), 63));
    	assertEquals(longList(62), CollectionUtil.shrinkOptimized(Arrays.asList(longList(64).toArray()), 62));
    	
    	assertEquals(longList(2), CollectionUtil.shrinkOptimized(Arrays.asList(longList(64).toArray()), 2));
    	assertEquals(longList(1), CollectionUtil.shrinkOptimized(Arrays.asList(longList(64).toArray()), 1));
    	assertEquals(longList(0), CollectionUtil.shrinkOptimized(Arrays.asList(longList(64).toArray()), 0));
    }
    
    public void testShrinkOptimization() {
    	List<Integer> l0 = longList(64);
		List<Integer> l0Shrunk = CollectionUtil.shrinkOptimized(l0, 0);
		assertEquals(list(), l0Shrunk);
		assertSame("Arraylist must be trimmed internally.", l0, l0Shrunk);
    	
    	List<Integer> l1 = longList(64);
		List<Integer> l1Shrunk = CollectionUtil.shrinkOptimized(l1, 63);
		assertEquals(longList(63), l1Shrunk);
		assertSame(l1, l1Shrunk);
    }
    
    private List<Integer> longList(int size) {
		ArrayList<Integer> result = new ArrayList<>(size);
		for (int n = 0; n < size; n++) {
			result.add(n);
		}
		return result;
	}

	/**
     * Tests {@link CollectionUtil#equals(Object, Object)}
     */
    public void testEquals() {
        String tmp = "ABC";
        assertTrue(CollectionUtil.equals(tmp  , tmp));
        assertTrue(CollectionUtil.equals(null , null));
        assertFalse(CollectionUtil.equals("A" , "B"));
        assertFalse(CollectionUtil.equals(null, "B"));
    }
    
    /**
     * Tests {@link CollectionUtil#containsOnly(Class, Collection)}
     */
    public void testContainsOnly() {
        assertTrue(CollectionUtil.containsOnly(Integer.class, createList(0)));
        Collection<Object> tCol = createList(12);
        assertTrue(CollectionUtil.containsOnly(Integer.class,           tCol));
        assertTrue(CollectionUtil.containsOnly(Integer.class, (List<?>) tCol));
        tCol.add(this);
        assertFalse(CollectionUtil.containsOnly(Integer.class, tCol));
        assertTrue (CollectionUtil.containsOnly(String.class, set("A", "Z")));
        assertFalse(CollectionUtil.containsOnly(String.class, set("A", this)));
    }
    
    /**
     * Tests {@link CollectionUtil#containsOnlyRecursivly(Class, Collection)}
     */
    public void testContainsOnlyRecursivly() {
    	assertTrue(CollectionUtil.containsOnly(Integer.class, createList(0)));
    	
    	Collection<Object> firstLevelCollection = createList(10);
    	Collection<Object> secondLevelCollection_1 = createList(10);
    	Collection<Object> secondLevelCollection_2 = createList(10);
    	Collection<Object> thirdLevelCollection = createList(10);
    	
    	assertTrue(CollectionUtil.containsOnlyRecursivly(Integer.class, firstLevelCollection));
    	
    	secondLevelCollection_1.add(thirdLevelCollection);
    	firstLevelCollection.add(secondLevelCollection_1);
    	firstLevelCollection.add(secondLevelCollection_2);
    	
    	assertTrue(CollectionUtil.containsOnlyRecursivly(Integer.class, firstLevelCollection));
    	
    	thirdLevelCollection.add("A");
    	
    	assertFalse(CollectionUtil.containsOnlyRecursivly(Integer.class, firstLevelCollection));
    	
    	thirdLevelCollection.remove("A");
    	
    	assertTrue(CollectionUtil.containsOnlyRecursivly(Integer.class, firstLevelCollection));
    	
    	firstLevelCollection.add("A");
    	
    	assertFalse(CollectionUtil.containsOnlyRecursivly(Integer.class, firstLevelCollection));
    }

    /**
     * Tests {@link CollectionUtil#containsAny(Collection, Collection)}
     */
    public void testContainsAnyOneNull() {
        assertFalse(CollectionUtil.containsAny(null, list("A", "B")));
        assertFalse(CollectionUtil.containsAny(list("A", "B"),null));
    }
    
    /**
     * Tests {@link CollectionUtil#containsAny(Collection, Collection)}
     */
    public void testContanisAnyBothNull() {
        assertFalse(CollectionUtil.containsAny(null, null));
    }
    
    /**
     * Tests {@link CollectionUtil#containsAny(Collection, Collection)}
     */
    public void testContainsAnyMultiple() {
        assertTrue(CollectionUtil.containsAny(list("A","B"), list("B", "A")));
        assertTrue(CollectionUtil.containsAny(list("A","B"), list("A", "B")));
        assertFalse(CollectionUtil.containsAny(list("A","B"), list("C", "D")));
    }
    
    /**
     * Tests {@link CollectionUtil#containsAny(Collection, Collection)}
     */
    public void testContainsAnySingleElement() {
        assertTrue(CollectionUtil.containsAny(list("1"),list("1")));
    }
    
    /**
     * Tests {@link CollectionUtil#containsAny(Collection, Collection)}
     */
    public void testContainsAnyEmptyLists() {
        assertFalse(CollectionUtil.containsAny(new ArrayList<>(), Collections.EMPTY_LIST));
    }
    
    /**
     * Tests {@link CollectionUtil#containsSame(Collection, Collection)}
     */
    public void testContainsSameWithSame() {
        assertTrue(CollectionUtil.containsSame(createList(0), createList(0)));
        assertTrue(CollectionUtil.containsSame(null, null));
        assertTrue(CollectionUtil.containsSame(list("A","B"), list("A","B")));
        assertTrue(CollectionUtil.containsSame(list("B","A"), list("A","B")));
    }
    
    /**
     * Tests {@link CollectionUtil#containsSame(Collection, Collection)}
     */
    public void testContainsSameOneNull() {
        assertFalse(CollectionUtil.containsSame(null, list("C", "D")));
        assertFalse(CollectionUtil.containsSame(list("C", "D"), null));
        assertFalse(CollectionUtil.containsSame(createList(0), null));
    }
    
    /**
     * Tests {@link CollectionUtil#containsSame(Collection, Collection)}
     */
    public void testContainsSameSingleElement() {
        assertFalse(CollectionUtil.containsSame(list("A"), createList(0)));
        assertFalse(CollectionUtil.containsSame(createList(1), createList(5)));
    }
    
    /**
     * Tests {@link CollectionUtil#getSingleValueFromCollection(Collection)}
     */
    public void testGetSingleValueFromCol() {
        assertEquals("A" , CollectionUtil.getSingleValueFromCollection(list("A")));
        assertEquals("X" , CollectionUtil.getSingleValueFromCollection(set("X")));
        assertEquals(null, CollectionUtil.getSingleValueFromCollection(createList(0)));
        assertEquals(null, CollectionUtil.getSingleValueFromCollection((Collection<?>) null));
    }
    
    /**
     * Tests {@link CollectionUtil#map(Iterator, Collection, Mapping)}
     */
	public void testMap() {
        Mapping<Integer, Integer> theMapping = new Mapping<>() {
            @Override
			public Integer map(Integer anObject) {
                return Integer.valueOf(anObject + 100);
            }
        };
        Collection<Integer> theDestination = new ArrayList<>();
        
        // test null and empty source
        theDestination.add(Integer.valueOf(0));
		CollectionUtil.map(null, new HashSet<>(), theMapping);
        assertEquals("Size of destination chanted after null source.", 1, theDestination.size());
        assertEquals("Content of destination chanted after null source.", Integer.valueOf(0), theDestination.iterator().next());
		CollectionUtil.map(Collections.<Integer> emptyList().iterator(), new HashSet<>(), theMapping);
        assertEquals("Size of destination chanted after empty source.", 1, theDestination.size());
        assertEquals("Content of destination chanted after empty source.", Integer.valueOf(0), theDestination.iterator().next());
        
        Collection<Integer> theOriginalObjects = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            theOriginalObjects.add(Integer.valueOf(i));
        }
        
        try {
            CollectionUtil.map(theOriginalObjects.iterator(), null, theMapping);
            fail("Called map() with illegal arguments.");
        } catch (IllegalArgumentException e) { /* expected */ }
        
        try {
            CollectionUtil.map(theOriginalObjects.iterator(), theDestination, null);
            fail("Called map() with illegal arguments.");
        } catch (IllegalArgumentException e) { /* expected */ }
        
        theDestination = new ArrayList<>();
        
        CollectionUtil.map(theOriginalObjects.iterator(), theDestination, theMapping);

        for (int i = 100; i < 200; i++) {
            assertTrue(i + "nuo contained in destination after mapping.", theDestination.contains(Integer.valueOf(i)));
        }
        
    }
    
    /**
     * Tests {@link CollectionUtil#mapIgnoreNull(Iterator, Collection, Mapping)}
     */
    public void testMapIgnoreNull() {
        Set<String> theSet = set("A", "B");
        Iterator<String> theIt = theSet.iterator();
        Collection<Object> aDestination = createList(0);
        
        Mapping<Object, String> aMapping = new Mapping<>(){
            @Override
			public String map(Object aInput) {
                return aInput.toString();
            }
        };
        
        try {
            CollectionUtil.mapIgnoreNull(null, aDestination, aMapping);
            fail("Iterator was null");
        } catch (IllegalArgumentException e) {/* expected */}
        
        try {
            CollectionUtil.mapIgnoreNull(theIt, null, aMapping);
            fail("Destination was null");
        } catch (IllegalArgumentException e) {/* expected */}
        
        try {
            CollectionUtil.mapIgnoreNull(theIt, aDestination, null);
            fail("Mapping was null");
        } catch (IllegalArgumentException e) {/* expected */}
        
        CollectionUtil.mapIgnoreNull(theIt, aDestination, aMapping);
        assertTrue(aDestination.containsAll(theSet));
    }
    
    /**
     * Tests {@link CollectionUtil#find(Collection, Object, Mapping)}
     */
    public void testFind() {
        Mapping<Object, Object> theMapping = new Mapping<>() {
            @Override
			public Object map(Object anObject) {
                return new String(anObject.toString());
            }
        };
        
        assertNull(CollectionUtil.find(null, null, null));
        assertNull(CollectionUtil.find(list("A"), "B", null));
        assertNull(CollectionUtil.find(list("A"), "B", theMapping));
        assertEquals("A", CollectionUtil.find(list("B", "A"), "A", theMapping));
        
        theMapping = new Mapping<>() {
			@Override
			public Object map(Object input) {
				if (input instanceof Integer) {
					return Integer.valueOf((((Integer)input).intValue()+1));
				}
				return null;
			}
        };
        assertEquals(Integer.valueOf(0), CollectionUtil.find(Collections.singleton(Integer.valueOf(0)), Integer.valueOf(1), theMapping));
    }

	public void testIndexOfFirst() {
		assertEquals(NOT_FOUND, indexOfFirst(null, null));

		assertEquals(NOT_FOUND, indexOfFirst(null, list(1)));
		assertEquals(NOT_FOUND, indexOfFirst(null, list(1, 2)));

		assertEquals(NOT_FOUND, indexOfFirst(list(1), null));
		assertEquals(NOT_FOUND, indexOfFirst(list(1, 2), null));

		assertEquals(NOT_FOUND, indexOfFirst(list(), list(1)));
		assertEquals(NOT_FOUND, indexOfFirst(list(), list(1, 2)));
		assertEquals(NOT_FOUND, indexOfFirst(list(1), list()));
		assertEquals(NOT_FOUND, indexOfFirst(list(1, 2), list()));

		assertEquals(NOT_FOUND, indexOfFirst(list(1), list(3)));
		assertEquals(NOT_FOUND, indexOfFirst(list(1), list(3, 4)));
		assertEquals(NOT_FOUND, indexOfFirst(list(1, 2), list(3)));
		assertEquals(NOT_FOUND, indexOfFirst(list(1, 2), list(3, 4)));

		assertEquals(0, indexOfFirst(list(1), list(1)));
		assertEquals(0, indexOfFirst(list(1), list(1, 2)));
		assertEquals(0, indexOfFirst(list(2), list(1, 2)));

		assertEquals(1, indexOfFirst(list(0, 1), list(1)));
		assertEquals(1, indexOfFirst(list(0, 1), list(1, 2)));
		assertEquals(1, indexOfFirst(list(0, 2), list(1, 2)));

		assertEquals(0, indexOfFirst(list(1, 0), list(1)));
		assertEquals(0, indexOfFirst(list(1, 0), list(1, 2)));
		assertEquals(0, indexOfFirst(list(2, 0), list(1, 2)));

		assertEquals(0, indexOfFirst(list(1, 1), list(1)));
		assertEquals(0, indexOfFirst(list(1, 1), list(1, 2)));
		assertEquals(0, indexOfFirst(list(1, 2), list(1, 2)));
		assertEquals(0, indexOfFirst(list(2, 1), list(1, 2)));
		assertEquals(0, indexOfFirst(list(2, 2), list(1, 2)));
	}

	public void testIndexOfLast() {
		assertEquals(NOT_FOUND, indexOfLast(null, null));

		assertEquals(NOT_FOUND, indexOfLast(null, list(1)));
		assertEquals(NOT_FOUND, indexOfLast(null, list(1, 2)));

		assertEquals(NOT_FOUND, indexOfLast(list(1), null));
		assertEquals(NOT_FOUND, indexOfLast(list(1, 2), null));

		assertEquals(NOT_FOUND, indexOfLast(list(), list(1)));
		assertEquals(NOT_FOUND, indexOfLast(list(), list(1, 2)));
		assertEquals(NOT_FOUND, indexOfLast(list(1), list()));
		assertEquals(NOT_FOUND, indexOfLast(list(1, 2), list()));

		assertEquals(NOT_FOUND, indexOfLast(list(1), list(3)));
		assertEquals(NOT_FOUND, indexOfLast(list(1), list(3, 4)));
		assertEquals(NOT_FOUND, indexOfLast(list(1, 2), list(3)));
		assertEquals(NOT_FOUND, indexOfLast(list(1, 2), list(3, 4)));

		assertEquals(0, indexOfLast(list(1), list(1)));
		assertEquals(0, indexOfLast(list(1), list(1, 2)));
		assertEquals(0, indexOfLast(list(2), list(1, 2)));

		assertEquals(1, indexOfLast(list(0, 1), list(1)));
		assertEquals(1, indexOfLast(list(0, 1), list(1, 2)));
		assertEquals(1, indexOfLast(list(0, 2), list(1, 2)));

		assertEquals(0, indexOfLast(list(1, 0), list(1)));
		assertEquals(0, indexOfLast(list(1, 0), list(1, 2)));
		assertEquals(0, indexOfLast(list(2, 0), list(1, 2)));

		assertEquals(1, indexOfLast(list(1, 1), list(1)));
		assertEquals(1, indexOfLast(list(1, 1), list(1, 2)));
		assertEquals(1, indexOfLast(list(1, 2), list(1, 2)));
		assertEquals(1, indexOfLast(list(2, 1), list(1, 2)));
		assertEquals(1, indexOfLast(list(2, 2), list(1, 2)));
	}

    /**
     * Tests {@link CollectionUtil#isEmptyOrNull(Collection)}.
     */
    public void testIsEmptyOrNull() {
        assertTrue(CollectionUtil.isEmptyOrNull(createList(0)));
        assertTrue(CollectionUtil.isEmptyOrNull((List<?>) null));
        assertFalse(CollectionUtil.isEmptyOrNull(createList(3)));
    }

    /**
	 * Tests {@link CollectionUtil#toList(Iterable)}.
	 */
    public void testToList() {
        assertEquals(0, CollectionUtil.toList((Collection<?>)null).size());
        Collection<String> theCol = set("A", "B");
        assertTrue(CollectionUtil.toList(theCol).containsAll(theCol));
        assertEquals(2, CollectionUtil.toList(theCol).size());
        theCol = list("A", "B", "A");
        assertTrue(CollectionUtil.toList(theCol).containsAll(theCol));
        assertEquals(3, CollectionUtil.toList(theCol).size());

        assertEquals(0, CollectionUtil.toList((Object[])null).size());
        assertEquals(0, CollectionUtil.toList(new Object[0]).size());
        theCol = list("A", "B");
        Object[] theArray = {"A", "B"};
        assertTrue(CollectionUtil.toList(theArray).containsAll(theCol));
        assertEquals(2, CollectionUtil.toList(theArray).size());
        theCol = list("A", "B", "A");
        theArray = new Object[] {"A", "B", "A"};
        assertTrue(CollectionUtil.toList(theArray).containsAll(theCol));
        assertEquals(3, CollectionUtil.toList(theArray).size());
        
        assertEquals(list("A", "B", "A"), CollectionUtil.toList(Arrays.asList(theArray).iterator()));
    }

    /**
	 * Tests {@link CollectionUtil#asList(Object)}.
	 */
	public void testAsList() {
		assertEquals(list(), CollectionUtil.asList(null));
		assertEquals(list("A"), CollectionUtil.asList("A"));

		assertEquals(list(), CollectionUtil.asList(list()));
		assertEquals(list("A"), CollectionUtil.asList(list("A")));
		assertEquals(list("A", "B"), CollectionUtil.asList(list("A", "B")));
		assertEquals(list("A", "B", "C"), CollectionUtil.asList(list("A", "B", "C")));

		assertEquals(list(), CollectionUtil.asList(set()));
		assertEquals(list("A"), CollectionUtil.asList(set("A")));
		assertEquals(list("A", "B"), CollectionUtil.asList(set("A", "B")));
		assertEquals(list("A", "B", "C"), CollectionUtil.asList(set("A", "B", "C")));

		assertEquals(list(), CollectionUtil.asList(iterable()));
		assertEquals(list("A"), CollectionUtil.asList(iterable("A")));
		assertEquals(list("A", "B"), CollectionUtil.asList(iterable("A", "B")));
		assertEquals(list("A", "B", "C"), CollectionUtil.asList(iterable("A", "B", "C")));

		assertEquals(list(), CollectionUtil.asList(set().iterator()));
		assertEquals(list("A"), CollectionUtil.asList(set("A").iterator()));
		assertEquals(list("A", "B"), CollectionUtil.asList(set("A", "B").iterator()));
		assertEquals(list("A", "B", "C"), CollectionUtil.asList(set("A", "B", "C").iterator()));

		assertEquals(list(), CollectionUtil.asList(new int[0]));
		assertEquals(list("A"), CollectionUtil.asList(new String[] { "A" }));
		assertEquals(list("A", "B"), CollectionUtil.asList(new String[] { "A", "B" }));
		assertEquals(list("A", "B", "C"), CollectionUtil.asList(new String[] { "A", "B", "C" }));
	}

    /**
     * Tests {@link CollectionUtil#toSet(Collection)}.
     */
    public void testToSet() {
        assertEquals(0, CollectionUtil.toSet((Collection<?>)null).size());
        Collection<String> theCol = set("A", "B");
        assertTrue(CollectionUtil.toSet(theCol).containsAll(theCol));
        assertEquals(2, CollectionUtil.toList(theCol).size());
        theCol = list("A", "B", "A");
        assertTrue(CollectionUtil.toSet(theCol).containsAll(theCol));
        assertEquals(2, CollectionUtil.toSet(theCol).size());

        assertEquals(0, CollectionUtil.toSet((Object[])null).size());
        assertEquals(0, CollectionUtil.toSet(new Object[0]).size());
        theCol = list("A", "B");
        Object[] theArray = {"A", "B"};
        assertTrue(CollectionUtil.toSet(theArray).containsAll(theCol));
        assertEquals(2, CollectionUtil.toSet(theArray).size());
        theCol = list("A", "B", "A");
        theArray = new Object[] {"A", "B", "A"};
        assertTrue(CollectionUtil.toSet(theArray).containsAll(theCol));
        assertEquals(2, CollectionUtil.toSet(theArray).size());
        
        assertEquals(set("A", "B"), CollectionUtil.toSet(Arrays.asList(theArray).iterator()));
    }

	/**
	 * Tests {@link CollectionUtil#asSet(Object)}.
	 */
	public void testAsSet() {
		assertEquals(set(), CollectionUtil.asSet(null));
		assertEquals(set("A"), CollectionUtil.asSet("A"));

		assertEquals(set(), CollectionUtil.asSet(list()));
		assertEquals(set("A"), CollectionUtil.asSet(list("A")));
		assertEquals(set("A", "B"), CollectionUtil.asSet(list("A", "B")));
		assertEquals(set("A", "B", "C"), CollectionUtil.asSet(list("A", "B", "C")));

		assertEquals(set(), CollectionUtil.asSet(set()));
		assertEquals(set("A"), CollectionUtil.asSet(set("A")));
		assertEquals(set("A", "B"), CollectionUtil.asSet(set("A", "B")));
		assertEquals(set("A", "B", "C"), CollectionUtil.asSet(set("A", "B", "C")));

		assertEquals(set(), CollectionUtil.asSet(iterable()));
		assertEquals(set("A"), CollectionUtil.asSet(iterable("A")));
		assertEquals(set("A", "B"), CollectionUtil.asSet(iterable("A", "B")));
		assertEquals(set("A", "B", "C"), CollectionUtil.asSet(iterable("A", "B", "C")));

		assertEquals(set(), CollectionUtil.asSet(set().iterator()));
		assertEquals(set("A"), CollectionUtil.asSet(set("A").iterator()));
		assertEquals(set("A", "B"), CollectionUtil.asSet(set("A", "B").iterator()));
		assertEquals(set("A", "B", "C"), CollectionUtil.asSet(set("A", "B", "C").iterator()));

		assertEquals(set(), CollectionUtil.asSet(new int[0]));
		assertEquals(set("A"), CollectionUtil.asSet(new String[] { "A" }));
		assertEquals(set("A", "B"), CollectionUtil.asSet(new String[] { "A", "B" }));
		assertEquals(set("A", "B", "C"), CollectionUtil.asSet(new String[] { "A", "B", "C" }));
	}

	private Iterable<Object> iterable(Object... values) {
		return new Iterable<>() {
			@Override
			public Iterator<Object> iterator() {
				return Arrays.asList(values).iterator();
			}
		};
	}

    /**
	 * Tests {@link CollectionUtil#toMultiSet(Collection)}.
	 */
	public void testToMultiSet() {
		assertEquals(0, CollectionUtil.toMultiSet((Collection<?>) null).size());
		Collection<String> theCol = multiSet("A", "B");
		assertTrue(CollectionUtil.toMultiSet(theCol).containsAll(theCol));
		assertEquals(2, CollectionUtil.toList(theCol).size());
		theCol = list("A", "B", "A");
		assertTrue(CollectionUtil.toMultiSet(theCol).containsAll(theCol));
		assertEquals(3, CollectionUtil.toMultiSet(theCol).size());

		assertEquals(0, CollectionUtil.toMultiSet((Object[]) null).size());
		assertEquals(0, CollectionUtil.toMultiSet(new Object[0]).size());
		theCol = list("A", "B");
		Object[] theArray = { "B", "A" };
		assertTrue(CollectionUtil.toMultiSet(theArray).containsAll(theCol));
		assertEquals(2, CollectionUtil.toMultiSet(theArray).size());
		theCol = list("A", "B", "A");
		theArray = new Object[] { "A", "A", "B" };
		assertTrue(CollectionUtil.toMultiSet(theArray).containsAll(theCol));
		assertEquals(3, CollectionUtil.toMultiSet(theArray).size());

		assertEquals(multiSet("A", "A", "B"), CollectionUtil.toMultiSet(Arrays.asList(theArray).iterator()));
	}

	/**
	 * Tests {@link CollectionUtil#intoList(Object)}.
	 */
    public void testIntoList() {
        assertEquals(1, CollectionUtil.intoList(null).size());
        assertEquals(null, CollectionUtil.intoList(null).get(0));
        assertEquals(1, CollectionUtil.intoList("").size());
        assertEquals("", CollectionUtil.intoList("").get(0));
        List<String> list = CollectionUtil.intoList("A");
        list.add("B");
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));

        assertEquals(0, CollectionUtil.intoListNotNull(null).size());
        assertEquals(1, CollectionUtil.intoListNotNull("").size());
        assertEquals("", CollectionUtil.intoListNotNull("").get(0));
        list = CollectionUtil.intoListNotNull("A");
        list.add("B");
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }

    /**
     * Tests {@link CollectionUtil#getFirst(Collection)}.
     */
    public void testGetFirst() {
        List<String> a = new ArrayList<>();
		assertNull(CollectionUtil.getFirst(a));
		assertNull(CollectionUtil.getFirst(null));
        a.add("A");
        assertEquals("A", CollectionUtil.getFirst(a));
        a.add("B");
        assertEquals("A", CollectionUtil.getFirst(a));
        a.set(0, "C");
        assertEquals("C", CollectionUtil.getFirst(a));
        a.remove(0);
        assertEquals("B", CollectionUtil.getFirst(a));

        a = new LinkedList<>();
        assertEquals(null, CollectionUtil.getFirst(a));
        a.add("A");
        assertEquals("A", CollectionUtil.getFirst(a));
        a.add("B");
        assertEquals("A", CollectionUtil.getFirst(a));
        a.set(0, "C");
        assertEquals("C", CollectionUtil.getFirst(a));
        a.remove(0);
        assertEquals("B", CollectionUtil.getFirst(a));
    }

    /**
     * Tests {@link CollectionUtil#getSecond(Collection)}.
     */
    public void testGetSecond() {
        List<String> a = new ArrayList<>();
		assertNull(CollectionUtil.getSecond(a));
		assertNull(CollectionUtil.getSecond(null));
        a.add("A");
        assertEquals(null, CollectionUtil.getSecond(a));
        a.add("B");
        assertEquals("B", CollectionUtil.getSecond(a));
        a.add("C");
        assertEquals("B", CollectionUtil.getSecond(a));
        a.set(1, "D");
        assertEquals("D", CollectionUtil.getSecond(a));
        a.remove(1);
        assertEquals("C", CollectionUtil.getSecond(a));

        a = new LinkedList<>();
        assertEquals(null, CollectionUtil.getSecond(a));
        a.add("A");
        assertEquals(null, CollectionUtil.getSecond(a));
        a.add("B");
        assertEquals("B", CollectionUtil.getSecond(a));
        a.add("C");
        assertEquals("B", CollectionUtil.getSecond(a));
        a.set(1, "D");
        assertEquals("D", CollectionUtil.getSecond(a));
        a.remove(1);
        assertEquals("C", CollectionUtil.getSecond(a));
    }

    /**
     * Tests {@link CollectionUtil#getLast(Collection)}.
     */
    public void testGetLast() {
        List<String> a = new ArrayList<>();
		assertNull(CollectionUtil.getLast(a));
		assertNull(CollectionUtil.getLast(null));
        a.add("A");
        assertEquals("A", CollectionUtil.getLast(a));
        a.add("B");
        assertEquals("B", CollectionUtil.getLast(a));
        a.set(1, "C");
        assertEquals("C", CollectionUtil.getLast(a));
        a.remove(1);
        assertEquals("A", CollectionUtil.getLast(a));
        a.add(0, "D");
        assertEquals("A", CollectionUtil.getLast(a));

        a = new LinkedList<>();
        a.add("A");
        assertEquals("A", CollectionUtil.getLast(a));
        a.add("B");
        assertEquals("B", CollectionUtil.getLast(a));
        a.set(1, "C");
        assertEquals("C", CollectionUtil.getLast(a));
        a.remove(1);
        assertEquals("A", CollectionUtil.getLast(a));
        a.add(0, "D");
        assertEquals("A", CollectionUtil.getLast(a));
    }

    /**
     * Tests {@link CollectionUtil#intoSet(Object)}.
     */
    public void testIntoSet() {
        assertEquals(1, CollectionUtil.intoSet(null).size());
        assertEquals(null, CollectionUtil.intoSet(null).iterator().next());
        assertEquals(1, CollectionUtil.intoSet("").size());
        assertEquals("", CollectionUtil.intoSet("").iterator().next());
        Set<String> set = CollectionUtil.intoSet("A");
        set.add("B");
        assertEquals(2, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));

        assertEquals(0, CollectionUtil.intoSetNotNull(null).size());
        assertEquals(1, CollectionUtil.intoSetNotNull("").size());
        assertEquals("", CollectionUtil.intoSetNotNull("").iterator().next());
        set = CollectionUtil.intoSetNotNull("A");
        set.add("B");
        assertEquals(2, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
    }
    
    /**
     * Tests {@link CollectionUtil#removeDuplicates(List)}.
     */
    public void testRemoveDuplicates() {
        List<Object> testList = new ArrayList<>(3);
        Object o1, o2, o3;
        o1 = new NamedConstant("o1");
        o2 = new NamedConstant("o2");
        o3 = o1;
        
        testList.add(o1);
        testList.add(o2);
        testList.add(o3);
        
        assertEquals(testList.size(), 3);
        testList = CollectionUtil.removeDuplicates(testList);
        assertEquals(testList.size(), 2);
    }
    
    /**
	 * Tests {@link CollectionUtil#removeDuplicates(List)}.
	 */
	public void testRemoveDuplicatesSortedInline() {
		List<Object> testList = new ArrayList<>();
		Object o1 = new NamedConstant("o1");
		Object o2 = new NamedConstant("o2");

		testList.add(o1);
		testList.add(o1);
		testList.add(o2);
		testList.add(null);
		testList.add(null);

		assertEquals(testList.size(), 5);
		CollectionUtil.removeDuplicatesSortedInline(testList);
		assertEquals(testList.size(), 3);

		assertEquals(o1, testList.get(0));
		assertEquals(o2, testList.get(1));
		assertEquals(null, testList.get(2));
	}

	/**
	 * Tests {@link CollectionUtil#sortRemovingDuplicates(List)}.
	 */
	public void testSortRemovingDuplicates() {
		List<String> testList = new ArrayList<>();
		String o1 = "foo";
		String o2 = "bar";
		testList.add(null);
		testList.add(o1);
		testList.add(o2);
		testList.add(o1);
		testList.add(null);

		assertEquals(testList.size(), 5);
		CollectionUtil.sortRemovingDuplicates(testList);
		assertEquals(testList.size(), 3);
		assertEquals(null, testList.get(0));
		assertEquals(o1, testList.get(2));
	}

	/**
	 * Tests {@link CollectionUtil#sortRemovingDuplicates(List)} asserting no-op in trivial cases.
	 */
	public void testSortRemovingDuplicatesTrivial() {
		// No modification in trivial cases - safe on empty and singleton lists.
		CollectionUtil.removeDuplicatesSortedInline(Collections.<String> emptyList());
		CollectionUtil.removeDuplicatesSortedInline(Collections.singletonList("A"));
		CollectionUtil.sortRemovingDuplicates(Collections.<String> emptyList());
		CollectionUtil.sortRemovingDuplicates(Collections.singletonList("A"));
	}

	/**
	 * Tests {@link CollectionUtil#unmodifiableList(Collection, Collection)}.
	 */
    public void testUnmodifiableLists() {
        List<String> theUMList = CollectionUtil.unmodifiableList(list("A", "B"), new String[] {"C","D"});
        try {
            theUMList.add("E");
            fail("Tried to add an object to an unmodifiableList");
        } catch (UnsupportedOperationException e) { /* expected */ }
    }
    
    /**
	 * Tests {@link CollectionUtil#unmodifiableList(Collection)}
	 */
	public void testUnmodifiableListCopy() {
		List<Integer> original = new ArrayList<>();
		original.add(1);
		List<Integer> copy = CollectionUtil.unmodifiableList(original);
		assertEquals(original, copy);
		original.add(2);
		assertFalse(copy.contains(2));
		original.clear();
		assertEquals(1, copy.size());
		assertTrue(copy.contains(1));
		try {
			copy.add(2);
			fail("UnmodifiableList did not throw an Exception when it was changed!");
		} catch (UnsupportedOperationException ex) {
			// Expected
		}
	}

    /**
     * Tests {@link CollectionUtil#unmodifiableSet(Collection, Collection)}.
     */
    public void testUnmodifiableSet() {
        Set<String> theUMSet = CollectionUtil.unmodifiableSet(set("A","B"), new String[] {"C","D"});
        try {
            theUMSet.add("E");
            fail("Tried to add an object to an unmodifiableSet");
        } catch (UnsupportedOperationException e) { /* expected */ }
    }
    
    /**
	 * Tests {@link CollectionUtil#unmodifiableSet(Collection)}
	 */
	public void testUnmodifiableSetCopy() {
		Set<Integer> original = new HashSet<>();
		original.add(1);
		Set<Integer> copy = CollectionUtil.unmodifiableSet(original);
		assertEquals(original, copy);
		original.add(2);
		assertFalse(copy.contains(2));
		original.clear();
		assertEquals(1, copy.size());
		assertTrue(copy.contains(1));
		try {
			copy.add(2);
			fail("UnmodifiableSet did not throw an Exception when it was changed!");
		} catch (UnsupportedOperationException ex) {
			// Expected
		}
	}

    /**
     * Tests {@link CollectionUtil#getFirstElementsAsList(Collection, int)}
     */
    public void testgetFirstElementsAsList() {
        // Case 1: number < size
        List<Object> list5 = CollectionUtil.getFirstElementsAsList(createList(10), 5);
        assertEquals(list5.size(), 5);
        assertEquals(list5.get(0), Integer.valueOf(0));
        assertEquals(list5.get(1), Integer.valueOf(1));
        assertEquals(list5.get(2), Integer.valueOf(2));
        assertEquals(list5.get(3), Integer.valueOf(3));
        assertEquals(list5.get(4), Integer.valueOf(4));
        
        // Case 2: number = size
        List<Object> list10 = CollectionUtil.getFirstElementsAsList(createList(10), 10);
        assertEquals(list10.size(), 10);
        
        // Case 3: number > size
        List<Object> list15 = CollectionUtil.getFirstElementsAsList(createList(15), 20);
        assertEquals(list15.size(), 15);
        
        // Case 4: number = 0
        List<Object> list2 = CollectionUtil.getFirstElementsAsList(createList(2), 0);
        assertEquals(list2.size(), 0);
        
       // Case 5: number < 0
        List<Object> list3 = CollectionUtil.getFirstElementsAsList(createList(3), -1);
        assertEquals(list3.size(), 0);
    }
    
    /**
     * Tests {@link CollectionUtil#getIterator(Object)}
     */
    public void testGetIterator() {
    	TestIteratorUtil.reusableTestGetIterator();
    }
    
    /**
     * Tests {@link CollectionUtil#union2(Set, Set)}
     */
    public void testUnion2() {
        assertNull(CollectionUtil.union2(null                 , null));
        assertNull(CollectionUtil.union2(Collections.emptySet(), null));
        assertSame(Collections.emptySet(), CollectionUtil.union2(null,Collections.emptySet()));
        assertSame(Collections.emptySet(), CollectionUtil.union2(Collections.emptySet(),Collections.emptySet()));
        
        Set<String> ab = set("a","b");
        assertSame(ab, CollectionUtil.union2(ab,Collections.<String>emptySet()));
        assertSame(ab, CollectionUtil.union2(Collections.<String>emptySet(),ab));

        Set<String> a = Collections.singleton("a");
        Set<String> b = Collections.singleton("b");
        assertEquals(ab, CollectionUtil.union2(a,b));
        assertEquals(ab, CollectionUtil.union2(b,a));
    }

    /**
     * Tests {@link CollectionUtil#union(Collection, Collection)}
     */
    public void testUnion() {
        assertEquals(Collections.emptySet(), CollectionUtil.union(Collections.emptySet(),Collections.emptySet()));
        
        Set<String> ab = set("a","b");
        assertEquals(ab, CollectionUtil.union(ab,Collections.emptySet()));
        assertEquals(ab, CollectionUtil.union(Collections.emptySet(),ab));

        Set<String> a = Collections.singleton("a");
        Set<String> b = Collections.singleton("b");
        assertEquals(ab, CollectionUtil.union(a,b));
        assertEquals(ab, CollectionUtil.union(b,a));
    }
    
    public void testUnionOfCollection() {
    	List<String> l1 = list("a", "a", "b");
    	List<String> l2 = list();
    	Set<String> s1 = set("a", "b" ,"c");
    	Set<String> s2 = set("d");
    	
    	
    	Collection<Collection<String>> src1 = set(l1, l2, s1, s2);
    	assertEquals(set("a", "b", "c","d"), CollectionUtil.union(src1));
    	Collection<Collection<String>> src2 = set(l1, l2, s1, s2, null);
    	assertEquals(set("a", "b", "c","d"), CollectionUtil.union(src2));
    	Collection<Collection<String>> src3 = set();
    	assertEquals(Collections.emptySet(), CollectionUtil.union(src3));
    	Collection<Collection<String>> src4 = Collections.emptySet();
    	assertEquals(Collections.emptySet(), CollectionUtil.union(src4));
    }
    
    /**
     * Tests {@link CollectionUtil#symmetricDifference(Set, Set)}
     */
    public void testSymmetricDifference() {
		Object one = new Object();
		Object two = new Object();
		Object three = new Object();
		Set<Object> set1 = set(one, two);
		Set<Object> set2 = set(two, three);
		Set<Object> set3 = set(three);
		
		assertTrue(CollectionUtil.symmetricDifference(set1, set2).contains(one));
		assertTrue(!CollectionUtil.symmetricDifference(set1, set2).contains(two));
		assertTrue(CollectionUtil.symmetricDifference(set1, set2).contains(three));

		assertEquals("Symmetric Difference is not commutative", CollectionUtil.symmetricDifference(set1, set2), CollectionUtil.symmetricDifference(
				set2, set1));
		assertEquals("Symmetric Difference is not associative", CollectionUtil.symmetricDifference(CollectionUtil.symmetricDifference(set1, set2),
				set3), CollectionUtil.symmetricDifference(set1, CollectionUtil.symmetricDifference(set2, set3)));

		assertEquals(set1, CollectionUtil.symmetricDifference(set1, null));
		assertEquals(set2, CollectionUtil.symmetricDifference(Collections.emptySet(), set2));
		assertNotNull(CollectionUtil.symmetricDifference(null, null));
	}
    
    /**
     * Tests {@link CollectionUtil#cleanUp(Collection)}
     */
    public void testCleanUp() {
    	ArrayList<Object> list = new ArrayList<>();
    	String nul = "null";
    	Object o = new Object();
		list.add(nul);
		list.add(null);
		list.add(o);
		list.add(null);
		CollectionUtil.cleanUp(list);
		assertEquals(2, list.size() );
		assertEquals(nul, list.get(0));
		assertEquals(o, list.get(1));
    }
    
    /**
     * Tests {@link CollectionUtil#reflexiveHull(Map)}
     */
    public void testReflexiveHull() {
    	Map<String, Set<String>> graph = new HashMap<>();
    	
		MultiMaps.add(graph, "a", "b");
		MultiMaps.add(graph, "b", "c");
    	
    	CollectionUtil.reflexiveHull(graph);
    	
    	assertEquals(set("a", "b"), graph.get("a"));
    	assertEquals(set("b", "c"), graph.get("b"));
    	assertNull(graph.get("c"));
    }
    
    /**
     * Tests {@link CollectionUtil#transitiveHullAcyclic(Map)}
     */
    public void testTransitiveHull() {
    	Map<String, Set<String>> graph = new HashMap<>();
    	
		MultiMaps.add(graph, "a", "b");
		MultiMaps.add(graph, "b", "c");
		MultiMaps.add(graph, "b", "d");
		MultiMaps.add(graph, "d", "e");
    	
    	CollectionUtil.transitiveHullAcyclic(graph);
    	
    	assertEquals(set("b", "c", "d", "e"), graph.get("a"));
    	assertEquals(set("c", "d", "e"), graph.get("b"));
    	assertNull(graph.get("c"));
    	assertEquals(set("e"), graph.get("d"));
    }
    
    /**
	 * Tests {@link CollectionUtil#topsort(java.util.function.Function, Collection, boolean)}.
	 */
    public void testTopsort() {
    	Map<String, Set<String>> graph = new HashMap<>();

		MultiMaps.add(graph, "a", "b");
		MultiMaps.add(graph, "a", "c");
		MultiMaps.add(graph, "c", "b");
		MultiMaps.add(graph, "c", "d");
		MultiMaps.add(graph, "b", "d");
    	
    	Mapping<String, Set<String>> dependencies = createDependenciesMapping(graph);
		List<String> sorted = topsort(dependencies, list("a", "b", "c", "d"), true);
    	
    	assertEquals(list("d", "b", "c", "a"), sorted);
    }

    /**
	 * Tests cycle detection in
	 * {@link CollectionUtil#topsort(java.util.function.Function, Collection, boolean)}
	 */
    public void testTopsortCycle() {
    	Map<String, Set<String>> graph = new HashMap<>();
    	
		MultiMaps.add(graph, "a", "b");
		MultiMaps.add(graph, "b", "c");
		MultiMaps.add(graph, "c", "d");
		MultiMaps.add(graph, "d", "a");
    	
    	try {
			Mapping<String, Set<String>> dependencies = createDependenciesMapping(graph);
			topsort(dependencies, list("c"), true);
			fail("Must detect cycle.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
    }

	/**
	 * Tests that {@link CollectionUtil#topsort(java.util.function.Function, Collection, boolean)}
	 * adds missing dependencies when the flag "addDependencies" is "true".
	 */
	public void testTopsort_addDependencies_true() {
		Map<String, Set<String>> graph = new HashMap<>();

		MultiMaps.add(graph, "a", "b");
		MultiMaps.add(graph, "a", "c");
		MultiMaps.add(graph, "c", "b");
		MultiMaps.add(graph, "c", "d");
		MultiMaps.add(graph, "b", "d");

		Mapping<String, Set<String>> dependencies = createDependenciesMapping(graph);
		List<String> sorted = topsort(dependencies, list("a"), true);

		assertEquals(list("d", "b", "c", "a"), sorted);
	}

	/**
	 * Tests that {@link CollectionUtil#topsort(java.util.function.Function, Collection, boolean)}
	 * does not add missing dependencies when the flag "addDependencies" is "false".
	 */
	public void testTopsort_addDependencies_false() {
		Map<String, Set<String>> graph = new HashMap<>();

		MultiMaps.add(graph, "a", "b");
		MultiMaps.add(graph, "a", "c");
		MultiMaps.add(graph, "c", "b");
		MultiMaps.add(graph, "c", "d");
		MultiMaps.add(graph, "b", "d");

		Mapping<String, Set<String>> dependencies = createDependenciesMapping(graph);

		assertEquals(list("a"), topsort(dependencies, list("a"), false));
		assertEquals(list("d", "a"), topsort(dependencies, list("a", "d"), false));
	}

	private <T> Mapping<T, Set<T>> createDependenciesMapping(Map<T, Set<T>> graph) {
		return Mappings.createMapBasedMapping(graph, Collections.<T> emptySet());
	}

	public void testInsertPosition_Empty() {
		int insertPosition = insertPosition(BasicTestCase.<Integer> list(), 2, intComparator());
		assertEquals(0, insertPosition);
	}

	public void testInsertPosition_Start() {
		int insertPosition = insertPosition(list(3), 2, intComparator());
		assertEquals(0, insertPosition);
	}

	public void testInsertPosition_End() {
		int insertPosition = insertPosition(list(1), 2, intComparator());
		assertEquals(1, insertPosition);
	}

	public void testInsertPosition_Middle() {
		int insertPosition = insertPosition(list(1, 3), 2, intComparator());
		assertEquals(1, insertPosition);
	}

	public void testInsertPosition_Conflict() {
		int insertPosition = insertPosition(list(2), 2, intComparator());
		assertEquals(1, insertPosition);
	}

	public void testInsertPosition_MultipleConflicts() {
		int insertPosition = insertPosition(list(2, 2, 2), 2, intComparator());
		assertEquals(3, insertPosition);
	}

	public void testInsertPosition_ConflictStart() {
		int insertPosition = insertPosition(list(2, 3), 2, intComparator());
		assertEquals(1, insertPosition);
	}

	public void testInsertPosition_ConflictEnd() {
		int insertPosition = insertPosition(list(1, 2), 2, intComparator());
		assertEquals(2, insertPosition);
	}

	public void testInsertPosition_ConflictMiddle() {
		int insertPosition = insertPosition(list(1, 2, 3), 2, intComparator());
		assertEquals(2, insertPosition);
	}

	private Comparator<Integer> intComparator() {
		return ComparableComparator.<Integer> comparableComparator();
	}

	public void testIntersection() {
		Set<String> set1 = set("a", "b", "c");
		Set<String> set2 = set("a", "b", "c");
		Set<String> set3 = set("b", "c", "d");
		Set<String> set4 = set();
		assertEquals(set1, CollectionUtil.intersection(set1, set1));
		assertEquals(set1, CollectionUtil.intersection(set1, set2));
		assertEquals(set("b", "c"), CollectionUtil.intersection(set1, set3));
		assertEquals(CollectionUtil.intersection(set1, set3), CollectionUtil.intersection(set3, set1));
		assertEquals(Collections.emptySet(), CollectionUtil.intersection(set1, set4));

		List<String> l1 = list("a", "b", "a", "b");
		List<String> l2 = list("c", "b", "b");
		List<String> l3 = list("d");
		List<String> l4 = list();
		assertEquals(set("a", "b"), CollectionUtil.intersection(l1, set1));
		assertEquals(set("b"), CollectionUtil.intersection(l1, l2));
		assertEquals(set("b"), CollectionUtil.intersection(l1, l2));
		assertEquals(Collections.emptySet(), CollectionUtil.intersection(l1, l3));
		assertEquals(Collections.emptySet(), CollectionUtil.intersection(l1, l4));
	}
	
	public void testToIterable() {
		TestIteratorUtil.reusableTestToIterable();
	}

	public void testCopyOnlyCollection() {
		assertEmpty(true, copyOnly(Object.class, Collections.emptyList()));
		assertEmpty(true, copyOnly(Object.class, Collections.singletonList(null)));

		class ConcreteClass { /* Nothing needed */ }
		class ConcreteSubclass  extends ConcreteClass  { /* Nothing needed */ }
		ConcreteClass concreteClassInstance = new ConcreteClass();
		ConcreteSubclass concreteSubclassInstance = new ConcreteSubclass();
		
		List<?> exampleList = CollectionUtil.createList(1, concreteClassInstance, concreteSubclassInstance);
		assertEquals(exampleList, copyOnly(Object.class, exampleList));
		assertEquals(Collections.singletonList(1), copyOnly(Integer.class, exampleList));
		assertEquals(Collections.emptyList(), copyOnly(String.class, exampleList));
		assertEquals(CollectionUtil.createList(concreteClassInstance, concreteSubclassInstance), copyOnly(ConcreteClass.class, exampleList));
	}

	public void testCopyOnlySet() {
		assertEmpty(true, copyOnly(Object.class, Collections.emptySet()));
		assertEmpty(true, copyOnly(Object.class, Collections.singleton(null)));

		class ConcreteClass { /* Nothing needed */ }
		class ConcreteSubclass  extends ConcreteClass  { /* Nothing needed */ }
		ConcreteClass concreteClassInstance = new ConcreteClass();
		ConcreteSubclass concreteSubclassInstance = new ConcreteSubclass();
		
		Set<?> exampleSet = CollectionUtil.createSet(1, concreteClassInstance, concreteSubclassInstance);
		assertEquals(exampleSet, copyOnly(Object.class, exampleSet));
		assertEquals(Collections.singleton(1), copyOnly(Integer.class, exampleSet));
		assertEquals(Collections.emptySet(), copyOnly(String.class, exampleSet));
		assertEquals(CollectionUtil.createSet(concreteClassInstance, concreteSubclassInstance), copyOnly(ConcreteClass.class, exampleSet));
	}
	
	public void testMoveEntry() {
		assertEquals(list(1, 2, 3, 4, 5), doMoveEntry(list(1, 2, 3, 4, 5), 0, 0));
		assertEquals(list(2, 1, 3, 4, 5), doMoveEntry(list(1, 2, 3, 4, 5), 0, 1));
		assertEquals(list(1, 2, 3, 4, 5), doMoveEntry(list(1, 2, 3, 4, 5), 4, 4));
		assertEquals(list(2, 3, 4, 5, 1), doMoveEntry(list(1, 2, 3, 4, 5), 0, 4));
		assertEquals(list(5, 1, 2, 3, 4), doMoveEntry(list(1, 2, 3, 4, 5), 4, 0));
		assertEquals(list(1, 2, 4, 3, 5), doMoveEntry(list(1, 2, 3, 4, 5), 2, 3));
	}
	
	public void testMoveEntryCheckUpper() {
		List<Integer> list = list(1, 2, 3, 4, 5);
		try {
			CollectionUtil.moveEntry(list, 0, 5);
			fail("Error expected.");
		} catch (IndexOutOfBoundsException ex) {
			// expected
		}
		assertEquals(list(1, 2, 3, 4, 5), list);
	}

	public void testMoveEntryCheckLower() {
		List<Integer> list = list(1, 2, 3, 4, 5);
		try {
			CollectionUtil.moveEntry(list, 4, -1);
			fail("Error expected.");
		} catch (IndexOutOfBoundsException ex) {
			// expected
		}
		assertEquals(list(1, 2, 3, 4, 5), list);
	}
	
    private List<?> doMoveEntry(List<?> list, int from, int to) {
    	CollectionUtil.moveEntry(list, from, to);
		return list;
	}

	/**
     * If parameter is zero, an empty list will be returned, 
     * otherwise a list will be returned with the given length.
     * 
     * @param aLength of the list. If zero, empty list will be returned.
     * @return list with aLength of integers
     */
    private static Collection<Object> createList(int aLength) {
        ArrayList<Object> result = new ArrayList<>(aLength);
        for(int i=0;i<aLength;i++) {
            result.add(Integer.valueOf(i));
        }
        return result;
    }

	public void testCompareInt() {
		assertCompareInt(0, 1);
		assertCompareInt(Integer.MIN_VALUE, 1);
		assertCompareInt(1, Integer.MAX_VALUE);
	}

	private void assertCompareInt(int small, int greater) {
		assertTrue(CollectionUtil.compareInt(small, greater) < 0);
		assertTrue(CollectionUtil.compareInt(greater, small) > 0);
		assertTrue(CollectionUtil.compareInt(small, small) == 0);
		assertTrue(CollectionUtil.compareInt(greater, greater) == 0);
	}

	public void testCompareLong() {
		assertCompareLong(0, 1);
		assertCompareLong(Long.MIN_VALUE, 1);
		assertCompareLong(1, Long.MAX_VALUE);
	}

	private void assertCompareLong(long small, long greater) {
		assertTrue(CollectionUtil.compareLong(small, greater) < 0);
		assertTrue(CollectionUtil.compareLong(greater, small) > 0);
		assertTrue(CollectionUtil.compareLong(small, small) == 0);
		assertTrue(CollectionUtil.compareLong(greater, greater) == 0);
	}

	public void testCompareComparableNullIsSmaller() {
		assertCompareComparableNullIsSmaller(null, "AAA");
		assertCompareComparableNullIsSmaller("AAA", "BBB");
		assertCompareComparableNullIsSmaller("AAA", "aaa");
	}

	private void assertCompareComparableNullIsSmaller(String small, String greater) {
		assertTrue(CollectionUtil.compareComparableNullIsSmaller(small, greater) < 0);
		assertTrue(CollectionUtil.compareComparableNullIsSmaller(greater, small) > 0);
		assertTrue(CollectionUtil.compareComparableNullIsSmaller(small, small) == 0);
		assertTrue(CollectionUtil.compareComparableNullIsSmaller(greater, greater) == 0);
	}

	public void testCompareComparableNullIsGreater() {
		assertCompareComparableNullIsGreater("AAA", null);
		assertCompareComparableNullIsGreater("AAA", "BBB");
		assertCompareComparableNullIsGreater("AAA", "aaa");
	}

	private void assertCompareComparableNullIsGreater(String small, String greater) {
		assertTrue(CollectionUtil.compareComparableNullIsGreater(small, greater) < 0);
		assertTrue(CollectionUtil.compareComparableNullIsGreater(greater, small) > 0);
		assertTrue(CollectionUtil.compareComparableNullIsGreater(small, small) == 0);
		assertTrue(CollectionUtil.compareComparableNullIsGreater(greater, greater) == 0);
	}

	public void testHashCodeLong() {
		assertTrue(CollectionUtil.hashCodeLong(1L) != 0);
		assertTrue(CollectionUtil.hashCodeLong(1L << 32) != 0);
	}

	public void testLazyListAdd() {
		List<String> lazyList = null;
		lazyList = CollectionUtil.lazyAdd(lazyList, "foo");
		lazyList = CollectionUtil.lazyAdd(lazyList, "bar");
		lazyList = CollectionUtil.nonNull(lazyList);
		assertEquals(list("foo", "bar"), lazyList);
	}

	public void testLazyListAddAllNone() {
		List<String> lazyList = null;
		lazyList = CollectionUtil.lazyAddAll(lazyList, null);
		lazyList = CollectionUtil.lazyAddAll(lazyList, Collections.<String> emptySet());
		assertNull(lazyList);
		lazyList = CollectionUtil.nonNull(lazyList);
		assertEquals(list(), lazyList);
	}

	public void testLazyListAddAllSome() {
		List<String> lazyList = null;
		lazyList = CollectionUtil.lazyAddAll(lazyList, list("foo", "bar"));
		lazyList = CollectionUtil.nonNull(lazyList);
		assertEquals(list("foo", "bar"), lazyList);
	}

	public void testLazySetAdd() {
		Set<String> lazyset = null;
		lazyset = CollectionUtil.lazyAdd(lazyset, "foo");
		lazyset = CollectionUtil.lazyAdd(lazyset, "bar");
		lazyset = CollectionUtil.nonNull(lazyset);
		assertEquals(set("foo", "bar"), lazyset);
	}

	public void testLazySetAddAllNone() {
		Set<String> lazySet = null;
		lazySet = CollectionUtil.lazyAddAll(lazySet, null);
		lazySet = CollectionUtil.lazyAddAll(lazySet, Collections.<String> emptySet());
		assertNull(lazySet);
		lazySet = CollectionUtil.nonNull(lazySet);
		assertEquals(set(), lazySet);
	}

	public void testLazySetAddAllSome() {
		Set<String> lazySet = null;
		lazySet = CollectionUtil.lazyAddAll(lazySet, list("foo", "bar"));
		lazySet = CollectionUtil.nonNull(lazySet);
		assertEquals(set("foo", "bar"), lazySet);
	}

	public void testLazyMapPut() {
		Map<String, String> lazyMap = null;
		lazyMap = CollectionUtil.lazyPut(lazyMap, "foo", "x");
		lazyMap = CollectionUtil.lazyPut(lazyMap, "bar", "y");
		lazyMap = CollectionUtil.nonNull(lazyMap);
		assertEquals(set("foo", "bar"), lazyMap.keySet());
	}

	public void testLazyMapPutAllNone() {
		Map<String, String> lazyMap = null;
		lazyMap = CollectionUtil.lazyPutAll(lazyMap, null);
		lazyMap = CollectionUtil.lazyPutAll(lazyMap, Collections.<String, String> emptyMap());
		assertNull(lazyMap);
		lazyMap = CollectionUtil.nonNull(lazyMap);
		assertEquals(new HashMap<>(), lazyMap);
	}

	public void testLazyMapPutAllSome() {
		Map<String, String> lazyMap = null;
		Map<String, String> values = new MapBuilder<String, String>().put("foo", "x").put("bar", "y").toMap();
		lazyMap = CollectionUtil.lazyPutAll(lazyMap, values);
		lazyMap = CollectionUtil.nonNull(lazyMap);
		assertEquals(set("foo", "bar"), lazyMap.keySet());
	}

	public void testRemovePrefix() {
		assertThrows(RuntimeException.class, () -> removePrefix(list(), -1));
		assertThrows(RuntimeException.class, () -> removePrefix(list(), 1));
		assertThrows(RuntimeException.class, () -> removePrefix(list("a"), -1));
		assertThrows(RuntimeException.class, () -> removePrefix(list("a"), 2));
		assertEquals(list(), removePrefix(list(), 0));
		assertEquals(list("a"), removePrefix(list("a"), 0));
		assertEquals(list(), removePrefix(list("a"), 1));
		assertEquals(list("a", "b", "c", "d"), removePrefix(list("a", "b", "c", "d"), 0));
		assertEquals(list("b", "c", "d"), removePrefix(list("a", "b", "c", "d"), 1));
		assertEquals(list("c", "d"), removePrefix(list("a", "b", "c", "d"), 2));
		assertEquals(list("d"), removePrefix(list("a", "b", "c", "d"), 3));
		assertEquals(list(), removePrefix(list("a", "b", "c", "d"), 4));
	}

	public void testRemoveSuffix() {
		assertThrows(RuntimeException.class, () -> removeSuffix(list(), -1));
		assertThrows(RuntimeException.class, () -> removeSuffix(list(), 1));
		assertThrows(RuntimeException.class, () -> removeSuffix(list("a"), -1));
		assertThrows(RuntimeException.class, () -> removeSuffix(list("a"), 2));
		assertEquals(list(), removeSuffix(list(), 0));
		assertEquals(list("a"), removeSuffix(list("a"), 0));
		assertEquals(list(), removeSuffix(list("a"), 1));
		assertEquals(list("a", "b", "c", "d"), removeSuffix(list("a", "b", "c", "d"), 0));
		assertEquals(list("a", "b", "c"), removeSuffix(list("a", "b", "c", "d"), 1));
		assertEquals(list("a", "b"), removeSuffix(list("a", "b", "c", "d"), 2));
		assertEquals(list("a"), removeSuffix(list("a", "b", "c", "d"), 3));
		assertEquals(list(), removeSuffix(list("a", "b", "c", "d"), 4));
	}

	public void testGetCommonPrefix() {
		assertCommonPrefix(list(), list(), list());
		assertCommonPrefix(list("a"), list(), list());
		assertCommonPrefix(list("a"), list("a"), list("a"));
		assertCommonPrefix(list("a"), list("b"), list());
		assertCommonPrefix(list("a", "a"), list(), list());
		assertCommonPrefix(list("a", "a"), list("a"), list("a"));
		assertCommonPrefix(list("a", "a"), list("b"), list());
		assertCommonPrefix(list("a", "a"), list("a", "a"), list("a", "a"));
		assertCommonPrefix(list("a", "a"), list("a", "b"), list("a"));
		assertCommonPrefix(list("a", "a"), list("b", "a"), list());
		assertCommonPrefix(list("a", "a"), list("b", "b"), list());
		assertCommonPrefix(list("a", "b"), list(), list());
		assertCommonPrefix(list("a", "b"), list("a"), list("a"));
		assertCommonPrefix(list("a", "b"), list("b"), list());
		assertCommonPrefix(list("a", "b"), list("c"), list());
		assertCommonPrefix(list("a", "b"), list("a", "a"), list("a"));
		assertCommonPrefix(list("a", "b"), list("a", "b"), list("a", "b"));
		assertCommonPrefix(list("a", "b"), list("a", "c"), list("a"));
		assertCommonPrefix(list("a", "b"), list("b", "a"), list());
		assertCommonPrefix(list("a", "b"), list("b", "b"), list());
		assertCommonPrefix(list("a", "b"), list("b", "c"), list());
		assertCommonPrefix(list("a", "b"), list("c", "a"), list());
		assertCommonPrefix(list("a", "b"), list("c", "b"), list());
		assertCommonPrefix(list("a", "b"), list("c", "c"), list());
	}

	private void assertCommonPrefix(List<?> first, List<?> second, List<?> commonPrefix) {
		assertEquals(commonPrefix, getCommonPrefix(first, second));
		/* Assert that the order of the two lists is irrelevant. (That is not always necessary, for
		 * example if the two lists are equal anyway, but it's not worth the effort to filter those
		 * few tests out.) */
		assertEquals(commonPrefix, getCommonPrefix(second, first));
	}

	public void testGetCommonSuffix() {
		assertCommonSuffix(list(), list(), list());
		assertCommonSuffix(list("a"), list(), list());
		assertCommonSuffix(list("a"), list("a"), list("a"));
		assertCommonSuffix(list("a"), list("b"), list());
		assertCommonSuffix(list("a", "a"), list(), list());
		assertCommonSuffix(list("a", "a"), list("a"), list("a"));
		assertCommonSuffix(list("a", "a"), list("b"), list());
		assertCommonSuffix(list("a", "a"), list("a", "a"), list("a", "a"));
		assertCommonSuffix(list("a", "a"), list("a", "b"), list());
		assertCommonSuffix(list("a", "a"), list("b", "a"), list("a"));
		assertCommonSuffix(list("a", "a"), list("b", "b"), list());
		assertCommonSuffix(list("a", "b"), list(), list());
		assertCommonSuffix(list("a", "b"), list("a"), list());
		assertCommonSuffix(list("a", "b"), list("b"), list("b"));
		assertCommonSuffix(list("a", "b"), list("c"), list());
		assertCommonSuffix(list("a", "b"), list("a", "a"), list());
		assertCommonSuffix(list("a", "b"), list("a", "b"), list("a", "b"));
		assertCommonSuffix(list("a", "b"), list("a", "c"), list());
		assertCommonSuffix(list("a", "b"), list("b", "a"), list());
		assertCommonSuffix(list("a", "b"), list("b", "b"), list("b"));
		assertCommonSuffix(list("a", "b"), list("b", "c"), list());
		assertCommonSuffix(list("a", "b"), list("c", "a"), list());
		assertCommonSuffix(list("a", "b"), list("c", "b"), list("b"));
		assertCommonSuffix(list("a", "b"), list("c", "c"), list());
	}

	private void assertCommonSuffix(List<?> first, List<?> second, List<?> commonSuffix) {
		assertEquals(commonSuffix, getCommonSuffix(first, second));
		/* Assert that the order of the two lists is irrelevant. (That is not always necessary, for
		 * example if the two lists are equal anyway, or if the reverse case is tested explicitly,
		 * but it's not worth the effort to filter those few tests out.) */
		assertEquals(commonSuffix, getCommonSuffix(second, first));
	}

	public void testGetAdded() {
		assertGetAdded(list(), list(), list());
		assertGetAdded(list(), list("a"), list("a"));
		assertGetAdded(list(), list("a", "a"), list("a", "a"));
		assertGetAdded(list(), list("a", "b"), list("a", "b"));
		assertGetAdded(list("a"), list(), list());
		assertGetAdded(list("a"), list("a"), list());
		assertGetAdded(list("a"), list("a", "a"), list("a"));
		assertGetAdded(list("a"), list("a", "b"), list("b"));
		assertGetAdded(list("a"), list("b"), list("b"));
		assertGetAdded(list("a"), list("b", "a"), list("b"));
		assertGetAdded(list("a"), list("b", "b"), list("b", "b"));
		assertGetAdded(list("a", "b"), list(), list());
		assertGetAdded(list("a", "b"), list("a"), list());
		assertGetAdded(list("a", "b"), list("a", "a"), list("a"));
		assertGetAdded(list("a", "b"), list("a", "b"), list());
		assertGetAdded(list("a", "b"), list("a", "c"), list("c"));
		assertGetAdded(list("a", "b"), list("b"), list());
		assertGetAdded(list("a", "b"), list("b", "a"), list());
		assertGetAdded(list("a", "b"), list("b", "b"), list("b"));
		assertGetAdded(list("a", "b"), list("b", "c"), list("c"));
		assertGetAdded(list("a", "b"), list("c"), list("c"));
		assertGetAdded(list("a", "b"), list("c", "a"), list("c"));
		assertGetAdded(list("a", "b"), list("c", "b"), list("c"));
		assertGetAdded(list("a", "b"), list("c", "c"), list("c", "c"));
	}

	private void assertGetAdded(List<?> oldEntries, List<?> newEntries, List<?> added) {
		assertEquals(added, getAdded(oldEntries, newEntries));
	}

	public void testGetRemoved() {
		assertGetRemoved(list(), list(), list());
		assertGetRemoved(list(), list("a"), list());
		assertGetRemoved(list(), list("a", "a"), list());
		assertGetRemoved(list(), list("a", "b"), list());
		assertGetRemoved(list("a"), list(), list("a"));
		assertGetRemoved(list("a"), list("a"), list());
		assertGetRemoved(list("a"), list("a", "a"), list());
		assertGetRemoved(list("a"), list("a", "b"), list());
		assertGetRemoved(list("a"), list("b"), list("a"));
		assertGetRemoved(list("a"), list("b", "a"), list());
		assertGetRemoved(list("a"), list("b", "b"), list("a"));
		assertGetRemoved(list("a", "b"), list(), list("a", "b"));
		assertGetRemoved(list("a", "b"), list("a"), list("b"));
		assertGetRemoved(list("a", "b"), list("a", "a"), list("b"));
		assertGetRemoved(list("a", "b"), list("a", "b"), list());
		assertGetRemoved(list("a", "b"), list("a", "c"), list("b"));
		assertGetRemoved(list("a", "b"), list("b"), list("a"));
		assertGetRemoved(list("a", "b"), list("b", "a"), list());
		assertGetRemoved(list("a", "b"), list("b", "b"), list("a"));
		assertGetRemoved(list("a", "b"), list("b", "c"), list("a"));
		assertGetRemoved(list("a", "b"), list("c"), list("a", "b"));
		assertGetRemoved(list("a", "b"), list("c", "a"), list("b"));
		assertGetRemoved(list("a", "b"), list("c", "b"), list("a"));
		assertGetRemoved(list("a", "b"), list("c", "c"), list("a", "b"));
	}

	private void assertGetRemoved(List<?> oldEntries, List<?> newEntries, List<?> removed) {
		assertEquals(removed, getRemoved(oldEntries, newEntries));
	}

	public void testSingletonOrEmptyList() {
		assertEquals(list(), singletonOrEmptyList(null));
		assertEquals(list("Hallo"), singletonOrEmptyList("Hallo"));
		assertEquals(list(""), singletonOrEmptyList(""));
	}

	public void testSingletonOrEmptySet() {
		assertEquals(set(), singletonOrEmptySet(null));
		assertEquals(set("Hallo"), singletonOrEmptySet("Hallo"));
		assertEquals(set(""), singletonOrEmptySet(""));
	}

    public static Test suite () {
       return BasicTestSetup.createBasicTestSetup(new TestSuite (TestCollectionUtil.class));
    }
 
    /** Main function for direct execution */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
  
}
