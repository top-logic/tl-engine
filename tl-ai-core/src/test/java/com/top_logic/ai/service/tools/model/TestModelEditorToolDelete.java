/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.model;

import junit.framework.TestCase;

/**
 * Tests for {@link ModelEditorTool#deleteModelElement(String)} verifying error handling, input
 * validation, and JSON response format when called outside a TopLogic application context.
 *
 * <p>
 * Since {@link ModelEditorTool} requires a running TopLogic application context for actual model
 * operations, these tests verify the tool's error-handling paths: null/empty parameters and
 * non-existent elements all produce structured error JSON.
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
	 * Verifies that a null qualifiedName returns error JSON.
	 */
	public void testDeleteNullQualifiedNameReturnsError() {
		String json = _tool.deleteModelElement(null);

		assertContains(json, "\"error\":");
	}

	/**
	 * Verifies that an empty qualifiedName returns error JSON.
	 */
	public void testDeleteEmptyQualifiedNameReturnsError() {
		String json = _tool.deleteModelElement("");

		assertContains(json, "\"error\":");
	}

	// -- Response is never null --

	/**
	 * Verifies that deleteModelElement never returns null, even with invalid input.
	 */
	public void testDeleteNeverReturnsNull() {
		assertNotNull(_tool.deleteModelElement(null));
		assertNotNull(_tool.deleteModelElement(""));
		assertNotNull(_tool.deleteModelElement("nonExistent"));
		assertNotNull(_tool.deleteModelElement("module:Type"));
		assertNotNull(_tool.deleteModelElement("module:Type#part"));
	}

	// -- All qualified name formats produce a response --

	/**
	 * Verifies that each qualified name format returns a non-null error response (since no TopLogic
	 * context is available).
	 */
	public void testDeleteAllQualifiedNameFormatsReturnResponse() {
		String[] names = {
			"testModule",
			"testModule:TestClass",
			"testModule:TestEnum",
			"testModule:TestClass#prop",
			"testModule:TestEnum#VALUE"
		};

		for (String name : names) {
			String json = _tool.deleteModelElement(name);
			assertNotNull("Response for '" + name + "' should not be null", json);
			// All should fail because no TopLogic context, but should return structured error
			assertContains(json, "\"error\":");
		}
	}

	/**
	 * Verifies that the error response includes the failure reason for traceability.
	 */
	public void testDeleteErrorResponseIncludesFailureReason() {
		String json = _tool.deleteModelElement("nonExistent");

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
