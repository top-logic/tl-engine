/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.TestModelCatalogLoad;
import com.top_logic.tl.ai.core.provider.catalog.CatalogStatus;
import com.top_logic.tl.ai.core.provider.catalog.ResolvedModel;

/**
 * Tests the TL-Script functions {@code aiModelCatalog()} and {@code aiModelCatalogStatus()}, which
 * the "AI models" section of the administration displays (ticket #29954).
 */
public class TestModelCatalogScriptFunctions extends TestCase {

	/** A model the bundled catalog lists and no override describes. */
	private static final String BUNDLED_MODEL = "claude-opus-4-20250514";

	/** The maximum output the bundled catalog gives {@link #BUNDLED_MODEL}. */
	private static final long BUNDLED_MAX_OUTPUT = 32000;

	/** The input price the bundled catalog gives {@link #BUNDLED_MODEL}. */
	private static final double BUNDLED_INPUT_PRICE = 15.0;

	/** The name of the provider {@link TestModelCatalogLoad#start} configures. */
	private static final String PROVIDER = "anthropic";

	/** A catalog URL nothing is loaded from, since the remote catalog is switched off. */
	private static final String UNUSED_URL = "http://127.0.0.1:1/catalog.json";

	private LLMProviderService _service;

	/**
	 * Instantiating a provider configuration needs the module system.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestModelCatalogScriptFunctions.class));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_service = TestModelCatalogLoad.start(UNUSED_URL, false,
			List.of(BUNDLED_MODEL, TestModelCatalogLoad.MODEL, TestModelCatalogLoad.LOCAL_MODEL),
			TestModelCatalogLoad.MODEL);
	}

	@Override
	protected void tearDown() throws Exception {
		if (_service != null) {
			TestModelCatalogLoad.stop(_service);
			_service = null;
		}
		super.tearDown();
	}

	/**
	 * Every configured model is a row, in configuration order, with its provider.
	 */
	public void testOneRowPerConfiguredModel() {
		List<Map<String, Object>> rows = AIScriptFunctions.modelCatalog(_service);

		assertEquals(3, rows.size());
		assertEquals(BUNDLED_MODEL, rows.get(0).get(AIScriptFunctions.MODEL_NAME));
		assertEquals(TestModelCatalogLoad.MODEL, rows.get(1).get(AIScriptFunctions.MODEL_NAME));
		assertEquals(TestModelCatalogLoad.LOCAL_MODEL, rows.get(2).get(AIScriptFunctions.MODEL_NAME));
		for (Map<String, Object> row : rows) {
			assertEquals(PROVIDER, row.get(AIScriptFunctions.MODEL_PROVIDER));
		}
	}

	/**
	 * A model of the bundled catalog shows the bundled origin and the values of the snapshot.
	 */
	public void testBundledModel() {
		Map<String, Object> row = row(BUNDLED_MODEL);

		assertEquals(ResolvedModel.Origin.BUNDLED, row.get(AIScriptFunctions.MODEL_ORIGIN));
		assertEquals(Long.valueOf(BUNDLED_MAX_OUTPUT), row.get(AIScriptFunctions.MODEL_MAX_OUTPUT));
		assertEquals(Double.valueOf(BUNDLED_INPUT_PRICE), row.get(AIScriptFunctions.MODEL_INPUT_PRICE));
		assertEquals(Boolean.TRUE, row.get(AIScriptFunctions.MODEL_TOOL_CALL));
		assertNotNull(row.get(AIScriptFunctions.MODEL_RELEASE_DATE));
	}

	/**
	 * A model with a model override shows the override origin and the overridden value.
	 */
	public void testOverriddenModel() {
		Map<String, Object> row = row(TestModelCatalogLoad.MODEL);

		assertEquals(ResolvedModel.Origin.OVERRIDE, row.get(AIScriptFunctions.MODEL_ORIGIN));
		assertEquals(Long.valueOf(TestModelCatalogLoad.OVERRIDE_CONTEXT),
			row.get(AIScriptFunctions.MODEL_CONTEXT_WINDOW));
	}

	/**
	 * A configured model the catalog does not describe is a row with a provider and a name only.
	 */
	public void testModelWithoutCatalogEntry() {
		Map<String, Object> row = row(TestModelCatalogLoad.LOCAL_MODEL);

		assertEquals(Map.of(
			AIScriptFunctions.MODEL_PROVIDER, PROVIDER,
			AIScriptFunctions.MODEL_NAME, TestModelCatalogLoad.LOCAL_MODEL), row);
		assertNull(row.get(AIScriptFunctions.MODEL_ORIGIN));
	}

	/**
	 * With the remote catalog switched off, the status says so and names the configured URL.
	 */
	public void testStatusDisabled() {
		Map<String, Object> status = AIScriptFunctions.modelCatalogStatus(_service);

		assertEquals(CatalogStatus.State.DISABLED, status.get(AIScriptFunctions.CATALOG_STATE));
		assertEquals(UNUSED_URL, status.get(AIScriptFunctions.CATALOG_URL));
		assertTrue(status.get(AIScriptFunctions.CATALOG_INSTALLED_AT) instanceof Date);
		assertNull(status.get(AIScriptFunctions.CATALOG_FAILURE));
	}

	private Map<String, Object> row(String model) {
		return AIScriptFunctions.modelCatalog(_service).stream()
			.filter(row -> model.equals(row.get(AIScriptFunctions.MODEL_NAME)))
			.findFirst()
			.orElseThrow(() -> new AssertionError("No row for " + model));
	}
}
