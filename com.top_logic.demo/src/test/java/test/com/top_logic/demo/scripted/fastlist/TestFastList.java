/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.fastlist;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

import com.top_logic.knowledge.wrap.list.FastList;

/**
 * Application tests to test {@link FastList}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestFastList {

	public static Test suite() {
		String[] testCases = {
			"01_SystemList",
			"02_DeleteUsedList"
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestFastList.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}
