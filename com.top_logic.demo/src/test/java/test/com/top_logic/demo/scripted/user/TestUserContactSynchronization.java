/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.user;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test suite, which holds tests for synchronization of user and contact.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestUserContactSynchronization {

	public static Test suite() {
		TestSuite testSuite =
			XmlScriptedTestUtil.suite(TestUserContactSynchronization.class,
				"1_newContactFromPerson",
				"2_newPersonFromContact", // needs data created in 1_newContactFromPerson
				"3_longEmailAddress",
				"4_userCreation"
			);
		return DemoSetup.createDemoSetup(testSuite);
	}

}
