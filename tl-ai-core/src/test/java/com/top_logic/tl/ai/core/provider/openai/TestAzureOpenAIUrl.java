/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.provider.FixedCredentials;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsProvider;

/**
 * Tests for Azure-OpenAI deployment-path URL construction in {@link ChatCompletionsProvider}.
 */
public class TestAzureOpenAIUrl extends TestCase {

	/** Minimal subclass that exposes the protected hooks under test. */
	static class TestableProvider extends ChatCompletionsProvider {

		/** Creates a provider with Azure-style path template and api-version query param. */
		TestableProvider(String apiVersion) {
			super(FixedCredentials.NONE, "https://example.openai.azure.com",
				30, 600, Map.of(),
				"/openai/deployments/{model}/chat/completions",
				Map.of("api-version", apiVersion));
		}

		/** Creates a plain provider using the default path {@code /v1/chat/completions}. */
		TestableProvider() {
			super(FixedCredentials.NONE, "https://example.com");
		}

		/** Expose {@link #endpointPath} for testing. */
		String exposedEndpointPath(ProviderRequest request) {
			return endpointPath(request);
		}

		@Override
		protected void processStream(InputStream input, StreamHandler handler)
				throws IOException, ProviderException {
			// no-op for tests
		}

		@Override
		protected void handleErrorResponse(HttpResponse<InputStream> response)
				throws ProviderException {
			// no-op for tests
		}
	}

	private ProviderRequest makeRequest(String model) {
		return new ProviderRequest(model, null, List.of(), List.of(), 100);
	}

	/** */
	public void testAzureEndpointPathEmbedsDeploymentName() {
		TestableProvider provider = new TestableProvider("2024-10-21");
		String path = provider.exposedEndpointPath(makeRequest("gpt-5.1"));
		assertEquals("/openai/deployments/gpt-5.1/chat/completions?api-version=2024-10-21", path);
	}

	/** */
	public void testDefaultPathIsUnchanged() {
		TestableProvider provider = new TestableProvider();
		String path = provider.exposedEndpointPath(makeRequest("gpt-5.1"));
		assertEquals("/v1/chat/completions", path);
	}
}
