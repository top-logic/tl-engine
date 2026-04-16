/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.axis;

import java.time.LocalDate;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.GanttTick;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.axis.providers.DaysSinceEpochAxisProvider;

/** Tests for {@link DaysSinceEpochAxisProvider}. */
public class TestDaysSinceEpochAxisProvider extends TestCase {

	public void testMonthTicksInJanuaryCarryYearLabel() {
		double from = LocalDate.of(2026, 1, 1).toEpochDay();
		double to = LocalDate.of(2026, 12, 31).toEpochDay();
		List<GanttTick> ticks = new DaysSinceEpochAxisProvider().ticksFor(from, to, 1.0);

		assertEquals("one tick per month in the year", 12, ticks.size());
		assertEquals("year label in January", "2026", tickLabelValue(ticks.get(0)));
		assertEquals("month abbreviation", "FEB", tickLabelValue(ticks.get(1)));
	}

	/** Returns the text value of a tick's label Box (must be a {@link Text} box). */
	private static String tickLabelValue(GanttTick tick) {
		Box label = tick.getLabel();
		assertNotNull("tick has a label box", label);
		assertTrue("label box is a Text", label instanceof Text);
		return ((Text) label).getValue();
	}

	public void testGetIdReturnsDefault() {
		assertEquals("days-since-epoch", new DaysSinceEpochAxisProvider().getId());
	}

	public void testGetIdReturnsCustomId() {
		assertEquals("custom", new DaysSinceEpochAxisProvider("custom").getId());
	}
}
