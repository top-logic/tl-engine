/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Calendar;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.MapConverter;
import com.top_logic.basic.col.ParseBooleanMapping;
import com.top_logic.basic.col.ParseDateMapping;
import com.top_logic.basic.col.ParseFloatMapping;
import com.top_logic.basic.col.ParseIntMapping;
import com.top_logic.basic.col.ParseLongMapping;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Test case for {@link MapConverter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestMapConverter extends BasicTestCase {
	
	public void testConversion() {
		
		MapConverter converter = new MapConverter(
			new MapBuilder()
				.put("a", ParseIntMapping.INSTANCE)
				.put("b", ParseFloatMapping.INSTANCE)
				.put("c", ParseBooleanMapping.INSTANCE)
				.put("d", new ParseDateMapping(CalendarUtil.newSimpleDateFormat("yyyy-MM-dd")))
				.put("e", ParseLongMapping.INSTANCE)
				.toMap());
		
		Map result = converter.convert(
			new MapBuilder()
				.put("a", "42")
				.put("b", "42.13")
				.put("c", "true")
				.put("d", "2008-07-28")
				.put("e", Long.toString(Long.MAX_VALUE))
				.toMap());
		
		Calendar calendar = CalendarUtil.createCalendar();
		calendar.set(2008, Calendar.JULY, 28, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Map expectedResult = new MapBuilder()
			.put("a", Integer.valueOf(42))
			.put("b", Float.valueOf(42.13f))
			.put("c", Boolean.TRUE)
			.put("d", calendar.getTime())
			.put("e", Long.valueOf(Long.MAX_VALUE))
			.toMap();

		// In case some test fails, it is more easy to catch the problem, if
		// comparison is done explicitly.
		assertEquals(expectedResult.get("a"), result.get("a"));
		assertEquals(expectedResult.get("b"), result.get("b"));
		assertEquals(expectedResult.get("c"), result.get("c"));
		assertEquals(expectedResult.get("d"), result.get("d"));
		assertEquals(expectedResult.get("e"), result.get("e"));
		
		assertEquals(expectedResult, result);
		
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestMapConverter.class));
	}
	

}
