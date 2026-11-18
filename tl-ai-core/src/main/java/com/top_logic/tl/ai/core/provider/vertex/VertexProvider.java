/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.vertex;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.provider.ToolChoice;

/**
 * {@link LLMProvider} for the models of a {@link VertexProviderConfig}.
 *
 * <p>
 * Each model has an endpoint of its own, in the dialect of its publisher; a request is passed to
 * the provider of its model, after its {@link ToolChoice} is
 * {@link ToolChoice#restrict(ProviderRequest, Set, String) restricted} to the ones the endpoint
 * accepts.
 * </p>
 */
public class VertexProvider implements LLMProvider {

	private final String _name;

	private final Set<ToolChoice> _toolChoices;

	private final Map<String, LLMProvider> _models;

	private final CredentialProvider _credentials;

	/**
	 * Creates a {@link VertexProvider}.
	 *
	 * @param name
	 *        The name of the configured provider, for error messages.
	 * @param toolChoices
	 *        The tool choices the endpoint accepts.
	 * @param models
	 *        The provider of each model.
	 * @param credentials
	 *        The credentials the model providers authenticate with.
	 */
	public VertexProvider(String name, Set<ToolChoice> toolChoices, Map<String, LLMProvider> models,
			CredentialProvider credentials) {
		_name = name;
		_toolChoices = Set.copyOf(toolChoices);
		_models = Map.copyOf(models);
		_credentials = Objects.requireNonNull(credentials, "Credentials must not be null.");
	}

	@Override
	public void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
		LLMProvider model = _models.get(request.model());
		if (model == null) {
			throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 0,
				"The Google Vertex AI provider '" + _name + "' does not serve the model '" + request.model()
					+ "'. It serves: " + _models.keySet() + ".");
		}
		model.stream(ToolChoice.restrict(request, _toolChoices, _name), handler);
	}

	@Override
	public boolean isAvailable() {
		return _credentials.isAvailable();
	}
}
