/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.monitor.busevent;

import junit.framework.Test;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * {@link TestBusEventMonitor} tests the bus event monitor.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestBusEventMonitor {

	@SuppressWarnings("javadoc")
	public static Test suite() {
		String[] testCases = {
			"01_CreateDocument",
		};
		return DemoSetup.createDemoSetup(XmlScriptedTestUtil.suite(TestBusEventMonitor.class, testCases));
	}

}
