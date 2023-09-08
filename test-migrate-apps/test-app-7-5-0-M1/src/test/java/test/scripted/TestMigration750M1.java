/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.scripted;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.AppMigrationTestSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Call application test booting the database with the baseline data.
 */
public class TestMigration750M1 extends TestCase {

	/**
	 * Test suite creation.
	 */
	public static Test suite() {
		String[] testCases = {
			"TestUI",
		};
		return AppMigrationTestSetup.suite(XmlScriptedTestUtil.suite(TestMigration750M1.class, testCases));
	}

}
