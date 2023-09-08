/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.scripting.ref;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * Tests for {@link ValueRef}s.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestRefs {

	/** The test suite which is executed by JUnit. */
	public static Test suite() {
		String[] testCases = {
			"SingletonValue",
			"SelectedModel",
			"ObjectAttribute",
			"ObjectAttributeByLabel"
		};
		TestSuite suite = XmlScriptedTestUtil.suite(TestRefs.class, testCases);
		return DemoSetup.createDemoSetup(suite);
	}

}
