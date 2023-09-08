/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SameFilter;

/**
 * The TestFilterUtil tests the class {@link FilterUtil}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@SuppressWarnings("javadoc")
public class TestFilterUtil extends BasicTestCase {

	public void testClassFilterList() {
		List<Object> input = BasicTestCase.<Object> list(1, "2", 3);
		assertEquals(list(1, 3), FilterUtil.filterList(Integer.class, input));

		Iterable<Object> iterableInput = input;
		assertEquals(list(1, 3), FilterUtil.filterList(Integer.class, iterableInput));
	}

	public void testClassFilterSet() {
		List<Object> input = BasicTestCase.<Object> list(1, "2", 3);
		assertEquals(set(1, 3), FilterUtil.filterSet(Integer.class, input));

		Iterable<Object> iterableInput = input;
		assertEquals(set(1, 3), FilterUtil.filterSet(Integer.class, iterableInput));
	}

    public void testFilter() {
        ArrayList<Object> theList = new ArrayList<>();
        theList.add("Hello");
        theList.add("Hi");
        theList.add(new ArrayList<>());
        theList.add(new Object());
        
        Filter<? super Object> theClassFilter = FilterFactory.createClassFilter(String.class);
        
        Collection<Object> theFilteredList = FilterUtil.filterList(theClassFilter, theList);
        assertTrue  (theFilteredList.contains("Hello"));
        assertTrue  (theFilteredList.contains("Hi"));
        assertEquals(theFilteredList.size(), 2);
        
        assertEquals(set("Hello", "Hi"), FilterUtil.filterSet(theClassFilter, theList));
        
        Filter<Object> theSameFilter = new SameFilter("Hello");
        List<Filter<? super Object>>   theFilters    = new ArrayList<>();
        theFilters.add(theClassFilter);
        theFilters.add(theSameFilter);
        
        assertTrue  (FilterUtil.filterAnd(theFilters, theList).contains("Hello"));
        assertEquals(FilterUtil.filterAnd(theFilters, theList).size(), 1);
    }
    
    /** 
     * Test Filter functions using indexes
     */
    public void testFilterIndex() {
        ArrayList<Object> theList = new ArrayList<>();
        theList.add("String");
        theList.add(null);
        theList.add(this);
        theList.add(theList);
        theList.add("another String");
        
        ArrayList<Object> filteredList = new ArrayList<>(2);
        Filter<? super Object>    theFilter    = FilterFactory.createClassFilter(String.class);
        FilterUtil.filterInto(filteredList, theFilter, theList);
        
        assertEquals(2               , filteredList.size());
        assertEquals("String"        , filteredList.get(0));
        assertEquals("another String", filteredList.get(1));
        
        filteredList.clear();

		FilterUtil.filterInto(filteredList, String.class, theList);

		assertEquals(2, filteredList.size());
		assertEquals("String", filteredList.get(0));
		assertEquals("another String", filteredList.get(1));

		filteredList.clear();

        FilterUtil.filterSublistInto(theFilter, theList, 1, 4, filteredList, 0);
        assertTrue(filteredList.isEmpty());
        
        filteredList.clear();

        filteredList.add("Oink");
        FilterUtil.filterSublistInto(theFilter, theList, 1, 5, filteredList, 1);

        assertEquals(2               , filteredList.size());
        assertEquals("Oink"          , filteredList.get(0));
        assertEquals("another String", filteredList.get(1));
    }

    /** 
     * No comment.
     */
    public void testVerifyElements() {
        // Test a list with elements with different types
        ArrayList<Object> theDefectiveList = new ArrayList<>();
        theDefectiveList.add(new String("A"));
        theDefectiveList.add(new String("B"));
		theDefectiveList.add(Integer.valueOf(4));
        theDefectiveList.add(new String("C"));
        
        assertFalse(FilterUtil.containsOnly(String.class, theDefectiveList));
        
        // Test a list with elements with the same type
        ArrayList<String> theCorrectList = new ArrayList<>();
        theCorrectList.add(new String("A"));
        theCorrectList.add(new String("B"));
        theCorrectList.add(new String("C"));
        
        assertTrue(FilterUtil.containsOnly(String.class, theCorrectList));
        
        class A {}
        class B extends A {}
        class C extends B {}
        
        ArrayList<A> list1 = new ArrayList<>();
        list1.add(new A());
        list1.add(new B());
        
        assertTrue(FilterUtil.containsOnly(A.class, list1));
        assertFalse(FilterUtil.containsOnly(B.class, list1));
        
        ArrayList<A> list2 = new ArrayList<>();
        list2.add(new B());
        list2.add(new C());
        
        assertTrue(FilterUtil.containsOnly(A.class, list2));
        assertTrue(FilterUtil.containsOnly(B.class, list2));
        assertFalse(FilterUtil.containsOnly(C.class, list2));
    }
    
    public void testGroupByLargeTail() {
    	List<String> source = Arrays.asList(new String[] {"a", "b", "b", "b", "c", "d", "e", "f", "f", "f"});
		List<List<String>> grouped1 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source));
		List<List<String>> grouped2 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source.iterator()));
		List<List<String>> expected = list(
			list("a"),
			list("b", "b", "b"),
			list("c"),
			list("d"),
			list("e"),
			list("f", "f", "f")
		);
		
		assertEquals(expected, grouped1);
		assertEquals(expected, grouped2);
    }
    
    public void testGroupBySingleTail() {
    	List<String> source = Arrays.asList(new String[] {"a", "b", "b", "b", "c"});
    	List<List<String>> grouped1 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source));
    	List<List<String>> grouped2 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source.iterator()));
    	List<List<String>> expected = list(
			list("a"),
			list("b", "b", "b"),
			list("c")
    	);
    	
    	assertEquals(expected, grouped1);
    	assertEquals(expected, grouped2);
    }
    
    public void testGroupBySingle() {
    	List<String> source = Arrays.asList(new String[] {"a"});
    	List<List<String>> grouped1 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source));
    	List<List<String>> grouped2 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source.iterator()));
    	List<List<String>> expected = list(
    		list("a")
    	);
    	
    	assertEquals(expected, grouped1);
    	assertEquals(expected, grouped2);
    }
    
    public void testGroupBySingleGroup() {
    	List<String> source = Arrays.asList(new String[] {"a", "a"});
    	List<List<String>> grouped1 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source));
    	List<List<String>> grouped2 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source.iterator()));
    	List<List<String>> expected = list(
    		list("a", "a")
    	);
    	
    	assertEquals(expected, grouped1);
    	assertEquals(expected, grouped2);
    }
    
    public void testGroupByPair() {
    	List<String> source = Arrays.asList(new String[] {"a", "b"});
    	List<List<String>> grouped1 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source));
    	List<List<String>> grouped2 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source.iterator()));
    	List<List<String>> expected = list(
    		list("a"),
    		list("b")
    	);
    	
    	assertEquals(expected, grouped1);
    	assertEquals(expected, grouped2);
    }
    
    public void testGroupByFail() {
    	List<String> source = Arrays.asList(new String[] {"b", "a"});
    	try {
    		CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source));
    		fail("unreachable");
    	} catch (IllegalArgumentException ex) {
    		// Expected.
    	}
    	try {
    		CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source.iterator()));
    		fail("unreachable");
    	} catch (IllegalArgumentException ex) {
    		// Expected.
    	}
    }
    
    public void testGroupByLargeStart() {
    	List<String> source = Arrays.asList(new String[] {"a", "a", "a", "b"});
    	List<List<String>> grouped1 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source));
    	List<List<String>> grouped2 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source.iterator()));
    	List<List<String>> expected = list(
    		list("a", "a", "a"),
    		list("b")
    	);
    	
    	assertEquals(expected, grouped1);
    	assertEquals(expected, grouped2);
    }
    
    public void testGroupByEmpty() {
    	List<String> source = Arrays.asList(new String[] {});
    	List<List<String>> grouped1 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source));
    	List<List<String>> grouped2 = CollectionUtil.toList(FilterUtil.groupBySorted(ComparableComparator.INSTANCE, source.iterator()));
    	List<?> expected = list();
    	
    	assertEquals(expected, grouped1);
    	assertEquals(expected, grouped2);
    }

    public void testGroupByRelativeComparision() {
    	Comparator<String> relativeComparator = new Comparator<>() {

			@Override
			public int compare(String o1, String o2) {
				int difference = o1.charAt(0) - o2.charAt(0);
				if (difference < -1) {
					return -1;
				} else if (difference > -1) {
					return 1;
				} else {
					return 0;
				}
			}
    		
    	};
    	List<String> source = Arrays.asList(new String[] {"a", "b", "c", "e", "f", "i", "k", "l", "m", "x"});
		List<List<String>> grouped1 = CollectionUtil.toList(FilterUtil.groupBySorted(relativeComparator, source));
		List<List<String>> grouped2 = CollectionUtil.toList(FilterUtil.groupBySorted(relativeComparator, source.iterator()));
		List<List<String>> expected = list(
			list("a", "b", "c"),
			list("e", "f"),
			list("i"),
			list("k", "l", "m"),
			list("x")
		);
		
		assertEquals(expected, grouped1);
		assertEquals(expected, grouped2);
    }
    
    public void testAcceptEach() {
    	Filter<Object> stringFilter = new Filter<>() {
			@Override
			public boolean accept(Object anObject) {
				return anObject instanceof String;
			}
    	};
    	Set<Object> s = new HashSet<>();
    	s.add("kjg");
    	s.add("yasg");
    	s.add("xdfg");
    	assertTrue(FilterUtil.matchAll(s, stringFilter));
    	
    	s.add(Boolean.TRUE);
    	assertFalse(FilterUtil.matchAll(s, stringFilter));
    }
    
    public void testAcceptAtLeastOne() {
    	Filter<Object> stringFilter = new Filter<>() {
    		@Override
			public boolean accept(Object anObject) {
    			return anObject instanceof String;
    		}
    	};
    	Set<Object> s = new HashSet<>();
    	s.add(Boolean.TRUE);
    	s.add(Integer.valueOf(42));
    	s.add(new HashSet<>());
    	assertFalse(FilterUtil.hasMatch(s, stringFilter));
    	
    	s.add("liuhz");
    	assertTrue(FilterUtil.hasMatch(s, stringFilter));
    }
    
	/** 
     * This method returns the test suite.
     */
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestFilterUtil.class));
    }

    /**
     * The main function can be used for direct testing.
     */
    public static void main(String[] args) {
        // Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

}

