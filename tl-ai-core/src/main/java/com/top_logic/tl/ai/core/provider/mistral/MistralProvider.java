/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.mistral;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsProvider;

/**
 * {@link ChatCompletionsProvider} for the Mistral Chat Completions API.
 *
 * <p>Mistral-specific behaviour:
 * <ul>
 *   <li>Default base URL: {@link #DEFAULT_BASE_URL}.</li>
 *   <li>{@code tool_choice} is always set when tools are present, and {@code REQUIRED} maps
 *       to {@code "any"} (Mistral's non-standard name for {@code "required"}).</li>
 *   <li>Request bodies above 5,000,000 bytes are rejected to guard
 *       against a server-side reverse proxy that hangs on large payloads.</li>
 * </ul>
 * </p>
 */
public class MistralProvider extends ChatCompletionsProvider {

	/** Default API base URL for Mistral. */
	private static final String DEFAULT_BASE_URL = "https://api.mistral.ai";

	/** Creates a {@link MistralProvider}. */
	public MistralProvider(String apiKey) {
		this(apiKey, DEFAULT_BASE_URL);
	}

	/** Creates a {@link MistralProvider}. */
	public MistralProvider(String apiKey, String baseUrl) {
		this(apiKey, baseUrl, defaultHttpClient());
	}

	/** Creates a {@link MistralProvider}. */
	public MistralProvider(String apiKey, String baseUrl, HttpClient httpClient) {
		super(apiKey, baseUrl, httpClient);
	}

	/** Creates a {@link MistralProvider} with configurable timeouts and headers. */
	public MistralProvider(String apiKey, String baseUrl,
			int connectTimeoutSeconds, int requestTimeoutSeconds, Map<String, String> customHeaders) {
		super(apiKey, baseUrl, connectTimeoutSeconds, requestTimeoutSeconds, customHeaders);
	}

	/**
	 * Undocumented Mistral API limit: request bodies above ~32 KB cause the connection to hang
	 * indefinitely with no response. Confirmed via curl — this is a server-side infrastructure
	 * issue (likely a reverse proxy), not a client bug. Guard with a conservative threshold.
	 */
	private static final int MAX_REQUEST_BODY_BYTES = 5_000_000;

	@Override
	protected void validateRequest(String body) throws ProviderException {
		int bodyBytes = body.getBytes(StandardCharsets.UTF_8).length;
		if (bodyBytes > MAX_REQUEST_BODY_BYTES) {
			throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 0,
				"Request body (" + bodyBytes + " bytes) exceeds Mistral's ~32 KB limit. "
					+ "Reduce the system prompt or number of tools, or use a different provider.");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Mistral always sets {@code tool_choice} when tools are present, and maps
	 * {@code REQUIRED} to {@code "any"} (Mistral's non-standard name for {@code "required"}).</p>
	 */
	@Override
	protected String toolChoiceOverride(ProviderRequest request) {
		if (request.toolChoice() != null || !request.tools().isEmpty()) {
			ToolChoice choice = request.toolChoice() != null ? request.toolChoice() : ToolChoice.AUTO;
			return switch (choice) {
				case REQUIRED -> "any";
				case NONE -> "none";
				case AUTO -> "auto";
			};
		}
		return null;
	}
}
