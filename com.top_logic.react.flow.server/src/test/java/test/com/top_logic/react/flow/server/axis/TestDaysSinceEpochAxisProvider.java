/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.axis;

import java.time.LocalDate;

import junit.framework.TestCase;

import com.top_logic.react.flow.server.axis.AxisContent;
import com.top_logic.react.flow.server.axis.providers.DaysSinceEpochAxisProvider;

/** Tests for {@link DaysSinceEpochAxisProvider}. */
public class TestDaysSinceEpochAxisProvider extends TestCase {

	public void testBuildAxisProducesYearAndMonthRows() {
		double from = LocalDate.of(2026, 1, 1).toEpochDay();
		double to = LocalDate.of(2026, 12, 31).toEpochDay();
		AxisContent content = new DaysSinceEpochAxisProvider().buildAxis(from, to, 1.0);

		assertEquals("two axis rows", 2, content.rows().size());
		// Items: 1 year span + 12 month points = 13
		assertTrue("at least 13 items", content.items().size() >= 13);
	}

	public void testGetIdReturnsDefault() {
		assertEquals("days-since-epoch", new DaysSinceEpochAxisProvider().getId());
	}

	public void testGetIdReturnsCustomId() {
		assertEquals("custom", new DaysSinceEpochAxisProvider("custom").getId());
	}
}
