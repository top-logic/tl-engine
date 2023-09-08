/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.stackedgoto;

import junit.framework.Test;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test for stacked gotos.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestGoto {

	public static Test suite() {
		String[] testCases = {
			"Setup",
			"01_ReturnOnClose",
			"02_DropStackOnVisitingSameObject",
			"03_RepeatedClose",
			"04_GotoSelf",
			"Teardown",
		};
		return DemoSetup.createDemoSetup(XmlScriptedTestUtil.suite(TestGoto.class, testCases));
	}

}
