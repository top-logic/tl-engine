/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.scripting.tests.variables;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

import com.top_logic.layout.scripting.runtime.GlobalVariableStore;

/**
 * Tests if the features of the {@link GlobalVariableStore} work properly.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestGlobalVariables {

	private static final String[] TESTS = {
		"01_SetGet",
		"02_Exists",
		"03_SetExists",
		"04_SetNullExists",
		"05_SetDelExists",
		"06_SetClearExists",
		"07_SetNullCheckNull",
		"08_SetNonNullCheckNonNull",
		"09_DifferentActionChain"
	};

	/** Creates the {@link Test} suite containing all the actual {@link Test}s of this class. */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestGlobalVariables.class.getSimpleName());
		suite.addTest(XmlScriptedTestUtil.suite(TestGlobalVariables.class, TESTS));
		suite.addTest(buildTestDifferentTests());
		suite.addTest(buildTestDifferentSuites());
		return DemoSetup.createDemoSetup(suite);
	}

	/**
	 * A global variable is set in one {@link Test} but read in another (but in the same
	 * {@link TestSuite}).
	 */
	private static TestSuite buildTestDifferentTests() {
		return XmlScriptedTestUtil.suite(TestGlobalVariables.class,
			"10_DifferentTest_SetVariable",
			"11_DifferentTest_GetVariable");
	}

	/** A global variable is set in one {@link TestSuite} but read in another. */
	private static TestSuite buildTestDifferentSuites() {
		TestSuite differentSuite = new TestSuite("Variable is read in another Suite");
		differentSuite.addTest(XmlScriptedTestUtil.suite(TestGlobalVariables.class, "12_DifferentSuite_SetVariable"));
		differentSuite.addTest(XmlScriptedTestUtil.suite(TestGlobalVariables.class, "13_DifferentSuite_GetVariable"));
		return differentSuite;
	}

}
