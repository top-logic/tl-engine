/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.xai;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.provider.FixedCredentials;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsProvider;
import com.top_logic.tl.ai.core.provider.local.LocalProviderConfig;
import com.top_logic.tl.ai.core.provider.openai.OpenAICompatibleProviderConfig;
import com.top_logic.tl.ai.core.provider.openai.OpenAIResponsesProvider;

/**
 * Tests for {@link XAIProviderConfig} protocol selection.
 */
public class TestXAIProviderConfig extends TestCase {

	/**
	 * Instantiating the credentials of a provider resolves their configuration types through the
	 * <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestXAIProviderConfig.class)));
	}

	/** */
	public void testChatCompletionsProtocolCreatesChatCompletionsProvider() {
		XAIProviderConfig.Config config = TypedConfiguration.newConfigItem(XAIProviderConfig.Config.class);
		config.setApiKey(FixedCredentials.secret("test-key"));
		config.setProtocol(XAIProviderConfig.Protocol.CHAT_COMPLETIONS);

		XAIProviderConfig providerConfig =
			new XAIProviderConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		LLMProvider provider = providerConfig.createProvider();

		assertInstanceOf(ChatCompletionsProvider.class, provider);
	}

	/** */
	public void testResponsesProtocolCreatesOpenAIResponsesProvider() {
		XAIProviderConfig.Config config = TypedConfiguration.newConfigItem(XAIProviderConfig.Config.class);
		config.setApiKey(FixedCredentials.secret("test-key"));
		config.setProtocol(XAIProviderConfig.Protocol.RESPONSES);

		XAIProviderConfig providerConfig =
			new XAIProviderConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		LLMProvider provider = providerConfig.createProvider();

		assertInstanceOf(OpenAIResponsesProvider.class, provider);
	}

	/** */
	public void testDefaultProtocolIsChatCompletions() {
		XAIProviderConfig.Config config = TypedConfiguration.newConfigItem(XAIProviderConfig.Config.class);
		config.setApiKey(FixedCredentials.secret("test-key"));
		// Protocol not set — default should apply.

		XAIProviderConfig providerConfig =
			new XAIProviderConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		LLMProvider provider = providerConfig.createProvider();

		assertInstanceOf(ChatCompletionsProvider.class, provider);
	}

	/** */
	public void testLocalProviderConfigCreatesChatCompletionsProvider() {
		LocalProviderConfig.Config config = TypedConfiguration.newConfigItem(LocalProviderConfig.Config.class);
		config.setBaseUrl("http://localhost:11434");

		LocalProviderConfig providerConfig =
			new LocalProviderConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		LLMProvider provider = providerConfig.createProvider();

		assertInstanceOf(ChatCompletionsProvider.class, provider);
	}

	/** */
	public void testOpenAICompatibleProviderConfigCreatesChatCompletionsProvider() {
		OpenAICompatibleProviderConfig.Config config =
			TypedConfiguration.newConfigItem(OpenAICompatibleProviderConfig.Config.class);
		config.setBaseUrl("https://custom.example.com");

		OpenAICompatibleProviderConfig providerConfig =
			new OpenAICompatibleProviderConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		LLMProvider provider = providerConfig.createProvider();

		assertInstanceOf(ChatCompletionsProvider.class, provider);
	}

	private static void assertInstanceOf(Class<?> expectedType, Object actual) {
		assertTrue(
			"Expected instance of " + expectedType.getSimpleName()
				+ " but got " + (actual == null ? "null" : actual.getClass().getSimpleName()),
			expectedType.isInstance(actual));
	}
}
