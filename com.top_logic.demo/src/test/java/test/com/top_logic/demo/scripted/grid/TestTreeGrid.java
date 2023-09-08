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
 * Scripted tests for the tree-structured grids.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTreeGrid {

	public static Test suite() {
		String[] testCases = {
			"01_SelectChildOfEditedRow",
			"02_TestCreateChildOfModifiedContext",
			"03_TestModifyStructureByAttributeEdit",
			"04_TestDeleteModel",
			"04-01_TestTreeGridInvisibleNodeSelection_Setup",
			"04-02_TestTreeGridInvisibleNodeSelection_Test",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestTreeGrid.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}
