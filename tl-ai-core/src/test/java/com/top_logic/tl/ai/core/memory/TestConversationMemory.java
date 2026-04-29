/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * Tests for {@link ConversationMemory}.
 */
public class TestConversationMemory extends TestCase {

	private static final Instant T0 = Instant.parse("2026-04-06T12:00:00Z");

	private UserMessage userMsg(String id, String text) {
		return new UserMessage(id, T0, List.of(new TextContent(text)));
	}

	private AssistantMessage assistantMsg(String id, String text) {
		return new AssistantMessage(id, T0, List.of(new TextContent(text)),
			TokenUsage.EMPTY, "stop");
	}

	/** add() delegates to underlying conversation. */
	public void testAdd() {
		Conversation conv = new Conversation();
		ConversationMemory memory = new ConversationMemory(
			conv, new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			200_000, 33_000);

		memory.add(userMsg("u1", "Hello"));
		assertEquals(1, memory.messages().size());
		assertEquals(1, conv.size());
	}

	/** estimateTokens without baseline uses full estimation. */
	public void testEstimateTokensNoBaseline() {
		Conversation conv = new Conversation();
		ConversationMemory memory = new ConversationMemory(
			conv, new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			200_000, 33_000);

		// "Hello, world!" = 13 bytes / 4 = 3 content + 4 overhead + 3 priming = 10
		memory.add(userMsg("u1", "Hello, world!"));
		assertEquals(10, memory.estimateTokens());
	}

	/** After updateTokenBaseline, uses API actual + estimate of new messages. */
	public void testEstimateTokensWithBaseline() {
		Conversation conv = new Conversation();
		ConversationMemory memory = new ConversationMemory(
			conv, new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			200_000, 33_000);

		memory.add(userMsg("u1", "Hello"));
		memory.add(assistantMsg("a1", "Hi"));

		// Simulate API saying the request used 5000 input tokens
		memory.updateTokenBaseline(new TokenUsage(5000, 100, 0, 0, 0));

		// Now add a new message after baseline
		// The list estimate for [u2] alone: priming(3) + content(4) + overhead(4) = 11
		memory.add(userMsg("u2", "New message text!"));

		// Should be: baseline (5000) + estimate of [u2] list (11) = 5011
		assertEquals(5011, memory.estimateTokens());
	}

	/** shouldCompact returns false when under budget. */
	public void testShouldCompactFalse() {
		Conversation conv = new Conversation();
		ConversationMemory memory = new ConversationMemory(
			conv, new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			200_000, 33_000);

		memory.add(userMsg("u1", "Hello"));
		assertFalse(memory.shouldCompact());
	}

	/** shouldCompact returns true when over usable budget. */
	public void testShouldCompactTrue() {
		Conversation conv = new Conversation();
		// Tiny window: 100 tokens, 20 reserved -> 80 usable
		ConversationMemory memory = new ConversationMemory(
			conv, new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			100, 20);

		// "x".repeat(400) = 400 bytes / 4 = 100 tokens -> over 80 usable
		memory.add(userMsg("u1", "x".repeat(400)));
		assertTrue(memory.shouldCompact());
	}

	/** compact() runs the strategy and updates the conversation. */
	public void testCompact() {
		Conversation conv = new Conversation();
		// Small window: 200 tokens, 50 reserved -> 150 usable
		ConversationMemory memory = new ConversationMemory(
			conv, new ByteBasedTokenEstimator(), new TruncatingStrategy(2),
			200, 50);

		// Add messages totaling > 150 tokens
		memory.add(userMsg("u1", "x".repeat(400)));      // 100 tokens
		memory.add(assistantMsg("a1", "x".repeat(400))); // 100 tokens
		memory.add(userMsg("u2", "recent"));              // ~1 token
		memory.add(assistantMsg("a2", "recent"));         // ~1 token

		assertTrue(memory.shouldCompact());

		CompactionResult result = memory.compact();

		assertNotNull(result);
		assertTrue(result.messagesSummarized() > 0);
		assertTrue(result.tokensAfter() < result.tokensBefore());

		// Conversation should now have fewer tokens
		assertFalse(memory.shouldCompact());
	}

	/** compact(trigger) preserves the trigger label. */
	public void testCompactManualTrigger() {
		Conversation conv = new Conversation();
		ConversationMemory memory = new ConversationMemory(
			conv, new ByteBasedTokenEstimator(), new TruncatingStrategy(2),
			200, 50);

		memory.add(userMsg("u1", "x".repeat(400)));
		memory.add(assistantMsg("a1", "x".repeat(400)));
		memory.add(userMsg("u2", "keep"));
		memory.add(assistantMsg("a2", "keep"));

		CompactionResult result = memory.compact("manual");
		assertEquals("manual", result.trigger());
	}

	/** usableTokens is contextWindow - reserved. */
	public void testUsableTokens() {
		ConversationMemory memory = new ConversationMemory(
			new Conversation(), new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			200_000, 33_000);

		assertEquals(167_000, memory.usableTokens());
	}

	/** conversation() returns the underlying Conversation. */
	public void testConversationAccessor() {
		Conversation conv = new Conversation();
		ConversationMemory memory = new ConversationMemory(
			conv, new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			200_000, 33_000);

		assertSame(conv, memory.conversation());
	}

	/** withDefaults factory creates memory with ByteBasedTokenEstimator and 33K reserve. */
	public void testWithDefaults() {
		ConversationMemory memory = ConversationMemory.withDefaults(
			new TruncatingStrategy(), 200_000);

		assertEquals(167_000, memory.usableTokens());
		assertNotNull(memory.conversation());
	}

	/** getMessages returns all messages when under budget. */
	public void testGetMessagesUnderBudget() {
		ConversationMemory memory = new ConversationMemory(
			new Conversation(), new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			200_000, 33_000);

		memory.add(userMsg("u1", "Hello"));
		memory.add(assistantMsg("a1", "Hi"));

		List<Message> result = memory.getMessages(100_000);
		assertEquals(2, result.size());
	}

	/** getMessages always includes system messages even when budget is tight. */
	public void testGetMessagesPreservesSystemMessages() {
		ConversationMemory memory = new ConversationMemory(
			new Conversation(), new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			200_000, 33_000);

		memory.add(new SystemMessage("s1", T0, "You are helpful."));
		memory.add(userMsg("u1", "x".repeat(400))); // large
		memory.add(assistantMsg("a1", "x".repeat(400))); // large
		memory.add(userMsg("u2", "recent"));
		memory.add(assistantMsg("a2", "recent"));

		// Small budget: should get system + most recent messages
		List<Message> result = memory.getMessages(50);
		assertTrue(result.stream().anyMatch(SystemMessage.class::isInstance));
		assertTrue(result.size() < 5);
	}

	/** After compaction, baseline is reset. */
	public void testBaselineResetAfterCompaction() {
		Conversation conv = new Conversation();
		ConversationMemory memory = new ConversationMemory(
			conv, new ByteBasedTokenEstimator(), new TruncatingStrategy(2),
			200, 50);

		memory.add(userMsg("u1", "x".repeat(400)));
		memory.add(assistantMsg("a1", "x".repeat(400)));
		memory.add(userMsg("u2", "keep"));
		memory.add(assistantMsg("a2", "keep"));

		// Set a baseline before compaction
		memory.updateTokenBaseline(new TokenUsage(50000, 100, 0, 0, 0));

		// Compact resets baseline
		memory.compact();

		// After compaction, estimate should use full estimation (no baseline)
		// because baseline was reset
		int estimate = memory.estimateTokens();
		// Should be small — just the preserved messages + marker
		assertTrue("Estimate should be small after compaction, was: " + estimate,
			estimate < 100);
	}
}
