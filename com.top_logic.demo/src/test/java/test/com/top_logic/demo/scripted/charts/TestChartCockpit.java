/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.charts;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test for charts.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestChartCockpit {

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestChartCockpit}.
	 */
	public static Test suite() {
		String[] testCases = {
			"01_booleanPieChart",
			"02_DisappearOnNull",
		};
		TestSuite testSuite = XmlScriptedTestUtil.suite(TestChartCockpit.class, testCases);
		return DemoSetup.createDemoSetup(testSuite);
	}
}

