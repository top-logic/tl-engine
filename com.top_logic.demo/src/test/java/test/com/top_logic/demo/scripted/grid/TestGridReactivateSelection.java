/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.grid;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Tests that grid releases token when opening a dialog and reactivates selection if dialog is
 * closed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestGridReactivateSelection {

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestGridReactivateSelection}.
	 */
	public static Test suite() {
		String[] testCases = {
			"Setup",
			"01_TestCloseDetailDialog",
			"02_TestDetailDialogEditsObject",
			"03_TestReleaseReacquireWhenOpeningDialog",
			"04_TestChangeCheckForGridInDialog",
			"05_TestReleaseReacquireWhenGridInDialog",
			"Cleanup",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestGridReactivateSelection.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}
