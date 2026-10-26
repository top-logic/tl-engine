/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;

/**
 * The Anthropic Messages API the {@link LLMProxyServlet} forwards the requests of the workspaces
 * to, with the API key it adds to them.
 *
 * <p>
 * The {@link #toString()} of an upstream leaves the key out, so that printing one never leaks it.
 * </p>
 *
 * @param baseUrl
 *        The URL of the API without the version path, e.g. {@code https://api.anthropic.com}.
 * @param apiKey
 *        The API key sent as {@link LLMProxyHandler#X_API_KEY}.
 * @param version
 *        The {@link LLMProxyHandler#ANTHROPIC_VERSION} sent when the client sends none.
 * @param connectTimeout
 *        How long to wait for a connection to the API.
 * @param requestTimeout
 *        How long to wait for the API to answer a request.
 * @param customHeaders
 *        Further headers sent with every request.
 */
public record LLMUpstream(String baseUrl, String apiKey, String version, Duration connectTimeout,
		Duration requestTimeout, Map<String, String> customHeaders) {

	/**
	 * Creates a {@link LLMUpstream}.
	 */
	public LLMUpstream {
		Objects.requireNonNull(baseUrl, "baseUrl");
		Objects.requireNonNull(apiKey, "apiKey");
		Objects.requireNonNull(version, "version");
		Objects.requireNonNull(connectTimeout, "connectTimeout");
		Objects.requireNonNull(requestTimeout, "requestTimeout");
		customHeaders = customHeaders == null ? Map.of() : Map.copyOf(customHeaders);
	}

	/**
	 * The upstream described by the configuration of an Anthropic provider.
	 *
	 * @return The upstream, {@code null} if the provider has no API key.
	 */
	public static LLMUpstream fromProvider(AnthropicProviderConfig.Config provider) {
		String apiKey = provider.getApiKey();
		if (apiKey == null || apiKey.isBlank()) {
			return null;
		}
		return new LLMUpstream(
			ModelDiscoveryHelper.normalizeBaseUrl(provider.getBaseUrl()),
			apiKey.strip(),
			provider.getVersion(),
			Duration.ofSeconds(provider.getConnectTimeout()),
			Duration.ofSeconds(provider.getRequestTimeout()),
			provider.getCustomHeaders());
	}

	@Override
	public String toString() {
		// Keeps the API key out of anything that prints an upstream.
		return "LLMUpstream[" + baseUrl + ", version " + version + "]";
	}

}
