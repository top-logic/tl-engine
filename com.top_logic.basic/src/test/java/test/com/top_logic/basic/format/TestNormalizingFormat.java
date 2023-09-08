/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.format;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.ParseException;
import java.util.Locale;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.format.NormalizingFormat;

/**
 * Test case for {@link NormalizingFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestNormalizingFormat extends TestCase {

	private Format format;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		DecimalFormat impl = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.GERMANY));
		impl.setRoundingMode(RoundingMode.HALF_UP);
		format = NormalizingFormat.newInstance(impl);
	}

	public void testParse() throws ParseException {
		checkParse(format, 42L, "42");
		checkParse(format, 0.2D, "0,2");
		checkParse(format, 0.234D, "0,234");

		checkParse(format, 42L, "42");
		checkParse(format, 0.2D, "0,2");
		checkParse(format, 0.234D, "0,234");
		checkParse(format, 0.234D, "0,2341");
		checkParse(format, 0.234D, "0,2344");
		checkParse(format, 0.234D, "0,234499");

		checkParse(format, 0.017D, "0,0165");

		checkParse(format, 0.235D, "0,234500001");
		checkParse(format, 0.235D, "0,23451");
		checkParse(format, 0.235D, "0,23459");
		checkParse(format, 0.235D, "0,2346");
		checkParse(format, 0.235D, "0,2347");
		checkParse(format, 0.235D, "0,2348");
		checkParse(format, 0.235D, "0,2349");
	}

	public void testNormalizeAtTieWithValueNotExactlyRepresentableAsDouble() throws ParseException {
		// Does no longer work with Java 8 due to a "bugfix" in DecimalFormat.
		// See: http://bugs.java.com/view_bug.do?bug_id=7131459
		//
		try {
			checkParse("Ticket #19414: Incorrect rounding of normalizing format with Java 8.", format, 0.235D, "0,2345");
			fail("Known problem #19414 fixed?");
		} catch (AssertionFailedError ex) {
			BasicTestCase.assertStartsWith(ex.getMessage(), "Ticket #19414");
		}
	}

	private void checkParse(Format format, Object expected, String source) throws ParseException {
		checkParse(null, format, expected, source);
	}

	private void checkParse(String message, Format format, Object expected, String source) throws ParseException {
		Object result = format.parseObject(source);
		assertEquals(message, expected, result);
	}

}
