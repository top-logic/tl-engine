/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.NoCredentials;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;

/**
 * The Anthropic Messages API the {@link LLMProxyServlet} forwards the requests of the workspaces
 * to, with the credentials it adds to them.
 *
 * @param baseUrl
 *        The URL of the API without the version path, e.g. {@code https://api.anthropic.com}.
 * @param credentials
 *        The credentials of the provider, e.g. its API key sent as
 *        {@link LLMProxyHandler#X_API_KEY}. They are read for each request, so that a rotated key
 *        is used without restart.
 * @param version
 *        The {@link LLMProxyHandler#ANTHROPIC_VERSION} sent when the client sends none.
 * @param connectTimeout
 *        How long to wait for a connection to the API.
 * @param requestTimeout
 *        How long to wait for the API to answer a request.
 * @param customHeaders
 *        Further headers sent with every request.
 */
public record LLMUpstream(String baseUrl, CredentialProvider credentials, String version, Duration connectTimeout,
		Duration requestTimeout, Map<String, String> customHeaders) {

	/**
	 * Creates a {@link LLMUpstream}.
	 */
	public LLMUpstream {
		Objects.requireNonNull(baseUrl, "baseUrl");
		Objects.requireNonNull(credentials, "credentials");
		Objects.requireNonNull(version, "version");
		Objects.requireNonNull(connectTimeout, "connectTimeout");
		Objects.requireNonNull(requestTimeout, "requestTimeout");
		customHeaders = customHeaders == null ? Map.of() : Map.copyOf(customHeaders);
	}

	/**
	 * The upstream of a started Anthropic provider, authenticated with the provider's credentials.
	 *
	 * @return The upstream, {@code null} if the provider has neither API key nor credentials.
	 */
	public static LLMUpstream fromProvider(AnthropicProviderConfig provider) {
		CredentialProvider credentials = provider.getCredentials();
		if (credentials instanceof NoCredentials) {
			return null;
		}
		AnthropicProviderConfig.Config config = provider.getConfig();
		return new LLMUpstream(
			ModelDiscoveryHelper.normalizeBaseUrl(config.getBaseUrl()),
			credentials,
			config.getVersion(),
			Duration.ofSeconds(config.getConnectTimeout()),
			Duration.ofSeconds(config.getRequestTimeout()),
			config.getCustomHeaders());
	}

	@Override
	public String toString() {
		return "LLMUpstream[" + baseUrl + ", version " + version + "]";
	}

}
