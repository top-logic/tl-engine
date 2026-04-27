/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.xai;

import java.net.http.HttpClient;

import com.top_logic.tl.ai.core.provider.openai.OpenAIProvider;

/**
 * xAI provider implementation.
 *
 * <p>
 * xAI's inference API is OpenAI-compatible. Therefore this provider reuses the robust
 * {@link OpenAIProvider} request/stream/error handling and only changes the default base URL.
 * </p>
 */
public class XAIProvider extends OpenAIProvider {

	private static final String DEFAULT_BASE_URL = "https://api.x.ai";

	/**
	 * Creates an {@link XAIProvider} using xAI default endpoint.
	 */
	public XAIProvider(String apiKey) {
		super(apiKey, DEFAULT_BASE_URL);
	}

	/**
	 * Creates an {@link XAIProvider} using explicit endpoint.
	 */
	public XAIProvider(String apiKey, String baseUrl) {
		super(apiKey, baseUrl);
	}

	/**
	 * Creates an {@link XAIProvider} with custom {@link HttpClient} for tests.
	 */
	public XAIProvider(String apiKey, String baseUrl, HttpClient httpClient) {
		super(apiKey, baseUrl, httpClient);
	}
}
