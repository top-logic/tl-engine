/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.provider.LLMProviderService.ModelCatalogConfig;
import com.top_logic.tl.ai.core.provider.bedrock.BedrockConverseProviderConfig;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.health.ModelOptions;

/**
 * Tests that Claude is of the {@link ApiFamilies#ANTHROPIC API family} on a provider that serves
 * the models of several vendors, so that it gets the options of that family there.
 */
public class TestApiFamily extends TestCase {

	private static final String CLAUDE = "eu.anthropic.claude-opus-5-5";

	private static final String NOVA = "amazon.nova-pro-v1:0";

	private static final Map<String, Object> RUN_OPTIONS = Map.of(
		ProviderRequest.THINKING_EFFORT, "high",
		ProviderRequest.TOP_K, Integer.valueOf(40),
		ProviderRequest.CACHE_TTL, "1h");

	private LLMProviderService _service;

	/**
	 * Instantiating a provider configuration resolves its implementation class through the
	 * {@code TypeIndex}, which needs the module system.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestApiFamily.class));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_service = LLMProviderServiceFixture.start(new DefaultInstantiationContext(TestApiFamily.class),
			serviceConfig(bedrock("bedrock", CLAUDE, NOVA)));
	}

	@Override
	protected void tearDown() throws Exception {
		LLMProviderServiceFixture.stop(_service);
		_service = null;
		super.tearDown();
	}

	/**
	 * Bedrock answers the family of the model's vendor, and its catalog ID by default.
	 */
	public void testBedrockFamily() {
		assertEquals(ApiFamilies.ANTHROPIC, _service.getApiFamily(CLAUDE, "bedrock"));
		assertEquals(ApiFamilies.ANTHROPIC, _service.getApiFamily(CLAUDE));
		assertEquals(BedrockConverseProviderConfig.DEFAULT_CATALOG_PROVIDER_ID,
			_service.getApiFamily(NOVA, "bedrock"));
		assertEquals(BedrockConverseProviderConfig.DEFAULT_CATALOG_PROVIDER_ID,
			_service.getProviderId(CLAUDE, "bedrock"));
	}

	/**
	 * With the default catalog ID, the catalog knows the Bedrock model IDs as configured.
	 */
	public void testBedrockModelsInCatalog() {
		ModelInfo info = _service.getModel(CLAUDE);
		assertNotNull("The catalog must know " + CLAUDE + ".", info);
		assertTrue(info.capabilities().reasoning());
	}

	/**
	 * The options of the family {@link ApiFamilies#ANTHROPIC} survive a switch to Claude on Bedrock.
	 */
	public void testClaudeOnBedrockKeepsAnthropicOptions() {
		Map<String, Object> options = ModelOptions.forTarget(RUN_OPTIONS,
			_service.getApiFamily(CLAUDE, "bedrock"), _service.getModel(CLAUDE));

		assertEquals("high", options.get(ProviderRequest.THINKING_EFFORT));
		assertEquals(Integer.valueOf(40), options.get(ProviderRequest.TOP_K));
		assertEquals("1h", options.get(ProviderRequest.CACHE_TTL));
	}

	/**
	 * Another model on Bedrock gets none of them.
	 */
	public void testOtherModelOnBedrockGetsNoAnthropicOptions() {
		Map<String, Object> options = ModelOptions.forTarget(RUN_OPTIONS,
			_service.getApiFamily(NOVA, "bedrock"), _service.getModel(NOVA));

		assertNull(options.get(ProviderRequest.THINKING_EFFORT));
		assertNull(options.get(ProviderRequest.TOP_K));
		assertNull(options.get(ProviderRequest.CACHE_TTL));
	}

	/**
	 * The effort levels offered for Claude on Bedrock are the thinking levels.
	 */
	public void testEffortLevelsOfClaudeOnBedrock() {
		assertEquals(EffortLevels.THINKING,
			EffortLevels.forModel(_service.getApiFamily(CLAUDE, "bedrock"), _service.getModel(CLAUDE)));
	}

	private static LLMProviderService.Config<?> serviceConfig(LLMProviderConfig.Config... providers) {
		@SuppressWarnings("unchecked")
		LLMProviderService.Config<LLMProviderService> result =
			(LLMProviderService.Config<LLMProviderService>) TypedConfiguration.newConfigItem(
				LLMProviderService.Config.class);
		for (LLMProviderConfig.Config provider : providers) {
			result.getProviders().put(provider.getName(), provider);
		}
		ModelCatalogConfig catalog = TypedConfiguration.newConfigItem(ModelCatalogConfig.class);
		set(catalog, ModelCatalogConfig.FETCH_REMOTE_CATALOG, Boolean.FALSE);
		set(result, LLMProviderService.Config.MODEL_CATALOG, catalog);
		return result;
	}

	private static LLMProviderConfig.Config bedrock(String name, String... models) {
		BedrockConverseProviderConfig.Config result =
			TypedConfiguration.newConfigItem(BedrockConverseProviderConfig.Config.class);
		set(result, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, BedrockConverseProviderConfig.class);
		result.setName(name);
		set(result, LLMProviderConfig.Config.BASE_URL, "https://bedrock.example.com");
		set(result, LLMProviderConfig.Config.MODELS, List.of(models));
		set(result, BedrockConverseProviderConfig.Config.API_KEY, FixedCredentials.secret("test"));
		return result;
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}
}
