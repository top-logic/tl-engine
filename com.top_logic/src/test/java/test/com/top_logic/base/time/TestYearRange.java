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

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.time.YearRange;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Testcase for {@link com.top_logic.base.time.YearRange}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestYearRange extends BasicTestCase {

    /** Utility function to create a reasonable YearRange */
    protected YearRange createYearRange(Locale aLoc, int years) {
        YearRange theRange  = new YearRange();
		Calendar cal = CalendarUtil.createCalendar(aLoc);
        Date       today     = cal.getTime();
        cal.add(Calendar.YEAR, years);
        Date       nextYear = cal.getTime();
        theRange.init(aLoc, today, nextYear);
        assertEquals(years, theRange.getNumRanges() - 1);
        return theRange;
    }
    
    /** Test Iterating over more than one year, satring today.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testYear(Locale aLoc) throws ParseException {
        final int  YEAR    = 22;
        YearRange theRange = createYearRange(aLoc, YEAR);
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
        assertEquals(YEAR + 1, count); // 0-13 are 14 Month, well
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
        assertEquals(YEAR + 1, count);   // 0-13 are 14 Month, well
    }

    /** Test some kinds of invalid operations.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testInvalid() throws ParseException {
        try {
            createYearRange(Locale.KOREAN, -1);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) { /* expected */ }
        
        YearRange theRange = createYearRange(Locale.US, 99);
        assertNull(theRange.parseInternal(null));
        assertNull(theRange.parseInternal(""));
        assertNull(theRange.parseInternal("BlahBlurb"));
    }

    /** Test Iterating for an US locale
     */
    public void testYearUS() throws ParseException {
        testYear(Locale.US);
    }

    /** Test Iterating for a German locale.
     */
    public void testYearDE() throws ParseException {
        testYear(Locale.GERMANY);
    }

    /** Test Iterating over three years and measure the time.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testTime() throws ParseException {
        final int  YEAR    = 44;
        YearRange theRange = createYearRange(Locale.GERMAN, YEAR);
        
        startTime();
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
        assertEquals(YEAR + 1, count); // 0-7 are eight Days, well
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
        assertEquals(YEAR + 1, count);  // 0-7 are eight Days, well
        logTime("testTime " + YEAR);
        
    }
    
    /** The equalsRange method needs some special treatment.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testEqualsRange() throws ParseException {
        final int  DAYS     = 0;
        YearRange theRange = createYearRange(Locale.JAPANESE, DAYS);
        Date       start    = theRange.getStart();
        Date       end      = theRange.getEnd();
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
        return TLTestSetup.createTLTestSetup(TestYearRange.class);
        // return new TestTimeRange("testCrossOver");
    }

    /** Main function for direct execution. */
    public static void main(String[] args) {
        SHOW_TIME=true;
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

}
