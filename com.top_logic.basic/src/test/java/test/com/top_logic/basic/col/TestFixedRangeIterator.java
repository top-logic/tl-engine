/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.FixedRangeIterator;
import com.top_logic.basic.col.IDRangeIterator;

/**
 * This class tests the class {@link com.top_logic.basic.col.FixedRangeIterator}.
 * 
 * @author     <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestFixedRangeIterator extends BasicTestCase {

    /**
     * This method starts all tests for the class
     * {@link com.top_logic.basic.col.FixedRangeIterator}.
     */
    public void testAll(){
        /* 
         * Test the iterator with a null instead of a collection. 
         */
        Collection      nullCollection = null;
        IDRangeIterator fixedIterator  = null;
        fixedIterator = new TestedFixedRangeIterator(nullCollection);
        assertNotNull(fixedIterator);
        assertNull(fixedIterator.nextObject());
        assertNull(fixedIterator.nextObject());
        
        /* 
         * Test the iterator with an empty Collection. 
         */
        Collection emptyCollection = new ArrayList();
        fixedIterator = null;
        fixedIterator = new TestedFixedRangeIterator(emptyCollection);
        assertNotNull(fixedIterator);
        assertNull(fixedIterator.nextObject());
        assertNull(fixedIterator.nextObject());
        
        /* 
         * Test the iterator with a collection with three strings. 
         */
        Collection stringCollection = new ArrayList();
        stringCollection.add("Hallo");
        stringCollection.add("Hi");
        stringCollection.add("Hm");
        fixedIterator = null;
        fixedIterator = new TestedFixedRangeIterator(stringCollection);
        assertNotNull(fixedIterator);
        Object first  = fixedIterator.nextObject();
        assertNotNull(first);
        assertInstanceof(first, String.class);
        assertEquals("Hallo", (String)first);
        Object second = fixedIterator.nextObject();
        assertNotNull(second);
        assertInstanceof(second, String.class);
        assertEquals("Hi", (String)second);
        Object third  = fixedIterator.nextObject();
        assertNotNull(third);
        assertInstanceof(third, String.class);
        assertEquals("Hm", (String)third);
        assertNull(fixedIterator.nextObject());
        assertNull(fixedIterator.nextObject());
        
        /*
         * Test the iterator with a null array.
         */
        Object[] nullArray = null;
        fixedIterator = null;
        fixedIterator = new TestedFixedRangeIterator(nullArray);
        assertNotNull(fixedIterator);
        assertNull(fixedIterator.nextObject());
        assertNull(fixedIterator.nextObject());
        
        /*
         * Test the iterator with a null array.
         */
        Object[] emptyArray = new String[]{};
        fixedIterator = null;
        fixedIterator = new TestedFixedRangeIterator(emptyArray);
        assertNotNull(fixedIterator);
        assertNull(fixedIterator.nextObject());
        assertNull(fixedIterator.nextObject());
        
        /*
         * Test with a string array with three elements.
         */
        Object[] stringArray = new String[]{"Mann", "o", "Mann"};
        fixedIterator = null;
        fixedIterator = new TestedFixedRangeIterator(stringArray);
        assertNotNull(fixedIterator);
        first  = fixedIterator.nextObject();
        assertNotNull(first);
        assertInstanceof(first, String.class);
        assertEquals("Mann", (String)first);
        second = fixedIterator.nextObject();
        assertNotNull(second);
        assertInstanceof(second, String.class);
        assertEquals("o", (String)second);
        third  = fixedIterator.nextObject();
        assertNotNull(third);
        assertInstanceof(third, String.class);
        assertEquals("Mann", (String)third);
        assertNull(fixedIterator.nextObject());
        assertNull(fixedIterator.nextObject());
        
        /* No need to test the abstract methods */
    }
    
    /**
     * Inner class to allow testing of FixedRangeIterator.
     */
    static class TestedFixedRangeIterator extends FixedRangeIterator {

        /** 
         * Make super CTor acessible
         */
        public TestedFixedRangeIterator(Collection aCollection) {
            super(aCollection);
        }

        /** 
         * Make super CTor acessible
         */
        public TestedFixedRangeIterator(Object[] anObjectArray) {
            super(anObjectArray);
        }

        @Override
		public Object getIDFor(Object anObject) {
            return null;
        }

        @Override
		public String getUIStringFor(Object anObject) {
            return null;
        }
        
    }
    
    /** 
     * Returns a suite of tests.
     */
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestFixedRangeIterator.class));
    }

    /**
     * This main function is for direct testing.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite ());
    }    
    
}
