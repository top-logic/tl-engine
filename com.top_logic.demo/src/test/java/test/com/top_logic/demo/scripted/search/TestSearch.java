/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.search;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test class for the search GUI.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestSearch {

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestSearch}.
	 */
	public static Test suite() {
		String[] testCases = {
			"00_ChangeSearchScope",
			"01_ResetSearchParameters",
			"02_SearchComplexAttribute",
			"03_ConfiguredSearchColumns",
		};
		TestSuite testSuite = XmlScriptedTestUtil.suite(TestSearch.class, testCases);
		return DemoSetup.createDemoSetup(testSuite);
	}

}

