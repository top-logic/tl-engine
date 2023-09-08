/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.mail;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test of mails in demo.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestMail {

	public static Test suite() {
		String[] testCases = {
			/* Split up test in different tests, because teardown should also be executed when test
			 * fails. This is needed as the test asserts that there is exactly one mail and teardown
			 * removes it. If teardown is not executed the number of mails will grow. */
			"01_SendReceiveMails_Setup",
			"01_SendReceiveMails_Test",
			"01_SendReceiveMails_Teardown",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestMail.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}
