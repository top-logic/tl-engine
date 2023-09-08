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

import test.com.top_logic.TLTestSetup;

import com.top_logic.base.time.QuarterYearRange;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Test {@link QuarterYearRange}.
 * 
 * @author    <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class TestQuarterYearRange extends TestCase {

    /**
     * Create a new TestQuarterYearRange for given test name
     * 
     * @param aName function to call as test-case.
     */
    public TestQuarterYearRange(String aName) {
        super(aName);
    }

    /** Utility function to create a reasonable QuarterYearRange */
    protected QuarterYearRange createQuarterYearRange(Locale aLoc, int quaters) {
        QuarterYearRange theRange  = new QuarterYearRange();
		Calendar cal = CalendarUtil.createCalendar(aLoc);
        Date       today     = cal.getTime();
        cal.add(Calendar.MONTH, quaters * 3);
        Date       nextRange = cal.getTime();
        theRange.init(aLoc, today, nextRange);
        
        assertEquals(quaters, theRange.getNumRanges() - 1);
        return theRange;
    }
    
    /** 
     * Regression test for a special data that was incorrect.
     */
    public void test20090731() throws ParseException {
        
        int quaters = 3;
        
        QuarterYearRange theRange  = new QuarterYearRange();
		Calendar cal = CalendarUtil.createCalendar(Locale.US);
        // Did not work a this particular day ?
        DateUtil.createDate(cal, 2009,Calendar.JULY,31,0,0,0);
        Date       today     = cal.getTime();
        cal.add(Calendar.MONTH, quaters * 3);
        Date       nextRange = cal.getTime();
        theRange.init(Locale.US, today, nextRange);

        int numRanges = theRange.getNumRanges();
        assertEquals(quaters, numRanges - 1);
        
    }

    /** Test Iterating over more than one year, starting today.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testQuarter(Locale aLoc) throws ParseException {
        final int  QUARTERS    = 9; // 2 Years and a Quarter
        QuarterYearRange theRange = createQuarterYearRange(aLoc, QUARTERS);
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
            assertEquals(current, theRange.parseInternal(formString));
            
            formString = format.format(current);
            assertEquals(current, theRange.align(format.parse(formString)));

            assertNotNull(simple.format(current));
            assertNotNull(theRange.formatCurrent());
            count ++;
        }
        assertEquals(QUARTERS + 1, count); 
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
        assertEquals(QUARTERS + 1, count);   // 0-13 are 14 Month, well
    }

    /** Test some kinds of invalid operations.
     * 
     * This depends on the current time and may not be reproducible :-(
     */
    public void testInvalid() throws ParseException {
        /*
        try {
            createQuarterYearRange(Locale.SIMPLIFIED_CHINESE, -1);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) { / * expected * / }
        */
        QuarterYearRange theRange = createQuarterYearRange(Locale.US, 3);
        assertNull(theRange.parseInternal(null));
        assertNull(theRange.parseInternal(""));
        assertNull(theRange.parseInternal("BlahBlurb"));
        assertNull(theRange.parseInternal("Rather Long Blah blurb that will not work"));
        assertNull(theRange.parseInternal("yearQz"));
    }

    /** Test Iterating for an US locale
     */
    public void testQuarterUS() throws ParseException {
        testQuarter(Locale.US);
    }

    /** Test Iterating for a German locale.
     */
    public void testQuarterDE() throws ParseException {
        testQuarter(Locale.GERMANY);
    }

    /** Test Iterating over five years and measure the time.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testTime() throws ParseException {
        final int  QUARTERS = 20; // 5 
        QuarterYearRange theRange = createQuarterYearRange(Locale.GERMAN, QUARTERS);
        
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
        assertEquals(QUARTERS + 1, count); 
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
        assertEquals(QUARTERS + 1, count); 
        
    }
    
    /** The equalsRange method needs some special treatment.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testEqualsRange() throws ParseException {
        final int  QUARTERS = 0; 
        QuarterYearRange theRange = createQuarterYearRange(Locale.GERMAN, QUARTERS);
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
        return TLTestSetup.createTLTestSetup(TestQuarterYearRange.class);
    }

}
