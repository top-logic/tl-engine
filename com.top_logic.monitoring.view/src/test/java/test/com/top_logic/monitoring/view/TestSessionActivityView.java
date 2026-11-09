/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.monitoring.view;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.monitor.UserMonitor;
import com.top_logic.model.search.expr.config.SearchBuilder;

/**
 * Evaluates the chart of the view <code>admin/monitoring/session-activity.view.xml</code>.
 */
@SuppressWarnings("javadoc")
public class TestSessionActivityView extends TestCase {

	private static final String VIEW = "admin/monitoring/session-activity.view.xml";

	/** Stand-ins for the period options of the view, in the order of the period names it maps them to. */
	private static final List<String> PERIODS = List.of("hour", "day", "week", "month", "year");

	public void testDaysOfAWeek() throws Exception {
		Calendar calendar = CalendarUtil.createCalendarInUserTimeZone();
		calendar.set(2026, Calendar.MARCH, 2, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date from = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, 7);
		Date to = calendar.getTime();

		// Inputs: periods, period, from, to, refresh.
		Map<?, ?> chart = ViewCharts.evaluate(VIEW, PERIODS, PERIODS.get(1), from, to, null);

		assertEquals("bar", chart.get("type"));
		Map<?, ?> data = (Map<?, ?>) chart.get("data");
		assertEquals(List.of("2026-03-02", "2026-03-03", "2026-03-04", "2026-03-05", "2026-03-06", "2026-03-07",
			"2026-03-08"), data.get("labels"));
		List<?> datasets = (List<?>) data.get("datasets");
		assertEquals(1, datasets.size());
		List<?> counts = (List<?>) ((Map<?, ?>) datasets.get(0)).get("data");
		assertEquals(7, counts.size());
		for (Object count : counts) {
			assertEquals("No session is recorded in the test.", 0, ((Number) count).intValue());
		}
	}

	public void testMonths() throws Exception {
		Calendar calendar = CalendarUtil.createCalendarInUserTimeZone();
		calendar.set(2026, Calendar.JANUARY, 15, 12, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date from = calendar.getTime();
		calendar.set(2026, Calendar.APRIL, 1, 0, 0, 0);
		Date to = calendar.getTime();

		Map<?, ?> chart = ViewCharts.evaluate(VIEW, PERIODS, PERIODS.get(3), from, to, null);

		Map<?, ?> data = (Map<?, ?>) chart.get("data");
		assertEquals(List.of("2026-01", "2026-02", "2026-03"), data.get("labels"));
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSessionActivityView.class),
				TypeIndex.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE,
				UserMonitor.Module.INSTANCE));
	}

}
