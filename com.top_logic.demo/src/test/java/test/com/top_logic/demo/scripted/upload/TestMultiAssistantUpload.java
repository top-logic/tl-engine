/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.upload;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Class that tests Ticket 13114
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestMultiAssistantUpload {

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestMultiAssistantUpload}.
	 */
	public static Test suite() {
		TestSuite testSuite = XmlScriptedTestUtil.suite(TestMultiAssistantUpload.class, "ticket_13114");
		return DemoSetup.createDemoSetup(testSuite);
	}
}

