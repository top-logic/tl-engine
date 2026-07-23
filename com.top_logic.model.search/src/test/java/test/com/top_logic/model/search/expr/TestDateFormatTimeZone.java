/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.Test;

import com.top_logic.basic.thread.ThreadContext;

/**
 * Tests for the optional time-zone argument of {@code dateFormat()} and the {@code timeZone()}
 * calendar accessor.
 *
 * @see com.top_logic.model.search.expr.DateFormatExpr
 * @see com.top_logic.model.search.expr.CalendarTimeZone
 */
@SuppressWarnings("javadoc")
public class TestDateFormatTimeZone extends AbstractSearchExpressionTest {

	/**
	 * The instant 2025-11-30T20:00:00 UTC. It falls on 2025-11-30 in UTC but already on 2025-12-01
	 * in {@code Asia/Tokyo} (UTC+9), so the rendered day depends on the format's time zone.
	 */
	private static Date utcInstant() {
		Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		utc.clear();
		utc.set(2025, Calendar.NOVEMBER, 30, 20, 0, 0);
		return utc.getTime();
	}

	public void testExplicitTimeZoneShiftsRenderedDay() throws Exception {
		Date d = utcInstant();

		assertEquals("2025-12-01", eval("d -> dateFormat('yyyy-MM-dd', 'Asia/Tokyo').format($d)", d));
		assertEquals("2025-11-30", eval("d -> dateFormat('yyyy-MM-dd', 'UTC').format($d)", d));
	}

	public void testNoTimeZoneUsesJvmDefault() throws Exception {
		Date d = utcInstant();

		Object explicitDefault =
			eval("d -> zone -> dateFormat('yyyy-MM-dd HH:mm', $zone).format($d)", d, TimeZone.getDefault().getID());
		Object implicitDefault = eval("d -> dateFormat('yyyy-MM-dd HH:mm').format($d)", d);

		assertEquals(implicitDefault, explicitDefault);
	}

	public void testCalendarTimeZoneAccessorFormatsCalendarDay() throws Exception {
		// A system calendar at midnight of 2025-12-01 in the system time zone must render as
		// 2025-12-01 when formatted in its own time zone, regardless of the JVM default time zone.
		Object result = eval("{ cal = date(2025, 11, 1).toSystemCalendar(); "
			+ "dateFormat('yyyy-MM-dd', $cal.timeZone()).format($cal.toDate()) }");

		assertEquals("2025-12-01", result);
	}

	public void testUserZoneId() throws Exception {
		Date d = utcInstant();
		String userZoneId = ThreadContext.getTimeZone().getID();

		Object viaUser = eval("d -> dateFormat('yyyy-MM-dd HH:mm', 'user').format($d)", d);
		Object viaId = eval("d -> zone -> dateFormat('yyyy-MM-dd HH:mm', $zone).format($d)", d, userZoneId);

		assertEquals(viaId, viaUser);
	}

	public void testSystemZoneId() throws Exception {
		Object viaId = eval("{ cal = date(2025, 11, 1).toSystemCalendar(); "
			+ "dateFormat('yyyy-MM-dd', 'system').format($cal.toDate()) }");
		Object viaAccessor = eval("{ cal = date(2025, 11, 1).toSystemCalendar(); "
			+ "dateFormat('yyyy-MM-dd', $cal.timeZone()).format($cal.toDate()) }");

		assertEquals(viaAccessor, viaId);
	}

	public static Test suite() {
		return suite(TestDateFormatTimeZone.class);
	}

}
