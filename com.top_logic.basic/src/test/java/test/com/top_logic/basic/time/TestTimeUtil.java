/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.time;

import static java.time.temporal.ChronoUnit.*;

import java.time.Duration;

import junit.framework.TestCase;

import com.top_logic.basic.time.TimeUtil;

/**
 * {@link TestCase} for {@link TimeUtil}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTimeUtil extends TestCase {

	public void testCreateCalendar() {
		// Trivial examples:
		assertFormat("0ns", Duration.ZERO);
		assertFormat("1ns", NANOS.getDuration());
		assertFormat("1000ns", MICROS.getDuration());
		assertFormat("1ms", MILLIS.getDuration());
		assertFormat("1s", SECONDS.getDuration());
		assertFormat("1min", MINUTES.getDuration());
		assertFormat("1h", HOURS.getDuration());
		assertFormat("1d", DAYS.getDuration());
		assertFormat("7d", WEEKS.getDuration());

		// Simple example:
		assertFormat("123456ns", Duration.ofNanos(123_456));
		assertFormat("123456789ns", Duration.ofNanos(123_456_789));
		assertFormat("123ms", Duration.ofNanos(123_000_000));
		assertFormat("12s", Duration.ofSeconds(12));
		assertFormat("12min", Duration.ofMinutes(12));
		assertFormat("12h", Duration.ofHours(12));
		assertFormat("1234d", Duration.ofDays(1234));

		// Complex example:
		assertFormat("30d 10h 29min 6s", MONTHS.getDuration()); // 365.2425d / 12
		assertFormat("365d 5h 49min 12s", YEARS.getDuration()); // 365.2425d
		assertFormat("1233d 23h 59min 59s 999999999ns", Duration.ofDays(1234).minus(1, NANOS));

		/* Negative durations: */
		assertFormat("-23h", Duration.ofDays(-1).plus(1, HOURS));
		assertFormat("-23h 59min 59s 999999999ns", Duration.ofDays(-1).plus(1, NANOS));
	}

	private void assertFormat(String formatted, Duration duration) {
		assertEquals(formatted, TimeUtil.format(duration));
	}

}
