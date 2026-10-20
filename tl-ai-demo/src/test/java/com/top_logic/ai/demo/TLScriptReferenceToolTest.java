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

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tl.ai.core.tools.script.TLScriptReferenceTool;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link TLScriptReferenceTool}.
 *
 * <p>
 * Three of these asserted a {@code functionName} and a {@code hasDocumentation} field on the
 * documentation response (#366). {@code FunctionDocResult} declares neither - only the body, the
 * parameters and an error message - so the assertions could never hold, and nothing noticed because
 * no build ran this class until #369. They now assert the fields the response really carries.
 * Nothing user-visible asked for the function name to be echoed: a caller gets the documentation for
 * the function it just named.
 * </p>
 */
public class TLScriptReferenceToolTest extends BasicTestCase {

	// Common TLScript functions that should always exist
	private static final String IF_FUNCTION = "if";
	private static final String CONCAT_FUNCTION = "concat";
	private static final String SIZE_FUNCTION = "size";
	private static final String MAP_FUNCTION = "map";

	/**
	 * Tests that listTLScriptFunctions() returns a valid JSON response.
	 */
	public void testListTLScriptFunctions() {
		TLScriptReferenceTool tool = new TLScriptReferenceTool();
		String result = tool.listTLScriptFunctions();

		assertNotNull("Result should not be null", result);
		assertTrue("Result should be valid JSON with functions array",
			result.contains("\"functions\""));
		assertTrue("Result should contain opening brace",
			result.contains("{"));
		assertTrue("Result should contain closing brace",
			result.contains("}"));
	}

	/**
	 * Tests that listTLScriptFunctions() returns common TLScript functions.
	 */
	public void testListTLScriptFunctionsContainsExpectedFunctions() {
		TLScriptReferenceTool tool = new TLScriptReferenceTool();
		String result = tool.listTLScriptFunctions();

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain 'if' function",
			result.contains("\"" + IF_FUNCTION + "\""));
		assertTrue("Result should contain 'concat' function",
			result.contains("\"" + CONCAT_FUNCTION + "\""));
		assertTrue("Result should contain 'size' function",
			result.contains("\"" + SIZE_FUNCTION + "\""));
		assertTrue("Result should contain 'map' function",
			result.contains("\"" + MAP_FUNCTION + "\""));
	}

	/**
	 * Tests that getTLScriptFunctionDocumentation() returns valid JSON for a valid function.
	 */
	public void testGetTLScriptFunctionDocumentationValidFunction() {
		TLScriptReferenceTool tool = new TLScriptReferenceTool();
		String result = tool.getTLScriptFunctionDocumentation(IF_FUNCTION);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain the documentation body",
			result.contains("\"documentation\""));
		assertTrue("Result should contain parameters array",
			result.contains("\"parameters\""));
	}

	/**
	 * Tests that getTLScriptFunctionDocumentation() returns error for null function name.
	 */
	public void testGetTLScriptFunctionDocumentationNullFunctionName() {
		TLScriptReferenceTool tool = new TLScriptReferenceTool();
		String result = tool.getTLScriptFunctionDocumentation(null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error message",
			result.contains("\"error\""));
		assertTrue("Error message should mention function name required",
			result.contains("required") || result.contains("Function name"));
	}

	/**
	 * Tests that getTLScriptFunctionDocumentation() returns error for empty function name.
	 */
	public void testGetTLScriptFunctionDocumentationEmptyFunctionName() {
		TLScriptReferenceTool tool = new TLScriptReferenceTool();
		String result = tool.getTLScriptFunctionDocumentation("");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error message",
			result.contains("\"error\""));
		assertTrue("Error message should mention function name required",
			result.contains("required") || result.contains("Function name"));
	}

	/**
	 * Tests that getTLScriptFunctionDocumentation() handles non-existent function gracefully.
	 */
	public void testGetTLScriptFunctionDocumentationNonExistentFunction() {
		TLScriptReferenceTool tool = new TLScriptReferenceTool();
		String result = tool.getTLScriptFunctionDocumentation("nonExistentFunction12345");

		assertNotNull("Result should not be null", result);
		assertTrue("An unknown function is reported as a missing documentation, not as a crash",
			result.contains("\"errorMessage\""));
		assertTrue("The reported error names the function that was asked for",
			result.contains("nonExistentFunction12345"));
	}

	/**
	 * Tests that getTLScriptFunctionDocumentation() returns documentation for multiple functions.
	 */
	public void testGetTLScriptFunctionDocumentationMultipleFunctions() {
		TLScriptReferenceTool tool = new TLScriptReferenceTool();

		String[] functions = {IF_FUNCTION, CONCAT_FUNCTION, MAP_FUNCTION, SIZE_FUNCTION};

		for (String function : functions) {
			String result = tool.getTLScriptFunctionDocumentation(function);

			assertNotNull("Result for " + function + " should not be null", result);
			assertTrue("Result for " + function + " should contain the documentation body",
				result.contains("\"documentation\""));
			assertTrue("Result for " + function + " should contain parameters array",
				result.contains("\"parameters\""));
			assertTrue("Result for " + function + " should be valid JSON",
				result.startsWith("{") && result.endsWith("}"));
		}
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(TLScriptReferenceToolTest.class);
	}

	/** */
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
