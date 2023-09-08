/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.comparison;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Scripted test for value comparison with old value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestComparison {

	public static Test suite() {
		String[] testCases = {
			"00_CompareCurrentWithOld",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestComparison.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}
