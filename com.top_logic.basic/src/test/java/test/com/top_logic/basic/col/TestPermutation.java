/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.FastReversePermutation;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.LazyReversePermutation;
import com.top_logic.basic.col.Permutation;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.StartsWithFilter;

/**
 * Test {@link Permutation}.
 * 
 * TODO use {@link Permutation#INVALID}
 * 
 * @author     <a href="mailto:dna@top-logic.com">dna</a>
 */
@SuppressWarnings("javadoc")
public class TestPermutation extends BasicTestCase {
		
    static final int BASE_SIZE=5;
    
    /** 
     * Create a new TestPermutation.
     */
    public TestPermutation(String aName) {
        super(aName);
    }

    /** 
     * Allow creation of subclasses of Permutation.
     */
    protected Permutation createPermutation(int size) {
        return new Permutation(size);
    }
		
    /** 
     * Allow creation of subclasses of Permutation.
     */
    protected Permutation createPermutation(int size, String aString) {
        return new Permutation(size, aString);
    }

	public void testIdentiy() {
	    Permutation thePermutation = createPermutation(BASE_SIZE);
        try {
            thePermutation.map(-17);
			fail("Expected failure.");
		} catch (IndexOutOfBoundsException expected) {
			// Expected.
		}

        assertEquals(0, thePermutation.map(0));
		assertEquals(1, thePermutation.map(1));
		assertEquals(4, thePermutation.map(4));
		
		thePermutation = createPermutation(thePermutation.getBaseSize(), thePermutation.store());

		try {
		    thePermutation.map(BASE_SIZE);
		} catch (IndexOutOfBoundsException expected) {  }
        
		try {
            thePermutation.map(Integer.MAX_VALUE);
        } catch (IndexOutOfBoundsException expected) {  }
        
        ArrayList<Integer> identity1 = Permutation.identity(BASE_SIZE);
        ArrayList<Integer> identity2 = new ArrayList<>(77);
        Permutation.resetIdentity(identity2, BASE_SIZE);
        assertEquals(identity1, identity2);
	}

	
	public void testPermute(){
	    Permutation thePermutation = createPermutation(5);

	    thePermutation.permute(1, 3);
		assertEquals(1 ,thePermutation.map(3));
		assertEquals(3 ,thePermutation.map(1));

		thePermutation.permute(1, 3);
		assertEquals(1 ,thePermutation.map(1));
		assertEquals(3 ,thePermutation.map(3));
	}
	
	/**
	 * What does reverse do?
	 * Permutation p;
	 * p.reverse(p.map(x)); // = x	 
	 */
	public void testReverse(){
	    Permutation thePermutation = createPermutation(6);

	    thePermutation.permute(1, 3);
		thePermutation.permute(3, 5);
		
		assertEquals(0, thePermutation.reverse(0));
		assertEquals(5, thePermutation.reverse(1));
		assertEquals(2, thePermutation.reverse(2));
		assertEquals(1, thePermutation.reverse(3));
		assertEquals(4, thePermutation.reverse(4));
		assertEquals(3, thePermutation.reverse(5));	
		
		thePermutation.permute(3, 5);
		thePermutation.permute(1, 3);

		assertEquals(0, thePermutation.reverse(0));
        assertEquals(1, thePermutation.reverse(1));
        assertEquals(2, thePermutation.reverse(2));
        assertEquals(3, thePermutation.reverse(3));
        assertEquals(4, thePermutation.reverse(4));
        assertEquals(5, thePermutation.reverse(5));
        
        String store =  thePermutation.store();
        thePermutation = createPermutation(thePermutation.getBaseSize(), store);
        assertEquals(6, thePermutation.getSize());
        
        assertEquals(0, thePermutation.reverse(0));
        assertEquals(1, thePermutation.reverse(1));
        assertEquals(2, thePermutation.reverse(2));
        assertEquals(3, thePermutation.reverse(3));
        assertEquals(4, thePermutation.reverse(4));
        assertEquals(5, thePermutation.reverse(5));

	}

	/**
	 * Test Permutation with lots of Data.
	 */
	public void testBig(){
	    
        final int BIG  = 2048;
	    final int BIG2 = BIG >>> 1;
	    
	    Permutation thePermutation = createPermutation(BIG);

	    List<Integer> aList = new ArrayList<>(BIG);
		for (int i = 0; i < BIG; i++) {
			aList.add(i, Integer.valueOf(i));
		}
		List<Integer> newList = thePermutation.map(aList);
		
		assertTrue(aList.containsAll(newList));
        assertTrue(newList.containsAll(aList));
        assertTrue(newList.equals(aList));
        assertTrue(aList.equals(newList));
		
		thePermutation.permute(BIG2 - 1 , BIG2 + 1);
	    String store =  thePermutation.store();
	    thePermutation = createPermutation(thePermutation.getBaseSize(), store);
	 
	    newList = thePermutation.map(aList);
		
        assertTrue(aList.containsAll(newList));
        assertTrue(newList.containsAll(aList));
        assertFalse(newList.equals(aList));
        assertFalse(aList.equals(newList));
	}
	
	/**
	 * Test a sort resulting in a reverse permutation.
	 */
	public void testSort(){
	    
	    final int SIZE = Short.MAX_VALUE;
	    Permutation thePermutation = createPermutation(SIZE);

        List<Short> theList = new ArrayList<>(SIZE);
		for (short s = SIZE; s >= 0; s--) {
			theList.add(s);
		}
		startTime();
		thePermutation.sort(theList, ComparableComparator.INSTANCE);
		assertEquals(0       , thePermutation.map(SIZE - 1));
        assertEquals(SIZE - 1, thePermutation.map(0));
        logTime(thePermutation + " sort ");

        assertSorted(thePermutation.map(theList));
	}
	
	public void testFilter(){
		List<Integer> aList = new ArrayList<>(10);
		for (int i = 0; i < 10; i++) { 
			aList.add(i, i);
		}
        Permutation thePermutation = createPermutation(aList.size());

		Filter<? super Object> aFilter =
			FilterFactory.or(new EqualsFilter(Integer.valueOf(2)), new EqualsFilter(Integer.valueOf(4)));
		thePermutation.filter(aList, aFilter);
		assertEquals(2, thePermutation.map(0));
		assertEquals(4, thePermutation.map(1));
		
		assertEquals(2, thePermutation.getSize());
        assertEquals(10, thePermutation.getBaseSize());

        try {
            thePermutation.map(2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {  }

        assertEquals(0                  , thePermutation.reverse(2));
        assertEquals(1                  , thePermutation.reverse(4));
        assertEquals(Permutation.INVALID, thePermutation.reverse(3));

		Filter<Object> secFilter = new EqualsFilter(Integer.valueOf(2));
        thePermutation.filter(aList, secFilter);
        assertEquals(2, thePermutation.map(0));
	}


	public void testEmpty(){
        Permutation thePermutation = createPermutation(0);
        assertTrue(thePermutation.isEmpty());
        assertEquals(0, thePermutation.getSize());
        assertEquals(0, thePermutation.getBaseSize());

        thePermutation = createPermutation(6);
		List<Integer> aList = new ArrayList<>(6);
		for (int i = 0; i < 6; i++) {
			aList.add(i, Integer.valueOf(i));
		}
		Filter<? super Object> aFilter = FilterFactory.falseFilter();
		thePermutation.filter(aList, aFilter);
		assertTrue  (thePermutation.isEmpty());
        assertEquals(0, thePermutation.getSize());
        assertEquals(6, thePermutation.getBaseSize());
       
        thePermutation = createPermutation(thePermutation.getBaseSize(), thePermutation.store());
        assertTrue  (thePermutation.isEmpty());
        assertEquals(0, thePermutation.getSize());
        assertEquals(6, thePermutation.getBaseSize());
	}
	
	/**
	 * Test for intended Usage (e.g TableViewModel)
	 */
    public void testRealLive() {
        
        final int SIZE = 20000;
        final int FILT = 335;
        Random rand = new Random(101010101010101010L);
        
        List<Object> theNames = new ArrayList<>(SIZE);
        for (int i=0; i < SIZE; i++) {
            theNames.add(StringServices.getRandomString(rand, 5 + rand.nextInt(20)));
        }
        
        Filter             theFilter     = new StartsWithFilter("M"); 
        Comparator<Object> theComparator = Collator.getInstance(Locale.GERMAN);

        Permutation thePermutation = createPermutation(theNames.size());

        startTime();
        thePermutation.filter(theNames, theFilter);
        thePermutation.sort  (theNames, theComparator);
        logTime(thePermutation.toString() + " filter, filter & sort");
        
        assertEquals(FILT           , thePermutation.getSize());
        assertEquals(theNames.size(), thePermutation.getBaseSize());
        
        thePermutation.reset();
        assertEquals(theNames.size() , thePermutation.getSize());

        thePermutation = createPermutation(theNames.size());

        startTime();
        thePermutation.sort  (theNames, theComparator);
        thePermutation.filter(theNames, theFilter);
        logTime(thePermutation.toString() + " sort, filter");

        assertEquals(FILT           , thePermutation.getSize());
        assertEquals(theNames.size(), thePermutation.getBaseSize());
        
        thePermutation.reset();
        assertEquals(theNames.size() , thePermutation.getSize());
    }
        
    public void testMapList() {
    	List<String> original = Arrays.asList(new String[] {"a", "b", "c", "d", "e"});
    	
    	Permutation pi = createPermutation(original.size());
    	
    	pi.permute(1, 4);
    	
    	List<String> view = pi.map(original);
    	
    	assertEquals("a", view.get(0));
    	assertEquals("e", view.get(1));
    	assertEquals("b", view.get(4));
    	
    	// Test that view is actually a view.
    	
    	original.set(1, "x");
    	
    	assertEquals("x", view.get(4));
    }
    
    public static class TestFastReversePermutation extends TestPermutation {

        // Constructors
        
        /** 
         * Create a new TestFastReversePermutation.
         */
        public TestFastReversePermutation(String aName) {
            super(aName);
        }
        
        @Override
		protected Permutation createPermutation(int aSize) {
            return new FastReversePermutation(aSize);
        }
        
        @Override
		protected Permutation createPermutation(int aSize, String aString) {
            return new FastReversePermutation(aSize, aString);
        }

    }

    public static class TestLazyReversePermutation extends TestPermutation {

        /** 
         * Create a new TestLazyReversePermutation.
         */
        public TestLazyReversePermutation(String aName) {
            super(aName);
        }
        
        @Override
		protected Permutation createPermutation(int aSize) {
            return new LazyReversePermutation(aSize);
        }
        
        @Override
		protected Permutation createPermutation(int aSize, String aString) {
            return new LazyReversePermutation(aSize, aString);
        }
    }

    /** 
     * Returns a suite of tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("TestPermutation");
        
        suite.addTestSuite(TestPermutation           .class);
        suite.addTestSuite(TestLazyReversePermutation.class);
        suite.addTestSuite(TestFastReversePermutation.class);
        
        return BasicTestSetup.createBasicTestSetup(suite);
    }
    
    /**
     * This main function is for direct testing.
     */
    public static void main(String[] args) {
        SHOW_TIME = true;
    	junit.textui.TestRunner.run (suite ());
    }    
}
