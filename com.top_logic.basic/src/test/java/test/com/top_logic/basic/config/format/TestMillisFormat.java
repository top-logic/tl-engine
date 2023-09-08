/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.format;

import static com.top_logic.basic.DateUtil.*;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.format.MillisFormat;

/**
 * Test case for {@link MillisFormat}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMillisFormat extends TestCase {

	public void testParse() throws ConfigurationException {
		assertEquals(Long.valueOf(10), MillisFormat.INSTANCE.getValue(null, "10"));
		assertEquals(Long.valueOf(10), MillisFormat.INSTANCE.getValue(null, "10ms"));
		assertEquals(Long.valueOf(10 * HOUR_MILLIS), MillisFormat.INSTANCE.getValue(null, "10h"));
		assertEquals(Long.valueOf(10), MillisFormat.INSTANCE.getValue(null, "10  ms"));
		assertEquals(Long.valueOf(10), MillisFormat.INSTANCE.getValue(null, "  10  ms  "));
		assertEquals(Long.valueOf(2 * DAY_MILLIS + 3 * HOUR_MILLIS + 4 * MINUTE_MILLIS + 5 * SECOND_MILLIS + 6),
			MillisFormat.INSTANCE.getValue(null, "2d 3h 4min 5s 6ms"));
		assertEquals(Long.valueOf(2 * DAY_MILLIS + 3 * HOUR_MILLIS + 4 * MINUTE_MILLIS + 5 * SECOND_MILLIS + 6),
			MillisFormat.INSTANCE.getValue(null, "2d3h4min5s6ms"));
		assertEquals(Long.valueOf(SECOND_MILLIS + 500),
			MillisFormat.INSTANCE.getValue(null, "1,5s"));
		assertEquals(Long.valueOf(SECOND_MILLIS + 500),
			MillisFormat.INSTANCE.getValue(null, "1.5s"));
		assertEquals(Long.valueOf(SECOND_MILLIS + 500),
			MillisFormat.INSTANCE.getValue(null, "1s 500ms"));
		assertEquals(Long.valueOf(SECOND_MILLIS + 500),
			MillisFormat.INSTANCE.getValue(null, "500ms 1s"));
		assertEquals(Long.valueOf(2 * HOUR_MILLIS + 30 * MINUTE_MILLIS),
			MillisFormat.INSTANCE.getValue(null, "2,5h"));
		assertEquals(Long.valueOf(2 * HOUR_MILLIS + 18 * MINUTE_MILLIS),
			MillisFormat.INSTANCE.getValue(null, "2,3h"));
		assertEquals(Long.valueOf(15), MillisFormat.INSTANCE.getValue(null, "10ms 5ms"));
	}

	public void testFormat() {
		assertEquals("10ms", MillisFormat.INSTANCE.getSpecification(10L));
		assertEquals("10h", MillisFormat.INSTANCE.getSpecification(10 * HOUR_MILLIS));
		assertEquals("1s 500ms", MillisFormat.INSTANCE.getSpecification(SECOND_MILLIS + 500));
		assertEquals("2d 3h 4min 5s 6ms",
			MillisFormat.INSTANCE
				.getSpecification(2 * DAY_MILLIS + 3 * HOUR_MILLIS + 4 * MINUTE_MILLIS + 5 * SECOND_MILLIS + 6));
	}

}
