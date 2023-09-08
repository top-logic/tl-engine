/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.base.time.HalfYearRange;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Test {@link com.top_logic.base.time.HalfYearRange}.
 * 
 * @author    <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class TestHalfYearRange extends TestCase {

    /**
     * Create a new TestHalfYearRange for given test name
     * 
     * @param aName function to call as test-case.
     */
    public TestHalfYearRange(String aName) {
        super(aName);
    }
    
    /** Utility function to create a reasonable HalfYearRange */
    protected HalfYearRange createHalfYearRange(Locale aLoc, int halfYears) {
        HalfYearRange theRange  = new HalfYearRange();
		Calendar cal = CalendarUtil.createCalendar(aLoc);
        // Did not work a this particular day 
        DateUtil.createDate(cal, 2009,7,31,14,30,0);
        Date       today     = cal.getTime();
        cal.add(Calendar.MONTH, halfYears * 6);
        Date       nextMonth = cal.getTime();
        theRange.init(aLoc, today, nextMonth);
        assertEquals(halfYears, theRange.getNumRanges() - 1);
        return theRange;
    }
    
    /** 
     * Regression test for a special data that was incorrect.
     */
    public void test20090731() throws ParseException {
        
        int halfYears = 3;
        
        HalfYearRange theRange  = new HalfYearRange();
		Calendar cal = CalendarUtil.createCalendar(Locale.US);
        // Did not work a this particular day 
        DateUtil.createDate(cal, 2009,Calendar.JULY,31,14,30,0);
        Date       today     = cal.getTime();
        cal.add(Calendar.MONTH, halfYears * 6);
        Date       nextMonth = cal.getTime();
        theRange.init(Locale.US, today, nextMonth);
        assertEquals(halfYears, theRange.getNumRanges() - 1);
    }

  
    /** Test Iterating over more than one year, starting today.
     * 
     * This depends on the current time and may not be reproducible :-(
     */
    public void testHalfYear(Locale aLoc) throws ParseException {
        final int  HALF    = 9; // 4 and a half Years
        HalfYearRange theRange = createHalfYearRange(aLoc, HALF);
        Date       start    = theRange.getStart();
        Date       end      = theRange.getEnd();
        DateFormat format   = theRange.getDateFormat();
        DateFormat simple   = theRange.getSimpleFormat();
        assertTrue(start.before(end));
        Date       current  = theRange.current();
        assertNull(current); // must call next, first
        int        count    = 0;
        
        while (null != (current = theRange.next())
              && count < 1000) {    // prevent runaway loop
            assertTrue(start.before(current));
            assertTrue(end  .after (current));
            assertNotNull(theRange.toString());
            String formString   = theRange.formatInternal(current);
            assertEquals(formString, current, theRange.parseInternal(formString));
            
            formString = format.format(current);
            assertEquals(current, theRange.align(format.parse(formString)));

            assertNotNull(simple.format(current));
            assertNotNull(theRange.formatCurrent());
            count ++;
        }
        assertEquals(HALF + 1, count); 
        count = 0;
        while (null != (current = theRange.previous())
               && count < 1000) {    // prevent runaway loop
            assertTrue(start.before(current));
            assertTrue(end.after(current));
            assertNotNull(theRange.toString());
            String formString = theRange.formatInternal(current);
            assertEquals(current, theRange.parseInternal(formString));

            formString = format.format(current);
            assertEquals(current, theRange.align(format.parse(formString)));
            
            assertNotNull(simple.format(current));
            assertNotNull(theRange.formatCurrent());
            count++;
        }
        assertEquals(HALF + 1, count); 
    }

    /** Test some kinds of invalid operations.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testInvalid() throws ParseException {
        try {
            createHalfYearRange(Locale.SIMPLIFIED_CHINESE, -1);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) { /* expected */ }
        
        HalfYearRange theRange = createHalfYearRange(Locale.US, 99);
        assertNull(theRange.parseInternal(null));
        assertNull(theRange.parseInternal(""));
        assertNull(theRange.parseInternal("BlahBlurb"));
        assertNull(theRange.parseInternal("Rather Long Blah blurb that will not work"));
        assertNull(theRange.parseInternal("yearHx"));
    }

    /** Test Iterating for an US locale
     */
    public void testtestHalfYearUS() throws ParseException {
        testHalfYear(Locale.US);
    }

    /** Test Iterating for a German locale.
     */
    public void testtestHalfYearDE() throws ParseException {
        testHalfYear(Locale.GERMANY);
    }

    /** Test Iterating over five years and measure the time.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testTime() throws ParseException {
        final int  HALF    = 9; // 4 and a half Years
        HalfYearRange theRange = createHalfYearRange(Locale.GERMAN, HALF);
        
        Date       start    = theRange.getStart();
        Date       end      = theRange.getEnd();
        DateFormat format   = theRange.getDateFormat();
        assertTrue(start.before(end));
        Date       current  = theRange.current();
        assertNull(current); // must call next, first
        int        count    = 0;
        
        while (null != (current = theRange.next())
              && count < 1000) {    // prevent runaway loop
            assertTrue(start.before(current));
            assertTrue(end  .after (current));
            assertNotNull(theRange.toString());
            String formString   = theRange.formatInternal(current);
            assertEquals(current, theRange.parseInternal(formString));
            
            formString = format.format(current);
            assertEquals(current, theRange.align(format.parse(formString)));
            count ++;
        }
        assertEquals(HALF + 1, count); 
        count = 0;
        while (null != (current = theRange.previous())
               && count < 1000) {    // prevent runaway loop
            assertTrue(start.before(current));
            assertTrue(end.after(current));
            assertNotNull(theRange.toString());
            String formString = theRange.formatInternal(current);
            assertEquals(current, theRange.parseInternal(formString));

            formString = format.format(current);
            assertEquals(current, theRange.align(format.parse(formString)));
            count++;
        }
        assertEquals(HALF + 1, count); 
        
    }
    
    /** The equalsRange method needs some special treatment.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testEqualsRange() throws ParseException {
        final int  HALF    = 0;
        HalfYearRange theRange = createHalfYearRange(Locale.GERMAN, HALF);
        Date       start    = theRange.getStart();
        Date       end      = theRange.getEnd();

        assertTrue( "Start " + start + " Not before End " + end, start.before(end));

        Date       current  = theRange.next();

        assertTrue(theRange.equalsRange(current , start));
        assertTrue(theRange.equalsRange(current , end));
        assertTrue(theRange.equalsRange(start   , end));
        
        assertEquals(theRange.formatInternal(current), 
                     theRange.formatInternal(start));
        assertEquals(theRange.formatInternal(current), 
                     theRange.formatInternal(end));
        assertEquals(theRange.formatInternal(start), 
                     theRange.formatInternal(end));
        
    }

    /** 
     * the suite of Tests to execute
     */
    public static Test suite() {
        return new TestSuite(TestHalfYearRange.class);
    }

}
