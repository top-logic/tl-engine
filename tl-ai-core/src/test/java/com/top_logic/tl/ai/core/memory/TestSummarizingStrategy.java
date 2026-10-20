/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Tests for {@link SummarizingStrategy}.
 */
public class TestSummarizingStrategy extends TestCase {

	private static final Instant T0 = Instant.parse("2026-04-06T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private final TokenEstimator _estimator = new ByteBasedTokenEstimator();

	private UserMessage userMsg(String id, String text) {
		return new UserMessage(id, T0, List.of(new TextContent(text)));
	}

	private AssistantMessage assistantMsg(String id, String text) {
		return new AssistantMessage(id, T0, List.of(new TextContent(text)),
			TokenUsage.EMPTY, "stop");
	}

	private AssistantMessage toolCallMsg(String id, String callId) {
		return new AssistantMessage(id, T0,
			List.of(new ToolCall(callId, "someTool", "{}")),
			TokenUsage.EMPTY, "tool_use");
	}

	private ToolResultMessage toolResultMsg(String id, String callId, String text) {
		return new ToolResultMessage(id, T0, callId, "someTool",
			new ToolState.Completed(text, T0, T0, false));
	}

	private static String textOf(UserMessage msg) {
		StringBuilder sb = new StringBuilder();
		for (ContentPart part : msg.content()) {
			if (part instanceof TextContent tc) {
				sb.append(tc.text());
			}
		}
		return sb.toString();
	}

	/** Creates a mock provider that returns the given summary text. */
	private LLMProvider summaryProvider(String summaryText) {
		return (request, handler) -> {
			handler.onEvent(new StreamEvent.TextDelta(0, summaryText));
			handler.onEvent(new StreamEvent.MessageComplete(
				"sum-1", "test-model", "end_turn", USAGE,
				List.of(new TextContent(summaryText))));
		};
	}

	/** Summarizes old messages, preserves recent ones. */
	public void testSummarizesOldPreservesRecent() {
		String longText = "x".repeat(400); // 100 tokens each
		List<Message> messages = List.of(
			userMsg("u1", longText),
			assistantMsg("a1", longText),
			userMsg("u2", longText),
			assistantMsg("a2", longText),
			userMsg("u3", "recent"),
			assistantMsg("a3", "recent"));

		LLMProvider provider = summaryProvider("Summary of old conversation.");

		SummarizingStrategy strategy = new SummarizingStrategy(provider, "test-model", 4096);
		CompactionResult result = strategy.compact(messages, 250, _estimator);

		// Result should have: CompactionMarker + UserMessage(summary) + 4 preserved messages
		assertTrue(result.replacementMessages().get(0) instanceof CompactionMarker);
		assertTrue(result.replacementMessages().get(1) instanceof UserMessage);
		UserMessage summary = (UserMessage) result.replacementMessages().get(1);
		assertTrue(textOf(summary).contains("Summary of old conversation."));

		// 4 recent messages preserved (u2, a2, u3, a3)
		assertEquals(6, result.replacementMessages().size());
		assertEquals("u2", result.replacementMessages().get(2).id());
		assertEquals("a2", result.replacementMessages().get(3).id());
		assertEquals("u3", result.replacementMessages().get(4).id());
		assertEquals("a3", result.replacementMessages().get(5).id());
		assertEquals(2, result.messagesSummarized());
	}

	/** Falls back to truncation when LLM call fails. */
	public void testFallsBackOnProviderError() {
		String longText = "x".repeat(400);
		List<Message> messages = List.of(
			userMsg("u1", longText),
			assistantMsg("a1", longText),
			userMsg("u2", longText),
			assistantMsg("a2", longText),
			userMsg("u3", "recent"),
			assistantMsg("a3", "recent"));

		LLMProvider failingProvider = (request, handler) -> {
			throw new ProviderException(
				ProviderException.Kind.OVERLOADED, 500, "Internal error");
		};

		SummarizingStrategy strategy = new SummarizingStrategy(failingProvider, "test-model", 4096);
		CompactionResult result = strategy.compact(messages, 50, _estimator);

		// Should still produce a valid result via truncation fallback
		assertNotNull(result);
		assertTrue(result.replacementMessages().size() > 0);
		assertTrue(result.replacementMessages().get(0) instanceof CompactionMarker);
	}

	/** Passes conversation messages to the LLM for summarization. */
	public void testPassesMessagesToProvider() {
		AtomicReference<ProviderRequest> captured = new AtomicReference<>();

		LLMProvider capturingProvider = (request, handler) -> {
			captured.set(request);
			handler.onEvent(new StreamEvent.MessageComplete(
				"sum-1", "test-model", "end_turn", USAGE,
				List.of(new TextContent("Summary."))));
		};

		List<Message> messages = List.of(
			userMsg("u1", "x".repeat(400)),
			assistantMsg("a1", "x".repeat(400)),
			userMsg("u2", "x".repeat(400)),
			assistantMsg("a2", "x".repeat(400)),
			userMsg("u3", "recent"),
			assistantMsg("a3", "recent"));

		SummarizingStrategy strategy = new SummarizingStrategy(capturingProvider, "sum-model", 4096);
		strategy.compact(messages, 50, _estimator);

		ProviderRequest req = captured.get();
		assertNotNull(req);
		assertEquals("sum-model", req.model());
		assertNotNull(req.systemPrompt());
		assertFalse(req.messages().isEmpty());
	}

	/** A Failed tool result's error is capped and sent as plain UserMessage text. */
	public void testFailedToolResultErrorTruncated() {
		AtomicReference<ProviderRequest> captured = new AtomicReference<>();
		LLMProvider capturingProvider = (request, handler) -> {
			captured.set(request);
			handler.onEvent(new StreamEvent.MessageComplete(
				"sum-1", "test-model", "end_turn", USAGE,
				List.of(new TextContent("Summary."))));
		};

		String longError = "e".repeat(SummarizingStrategy.MAX_TOOL_RESULT_CHARS + 500);
		List<Message> messages = List.of(
			userMsg("u1", "x".repeat(400)),
			toolCallMsg("a1", "call-1"),
			new ToolResultMessage("t1", T0, "call-1", "someTool",
				new ToolState.Failed(longError, T0, T0, false)),
			userMsg("u2", "x".repeat(400)),
			assistantMsg("a2", "x".repeat(400)),
			userMsg("u3", "recent"),
			assistantMsg("a3", "recent"));

		SummarizingStrategy strategy = new SummarizingStrategy(capturingProvider, "sum-model", 4096);
		strategy.compact(messages, 50, _estimator);

		ProviderRequest req = captured.get();
		assertNotNull(req);
		// ToolResultMessage is converted to a UserMessage with text; no raw tool_result block.
		assertFalse("Provider request must not contain ToolResultMessage",
			req.messages().stream().anyMatch(ToolResultMessage.class::isInstance));
		// The error text must appear truncated inside the UserMessage.
		boolean sawTruncatedError = req.messages().stream()
			.filter(UserMessage.class::isInstance)
			.map(UserMessage.class::cast)
			.flatMap(um -> um.content().stream())
			.filter(TextContent.class::isInstance)
			.map(TextContent.class::cast)
			.map(TextContent::text)
			.anyMatch(t -> t.contains("[Tool error: someTool →")
				&& t.contains("[truncated:"));
		assertTrue("Failed tool result's error must be truncated before being sent to the summarizer",
			sawTruncatedError);
	}

	/** Tool calls and results are converted to text so the provider never receives tool_use blocks. */
	public void testToolCallsConvertedToTextForSummarizer() {
		AtomicReference<ProviderRequest> captured = new AtomicReference<>();
		LLMProvider capturingProvider = (request, handler) -> {
			captured.set(request);
			handler.onEvent(new StreamEvent.MessageComplete(
				"sum-1", "test-model", "end_turn", USAGE,
				List.of(new TextContent("Summary."))));
		};

		List<Message> messages = List.of(
			userMsg("u1", "x".repeat(400)),
			toolCallMsg("a1", "call-1"),
			toolResultMsg("t1", "call-1", "the output"),
			userMsg("u2", "x".repeat(400)),
			assistantMsg("a2", "x".repeat(400)),
			userMsg("u3", "recent"),
			assistantMsg("a3", "recent"));

		SummarizingStrategy strategy = new SummarizingStrategy(capturingProvider, "sum-model", 4096);
		strategy.compact(messages, 50, _estimator);

		ProviderRequest req = captured.get();
		assertNotNull(req);
		// No tool_result blocks
		assertFalse("Provider request must not contain ToolResultMessage",
			req.messages().stream().anyMatch(ToolResultMessage.class::isInstance));
		// No tool_use blocks inside AssistantMessages
		assertFalse("Provider request must not contain ToolCall blocks",
			req.messages().stream()
				.filter(AssistantMessage.class::isInstance)
				.map(AssistantMessage.class::cast)
				.flatMap(a -> a.content().stream())
				.anyMatch(ToolCall.class::isInstance));
		// Tool call appears as text in the AssistantMessage
		assertTrue("AssistantMessage must describe the tool call as text",
			req.messages().stream()
				.filter(AssistantMessage.class::isInstance)
				.map(AssistantMessage.class::cast)
				.flatMap(a -> a.content().stream())
				.filter(TextContent.class::isInstance)
				.map(TextContent.class::cast)
				.map(TextContent::text)
				.anyMatch(t -> t.contains("[Called tool: someTool")));
		// Tool result appears as text in a UserMessage
		assertTrue("Tool result must appear as UserMessage text",
			req.messages().stream()
				.filter(UserMessage.class::isInstance)
				.map(UserMessage.class::cast)
				.flatMap(um -> um.content().stream())
				.filter(TextContent.class::isInstance)
				.map(TextContent.class::cast)
				.map(TextContent::text)
				.anyMatch(t -> t.contains("[Tool result: someTool →")));
	}

	/** Falls back to truncation when the LLM returns an empty summary. */
	public void testFallsBackOnEmptySummary() {
		String longText = "x".repeat(400);
		List<Message> messages = List.of(
			userMsg("u1", longText),
			assistantMsg("a1", longText),
			userMsg("u2", longText),
			assistantMsg("a2", longText),
			userMsg("u3", "recent"),
			assistantMsg("a3", "recent"));

		LLMProvider emptyProvider = (request, handler) -> {
			handler.onEvent(new StreamEvent.MessageComplete(
				"sum-1", "test-model", "end_turn", USAGE, List.of()));
		};

		SummarizingStrategy strategy = new SummarizingStrategy(emptyProvider, "test-model", 4096);
		CompactionResult result = strategy.compact(messages, 50, _estimator);

		assertNotNull(result);
		assertTrue(result.replacementMessages().size() > 0);
		assertTrue(result.replacementMessages().get(0) instanceof CompactionMarker);
		// Falls back to truncation: no summary UserMessage with SUMMARY_PREFIX
		assertFalse(result.replacementMessages().stream()
			.filter(UserMessage.class::isInstance)
			.map(UserMessage.class::cast)
			.anyMatch(um -> textOf(um).contains(SummarizingStrategy.SUMMARY_PREFIX)));
	}

	/** When already under budget, returns messages unchanged. */
	public void testAlreadyUnderBudget() {
		List<Message> messages = List.of(
			userMsg("u1", "Hi"),
			assistantMsg("a1", "Hello"));

		LLMProvider provider = summaryProvider("Should not be called.");

		SummarizingStrategy strategy = new SummarizingStrategy(provider, "test-model", 4096);
		CompactionResult result = strategy.compact(messages, 100_000, _estimator);

		assertEquals(2, result.replacementMessages().size());
		assertEquals(0, result.messagesSummarized());
	}

	/** The split never orphans tool results from their originating tool calls. */
	public void testSplitKeepsToolCallAndResultsTogether() {
		String longText = "x".repeat(400);
		// Default minPreserve = 4 would split at index 4 — right between the
		// tool-call assistant message (index 3) and its tool results (indices 4, 5).
		List<Message> messages = List.of(
			userMsg("u1", longText),
			assistantMsg("a1", longText),
			userMsg("u2", longText),
			toolCallMsg("a2", "call-1"),
			toolResultMsg("t1", "call-1", longText),
			toolResultMsg("t2", "call-1", longText),
			userMsg("u3", "recent"),
			assistantMsg("a3", "recent"));

		LLMProvider provider = summaryProvider("Summary.");
		SummarizingStrategy strategy = new SummarizingStrategy(provider, "test-model", 4096);
		CompactionResult result = strategy.compact(messages, 250, _estimator);

		List<Message> replacement = result.replacementMessages();
		// The tool-call message must be preserved together with its results.
		assertTrue(replacement.stream().anyMatch(m -> "a2".equals(m.id())));
		assertTrue(replacement.stream().anyMatch(m -> "t1".equals(m.id())));
		assertTrue(replacement.stream().anyMatch(m -> "t2".equals(m.id())));
		// No preserved message may be a tool result without its tool call.
		for (int i = 0; i < replacement.size(); i++) {
			Message msg = replacement.get(i);
			if (msg instanceof ToolResultMessage tr) {
				final String callId = tr.toolCallId();
				boolean callPreserved = replacement.stream()
					.filter(AssistantMessage.class::isInstance)
					.map(AssistantMessage.class::cast)
					.flatMap(a -> a.content().stream())
					.filter(ToolCall.class::isInstance)
					.map(ToolCall.class::cast)
					.anyMatch(tc -> tc.id().equals(callId));
				assertTrue("Tool result " + msg.id() + " lost its tool call", callPreserved);
			}
		}
	}
}
