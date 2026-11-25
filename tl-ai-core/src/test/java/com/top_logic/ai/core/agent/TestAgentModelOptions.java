/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.agent;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.ai.core.provider.AgentModelAssignment;
import com.top_logic.ai.core.provider.LLMProviderConfig;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.provider.LLMProviderService.ModelCatalogConfig;
import com.top_logic.ai.core.provider.LLMProviderServiceFixture;
import com.top_logic.ai.core.provider.catalog.ModelInfo;
import com.top_logic.ai.core.provider.vertex.VertexProviderConfig;

/**
 * Tests {@link AgentFactory#buildModelOptions(AgentModelAssignment, ModelInfo)}: Claude gets its
 * options by its API family, also on a provider whose catalog ID is not the one of the Anthropic API.
 */
public class TestAgentModelOptions extends TestCase {

	private static final String PROVIDER = "vertex";

	private static final String MODEL = "claude-sonnet-5-5";

	private LLMProviderService _service;

	/**
	 * Instantiating a provider configuration resolves its implementation class through the
	 * {@code TypeIndex}, which needs the module system.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestAgentModelOptions.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		@SuppressWarnings("unchecked")
		LLMProviderService.Config<LLMProviderService> config =
			(LLMProviderService.Config<LLMProviderService>) TypedConfiguration.newConfigItem(
				LLMProviderService.Config.class);
		LLMProviderConfig.Config vertex = vertex();
		config.getProviders().put(vertex.getName(), vertex);
		ModelCatalogConfig catalog = TypedConfiguration.newConfigItem(ModelCatalogConfig.class);
		catalog.update(catalog.descriptor().getProperty(ModelCatalogConfig.FETCH_REMOTE_CATALOG), Boolean.FALSE);
		config.update(config.descriptor().getProperty(LLMProviderService.Config.MODEL_CATALOG), catalog);
		_service = LLMProviderServiceFixture.start(new DefaultInstantiationContext(TestAgentModelOptions.class),
			config);
	}

	@Override
	protected void tearDown() throws Exception {
		LLMProviderServiceFixture.stop(_service);
		_service = null;
		super.tearDown();
	}

	/**
	 * Claude on Google Vertex AI receives the thinking effort and {@code top_k}, whether the
	 * assignment names the provider or not.
	 */
	public void testClaudeOnVertex() {
		ModelInfo info = _service.getModel(MODEL);
		assertNotNull("The catalog must know the model.", info);
		assertTrue("The model must be able to reason.", info.capabilities().reasoning());

		for (String provider : new String[] { PROVIDER, null }) {
			Map<String, Object> options = AgentFactory.buildModelOptions(assignment(provider), info);

			assertEquals("high", options.get("thinking_effort"));
			assertEquals(Integer.valueOf(40), options.get("top_k"));
			assertFalse(options.containsKey("reasoning_effort"));
		}
	}

	private static AgentModelAssignment assignment(String provider) {
		return new AgentModelAssignment(MODEL, provider, null, null, null, null, null, "HIGH", Integer.valueOf(40),
			"high", null, null, null, null, null);
	}

	private static LLMProviderConfig.Config vertex() throws ConfigurationException {
		String xml = "<provider xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + VertexProviderConfig.Config.class.getName() + "'"
			+ " class='" + VertexProviderConfig.class.getName() + "' name='" + PROVIDER + "'"
			+ " project='my-project' location='eu' models='" + MODEL + "'/>";
		return (LLMProviderConfig.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
	}
}
