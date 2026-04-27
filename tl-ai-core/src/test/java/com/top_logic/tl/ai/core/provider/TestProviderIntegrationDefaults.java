/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import junit.framework.TestCase;

/**
 * Tests for provider integration defaults used by local/custom providers.
 */
public class TestProviderIntegrationDefaults extends TestCase {

	/** */
	public void testNormalizeProviderBaseUrl() {
		assertEquals("https://api.openai.com",
			LLMProviderRegistry.normalizeProviderBaseUrl("https://api.openai.com/v1"));
		assertEquals("http://localhost:11434",
			LLMProviderRegistry.normalizeProviderBaseUrl("http://localhost:11434/v1/"));
		assertEquals("http://localhost:8080",
			LLMProviderRegistry.normalizeProviderBaseUrl("http://localhost:8080/"));
		assertNull(LLMProviderRegistry.normalizeProviderBaseUrl(null));
	}

	/** */
	public void testOpenAICompatibleProviderNameMatching() {
		assertTrue(LLMProviderRegistry.isOpenAICompatibleProviderName("openai"));
		assertTrue(LLMProviderRegistry.isOpenAICompatibleProviderName("openai-compatible"));
		assertTrue(LLMProviderRegistry.isOpenAICompatibleProviderName("local-ollama"));
		assertTrue(LLMProviderRegistry.isOpenAICompatibleProviderName("custom-provider"));
		assertFalse(LLMProviderRegistry.isOpenAICompatibleProviderName("anthropic"));
	}

	/** */
	public void testResolveBaseUrlForCustomProviders() {
		assertEquals("http://localhost:11434",
			ProviderModelDiscovery.resolveBaseUrl("local-ollama", null));
		assertEquals("http://localhost:11434",
			ProviderModelDiscovery.resolveBaseUrl("openai-compatible", null));
		assertEquals("https://api.openai.com",
			ProviderModelDiscovery.resolveBaseUrl("openai", null));
		assertEquals("http://127.0.0.1:1234",
			ProviderModelDiscovery.resolveBaseUrl("local-lmstudio", "http://127.0.0.1:1234/v1"));
	}

	/** */
	public void testAuthOptionalForCustomProviders() {
		assertTrue(ProviderModelDiscovery.isAuthOptionalProvider("local-ollama"));
		assertTrue(ProviderModelDiscovery.isAuthOptionalProvider("custom-dev"));
		assertTrue(ProviderModelDiscovery.isAuthOptionalProvider("openai-compatible"));
		assertFalse(ProviderModelDiscovery.isAuthOptionalProvider("openai"));
		assertFalse(ProviderModelDiscovery.isAuthOptionalProvider("anthropic"));
	}
}
