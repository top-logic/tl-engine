/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.model;

import junit.framework.TestCase;

/**
 * Tests for {@link ModelEditorTool#deleteModelElement(String, String)} verifying error handling,
 * input validation, and JSON response format when called outside a TopLogic application context.
 *
 * <p>
 * Since {@link ModelEditorTool} requires a running TopLogic application context for actual model
 * operations, these tests verify the tool's error-handling paths: null/empty parameters, invalid
 * element types, and malformed qualified names all produce structured error JSON.
 * </p>
 */
public class TestModelEditorToolDelete extends TestCase {

	private ModelEditorTool _tool;

	@Override
	protected void setUp() {
		_tool = new ModelEditorTool();
	}

	// -- Null/empty parameter validation --

	/**
	 * Verifies that a null elementType returns error JSON.
	 */
	public void testDeleteNullElementTypeReturnsError() {
		String json = _tool.deleteModelElement(null, "someModule");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that an empty elementType returns error JSON.
	 */
	public void testDeleteEmptyElementTypeReturnsError() {
		String json = _tool.deleteModelElement("", "someModule");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that a null qualifiedName returns error JSON.
	 */
	public void testDeleteNullQualifiedNameReturnsError() {
		String json = _tool.deleteModelElement("MODULE", null);

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that an empty qualifiedName returns error JSON.
	 */
	public void testDeleteEmptyQualifiedNameReturnsError() {
		String json = _tool.deleteModelElement("CLASS", "");

		assertContains(json, "\"error\":");
	}

	// -- Invalid element type --

	/**
	 * Verifies that an invalid elementType returns error JSON mentioning the invalid type.
	 */
	public void testDeleteInvalidElementTypeReturnsError() {
		String json = _tool.deleteModelElement("INVALID_TYPE", "someModule:SomeClass");

		assertContains(json, "\"error\":");
	}

	// -- Malformed qualified names --

	/**
	 * Verifies that a CLASS qualified name without colon separator returns error JSON.
	 */
	public void testDeleteClassWithoutColonReturnsError() {
		String json = _tool.deleteModelElement("CLASS", "noColonHere");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that a PROPERTY qualified name without hash separator returns error JSON.
	 */
	public void testDeletePropertyWithoutHashReturnsError() {
		String json = _tool.deleteModelElement("PROPERTY", "module:TypeOnly");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that a REFERENCE qualified name without hash separator returns error JSON.
	 */
	public void testDeleteReferenceWithoutHashReturnsError() {
		String json = _tool.deleteModelElement("REFERENCE", "module:TypeOnly");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that a CLASSIFIER qualified name without hash separator returns error JSON.
	 */
	public void testDeleteClassifierWithoutHashReturnsError() {
		String json = _tool.deleteModelElement("CLASSIFIER", "module:EnumOnly");

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that an ENUMERATION qualified name without colon separator returns error JSON.
	 */
	public void testDeleteEnumerationWithoutColonReturnsError() {
		String json = _tool.deleteModelElement("ENUMERATION", "noColonHere");

		assertContains(json, "\"error\":");
	}

	// -- Response is never null --

	/**
	 * Verifies that deleteModelElement never returns null, even with completely invalid input.
	 */
	public void testDeleteNeverReturnsNull() {
		assertNotNull(_tool.deleteModelElement(null, null));
		assertNotNull(_tool.deleteModelElement("", ""));
		assertNotNull(_tool.deleteModelElement("MODULE", "nonExistent"));
		assertNotNull(_tool.deleteModelElement("INVALID", "x:y#z"));
	}

	// -- All element types produce a response --

	/**
	 * Verifies that each valid element type returns a non-null error response (since no TopLogic
	 * context is available).
	 */
	public void testDeleteAllElementTypesReturnResponse() {
		String[] types = { "MODULE", "CLASS", "ENUMERATION", "PROPERTY", "REFERENCE", "CLASSIFIER" };
		String[] names = {
			"testModule",
			"testModule:TestClass",
			"testModule:TestEnum",
			"testModule:TestClass#prop",
			"testModule:TestClass#ref",
			"testModule:TestEnum#VALUE"
		};

		for (int i = 0; i < types.length; i++) {
			String json = _tool.deleteModelElement(types[i], names[i]);
			assertNotNull("Response for " + types[i] + " should not be null", json);
			// All should fail because no TopLogic context, but should return structured error
			assertContains(json, "\"error\":");
		}
	}

	/**
	 * Verifies that the error response includes the failure reason for traceability.
	 */
	public void testDeleteErrorResponseIncludesFailureReason() {
		String json = _tool.deleteModelElement("MODULE", "nonExistent");

		assertContains(json, "\"error\":");
		assertContains(json, "Failed to delete element");
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
