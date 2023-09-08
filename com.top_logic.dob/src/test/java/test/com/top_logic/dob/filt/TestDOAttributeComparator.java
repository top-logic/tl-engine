/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.filt;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.filt.DOAttributeComparator;
import com.top_logic.dob.simple.ExampleDataObject;

/**
 * This is a test for the {@link com.top_logic.dob.filt.DOAttributeComparator}
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestDOAttributeComparator extends TestCase {


    /**
     * Default Constructor.
     *
     * @param name  name of testFunction to perform. 
     */
    public TestDOAttributeComparator (String name) {
        super (name);
    }
    
    /** 
     * Checks that the Objects are equal according to given Comparator.
     */
	public void checkEquality(Comparator<DataObject> c, DataObject o1, DataObject o2) {
    
        assertEquals(0, c.compare(o1,o2));
        assertEquals(0, c.compare(o2,o1));
    }
    
    /** 
     * Checks that the Objects less according to given comparator.
     */
	public void checkLess(Comparator<DataObject> c, DataObject o1, DataObject o2, DataObject o3) {
    
        assertTrue(c.compare(o1,o2) < 0);
        assertTrue(c.compare(o2,o3) < 0);
        assertTrue(c.compare(o1,o3) < 0);

        assertTrue(c.compare(o2,o1) > 0);
        assertTrue(c.compare(o3,o2) > 0);
        assertTrue(c.compare(o3,o1) > 0);
    }
    
    /** 
     * Checks that the Objects less according to given coperator.
     */
	public void checkLess(Comparator<DataObject> c, DataObject o1, DataObject o2) {
    
        assertTrue(c.compare(o1,o2) < 0);
        assertTrue(c.compare(o2,o1) > 0);
    }
    
    /** 
     * Simple tests for Equality
     */
    public void testEquality() {
    
        HashMap ex = new HashMap();
        ex.put("aaa", "aaa");
		ex.put("bbb", Integer.valueOf(17));
        ex.put("zzz", new Date(9999999));
        ex.put("uuu", Boolean.TRUE);
    
        ExampleDataObject edo1 = new ExampleDataObject(ex);
        
        ex = new HashMap();
        ex.put("aaa", "aaa");
		ex.put("bbb", Integer.valueOf(17));
        ex.put("zzz", new Date(9999999));
        ex.put("uuu", Boolean.TRUE);
   
        ExampleDataObject edo2 = new ExampleDataObject(ex);
        
        DOAttributeComparator comp;

        // worst case, no Attributes at all ;-)
        
        comp = new DOAttributeComparator(new String[0]);
        
        // This will not work, but thats ok ...    
        // checkEquality(comp, edo1, edo2, edo3); 

        comp = new DOAttributeComparator("aaa");
        checkEquality(comp, edo1, edo2); 
        comp = new DOAttributeComparator("bbb");
        checkEquality(comp, edo1, edo2); 
        comp = new DOAttributeComparator("zzz");
        checkEquality(comp, edo1, edo2); 
		comp = new DOAttributeComparator("zzz", DOAttributeComparator.DESCENDING);
        checkEquality(comp, edo1, edo2); 
        comp = new DOAttributeComparator("uuu");
        checkEquality(comp, edo1, edo2); 

        comp = new DOAttributeComparator(
            new String[]  { "aaa", "bbb" } );
        checkEquality(comp, edo1, edo2); 
        comp = new DOAttributeComparator(
            new String[]  { "aaa", "zzz" } );
        checkEquality(comp, edo1, edo2); 
        comp = new DOAttributeComparator(
            new String[]  { "bbb", "zzz" } );
        checkEquality(comp, edo1, edo2); 

        comp = new DOAttributeComparator(
            new String[]  { "aaa", "bbb", "zzz" } );
        checkEquality(comp, edo1, edo2); 
        comp = new DOAttributeComparator(
            new String[]   { "aaa", "bbb", "zzz" },
			new boolean[] { false, true, true });
        checkEquality(comp, edo1, edo2); 
        
        comp = new DOAttributeComparator(
            new String[]  { "aaa", "bbb", "zzz", "uuu" } );
        checkEquality(comp, edo1, edo2); 
        comp = new DOAttributeComparator(
            new String[]   { "aaa", "uuu", "bbb", "zzz" },
			new boolean[] { false, true, true });
        checkEquality(comp, edo1, edo2); 
    }
    

    /** 
     * Simple tests for Less repective Greater values.
     */
    public void testCompare() {
    
        HashMap ex = new HashMap();
        ex.put("aaa", "ccc");
		ex.put("bbb", Integer.valueOf(17));
        ex.put("zzz", new Date(9999999));
        ex.put("uuu", Boolean.FALSE);
    
        ExampleDataObject edo1 = new ExampleDataObject(ex);
        
        ex = new HashMap();
        ex.put("aaa", "bbb");
		ex.put("bbb", Integer.valueOf(18));
        ex.put("zzz", new Date(9999999));
        ex.put("uuu", Boolean.TRUE);
    
        ExampleDataObject edo2 = new ExampleDataObject(ex);
        
        ex = new HashMap();
        ex.put("aaa", "aaa");
		ex.put("bbb", Integer.valueOf(19));
        ex.put("zzz", new Date(9999999));
        ex.put("uuu", Boolean.TRUE);
    
        ExampleDataObject edo3 = new ExampleDataObject(ex);

        DOAttributeComparator comp;

        // worst case, no Attributes at all ;-)
        
        comp = new DOAttributeComparator(new String[0]);
        
        // This will not work, but thats ok ...    
        // checkLess(comp, edo1, edo2, edo3); 

        comp = new DOAttributeComparator("aaa");
        checkLess(comp, edo3, edo2, edo1); 
        comp = new DOAttributeComparator("bbb");
        checkLess(comp, edo1, edo2, edo3); 
        comp = new DOAttributeComparator("uuu");
        checkLess(comp, edo1, edo2); 

        comp = new DOAttributeComparator(
            new String[]  { "aaa", "bbb" } );
        checkLess(comp, edo3, edo2, edo1); 
        comp = new DOAttributeComparator(
            new String[]  { "bbb", "aaa" } );
        checkLess(comp, edo1, edo2, edo3); 
        comp = new DOAttributeComparator(
            new String[]  { "zzz", "aaa" } );
        checkLess(comp, edo3, edo2, edo1); 
        comp = new DOAttributeComparator(
            new String[]  { "zzz", "bbb" } );
        checkLess(comp, edo1, edo2, edo3); 
        comp = new DOAttributeComparator(
            new String[]  { "zzz", "bbb" }  , 
			new boolean[] { false, false, });
        checkLess(comp, edo3, edo2, edo1); 
        comp = new DOAttributeComparator("uuu", "bbb");
        checkLess(comp, edo2, edo3); 

        comp = new DOAttributeComparator(
            new String[]  { "zzz", "bbb", "aaa" } );
        checkLess(comp, edo1, edo2, edo3); 
        comp = new DOAttributeComparator(
            new String[]  { "uuu", "zzz", "bbb", "aaa" } );
        checkLess(comp, edo1, edo2, edo3); 
    }
    
    /**
     * Test behavior on null.
     */
    public void testNull() throws Exception {
    
        // An non-null data object
        HashMap nonNullMap = new HashMap();
        nonNullMap.put("aaa", "ccc");
		nonNullMap.put("bbb", Integer.valueOf(17));
        nonNullMap.put("zzz", new Date(9999999));
        nonNullMap.put("uuu", Boolean.FALSE);
        ExampleDataObject nonNull = new ExampleDataObject(nonNullMap);
    
        // An string-null data object
        HashMap stringNullMap = new HashMap();
        stringNullMap.put("aaa", null);
		stringNullMap.put("bbb", Integer.valueOf(17));
        stringNullMap.put("zzz", new Date(9999999));
        stringNullMap.put("uuu", Boolean.FALSE);
        ExampleDataObject stringNull = new ExampleDataObject(stringNullMap);
    
        // An integer-null data object
        HashMap integerNullMap = new HashMap();
        integerNullMap.put("aaa", "ccc");
        integerNullMap.put("bbb", null);
        integerNullMap.put("zzz", new Date(9999999));
        integerNullMap.put("uuu", Boolean.FALSE);
        ExampleDataObject integerNull = new ExampleDataObject(integerNullMap);
    
        // An date-null data object
        HashMap dateNullMap = new HashMap();
        dateNullMap.put("aaa", "ccc");
		dateNullMap.put("bbb", Integer.valueOf(17));
        dateNullMap.put("zzz", null);
        dateNullMap.put("uuu", Boolean.FALSE);
        ExampleDataObject dateNull = new ExampleDataObject(dateNullMap);
    
        // An boolean-null data object
        HashMap booleanNullMap = new HashMap();
        booleanNullMap.put("aaa", "ccc");
		booleanNullMap.put("bbb", Integer.valueOf(17));
        booleanNullMap.put("zzz", new Date(9999999));
        booleanNullMap.put("uuu", null);
        ExampleDataObject booleanNull = new ExampleDataObject(booleanNullMap);
    
        // An all-null data object
        HashMap allNullMap = new HashMap();
        allNullMap.put("aaa", null);
        allNullMap.put("bbb", null);
        allNullMap.put("zzz", null);
        allNullMap.put("uuu", null);
        ExampleDataObject allNull = new ExampleDataObject(allNullMap);
        
        // null is greater than anything but null
        DOAttributeComparator comp;

        comp = new DOAttributeComparator("aaa");
        checkLess(comp, nonNull, stringNull); 
        checkEquality(comp, null    , null); 
		comp = new DOAttributeComparator("bbb", DOAttributeComparator.ASCENDING);
        checkLess(comp, nonNull, integerNull); 
        checkEquality(comp, integerNull, allNull); 
		comp = new DOAttributeComparator("zzz", DOAttributeComparator.ASCENDING);
        checkLess(comp, nonNull, dateNull); 
        checkEquality(comp, dateNull, allNull); 
		comp = new DOAttributeComparator("uuu", DOAttributeComparator.ASCENDING);
        checkLess(comp, nonNull, booleanNull); 
        checkEquality(comp, null    , null); 
        checkEquality(comp, booleanNull, allNull); 
        
        
    }
    

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestDOAttributeComparator.class);
        // TestSuite suite = new TestSuite ();
        // suite.addTest(new TestDOAttributeComparator("nameOfTest"));
        return suite;
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
