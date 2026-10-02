/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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
import com.top_logic.tl.ai.core.tools.model.ModelBrowserTool;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link ModelBrowserTool}.
 */
public class ModelBrowserToolTest extends BasicTestCase {

	private static final String EVENTS_MODULE = "demo.events";
	private static final String EVENT_STATUS_ENUM = "EventStatus";
	private static final String EVENT_CLASS = "Event";
	private static final String NAMED_INTERFACE = "Named";
	private static final String DOCUMENTED_INTERFACE = "Documented";

	/**
	 * Tests that listModules() returns only application modules by default (framework modules
	 * excluded).
	 */
	public void testListModules() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listModules(null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain demo.events module",
			result.contains(EVENTS_MODULE));
		assertTrue("Result should be valid JSON with modules array",
			result.contains("\"modules\""));
		assertFalse("Result should NOT contain framework module tl.model",
			result.contains("\"tl.model\""));
	}

	/**
	 * Tests that listModules(includeFramework=true) returns both application and framework modules.
	 */
	public void testListModulesIncludeFramework() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listModules(true);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain demo.events module",
			result.contains(EVENTS_MODULE));
		assertTrue("Result should contain framework module tl.model",
			result.contains("\"tl.model\""));
		assertTrue("Result should be valid JSON with modules array",
			result.contains("\"modules\""));
	}

	/**
	 * Tests that listTypes() returns types from the demo.events module.
	 */
	public void testListTypes() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listTypes(EVENTS_MODULE);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain EventStatus enum",
			result.contains(EVENT_STATUS_ENUM));
		assertTrue("Result should contain Event class",
			result.contains(EVENT_CLASS));
		assertTrue("Result should contain Named interface",
			result.contains(NAMED_INTERFACE));
		assertTrue("Result should contain Documented interface",
			result.contains(DOCUMENTED_INTERFACE));
		assertTrue("Result should contain types array",
			result.contains("\"types\""));
		assertTrue("Result should contain meta-type information",
			result.contains("\"metaType\""));
	}

	/**
	 * Tests that listTypes() returns an error for an invalid module name.
	 */
	public void testListTypesInvalidModule() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listTypes("nonexistent.module");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error message",
			result.contains("\"error\""));
		assertTrue("Error message should mention module not found",
			result.contains("Module not found") || result.contains("not found"));
	}

	/**
	 * Tests that listTypes() returns an error for null module name.
	 */
	public void testListTypesNullModule() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listTypes(null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error message",
			result.contains("\"error\""));
		assertTrue("Error message should mention module name required",
			result.contains("required") || result.contains("Module name"));
	}

	/**
	 * Tests that listTypeParts() returns parts from the Event class.
	 */
	public void testListTypeParts() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listTypeParts(EVENTS_MODULE, EVENT_CLASS, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain parts array",
			result.contains("\"parts\""));
		assertTrue("Result should contain 'sessions' reference",
			result.contains("sessions"));
		assertTrue("Result should contain 'startDate' property",
			result.contains("startDate"));
		assertTrue("Result should contain 'ticketPrice' property",
			result.contains("ticketPrice"));
		assertTrue("Result should contain part kind information",
			result.contains("\"kind\""));
		assertTrue("Result should contain type information",
			result.contains("\"type\""));
	}

	/**
	 * Tests that listTypeParts() includes inherited parts when requested.
	 */
	public void testListTypePartsWithInherited() {
		ModelBrowserTool tool = new ModelBrowserTool();

		// Event inherits from Named, which has a 'name' property
		String resultWithInherited = tool.listTypeParts(EVENTS_MODULE, EVENT_CLASS, true);
		String resultWithoutInherited = tool.listTypeParts(EVENTS_MODULE, EVENT_CLASS, false);

		assertNotNull("Result with inherited should not be null", resultWithInherited);
		assertNotNull("Result without inherited should not be null", resultWithoutInherited);

		assertTrue("Result with inherited should contain 'name' property from Named",
			resultWithInherited.contains("\"name\""));
		assertTrue("Result with inherited should mark inherited parts",
			resultWithInherited.contains("\"inherited\""));

		// The result without inherited may or may not contain 'name' if Event overrides it,
		// but it should have fewer total parts
		int partsCountWithInherited = countOccurrences(resultWithInherited, "\"name\"");
		int partsCountWithoutInherited = countOccurrences(resultWithoutInherited, "\"name\"");

		assertTrue("With inherited should include parts from supertype",
			partsCountWithInherited >= partsCountWithoutInherited);
	}

	/**
	 * Tests that listTypeParts() returns an error for an invalid type.
	 */
	public void testListTypePartsInvalidType() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listTypeParts(EVENTS_MODULE, "NonExistentType", false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error message",
			result.contains("\"error\""));
		assertTrue("Error message should mention type not found",
			result.contains("Type not found") || result.contains("not found"));
	}

	/**
	 * Tests that listTypeParts() returns an error for an enumeration type.
	 */
	public void testListTypePartsOnEnumeration() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listTypeParts(EVENTS_MODULE, EVENT_STATUS_ENUM, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error message",
			result.contains("\"error\""));
		assertTrue("Error message should mention enumeration and listClassifiers",
			result.contains("enumeration") && result.contains("listClassifiers"));
	}

	/**
	 * Tests that listClassifiers() returns classifiers from the EventStatus enumeration.
	 */
	public void testListClassifiers() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listClassifiers(EVENTS_MODULE, EVENT_STATUS_ENUM);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain classifiers array",
			result.contains("\"classifiers\""));
		assertTrue("Result should contain DRAFT classifier",
			result.contains("\"DRAFT\""));
		assertTrue("Result should contain PLANNED classifier",
			result.contains("\"PLANNED\""));
		assertTrue("Result should contain OPEN_FOR_REGISTRATION classifier",
			result.contains("\"OPEN_FOR_REGISTRATION\""));
		assertTrue("Result should contain SOLD_OUT classifier",
			result.contains("\"SOLD_OUT\""));
		assertTrue("Result should contain RUNNING classifier",
			result.contains("\"RUNNING\""));
		assertTrue("Result should contain COMPLETED classifier",
			result.contains("\"COMPLETED\""));
		assertTrue("Result should contain CANCELLED classifier",
			result.contains("\"CANCELLED\""));
		assertTrue("Result should contain classifier count",
			result.contains("\"classifierCount\""));

		// Verify we have all 7 classifiers
		assertTrue("Result should indicate 7 classifiers",
			result.contains("\"classifierCount\":7") || result.contains("\"classifierCount\": 7"));
	}

	/**
	 * Tests that listClassifiers() returns an error for an invalid enumeration name.
	 */
	public void testListClassifiersInvalidEnum() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listClassifiers(EVENTS_MODULE, "NonExistentEnum");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error message",
			result.contains("\"error\""));
		assertTrue("Error message should mention type not found",
			result.contains("Type not found") || result.contains("not found"));
	}

	/**
	 * Tests that listClassifiers() returns an error for a non-enumeration type.
	 */
	public void testListClassifiersOnClass() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listClassifiers(EVENTS_MODULE, EVENT_CLASS);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error message",
			result.contains("\"error\""));
		assertTrue("Error message should mention type is not an enumeration",
			result.contains("not an enumeration") || result.contains("is not an enumeration"));
	}

	/**
	 * Tests that listClassifiers() returns an error for null module name.
	 */
	public void testListClassifiersNullModule() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String result = tool.listClassifiers(null, EVENT_STATUS_ENUM);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error message",
			result.contains("\"error\""));
		assertTrue("Error message should mention module name required",
			result.contains("required") || result.contains("Module name"));
	}

	/**
	 * Helper method to count occurrences of a substring.
	 */
	private int countOccurrences(String text, String substring) {
		int count = 0;
		int index = 0;
		while ((index = text.indexOf(substring, index)) != -1) {
			count++;
			index += substring.length();
		}
		return count;
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(ModelBrowserToolTest.class);
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
