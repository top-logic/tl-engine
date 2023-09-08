/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.filt;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.filt.DOAttributeFilter;
import com.top_logic.dob.simple.ExampleDataObject;

/**
 * This is a test for the {@link com.top_logic.dob.filt.DOAttributeFilter}
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestDOAttributeFilter extends TestCase {

	/**
	 * {@link ExampleDataObject} that denies access to all attributes not
	 * currently in its data map.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
    private final class ExactExampleDataObject extends ExampleDataObject {
		ExactExampleDataObject(Map someMap) {
			super(someMap);
		}

		@Override
		public Object getAttributeValue(String attrName) throws NoSuchAttributeException {
			if (this.map.containsKey(attrName)) {
				return this.map.get(attrName);
			} else {
				throw new NoSuchAttributeException(attrName);
			}
		}
	}

	/**
     * Default Constructor.
     *
     * @param name  name of testFunction to perform. 
     */
    public TestDOAttributeFilter (String name) {
        super (name);
    }
        
    /** 
     * Test createEqualsFilter.
     */
    public void testEquals() {
    
		DOAttributeFilter doaf1 = DOAttributeFilter.createEqualsFilter("bbb", Integer.valueOf(17));
        DOAttributeFilter doaf2 = DOAttributeFilter.createEqualsFilter("nnnn"    , null);
        DOAttributeFilter doaf3 = DOAttributeFilter.createEqualsFilter("notThere", null);
        
        HashMap ex = new HashMap();
		ex.put("bbb", Integer.valueOf(17));
        ex.put("nnnn"   , null);
    
        ExampleDataObject edo = new ExactExampleDataObject(ex);
        
        assertTrue (doaf1.accept(edo));
        assertTrue (doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));

        ex = new HashMap();
		ex.put("bbb", Integer.valueOf(77));
        ex.put("null", "No Null!");
    
        edo = new ExactExampleDataObject(ex);

        assertFalse(doaf1.accept(edo));
        assertFalse(doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));
    }

    /** 
     * Test createLEFilter.
     */
    public void testLE() {
    
		DOAttributeFilter doaf1 = DOAttributeFilter.createLEFilter("bbb", Integer.valueOf(17));
        DOAttributeFilter doaf2 = DOAttributeFilter.createLEFilter("string"  , "hhh");
        DOAttributeFilter doaf3 = DOAttributeFilter.createLEFilter("notThere", null);
        
        // check equals
        HashMap ex = new HashMap();
		ex.put("bbb", Integer.valueOf(17));
        ex.put("string", "hh");
    
        ExampleDataObject edo = new ExampleDataObject(ex);
        
        assertTrue (doaf1.accept(edo));
        assertTrue (doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));

        // check greater
        ex = new HashMap();
		ex.put("bbb", Integer.valueOf(77));
        ex.put("string", "zzz");
    
        edo = new ExampleDataObject(ex);

        assertFalse(doaf1.accept(edo));
        assertFalse(doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));

        // check less
        ex = new HashMap();
		ex.put("bbb", Integer.valueOf(-3));
        ex.put("string", "aaa");
    
        edo = new ExampleDataObject(ex);

        assertTrue (doaf1.accept(edo));
        assertTrue (doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));
    }

    /** 
     * Test createLEFilter.
     */
    public void testLT() {
    
		DOAttributeFilter doaf1 = DOAttributeFilter.createLTFilter("bbb", Integer.valueOf(17));
        DOAttributeFilter doaf2 = DOAttributeFilter.createLTFilter("string"  , "hhh");
        DOAttributeFilter doaf3 = DOAttributeFilter.createLTFilter("notThere", null);
        
        // check equals
        HashMap ex = new HashMap();
		ex.put("bbb", Integer.valueOf(17));
        ex.put("string", "hhh");
    
        ExampleDataObject edo = new ExampleDataObject(ex);
        
        assertFalse(doaf1.accept(edo));
        assertFalse(doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));

        // check greater
        ex = new HashMap();
		ex.put("bbb", Integer.valueOf(77));
        ex.put("string", "zzz");
    
        edo = new ExampleDataObject(ex);

        assertFalse(doaf1.accept(edo));
        assertFalse(doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));

        // check less
        ex = new HashMap();
		ex.put("bbb", Integer.valueOf(-3));
        ex.put("string", "aaa");
    
        edo = new ExampleDataObject(ex);

        assertTrue (doaf1.accept(edo));
        assertTrue (doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));
    }

    /** 
     * Test createContainsFilter.
     */
    public void testContains() {
    
        DOAttributeFilter doaf1 = DOAttributeFilter.createContainsFilter("stringA" , "b");
        DOAttributeFilter doaf2 = DOAttributeFilter.createContainsFilter("stringB" , "X");
        DOAttributeFilter doaf3 = DOAttributeFilter.createContainsFilter("notThere", "");
        
        try {
            DOAttributeFilter.createContainsFilter("notThere", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) { /* expected */ }

        
        // check equals
        HashMap ex = new HashMap();
        ex.put("stringA" , "AbC");
        ex.put("stringB" , "xYz");
    
        ExampleDataObject edo = new ExampleDataObject(ex);
        
        assertTrue (doaf1.accept(edo));
        assertFalse(doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));
    }

    /** 
     * Test createGTFilter.
     */
    public void testGT() {
    
		DOAttributeFilter doaf1 = DOAttributeFilter.createGTFilter("bbb", Integer.valueOf(17));
        DOAttributeFilter doaf2 = DOAttributeFilter.createGTFilter("string"  , "hhh");
        DOAttributeFilter doaf3 = DOAttributeFilter.createGTFilter("notThere", null);
        
        // check equals
        HashMap ex = new HashMap();
		ex.put("bbb", Integer.valueOf(17));
        ex.put("string", "hh");
    
        ExampleDataObject edo = new ExampleDataObject(ex);
        
        assertFalse (doaf1.accept(edo));
        assertFalse (doaf2.accept(edo));
        assertFalse (doaf3.accept(edo));

        // check greater
        ex = new HashMap();
		ex.put("bbb", Integer.valueOf(7));
        ex.put("string", "bbb");
    
        edo = new ExampleDataObject(ex);

        assertFalse(doaf1.accept(edo));
        assertFalse(doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));

        // check less
        ex = new HashMap();
		ex.put("bbb", Integer.valueOf(33));
        ex.put("string", "zzz");
    
        edo = new ExampleDataObject(ex);

        assertTrue (doaf1.accept(edo));
        assertTrue (doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));
    }

    /** 
     * Test createGEFilter.
     */
    public void testGE() {
    
		DOAttributeFilter doaf1 = DOAttributeFilter.createGEFilter("bbb", Integer.valueOf(17));
        DOAttributeFilter doaf2 = DOAttributeFilter.createGEFilter("string"  , "hhh");
        DOAttributeFilter doaf3 = DOAttributeFilter.createGEFilter("notThere", null);
        
        // check equals
        HashMap ex = new HashMap();
		ex.put("bbb", Integer.valueOf(17));
        ex.put("string", "hhhh");
    
        ExampleDataObject edo = new ExampleDataObject(ex);
        
        assertTrue (doaf1.accept(edo));
        assertTrue (doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));

        // check greater
        ex = new HashMap();
		ex.put("bbb", Integer.valueOf(-7));
        ex.put("string", "aaa");
    
        edo = new ExampleDataObject(ex);

        assertFalse(doaf1.accept(edo));
        assertFalse(doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));

        // check less
        ex = new HashMap();
		ex.put("bbb", Integer.valueOf(44));
        ex.put("string", "yyy");
    
        edo = new ExampleDataObject(ex);

        assertTrue (doaf1.accept(edo));
        assertTrue (doaf2.accept(edo));
        assertFalse(doaf3.accept(edo));
    }

    /** 
     * Test createGEFilter.
     */
    public void testNull() {
    
        DOAttributeFilter doaf1 = DOAttributeFilter.createNullFilter("bbb");
        DOAttributeFilter doaf2 = DOAttributeFilter.createNullFilter("string");
        DOAttributeFilter doaf3 = DOAttributeFilter.createNullFilter("notThere");
        
        // check equals
        HashMap ex = new HashMap();
        ex.put("bbb"   , null);
        ex.put("string", "hhhh");
    
        ExampleDataObject edo = new ExactExampleDataObject(ex);
        
        assertTrue  (doaf1.accept(edo));
        assertFalse (doaf2.accept(edo));
        assertFalse (doaf3.accept(edo));

        ex.clear();
		ex.put("bbb", Integer.valueOf(4711));
        ex.put("string", null);
    
        edo = new ExactExampleDataObject(ex);

        assertFalse (doaf1.accept(edo));
        assertTrue  (doaf2.accept(edo));
        assertFalse (doaf3.accept(edo));
    }

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        return new TestSuite (TestDOAttributeFilter.class);
        // return new TestDOAttributeFilter("testContains");
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
