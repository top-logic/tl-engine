/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.ai.core.agent.AgentRuntimeService;

/**
 * Puts one {@link ScriptedProvider} in the place of the configured providers of the running
 * {@link LLMProviderService}, for a test that runs on the services of the module system.
 *
 * <p>
 * Changes the service configurations of the application and restarts the provider service, which
 * also restarts every running service that depends on it, the policy service and the agent runtime
 * among them. Then no configured key can make a real provider available, and no request leaves the
 * test. {@link #restore()} puts the configurations back and restarts again.
 * </p>
 *
 * <p>
 * The default model of the agent runtime is set as well, so that a {@code tl_ai_default_model} of
 * the environment cannot name a model the scripted provider does not serve. The catalog is switched
 * off, so that the start sends no request for model metadata.
 * </p>
 */
public final class ScriptedProviderOverride {

	/** The name of the one provider. */
	public static final String PROVIDER = "scripted";

	private final Map<String, LLMProviderConfig.Config> _providers;

	private final LLMProviderService.ModelCatalogConfig _catalog;

	private final String _defaultModel;

	private ScriptedProviderOverride(Map<String, LLMProviderConfig.Config> providers,
			LLMProviderService.ModelCatalogConfig catalog, String defaultModel) {
		_providers = providers;
		_catalog = catalog;
		_defaultModel = defaultModel;
	}

	/**
	 * Serves the given models from one {@link ScriptedProvider} until {@link #restore()}.
	 *
	 * @param defaultModel
	 *        The default model of the agent runtime; one of {@code models}.
	 * @param models
	 *        The models the provider serves. A name the tool access policy of the application names
	 *        must be among them, or the policy service refuses its restart.
	 * @return The handle that puts the configured providers back.
	 */
	public static ScriptedProviderOverride install(String defaultModel, List<String> models)
			throws ConfigurationException, RestartException {
		LLMProviderService.Config<?> providers = providerConfig();
		AgentRuntimeService.Config<?> runtime = runtimeConfig();
		ScriptedProviderOverride result = new ScriptedProviderOverride(
			new LinkedHashMap<>(providers.getProviders()), providers.getModelCatalog(),
			runtime.getDefaultModel());

		set(providers, LLMProviderService.Config.PROVIDERS, Map.of(PROVIDER, scriptedProvider(models)));
		set(providers, LLMProviderService.Config.MODEL_CATALOG, null);
		set(runtime, AgentRuntimeService.Config.DEFAULT_MODEL, defaultModel);
		try {
			ModuleUtil.INSTANCE.restart(LLMProviderService.Module.INSTANCE);
		} catch (RestartException | RuntimeException ex) {
			// A test whose setUp fails gets no tearDown. Without this, the later tests of the same JVM
			// would run on the scripted provider, or on services that did not start.
			try {
				result.restore();
			} catch (ConfigurationException | RestartException | RuntimeException restoreFailure) {
				ex.addSuppressed(restoreFailure);
			}
			throw ex;
		}
		return result;
	}

	/**
	 * Puts the configured providers and the default model back, and restarts the provider service.
	 */
	public void restore() throws ConfigurationException, RestartException {
		set(providerConfig(), LLMProviderService.Config.PROVIDERS, _providers);
		set(providerConfig(), LLMProviderService.Config.MODEL_CATALOG, _catalog);
		set(runtimeConfig(), AgentRuntimeService.Config.DEFAULT_MODEL, _defaultModel);
		ModuleUtil.INSTANCE.restart(LLMProviderService.Module.INSTANCE);
	}

	private static LLMProviderConfig.Config scriptedProvider(List<String> models) {
		ScriptedProvider.Config result = TypedConfiguration.newConfigItem(ScriptedProvider.Config.class);
		set(result, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, ScriptedProvider.class);
		result.setName(PROVIDER);
		set(result, LLMProviderConfig.Config.MODELS, List.copyOf(models));
		return result;
	}

	private static LLMProviderService.Config<?> providerConfig() throws ConfigurationException {
		return (LLMProviderService.Config<?>) ApplicationConfig.getInstance()
			.getServiceConfiguration(LLMProviderService.class);
	}

	private static AgentRuntimeService.Config<?> runtimeConfig() throws ConfigurationException {
		return (AgentRuntimeService.Config<?>) ApplicationConfig.getInstance()
			.getServiceConfiguration(AgentRuntimeService.class);
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}
}
