/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.provider.AuthScheme;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsProvider;

/**
 * Tests for Azure-OpenAI deployment-path URL construction and auth-scheme switching in
 * {@link ChatCompletionsProvider}.
 */
public class TestAzureOpenAIUrlAndAuth extends TestCase {

	/** Minimal subclass that exposes the protected hooks under test. */
	static class TestableProvider extends ChatCompletionsProvider {

		/** Creates a provider with Azure-style path template and api-version query param. */
		TestableProvider(String apiKey, AuthScheme authScheme) {
			super(apiKey, "https://example.openai.azure.com",
				30, 600, Map.of(),
				"/openai/deployments/{model}/chat/completions",
				Map.of("api-version", "2024-10-21"),
				authScheme);
		}

		/** Creates a plain provider using the default path {@code /v1/chat/completions}. */
		TestableProvider() {
			super("key", "https://example.com");
		}

		/** Expose {@link #endpointPath} for testing. */
		String exposedEndpointPath(ProviderRequest request) {
			return endpointPath(request);
		}

		/** Expose {@link #addAuthHeaders} for testing. */
		void exposedAddAuthHeaders(HttpRequest.Builder builder) {
			addAuthHeaders(builder);
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
		TestableProvider provider = new TestableProvider("my-key", AuthScheme.BEARER);
		String path = provider.exposedEndpointPath(makeRequest("gpt-5.1"));
		assertEquals("/openai/deployments/gpt-5.1/chat/completions?api-version=2024-10-21", path);
	}

	/** */
	public void testDefaultPathIsUnchanged() {
		TestableProvider provider = new TestableProvider();
		String path = provider.exposedEndpointPath(makeRequest("gpt-5.1"));
		assertEquals("/v1/chat/completions", path);
	}

	/** */
	public void testBearerAuthAddsAuthorizationHeader() {
		TestableProvider provider = new TestableProvider("secret-bearer", AuthScheme.BEARER);
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("https://x"));
		provider.exposedAddAuthHeaders(builder);
		HttpRequest req = builder.build();
		assertTrue("Authorization header must start with 'Bearer '",
			req.headers().firstValue("Authorization").orElse("").startsWith("Bearer "));
		assertFalse("No api-key header expected for BEARER",
			req.headers().firstValue("api-key").isPresent());
	}

	/** */
	public void testApiKeyAuthAddsApiKeyHeader() {
		TestableProvider provider = new TestableProvider("secret-key", AuthScheme.API_KEY);
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("https://x"));
		provider.exposedAddAuthHeaders(builder);
		HttpRequest req = builder.build();
		assertEquals("secret-key", req.headers().firstValue("api-key").orElse(null));
		assertFalse("No Authorization header expected for API_KEY",
			req.headers().firstValue("Authorization").isPresent());
	}
}
