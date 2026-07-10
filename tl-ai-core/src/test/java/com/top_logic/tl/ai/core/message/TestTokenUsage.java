/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import junit.framework.TestCase;

/**
 * Tests for {@link TokenUsage}.
 */
public class TestTokenUsage extends TestCase {

	/** Total tokens is input + output. */
	public void testTotalTokens() {
		TokenUsage usage = new TokenUsage(100, 50, 0, 0, 0);
		assertEquals(150, usage.totalTokens());
	}

	/** Total tokens includes thinking tokens. */
	public void testTotalWithThinking() {
		TokenUsage usage = new TokenUsage(100, 50, 200, 0, 0);
		assertEquals(350, usage.totalTokens());
	}

	/** Prompt tokens is the full prompt: non-cached input plus cache read/write. */
	public void testPromptTokens() {
		TokenUsage usage = new TokenUsage(100, 50, 0, 4000, 900);
		assertEquals(5000, usage.promptTokens());
	}

	/** Total tokens counts the full prompt including cached parts. */
	public void testTotalIncludesCachedPrompt() {
		TokenUsage usage = new TokenUsage(100, 50, 200, 4000, 900);
		assertEquals(5250, usage.totalTokens());
	}

	/** All fields accessible. */
	public void testFields() {
		TokenUsage usage = new TokenUsage(100, 50, 200, 30, 10);
		assertEquals(100, usage.inputTokens());
		assertEquals(50, usage.outputTokens());
		assertEquals(200, usage.thinkingTokens());
		assertEquals(30, usage.cacheReadTokens());
		assertEquals(10, usage.cacheWriteTokens());
	}

	/** EMPTY constant has all zeros. */
	public void testEmpty() {
		TokenUsage empty = TokenUsage.EMPTY;
		assertEquals(0, empty.totalTokens());
		assertEquals(0, empty.inputTokens());
		assertEquals(0, empty.outputTokens());
	}

	/** Add combines two usages. */
	public void testAdd() {
		TokenUsage a = new TokenUsage(100, 50, 0, 10, 5);
		TokenUsage b = new TokenUsage(200, 30, 100, 20, 10);
		TokenUsage sum = a.add(b);
		assertEquals(300, sum.inputTokens());
		assertEquals(80, sum.outputTokens());
		assertEquals(100, sum.thinkingTokens());
		assertEquals(30, sum.cacheReadTokens());
		assertEquals(15, sum.cacheWriteTokens());
	}

	/** Adding EMPTY is identity. */
	public void testAddIdentity() {
		TokenUsage a = new TokenUsage(100, 50, 200, 30, 10);
		assertEquals(a, a.add(TokenUsage.EMPTY));
	}

	/**
	 * fromCacheInclusivePrompt subtracts the cached share out of a cache-inclusive prompt
	 * count (the OpenAI-style convention), so promptTokens() reconstructs the original value.
	 */
	public void testFromCacheInclusivePrompt() {
		TokenUsage usage = TokenUsage.fromCacheInclusivePrompt(4096, 3072, 12, 0);
		assertEquals(1024, usage.inputTokens());
		assertEquals(3072, usage.cacheReadTokens());
		assertEquals(0, usage.cacheWriteTokens());
		assertEquals(12, usage.outputTokens());
		assertEquals(4096, usage.promptTokens());
	}

	/** fromCacheInclusivePrompt never produces a negative inputTokens. */
	public void testFromCacheInclusivePromptClampsAtZero() {
		// Defensive: a provider reporting cachedTokens > promptTokens should not underflow.
		TokenUsage usage = TokenUsage.fromCacheInclusivePrompt(100, 150, 5, 0);
		assertEquals(0, usage.inputTokens());
	}
}
