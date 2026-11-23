/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider.vertex;

import java.net.http.HttpRequest;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.ai.core.provider.ProviderException;
import com.top_logic.ai.core.provider.ProviderRequest;
import com.top_logic.ai.core.provider.ToolChoice;
import com.top_logic.ai.core.provider.anthropic.AnthropicMessagesProvider;
import com.top_logic.ai.core.provider.anthropic.AnthropicRequestBuilder;

/**
 * {@link AnthropicMessagesProvider} for the Claude models on Google Vertex AI.
 *
 * <p>
 * Vertex AI speaks the Messages API with three differences: the model is named in the path of its
 * endpoint instead of the body, the API version is sent in the body ({@code anthropic_version})
 * instead of a header, and an error Google Cloud answers itself has Google's form. The stream is
 * the one of the Anthropic API.
 * </p>
 *
 * <p>
 * The {@link ToolChoice} of a request is {@link ToolChoice#restrict(ProviderRequest, Set, String)
 * restricted} to the ones the endpoint accepts before it is sent.
 * </p>
 */
public class VertexAnthropicProvider extends AnthropicMessagesProvider {

	/**
	 * The {@link ProviderException.Kind} of each Google {@code error.status}.
	 */
	private static final Map<String, ProviderException.Kind> GOOGLE_STATUS_MAP = Map.ofEntries(
		Map.entry("UNAUTHENTICATED", ProviderException.Kind.AUTHENTICATION),
		Map.entry("PERMISSION_DENIED", ProviderException.Kind.AUTHENTICATION),
		Map.entry("RESOURCE_EXHAUSTED", ProviderException.Kind.RATE_LIMITED),
		Map.entry("UNAVAILABLE", ProviderException.Kind.OVERLOADED),
		Map.entry("INTERNAL", ProviderException.Kind.OVERLOADED),
		Map.entry("DEADLINE_EXCEEDED", ProviderException.Kind.OVERLOADED),
		Map.entry("INVALID_ARGUMENT", ProviderException.Kind.INVALID_REQUEST),
		Map.entry("FAILED_PRECONDITION", ProviderException.Kind.INVALID_REQUEST),
		Map.entry("OUT_OF_RANGE", ProviderException.Kind.INVALID_REQUEST),
		Map.entry("NOT_FOUND", ProviderException.Kind.INVALID_REQUEST));

	/** Method of the streaming endpoint of a model that answers in its publisher's dialect. */
	private static final String STREAM_RAW_PREDICT = ":streamRawPredict";

	private final String _name;

	private final String _modelsPath;

	private final Set<ToolChoice> _toolChoices;

	/**
	 * Creates a {@link VertexAnthropicProvider}.
	 *
	 * @param name
	 *        The name of the configured provider, for error messages.
	 * @param credentials
	 *        Authentication of the requests.
	 * @param baseUrl
	 *        The URL the path of a model is appended to.
	 * @param modelsPath
	 *        The path of the models of the publisher, to which the model name is appended.
	 * @param anthropicVersion
	 *        The version of the Messages API sent in the body.
	 * @param toolChoices
	 *        The tool choices the endpoint accepts.
	 * @param connectTimeoutSeconds
	 *        TCP connect timeout in seconds.
	 * @param requestTimeoutSeconds
	 *        Per-request timeout in seconds.
	 * @param customHeaders
	 *        Additional HTTP headers.
	 * @param catalogProviderId
	 *        The catalog provider ID stamped onto the thinking blocks.
	 */
	public VertexAnthropicProvider(String name, CredentialProvider credentials, String baseUrl, String modelsPath,
			String anthropicVersion, Set<ToolChoice> toolChoices, int connectTimeoutSeconds,
			int requestTimeoutSeconds, Map<String, String> customHeaders, String catalogProviderId) {
		super(credentials, baseUrl, anthropicVersion, connectTimeoutSeconds, requestTimeoutSeconds, customHeaders,
			catalogProviderId);
		_name = name;
		_modelsPath = modelsPath;
		_toolChoices = Set.copyOf(toolChoices);
	}

	@Override
	protected String endpointPath(ProviderRequest request) {
		return _modelsPath + request.model() + STREAM_RAW_PREDICT;
	}

	@Override
	protected void addExtraHeaders(HttpRequest.Builder builder) {
		// The API version is sent in the body.
	}

	@Override
	protected String buildRequestJson(ProviderRequest request) throws ProviderException {
		return AnthropicRequestBuilder.buildRequestJson(ToolChoice.restrict(request, _toolChoices, _name),
			apiVersion());
	}

	/**
	 * An error in Anthropic's form is classified like one of the Anthropic API, an error Google
	 * Cloud answers itself by its {@code error.status}.
	 */
	@Override
	protected ProviderException.Kind classifyError(ErrorBody error, int statusCode) {
		if (error.type().isEmpty()) {
			ProviderException.Kind byStatus = GOOGLE_STATUS_MAP.get(error.status());
			if (byStatus != null) {
				return byStatus;
			}
		}
		return super.classifyError(error, statusCode);
	}
}
