/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.providers;

import java.util.Set;

import junit.framework.TestCase;

/**
 * Tests for {@link AnthropicChatModelFactory#anthropicModelCapabilities(String)}.
 */
public class TestAnthropicModelCapabilities extends TestCase {

	/**
	 * Opus 4.6 supports both enabled and adaptive thinking.
	 */
	public void testOpus46HasAdaptiveAndEnabled() {
		Set<ModelCapability> caps = AnthropicChatModelFactory.anthropicModelCapabilities("claude-opus-4-6-20260401");
		assertTrue(caps.contains(ModelCapability.THINKING_ENABLED));
		assertTrue(caps.contains(ModelCapability.THINKING_ADAPTIVE));
	}

	/**
	 * Sonnet 4.6 supports both enabled and adaptive thinking.
	 */
	public void testSonnet46HasAdaptiveAndEnabled() {
		Set<ModelCapability> caps = AnthropicChatModelFactory.anthropicModelCapabilities("claude-sonnet-4-6-20260401");
		assertTrue(caps.contains(ModelCapability.THINKING_ENABLED));
		assertTrue(caps.contains(ModelCapability.THINKING_ADAPTIVE));
	}

	/**
	 * Haiku 4.5 supports enabled thinking only.
	 */
	public void testHaiku45HasEnabledOnly() {
		Set<ModelCapability> caps = AnthropicChatModelFactory.anthropicModelCapabilities("claude-haiku-4-5-20250301");
		assertTrue(caps.contains(ModelCapability.THINKING_ENABLED));
		assertFalse(caps.contains(ModelCapability.THINKING_ADAPTIVE));
	}

	/**
	 * Sonnet 4 supports enabled thinking only.
	 */
	public void testSonnet4HasEnabledOnly() {
		Set<ModelCapability> caps = AnthropicChatModelFactory.anthropicModelCapabilities("claude-sonnet-4-20250514");
		assertTrue(caps.contains(ModelCapability.THINKING_ENABLED));
		assertFalse(caps.contains(ModelCapability.THINKING_ADAPTIVE));
	}

	/**
	 * Opus 4.5 supports enabled thinking only.
	 */
	public void testOpus45HasEnabledOnly() {
		Set<ModelCapability> caps = AnthropicChatModelFactory.anthropicModelCapabilities("claude-opus-4-5-20250501");
		assertTrue(caps.contains(ModelCapability.THINKING_ENABLED));
		assertFalse(caps.contains(ModelCapability.THINKING_ADAPTIVE));
	}

	/**
	 * Claude 3.7 Sonnet supports enabled thinking only.
	 */
	public void testSonnet37HasEnabledOnly() {
		Set<ModelCapability> caps = AnthropicChatModelFactory.anthropicModelCapabilities("claude-3-7-sonnet-20250219");
		assertTrue(caps.contains(ModelCapability.THINKING_ENABLED));
		assertFalse(caps.contains(ModelCapability.THINKING_ADAPTIVE));
	}

	/**
	 * Claude 3.5 Sonnet does not support thinking.
	 */
	public void testSonnet35HasNoThinking() {
		Set<ModelCapability> caps = AnthropicChatModelFactory.anthropicModelCapabilities("claude-3-5-sonnet-20241022");
		assertTrue(caps.isEmpty());
	}

	/**
	 * Unknown model returns empty capabilities.
	 */
	public void testUnknownModelHasNoThinking() {
		Set<ModelCapability> caps = AnthropicChatModelFactory.anthropicModelCapabilities("some-unknown-model");
		assertTrue(caps.isEmpty());
	}
}
