/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.bedrock;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.provider.LLMProvider;

/**
 * Tests for {@link BedrockConverseProviderConfig}.
 */
public class TestBedrockConverseProviderConfig extends TestCase {

	/** */
	public void testCreateProviderReturnsBedrockConverseProvider() {
		BedrockConverseProviderConfig.Config config =
			TypedConfiguration.newConfigItem(BedrockConverseProviderConfig.Config.class);
		config.setBaseUrl("https://genai-nexus.api.corpinter.net");
		config.setApiKey("test-key");
		config.setModels(List.of("claude-sonnet-4.6"));

		BedrockConverseProviderConfig providerConfig =
			new BedrockConverseProviderConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		LLMProvider provider = providerConfig.createProvider();

		assertInstanceOf(BedrockConverseProvider.class, provider);
	}

	/** */
	public void testGetConfiguredModels() {
		BedrockConverseProviderConfig.Config config =
			TypedConfiguration.newConfigItem(BedrockConverseProviderConfig.Config.class);
		config.setBaseUrl("https://genai-nexus.api.corpinter.net");
		config.setModels(List.of("claude-sonnet-4.6"));

		BedrockConverseProviderConfig providerConfig =
			new BedrockConverseProviderConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);

		assertEquals(List.of("claude-sonnet-4.6"), providerConfig.getConfiguredModels());
	}

	/** */
	public void testDiscoverModelsReturnsEmpty() {
		BedrockConverseProviderConfig.Config config =
			TypedConfiguration.newConfigItem(BedrockConverseProviderConfig.Config.class);
		config.setBaseUrl("https://bedrock-runtime.us-east-1.amazonaws.com");

		BedrockConverseProviderConfig providerConfig =
			new BedrockConverseProviderConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);

		assertTrue("discoverModels() must return empty list", providerConfig.discoverModels().isEmpty());
	}

	private static void assertInstanceOf(Class<?> expectedType, Object actual) {
		assertTrue(
			"Expected instance of " + expectedType.getSimpleName()
				+ " but got " + (actual == null ? "null" : actual.getClass().getSimpleName()),
			expectedType.isInstance(actual));
	}
}
