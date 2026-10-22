/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import junit.framework.TestCase;

/**
 * Tests for {@link ModelEditorTool#createProperty} verifying error handling, input validation, and
 * JSON response format when called outside a TopLogic application context.
 *
 * <p>
 * Since {@link ModelEditorTool} requires a running TopLogic application context for actual model
 * operations, these tests verify the tool's error-handling paths: null/empty parameters and invalid
 * types all produce structured error JSON.
 * </p>
 */
public class TestModelEditorToolCreateProperty extends TestCase {

	private ModelEditorTool _tool;

	@Override
	protected void setUp() {
		_tool = new ModelEditorTool();
	}

	// -- Null/empty parameter validation --

	/**
	 * Verifies that a null module name returns error JSON.
	 */
	public void testNullModuleNameReturnsError() {
		String json = callCreateProperty(null, "MyClass", "myProp", "String");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that an empty module name returns error JSON.
	 */
	public void testEmptyModuleNameReturnsError() {
		String json = callCreateProperty("", "MyClass", "myProp", "String");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that a null class name returns error JSON.
	 */
	public void testNullClassNameReturnsError() {
		String json = callCreateProperty("myModule", null, "myProp", "String");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that an empty class name returns error JSON.
	 */
	public void testEmptyClassNameReturnsError() {
		String json = callCreateProperty("myModule", "", "myProp", "String");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that a null property name returns error JSON.
	 */
	public void testNullPropertyNameReturnsError() {
		String json = callCreateProperty("myModule", "MyClass", null, "String");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that an empty property name returns error JSON.
	 */
	public void testEmptyPropertyNameReturnsError() {
		String json = callCreateProperty("myModule", "MyClass", "", "String");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that a null property type returns error JSON.
	 */
	public void testNullPropertyTypeReturnsError() {
		String json = callCreateProperty("myModule", "MyClass", "myProp", null);

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that an empty property type returns error JSON.
	 */
	public void testEmptyPropertyTypeReturnsError() {
		String json = callCreateProperty("myModule", "MyClass", "myProp", "");

		assertContains(json, "\"error\":");
	}

	// -- Response is never null --

	/**
	 * Verifies that createProperty never returns null, even with invalid input.
	 */
	public void testNeverReturnsNull() {
		assertNotNull(callCreateProperty(null, "MyClass", "myProp", "String"));
		assertNotNull(callCreateProperty("myModule", null, "myProp", "String"));
		assertNotNull(callCreateProperty("myModule", "MyClass", null, "String"));
		assertNotNull(callCreateProperty("myModule", "MyClass", "myProp", null));
		assertNotNull(callCreateProperty("myModule", "MyClass", "myProp", "String"));
		assertNotNull(callCreateProperty("myModule", "MyClass", "myProp", "Text"));
		assertNotNull(callCreateProperty("myModule", "MyClass", "myProp", "Double"));
		assertNotNull(callCreateProperty("myModule", "MyClass", "myProp", "InvalidType"));
	}

	// -- Error responses include failure information --

	/**
	 * Verifies that the error response includes a failure reason for traceability.
	 */
	public void testErrorResponseIncludesFailureReason() {
		String json = callCreateProperty("myModule", "MyClass", "myProp", "String");

		assertContains(json, "\"error\":");
		assertContains(json, "Failed to create property");
	}

	private String callCreateProperty(String moduleName, String className,
			String propertyName, String propertyType) {
		return _tool.createProperty(moduleName, className, propertyName, propertyType,
			"Label EN", "Label DE", null, null, null, null, null, null, null);
	}

	/**
	 * Asserts that the given string contains the expected substring.
	 */
	private static void assertContains(String actual, String expectedSubstring) {
		assertNotNull("Response should not be null", actual);
		assertTrue(
			"Expected JSON to contain '" + expectedSubstring + "' but was: " + actual,
			actual.contains(expectedSubstring));
	}
}
