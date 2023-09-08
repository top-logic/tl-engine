/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.time.DayRange;
import com.top_logic.base.time.HourRange;
import com.top_logic.base.time.TimeRangeIterator;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Testcase for {@link com.top_logic.base.time.HourRange}.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>Till Bentz</a>
 */
public class TestHourRange extends BasicTestCase {

    /** Utily function to create a reasonable HourRange */
    protected HourRange createHourRange(Locale aLoc, int hours) {
		return createHourRange(aLoc, createCalendar(aLoc), hours);
	}

	private HourRange createHourRange(Locale locale, Calendar cal, int expectedHours) {
		HourRange theRange = new HourRange();
        Date     today    = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, expectedHours);
        Date     nextHour  = cal.getTime();
		theRange.init(locale, today, nextHour);
        
		assertEquals(expectedHours, theRange.getNumRanges() - 1);
        return theRange;
	}

	private void testHours(Locale locale, Calendar cal, int expectedHours) throws ParseException {
		testHours(createHourRange(locale, cal, expectedHours), expectedHours);
	}
    
	private void testHours(HourRange range, int hours) throws ParseException {
		Date start = range.getStart();
		Date end = range.getEnd();
        assertTrue(start.before(end));
		Date current = range.current();
        assertNull(current); // must call next, first
        int        count    = 0;
        
		while (null != (current = range.next())
              && count < 10000) {    // prevent runaway loop
			checkInRange(range, start, end, current);
            count ++;
        }
		assertEquals(hours + 1, count);
        count = 0;
		while (null != (current = range.previous())
               && count < 10000) {    // prevent runaway loop
			checkInRange(range, start, end, current);
            count++;
        }
		assertEquals(hours + 1, count);
	}

	private void checkInRange(HourRange range, Date start, Date end, Date current) throws ParseException {
		DateFormat format = range.getDateFormat();
		DateFormat simple = range.getSimpleFormat();
		assertTrue(start.before(current));
		assertTrue(end.after(current));
		assertNotNull(range.toString());
		String formString = range.formatInternal(current);
		assertEquals(current, range.parseInternal(formString));

		formString = format.format(current);
		assertEquals(current, range.align(format.parse(formString)));

		assertNotNull(simple.format(current));
		assertNotNull(range.formatCurrent());
	}

    /** Test some kinds of invalid operations.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
	public void testInvalid() {
        try {
            createHourRange(Locale.SIMPLIFIED_CHINESE, -1);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) { /* expected */ }
        
        HourRange   theRange = createHourRange(Locale.US, 99);
        assertNull(theRange.parseInternal(null));
        assertNull(theRange.parseInternal(""));
        assertNull(theRange.parseInternal("BlahBlurb"));
    }

    /** Test Iterating over the current day for an US locale
     */
    public void testDayUS() throws ParseException {
		testHours(Locale.US, createCalendar(Locale.US), 24);
    }

    /** Test Iterating over the current day for a German locale.
     */
    public void testDayDE() throws ParseException {
		testHours(Locale.GERMANY, createCalendar(Locale.GERMANY), 24);
    }

    /** Test Iterating over ten days and measure the time.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
    public void testTime() throws ParseException {
		int hours = 24 * 10;
		testHours(Locale.GERMAN, createCalendar(Locale.GERMAN), hours);
    }
    
    /** The equalsRange method needs some special treatment.
     * 
     * Hmm, this depends on the current time and may not be reproducable :-(
     */
	public void testEqualsRange() {
        final int  HOURS     = 0;
        HourRange   theRange = createHourRange(Locale.GERMAN, HOURS);
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
    	HourRange theMidRange = new HourRange();
		Calendar cal = createCalendar(aLoc);
        Date     now       = cal.getTime();           // 0
        cal.add(Calendar.HOUR_OF_DAY,3);                // 3
        Date     nextHour     = cal.getTime();
        theMidRange.init(aLoc, now, nextHour);

        theMidRange.next();
        assertTrue  (theMidRange.contains(now));
        assertFalse (theMidRange.contains(nextHour));
        
        assertTrue (theMidRange.containsRange(theMidRange));
        assertFalse(theMidRange.after        (theMidRange));
        assertFalse(theMidRange.before       (theMidRange));
        assertEquals(TimeRangeIterator.OVERLAP, theMidRange.overlaps(theMidRange));
        
        HourRange theRange    = new HourRange();
		cal = createCalendar(aLoc);
        cal.add(Calendar.HOUR_OF_DAY,-1);               // -1
        Date     lastHour   = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY,5);                // 5
        Date     coupleHoursLater = cal.getTime();
        theRange.init(aLoc, lastHour, coupleHoursLater);

        // [0..3] ? [ -1..5]
        assertFalse(theMidRange.containsRange(theRange));
        assertFalse(theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.OVERLAP, theMidRange.overlaps(theRange));
        
        theRange    = new HourRange();
		cal = createCalendar(aLoc);
        cal.add(Calendar.HOUR_OF_DAY,1);            // 1
        Date     followingHour    = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY,1);            // 2
        Date     afterFollowingHour = cal.getTime();
        theRange.init(aLoc, followingHour, afterFollowingHour);

        // [0..3] ? [1..2]
        assertTrue (theMidRange.containsRange(theRange));
        assertFalse(theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.OVERLAP, theMidRange.overlaps(theRange));

        theRange    = new HourRange();
        theRange.init(aLoc, lastHour, afterFollowingHour);

        // [0..3] ? [-1..2]
        assertFalse(theMidRange.containsRange(theRange));
        assertFalse(theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.BEFORE_PARTIAL, theMidRange.overlaps(theRange));

        theRange    = new HourRange();
        theRange.init(aLoc, afterFollowingHour, coupleHoursLater);

        // [0..3] ? [2..5]
        assertFalse(theMidRange.containsRange(theRange));
        assertFalse(theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.AFTER_PARTIAL, theMidRange.overlaps(theRange));

        theRange    = new HourRange();
        theRange.init(aLoc, lastHour, lastHour);

        // [0..3] ? [-1..-1]
        assertFalse(theMidRange.containsRange(theRange));
        assertTrue (theMidRange.after        (theRange));
        assertFalse(theMidRange.before       (theRange));
        assertEquals(DayRange.AFTER_COMPLETE, theMidRange.overlaps(theRange));

        theRange    = new HourRange();
        theRange.init(aLoc, coupleHoursLater, coupleHoursLater);

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
	 * Test that parsing/formating during change of summer time to winter time works.
	 */
	public void testSummerWinterTime() throws ParseException {
		Locale locale = Locale.GERMAN;
		Calendar cal = createCalendar(locale);
		// end of summer time
		cal.setTime(CalendarUtil.newSimpleDateFormat("dd.MM.yyyy hh:mm:ss").parse("30.10.2016 01:00:00"));
		try {
			testHours(locale, cal, 3);
			fail(
				"Test should fail due to the known bug in ticket #11597: Start date was in summer time but remains in winter time after parsing/formating.");
		} catch (AssertionFailedError err) {
			String regexp =
				"expected:<(\\w{3} \\w{3} \\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) CEST (\\d{4})> but was:<\\1 CET \\2>";
			if (err.getLocalizedMessage().matches(regexp)) {
				/* Exptected due to the known bug in ticket #11597: Start date was in summer time
				 * but remains in winter time after parsing/formating. */
				return;
			}
		}
	}

	/**
	 * Test that parsing/formating during change of winter time to summer time works.
	 */
	public void testWinterSummerTime() throws ParseException {
		Locale locale = Locale.GERMAN;
		Calendar cal = createCalendar(locale);
		// end of winter time
		cal.setTime(CalendarUtil.newSimpleDateFormat("dd.MM.yyyy hh:mm:ss").parse("31.03.2013 01:00:00"));
		testHours(locale, cal, 3);
	}

	private Calendar createCalendar(Locale locale) {
		ZonedDateTime dateTime = LocalDateTime.of(2022, 5, 5, 5, 30).atZone(ZoneId.systemDefault());

		return CalendarUtil.createCalendar(dateTime.toInstant().toEpochMilli(), locale);
	}
    
    /** 
     * the suite of Tests to execute
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestHourRange.class);
        // return new TestTimeRange("testCrossOver");
    }

    /** Main function for direct execution. */
    public static void main(String[] args) {
        SHOW_TIME=true;
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

}
