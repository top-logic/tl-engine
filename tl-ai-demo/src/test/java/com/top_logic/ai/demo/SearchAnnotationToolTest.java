/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.service.tools.model.SearchAnnotationTool;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link SearchAnnotationTool}.
 */
public class SearchAnnotationToolTest extends BasicTestCase {

	private static final String TARGET_REF_PRODUCT_NAME = "luigis.pizzashop:Product#name";

	/**
	 * Tests searching annotations by keyword.
	 */
	public void testSearchByKeyword() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.searchAnnotations("visibility", null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain tagName field", result.contains("\"tagName\""));
		assertTrue("Result should contain configInterface field", result.contains("\"configInterface\""));
		assertTrue("Result should contain score field", result.contains("\"score\""));
		assertTrue("Result should contain totalMatches field", result.contains("\"totalMatches\""));
	}

	/**
	 * Tests that null query returns error JSON.
	 */
	public void testSearchWithNullQuery() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.searchAnnotations(null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests that empty query returns error JSON.
	 */
	public void testSearchWithEmptyQuery() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.searchAnnotations("", null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests that searching for a non-existent term returns zero matches.
	 */
	public void testSearchNoResults() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.searchAnnotations("xyznonexistent123", null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should have zero totalMatches",
			result.contains("\"totalMatches\":0") || result.contains("\"totalMatches\": 0"));
	}

	/**
	 * Tests that search results include required metadata fields.
	 */
	public void testSearchResultMetadata() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.searchAnnotations("format", null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain tagName", result.contains("\"tagName\""));
		assertTrue("Result should contain configInterface", result.contains("\"configInterface\""));
		assertTrue("Result should contain score", result.contains("\"score\""));
		assertTrue("Result should contain applicableTo", result.contains("\"applicableTo\""));
	}

	/**
	 * Tests filtering annotations by target reference.
	 */
	public void testSearchWithTargetRef() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String resultWithoutFilter = tool.searchAnnotations("display", null);
		String resultWithFilter = tool.searchAnnotations("display", TARGET_REF_PRODUCT_NAME);

		assertNotNull("Result without filter should not be null", resultWithoutFilter);
		assertNotNull("Result with filter should not be null", resultWithFilter);

		assertTrue("Result with filter should contain targetKind", resultWithFilter.contains("\"targetKind\""));
	}

	/**
	 * Tests that search is case-insensitive.
	 */
	public void testSearchCaseInsensitive() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String resultLower = tool.searchAnnotations("format", null);
		String resultUpper = tool.searchAnnotations("FORMAT", null);

		assertNotNull("Lower case result should not be null", resultLower);
		assertNotNull("Upper case result should not be null", resultUpper);

		assertTrue("Lower case search should find results",
			resultLower.contains("\"tagName\""));
		assertTrue("Upper case search should find results",
			resultUpper.contains("\"tagName\""));
	}

	/**
	 * Tests that visibility annotations are applicable to attributes.
	 */
	public void testSearchApplicableKinds() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.searchAnnotations("visibility", null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain applicableTo with attribute",
			result.contains("\"attribute\""));
	}

	/**
	 * Tests that searching with an invalid target reference returns an error.
	 */
	public void testSearchWithInvalidTargetRef() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.searchAnnotations("visibility", "nonexistent.module:FakeType");

		assertNotNull("Result should not be null", result);
		assertTrue("Result with invalid target ref should contain error",
			result.contains("\"error\""));
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(SearchAnnotationToolTest.class);
	}

	private static Test suite(Class<? extends Test> test, BasicRuntimeModule<?>... additionalModules) {
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

		for (BasicRuntimeModule<?> module : additionalModules) {
			modules.add(module);
		}

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
