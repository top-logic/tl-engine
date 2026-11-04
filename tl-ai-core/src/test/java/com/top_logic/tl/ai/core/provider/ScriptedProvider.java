/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.tl.ai.core.provider.ScriptedModel.Script;

/**
 * A provider whose answers a test writes, configured as a provider of {@link LLMProviderService}:
 * an agent run is tested in an ordinary build, at no cost and with no key.
 *
 * <p>
 * It is a configured {@link LLMProviderConfig}, not a {@link ScriptedModel} handed to an
 * {@link com.top_logic.tl.ai.core.agent.AgentLoop}, and that is the point. A test of an agent - its
 * definition, its skills, its tools - has to go through the same route a real run takes: the agent
 * definition, the tool registry, the approval gate, the transaction scope, the model routing and
 * failover of {@link LLMProviderService}. A fake wired in below all of that would test a loop nobody
 * ships.
 * </p>
 *
 * <p>
 * The script is {@link #install(Script) installed} statically, because a provider is created once at
 * service startup and the test runs it on its own thread. A script therefore dispatches on what it is
 * asked - the prompt in the request, the {@link ProviderRequest#model() model} it is addressed to and
 * how many assistant turns have already happened - rather than on being re-installed between the
 * calls of one run. Every provider entry of this class answers from the same installed script, and
 * {@link #requests()} returns what all of them were sent.
 * </p>
 *
 * <p>
 * Configured {@link LLMProviderConfig.Config#getCredentials() credentials} are honored: while they
 * are not available, the provider is inactive, as a real one would be.
 * </p>
 */
public class ScriptedProvider implements LLMProviderConfig {

	/** Configuration of a {@link ScriptedProvider}. */
	public interface Config extends LLMProviderConfig.Config {
		// No properties of its own: what it answers is the installed script, not configuration.
	}

	/**
	 * Refuses rather than answers, so that a test which forgot to install a script fails saying so
	 * instead of timing out or passing on an empty answer.
	 */
	private static final Script NOT_INSTALLED = (request, turn) -> {
		throw new IllegalStateException("No script installed. Call ScriptedProvider.install(...)"
			+ " before running anything against the model '" + request.model() + "'.");
	};

	private static volatile ScriptedModel _model = new ScriptedModel(NOT_INSTALLED);

	private final Config _config;

	private final CredentialProvider _credentials;

	/** Creates a {@link ScriptedProvider} from configuration. */
	@CalledByReflection
	public ScriptedProvider(InstantiationContext context, Config config) {
		_config = config;
		_credentials = LLMProviderConfig.createCredentials(context, config);
	}

	/**
	 * Installs the script every subsequent call answers from, and forgets the requests recorded so
	 * far.
	 */
	public static void install(Script script) {
		_model = new ScriptedModel(script);
	}

	/**
	 * Every request the providers of this class received since the last {@link #install(Script)}, in
	 * the order they received them.
	 */
	public static List<ProviderRequest> requests() {
		return _model.requests();
	}

	@Override
	public CredentialProvider getCredentials() {
		return _credentials;
	}

	@Override
	public LLMProvider createProvider() {
		return new LLMProvider() {
			@Override
			public void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
				_model.stream(request, handler);
			}

			@Override
			public boolean isAvailable() {
				return ScriptedProvider.this.isAvailable();
			}
		};
	}

	@Override
	public List<String> discoverModels() {
		// Nothing to discover: what this provider serves is whatever the configuration allows it to.
		return List.of();
	}

	@Override
	public List<String> getConfiguredModels() {
		List<String> models = _config.getModels();
		return models != null ? models : List.of();
	}

	@Override
	public String getCatalogProviderId() {
		// No remote catalogue knows a scripted model unless the configuration says so; its metadata
		// usually comes from <model-overrides>.
		return _config.getCatalogProviderId();
	}
}
