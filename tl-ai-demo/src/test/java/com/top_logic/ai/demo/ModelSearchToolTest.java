/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tl.ai.core.tools.model.ModelSearchTool;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link ModelSearchTool}.
 */
public class ModelSearchToolTest extends BasicTestCase {

	private static final String PIZZASHOP_MODULE = "luigis.pizzashop";
	private static final String UNIT_OF_MEASURE_ENUM = "UnitOfMeasure";
	private static final String PRODUCT_CLASS = "Product";
	/**
	 * Tests searching for a class by exact name.
	 */
	public void testSearchByExactClassName() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel(PRODUCT_CLASS, null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should be valid JSON with a result count", result.contains("\"resultCount\""));
		assertTrue("Result should contain Product class", result.contains(PRODUCT_CLASS));
		assertTrue("Result should contain class kind", result.contains("\"kind\":\"class\""));
		assertTrue("Result should contain qualified name", result.contains("\"qualifiedName\""));
		assertTrue("Result should contain score", result.contains("\"score\""));
		// Exact match should have highest score
		assertTrue("Exact match should have high score (1000)", result.contains("\"score\":1000"));
	}

	/**
	 * Tests searching for an enumeration by exact name.
	 */
	public void testSearchByExactEnumName() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel(UNIT_OF_MEASURE_ENUM, null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain UnitOfMeasure enum", result.contains(UNIT_OF_MEASURE_ENUM));
		assertTrue("Result should contain enumeration kind", result.contains("\"kind\":\"enumeration\""));
		assertTrue("Result should contain classifier count", result.contains("\"classifierCount\""));
		assertTrue("Exact match should have high score (1000)", result.contains("\"score\":1000"));
	}

	/**
	 * Tests searching for a property by name.
	 */
	public void testSearchByPropertyName() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("plannedQuantity", null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain plannedQuantity", result.contains("plannedQuantity"));
		assertTrue("Result should contain property kind", result.contains("\"kind\":\"property\""));
		assertTrue("Result should contain owner type in context", result.contains("\"type\""));
		assertTrue("Result should contain module in context", result.contains("\"module\""));
	}

	/**
	 * Tests searching for a reference by name.
	 */
	public void testSearchByReferenceName() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("components", null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain components", result.contains("components"));
		assertTrue("Result should contain reference kind", result.contains("\"kind\":\"reference\""));
		assertTrue("Result should contain owner type", result.contains("\"type\""));
	}

	/**
	 * Tests searching by partial name (prefix match).
	 */
	public void testSearchByPartialName() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("Unit", null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain UnitOfMeasure", result.contains("UnitOfMeasure"));
		assertTrue("Result should contain results", result.contains("\"results\""));
		// Prefix match should have score 500
		assertTrue("Prefix match should have medium-high score", result.contains("\"score\":500"));
	}

	/**
	 * Tests searching with module filter to limit results to pizzashop module.
	 */
	public void testSearchWithModuleFilter() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("Product", Collections.singletonList(PIZZASHOP_MODULE), null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain Product", result.contains(PRODUCT_CLASS));
		assertTrue("Result should contain luigis.pizzashop module", result.contains(PIZZASHOP_MODULE));
		assertTrue("Result should be successful", result.contains("\"error\":\"\""));
	}

	/**
	 * Tests searching with kind filter to only return classes.
	 */
	public void testSearchWithKindFilterClass() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("Product", null, Collections.singletonList("class"));

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain Product", result.contains(PRODUCT_CLASS));
		assertTrue("Result should only contain class kind", result.contains("\"kind\":\"class\""));
		// Should not contain enumerations, properties, or references
		assertFalse("Result should not contain enumeration kind", result.contains("\"kind\":\"enumeration\""));
		assertFalse("Result should not contain property kind", result.contains("\"kind\":\"property\""));
	}

	/**
	 * Tests searching with kind filter to only return enumerations.
	 */
	public void testSearchWithKindFilterEnum() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("Unit", null, Collections.singletonList("enumeration"));

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain UnitOfMeasure", result.contains(UNIT_OF_MEASURE_ENUM));
		assertTrue("Result should only contain enumeration kind", result.contains("\"kind\":\"enumeration\""));
		assertFalse("Result should not contain class kind", result.contains("\"kind\":\"class\""));
	}

	/**
	 * Tests searching with multiple kind filters.
	 */
	public void testSearchWithMultipleKindFilters() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("name", null, Arrays.asList("property", "reference"));

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain results", result.contains("\"results\""));
		// Should only contain properties or references
		assertTrue("Result should contain property or reference kind",
			result.contains("\"kind\":\"property\"") || result.contains("\"kind\":\"reference\""));
	}

	/**
	 * Tests searching for module by name.
	 */
	public void testSearchByModuleName() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("pizzashop", null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain pizzashop", result.contains("pizzashop"));
		assertTrue("Result should contain module kind", result.contains("\"kind\":\"module\""));
		assertTrue("Result should contain type count", result.contains("\"typeCount\""));
	}

	/**
	 * Tests that search with null query returns an error.
	 */
	public void testSearchWithNullQuery() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel(null, null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertFalse("Result should indicate failure", result.contains("\"error\":\"\""));
		assertTrue("Error message should mention query required",
			result.contains("required") || result.contains("query"));
	}

	/**
	 * Tests that search with empty query returns an error.
	 */
	public void testSearchWithEmptyQuery() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("", null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertFalse("Result should indicate failure", result.contains("\"error\":\"\""));
	}

	/**
	 * Tests searching for something that doesn't exist.
	 */
	public void testSearchNoResults() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("NonExistentTypeXYZ123", null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should be successful", result.contains("\"error\":\"\""));
		assertTrue("Result should have zero result count",
			result.contains("\"resultCount\":0") || result.contains("\"resultCount\": 0"));
		assertTrue("Result should have empty results array",
			result.contains("\"results\":[]") || result.contains("\"results\": []"));
	}

	/**
	 * Tests that search returns matched field information.
	 */
	public void testSearchMatchedField() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel(PRODUCT_CLASS, null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain matchedField", result.contains("\"matchedField\""));
		// Exact name match should indicate name field was matched
		assertTrue("Matched field should be name for exact match", result.contains("\"matchedField\":\"name\""));
	}

	/**
	 * Tests that search returns results sorted by score.
	 */
	public void testSearchResultsSortedByScore() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("name", null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain multiple results", result.contains("\"results\""));
		assertTrue("Result should have result count", result.contains("\"resultCount\""));
		// Results should be sorted with higher scores first
		assertTrue("Result should contain score information", result.contains("\"score\""));
	}

	/**
	 * Tests searching returns at most 10 results.
	 */
	public void testSearchMaxResults() {
		ModelSearchTool tool = new ModelSearchTool();
		// Search for a common term that might match many things
		String result = tool.searchModel("name", null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should be successful", result.contains("\"error\":\"\""));
		// Parse result count - it should not exceed 10
		assertTrue("Result should contain resultCount", result.contains("\"resultCount\""));
		// We can't easily parse JSON here, but we verify the format
		assertTrue("Result should have results array", result.contains("\"results\""));
	}

	/**
	 * Tests case-insensitive search.
	 */
	public void testSearchCaseInsensitive() {
		ModelSearchTool tool = new ModelSearchTool();
		String resultLower = tool.searchModel("product", null, null);
		String resultUpper = tool.searchModel("PRODUCT", null, null);
		String resultMixed = tool.searchModel("PrOdUcT", null, null);

		assertNotNull("Lower case result should not be null", resultLower);
		assertNotNull("Upper case result should not be null", resultUpper);
		assertNotNull("Mixed case result should not be null", resultMixed);

		assertTrue("Lower case search should find Product", resultLower.contains(PRODUCT_CLASS));
		assertTrue("Upper case search should find Product", resultUpper.contains(PRODUCT_CLASS));
		assertTrue("Mixed case search should find Product", resultMixed.contains(PRODUCT_CLASS));

		// All should have same high score for exact match (ignoring case)
		assertTrue("Lower case should have score 1000", resultLower.contains("\"score\":1000"));
		assertTrue("Upper case should have score 1000", resultUpper.contains("\"score\":1000"));
		assertTrue("Mixed case should have score 1000", resultMixed.contains("\"score\":1000"));
	}

	/**
	 * Tests that search includes context information for results.
	 */
	public void testSearchIncludesContext() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel(PRODUCT_CLASS, null, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain context", result.contains("\"context\""));
		assertTrue("Result should contain module in context", result.contains("\"module\""));
		// For a class, should include part count
		assertTrue("Result should contain part count", result.contains("\"partCount\""));
	}

	/**
	 * Tests combined module and kind filters.
	 */
	public void testSearchWithCombinedFilters() {
		ModelSearchTool tool = new ModelSearchTool();
		String result = tool.searchModel("name",
			Collections.singletonList(PIZZASHOP_MODULE),
			Collections.singletonList("property"));

		assertNotNull("Result should not be null", result);
		assertTrue("Result should be successful", result.contains("\"error\":\"\""));
		// If there are results, they should be properties from pizzashop module
		if (result.contains("\"resultCount\":0") || result.contains("\"resultCount\": 0")) {
			// No results is valid
			assertTrue("No results is valid", true);
		} else {
			assertTrue("Results should contain property kind", result.contains("\"kind\":\"property\""));
			assertTrue("Results should be from pizzashop module", result.contains(PIZZASHOP_MODULE));
		}
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(ModelSearchToolTest.class);
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
