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

import com.top_logic.base.time.MonthRange;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Testcase for {@link com.top_logic.base.time.MonthRange}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestMonthRange extends BasicTestCase {

    /**
     * Default constructor.
     *
     * @param name of test to execute.
     */
    public TestMonthRange(String name) {
        super(name);
    }
    
    /** Utily function to create a reasonable MonthRange */
    protected MonthRange createMonthRange(Locale aLoc, int month) {
        MonthRange theRange  = new MonthRange();
		Calendar cal = CalendarUtil.createCalendar(aLoc);
        Date       today     = cal.getTime();
        cal.add(Calendar.MONTH, month);
        Date       nextMonth = cal.getTime();
        theRange.init(aLoc, today, nextMonth);
        assertEquals(month, theRange.getNumRanges() - 1);
        return theRange;
    }
    
    /** Test Iterating over more than one year, starting today.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testMonth(Locale aLoc) throws ParseException {
        final int  MONTH    = 13;
        MonthRange theRange = createMonthRange(aLoc, MONTH);
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
        assertEquals(MONTH + 1, count); // 0-13 are 14 Month, well
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
        assertEquals(MONTH + 1, count);   // 0-13 are 14 Month, well
    }

    /** Test some kinds of invalid operations.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testInvalid() throws ParseException {
        try {
            createMonthRange(Locale.SIMPLIFIED_CHINESE, -1);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) { /* expected */ }
        
        MonthRange theRange = createMonthRange(Locale.US, 99);
        assertNull(theRange.parseInternal(null));
        assertNull(theRange.parseInternal(""));
        assertNull(theRange.parseInternal("BlahBlurb"));
    }

    /** Test Iterating for an US locale
     */
    public void testMonthUS() throws ParseException {
        testMonth(Locale.US);
    }

    /** Test Iterating for a German locale.
     */
    public void testMonthDE() throws ParseException {
        testMonth(Locale.GERMANY);
    }

    /** Test Iterating over three years and measure the time.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testTime() throws ParseException {
        final int  MONTH    = 12 * 10;
        MonthRange theRange = createMonthRange(Locale.GERMAN, MONTH);
        
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
        assertEquals(MONTH + 1, count); // 0-7 are eight Days, well
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
        assertEquals(MONTH + 1, count);  // 0-7 are eight Days, well
        logTime("testTime " + MONTH);
        
    }
    
    /** The equalsRange method needs some special treatment.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testEqualsRange() throws ParseException {
        final int  DAYS     = 0;
        MonthRange theRange = createMonthRange(Locale.FRENCH, DAYS);
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

    /** getNumRanges is most critical mit MonthRange, better have a closer look. */
    public void testNumRanges(){
        Locale     loc       = Locale.GERMAN;
		Calendar cal = CalendarUtil.createCalendar(loc);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.YEAR , 2004);
        for (int janDay = 1; janDay <= 31; janDay++) {
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, janDay);
            Date start = cal.getTime();
            cal.set(Calendar.MONTH, Calendar.FEBRUARY);
            for (int febDay =29; febDay <= 29; febDay++) {
                cal.set(Calendar.DAY_OF_MONTH, febDay);
                Date end = cal.getTime();
                MonthRange theRange  = new MonthRange();
                theRange.init(loc, start, end);
                assertEquals(theRange.toString(), 2, theRange.getNumRanges());
            }
        }
    }
 
    /** 
     * the suite of Tests to execute
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestMonthRange.class);
        // return new TestMonthRange("testNumRanges");
    }

    /** Main function for direct execution. */
    public static void main(String[] args) {
        SHOW_TIME=true;
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

}
