/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelsDevFetcher;

/**
 * The providers tl-ai-core ships in its configuration: the four public providers, each with its
 * secret and with models the bundled catalog knows as able to call tools.
 *
 * <p>
 * Read from the configuration of the test application, so the aliases of {@code tl-ai-core.conf.xml}
 * are expanded as in an application. The expected model lists are the defaults of these aliases;
 * where the variable of an alias is set in the environment, the comparison of that list is skipped.
 * </p>
 */
public class TestShippedProviders extends TestCase {

	/** Provider name, secret name, variable of the models and the models used without it. */
	private record Shipped(String name, String secret, String modelsVariable, List<String> models) {
		// Plain value.
	}

	private static final List<Shipped> SHIPPED = List.of(
		new Shipped("anthropic", "tl_anthropic_api_key", "tl_anthropic_models",
			List.of("claude-sonnet-4-6", "claude-opus-4-7", "claude-haiku-4-5")),
		new Shipped("openai", "tl_openai_api_key", "tl_openai_models",
			List.of("gpt-5.5", "gpt-5.4-mini")),
		new Shipped("mistral", "tl_mistral_api_key", "tl_mistral_models",
			List.of("mistral-medium-latest", "mistral-small-latest")),
		new Shipped("xai", "tl_xai_api_key", "tl_xai_models",
			List.of("grok-4.3", "grok-4-1-fast")));

	/** The variable of the default model of the agents. */
	private static final String DEFAULT_MODEL_VARIABLE = "tl_ai_default_model";

	/** The four providers, in this order, each referencing its secret by name. */
	public void testTheProvidersAndTheirSecrets() throws ConfigurationException {
		Map<String, LLMProviderConfig.Config> providers = providers();
		assertEquals(SHIPPED.stream().map(Shipped::name).toList(), List.copyOf(providers.keySet()));
		for (Shipped shipped : SHIPPED) {
			LLMProviderConfig.Config provider = providers.get(shipped.name());
			NamedSecretSource.Config secret = apiKey(provider);
			assertEquals(shipped.name(), shipped.secret(), secret.getName());
		}
	}

	/**
	 * The model lists survive the alias expansion as lists: a default containing commas reaches the
	 * provider as one model per entry.
	 */
	public void testTheModelsOfTheAliasDefaults() throws ConfigurationException {
		Map<String, LLMProviderConfig.Config> providers = providers();
		for (Shipped shipped : SHIPPED) {
			if (isSet(shipped.modelsVariable())) {
				continue;
			}
			assertEquals(shipped.name(), shipped.models(), providers.get(shipped.name()).getModels());
		}
	}

	/** Every model a shipped provider exposes is one the bundled catalog lists as calling tools. */
	public void testTheModelsAreInTheCatalogAndCallTools() throws ConfigurationException {
		for (LLMProviderConfig.Config provider : providers().values()) {
			String catalogId = provider.getCatalogProviderId();
			assertNotNull(provider.getName(), catalogId);
			Map<String, ModelInfo> known =
				ModelsDevFetcher.loadSnapshot(Map.of(catalogId, Set.copyOf(provider.getModels())));
			for (String model : provider.getModels()) {
				ModelInfo info = known.get(model);
				assertNotNull("Model '" + model + "' of provider '" + provider.getName()
					+ "' is not in the bundled catalog.", info);
				assertTrue("Model '" + model + "' cannot call tools.", info.capabilities().toolCall());
			}
		}
	}

	/** Without its variable, the default model of the agents is empty, that is: not configured. */
	public void testTheDefaultModelIsEmptyWithoutItsVariable() throws ConfigurationException {
		if (isSet(DEFAULT_MODEL_VARIABLE)) {
			return;
		}
		AgentRuntimeService.Config<?> runtime = (AgentRuntimeService.Config<?>) ApplicationConfig.getInstance()
			.getServiceConfiguration(AgentRuntimeService.class);
		String model = runtime.getDefaultModel();
		assertTrue("Default model: '" + model + "'", model == null || model.isBlank());
	}

	private static boolean isSet(String variable) {
		return System.getProperty(variable) != null || System.getenv(variable) != null;
	}

	/**
	 * The API key of the given provider, a property of the same name in each provider type.
	 */
	private static NamedSecretSource.Config apiKey(LLMProviderConfig.Config provider) {
		Object apiKey = provider.value(provider.descriptor().getProperty(AnthropicProviderConfig.Config.API_KEY));
		assertTrue(provider.getName() + ": " + apiKey, apiKey instanceof NamedSecretSource.Config);
		return (NamedSecretSource.Config) apiKey;
	}

	private static Map<String, LLMProviderConfig.Config> providers() throws ConfigurationException {
		LLMProviderService.Config<?> config = (LLMProviderService.Config<?>) ApplicationConfig.getInstance()
			.getServiceConfiguration(LLMProviderService.class);
		return config.getProviders();
	}

	/**
	 * Reading the application configuration needs the configuration of the test application; the
	 * provider configurations resolve their types through the {@code TypeIndex}.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestShippedProviders.class));
	}

}
