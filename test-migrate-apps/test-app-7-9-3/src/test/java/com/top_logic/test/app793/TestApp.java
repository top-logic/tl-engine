/*
 * Copyright (c) 2021 My Company. All Rights Reserved
 */
package com.top_logic.test.app793;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.AppMigrationTestSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test case for the app <code>Test App 7.9.3</code>. 
 */
public class TestApp extends TestCase {
	
	/**
	 * Test suite creation.
	 */
	public static Test suite() {
		String[] testCases = {
			"TestUI",
		};
		return AppMigrationTestSetup.suite(XmlScriptedTestUtil.suite(TestApp.class, testCases));
	}

}