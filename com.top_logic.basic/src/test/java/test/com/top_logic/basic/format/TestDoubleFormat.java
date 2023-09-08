/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.format;

import java.math.RoundingMode;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import junit.framework.TestCase;

import com.top_logic.basic.format.DoubleFormat;

/**
 * Test case for {@link DoubleFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDoubleFormat extends TestCase {

	public void testParse() throws ParseException {
		NumberFormat impl = NumberFormat.getIntegerInstance(Locale.GERMAN);
		impl.setRoundingMode(RoundingMode.HALF_UP);
		Format format = DoubleFormat.newInstance(impl);

		checkParse(format, 42D, "42");
		checkParse(format, 0D, "0,2");
		checkParse(format, 42D, "42,9");
	}

	private void checkParse(Format format, Object expected, String source) throws ParseException {
		Object result = format.parseObject(source);
		assertEquals(expected, result);
	}

}
