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

import com.top_logic.base.time.WeekRange;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Testcase for {@link com.top_logic.base.time.WeekRange}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestWeekRange extends BasicTestCase {

    /**
     * @param aName the function to execute for testing.
     */
    public TestWeekRange(String aName) {
        super(aName);
    }
    
    /** Utily fucntion to create a reasonable WeekRange */
    protected WeekRange createWeekRange(Locale aLoc, int weeks) {
        WeekRange theRange = new WeekRange();
		Calendar cal = CalendarUtil.createCalendar(aLoc);
        Date     today     = cal.getTime();
        cal.add(Calendar.WEEK_OF_YEAR,weeks);
        Date     nextWeek   = cal.getTime();
        theRange.init(aLoc, today, nextWeek);
        assertEquals(weeks, theRange.getNumRanges() - 1);
        return theRange;
    }
    
    /** Test Iterating over the current week.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testWeek(Locale aLoc) throws ParseException {
        final int  WEEKS     = 12;
        WeekRange  theRange = createWeekRange(aLoc, WEEKS);
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
        assertEquals(WEEKS + 1, count); // 0-12 are 13 Weeks, well
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
        assertEquals(WEEKS + 1, count);  // 0-12 are 13 Weeks, well
    }

    /** Test some kinds of invalid operations.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testInvalid() throws ParseException {
        try {
            createWeekRange(Locale.SIMPLIFIED_CHINESE, -1);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) { /* expected */ }
        
        WeekRange   theRange = createWeekRange(Locale.US, 99);
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

    /** Test Iterating over three years and measure the time.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testTime() throws ParseException {
        final int   WEEKS    = 52 * 10;
        WeekRange   theRange = createWeekRange(Locale.GERMAN, WEEKS);
        
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
        assertEquals(WEEKS + 1, count); 
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
        assertEquals(WEEKS + 1, count); 
        logTime("testTime " + WEEKS);
        
    }
    
    /** The equalsRange method needs some special treatment.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testEqualsRange() throws ParseException {
        final int  WEEKS    = 0;
        WeekRange  theRange = createWeekRange(Locale.GERMAN, WEEKS);
        Date       start    = theRange.getStart();
        Date       end      = theRange.getEnd();
        Date       current  = theRange.next();

        assertTrue(theRange.equalsRange(current , start));
        assertTrue(theRange.equalsRange(current , end));
        assertTrue(theRange.equalsRange(start   , end));

        /** Can not work here due to special formatter, but is quite OK
        assertEquals(theRange.formatInternal(current), 
                     theRange.formatInternal(start));
        assertEquals(theRange.formatInternal(current), 
                     theRange.formatInternal(end));
        assertEquals(theRange.formatInternal(start), 
                     theRange.formatInternal(end));
        */
    }

    /** Test iterationg over the "Edge" of the next 10 years.
     * 
     * Since the first week in the year is locale depended, this can be a paint
     */
    public void testEdge(Locale aLoc) throws ParseException {
        final int   YEARS    = 10;
        final int   WEEKS    = 4;
		Calendar cal = CalendarUtil.createCalendar(aLoc);
        int      startYear   = 2008; // cal.get(Calendar.YEAR);
        int      endYear     = startYear + YEARS;
        startTime();
        for (int year = startYear; year < endYear; year++) {
            WeekRange theRange = new WeekRange();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.WEEK_OF_YEAR, cal.getMaximum(Calendar.WEEK_OF_YEAR) -1);
            Date     today     = cal.getTime();
            cal.add(Calendar.WEEK_OF_YEAR,WEEKS);
            Date     nextWeek   = cal.getTime();
            theRange.init(aLoc, today, nextWeek);

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
                Date parsedDate     = theRange.parseInternal(formString);
                assertEquals(current, parsedDate);
                
                formString = format.format(current);
                assertEquals(current, theRange.align(format.parse(formString)));
                count ++;
            }
            assertEquals(WEEKS + 1, count); 
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
            assertEquals(WEEKS + 1, count); 
        }
        logTime("testEdge " + WEEKS + "/" + YEARS);
        
    }

    /** testEdges in Germany 
     */ 
    public void testEdgeDE() throws ParseException {
        testEdge(Locale.GERMANY);
    }

    /** testEdges in the US 
     */ 
    public void testEdgeUS() throws ParseException {
        testEdge(Locale.US);
    }
    
    /** 
     * the suite of Tests to execute
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestWeekRange.class);
        // return new TestWeekRange("testEdgeDE");
    }

    /** Main function for direct execution. */
    public static void main(String[] args) {
        SHOW_TIME=true;
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

}
