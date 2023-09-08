/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.demo;

import junit.framework.Test;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test case that rudimentarily shows automated application tests.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTLDemo {
	
	public static Test suite() {
		return DemoSetup.createDemoSetup(XmlScriptedTestUtil.suite(TestTLDemo.class, "testTabSwitch"));
	}

}
