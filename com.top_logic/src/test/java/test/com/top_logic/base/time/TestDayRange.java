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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.time.DayRange;
import com.top_logic.base.time.TimeRangeIterator;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Testcase for {@link com.top_logic.base.time.DayRange}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestDayRange extends BasicTestCase {

    /** Utily function to create a reasonable DayRange */
    protected DayRange createDayRange(Locale aLoc, int days) {
        DayRange theRange = new DayRange();
		Calendar cal = CalendarUtil.createCalendar(aLoc);
        Date     today    = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR,days);
        Date     nextDay  = cal.getTime();
        theRange.init(aLoc, today, nextDay);
        
        assertEquals(days, theRange.getNumRanges() - 1);
        return theRange;
    }
    
    /** Test Iterating over the current week.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testWeek(Locale aLoc) throws ParseException {
        final int  DAYS     = 7;
        DayRange   theRange = createDayRange(aLoc, DAYS);
        Date       start    = theRange.getStart();
        Date       end      = theRange.getEnd();
        DateFormat format   = theRange.getDateFormat();
        DateFormat simple   = theRange.getSimpleFormat();
        assertTrue(start.before(end));
        Date       current  = theRange.current();
        assertNull(current); // must call next, first
        int        count    = 0;
        
        while (null != (current = theRange.next())
              && count < 10000) {    // prevent runaway loop
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
        assertEquals(DAYS + 1, count); // 0-7 are eight Days, well
        count = 0;
        while (null != (current = theRange.previous())
               && count < 10000) {    // prevent runaway loop
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
        assertEquals(DAYS + 1, count);  // 0-7 are eight Days, well
    }

    /** Test some kinds of invalid operations.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testInvalid() throws ParseException {
        try {
            createDayRange(Locale.SIMPLIFIED_CHINESE, -1);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) { /* expected */ }
        
        DayRange   theRange = createDayRange(Locale.US, 99);
        assertNull(theRange.parseInternal(null));
        assertNull(theRange.parseInternal(""));
        assertNull(theRange.parseInternal("BlahBlurb"));
    }

    /** Test Iterating over the current week for an US locale
     */
    public void testWeekUS() throws ParseException {
        testWeek(Locale.US);
    }

    /** Test Iterating over the current week for a German locale.
     */
    public void testWeekDE() throws ParseException {
        testWeek(Locale.GERMANY);
    }

    /** Test Iterating over ten years and measure the time.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testTime() throws ParseException {
        final int  DAYS     = 365 * 10;
        DayRange   theRange = createDayRange(Locale.GERMAN, DAYS);
        
        startTime();
        Date       start    = theRange.getStart();
        Date       end      = theRange.getEnd();
        DateFormat format   = theRange.getDateFormat();
        assertTrue(start.before(end));
        Date       current  = theRange.current();
        assertNull(current); // must call next, first
        int        count    = 0;
        
        while (null != (current = theRange.next())
              && count < 10000) {    // prevent runaway loop
            assertTrue(start.before(current));
            assertTrue(end  .after (current));
            assertNotNull(theRange.toString());
            String formString   = theRange.formatInternal(current);
            assertEquals(current, theRange.parseInternal(formString));
            
            formString = format.format(current);
            assertEquals(current, theRange.align(format.parse(formString)));
            count ++;
        }
        assertEquals(DAYS + 1, count); // 0-7 are eight Days, well
        count = 0;
        while (null != (current = theRange.previous())
               && count < 10000) {    // prevent runaway loop
            assertTrue(start.before(current));
            assertTrue(end.after(current));
            assertNotNull(theRange.toString());
            String formString = theRange.formatInternal(current);
            assertEquals(current, theRange.parseInternal(formString));

            formString = format.format(current);
            assertEquals(current, theRange.align(format.parse(formString)));
            count++;
        }
        assertEquals(DAYS + 1, count);  // 0-7 are eight Days, well
        logTime("testTime " + DAYS);
        
    }
    
    /** The equalsRange method needs some special treatment.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testEqualsRange() throws ParseException {
        final int  DAYS     = 0;
        DayRange   theRange = createDayRange(Locale.GERMAN, DAYS);
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
    
    /** Test the complex possible overlappings. */
    public void testOverlap(Locale aLoc) {
        DayRange theMidRange = new DayRange();
		Calendar cal = CalendarUtil.createCalendar(aLoc);
        Date     today       = cal.getTime();           // 0
        cal.add(Calendar.DAY_OF_YEAR,3);                // 3
        Date     nextDay     = cal.getTime();
        theMidRange.init(aLoc, today, nextDay);

        theMidRange.next();
        assertTrue  (theMidRange.contains(today));
        assertFalse (theMidRange.contains(nextDay));
        
        assertTrue (theMidRange.containsRange(theMidRange));
        assertFalse(theMidRange.after        (theMidRange));
        assertFalse(theMidRange.before       (theMidRange));
        assertEquals(TimeRangeIterator.OVERLAP, theMidRange.overlaps(theMidRange));
        
        DayRange theRange    = new DayRange();
		cal = CalendarUtil.createCalendar(aLoc);
        cal.add(Calendar.DAY_OF_YEAR,-1);               // -1
        Date     yesterday   = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR,5);                // 5
        Date     overNextDay = cal.getTime();
        theRange.init(aLoc, yesterday, overNextDay);

        // [0..3] ? [ -1..5]
        assertFalse(theMidRange.containsRange(theRange));
        assertFalse(theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.OVERLAP, theMidRange.overlaps(theRange));
        
        theRange    = new DayRange();
		cal = CalendarUtil.createCalendar(aLoc);
        cal.add(Calendar.DAY_OF_YEAR,1);            // 1
        Date     tomorrow    = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR,1);            // 2
        Date     afterTomorrow = cal.getTime();
        theRange.init(aLoc, tomorrow, afterTomorrow);

        // [0..3] ? [1..2]
        assertTrue (theMidRange.containsRange(theRange));
        assertFalse(theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.OVERLAP, theMidRange.overlaps(theRange));

        theRange    = new DayRange();
        theRange.init(aLoc, yesterday, afterTomorrow);

        // [0..3] ? [-1..2]
        assertFalse(theMidRange.containsRange(theRange));
        assertFalse(theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.BEFORE_PARTIAL, theMidRange.overlaps(theRange));

        theRange    = new DayRange();
        theRange.init(aLoc, afterTomorrow, overNextDay);

        // [0..3] ? [2..5]
        assertFalse(theMidRange.containsRange(theRange));
        assertFalse(theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.AFTER_PARTIAL, theMidRange.overlaps(theRange));

        theRange    = new DayRange();
        theRange.init(aLoc, yesterday, yesterday);

        // [0..3] ? [-1..-1]
        assertFalse(theMidRange.containsRange(theRange));
        assertTrue (theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.AFTER_COMPLETE, theMidRange.overlaps(theRange));

        theRange    = new DayRange();
        theRange.init(aLoc, overNextDay, overNextDay);

        // [0..3] ? [5..5]
        assertFalse(theMidRange.containsRange(theRange));
        assertFalse(theMidRange.after        (theRange));
        assertTrue (theMidRange.before       (theRange));
        assertEquals(DayRange.BEFORE_COMPLETE, theMidRange.overlaps(theRange));
}

    /** Test the complex possible overlappings, in German */
    public void testOverlapDE() {
        testOverlap(Locale.GERMAN);
    }

    /** Test the complex possible overlappings, in US */
    public void testOverlapUS() {
        testOverlap(Locale.US);
    }

    /**
     * Test the algorithm for retrieving work days in a range.
     *
     */
    public void testGetWorkDayCount() {
        // basically, we have following test cases here:
        // 1. a range greater than 7 days
        // 2. a range equal 7 days
        // 3. a range less than 7 days
        // 4. a range with exactly one day
        
        // set this locale for calendars and the day range
        final Locale locale = Locale.GERMAN;
        
        // let's use the usual weekends here, shall we.
        final Set<Integer> weekend = new HashSet<>();
        weekend.add(Calendar.SATURDAY);
        weekend.add(Calendar.SUNDAY);
        
        // use two different calendars here for better code readability
		final Calendar startCal = CalendarUtil.createCalendar(locale);
		final Calendar endCal = CalendarUtil.createCalendar(locale);
        
        // case 1: 08.12.2009 - 17.12.2009
        // expected: 8 workdays
        startCal.set(2009, Calendar.DECEMBER, 8);
        endCal.set(2009, Calendar.DECEMBER, 17);
        assertEquals(8, new DayRange(locale, startCal.getTime(), endCal.getTime()).getWorkdayCount(weekend));
        
        // case 2: 04.01.2010 - 10.01.2010
        // expected: 5 workdays
        startCal.set(2010, Calendar.JANUARY, 4);
        endCal.set(2010, Calendar.JANUARY, 10);
        assertEquals(5, new DayRange(locale, startCal.getTime(), endCal.getTime()).getWorkdayCount(weekend));
        
        // case 3: 04.02.2010 - 10.02.2010
        // expected: 5
        startCal.set(2010, Calendar.FEBRUARY, 4);
        endCal.set(2010, Calendar.FEBRUARY, 10);
        assertEquals(5, new DayRange(locale, startCal.getTime(), endCal.getTime()).getWorkdayCount(weekend));
        
        // case 4: 03.01.2010 - 03.01.2010
        // expected: 0
        startCal.set(2010, Calendar.JANUARY, 3);
        endCal.set(2010, Calendar.JANUARY, 3);
        assertEquals(0, new DayRange(locale, startCal.getTime(), endCal.getTime()).getWorkdayCount(weekend));
    }
    
    /** 
     * the suite of Tests to execute
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestDayRange.class);
        // return new TestTimeRange("testCrossOver");
    }

    /** Main function for direct execution. */
    public static void main(String[] args) {
        SHOW_TIME=true;
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

}
