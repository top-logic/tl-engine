/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.ScriptedTest;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * {@link ScriptedTest} for <i>TopLogic</i>.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class TestTL {

	public static Test suite() {
		String[] testCases = {
			"01_ProgressComponent"
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestTL.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}
