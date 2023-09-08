/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterIterator;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;

/**
 * Test class to check the {@link com.top_logic.basic.col.FilteredIterator}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestFilterIterator extends TestCase {

    /** A Vector to generate iterators from. */
    private static List iteratorGenerator;
    
    /**
     * Constructor to conduct a special test.
     *
     * @param name name of the test to execute.
     */
    public TestFilterIterator (String name) {
        super (name);
    }

    /** Prepare for the test by creating a temporary file.
     */
    @Override
	protected void setUp () throws Exception {
        
        if (iteratorGenerator != null)    // avoid unneeded, duplicate setup
            return;
    
        iteratorGenerator = new ArrayList(5);
		iteratorGenerator.add(Integer.valueOf(-10));
		iteratorGenerator.add(Integer.valueOf(10));
		iteratorGenerator.add(Double.valueOf(-5));
		iteratorGenerator.add(Float.valueOf(10));
		iteratorGenerator.add(Byte.valueOf((byte) 127));

    }

    // Test methodes

    /** Test using Empty Iterator for trivial cases.
     */
    public void testEmpty () throws Exception
    {
        Iterator filter = new FilterIterator(
        	Collections.EMPTY_LIST.iterator(), new NumberFilter (42));
    
        assertTrue(!filter.hasNext());
        NoSuchElementException expected = null;
        try {
            filter.next();
        }
        catch (NoSuchElementException nse)  {
            expected = nse;
        }    
        assertTrue("Expected NoSuchElementException ?", expected != null);
    }

    /** Test expected usage of FilteredIterator.
     */
    public void testNormal () throws Exception
    {
        Iterator filter = new FilterIterator(
            iteratorGenerator.iterator(), new NumberFilter (42));
    
        assertTrue  (filter.hasNext());
		assertEquals(Byte.valueOf((byte) 127), filter.next());
        assertTrue  (!filter.hasNext());

        filter = new FilterIterator(iteratorGenerator.iterator(), new NumberFilter (0));

        assertTrue  (filter.hasNext());
		assertEquals(Integer.valueOf(10), filter.next());
        assertTrue  (filter.hasNext());
		assertEquals(Float.valueOf(10), filter.next());
        assertTrue  (filter.hasNext());
		assertEquals(Byte.valueOf((byte) 127), filter.next());
        assertTrue  (!filter.hasNext());
    }

    /** Test out Of order usage of FilteredIterator.
     */
    public void testOutOfOrder () throws Exception
    {
        Iterator filter = new FilterIterator(
            iteratorGenerator.iterator(), new NumberFilter (42));
    
        assertTrue  (filter.hasNext());
        assertTrue  (filter.hasNext());
        assertTrue  (filter.hasNext());
		assertEquals(Byte.valueOf((byte) 127), filter.next());
        assertTrue  (!filter.hasNext());
        assertTrue  (!filter.hasNext());

        filter = new FilterIterator(iteratorGenerator.iterator(), new NumberFilter (0));

        assertTrue  (filter.hasNext());
		assertEquals(Integer.valueOf(10), filter.next());
        assertTrue  (filter.hasNext());
		assertEquals(Float.valueOf(10), filter.next());
        assertTrue  (filter.hasNext());
        assertTrue  (filter.hasNext());
		assertEquals(Byte.valueOf((byte) 127), filter.next());
        assertTrue  (!filter.hasNext());
    }
    
    /** Test with an anonymous inner class
     */
    public void testAnoymous () throws Exception
    {
        Filter integerFilter = new Filter() {
             /** Allow Integer Only */
            @Override
			public boolean accept(Object aNumber) {
                return aNumber instanceof Integer;
            }
        };        
        
        Iterator filter = new FilterIterator(
            iteratorGenerator.iterator(),integerFilter);
  
        assertTrue  (filter.hasNext());
		assertEquals(Integer.valueOf(-10), filter.next());
        assertTrue  (filter.hasNext());
		assertEquals(Integer.valueOf(10), filter.next());
        assertTrue  (!filter.hasNext());

        filter = new FilterIterator(iteratorGenerator.iterator(),
                    FilterFactory.falseFilter());
    
        assertTrue  (!filter.hasNext());
    }
    
    /** Test static functions of FilteredIterator.
     */
    public void testStatic() throws Exception
    {
        Filter zeroFilter = new NumberFilter (0);
        
        Collection filtered = FilterUtil.filterInline(zeroFilter, new ArrayList(iteratorGenerator));
        
        assertEquals(3, filtered.size());
        
        ArrayList someList = new ArrayList(3);
        FilterUtil.filterInto(someList, zeroFilter, iteratorGenerator);

        assertEquals(3, someList.size());
    }

    /** A Filter that comapres numbers to a given value. */
    public static class NumberFilter implements Filter {
        
        /** value used for testing */
        private int compareTo;

        /** 
         * Filter the return only Object &gt; greaterThan.
         */
        public NumberFilter (int greaterThan) {
           compareTo = greaterThan;
        }
        
        /** Compare the number against compareTo */
        @Override
		public boolean accept(Object aNumber) {
            return ((Number) aNumber).intValue() > compareTo;
        }
    }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestFilterIterator.class);
        // TestSuite suite = new TestSuite();
        // suite.addTest(new TestFilterIterator("testEmpty"));
        return suite;
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
