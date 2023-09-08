/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import junit.framework.TestCase;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;

/**
 * Test case for {@link DisplayDimension}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDisplayDimension extends TestCase {

	public void testParse() {
		checkParse(0, DisplayUnit.PIXEL, "0");
		checkParse(0, DisplayUnit.PIXEL, "0px");
		checkParse(0, DisplayUnit.PERCENT, "0%");

		checkParse(100, DisplayUnit.PIXEL, "100");
		checkParse(100, DisplayUnit.PIXEL, "100px");
		checkParse(100, DisplayUnit.PERCENT, "100%");

		checkParse(50, DisplayUnit.PIXEL, "50");
		checkParse(50, DisplayUnit.PIXEL, "50px");
		checkParse(50, DisplayUnit.PERCENT, "50%");

		checkParse(13.42f, DisplayUnit.PIXEL, "13.42");
		checkParse(13.42f, DisplayUnit.PIXEL, "13.42px");
		checkParse(13.42f, DisplayUnit.PERCENT, "13.42%");
	}

	private void checkParse(float value, DisplayUnit unit, String spec) {
		assertEquals(DisplayDimension.dim(value, unit), DisplayDimension.parseDimension(spec));
	}

	public void testToString() {
		checkToString("0px", 0, DisplayUnit.PIXEL);
		checkToString("0%", 0, DisplayUnit.PERCENT);

		checkToString("100px", 100, DisplayUnit.PIXEL);
		checkToString("100%", 100, DisplayUnit.PERCENT);

		checkToString("50px", 50, DisplayUnit.PIXEL);
		checkToString("50%", 50, DisplayUnit.PERCENT);

		checkToString("77.7px", 77.7f, DisplayUnit.PIXEL);
		checkToString("77.7%", 77.7f, DisplayUnit.PERCENT);
	}

	private void checkToString(String spec, float value, DisplayUnit unit) {
		assertEquals(spec, DisplayDimension.dim(value, unit).toString());
	}

}
