/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.plain;

import junit.framework.Test;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test from creating plain type objects.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCreatePlainTypes {

	private static final String[] TESTS = {
		"testCreateBandC"
	};

	public static Test suite() {
		return DemoSetup.createDemoSetup(XmlScriptedTestUtil.suite(TestCreatePlainTypes.class, TESTS));
	}
	
	
}
