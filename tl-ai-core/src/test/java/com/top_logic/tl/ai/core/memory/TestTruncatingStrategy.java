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
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * Tests for {@link TruncatingStrategy}.
 */
public class TestTruncatingStrategy extends TestCase {

	private static final Instant T0 = Instant.parse("2026-04-06T12:00:00Z");

	private final TokenEstimator _estimator = new ByteBasedTokenEstimator();

	private UserMessage userMsg(String id, String text) {
		return new UserMessage(id, T0, List.of(new TextContent(text)));
	}

	private AssistantMessage assistantMsg(String id, String text) {
		return new AssistantMessage(id, T0, List.of(new TextContent(text)),
			TokenUsage.EMPTY, "stop");
	}

	private AssistantMessage assistantWithToolCall(String id, String tcId, String toolName) {
		return new AssistantMessage(id, T0,
			List.of(new ToolCall(tcId, toolName, "{\"arg\":\"value\"}")),
			TokenUsage.EMPTY, "tool_use");
	}

	private ToolResultMessage toolResult(String tcId, String toolName, String result) {
		return new ToolResultMessage("tr-" + tcId, T0, tcId, toolName,
			new ToolState.Completed(result, T0, T0, false));
	}

	/** When already under budget, returns all messages unchanged (no marker). */
	public void testAlreadyUnderBudget() {
		List<Message> messages = List.of(
			userMsg("u1", "Hi"),
			assistantMsg("a1", "Hello"));

		TruncatingStrategy strategy = new TruncatingStrategy(2);
		CompactionResult result = strategy.compact(messages, 100_000, _estimator);

		assertEquals(2, result.replacementMessages().size());
		assertEquals(0, result.messagesSummarized());
	}

	/**
	 * The package-private overload that accepts a precomputed tokensBefore uses it as-is
	 * instead of recomputing from the message list — this is what lets
	 * {@link SummarizingStrategy}'s truncation fallback skip a redundant full re-scan.
	 */
	public void testPrecomputedTokensBeforeOverloadSkipsRecomputation() {
		List<Message> messages = List.of(
			userMsg("u1", "x".repeat(400)),
			assistantMsg("a1", "recent"));

		TruncatingStrategy strategy = new TruncatingStrategy(1);
		// Deliberately wrong on purpose: if this were ignored and recomputed from the
		// message list, tokensBefore in the result would differ from this value.
		int deliberatelyWrongTokensBefore = 999_999;
		CompactionResult result = strategy.compact(messages, 50, _estimator, deliberatelyWrongTokensBefore);

		assertEquals(deliberatelyWrongTokensBefore, result.tokensBefore());
	}

	/** Drops oldest messages to fit under target. */
	public void testDropsOldest() {
		// Each "x".repeat(400) text = 400 bytes / 4 = 100 tokens
		String longText = "x".repeat(400);
		List<Message> messages = List.of(
			userMsg("u1", longText),
			assistantMsg("a1", longText),
			userMsg("u2", longText),
			assistantMsg("a2", longText),
			userMsg("u3", "recent"),
			assistantMsg("a3", "recent"));

		TruncatingStrategy strategy = new TruncatingStrategy(2);
		CompactionResult result = strategy.compact(messages, 250, _estimator);

		// Should have: CompactionMarker + u3 + a3
		assertEquals(3, result.replacementMessages().size());
		assertTrue(result.replacementMessages().get(0) instanceof CompactionMarker);
		assertEquals("u3", result.replacementMessages().get(1).id());
		assertEquals("a3", result.replacementMessages().get(2).id());
		assertEquals(4, result.messagesSummarized());
	}

	/** Preserves minimum number of recent messages even if over budget. */
	public void testPreservesMinimumRecent() {
		String longText = "x".repeat(400); // 100 tokens each
		List<Message> messages = List.of(
			userMsg("u1", longText),
			assistantMsg("a1", longText),
			userMsg("u2", longText),
			assistantMsg("a2", longText));

		TruncatingStrategy strategy = new TruncatingStrategy(2);
		CompactionResult result = strategy.compact(messages, 50, _estimator);

		// CompactionMarker + u2 + a2 (last 2 preserved)
		assertEquals(3, result.replacementMessages().size());
		assertTrue(result.replacementMessages().get(0) instanceof CompactionMarker);
		assertEquals("u2", result.replacementMessages().get(1).id());
		assertEquals("a2", result.replacementMessages().get(2).id());
	}

	/** Tool call and its result are dropped together. */
	public void testToolPairingDrop() {
		List<Message> messages = List.of(
			userMsg("u1", "x".repeat(400)),              // 100 tokens
			assistantWithToolCall("a1", "tc1", "tool"),   // ~10 tokens
			toolResult("tc1", "tool", "x".repeat(400)),   // 100 tokens
			assistantMsg("a2", "Done"),                    // ~1 token
			userMsg("u2", "next"),                         // ~1 token
			assistantMsg("a3", "ok"));                     // ~0 tokens

		TruncatingStrategy strategy = new TruncatingStrategy(2);
		CompactionResult result = strategy.compact(messages, 50, _estimator);

		// Verify no orphaned tool results in the replacement
		boolean hasToolResult = result.replacementMessages().stream()
			.anyMatch(ToolResultMessage.class::isInstance);
		boolean hasToolCall = result.replacementMessages().stream()
			.filter(AssistantMessage.class::isInstance)
			.map(AssistantMessage.class::cast)
			.flatMap(a -> a.content().stream())
			.anyMatch(ToolCall.class::isInstance);

		if (!hasToolCall) {
			assertFalse("Orphaned tool results should be dropped", hasToolResult);
		}
	}

	/** System messages in the drop zone are preserved across compaction. */
	public void testPreservesSystemMessages() {
		SystemMessage sys = new SystemMessage("s1", T0, "You are helpful.");
		String longText = "x".repeat(400); // 100 tokens each
		List<Message> messages = List.of(
			sys,
			userMsg("u1", longText),
			assistantMsg("a1", longText),
			userMsg("u2", "recent"),
			assistantMsg("a2", "recent"));

		TruncatingStrategy strategy = new TruncatingStrategy(2);
		CompactionResult result = strategy.compact(messages, 50, _estimator);

		// System message must survive: CompactionMarker + SystemMessage + u2 + a2
		assertEquals(4, result.replacementMessages().size());
		assertTrue(result.replacementMessages().get(0) instanceof CompactionMarker);
		assertTrue(result.replacementMessages().get(1) instanceof SystemMessage);
		assertEquals("s1", result.replacementMessages().get(1).id());
		assertEquals("u2", result.replacementMessages().get(2).id());
		// Only the non-system messages were dropped
		assertEquals(2, result.messagesSummarized());
	}

	/** Multiple system messages (e.g. original + compaction summary) are all preserved. */
	public void testPreservesMultipleSystemMessages() {
		SystemMessage sys1 = new SystemMessage("s1", T0, "You are helpful.");
		SystemMessage sys2 = new SystemMessage("s2", T0, "Previous conversation summary.");
		String longText = "x".repeat(400);
		List<Message> messages = List.of(
			sys1,
			sys2,
			userMsg("u1", longText),
			assistantMsg("a1", longText),
			userMsg("u2", "recent"),
			assistantMsg("a2", "recent"));

		TruncatingStrategy strategy = new TruncatingStrategy(2);
		CompactionResult result = strategy.compact(messages, 50, _estimator);

		long systemCount = result.replacementMessages().stream()
			.filter(SystemMessage.class::isInstance).count();
		assertEquals(2, systemCount);
	}

	/** Never drops past an existing CompactionMarker. */
	public void testRespectsExistingMarker() {
		CompactionMarker marker = new CompactionMarker("cm1", T0, 3, 5000, "auto");
		List<Message> messages = List.of(
			marker,
			userMsg("u1", "x".repeat(400)),
			assistantMsg("a1", "x".repeat(400)),
			userMsg("u2", "recent"),
			assistantMsg("a2", "recent"));

		TruncatingStrategy strategy = new TruncatingStrategy(2);
		CompactionResult result = strategy.compact(messages, 50, _estimator);

		assertTrue(result.replacementMessages().get(0) instanceof CompactionMarker);
		assertFalse("cm1".equals(result.replacementMessages().get(0).id()));
	}
}
