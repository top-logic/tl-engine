/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.time.FixedDatesRange;
import com.top_logic.basic.Logger;

/**
 * Testcase for the {@link FixedDatesRange}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestFixedDatesRange extends BasicTestCase {

    private static final int THREE_DAYS_MILLIS = 1000 * 60 * 60 * 24 * 3;
    
    /** Helper function for creation of a Date array */
    public Date[] createArray(int size) {

        
        long millis = 1112095556892L;
        Date[] result = new Date[size];
        Random rand = new Random(size << 17 | size << 4 | size);
        for (int i=0; i < size; i++) {
            result[i] = new Date(millis);
            millis += rand.nextInt(THREE_DAYS_MILLIS);
        }
        return result;
    }
    
    /** init() will not work for a FixedDatesRange */ 
    public void testInit() {
		Locale loc = Locale.GERMAN;
       FixedDatesRange fdr = new FixedDatesRange(loc,new Date[1]);
       try {
           fdr.init(loc, new Date(), new Date());
           fail("Expected IllegalArgumentException");
       } catch (IllegalArgumentException expected) { /* expected */ }
    }

    /** 
     * Test Iterating over some given number of dates.
     */
    public void testSize(Locale aLoc, int aSize) throws ParseException {
        
        Date       array[]  = createArray(aSize);
        FixedDatesRange theRange = new FixedDatesRange(aLoc, array);
        assertEquals(aSize, theRange.getNumRanges());
        Date       start    = theRange.getStart();
        Date       end      = theRange.getEnd();
        DateFormat format   = theRange.getDateFormat();
        DateFormat simple   = theRange.getSimpleFormat();
        assertTrue(start.before(end));
        Date       current  = theRange.current();
        assertNull(current); // must call next, first
        int        count    = 0;
        
        int sizeM = aSize - 1;
        while (null != (current = theRange.next())
              && count < 1000) {    // prevent runaway loop
            assertEquals(array[count], current);
            if (count > 0)
                assertTrue(start.before(current));
            if (count < sizeM)
                assertTrue("At " + count , end  .after (current));
            assertNotNull(theRange.toString());
            String formString   = theRange.formatInternal(current);
            assertEquals(current.getTime(), theRange.parseInternal(formString).getTime());
            
            formString = format.format(current);
            assertEquals(current, theRange.align(format.parse(formString)));

            assertNotNull(simple.format(current));
            assertNotNull(theRange.formatCurrent());
            count ++;
        }
        assertEquals(aSize, count);
        count = 0;
        while (null != (current = theRange.previous())
               && count < 1000) {    // prevent runaway loop
            if (count < sizeM)
                assertTrue(start.before(current));
            if (count > 0)
                assertTrue(end  .after (current));
            assertNotNull(theRange.toString());
            String formString = theRange.formatInternal(current);
            assertEquals(current, theRange.parseInternal(formString));

            formString = format.format(current);
            assertEquals(current, theRange.align(format.parse(formString)));
            
            assertNotNull(simple.format(current));
            assertNotNull(theRange.formatCurrent());
            count++;
        }
        assertEquals(aSize, count);  
    }

    /** 
     * Test Iterating over 22 dates.
     */
    public void test22() throws ParseException {
        testSize(Locale.GERMAN, 22);
    }

    /** 
     * Test Iterating over 2 dates.
     */
    public void test2() throws ParseException {
        testSize(Locale.US, 2);
    }

    /** 
     * Test Iterating over 99 dates.
     */
    public void test99() throws ParseException {
        testSize(Locale.ENGLISH, 99);
    }
    
    /** 
     * the suite of Tests to execute
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestFixedDatesRange.class);
        // return new TestFixedDatesRange("testCrossOver");
    }

    /** Main function for direct execution. */
    public static void main(String[] args) {
        SHOW_TIME=true;
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

}
