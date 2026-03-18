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
	 * Tests listing annotations for a target returns results.
	 */
	public void testListAvailableAnnotations() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.listAvailableAnnotations(TARGET_REF_PRODUCT_NAME);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain tagName", result.contains("\"tagName\""));
		assertTrue("Result should contain configInterface", result.contains("\"configInterface\""));
		assertTrue("Result should contain applicableTo", result.contains("\"applicableTo\""));
		assertTrue("Result should contain totalMatches", result.contains("\"totalMatches\""));
		assertTrue("Result should contain targetRef", result.contains("\"targetRef\""));
		assertTrue("Result should contain targetKind", result.contains("\"targetKind\""));
	}

	/**
	 * Tests that null target reference returns error.
	 */
	public void testListWithNullTargetRef() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.listAvailableAnnotations(null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests that empty target reference returns error.
	 */
	public void testListWithEmptyTargetRef() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.listAvailableAnnotations("");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests that an invalid target reference returns error.
	 */
	public void testListWithInvalidTargetRef() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.listAvailableAnnotations("nonexistent.module:FakeType");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests that results contain attribute-applicable annotations for an attribute target.
	 */
	public void testListForAttributeContainsApplicableKinds() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.listAvailableAnnotations(TARGET_REF_PRODUCT_NAME);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain attribute applicability", result.contains("\"attribute\""));
	}

	/**
	 * Tests listing annotations for a type target.
	 */
	public void testListForType() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.listAvailableAnnotations("luigis.pizzashop:Product");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain results", result.contains("\"results\""));
		assertTrue("Result should contain targetKind", result.contains("\"targetKind\""));
	}

	/**
	 * Tests listing annotations for a module target.
	 */
	public void testListForModule() {
		SearchAnnotationTool tool = new SearchAnnotationTool();
		String result = tool.listAvailableAnnotations("luigis.pizzashop");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain results", result.contains("\"results\""));
		assertTrue("Result should contain targetKind", result.contains("\"targetKind\""));
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
