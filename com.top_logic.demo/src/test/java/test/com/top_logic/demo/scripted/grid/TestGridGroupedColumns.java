/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.grid;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Scripted tests for the grid with grouped columns.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestGridGroupedColumns {

	/**
	 * Creates the result test for this class.
	 */
	public static Test suite() {
		String[] testCases = {
			"01_TestSort",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestGridGroupedColumns.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}
