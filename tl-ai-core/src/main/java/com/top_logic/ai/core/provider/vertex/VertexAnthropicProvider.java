/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider.vertex;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Map;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.ai.core.provider.ProviderException;
import com.top_logic.ai.core.provider.ProviderRequest;
import com.top_logic.ai.core.provider.anthropic.AnthropicMessagesProvider;
import com.top_logic.ai.core.provider.anthropic.AnthropicRequestBuilder;

/**
 * {@link AnthropicMessagesProvider} for one Claude model on Google Vertex AI.
 *
 * <p>
 * Vertex AI speaks the Messages API with three differences: the model is named in the path of its
 * endpoint instead of the body, the API version is sent in the body ({@code anthropic_version})
 * instead of a header, and an error Google Cloud answers itself has Google's form. The stream is
 * the one of the Anthropic API.
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

	private final String _path;

	/**
	 * Creates a {@link VertexAnthropicProvider}.
	 *
	 * @param credentials
	 *        Authentication of the requests.
	 * @param baseUrl
	 *        The URL the path is appended to.
	 * @param path
	 *        The path of the streaming endpoint of the model.
	 * @param anthropicVersion
	 *        The version of the Messages API sent in the body.
	 * @param connectTimeoutSeconds
	 *        TCP connect timeout in seconds.
	 * @param requestTimeoutSeconds
	 *        Per-request timeout in seconds.
	 * @param customHeaders
	 *        Additional HTTP headers.
	 * @param catalogProviderId
	 *        The catalog provider ID stamped onto the thinking blocks.
	 */
	public VertexAnthropicProvider(CredentialProvider credentials, String baseUrl, String path,
			String anthropicVersion, int connectTimeoutSeconds, int requestTimeoutSeconds,
			Map<String, String> customHeaders, String catalogProviderId) {
		super(credentials, baseUrl, anthropicVersion, connectTimeoutSeconds, requestTimeoutSeconds, customHeaders,
			catalogProviderId);
		_path = path;
	}

	/**
	 * Creates a {@link VertexAnthropicProvider} with the given {@link HttpClient}.
	 *
	 * @see #VertexAnthropicProvider(CredentialProvider, String, String, String, int, int, Map, String)
	 */
	public VertexAnthropicProvider(CredentialProvider credentials, String baseUrl, String path,
			String anthropicVersion, HttpClient httpClient, String catalogProviderId) {
		super(credentials, baseUrl, anthropicVersion, httpClient, catalogProviderId);
		_path = path;
	}

	@Override
	protected String endpointPath(ProviderRequest request) {
		return _path;
	}

	@Override
	protected void addExtraHeaders(HttpRequest.Builder builder) {
		// The API version is sent in the body.
	}

	@Override
	protected String buildRequestJson(ProviderRequest request) throws ProviderException {
		return AnthropicRequestBuilder.buildRequestJson(request, apiVersion());
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
