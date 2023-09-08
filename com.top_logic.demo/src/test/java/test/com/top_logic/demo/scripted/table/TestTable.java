/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.table;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Scripted tests for tables.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestTable {

	@SuppressWarnings("javadoc")
	public static Test suite() {
		String[] testCases = {
			"01_AccessorOnTable",
			"02_MultiSortDialog",
			"03_ExportTable",
			"04_AdditionalSelectColumn",
			"06_RepeatedFiltering",
		};
		TestSuite testSuite = XmlScriptedTestUtil.suite(TestTable.class, testCases);
		return DemoSetup.createDemoSetup(testSuite);
	}

}
