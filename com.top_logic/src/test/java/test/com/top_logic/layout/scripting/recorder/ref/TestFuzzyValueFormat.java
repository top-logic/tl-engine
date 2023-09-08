/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.recorder.ref;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Calendar;
import java.util.Locale;
import java.util.Locale.Category;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.DateUtil;
import com.top_logic.layout.scripting.recorder.ref.FuzzyValueFormat;

/**
 * Test case for {@link FuzzyValueFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestFuzzyValueFormat extends TestCase {

	public void testParseString() {
		assertParse("Hello world!", "Hello world!");
	}

	public void testParseStringStartingWithNumber() {
		assertParse("123 Hello world!", "123 Hello world!");
	}

	public void testParseQuotedString() {
		assertSymmetric("Hello world!", "\"Hello world!\"");
	}

	public void testParseInt() {
		assertSymmetric(42, "42");
	}

	public void testParseQuotedInt() {
		assertSymmetric("42", "\"42\"");
	}

	public void testParseNegativeInt() {
		assertSymmetric(-42, "-42");
	}

	public void testParseDouble() {
		assertSymmetric(42.13D, "42,13");
	}

	public void testParseNegativeDouble() {
		assertSymmetric(-42.13D, "-42,13");
	}

	public void testParseIntegerDouble() {
		assertSymmetric(-42.0D, "-42,0");
	}

	public void testParseWahr() {
		assertSymmetric(true, "wahr");
	}

	public void testParseFalsch() {
		assertSymmetric(false, "falsch");
	}

	public void testParseTrue() {
		assertParse(true, "true");
	}

	public void testParseFalse() {
		assertParse(false, "false");
	}

	public void testParseYes() {
		assertParse(true, "yes");
	}

	public void testParseNo() {
		assertParse(false, "no");
	}

	public void testParseJa() {
		assertParse(true, "ja");
	}

	public void testParseNein() {
		assertParse(false, "nein");
	}

	public void testParseDateTime() {
		assertSymmetric(DateUtil.createDate(2013, Calendar.AUGUST, 6), "06.08.2013 00:00");
	}

	public void testParseDate() {
		assertParse(DateUtil.createDate(2013, Calendar.AUGUST, 6), "06.08.2013");
	}

	public void testParseRelativeYearDate() {
		assertParse(DateUtil.createDate(2014, Calendar.AUGUST, 6),
			"06.08.2013 +1 year");
	}

	public void testParseRelativeDateAll() {
		assertParse(DateUtil.createDate(2014, Calendar.SEPTEMBER, 7, 1, 1, 1),
			"06.08.2013 +1 year +1 month +1 day +1 hour +1 minute +1 second");
	}

	public void testParseRelativeDateAllShort() {
		assertParse(DateUtil.createDate(2014, Calendar.SEPTEMBER, 7, 1, 1, 1),
			"06.08.2013 +1a +1m +1d +1h +1min +1s");
	}

	public void testParseRelativeDateAllShortNeg() {
		assertParse(DateUtil.createDate(2012, Calendar.JULY, 4, 22, 58, 59),
			"06.08.2013 -1a -1m -1d -1h -1min -1s");
	}

	public void testParseList() {
		assertSymmetric(list(1, 2, 3), "1; 2; 3");
	}

	public void testParseListOfStrings() {
		assertParse(list("foo", "2 bar", "3m", "4"), "foo; 2 bar; 3m; \"4\"");
	}

	public void testParseNull() {
		assertSymmetric(null, "");
	}

	protected void assertSymmetric(Object expected, String value) {
		assertParse(expected, value);
		assertEquals(value, FuzzyValueFormat.toLabel(expected));
	}

	protected void assertParse(Object expected, String value) {
		Object parsedValue = FuzzyValueFormat.resolveLabeledValue(null, null, null, null, value);
		assertEquals(expected, parsedValue);
	}

	private static class ChangeLocaleSetup extends TestSetup {

		private Locale _origLocale;

		private Locale _newLocale;

		public ChangeLocaleSetup(Test test, Locale newLocale) {
			super(test);
			_newLocale = newLocale;
		}

		@Override
		protected void setUp() throws Exception {
			super.setUp();
			_origLocale = Locale.getDefault(Category.FORMAT);
			Locale.setDefault(Category.FORMAT, _newLocale);
		}

		@Override
		protected void tearDown() throws Exception {
			Locale.setDefault(Category.FORMAT, _origLocale);
			if (_origLocale != Locale.getDefault(Category.FORMAT)) {
				throw new Error("Unable to reset locale to " + _origLocale);
			}
			super.tearDown();
		}

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestFuzzyValueFormat}.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("TestFuzzyValueFormat");
		suite.addTest(new TestSuite(TestFuzzyValueFormat.class));
		suite.addTest(new ChangeLocaleSetup(new TestSuite(TestFuzzyValueFormat.class), Locale.US));
		return suite;
	}

}
