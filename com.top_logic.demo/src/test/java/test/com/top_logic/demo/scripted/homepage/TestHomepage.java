/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.homepage;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Tests for the homepage function in TL-Demo
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestHomepage {

	/**
	 * Creates the main {@link Test} for this class.
	 */
	public static Test suite() {
		String[] testCases = {
			"00_SetMaintenanceHomepage",
			"01_CheckMaintenanceHomepage",
			"04_SetMonitorHomepage",
			"05_CheckMonitorHomepage",
			"06_SetSearchHomepage",
			"07_CheckSearchHomepage",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestHomepage.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}
