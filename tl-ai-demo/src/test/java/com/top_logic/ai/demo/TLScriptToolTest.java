/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.tl.ai.core.tools.script.TLScriptTool;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link TLScriptTool}.
 */
public class TLScriptToolTest extends BasicTestCase {

	/**
	 * Tests that validateTLScript() validates various valid expressions.
	 */
	public void testValidateValidScript() {
		TLScriptTool tool = new TLScriptTool();

		// Test arithmetic
		String result1 = tool.validateTLScript("1 + 1");
		assertNotNull("Result should not be null", result1);
		assertTrue("Result should be valid", result1.contains("\"valid\":true") || result1.contains("\"valid\": true"));

		// Test list operations
		String result2 = tool.validateTLScript("[1, 2, 3].size()");
		assertNotNull("Result should not be null", result2);
		assertTrue("Result should be valid", result2.contains("\"valid\":true") || result2.contains("\"valid\": true"));

		// Test function call
		String result3 = tool.validateTLScript("size([1, 2, 3])");
		assertNotNull("Result should not be null", result3);
		assertTrue("Result should be valid", result3.contains("\"valid\":true") || result3.contains("\"valid\": true"));

		// Test let binding
		String result4 = tool.validateTLScript("{x = 10; $x + 5}");
		assertNotNull("Result should not be null", result4);
		assertTrue("Result should be valid", result4.contains("\"valid\":true") || result4.contains("\"valid\": true"));

		// Test filter
		String result5 = tool.validateTLScript("[1, 2, 3].filter(x -> $x > 1)");
		assertNotNull("Result should not be null", result5);
		assertTrue("Result should be valid", result5.contains("\"valid\":true") || result5.contains("\"valid\": true"));
	}

	/**
	 * Tests that validateTLScript() returns error for invalid syntax.
	 */
	public void testValidateInvalidSyntax() {
		TLScriptTool tool = new TLScriptTool();

		// Incomplete expression
		String result = tool.validateTLScript("1 +");
		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\"") || result.contains("\"valid\":false"));
	}

	/**
	 * Tests that validateTLScript() returns error for null script.
	 */
	public void testValidateNullScript() {
		TLScriptTool tool = new TLScriptTool();

		// Null input
		String result1 = tool.validateTLScript(null);
		assertNotNull("Result should not be null", result1);
		assertTrue("Result should contain error", result1.contains("\"error\""));

		// Empty input
		String result2 = tool.validateTLScript("");
		assertNotNull("Result should not be null", result2);
		assertTrue("Result should contain error", result2.contains("\"error\""));

		// Whitespace only
		String result3 = tool.validateTLScript("   ");
		assertNotNull("Result should not be null", result3);
		assertTrue("Result should contain error", result3.contains("\"error\""));
	}

	/**
	 * Tests that executeTLScript() executes and returns simple results.
	 */
	public void testExecuteSimpleScript() {
		TLScriptTool tool = new TLScriptTool();

		// Test number result
		String result1 = tool.executeTLScript("42", null, null);
		assertNotNull("Result should not be null", result1);
		assertTrue("Result should contain success", result1.contains("\"success\":true") || result1.contains("\"success\": true"));
		assertTrue("Result should contain executed flag",
			result1.contains("\"executed\":true") || result1.contains("\"executed\": true"));
		assertTrue("Result should contain number value", result1.contains("42"));

		// Test string result
		String result2 = tool.executeTLScript("\"hello\"", null, null);
		assertNotNull("Result should not be null", result2);
		assertTrue("Result should contain success", result2.contains("\"success\":true") || result2.contains("\"success\": true"));
		assertTrue("Result should contain string value", result2.contains("hello"));

		// Test boolean result
		String result3 = tool.executeTLScript("true", null, null);
		assertNotNull("Result should not be null", result3);
		assertTrue("Result should contain success", result3.contains("\"success\":true") || result3.contains("\"success\": true"));
		assertTrue("Result should contain boolean value", result3.contains("true"));

		// Test list result
		String result4 = tool.executeTLScript("[1, 2, 3]", null, null);
		assertNotNull("Result should not be null", result4);
		assertTrue("Result should contain success", result4.contains("\"success\":true") || result4.contains("\"success\": true"));
		assertTrue("Result should contain collection type",
			result4.contains("\"resultType\":\"collection\"") || result4.contains("\"resultType\": \"collection\""));
	}

	/**
	 * Tests that executeTLScript() executes complex scripts.
	 */
	public void testExecuteComplexScript() {
		TLScriptTool tool = new TLScriptTool();

		// Test let binding
		String result1 = tool.executeTLScript("{x = 10; $x + 5}", null, null);
		assertNotNull("Result should not be null", result1);
		assertTrue("Result should contain success", result1.contains("\"success\":true") || result1.contains("\"success\": true"));
		assertTrue("Result should contain computed value", result1.contains("15"));

		// Test filter
		String result2 = tool.executeTLScript("[1, 2, 3].filter(x -> $x > 1)", null, null);
		assertNotNull("Result should not be null", result2);
		assertTrue("Result should contain success", result2.contains("\"success\":true") || result2.contains("\"success\": true"));

		// Test toString function
		String result3 = tool.executeTLScript("toString(\"a\", \"b\", \"c\")", null, null);
		assertNotNull("Result should not be null", result3);
		assertTrue("Result should contain success", result3.contains("\"success\":true") || result3.contains("\"success\": true"));
		assertTrue("Result should contain string", result3.contains("abc"));

		// Test size function
		String result4 = tool.executeTLScript("size([1, 2, 3])", null, null);
		assertNotNull("Result should not be null", result4);
		assertTrue("Result should contain success", result4.contains("\"success\":true") || result4.contains("\"success\": true"));
		assertTrue("Result should contain size", result4.contains("3"));
	}

	/**
	 * Tests that executeTLScript() works with withCommit flag set to true.
	 */
	public void testExecuteWithCommit() {
		TLScriptTool tool = new TLScriptTool();

		// Execute with commit=true
		String result = tool.executeTLScript("1 + 1", true, null);
		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain success", result.contains("\"success\":true") || result.contains("\"success\": true"));
		assertTrue("Result should contain executed flag",
			result.contains("\"executed\":true") || result.contains("\"executed\": true"));
		assertTrue("Result should contain computed value", result.contains("2"));
	}

	/**
	 * Tests that executeTLScript() works in read-only mode (withCommit=false or null).
	 */
	public void testExecuteWithoutCommit() {
		TLScriptTool tool = new TLScriptTool();

		// Execute with commit=false (explicit)
		String result1 = tool.executeTLScript("1 + 1", false, null);
		assertNotNull("Result should not be null", result1);
		assertTrue("Result should contain success", result1.contains("\"success\":true") || result1.contains("\"success\": true"));

		// Execute with commit=null (default should be false/read-only)
		String result2 = tool.executeTLScript("1 + 1", null, null);
		assertNotNull("Result should not be null", result2);
		assertTrue("Result should contain success", result2.contains("\"success\":true") || result2.contains("\"success\": true"));
	}

	/**
	 * Tests that executeTLScript() returns error for null script.
	 */
	public void testExecuteNullScript() {
		TLScriptTool tool = new TLScriptTool();

		// Null input
		String result1 = tool.executeTLScript(null, null, null);
		assertNotNull("Result should not be null", result1);
		assertTrue("Result should contain error", result1.contains("\"error\""));

		// Empty input
		String result2 = tool.executeTLScript("", null, null);
		assertNotNull("Result should not be null", result2);
		assertTrue("Result should contain error", result2.contains("\"error\""));
	}

	/**
	 * Tests that executeTLScript() returns error for invalid syntax.
	 */
	public void testExecuteInvalidScript() {
		TLScriptTool tool = new TLScriptTool();

		// Incomplete expression
		String result = tool.executeTLScript("1 +", null, null);
		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\"") || result.contains("\"success\":false"));
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(TLScriptToolTest.class);
	}

	protected static Test suite(Class<? extends Test> test, BasicRuntimeModule<?>... additionalModules) {
		BasicRuntimeModule<?>[] modules = getModules(additionalModules);

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(test),
				modules));
	}

	private static BasicRuntimeModule<?>[] getModules(BasicRuntimeModule<?>... additionalModules) {
		ArrayList<BasicRuntimeModule<?>> modules = new ArrayList<>();

		modules.add(ModelService.Module.INSTANCE);
		modules.add(LabelProviderService.Module.INSTANCE);

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
