/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.util.TestCollector;
import test.com.top_logic.layout.component.TestComponentConfiguration;
import test.com.top_logic.layout.scripting.ApplicationTestSetup;
import test.com.top_logic.layout.scripting.ScriptedTestCollector;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * {@link TestCollector} for tests depending on the "com.top_logic" module.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class CoreTestCollector implements TestCollector {

	@Override
	public void addModuleIndependentTests(TestSuite suite) {
		suite.addTest(TestResourceTemplates.suite());
		suite.addTest(TestComponentConfiguration.suite());
		suite.addTest(TestConsistentMigrationScripts.suite());
	}

	@Override
	public void addTestForDirectory(TestSuite suite, File testDir, boolean recursive) {
		Test scriptedTests = new ScriptedTestCollector(testDir, recursive).get();
		/* Avoid an empty TestSuite, as JUnit complains about them. */
		if (scriptedTests.countTestCases() > 0) {
			suite.addTest(scriptedTests);
		}
	}

	@Override
	public Test getTestsForFile(File targetFile) {
		if (isScriptFile(targetFile)) {
			return ApplicationTestSetup.setupApplication(createScriptedTest(targetFile));
		}
		return null;
	}

	private boolean isScriptFile(File targetFile) {
		return targetFile.getName().endsWith(".xml");
	}

	private Test createScriptedTest(File targetFile) {
		return XmlScriptedTestUtil.createTest(targetFile);
	}

}
