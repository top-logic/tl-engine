/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.providers;

import java.util.Set;

import junit.framework.TestCase;

/**
 * Tests for {@link OpenAIChatModelFactory#openAIModelCapabilities(String)}.
 */
public class TestOpenAIModelCapabilities extends TestCase {

	/**
	 * {@code o1} supports reasoning effort.
	 */
	public void testO1HasReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("o1");
		assertTrue(caps.contains(ModelCapability.REASONING_EFFORT));
	}

	/**
	 * {@code o1-2024-12-17} (versioned o1) supports reasoning effort.
	 */
	public void testO1VersionedHasReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("o1-2024-12-17");
		assertTrue(caps.contains(ModelCapability.REASONING_EFFORT));
	}

	/**
	 * {@code o1-mini} does not support reasoning effort.
	 */
	public void testO1MiniHasNoReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("o1-mini");
		assertTrue(caps.isEmpty());
	}

	/**
	 * {@code o1-preview} does not support reasoning effort.
	 */
	public void testO1PreviewHasNoReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("o1-preview");
		assertTrue(caps.isEmpty());
	}

	/**
	 * {@code o3} supports reasoning effort.
	 */
	public void testO3HasReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("o3");
		assertTrue(caps.contains(ModelCapability.REASONING_EFFORT));
	}

	/**
	 * {@code o3-mini} supports reasoning effort.
	 */
	public void testO3MiniHasReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("o3-mini");
		assertTrue(caps.contains(ModelCapability.REASONING_EFFORT));
	}

	/**
	 * {@code o4-mini} supports reasoning effort.
	 */
	public void testO4MiniHasReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("o4-mini");
		assertTrue(caps.contains(ModelCapability.REASONING_EFFORT));
	}

	/**
	 * {@code gpt-5} supports reasoning effort.
	 */
	public void testGpt5HasReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("gpt-5");
		assertTrue(caps.contains(ModelCapability.REASONING_EFFORT));
	}

	/**
	 * {@code gpt-5.1} supports reasoning effort.
	 */
	public void testGpt51HasReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("gpt-5.1");
		assertTrue(caps.contains(ModelCapability.REASONING_EFFORT));
	}

	/**
	 * {@code gpt-4o} does not support reasoning effort.
	 */
	public void testGpt4oHasNoReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("gpt-4o");
		assertTrue(caps.isEmpty());
	}

	/**
	 * {@code gpt-4o-mini} does not support reasoning effort.
	 */
	public void testGpt4oMiniHasNoReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("gpt-4o-mini");
		assertTrue(caps.isEmpty());
	}

	/**
	 * {@code codex-mini-2025-05-16} supports reasoning effort.
	 */
	public void testCodexMiniHasReasoningEffort() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities("codex-mini-2025-05-16");
		assertTrue(caps.contains(ModelCapability.REASONING_EFFORT));
	}

	/**
	 * {@code null} model name returns empty capabilities.
	 */
	public void testNullModelReturnsEmpty() {
		Set<ModelCapability> caps = OpenAIChatModelFactory.openAIModelCapabilities(null);
		assertTrue(caps.isEmpty());
	}
}
