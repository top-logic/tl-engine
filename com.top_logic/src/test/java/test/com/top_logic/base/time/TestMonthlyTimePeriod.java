/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.time;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.time.HalfYear;
import com.top_logic.base.time.MonthlyTimePeriod;
import com.top_logic.base.time.TimePeriod;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;


/**
 * TODO fsc This class test {@link HalfYear}
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class TestMonthlyTimePeriod extends BasicTestCase {

    
	public void testHalfYear() throws Exception {
		Calendar now = CalendarUtil.createCalendar();
		DateUtil.adjustToDayBegin(now);

		// start and end dates of this year's half years
		Date begin1 = DateUtil.createDate(now.get(Calendar.YEAR), 0, 1);
		Date end1 = DateUtil.adjustToDayEnd(DateUtil.createDate(now.get(Calendar.YEAR), 5, 30));
		Date begin2 = DateUtil.createDate(now.get(Calendar.YEAR), 6, 1);
		Date end2 = DateUtil.adjustToDayEnd(DateUtil.createDate(now.get(Calendar.YEAR), 11, 31));
		Date begin3 = DateUtil.createDate(now.get(Calendar.YEAR) + 1, 0, 1);
		Date end3 = DateUtil.adjustToDayEnd(DateUtil.createDate(now.get(Calendar.YEAR) + 1, 5, 30));

		Calendar cal = CalendarUtil.clone(now);
		// check with 1.1. = start point
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		for (int i = 0; i < 12; i++) {
			Date time = cal.getTime();
			HalfYear hy = new HalfYear(CalendarUtil.clone(cal));
			if (i < 6) {
				assertEquals("Begin for: " + time, begin1, hy.getBegin());
				assertEquals("End for: " + time, end1, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: " + time, begin2, next.getBegin());
				assertEquals("Next end for: " + time, end2, next.getEnd());
			} else {
				assertEquals("Begin for: " + time, begin2, hy.getBegin());
				assertEquals("End for: " + time, end2, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: " + time, begin3, next.getBegin());
				assertEquals("Next end for: " + time, end3, next.getEnd());
			}
			cal.add(Calendar.MONTH, +1);
			Logger.info("OK", this);
		}

		cal.setTime(now.getTime());
		// check with 15.10. = somwhere in first month
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 15);
		for (int i = 0; i < 12; i++) {
			Date time = cal.getTime();
			HalfYear hy = new HalfYear(CalendarUtil.clone(cal));
			if (i < 6) {
				assertEquals("Begin for: " + time, begin1, hy.getBegin());
				assertEquals("End for: " + time, end1, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: " + time, begin2, next.getBegin());
				assertEquals("Next end for: " + time, end2, next.getEnd());
			} else {
				assertEquals("Begin for: " + time, begin2, hy.getBegin());
				assertEquals("End for: " + time, end2, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: " + time, begin3, next.getBegin());
				assertEquals("Next end for: " + time, end3, next.getEnd());
			}
			cal.add(Calendar.MONTH, +1);
			Logger.info("OK", this);
		}

		cal.setTime(now.getTime());
		// check with 31.1. = start + 1 month -1
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 31);
		for (int i = 0; i < 12; i++) {
			Date time = cal.getTime();
			HalfYear hy = new HalfYear(CalendarUtil.clone(cal));
			if (i < 6) {
				assertEquals("Begin for: " + time, begin1, hy.getBegin());
				assertEquals("End for: " + time, end1, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: " + time, begin2, next.getBegin());
				assertEquals("Next end for: " + time, end2, next.getEnd());
			} else {
				assertEquals("Begin for: " + time, begin2, hy.getBegin());
				assertEquals("End for: " + time, end2, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: " + time, begin3, next.getBegin());
				assertEquals("Next end for: " + time, end3, next.getEnd());
			}
			cal.add(Calendar.MONTH, +1);
			Logger.info("OK", this);
		}
	}

	public void testHalfYearWithDifferentBusinessYear() throws Exception {
		Calendar now = CalendarUtil.createCalendar();
		DateUtil.adjustToDayBegin(now);

		// start and end dates of this business year's half years
		// from 1.10. until 31.3, and 1.4 until 31.9.
		Date begin1 = DateUtil.createDate(now.get(Calendar.YEAR), Calendar.OCTOBER, 1); // 2011
		Date end1 = DateUtil.adjustToDayEnd(DateUtil.createDate(now.get(Calendar.YEAR) + 1, Calendar.MARCH, 31)); // 2012
		Date begin2 = DateUtil.createDate(now.get(Calendar.YEAR) + 1, 3, 1); // 2012
		Date end2 = DateUtil.adjustToDayEnd(DateUtil.createDate(now.get(Calendar.YEAR) + 1, Calendar.SEPTEMBER, 30)); // 2012
		Date begin3 = DateUtil.createDate(now.get(Calendar.YEAR) + 1, 9, 1); // 2012
		Date end3 = DateUtil.adjustToDayEnd(DateUtil.createDate(now.get(Calendar.YEAR) + 2, Calendar.MARCH, 31)); // 2013

		Calendar cal = CalendarUtil.clone(now);
		// check with 1.10. = start point
		cal.set(Calendar.MONTH, 9);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		for (int i = 0; i < 12; i++) {
			Date time = cal.getTime();
			HalfYear hy = new HalfYear(CalendarUtil.clone(cal), 9, 1);
			if (i < 6) {
				assertEquals("Begin for: " + time, begin1, hy.getBegin());
				assertEquals("End for: " + time, end1, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin2, next.getBegin());
				assertEquals("Next end for: ", end2, next.getEnd());
			} else {
				assertEquals("Begin for: " + time, begin2, hy.getBegin());
				assertEquals("End for: " + time, end2, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin3, next.getBegin());
				assertEquals("Next end for: ", end3, next.getEnd());
			}
			cal.add(Calendar.MONTH, +1);
			Logger.info("OK", this);
		}

		cal.setTime(now.getTime());
		// check with 15.10. = somewhere in interval
		cal.set(Calendar.MONTH, 9);
		cal.set(Calendar.DAY_OF_MONTH, 15);
		for (int i = 0; i < 12; i++) {
			Date time = cal.getTime();
			HalfYear hy = new HalfYear(CalendarUtil.clone(cal), 9, 1);
			if (i < 6) {
				assertEquals("Begin for: " + time, begin1, hy.getBegin());
				assertEquals("End for: " + time, end1, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin2, next.getBegin());
				assertEquals("Next end for: ", end2, next.getEnd());
			} else {
				assertEquals("Begin for: " + time, begin2, hy.getBegin());
				assertEquals("End for: " + time, end2, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin3, next.getBegin());
				assertEquals("Next end for: ", end3, next.getEnd());
			}
			cal.add(Calendar.MONTH, +1);
			Logger.info("OK", this);
		}

		cal.setTime(now.getTime());
		// check with 31.10. = start + 1 month -1
		cal.set(Calendar.MONTH, 9);
		cal.set(Calendar.DAY_OF_MONTH, 31);
		for (int i = 0; i < 12; i++) {
			Date time = cal.getTime();
			HalfYear hy = new HalfYear(CalendarUtil.clone(cal), 9, 1);
			if (i < 6) {
				assertEquals("Begin for: " + time, begin1, hy.getBegin());
				assertEquals("End for: " + time, end1, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin2, next.getBegin());
				assertEquals("Next end for: ", end2, next.getEnd());
			} else {
				assertEquals("Begin for: " + time, begin2, hy.getBegin());
				assertEquals("End for: " + time, end2, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin3, next.getBegin());
				assertEquals("Next end for: ", end3, next.getEnd());
			}
			cal.add(Calendar.MONTH, +1);
			Logger.info("OK", this);
		}
	}
	
	public void testHalfYearWithDifferentBusinessYearAndDay() throws Exception {
		Calendar now = CalendarUtil.createCalendar();
		DateUtil.adjustToDayBegin(now);

		// start and end dates of this business year's half years
		// from 15.4. until 14.10., and 15.10. until 14.4.
		Date begin1 = DateUtil.createDate(now.get(Calendar.YEAR), 3, 15); // 2011
		Date end1 = DateUtil.adjustToDayEnd(DateUtil.createDate(now.get(Calendar.YEAR), 9, 14)); // 2011
		Date begin2 = DateUtil.createDate(now.get(Calendar.YEAR), 9, 15); // 2011
		Date end2 = DateUtil.adjustToDayEnd(DateUtil.createDate(now.get(Calendar.YEAR) + 1, 3, 14)); // 2012
		Date begin3 = DateUtil.createDate(now.get(Calendar.YEAR) + 1, 3, 15); // 2012
		Date end3 = DateUtil.adjustToDayEnd(DateUtil.createDate(now.get(Calendar.YEAR) + 1, 9, 14)); // 2012

		Calendar cal = CalendarUtil.clone(now);
		// check with 15.4. = start point
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 15);
		for (int i = 0; i < 12; i++) {
			Date time = cal.getTime();
			HalfYear hy = new HalfYear(CalendarUtil.clone(cal), 3, 15);
			if (i < 6) {
				assertEquals("Begin for: " + time, begin1, hy.getBegin());
				assertEquals("End for: " + time, end1, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin2, next.getBegin());
				assertEquals("Next end for: ", end2, next.getEnd());
			} else {
				assertEquals("Begin for: " + time, begin2, hy.getBegin());
				assertEquals("End for: " + time, end2, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin3, next.getBegin());
				assertEquals("Next end for: ", end3, next.getEnd());
			}
			cal.add(Calendar.MONTH, +1);
			Logger.info("OK", this);
		}

		cal.setTime(now.getTime());
		// check with 31.4. = somewhere in interval
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 31);
		for (int i = 0; i < 12; i++) {
			Date time = cal.getTime();
			HalfYear hy = new HalfYear(CalendarUtil.clone(cal), 3, 15);
			if (i < 6) {
				assertEquals("Begin for: " + time, begin1, hy.getBegin());
				assertEquals("End for: " + time, end1, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin2, next.getBegin());
				assertEquals("Next end for: ", end2, next.getEnd());
			} else {
				assertEquals("Begin for: " + time, begin2, hy.getBegin());
				assertEquals("End for: " + time, end2, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin3, next.getBegin());
				assertEquals("Next end for: ", end3, next.getEnd());
			}
			cal.add(Calendar.MONTH, +1);
			Logger.info("OK", this);
		}

		cal.setTime(now.getTime());
		// check with 14.5. = start + 1 month -1
		cal.set(Calendar.MONTH, 4);
		cal.set(Calendar.DAY_OF_MONTH, 14);
		for (int i = 0; i < 12; i++) {
			Date time = cal.getTime();
			HalfYear hy = new HalfYear(CalendarUtil.clone(cal), 3, 15);
			if (i < 6) {
				assertEquals("Begin for: " + time, begin1, hy.getBegin());
				assertEquals("End for: " + time, end1, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin2, next.getBegin());
				assertEquals("Next end for: ", end2, next.getEnd());
			} else {
				assertEquals("Begin for: " + time, begin2, hy.getBegin());
				assertEquals("End for: " + time, end2, hy.getEnd());
				TimePeriod next = hy.getNextPeriod();
				assertEquals("Next begin for: ", begin3, next.getBegin());
				assertEquals("Next end for: ", end3, next.getEnd());
			}
			cal.add(Calendar.MONTH, +1);
			Logger.info("OK", this);
		}
	}

	public void testIntervalLenth1() throws Exception {

		Date[][] expectedBeginEnds = new Date[12][];
		Date[][] expectedNextBeginEnds = new Date[12][];

		// 2004 is a leap year, with a 29. February
		expectedBeginEnds[0] = createExpectedBeginEnd(2004, 0, 1, 2004, 0, 31);
		expectedBeginEnds[1] = createExpectedBeginEnd(2004, 1, 1, 2004, 1, 29);
		expectedBeginEnds[2] = createExpectedBeginEnd(2004, 2, 1, 2004, 2, 31);
		expectedBeginEnds[3] = createExpectedBeginEnd(2004, 3, 1, 2004, 3, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2004, 4, 1, 2004, 4, 31);
		expectedBeginEnds[5] = createExpectedBeginEnd(2004, 5, 1, 2004, 5, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2004, 6, 1, 2004, 6, 31);
		expectedBeginEnds[7] = createExpectedBeginEnd(2004, 7, 1, 2004, 7, 31);
		expectedBeginEnds[8] = createExpectedBeginEnd(2004, 8, 1, 2004, 8, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2004, 9, 1, 2004, 9, 31);
		expectedBeginEnds[10] = createExpectedBeginEnd(2004, 10, 1, 2004, 10, 30);
		expectedBeginEnds[11] = createExpectedBeginEnd(2004, 11, 1, 2004, 11, 31);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2004, 1, 1, 2004, 1, 29);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2004, 2, 1, 2004, 2, 31);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2004, 3, 1, 2004, 3, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2004, 4, 1, 2004, 4, 31);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2004, 5, 1, 2004, 5, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2004, 6, 1, 2004, 6, 31);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2004, 7, 1, 2004, 7, 31);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2004, 8, 1, 2004, 8, 30);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2004, 9, 1, 2004, 9, 31);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2004, 10, 1, 2004, 10, 30);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2004, 11, 1, 2004, 11, 31);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2005, 0, 1, 2005, 0, 31);

		checkYear(createMonthLine(2004, 0, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 1);
		checkYear(createMonthLine(2004, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 1);
		checkYear(createMonthLineEnd(2004, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 1);

		expectedBeginEnds[0] = createExpectedBeginEnd(2004, 0, 15, 2004, 1, 14);
		expectedBeginEnds[1] = createExpectedBeginEnd(2004, 1, 15, 2004, 2, 14);
		expectedBeginEnds[2] = createExpectedBeginEnd(2004, 2, 15, 2004, 3, 14);
		expectedBeginEnds[3] = createExpectedBeginEnd(2004, 3, 15, 2004, 4, 14);
		expectedBeginEnds[4] = createExpectedBeginEnd(2004, 4, 15, 2004, 5, 14);
		expectedBeginEnds[5] = createExpectedBeginEnd(2004, 5, 15, 2004, 6, 14);
		expectedBeginEnds[6] = createExpectedBeginEnd(2004, 6, 15, 2004, 7, 14);
		expectedBeginEnds[7] = createExpectedBeginEnd(2004, 7, 15, 2004, 8, 14);
		expectedBeginEnds[8] = createExpectedBeginEnd(2004, 8, 15, 2004, 9, 14);
		expectedBeginEnds[9] = createExpectedBeginEnd(2004, 9, 15, 2004, 10, 14);
		expectedBeginEnds[10] = createExpectedBeginEnd(2004, 10, 15, 2004, 11, 14);
		expectedBeginEnds[11] = createExpectedBeginEnd(2004, 11, 15, 2005, 0, 14);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2004, 1, 15, 2004, 2, 14);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2004, 2, 15, 2004, 3, 14);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2004, 3, 15, 2004, 4, 14);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2004, 4, 15, 2004, 5, 14);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2004, 5, 15, 2004, 6, 14);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2004, 6, 15, 2004, 7, 14);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2004, 7, 15, 2004, 8, 14);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2004, 8, 15, 2004, 9, 14);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2004, 9, 15, 2004, 10, 14);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2004, 10, 15, 2004, 11, 14);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2004, 11, 15, 2005, 0, 14);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2005, 0, 15, 2005, 1, 14);

		checkYear(createMonthLine(2004, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);
		checkYear(createMonthLine(2004, 0, 21), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);
		checkYear(createMonthLineEnd(2004, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);
		checkYear(createMonthLine(2004, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);
		checkYear(createMonthLine(2004, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);

		expectedBeginEnds[0] = createExpectedBeginEnd(2004, 0, 31, 2004, 1, 28);
		expectedBeginEnds[1] = createExpectedBeginEnd(2004, 1, 29, 2004, 2, 30);
		expectedBeginEnds[2] = createExpectedBeginEnd(2004, 2, 31, 2004, 3, 29);
		expectedBeginEnds[3] = createExpectedBeginEnd(2004, 3, 30, 2004, 4, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2004, 4, 31, 2004, 5, 29);
		expectedBeginEnds[5] = createExpectedBeginEnd(2004, 5, 30, 2004, 6, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2004, 6, 31, 2004, 7, 30);
		expectedBeginEnds[7] = createExpectedBeginEnd(2004, 7, 31, 2004, 8, 29);
		expectedBeginEnds[8] = createExpectedBeginEnd(2004, 8, 30, 2004, 9, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2004, 9, 31, 2004, 10, 29);
		expectedBeginEnds[10] = createExpectedBeginEnd(2004, 10, 30, 2004, 11, 30);
		expectedBeginEnds[11] = createExpectedBeginEnd(2004, 11, 31, 2005, 0, 30);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2004, 1, 29, 2004, 2, 30);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2004, 2, 31, 2004, 3, 29);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2004, 3, 30, 2004, 4, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2004, 4, 31, 2004, 5, 29);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2004, 5, 30, 2004, 6, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2004, 6, 31, 2004, 7, 30);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2004, 7, 31, 2004, 8, 29);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2004, 8, 30, 2004, 9, 30);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2004, 9, 31, 2004, 10, 29);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2004, 10, 30, 2004, 11, 30);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2004, 11, 31, 2005, 0, 30);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2005, 0, 31, 2005, 1, 27);

		checkYear(createMonthLineEnd(2004, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);
		checkYear(createMonthLine(2004, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);
		checkYear(createMonthLine(2004, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);
		checkYear(createMonthLine(2004, 1, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);
		checkYear(createMonthLine(2004, 1, 27), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);

		// 2006 is a normal year, the only difference to above is 28. February
		expectedBeginEnds[0] = createExpectedBeginEnd(2006, 0, 1, 2006, 0, 31);
		expectedBeginEnds[1] = createExpectedBeginEnd(2006, 1, 1, 2006, 1, 28);
		expectedBeginEnds[2] = createExpectedBeginEnd(2006, 2, 1, 2006, 2, 31);
		expectedBeginEnds[3] = createExpectedBeginEnd(2006, 3, 1, 2006, 3, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2006, 4, 1, 2006, 4, 31);
		expectedBeginEnds[5] = createExpectedBeginEnd(2006, 5, 1, 2006, 5, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2006, 6, 1, 2006, 6, 31);
		expectedBeginEnds[7] = createExpectedBeginEnd(2006, 7, 1, 2006, 7, 31);
		expectedBeginEnds[8] = createExpectedBeginEnd(2006, 8, 1, 2006, 8, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2006, 9, 1, 2006, 9, 31);
		expectedBeginEnds[10] = createExpectedBeginEnd(2006, 10, 1, 2006, 10, 30);
		expectedBeginEnds[11] = createExpectedBeginEnd(2006, 11, 1, 2006, 11, 31);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2006, 1, 1, 2006, 1, 28);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2006, 2, 1, 2006, 2, 31);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2006, 3, 1, 2006, 3, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2006, 4, 1, 2006, 4, 31);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2006, 5, 1, 2006, 5, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2006, 6, 1, 2006, 6, 31);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2006, 7, 1, 2006, 7, 31);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2006, 8, 1, 2006, 8, 30);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2006, 9, 1, 2006, 9, 31);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2006, 10, 1, 2006, 10, 30);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2006, 11, 1, 2006, 11, 31);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2007, 0, 1, 2007, 0, 31);

		checkYear(createMonthLine(2006, 0, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 1);
		checkYear(createMonthLine(2006, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 1);
		checkYear(createMonthLineEnd(2006, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 1);

		expectedBeginEnds[0] = createExpectedBeginEnd(2006, 0, 15, 2006, 1, 14);
		expectedBeginEnds[1] = createExpectedBeginEnd(2006, 1, 15, 2006, 2, 14);
		expectedBeginEnds[2] = createExpectedBeginEnd(2006, 2, 15, 2006, 3, 14);
		expectedBeginEnds[3] = createExpectedBeginEnd(2006, 3, 15, 2006, 4, 14);
		expectedBeginEnds[4] = createExpectedBeginEnd(2006, 4, 15, 2006, 5, 14);
		expectedBeginEnds[5] = createExpectedBeginEnd(2006, 5, 15, 2006, 6, 14);
		expectedBeginEnds[6] = createExpectedBeginEnd(2006, 6, 15, 2006, 7, 14);
		expectedBeginEnds[7] = createExpectedBeginEnd(2006, 7, 15, 2006, 8, 14);
		expectedBeginEnds[8] = createExpectedBeginEnd(2006, 8, 15, 2006, 9, 14);
		expectedBeginEnds[9] = createExpectedBeginEnd(2006, 9, 15, 2006, 10, 14);
		expectedBeginEnds[10] = createExpectedBeginEnd(2006, 10, 15, 2006, 11, 14);
		expectedBeginEnds[11] = createExpectedBeginEnd(2006, 11, 15, 2007, 0, 14);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2006, 1, 15, 2006, 2, 14);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2006, 2, 15, 2006, 3, 14);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2006, 3, 15, 2006, 4, 14);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2006, 4, 15, 2006, 5, 14);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2006, 5, 15, 2006, 6, 14);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2006, 6, 15, 2006, 7, 14);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2006, 7, 15, 2006, 8, 14);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2006, 8, 15, 2006, 9, 14);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2006, 9, 15, 2006, 10, 14);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2006, 10, 15, 2006, 11, 14);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2006, 11, 15, 2007, 0, 14);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2007, 0, 15, 2007, 1, 14);

		checkYear(createMonthLine(2006, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);
		checkYear(createMonthLine(2006, 0, 21), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);
		checkYear(createMonthLineEnd(2006, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);
		checkYear(createMonthLine(2006, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);
		checkYear(createMonthLine(2006, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 1);

		expectedBeginEnds[0] = createExpectedBeginEnd(2006, 0, 31, 2006, 1, 27);
		expectedBeginEnds[1] = createExpectedBeginEnd(2006, 1, 28, 2006, 2, 30);
		expectedBeginEnds[2] = createExpectedBeginEnd(2006, 2, 31, 2006, 3, 29);
		expectedBeginEnds[3] = createExpectedBeginEnd(2006, 3, 30, 2006, 4, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2006, 4, 31, 2006, 5, 29);
		expectedBeginEnds[5] = createExpectedBeginEnd(2006, 5, 30, 2006, 6, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2006, 6, 31, 2006, 7, 30);
		expectedBeginEnds[7] = createExpectedBeginEnd(2006, 7, 31, 2006, 8, 29);
		expectedBeginEnds[8] = createExpectedBeginEnd(2006, 8, 30, 2006, 9, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2006, 9, 31, 2006, 10, 29);
		expectedBeginEnds[10] = createExpectedBeginEnd(2006, 10, 30, 2006, 11, 30);
		expectedBeginEnds[11] = createExpectedBeginEnd(2006, 11, 31, 2007, 0, 30);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2006, 1, 28, 2006, 2, 30);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2006, 2, 31, 2006, 3, 29);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2006, 3, 30, 2006, 4, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2006, 4, 31, 2006, 5, 29);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2006, 5, 30, 2006, 6, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2006, 6, 31, 2006, 7, 30);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2006, 7, 31, 2006, 8, 29);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2006, 8, 30, 2006, 9, 30);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2006, 9, 31, 2006, 10, 29);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2006, 10, 30, 2006, 11, 30);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2006, 11, 31, 2007, 0, 30);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2007, 0, 31, 2007, 1, 27);

		checkYear(createMonthLineEnd(2006, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);
		checkYear(createMonthLine(2006, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);
		checkYear(createMonthLine(2006, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);
		checkYear(createMonthLine(2006, 1, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);
		checkYear(createMonthLine(2006, 1, 27), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 1);
	}

	public void testIntervalLenth12() throws Exception {

		Date[][] expectedBeginEnds = new Date[12][];
		Date[][] expectedNextBeginEnds = new Date[12][];
		// 2004 is a leap year, with a 29. February
		expectedBeginEnds[0] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[1] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[2] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[3] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[4] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[5] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[6] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[7] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[8] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[9] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[10] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);
		expectedBeginEnds[11] = createExpectedBeginEnd(2004, 0, 1, 2004, 11, 31);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2005, 0, 1, 2005, 11, 31);

		checkYear(createMonthLine(2004, 0, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 12);
		checkYear(createMonthLine(2004, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 12);
		checkYear(createMonthLineEnd(2004, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 12);

		expectedBeginEnds[0] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[1] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[2] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[3] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[4] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[5] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[6] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[7] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[8] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[9] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[10] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);
		expectedBeginEnds[11] = createExpectedBeginEnd(2004, 0, 15, 2005, 0, 14);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2005, 0, 15, 2006, 0, 14);

		checkYear(createMonthLine(2004, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);
		checkYear(createMonthLine(2004, 0, 21), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);
		checkYear(createMonthLineEnd(2004, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);
		checkYear(createMonthLine(2004, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);
		checkYear(createMonthLine(2004, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);

		expectedBeginEnds[0] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[1] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[2] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[3] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[5] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[7] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[8] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[10] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);
		expectedBeginEnds[11] = createExpectedBeginEnd(2004, 0, 31, 2005, 0, 30);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2005, 0, 31, 2006, 0, 30);

		checkYear(createMonthLineEnd(2004, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);
		checkYear(createMonthLine(2004, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);
		checkYear(createMonthLine(2004, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);
		checkYear(createMonthLine(2004, 1, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);
		checkYear(createMonthLine(2004, 1, 28), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);

		// 2006 is a normal year, the only difference to above is 28. February
		expectedBeginEnds[0] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[1] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[2] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[3] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[4] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[5] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[6] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[7] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[8] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[9] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[10] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);
		expectedBeginEnds[11] = createExpectedBeginEnd(2006, 0, 1, 2006, 11, 31);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2007, 0, 1, 2007, 11, 31);

		checkYear(createMonthLine(2006, 0, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 12);
		checkYear(createMonthLine(2006, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 12);
		checkYear(createMonthLineEnd(2006, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 12);

		expectedBeginEnds[0] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[1] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[2] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[3] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[4] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[5] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[6] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[7] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[8] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[9] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[10] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);
		expectedBeginEnds[11] = createExpectedBeginEnd(2006, 0, 15, 2007, 0, 14);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2007, 0, 15, 2008, 0, 14);

		checkYear(createMonthLine(2006, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);
		checkYear(createMonthLine(2006, 0, 21), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);
		checkYear(createMonthLineEnd(2006, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);
		checkYear(createMonthLine(2006, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);
		checkYear(createMonthLine(2006, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 12);

		expectedBeginEnds[0] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[1] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[2] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[3] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[5] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[7] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[8] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[10] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);
		expectedBeginEnds[11] = createExpectedBeginEnd(2006, 0, 31, 2007, 0, 30);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2007, 0, 31, 2008, 0, 30);

		checkYear(createMonthLineEnd(2006, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);
		checkYear(createMonthLine(2006, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);
		checkYear(createMonthLine(2006, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);
		checkYear(createMonthLine(2006, 1, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);
		checkYear(createMonthLine(2006, 1, 28), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 12);
	}

	public void testIntervalLenth3() throws Exception {

		Date[][] expectedBeginEnds = new Date[12][];
		Date[][] expectedNextBeginEnds = new Date[12][];

		// 2004 is a leap year, with a 29. February
		expectedBeginEnds[0] = createExpectedBeginEnd(2004, 0, 1, 2004, 2, 31);
		expectedBeginEnds[1] = createExpectedBeginEnd(2004, 0, 1, 2004, 2, 31);
		expectedBeginEnds[2] = createExpectedBeginEnd(2004, 0, 1, 2004, 2, 31);
		expectedBeginEnds[3] = createExpectedBeginEnd(2004, 3, 1, 2004, 5, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2004, 3, 1, 2004, 5, 30);
		expectedBeginEnds[5] = createExpectedBeginEnd(2004, 3, 1, 2004, 5, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2004, 6, 1, 2004, 8, 30);
		expectedBeginEnds[7] = createExpectedBeginEnd(2004, 6, 1, 2004, 8, 30);
		expectedBeginEnds[8] = createExpectedBeginEnd(2004, 6, 1, 2004, 8, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2004, 9, 1, 2004, 11, 31);
		expectedBeginEnds[10] = createExpectedBeginEnd(2004, 9, 1, 2004, 11, 31);
		expectedBeginEnds[11] = createExpectedBeginEnd(2004, 9, 1, 2004, 11, 31);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2004, 3, 1, 2004, 5, 30);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2004, 3, 1, 2004, 5, 30);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2004, 3, 1, 2004, 5, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2004, 6, 1, 2004, 8, 30);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2004, 6, 1, 2004, 8, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2004, 6, 1, 2004, 8, 30);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2004, 9, 1, 2004, 11, 31);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2004, 9, 1, 2004, 11, 31);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2004, 9, 1, 2004, 11, 31);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2005, 0, 1, 2005, 2, 31);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2005, 0, 1, 2005, 2, 31);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2005, 0, 1, 2005, 2, 31);

		checkYear(createMonthLine(2004, 0, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 3);
		checkYear(createMonthLine(2004, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 3);
		checkYear(createMonthLineEnd(2004, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 3);

		expectedBeginEnds[0] = createExpectedBeginEnd(2004, 0, 15, 2004, 3, 14);
		expectedBeginEnds[1] = createExpectedBeginEnd(2004, 0, 15, 2004, 3, 14);
		expectedBeginEnds[2] = createExpectedBeginEnd(2004, 0, 15, 2004, 3, 14);
		expectedBeginEnds[3] = createExpectedBeginEnd(2004, 3, 15, 2004, 6, 14);
		expectedBeginEnds[4] = createExpectedBeginEnd(2004, 3, 15, 2004, 6, 14);
		expectedBeginEnds[5] = createExpectedBeginEnd(2004, 3, 15, 2004, 6, 14);
		expectedBeginEnds[6] = createExpectedBeginEnd(2004, 6, 15, 2004, 9, 14);
		expectedBeginEnds[7] = createExpectedBeginEnd(2004, 6, 15, 2004, 9, 14);
		expectedBeginEnds[8] = createExpectedBeginEnd(2004, 6, 15, 2004, 9, 14);
		expectedBeginEnds[9] = createExpectedBeginEnd(2004, 9, 15, 2005, 0, 14);
		expectedBeginEnds[10] = createExpectedBeginEnd(2004, 9, 15, 2005, 0, 14);
		expectedBeginEnds[11] = createExpectedBeginEnd(2004, 9, 15, 2005, 0, 14);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2004, 3, 15, 2004, 6, 14);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2004, 3, 15, 2004, 6, 14);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2004, 3, 15, 2004, 6, 14);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2004, 6, 15, 2004, 9, 14);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2004, 6, 15, 2004, 9, 14);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2004, 6, 15, 2004, 9, 14);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2004, 9, 15, 2005, 0, 14);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2004, 9, 15, 2005, 0, 14);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2004, 9, 15, 2005, 0, 14);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2005, 0, 15, 2005, 3, 14);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2005, 0, 15, 2005, 3, 14);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2005, 0, 15, 2005, 3, 14);

		checkYear(createMonthLine(2004, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);
		checkYear(createMonthLine(2004, 0, 21), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);
		checkYear(createMonthLineEnd(2004, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);
		checkYear(createMonthLine(2004, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);
		checkYear(createMonthLine(2004, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);

		expectedBeginEnds[0] = createExpectedBeginEnd(2004, 0, 31, 2004, 3, 29);
		expectedBeginEnds[1] = createExpectedBeginEnd(2004, 0, 31, 2004, 3, 29);
		expectedBeginEnds[2] = createExpectedBeginEnd(2004, 0, 31, 2004, 3, 29);
		expectedBeginEnds[3] = createExpectedBeginEnd(2004, 3, 30, 2004, 6, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2004, 3, 30, 2004, 6, 30);
		expectedBeginEnds[5] = createExpectedBeginEnd(2004, 3, 30, 2004, 6, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2004, 6, 31, 2004, 9, 30);
		expectedBeginEnds[7] = createExpectedBeginEnd(2004, 6, 31, 2004, 9, 30);
		expectedBeginEnds[8] = createExpectedBeginEnd(2004, 6, 31, 2004, 9, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2004, 9, 31, 2005, 0, 30);
		expectedBeginEnds[10] = createExpectedBeginEnd(2004, 9, 31, 2005, 0, 30);
		expectedBeginEnds[11] = createExpectedBeginEnd(2004, 9, 31, 2005, 0, 30);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2004, 3, 30, 2004, 6, 30);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2004, 3, 30, 2004, 6, 30);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2004, 3, 30, 2004, 6, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2004, 6, 31, 2004, 9, 30);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2004, 6, 31, 2004, 9, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2004, 6, 31, 2004, 9, 30);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2004, 9, 31, 2005, 0, 30);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2004, 9, 31, 2005, 0, 30);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2004, 9, 31, 2005, 0, 30);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2005, 0, 31, 2005, 3, 29);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2005, 0, 31, 2005, 3, 29);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2005, 0, 31, 2005, 3, 29);

		checkYear(createMonthLineEnd(2004, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);
		checkYear(createMonthLine(2004, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);
		checkYear(createMonthLine(2004, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);
		checkYear(createMonthLine(2004, 1, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);
		checkYear(createMonthLine(2004, 1, 28), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);

		// 2006 is a normal year, the only difference to above is 28. February
		expectedBeginEnds[0] = createExpectedBeginEnd(2006, 0, 1, 2006, 2, 31);
		expectedBeginEnds[1] = createExpectedBeginEnd(2006, 0, 1, 2006, 2, 31);
		expectedBeginEnds[2] = createExpectedBeginEnd(2006, 0, 1, 2006, 2, 31);
		expectedBeginEnds[3] = createExpectedBeginEnd(2006, 3, 1, 2006, 5, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2006, 3, 1, 2006, 5, 30);
		expectedBeginEnds[5] = createExpectedBeginEnd(2006, 3, 1, 2006, 5, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2006, 6, 1, 2006, 8, 30);
		expectedBeginEnds[7] = createExpectedBeginEnd(2006, 6, 1, 2006, 8, 30);
		expectedBeginEnds[8] = createExpectedBeginEnd(2006, 6, 1, 2006, 8, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2006, 9, 1, 2006, 11, 31);
		expectedBeginEnds[10] = createExpectedBeginEnd(2006, 9, 1, 2006, 11, 31);
		expectedBeginEnds[11] = createExpectedBeginEnd(2006, 9, 1, 2006, 11, 31);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2006, 3, 1, 2006, 5, 30);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2006, 3, 1, 2006, 5, 30);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2006, 3, 1, 2006, 5, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2006, 6, 1, 2006, 8, 30);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2006, 6, 1, 2006, 8, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2006, 6, 1, 2006, 8, 30);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2006, 9, 1, 2006, 11, 31);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2006, 9, 1, 2006, 11, 31);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2006, 9, 1, 2006, 11, 31);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2007, 0, 1, 2007, 2, 31);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2007, 0, 1, 2007, 2, 31);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2007, 0, 1, 2007, 2, 31);

		checkYear(createMonthLine(2006, 0, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 3);
		checkYear(createMonthLine(2006, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 3);
		checkYear(createMonthLineEnd(2006, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 1, 3);

		expectedBeginEnds[0] = createExpectedBeginEnd(2006, 0, 15, 2006, 3, 14);
		expectedBeginEnds[1] = createExpectedBeginEnd(2006, 0, 15, 2006, 3, 14);
		expectedBeginEnds[2] = createExpectedBeginEnd(2006, 0, 15, 2006, 3, 14);
		expectedBeginEnds[3] = createExpectedBeginEnd(2006, 3, 15, 2006, 6, 14);
		expectedBeginEnds[4] = createExpectedBeginEnd(2006, 3, 15, 2006, 6, 14);
		expectedBeginEnds[5] = createExpectedBeginEnd(2006, 3, 15, 2006, 6, 14);
		expectedBeginEnds[6] = createExpectedBeginEnd(2006, 6, 15, 2006, 9, 14);
		expectedBeginEnds[7] = createExpectedBeginEnd(2006, 6, 15, 2006, 9, 14);
		expectedBeginEnds[8] = createExpectedBeginEnd(2006, 6, 15, 2006, 9, 14);
		expectedBeginEnds[9] = createExpectedBeginEnd(2006, 9, 15, 2007, 0, 14);
		expectedBeginEnds[10] = createExpectedBeginEnd(2006, 9, 15, 2007, 0, 14);
		expectedBeginEnds[11] = createExpectedBeginEnd(2006, 9, 15, 2007, 0, 14);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2006, 3, 15, 2006, 6, 14);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2006, 3, 15, 2006, 6, 14);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2006, 3, 15, 2006, 6, 14);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2006, 6, 15, 2006, 9, 14);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2006, 6, 15, 2006, 9, 14);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2006, 6, 15, 2006, 9, 14);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2006, 9, 15, 2007, 0, 14);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2006, 9, 15, 2007, 0, 14);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2006, 9, 15, 2007, 0, 14);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2007, 0, 15, 2007, 3, 14);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2007, 0, 15, 2007, 3, 14);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2007, 0, 15, 2007, 3, 14);

		checkYear(createMonthLine(2006, 0, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);
		checkYear(createMonthLine(2006, 0, 21), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);
		checkYear(createMonthLineEnd(2006, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);
		checkYear(createMonthLine(2006, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);
		checkYear(createMonthLine(2006, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 15, 3);

		expectedBeginEnds[0] = createExpectedBeginEnd(2006, 0, 31, 2006, 3, 29);
		expectedBeginEnds[1] = createExpectedBeginEnd(2006, 0, 31, 2006, 3, 29);
		expectedBeginEnds[2] = createExpectedBeginEnd(2006, 0, 31, 2006, 3, 29);
		expectedBeginEnds[3] = createExpectedBeginEnd(2006, 3, 30, 2006, 6, 30);
		expectedBeginEnds[4] = createExpectedBeginEnd(2006, 3, 30, 2006, 6, 30);
		expectedBeginEnds[5] = createExpectedBeginEnd(2006, 3, 30, 2006, 6, 30);
		expectedBeginEnds[6] = createExpectedBeginEnd(2006, 6, 31, 2006, 9, 30);
		expectedBeginEnds[7] = createExpectedBeginEnd(2006, 6, 31, 2006, 9, 30);
		expectedBeginEnds[8] = createExpectedBeginEnd(2006, 6, 31, 2006, 9, 30);
		expectedBeginEnds[9] = createExpectedBeginEnd(2006, 9, 31, 2007, 0, 30);
		expectedBeginEnds[10] = createExpectedBeginEnd(2006, 9, 31, 2007, 0, 30);
		expectedBeginEnds[11] = createExpectedBeginEnd(2006, 9, 31, 2007, 0, 30);

		expectedNextBeginEnds[0] = createExpectedBeginEnd(2006, 3, 30, 2006, 6, 30);
		expectedNextBeginEnds[1] = createExpectedBeginEnd(2006, 3, 30, 2006, 6, 30);
		expectedNextBeginEnds[2] = createExpectedBeginEnd(2006, 3, 30, 2006, 6, 30);
		expectedNextBeginEnds[3] = createExpectedBeginEnd(2006, 6, 31, 2006, 9, 30);
		expectedNextBeginEnds[4] = createExpectedBeginEnd(2006, 6, 31, 2006, 9, 30);
		expectedNextBeginEnds[5] = createExpectedBeginEnd(2006, 6, 31, 2006, 9, 30);
		expectedNextBeginEnds[6] = createExpectedBeginEnd(2006, 9, 31, 2007, 0, 30);
		expectedNextBeginEnds[7] = createExpectedBeginEnd(2006, 9, 31, 2007, 0, 30);
		expectedNextBeginEnds[8] = createExpectedBeginEnd(2006, 9, 31, 2007, 0, 30);
		expectedNextBeginEnds[9] = createExpectedBeginEnd(2007, 0, 31, 2007, 3, 29);
		expectedNextBeginEnds[10] = createExpectedBeginEnd(2007, 0, 31, 2007, 3, 29);
		expectedNextBeginEnds[11] = createExpectedBeginEnd(2007, 0, 31, 2007, 3, 29);

		checkYear(createMonthLineEnd(2006, 0, 31), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);
		checkYear(createMonthLine(2006, 1, 1), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);
		checkYear(createMonthLine(2006, 1, 11), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);
		checkYear(createMonthLine(2006, 1, 15), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);
		checkYear(createMonthLine(2006, 1, 28), expectedBeginEnds, expectedNextBeginEnds, 0, 31, 3);

	}

	private Date[] createMonthLine(int beginYear, int beginMonth, int beginDay) {
		Date date = DateUtil.createDate(beginYear, beginMonth, beginDay);

		Date[] dates = new Date[12];
		dates[0] = date;
		for (int i = 1; i < 12; i++) {
			dates[i] = DateUtil.addMonths(date, +i);
		}
		return dates;
	}

	private Date[] createMonthLineEnd(int beginYear, int beginMonth, int beginDay) {
		Calendar cal = CalendarUtil.createCalendar();
		cal.set(Calendar.YEAR, beginYear);
		cal.set(Calendar.MONTH, beginMonth);
		cal.set(Calendar.DAY_OF_MONTH, Math.min(beginDay, cal.getActualMaximum(Calendar.DAY_OF_MONTH)));

		Date[] dates = new Date[12];
		dates[0] = cal.getTime();
		for (int i = 1; i < 12; i++) {
			cal.add(Calendar.MONTH, +1);
			cal.set(Calendar.DAY_OF_MONTH, Math.min(beginDay, cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
			dates[i] = cal.getTime();
		}
		return dates;

	}

	private Date[] createExpectedBeginEnd(int beginYear, int beginMonth, int beginDay, int endYear, int endMonth,
			int endDay) {
		Date[] beginEnd = new Date[2];
		beginEnd[0] = DateUtil.adjustToDayBegin(DateUtil.createDate(beginYear, beginMonth, beginDay));
		beginEnd[1] = DateUtil.adjustToDayEnd(DateUtil.createDate(endYear, endMonth, endDay));
		return beginEnd;
	}

	private void checkYear(Date[] timestamps, Date[][] expectedBegins, Date[][] expectedNextBegins, int beginMonth,
			int beginDay, int intervalLength)
			throws Exception {

		for (int i = 0; i < 12; i++) {
			Date currentDate = timestamps[i];
			Date[] beginEnd = expectedBegins[i];

			Calendar cal = CalendarUtil.createCalendar(currentDate);
			MonthlyTimePeriod hy = new MonthlyTimePeriod(cal, beginMonth, beginDay, intervalLength);
			assertEquals("Begin for: " + currentDate, beginEnd[0], hy.getBegin());
			assertEquals("End for: " + currentDate, beginEnd[1], hy.getEnd());

			TimePeriod next = hy.getNextPeriod();
			Date[] beginEndNext = expectedNextBegins[i];
			assertEquals("Next begin for: " + currentDate, beginEndNext[0], next.getBegin());
			assertEquals("Next end for: " + currentDate, beginEndNext[1], next.getEnd());
		}
	}

	/**
	 * the suite of Tests to execute
	 */
    public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestMonthlyTimePeriod.class);
        // return new TestTimeRange("testCrossOver");
    }

    /** Main function for direct execution. */
    public static void main(String[] args) {
        SHOW_TIME=true;
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

}
