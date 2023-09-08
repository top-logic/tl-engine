/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Test case for {@link XmlDateTimeFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestXmlDateTimeFormat extends BasicTestCase {

	public void testNormalizedFormat() throws ParseException {
		doTest("2009-08-10T13:23:07.123Z", "2009-08-10 15:23:07.123 CEST");
	}

	public void testParseExplicitGMT() throws ParseException {
		doTestParse("2009-08-10T13:23:07.123+00:00", "2009-08-10 15:23:07.123 CEST");
	}
	
	public void testParseLessPrecisionThanMillis() throws ParseException {
		doTestParse("2009-08-10T13:23:07.1+00:00", "2009-08-10 15:23:07.100 CEST");
	}
	
	public void testParseMorePrecisionThanMillis() throws ParseException {
		doTestParse("2009-08-10T13:23:07.12345+00:00", "2009-08-10 15:23:07.123 CEST");
	}
	
	public void testParsePreceedingZerosInMillis() throws ParseException {
		doTestParse("2009-08-10T13:23:07.0012345+00:00", "2009-08-10 15:23:07.001 CEST");
	}
	
	public void testParseZoneSpec() throws ParseException {
		doTestParse("2009-08-10T13:23:07+02:00", "2009-08-10 11:23:07.000 GMT");
		doTestParse("2009-07-31T23:23:07-03:00", "2009-08-01 02:23:07.000 GMT");
	}
	
	public void testInvalidZoneSpec() throws ParseException {
		doTestInvalidFormat("2009-07-31T23:23:07+23:00", 20, "Zone hour must not be greater than 14.");
		doTestInvalidFormat("2009-07-31T23:23:07+06:99", 23, "Zone minutes must not be greater than 59.");
		doTestInvalidFormat("2009-07-31T23:23:07+14:03", 23, "Zone minutes must not 0 wheth zonbe hour is 14.");
		// Check maximal time zone difference
		doTestParse("2009-07-31T23:23:07-14:00", "2009-08-01 13:23:07.000 GMT");
		doTestParse("2009-07-31T23:23:07+14:00", "2009-07-31 09:23:07.000 GMT");
	}

	private void doTestInvalidFormat(String string, int errorPos, String errorMessage) {
		ParsePosition pos = new ParsePosition(0);
		Object parsed = XmlDateTimeFormat.INSTANCE.parseObject(string, pos);
		assertNull("Format must return null on invalid input", parsed);
		assertEquals(errorMessage, errorPos, pos.getErrorIndex());
	}

	private void doTest(String xmlDateTime, String dateSpec) {
		Date date = date(dateSpec);
		
		assertEquals(xmlDateTime, XmlDateTimeFormat.INSTANCE.format(date));
		internalTestParse(xmlDateTime, date);
	}

	private void doTestParse(String xmlDateTime, String dateSpec) throws ParseException {
		internalTestParse(xmlDateTime, date(dateSpec));
	}

	private void internalTestParse(String xmlDateTime, Date expectedDate) {
		try {
			Date parsedDate = (Date) XmlDateTimeFormat.INSTANCE.parseObject(xmlDateTime);
			if (parsedDate.getTime() != expectedDate.getTime()) {
				fail("Parsed date does not match, expected: " + expectedDate + ", parsed: " + parsedDate);
			}
		} catch (ParseException e) {
			fail("Parsing '" + xmlDateTime + "' failed at position " + e.getErrorOffset());
		}
	}
	
	public static Date date(String dateSpec) {
		try {
			SimpleDateFormat dateFormat = CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z", Locale.US);
			return dateFormat.parse(dateSpec);
		} catch (ParseException e) {
			throw (AssertionError) new AssertionError("Invalid date spec: " + dateSpec).initCause(e);
		}
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestXmlDateTimeFormat.class));
	}

}
