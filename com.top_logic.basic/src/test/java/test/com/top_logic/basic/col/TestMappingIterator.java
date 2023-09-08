/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.MappedIterable;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.MappingIterator;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.ParseDateMapping;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;

/**
 * Test class for {@linkplain MappingIterator} and {@link Mappings}.
 * 
 * @author    <a href=mailto:cdo@top-logic.com>cdo</a>
 */
public class TestMappingIterator extends TestCase {
    
    private static final String[] STRINGS_1_TO_5 
        = new String[] {"1", "2", "3", "4", "5"};
    
	private static final Mapping MAPPING = new Mapping() {
        /** {@inheritDoc} */
        @Override
		public Object map(Object someInput) {
            return "mapping-" + someInput;
        }
    };
    
    /* ---------------------------------------------------------------------- *
     * Overrides TestCase
     * ---------------------------------------------------------------------- */
    
    /* ---------------------------------------------------------------------- *
     * Test methods
     * ---------------------------------------------------------------------- */
    
    /**
     * Test case for {@link MappedIterable}.
     */
    public void testMappedIterable() {
    	assertEquals(
    		BasicTestCase.list("mapping-1", "mapping-2", "mapping-3", "mapping-4", "mapping-5"), 
			CollectionUtil.toListIterable(new MappedIterable<>(MAPPING, Arrays.asList(STRINGS_1_TO_5))));
    }
    
    public void testHasNext() {
    	List            theList   = Arrays.asList(STRINGS_1_TO_5);
        Iterator        theSource = theList.iterator();
        MappingIterator theTest   = new MappingIterator(MAPPING, theSource);

    	assertTrue(theSource.hasNext());
        assertEquals("mapping-1", theTest.next());
        assertEquals("mapping-2", theTest.next());
        assertEquals("mapping-3", theTest.next());
        assertTrue(theTest.hasNext());
        assertEquals("mapping-4", theTest.next());
        assertEquals("mapping-5", theTest.next());
        assertFalse(theTest.hasNext());
        assertFalse(theSource.hasNext());
    }
    
    public void testNext() {
        List            theList = Arrays.asList(STRINGS_1_TO_5);
        MappingIterator theTest = new MappingIterator(MAPPING,  theList.iterator());

    	for (int i = 1; theTest.hasNext(); i++) {
            assertEquals("mapping-" + i, theTest.next());
        }
    }
    
    public void testRemove() {
        List            theList = new ArrayList(Arrays.asList(STRINGS_1_TO_5));
        MappingIterator theTest = new MappingIterator(MAPPING,  theList.iterator());

    	theTest.next();
        theTest.remove();
        
        assertEquals(4,   theList.size());
        assertEquals("2", theList.get(0));
    }

    public void testMappings() {
        List theList = Arrays.asList(STRINGS_1_TO_5);

    	List mappedList = Mappings.map(MAPPING, theList);
    	
        for (int i = 1; i < mappedList.size(); i++) {
            assertEquals("mapping-" + i, mappedList.get(i - 1));
        }
    }
    
    public void testIdentity() {
    	Object test = new Object();
    	assertEquals(test, Mappings.<Object>identity().map(test));
    	assertEquals(null, Mappings.<Object>identity().map(null));
    }

    public void testDateMapping() {
        DateFormat      germanDF = CalendarUtil.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
		germanDF.setTimeZone(TimeZones.systemTimeZone());
        List            theList  = Arrays.asList(new String[] { "1.1.1", "22.12.2222", "15.04.2008", "IstSichKabbud"} );
        MappingIterator theTest  = new MappingIterator(new ParseDateMapping(germanDF),  theList.iterator());

        assertEquals(DateUtil.createDate(1   ,Calendar.JANUARY , 1) , theTest.next());
        assertEquals(DateUtil.createDate(2222,Calendar.DECEMBER,22) , theTest.next());
        assertEquals(DateUtil.createDate(2008,Calendar.APRIL   ,15) , theTest.next());
        try {
            theTest.next();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expecetd */ }
        assertFalse(theTest.hasNext());
    }

    /* ---------------------------------------------------------------------- *
     * Suite Methods
     * ---------------------------------------------------------------------- */

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite(TestMappingIterator.class);
        return suite;
    }

    /** main function for direct testing.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}