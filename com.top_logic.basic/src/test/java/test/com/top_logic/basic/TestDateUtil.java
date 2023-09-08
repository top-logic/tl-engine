/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.Computation;

/**
 * The TestDateUtil tests the class {@link DateUtil}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestDateUtil extends BasicTestCase {

    private static final long MILLIS = 1;
	private static final long SECONDS = 1000 * MILLIS;
	private static final long MINUTES = 60 * SECONDS;
	private static final long HOURS = 60 * MINUTES;
	
    
    /**
     * Test {@link DateUtil#createYear(int)} method.
     */
    public void testCreateYear() {
		assertEquals(599612400000L, DateUtil.createYear("1989").getTime());
        assertNull  (DateUtil.createYear("Orwell"));
    }
    
    /**
     * Test some variants of the {@link DateUtil#createDate(int, int, int)} method.
     */
    public void testCreateDate() {
		Calendar cal = CalendarUtil.createCalendar(BasicTestCase.getTimeZoneBerlin());
        // Mon Mar 01 00:00:00 CET 1999
		assertEquals(920242800000L, DateUtil.createDate(cal, 1999, Calendar.FEBRUARY, 29).getTime());
        // Mon Mar 01 23:59:00 CET 1999
		assertEquals(920329140000L, DateUtil.createDate(cal, 1999, Calendar.FEBRUARY, 29, 23, 59).getTime());
        // Mon Mar 01 23:59:59 CET 1999
		assertEquals(920329199000L, DateUtil.createDate(cal, 1999, Calendar.FEBRUARY, 29, 23, 59, 59).getTime());
    }

    /**
     * Test {@link DateUtil#compareDates(Date, Date)}.
     */
    public void testCompareDates() {
        Date date1 = DateUtil.createDate(2011, Calendar.JULY, 14);
        Date date2 = DateUtil.createDate(2011, Calendar.JULY, 15);
        assertEquals( 0, DateUtil.compareDates(date1, date1)); 
        assertEquals( 0, DateUtil.compareDates(date2, date2)); 
        assertEquals(-1, DateUtil.compareDates(date1, date2)); 
        assertEquals( 1, DateUtil.compareDates(date2, date1)); 
    }

	/**
	 * Test {@link DateUtil#mergeDateTime(Calendar, Date, Date)}.
	 */
	public void testMergeDateTime() throws ParseException {
		{
			// Test "normal" usage as expected by FormFields ....
			DateFormat shortDateFormat = CalendarUtil.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
			final Date dateAscpect = shortDateFormat.parse("14.7.2011");
			DateFormat shortTimeFormat = CalendarUtil.getTimeInstance(DateFormat.SHORT, Locale.GERMANY);
			final Date timeAscpect = shortTimeFormat.parse("8:12:30");
			DateFormat shortDateTimeFormat = CalendarUtil.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.GERMANY);
			final Date expected = shortDateTimeFormat.parse("14.7.2011, 8:12:30");
			final Date result = DateUtil.mergeDateTime(dateAscpect, timeAscpect);
			assertEquals(expected, result);
		}
		{
			final Calendar cal = CalendarUtil.createCalendar(Locale.GERMANY);

			cal.set(2011, Calendar.JULY, 14, 22, 59, 44);
			cal.set(Calendar.MILLISECOND, 0);
			final Date dateAscpect = cal.getTime();

			cal.set(1, Calendar.JANUARY, 1, 19, 30, 59); // Don try the year 0 (illegal value)
			cal.set(Calendar.MILLISECOND, 22);
			final Date timeAscpect = cal.getTime();
			
			cal.set(Calendar.YEAR, 2011);
			cal.set(2011, Calendar.JULY, 14, 19, 30, 59);
			final Date expected = cal.getTime();

			final Date result = DateUtil.mergeDateTime(cal, dateAscpect, timeAscpect).getTime();

			assertEquals(expected, result);
		}
	}
    
    /**
     * This method tests the {@link DateUtil#nextDay(Date)} method.
     */
    public void testNextDay() {
        Date theDate = new Date();
		Calendar calendar = CalendarUtil.createCalendar(theDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date theNextDay = calendar.getTime();
        
        assertEquals(theNextDay, DateUtil.nextDay(theDate));
        
        try {
            DateUtil.nextDay(null);
            fail();
        } catch (NullPointerException npe) {
            /* Expected */
        }
    }
    
    /**
    * Tests the DateUtil.adjustDateToYearBegin /DateUtil#adjustDateToYearEnd methods.
    */
    public void testAdjustToYear() {
        Date mar1 = new Date(920242800000L); // Mon Mar 01 00:00:00 CET 1999
		Calendar cal = CalendarUtil.createCalendar(BasicTestCase.getTimeZoneBerlin());
		Date first = DateUtil.adjustDateToYearBegin(cal, mar1);
		Date last = DateUtil.adjustDateToYearEnd(cal, mar1);
        assertEquals(915145200000L, first.getTime());   // Mon Mar 01 00:00:00 CET 1999
        assertEquals(946681199999L, last .getTime());   // Fri Dec 31 23:59:59 CET 1999

		Calendar now = CalendarUtil.createCalendar();
        assertEquals(0L, DateUtil.adjustDateToYearBegin(now).getTime() % 100000L);
        assertEquals(99999L, DateUtil.adjustDateToYearEnd  (now).getTime() % 100000L);
    }
    
    /**
     * This method tests adjusting {@link Date}s and Times.
     */
    public void testAdjustTime() {
		assertNull(DateUtil.adjustTime((Date) null, null));
        Date now = new Date();
        assertEquals(now, DateUtil.adjustTime(now, null));
        assertEquals(now, DateUtil.adjustTime(now, now));
    }

    /**
     * Test that timezone "Europe/Berlin" implicitly respects daylight saving time.
     */
    public void testDST() {
		Calendar met = CalendarUtil.createCalendar(TimeZone.getTimeZone("Europe/Berlin"));

    	met.set(Calendar.YEAR, 2011);
    	met.set(Calendar.MONTH, Calendar.MARCH);
    	met.set(Calendar.DAY_OF_MONTH, 27);
    	met.set(Calendar.HOUR_OF_DAY, 0);
    	met.set(Calendar.MINUTE, 0);
    	met.set(Calendar.SECOND, 0);
    	met.set(Calendar.MILLISECOND, 0);
    	long sundayMorning = met.getTimeInMillis();
    	
    	met.set(Calendar.YEAR, 2011);
    	met.set(Calendar.MONTH, Calendar.MARCH);
    	met.set(Calendar.DAY_OF_MONTH, 28);
    	met.set(Calendar.HOUR_OF_DAY, 0);
    	met.set(Calendar.MINUTE, 0);
    	met.set(Calendar.SECOND, 0);
    	met.set(Calendar.MILLISECOND, 0);
    	long sundayMidnight = met.getTimeInMillis();
    	
    	assertEquals("Day has 23 hours.", 23 * HOURS, sundayMidnight - sundayMorning);
    }

	/**
	 * Test that {@link DateUtil#adjustToDayBegin(Date)} even works for the day where daylight
	 * saving time starts, if the server time zone is set to a time zone using DST.
	 */
    public void testAdjustDateDST() {
		BasicTestCase.executeInSystemTimeZone(getTimeZoneBerlin(), new Computation<Void>() {

			@Override
			public Void run() {
				Calendar met2 = Calendar.getInstance(TimeZone.getTimeZone("MET"));
				met2.set(Calendar.YEAR, 2011);
				met2.set(Calendar.MONTH, Calendar.MARCH);
				met2.set(Calendar.DAY_OF_MONTH, 27);
				met2.set(Calendar.HOUR_OF_DAY, 13);
				met2.set(Calendar.MINUTE, 42);
				met2.set(Calendar.SECOND, 23);
				met2.set(Calendar.MILLISECOND, 5);

				Calendar mest = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
				mest.set(Calendar.YEAR, 2011);
				mest.set(Calendar.MONTH, Calendar.MARCH);
				mest.set(Calendar.DAY_OF_MONTH, 27);
				mest.set(Calendar.HOUR_OF_DAY, 13);
				mest.set(Calendar.MINUTE, 42);
				mest.set(Calendar.SECOND, 23);
				mest.set(Calendar.MILLISECOND, 5);

				assertEquals(met2.getTimeInMillis(), mest.getTimeInMillis());

				Date mestSundayAfterNoon = mest.getTime();

				Date sundayMorning = DateUtil.adjustToDayBegin(mestSundayAfterNoon);

				Calendar met = Calendar.getInstance();
				met.set(Calendar.YEAR, 2011);
				met.set(Calendar.MONTH, Calendar.MARCH);
				met.set(Calendar.DAY_OF_MONTH, 27);
				met.set(Calendar.HOUR_OF_DAY, 0);
				met.set(Calendar.MINUTE, 0);
				met.set(Calendar.SECOND, 0);
				met.set(Calendar.MILLISECOND, 0);

				Date metSundayMorning = met.getTime();

				assertEquals(met.getTimeInMillis(), sundayMorning.getTime());
				assertEquals(metSundayMorning.getTime(), sundayMorning.getTime());

				assertEquals(12 * HOURS + 42 * MINUTES + 23 * SECONDS + 5 * MILLIS,
					mestSundayAfterNoon.getTime() - sundayMorning.getTime());
				return null;
			}
		});
	}

	/**
	 * This method tests adjusting {@link Date}s.
	 */
    public void testAdjustDate() {
        Date     theDate     = new Date();
		Calendar theCalendar = CalendarUtil.createCalendar(theDate);
        theCalendar.set(Calendar.HOUR_OF_DAY, 0);
        theCalendar.set(Calendar.MINUTE,      0);
        theCalendar.set(Calendar.SECOND,      0);
        theCalendar.set(Calendar.MILLISECOND, 0);
        
        assertEquals(DateUtil.adjustToDayBegin(theDate), theCalendar.getTime());
        
        theCalendar.set(Calendar.HOUR_OF_DAY, 23);
        theCalendar.set(Calendar.MINUTE,      59);
        theCalendar.set(Calendar.SECOND,      59);
        theCalendar.set(Calendar.MILLISECOND, 999);
        
        assertEquals(DateUtil.adjustToDayEnd(theDate), theCalendar.getTime());
        
        theCalendar.set(Calendar.HOUR_OF_DAY, 12);
        theCalendar.set(Calendar.MINUTE,      0);
        theCalendar.set(Calendar.SECOND,      0);
        theCalendar.set(Calendar.MILLISECOND, 0);
        
        assertEquals(DateUtil.adjustToNoon(theDate), theCalendar.getTime());
    }
    
    public void testAdjustToWeek() {
        Date     theDate     = new Date();
		Calendar theCalendar = CalendarUtil.createCalendar(theDate);
        theCalendar.set(Calendar.HOUR_OF_DAY, 0);
        theCalendar.set(Calendar.MINUTE,      0);
        theCalendar.set(Calendar.SECOND,      0);
        theCalendar.set(Calendar.MILLISECOND, 0);
        theCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        assertEquals(DateUtil.adjustToWeekBegin(theDate), theCalendar.getTime());
        
        theCalendar.set(Calendar.HOUR_OF_DAY, 23);
        theCalendar.set(Calendar.MINUTE,      59);
        theCalendar.set(Calendar.SECOND,      59);
        theCalendar.set(Calendar.MILLISECOND, 999);
        theCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        
        assertEquals(DateUtil.adjustToWeekEnd(theDate), theCalendar.getTime());
    }
    
    /** 
     * Nothing to comment. Read the method name. 
     */
    public void testSameDay() {
        Date theDate  = new Date();
        Date theBegin = DateUtil.adjustToDayBegin(theDate);
        Date theEnd   = DateUtil.adjustToDayEnd(theDate);
        
        assertTrue(DateUtil.sameDay(theBegin, theEnd));
        
		Calendar theCalendar = CalendarUtil.createCalendar(theBegin);
        theCalendar.add(Calendar.MILLISECOND, -1);
        
        assertFalse(DateUtil.sameDay(theCalendar.getTime(), theEnd));
        
        theCalendar.setTime(theEnd);
        theCalendar.add(Calendar.MILLISECOND,  1);
        
        assertFalse(DateUtil.sameDay(theCalendar.getTime(), theBegin));
    }
    
    /**
     * This method tests the {@link DateUtil#sameYear(Date, Date)} method.
     */
    public void testSameYear() {
		Calendar theCalendar = CalendarUtil.createCalendar();
        theCalendar.set(2007, Calendar.MARCH, 1, 0, 0, 0);
        Date date2007Mar = theCalendar.getTime();
        theCalendar.set(2007, Calendar.JANUARY, 1, 0, 0, 0);
        Date date2007Jan = theCalendar.getTime();
        theCalendar.set(2008, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date date2008Feb = theCalendar.getTime();
        
        assertTrue (DateUtil.sameYear(date2007Mar, date2007Jan));
        assertFalse(DateUtil.sameYear(date2007Mar, date2008Feb));
        assertFalse(DateUtil.sameYear(date2008Feb, date2007Jan));
    }
    
    public void testCompare() {
        Date date1, date2;

        date1 = DateUtil.createDate(2009, 3, 15);
        date2 = DateUtil.createDate(2009, 3, 15);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) == 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == 0);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == 0);
        date2 = DateUtil.createDate(2009, 3, 1);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) > 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == 0);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == 0);
        date2 = DateUtil.createDate(2009, 3, 17);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) < 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == 0);
        date2 = DateUtil.createDate(2009, 1, 17);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) > 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == 2);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == 0);
        date2 = DateUtil.createDate(2009, 6, 15);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) < 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == -3);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == 0);

        date2 = DateUtil.createDate(2010, 3, 15);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) < 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == -12);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == -1);
        date2 = DateUtil.createDate(2010, 7, 15);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) < 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == -16);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == -1);
        date2 = DateUtil.createDate(2000, 3, 15);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) > 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == 108);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == 9);
        date2 = DateUtil.createDate(2000, 6, 15);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) > 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == 105);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == 9);

        date1 = DateUtil.createDate(2008, 11, 31);
        date2 = DateUtil.createDate(2009, 0, 1);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) < 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == -1);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == -1);
        assertTrue(DateUtil.compareDatesByDay(date2, date1) > 0);
        assertTrue(DateUtil.compareDatesByMonth(date2, date1) == 1);
        assertTrue(DateUtil.compareDatesByYear(date2, date1) == 1);
        date1 = DateUtil.createDate(2008, 10, 5);
        date2 = DateUtil.createDate(2009, 1, 14);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) < 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == -3);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == -1);
        date1 = DateUtil.createDate(2007, 10, 6);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) < 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == -15);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == -2);
        date2 = DateUtil.createDate(2006, 10, 6);
        assertTrue(DateUtil.compareDatesByDay(date1, date2) > 0);
        assertTrue(DateUtil.compareDatesByMonth(date1, date2) == 12);
        assertTrue(DateUtil.compareDatesByYear(date1, date2) == 1);
    }

	/**
	 * This method tests the ateUtil.difference* methods.
	 */
	public void testDifference() {
		Date date1 = DateUtil.createDate(2013, 3, 27, 8, 47, 12);
		Date date2 = DateUtil.createDate(2013, 3, 27, 8, 48, 13);

		assertEquals(0, DateUtil.distance(date1, date1));
		assertEquals(61000, DateUtil.distance(date1, date2));
		assertEquals(61000, DateUtil.distance(date2, date1));

		assertEquals(0, DateUtil.difference(date1, date1));
		assertEquals(61000, DateUtil.difference(date1, date2));
		assertEquals(-61000, DateUtil.difference(date2, date1));

		assertEquals(0, DateUtil.differenceInDays(date1, date2));
		assertEquals(0, DateUtil.differenceInWeeks(date1, date2));
		assertEquals(0, DateUtil.differenceInMonths(date1, date2));
		assertEquals(0, DateUtil.differenceInQuarters(date1, date2));
		assertEquals(0, DateUtil.differenceInYears(date1, date2));

		date1 = DateUtil.createDate(2013, 3, 20, 8, 47, 12);
		date2 = DateUtil.createDate(2013, 3, 27, 7, 38, 13);

		assertEquals(7, DateUtil.differenceInDays(date1, date2));
		assertEquals(-7, DateUtil.differenceInDays(date2, date1));
		assertEquals(1, DateUtil.differenceInWeeks(date1, date2));
		assertEquals(-1, DateUtil.differenceInWeeks(date2, date1));
		assertEquals(0, DateUtil.differenceInMonths(date1, date2));
		assertEquals(0, DateUtil.differenceInMonths(date2, date1));
		assertEquals(0, DateUtil.differenceInQuarters(date1, date2));
		assertEquals(0, DateUtil.differenceInQuarters(date2, date1));
		assertEquals(0, DateUtil.differenceInYears(date1, date2));
		assertEquals(0, DateUtil.differenceInYears(date2, date1));

		date1 = DateUtil.createDate(2013, 2, 20);
		date2 = DateUtil.createDate(2013, 3, 27);

		assertEquals(38, DateUtil.differenceInDays(date1, date2));
		assertEquals(-38, DateUtil.differenceInDays(date2, date1));
		assertEquals(5, DateUtil.differenceInWeeks(date1, date2));
		assertEquals(-5, DateUtil.differenceInWeeks(date2, date1));
		assertEquals(1, DateUtil.differenceInMonths(date1, date2));
		assertEquals(-1, DateUtil.differenceInMonths(date2, date1));
		assertEquals(1, DateUtil.differenceInQuarters(date1, date2));
		assertEquals(-1, DateUtil.differenceInQuarters(date2, date1));
		assertEquals(0, DateUtil.differenceInYears(date1, date2));
		assertEquals(0, DateUtil.differenceInYears(date2, date1));

		date1 = DateUtil.createDate(2012, 1, 1);
		date2 = DateUtil.createDate(2014, 0, 31);

		assertEquals(730, DateUtil.differenceInDays(date1, date2));
		assertEquals(-730, DateUtil.differenceInDays(date2, date1));
		assertEquals(104, DateUtil.differenceInWeeks(date1, date2));
		assertEquals(-104, DateUtil.differenceInWeeks(date2, date1));
		assertEquals(23, DateUtil.differenceInMonths(date1, date2));
		assertEquals(-23, DateUtil.differenceInMonths(date2, date1));
		assertEquals(8, DateUtil.differenceInQuarters(date1, date2));
		assertEquals(-8, DateUtil.differenceInQuarters(date2, date1));
		assertEquals(2, DateUtil.differenceInYears(date1, date2));
		assertEquals(-2, DateUtil.differenceInYears(date2, date1));
	}

    /**
     * This method tests the {@link DateUtil#dayDiff(Date, Date)} method.
     */
    public void testDayDiff() {
		Calendar theCalendar = CalendarUtil.createCalendar();
        theCalendar.set(2007, Calendar.JANUARY, 1, 0, 0, 0);
        Date firstJanuary2007 = theCalendar.getTime();
        theCalendar.set(2007, Calendar.JANUARY, 2, 0, 0, 0);
        Date secondJanuary2007 = theCalendar.getTime();
        theCalendar.set(2007, Calendar.JANUARY, 10, 0, 0, 0);
        Date tenthJanuary2007 = theCalendar.getTime();

        assertEquals(1, DateUtil.dayDiff(firstJanuary2007, firstJanuary2007));
        assertEquals(2, DateUtil.dayDiff(firstJanuary2007, secondJanuary2007));
        assertEquals(10, DateUtil.dayDiff(firstJanuary2007, tenthJanuary2007));
        assertEquals(-10, DateUtil.dayDiff(tenthJanuary2007, firstJanuary2007));
        assertEquals(-2, DateUtil.dayDiff(secondJanuary2007, firstJanuary2007));

        assertEquals(-1, DateUtil.dayDiff(secondJanuary2007, firstJanuary2007, DateUtil.EXCLUDE_FIRST_DATE));
        assertEquals(0, DateUtil.dayDiff(firstJanuary2007, firstJanuary2007,DateUtil.EXCLUDE_SECOND_DATE));
        assertEquals(0, DateUtil.dayDiff(firstJanuary2007, firstJanuary2007, DateUtil.EXCLUDE_BOTH_DATES));
        assertEquals(0, DateUtil.dayDiff(firstJanuary2007, secondJanuary2007,DateUtil.EXCLUDE_BOTH_DATES));
        assertEquals(1, DateUtil.dayDiff(firstJanuary2007, secondJanuary2007,DateUtil.EXCLUDE_FIRST_DATE));
        assertEquals(0, DateUtil.dayDiff(secondJanuary2007, firstJanuary2007,DateUtil.EXCLUDE_BOTH_DATES));
        assertEquals(10, DateUtil.dayDiff(firstJanuary2007, tenthJanuary2007,DateUtil.EXCLUDE_NO_DATE));
        assertEquals(1, DateUtil.dayDiff(firstJanuary2007, firstJanuary2007,DateUtil.EXCLUDE_NO_DATE));

        try {
            DateUtil.dayDiff(firstJanuary2007, secondJanuary2007, 3);
            fail();
        }catch (IllegalArgumentException iae) {
            /* Expected */
        }
        
        try {
            DateUtil.dayDiff(firstJanuary2007, secondJanuary2007, -1);
            fail();
        }catch (IllegalArgumentException iae) {
            /* Expected */
        }

        Date date1 = DateUtil.createDate(2010, 2, 1);
        Date date2 = DateUtil.createDate(2010, 2, 30);
        assertEquals(30, DateUtil.dayDiff(date1, date2));
        date1 = DateUtil.createDate(2010, 6, 1);
        date2 = DateUtil.createDate(2010, 6, 30);
        assertEquals(30, DateUtil.dayDiff(date1, date2));
        date1 = DateUtil.createDate(2010, 9, 1);
        date2 = DateUtil.createDate(2010, 9, 30);
        assertEquals(30, DateUtil.dayDiff(date1, date2));

        // leap year 2012:
        date1 = DateUtil.createDate(2010, 1, 1);
        date2 = DateUtil.createDate(2010, 2, 1);
        assertEquals(29, DateUtil.dayDiff(date1, date2));
        date1 = DateUtil.createDate(2012, 1, 1);
        date2 = DateUtil.createDate(2012, 2, 1);
        assertEquals(30, DateUtil.dayDiff(date1, date2));
    }
    
    /** 
     * This method tests {@link DateUtil#adjustDateToMonthBegin(Calendar, Date)}.
     */
    public void testAdjustDateToMonthBegin() {
		Calendar theCal = CalendarUtil.createCalendar();
        theCal.set(Calendar.YEAR,         2007);
        theCal.set(Calendar.MONTH,        Calendar.FEBRUARY);
        theCal.set(Calendar.DAY_OF_MONTH, 10);
        
        Date testDate = theCal.getTime();
        
        testDate = DateUtil.adjustDateToMonthBegin(theCal, testDate);
        
        theCal.setTime(testDate);
        assertEquals(theCal.get(Calendar.YEAR),         2007);
        assertEquals(theCal.get(Calendar.MONTH),        Calendar.FEBRUARY);
        assertEquals(theCal.get(Calendar.DAY_OF_MONTH), 1);
    }
    
    /**
     * This method tests the {@link DateUtil#nextDay(Date)} method.
     */
    public void testPrevDay() {
		Date theDate = new Date();
		Calendar calendar = CalendarUtil.createCalendar(theDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date thePrevDay = calendar.getTime();
        
        assertEquals(thePrevDay, DateUtil.prevDay(theDate));
        
        try {
            DateUtil.prevDay(null);
            fail();
        } catch (NullPointerException npe) {
            /* Expected */
        }
    }
    
    /**
     * This method tests the {@link DateUtil#lastDayofTheMonth(Date)} method.
     */
    public void testLastDayofTheMonth() {
		Calendar theCalendar = CalendarUtil.createCalendar();
        theCalendar.set(2007, Calendar.JANUARY, 1, 23, 10, 999);
        Date january = theCalendar.getTime();
        theCalendar.setTime(DateUtil.lastDayofTheMonth(january));
        assertEquals(31, theCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        theCalendar.set(2007, Calendar.FEBRUARY,1,0,0,0);
        Date february = theCalendar.getTime();
        theCalendar.setTime(DateUtil.lastDayofTheMonth(february));
        assertEquals(28, theCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    }
    
    public void testMonthDiff() throws Exception {
    	// Test same date
    	Date currentDate = new Date();
    	assertEquals(1, DateUtil.monthDiffNo0(currentDate, currentDate));

    	// Test same month
    	Date monthStart = DateUtil.adjustDateToMonthBegin(currentDate);
    	Date monthEnd = DateUtil.adjustDateToMonthEnd(currentDate);
    	assertEquals( 1, DateUtil.monthDiffNo0(monthStart, monthEnd));
    	
    	// Test month before
    	Date beforeMonth = DateUtil.addDays(monthStart, -1);
    	assertEquals( 2, DateUtil.monthDiffNo0(beforeMonth, monthStart));
    	assertEquals(-2, DateUtil.monthDiffNo0(monthStart, beforeMonth));

    	// Test month after
    	Date afterMonth = DateUtil.addDays(monthEnd, 1);
    	assertEquals( 2, DateUtil.monthDiffNo0(monthEnd, afterMonth));
    	assertEquals(-2, DateUtil.monthDiffNo0(afterMonth, monthEnd));
    	
    	// Test one year
    	Date yearStart = DateUtil.adjustDateToYearBegin(currentDate);
    	Date yearEnd = DateUtil.adjustDateToYearEnd(currentDate);
    	assertEquals( 12, DateUtil.monthDiffNo0(yearStart, yearEnd));
    	assertEquals(-12, DateUtil.monthDiffNo0(yearEnd, yearStart));
    	
    	// Test more years
		Calendar calendar = CalendarUtil.createCalendar();
        calendar.set(2007, Calendar.MARCH, 5, 15, 30, 555);
        Date firstDate = calendar.getTime();

        calendar.set(2009, Calendar.SEPTEMBER, 20, 12, 0, 876);
        Date secondDate = calendar.getTime();

        assertEquals( 31, DateUtil.monthDiffNo0(firstDate, secondDate));
        assertEquals(-31, DateUtil.monthDiffNo0(secondDate, firstDate));
    }
    
    public void testInInterval() throws Exception {
        Date now = new Date();
        Date a   = DateUtil.addYears(now, -1);
        Date z   = DateUtil.addYears(now, +3);
        
        assertTrue(DateUtil.inInterval(now, a, z));
        assertTrue(DateUtil.inInterval(now, now, now));
        assertFalse(DateUtil.inInterval(now, a, a));
        assertFalse(DateUtil.inInterval(z, a, now));
        assertFalse(DateUtil.inInterval(z, now, a));
        
        assertTrue(DateUtil.inInterval(null, null, null));
        assertTrue(DateUtil.inInterval(now, a, null));
        assertTrue(DateUtil.inInterval(now, null, z));
        assertFalse(DateUtil.inInterval(now, null, a));
        
        assertTrue(DateUtil.inInterval(now, now, null));
        assertTrue(DateUtil.inInterval(now, null, now));
        assertTrue(DateUtil.inInterval(now, null, null));
    }
    
    public void testInDayMonthYearInterval() throws Exception {
        Date date, a, b;
        a = DateUtil.createDate(2010, Calendar.JULY, 5, 9, 0);

        b = DateUtil.createDate(2010, Calendar.JULY, 5, 17, 0);
        date = DateUtil.createDate(2010, Calendar.JULY, 5, 14, 0);
        assertTrue(DateUtil.inInterval(date, a, b));
        assertTrue(DateUtil.inDayInterval(date, a, b));
        assertTrue(DateUtil.inMonthInterval(date, a, b));
        assertTrue(DateUtil.inYearInterval(date, a, b));
        
        b = DateUtil.createDate(2010, Calendar.JULY, 5, 13, 0);
        assertFalse(DateUtil.inInterval(date, a, b));
        assertTrue(DateUtil.inDayInterval(date, a, b));
        assertTrue(DateUtil.inMonthInterval(date, a, b));
        assertTrue(DateUtil.inYearInterval(date, a, b));
        
        b = DateUtil.createDate(2010, Calendar.AUGUST, 4, 8, 0);
        assertTrue(DateUtil.inInterval(date, a, b));
        assertTrue(DateUtil.inDayInterval(date, a, b));
        assertTrue(DateUtil.inMonthInterval(date, a, b));
        assertTrue(DateUtil.inYearInterval(date, a, b));

        date = DateUtil.createDate(2010, Calendar.AUGUST, 4, 18, 0);
        assertFalse(DateUtil.inInterval(date, a, b));
        assertTrue(DateUtil.inDayInterval(date, a, b));
        assertTrue(DateUtil.inMonthInterval(date, a, b));
        assertTrue(DateUtil.inYearInterval(date, a, b));
        
        date = DateUtil.createDate(2010, Calendar.AUGUST, 12, 15, 0);
        assertFalse(DateUtil.inInterval(date, a, b));
        assertFalse(DateUtil.inDayInterval(date, a, b));
        assertTrue(DateUtil.inMonthInterval(date, a, b));
        assertTrue(DateUtil.inYearInterval(date, a, b));
        
        b = DateUtil.createDate(2011, Calendar.SEPTEMBER, 14, 11, 0);
        date = DateUtil.createDate(2010, Calendar.APRIL, 13, 12, 0);
        assertFalse(DateUtil.inInterval(date, a, b));
        assertFalse(DateUtil.inDayInterval(date, a, b));
        assertFalse(DateUtil.inMonthInterval(date, a, b));
        assertTrue(DateUtil.inYearInterval(date, a, b));

        date = DateUtil.createDate(2009, Calendar.APRIL, 13, 12, 0);
        assertFalse(DateUtil.inInterval(date, a, b));
        assertFalse(DateUtil.inDayInterval(date, a, b));
        assertFalse(DateUtil.inMonthInterval(date, a, b));
        assertFalse(DateUtil.inYearInterval(date, a, b));
        
        a = null;
        assertTrue(DateUtil.inInterval(date, a, b));
        assertTrue(DateUtil.inDayInterval(date, a, b));
        assertTrue(DateUtil.inMonthInterval(date, a, b));
        assertTrue(DateUtil.inYearInterval(date, a, b));
        
        date = DateUtil.createDate(2012, Calendar.APRIL, 13, 12, 0);
        assertFalse(DateUtil.inInterval(date, a, b));
        assertFalse(DateUtil.inDayInterval(date, a, b));
        assertFalse(DateUtil.inMonthInterval(date, a, b));
        assertFalse(DateUtil.inYearInterval(date, a, b));
    }
    
    public void testOverlaps() throws Exception {
        Date a1 = new Date(); // today
        Date b1 = DateUtil.addMonths(a1, +6); // today +6 month 
        Date a2 = DateUtil.addYears(a1, +1);  // today +1 year
        Date b2 = DateUtil.addYears(b1, +1);  // today +1.5 years
        
        assertTrue(DateUtil.overlaps(b1, a2, a1, b2));
        assertTrue(DateUtil.overlaps(a1, b2, b1, a2));
        
        assertTrue(DateUtil.overlaps(a1, a2, b1, b2));
        assertTrue(DateUtil.overlaps(a1, a2, a1, a2));
        assertTrue(DateUtil.overlaps(a2, a2, a1, a2));
        assertFalse(DateUtil.overlaps(a1, b1, a2, b2));
        assertFalse(DateUtil.overlaps(a1, a1, b1, b2));

        assertTrue(DateUtil.overlaps(a1, a1, a1, a1));
        
        assertFalse(DateUtil.overlaps(null, null, null, null));
        assertFalse(DateUtil.overlaps(a1, null, null, null));
        assertFalse(DateUtil.overlaps(null, a1, null, null));
        assertFalse(DateUtil.overlaps(null, null, a1, null));
        assertFalse(DateUtil.overlaps(null, null, null, a1));
        
        assertTrue(DateUtil.overlaps(null, a2, b1, b2));
        assertTrue(DateUtil.overlaps(a2, null, b1, b2));
        assertFalse(DateUtil.overlaps(a1, null, b1, b2));
        assertFalse(DateUtil.overlaps(null, a1, b1, b2));
    }
    
    public void testMoveDate() {
    	// some simple positive tests
        Date   refDate = new Date();
        assertEquals(DateUtil.addDays(refDate, 1), DateUtil.add(refDate, "1d"));
        assertEquals(DateUtil.addMonths(refDate, 2), DateUtil.add(refDate, "2M"));
        assertEquals(DateUtil.addMonths(refDate, -2), DateUtil.add(refDate, "-2M"));
        assertEquals(DateUtil.addYears(refDate, -10), DateUtil.add(refDate, "-10y"));
        assertEquals(DateUtil.addYears(refDate, 10), DateUtil.add(refDate, "+10y"));
        assertSame(refDate, DateUtil.add(refDate, null));
        
        // some more complex positive tests
        assertEquals(DateUtil.addDays(DateUtil.addMonths(DateUtil.addYears(refDate, 10), 2), 32), DateUtil.add(refDate, "10y2M32d"));
        assertEquals(DateUtil.addDays(DateUtil.addMonths(DateUtil.addYears(refDate, -10), -2), -32), DateUtil.add(refDate, "-10y2M32d"));
        assertEquals(DateUtil.addDays(DateUtil.addDays(DateUtil.addDays(refDate, -10), -2), -32), DateUtil.add(refDate, "-10d2d32d"));
        assertEquals(DateUtil.addDays(DateUtil.addDays(DateUtil.addDays(refDate, 10), 2), 32), DateUtil.add(refDate, "10d2d32d"));
        // spaces are allowed (just for better readablility in configs)
        assertEquals(DateUtil.addDays(DateUtil.addMonths(DateUtil.addYears(refDate, 10), 2), 32), DateUtil.add(refDate, "  10y   2M  32d    "));
        assertEquals(DateUtil.addDays(DateUtil.addMonths(DateUtil.addYears(refDate, 10), 2), 32), DateUtil.add(refDate, " + 10y   2M  32d    "));
        assertEquals(DateUtil.addDays(DateUtil.addMonths(DateUtil.addYears(refDate, -10), -2), -32), DateUtil.add(refDate, " - 10y   2M  32d    "));
        
        // negative tests
        try {
            // null reference date
			DateUtil.add((Date) null, "13d");
            fail("Expected Exception");
        } catch (Exception e) {
            // expected
        }

        try {
            // no time codes
            DateUtil.add(refDate, "aaa");
            fail("Expected Exception");
        } catch (Exception e) {
            // expected
        }
        
        try {
            // time codes inside a 'normal' string
            DateUtil.add(refDate, "Time: 19d Logfile from +4M33y");
            fail("Expected Exception");
        } catch (Exception e) {
            // expected
        }

        try {
            // multiple -+ signs
            DateUtil.add(refDate, "---+4M3s");
            fail("Expected Exception");
        } catch (Exception e) {
            // expected
        }
        
        try {
            // multiple -+ signs
            DateUtil.add(refDate, "+4M+3d-15S0S");
            fail("Expected Exception");
        } catch (Exception e) {
            // expected
        }
        
        try {
            // duplicate time period
            DateUtil.add(refDate, "3MM");
            fail("Expected Exception");
        } catch (Exception e) {
            // expected
        }
        
        try {
            // unknown time period
            DateUtil.add(refDate, "3ü");
            fail("Expected Exception");
        } catch (Exception e) {
            // expected
        }
    }
    
    public void testToStringSortable() throws Exception {
        // test null
        assertNull(DateUtil.toSortableString(null));
        
        DateFormat format = CalendarUtil.newSimpleDateFormat("yyyyMMdd-HHmmss");
        Date date = new Date();
        assertEquals(format.format(date), DateUtil.toSortableString(date));
    }

	/** Test for: {@link DateUtil#addDays(Date, int)} */
	public void testAddDays() {
		testAddDays(3);
		testAddDays(-3);
	}

	private void testAddDays(int difference) {
		Date baseDate = new Date(0);
		Date calculatedDate = DateUtil.addDays(baseDate, difference);
		assertEquals(difference, DateUtil.dayDiff(baseDate, calculatedDate, DateUtil.EXCLUDE_FIRST_DATE));
	}

	/** Test for: {@link DateUtil#addHours(Date, int)} */
	public void testAddHours() {
		testAddHours(3);
		testAddHours(-3);
	}

	private void testAddHours(int difference) {
		Date baseDate = new Date(0);
		Date calculatedDate = DateUtil.addHours(baseDate, difference);
		assertEquals(difference * HOURS, calculatedDate.getTime() - baseDate.getTime());
	}

	/** Test for: {@link DateUtil#addMinutes(Date, int)} */
	public void testAddMinutes() {
		testAddMinutes(3);
		testAddMinutes(-3);
	}

	private void testAddMinutes(int difference) {
		Date baseDate = new Date(0);
		Date calculatedDate = DateUtil.addMinutes(baseDate, difference);
		assertEquals(difference * MINUTES, calculatedDate.getTime() - baseDate.getTime());
	}

	/** Test for: {@link DateUtil#addSeconds(Date, int)} */
	public void testAddSeconds() {
		testAddSeconds(3);
		testAddSeconds(-3);
	}

	private void testAddSeconds(int difference) {
		Date baseDate = new Date(0);
		Date calculatedDate = DateUtil.addSeconds(baseDate, difference);
		assertEquals(difference * SECONDS, calculatedDate.getTime() - baseDate.getTime());
	}

	/** Test for: {@link DateUtil#addMilliseconds(Date, int)} */
	public void testAddMilliseconds() {
		testAddMilliseconds(3);
		testAddMilliseconds(-3);
	}

	private void testAddMilliseconds(int difference) {
		Date baseDate = new Date(0);
		Date calculatedDate = DateUtil.addMilliseconds(baseDate, difference);
		assertEquals(difference, calculatedDate.getTime() - baseDate.getTime());
	}

	/**
	 * Test for {@link DateUtil#getWeekOfYearFor(Locale, Date)}.
	 */
	public void testWeekOfYear() {
		Calendar cal = CalendarUtil.createCalendar(Locale.GERMAN);
		cal.set(Calendar.YEAR, 2016);
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DAY_OF_MONTH, 15);
		Date date = cal.getTime();

		assertEquals(15, DateUtil.getWeekOfYearFor(Locale.GERMANY, date));
		assertEquals(16, DateUtil.getWeekOfYearFor(Locale.ENGLISH, date));
	}

    /** 
     * This method returns the test suite.
     */
    public static Test suite() {
		Test test = new TestSuite(TestDateUtil.class);
		test = ServiceTestSetup.createSetup(test, TimeZones.Module.INSTANCE);
		return BasicTestSetup.createBasicTestSetup(test);
    }

    /**
     * The main function can be used for direct testing.
     */
    public static void main(String[] args) {
        // Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }
    
}

