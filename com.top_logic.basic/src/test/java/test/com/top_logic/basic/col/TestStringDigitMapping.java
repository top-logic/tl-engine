/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.MappingSorter;
import com.top_logic.basic.col.StringDigitMapping;
import com.top_logic.basic.col.TupleFactory.Tuple;

/**
 * Test the {@link StringDigitMapping}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestStringDigitMapping extends BasicTestCase {

    /** 
     * Create a new TestStringDigitMapping for given test name.
     */
    public TestStringDigitMapping(String name) {
        super(name);
    }

    /**
     * Test mapping.
     */
    public void testMap() {
        StringDigitMapping sdm = StringDigitMapping.INSTANCE;
        
        // Test mapping only once as anything else is based on slice
        Tuple key = (Tuple) sdm.map(null);
        assertEquals(0, key.size());
    }

    /**
     * Test method for {@link com.top_logic.basic.col.StringDigitMapping#slice(java.lang.CharSequence)}.
     */
    public void testSlice() {
        StringDigitMapping sdm = StringDigitMapping.INSTANCE;
        Object [] slice;
        
        assertEquals(0, sdm.slice(null).length);
        assertEquals(0, sdm.slice("").length);
        
        slice = sdm.slice("A");
        assertEquals(1, slice.length);
        assertEquals("A",slice[0]);
        
        slice = sdm.slice("0");
        assertEquals(2, slice.length);
        assertEquals("",slice[0]);
        assertEquals(Integer.valueOf(0),slice[1]);

        slice = sdm.slice("X9Y8Z7");
        assertEquals(6, slice.length);
        assertEquals("X",slice[0]);
        assertEquals(Integer.valueOf(9),slice[1]);
        assertEquals("Y",slice[2]);
        assertEquals(Integer.valueOf(8),slice[3]);
        assertEquals("Z",slice[4]);
        assertEquals(Integer.valueOf(7),slice[5]);
        
        slice = sdm.slice("99OOO888PPPP4444NN");
        assertEquals(7, slice.length);
        assertEquals("",slice[0]);
        assertEquals(Integer.valueOf(99),slice[1]);
        assertEquals("OOO",slice[2]);
        assertEquals(Integer.valueOf(888),slice[3]);
        assertEquals("PPPP",slice[4]);
        assertEquals(Integer.valueOf(4444),slice[5]);
        assertEquals("NN",slice[6]);
    }

    /**
     * Test method for {@link com.top_logic.basic.col.StringDigitMapping#compare(java.lang.Object, java.lang.Object)}.
     */
    public void testCompare() {
        StringDigitMapping sdm = StringDigitMapping.INSTANCE;
        
        assertEquals(0, sdm.compare(""  , null));
        assertEquals(0, sdm.compare(null, ""));
        
        assertTrue(sdm.compare("1a2b3", "a2b3") < 0);
        assertTrue(sdm.getKey("1a2b3").compareTo(sdm.getKey("a2b3")) < 0);
        
        assertTrue(sdm.compare("a" , "b") < 0);
        assertTrue(sdm.compare("b" , "a") > 0);

        assertTrue(sdm.compare("1" , "2") < 0);
        assertTrue(sdm.compare("9" , "8") > 0);

        assertEquals(0, sdm.compare("A01" , "A1"));
        assertEquals(0, sdm.compare("A1"  , "A001"));
        
        assertEquals(0, sdm.compare("R2D2"    , "R02D02"));
        assertEquals(0, sdm.compare("R022D022", "R22D22"));
        
        assertTrue(sdm.compare("111"  , "111U") < 0);
        assertTrue(sdm.compare("111U" , "111")  > 0);
        
        assertTrue(sdm.compare("AAA"  , "AAA7") < 0);
        assertTrue(sdm.compare("AAA7" , "AAA")  > 0);

        String[] list = new String[] {
                   "1-02-03C", 
                   "2-1-1", 
                   "2-1-01U", 
                   "01-2-3B", 
                   "3-2-1Z", 
                   "3-002-001Y", 
                   "03-02-01X", 
                   "2-01-1V", 
                   "1-2-3A" 
        };
        
        Arrays.sort(list, sdm);
        
        assertEquals("1-2-3A"    , list[0]);
        assertEquals("01-2-3B"   , list[1]);
        assertEquals("1-02-03C"  , list[2]);
        assertEquals("2-1-1"     , list[3]);
        assertEquals("2-1-01U"   , list[4]);
        assertEquals("2-01-1V"   , list[5]);
        assertEquals("03-02-01X" , list[6]);
        assertEquals("3-002-001Y", list[7]);
        assertEquals("3-2-1Z"    , list[8]);
    }
        
    /**
     * Compare MappingSorter with usage as {@link Comparator}.
     */
    public void testWithMappingSorter() {
        StringDigitMapping sdm = StringDigitMapping.INSTANCE;
        
        String[] array = new String[] {
            "1-02-03C", 
            "2-1-1", 
            "2-1-01U", 
            "01-2-3B", 
            null,
            "3-2-1Z", 
            "3-002-001Y", 
            "03-02-01X", 
            "2-01-1V", 
            "1-2-3A", 
        };
         
        // With this sizing both approaches work quite similar
        final int EXTRA   = 100 - array.length;
        final int SHUFFLE = 100;

        Random rand = new Random(815); // 0815 does not work, guess why
        List list = new ArrayList(Arrays.asList(array));
        for (int i=0; i < EXTRA; i++) {
        	StringBuffer testEntry = new StringBuffer();
        	for (int n = 0; n < 4; n++) {
        		if (n > 0) {
        			testEntry.append('.');
        		}
        		testEntry.append(Integer.toString(rand.nextInt(10)));
        	}
        	
            list.add(testEntry.toString());
        }
    
        List l1 = new ArrayList(list);
        List l2 = new ArrayList(list);
        
        
        startTime();
        Collections.sort(l1, sdm);
        rand = new Random(815);
        for (int j=0; j < SHUFFLE; j++) {
            Collections.shuffle(l1);
            Collections.sort(l1, sdm);
        }
        logTime("Comparator    " + SHUFFLE + "*" + list.size());
		MappingSorter.sortByMappingInline(l2, sdm, ComparableComparator.INSTANCE);
        rand = new Random(815);
        for (int j=0; j < SHUFFLE; j++) {
            try {
				MappingSorter.sortByMappingInline(l2, sdm, ComparableComparator.INSTANCE);
			} catch (Exception e) {
				Logger.error("Exception in MappingSorter.sortByMappingInline(..).", e, this);
			}
            Collections.sort(l2, sdm);
        }
        logTime("MappingSorter " + SHUFFLE + "*" + list.size());
        
        assertEquals(l1, l2);
    }
    
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestStringDigitMapping.class));
    }
    
    /**
     * This main function is for direct testing.
     */
    public static void main(String[] args) {
        SHOW_TIME = true;
        TestRunner.run (new TestStringDigitMapping("testWithMappingSorter"));
    }    
    

}

