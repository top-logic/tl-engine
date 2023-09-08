/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted;

import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.ScriptedTest.ScriptedTestParameters;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Regression test for Ticket 10161.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestLocalizedNumberFormat {

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestLocalizedNumberFormat}.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestLocalizedNumberFormat.class.getName());
		suite.addTest(newLocalizedTest(Locale.GERMANY));
		suite.addTest(newLocalizedTest(Locale.UK));
		TestSuite theTestSuite = suite;
		return DemoSetup.createDemoSetup(theTestSuite);
	}

	private static Test newLocalizedTest(Locale locale) {
		return newLocalizedTest(locale, locale.toString());
	}

	private static Test newLocalizedTest(Locale locale, String testName) {
		ScriptedTestParameters parameters = new ScriptedTestParameters();
		parameters.setLocale(locale);
		return XmlScriptedTestUtil.createTest(parameters, TestLocalizedNumberFormat.class, testName);
	}

}
