/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.NotEqualsFilter;
import com.top_logic.basic.col.filter.SameFilter;
import com.top_logic.basic.col.filter.SetFilter;

/**
 * Testcase for the classes implementing  {@link com.top_logic.basic.col.Filter}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestFilters extends TestCase {

    private final class UnusableFilter implements Filter<Object> {
    	public UnusableFilter() {
    		// Default constructor.
		}
    	
		@Override
		public boolean accept(Object anObject) {
			throw new UnsupportedOperationException();
		}
	}

	/**
     * Constructor for TestFilters.
     * 
     * @param name the function to execute for testing.
     */
    public TestFilters(String name) {
        super(name);
    }

    public void testAND() {
        Filter<? super Object> and = FilterFactory.and(); 
        assertNotNull(and.toString());
        assertTrue(and.accept(and));
        
        and = FilterFactory.and(and, FilterFactory.trueFilter());
        assertTrue(and.accept(and));
        and = FilterFactory.and(and, FilterFactory.falseFilter());
        assertFalse(and.accept(and));
        
        and = FilterFactory.and((new Filter[] {FilterFactory.trueFilter(), FilterFactory.falseFilter()}));
        assertFalse(and.accept(and));

        assertNotNull(and.toString());
    }

    public void testOR() {
        Filter<? super Object> or = FilterFactory.or(); 
        assertFalse(or.accept(or));
        
        or = FilterFactory.or(or, FilterFactory.falseFilter());
        assertFalse(or.accept(or));
        or = FilterFactory.or(or, FilterFactory.trueFilter());
        assertTrue(or.accept(or));
        
        or = FilterFactory.or(new Filter[] {FilterFactory.trueFilter(), FilterFactory.falseFilter()});
        assertTrue(or.accept(or));
        
        assertNotNull(or.toString());
    }

    public void testSimplifyAnd() {
    	UnusableFilter unusableFilter = new UnusableFilter();
    	
    	assertEquals(FilterFactory.trueFilter(), FilterFactory.and(Collections.<Filter<Object>>emptyList()));
		assertEquals(unusableFilter, FilterFactory.and(Arrays.asList(unusableFilter)));
		assertEquals(FilterFactory.falseFilter(), FilterFactory.and(Arrays.asList(unusableFilter, FilterFactory.falseFilter())));
    }

    public void testSimplifyOr() {
    	UnusableFilter unusableFilter = new UnusableFilter();
    	
    	assertEquals(FilterFactory.falseFilter(), FilterFactory.or(Arrays.<Filter<Object>>asList()));
		assertEquals(unusableFilter, FilterFactory.or(Arrays.asList(unusableFilter)));
		assertEquals(FilterFactory.trueFilter(), FilterFactory.or(Arrays.asList(unusableFilter, FilterFactory.trueFilter())));
    }

    
    /** Test the {@link com.top_logic.basic.col.filter.EqualsFilter}. */
    public void testEquals() {
        EqualsFilter equals = new EqualsFilter("X"); 
        assertTrue (equals.accept("X"));
        assertFalse(equals.accept("U"));
        try {
            new EqualsFilter(null);
            fail("Must not allow EqualsFilter with null"); 
        } catch (Exception expected) { /* expected */  }
        
        assertNotNull(equals.toString());
    }

    /** Test the {@link com.top_logic.basic.col.filter.SameFilter}. */
    public void testSame() {
        SameFilter same = new SameFilter("X"); 
        assertTrue (same.accept("X"));
        assertFalse(same.accept(new String("X")));
        assertFalse(same.accept("U"));
        assertNotNull(same.toString());

        // Same filter with null is ok 
        same = new SameFilter(null); 
        assertTrue (same.accept(null));
        assertFalse(same.accept("U"));

        assertNotNull(same.toString());
    }

    /** Test the {@link com.top_logic.basic.col.filter.NotEqualsFilter}. */
    public void testNotEquals() {
        NotEqualsFilter nequals = new NotEqualsFilter("X"); 
        assertFalse(nequals.accept("X"));
        assertTrue (nequals.accept("U"));
        try {
            new NotEqualsFilter(null);
            fail("Must not allow NotEqualsFilter with null"); 
        } catch (Exception expected) { /* expected */ }
        
        assertNotNull(nequals.toString());
    }

    /** Test the {@link com.top_logic.basic.col.filter.NOTFilter}. */
    public void testNOT() {
        Filter not = FilterFactory.not(FilterFactory.trueFilter()); 
        assertFalse(not.accept(this));
        
        not = FilterFactory.not(FilterFactory.falseFilter()); 
        assertTrue(not.accept(this));
    }

	/** Test {@link FilterFactory#createClassFilter(String)}. */
    public void testStringClass() {
    	Filter<? super Object> filter = FilterFactory.createClassFilter(String.class);
    	assertTrue(filter.accept(""));
    	assertFalse(filter.accept(null));
    	assertFalse(filter.accept(new Object()));
    }
    	
	/** Test {@link FilterFactory#createClassFilter(Class)}. */
    public void testSerializableClass() {
    	Filter<? super Object> filter = FilterFactory.createClassFilter(Serializable.class);
    	assertTrue(filter.accept(""));
    	assertFalse(filter.accept(new Object()));
    	assertTrue(filter.accept(new Serializable() { /* no impl */ }));
		assertTrue(filter.accept(Integer.valueOf(13)));
    }

	/**
	 * Test {@link FilterFactory#createClassFilter(Class)} with {@link Number} class.
	 */
    public void testNumberClass() throws ClassNotFoundException {
    	Filter<? super Object> filter = FilterFactory.createClassFilter("java.lang.Number");
    	assertFalse(filter.accept(""));
    	assertFalse(filter.accept(new Object()));
		assertTrue(filter.accept(Integer.valueOf(42)));
		assertTrue(filter.accept(Double.valueOf(42)));
		assertTrue(filter.accept(Float.valueOf(42)));
    }

    public void testNullClass() {
    	try {
    		FilterFactory.createClassFilter((Class) null);
    		fail("Expected NullPointerException");
    	} catch (NullPointerException expected) { /* expected */ }
    }
    
    /** Test the {@link com.top_logic.basic.col.filter.SetFilter}. */
    public void testSet() {
        SetFilter set = new SetFilter(Collections.EMPTY_SET); 
        assertFalse(set.accept(set));

        set = new SetFilter(Collections.singleton(this)); 
        assertTrue(set.accept(this));
        
        assertNotNull(set.toString());
    }

    /** 
     * Return the suite of Test to execute.
     */
    public static Test suite() {
        return new TestSuite(TestFilters.class);
        // return new TestFileManager("testXXX"));
    }

    /**
     * Main function for direct testing.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite ());
    }    


}
