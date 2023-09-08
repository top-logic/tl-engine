/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.common.format;

import java.text.DateFormat;
import java.util.Date;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.reporting.common.format.ReportingDateFormat;

/**
 * Test for {@link ReportingDateFormat}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestReportingDateFormat extends BasicTestCase {

	public void testClone() {
		DateFormat halfYearFormat = ReportingDateFormat.getHalfYearFormat();
		DateFormat clone = (DateFormat) halfYearFormat.clone();
		Date date = new Date();
		assertEquals(halfYearFormat.format(date), clone.format(date));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestReportingDateFormat}.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestReportingDateFormat.class);
	}

}
