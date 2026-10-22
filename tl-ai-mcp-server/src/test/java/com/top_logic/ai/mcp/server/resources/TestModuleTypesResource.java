/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.resources;

import java.util.Map;

import com.top_logic.ai.mcp.server.UriPattern;

import junit.framework.TestCase;

/**
 * Test case for {@link ModuleTypesResource}.
 *
 * @author Bernhard Haumacher
 */
public class TestModuleTypesResource extends TestCase {

	/**
	 * Tests the URI pattern creation from template strings.
	 *
	 * <p>
	 * This test verifies that the pattern correctly extracts variable values from URIs that match
	 * the template format.
	 * </p>
	 */
	public void testCreateUriPattern() {
		// Test the actual URI template used by ModuleTypesResource
		String template = "toplogic://model/modules/{moduleName}/types";
		UriPattern uriPattern = UriPattern.compile(template);

		// Test valid URIs with different module names
		assertMatches(uriPattern, "toplogic://model/modules/tl.core/types", "moduleName", "tl.core");
		assertMatches(uriPattern, "toplogic://model/modules/myapp.model/types", "moduleName", "myapp.model");
		assertMatches(uriPattern, "toplogic://model/modules/com.example.app/types", "moduleName", "com.example.app");

		// Test that invalid URIs don't match
		assertNoMatch(uriPattern, "toplogic://model/modules/types"); // Missing module name
		assertNoMatch(uriPattern, "toplogic://model/modules/tl.core"); // Missing /types
		assertNoMatch(uriPattern, "toplogic://model/tl.core/types"); // Missing /modules/
		assertNoMatch(uriPattern, "http://model/modules/tl.core/types"); // Wrong protocol
	}

	/**
	 * Tests pattern creation with multiple template variables.
	 */
	public void testMultipleVariables() {
		String template = "toplogic://model/{moduleName}/types/{typeName}";
		UriPattern uriPattern = UriPattern.compile(template);

		String uri = "toplogic://model/tl.core/types/MyType";
		Map<String, String> params = uriPattern.extractParameters(uri);

		assertNotNull("URI should match pattern", params);
		assertEquals("First variable", "tl.core", params.get("moduleName"));
		assertEquals("Second variable", "MyType", params.get("typeName"));
	}

	/**
	 * Tests pattern creation with special regex characters in the template.
	 */
	public void testSpecialCharacters() {
		// Template contains dots and slashes which are special in regex
		String template = "toplogic://model.v2/modules/{moduleName}/types";
		UriPattern uriPattern = UriPattern.compile(template);

		assertMatches(uriPattern, "toplogic://model.v2/modules/tl.core/types", "moduleName", "tl.core");

		// Should NOT match if the dot is replaced (dot should be literal, not regex wildcard)
		assertNoMatch(uriPattern, "toplogic://modelXv2/modules/tl.core/types");
	}

	/**
	 * Tests that module names with slashes are NOT captured (they should be rejected).
	 */
	public void testNoSlashesInVariables() {
		String template = "toplogic://model/modules/{moduleName}/types";
		UriPattern uriPattern = UriPattern.compile(template);

		// Module names with slashes should not match
		assertNoMatch(uriPattern, "toplogic://model/modules/tl/core/types");
		assertNoMatch(uriPattern, "toplogic://model/modules/my/app/model/types");
	}

	/**
	 * Helper method to assert that a URI matches the pattern and extracts the expected value.
	 */
	private void assertMatches(UriPattern uriPattern, String uri, String paramName, String expectedValue) {
		Map<String, String> params = uriPattern.extractParameters(uri);
		assertNotNull("URI should match: " + uri, params);
		assertEquals("Extracted value for parameter '" + paramName + "' from: " + uri, expectedValue, params.get(paramName));
	}

	/**
	 * Helper method to assert that a URI does NOT match the pattern.
	 */
	private void assertNoMatch(UriPattern uriPattern, String uri) {
		try {
			uriPattern.extractParameters(uri);
			fail("URI should NOT match but did: " + uri);
		} catch (IllegalArgumentException ex) {
			// Expected - URI does not match the pattern
		}
	}
}
