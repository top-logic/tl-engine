/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.user.passwordchange;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test suite, which holds tests for user password change operation.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestPasswordChange {

	public static Test suite() {
		TestSuite testSuite =
			XmlScriptedTestUtil.suite(TestPasswordChange.class,
				"1_ChangePassword",
				"2_InvalidOldPassword",
				"3_InvalidNewPasswordConfirmation");
		return DemoSetup.createDemoSetup(testSuite);
	}

}
