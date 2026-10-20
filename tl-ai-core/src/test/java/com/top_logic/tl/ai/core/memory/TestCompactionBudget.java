/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Tests that {@link SummarizingStrategy} honours the token budget it is handed and that
 * repeated compaction converges.
 *
 * <p>Before the fix for issue #339 the split point was purely positional (keep the last
 * {@code minPreserve} messages, whatever they cost), so a compaction could return at eight
 * times the requested budget and three consecutive passes each spent a summarizer call to
 * free zero tokens.</p>
 */
public class TestCompactionBudget extends TestCase {

	private static final Instant T0 = Instant.parse("2026-09-04T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	/** ~100 estimated tokens. */
	private static final String MEDIUM_TEXT = "x".repeat(400);

	/** ~20000 estimated tokens. */
	private static final String FAT_TEXT = "y".repeat(80_000);

	private final TokenEstimator _estimator = new ByteBasedTokenEstimator();

	private UserMessage userMsg(String id, String text) {
		return new UserMessage(id, T0, List.of(new TextContent(text)));
	}

	private AssistantMessage assistantMsg(String id, String text) {
		return new AssistantMessage(id, T0, List.of(new TextContent(text)), TokenUsage.EMPTY, "stop");
	}

	private AssistantMessage toolCallMsg(String id, String callId) {
		return new AssistantMessage(id, T0, List.of(new ToolCall(callId, "someTool", "{}")),
			TokenUsage.EMPTY, "tool_use");
	}

	private ToolResultMessage toolResultMsg(String id, String callId, String text) {
		return new ToolResultMessage(id, T0, callId, "someTool",
			new ToolState.Completed(text, T0, T0, false));
	}

	/** A provider that answers every summarization request with the given text. */
	private LLMProvider summaryProvider(String summaryText) {
		return summaryProvider(summaryText, new AtomicInteger());
	}

	/** A provider that answers with the given text and counts how often it was called. */
	private LLMProvider summaryProvider(String summaryText, AtomicInteger calls) {
		return (request, handler) -> {
			calls.incrementAndGet();
			handler.onEvent(new StreamEvent.TextDelta(0, summaryText));
			handler.onEvent(new StreamEvent.MessageComplete("sum-1", "test-model", "end_turn",
				USAGE, List.of(new TextContent(summaryText))));
		};
	}

	private SummarizingStrategy strategy(LLMProvider provider) {
		return new SummarizingStrategy(provider, "test-model", 1024);
	}

	/** The messages of the replacement that are not compaction bookkeeping. */
	private static List<Message> preserved(CompactionResult result) {
		List<Message> preserved = new ArrayList<>();
		for (Message msg : result.replacementMessages()) {
			if (msg instanceof CompactionMarker) {
				continue;
			}
			if (msg instanceof UserMessage user && textOf(user).startsWith(SummarizingStrategy.SUMMARY_PREFIX)) {
				continue;
			}
			preserved.add(msg);
		}
		return preserved;
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

	/** Fails if any tool result in the list has lost the tool call that produced it. */
	private static void assertNoOrphanedToolResult(List<Message> messages) {
		for (Message msg : messages) {
			if (msg instanceof ToolResultMessage tr) {
				String callId = tr.toolCallId();
				boolean callPresent = messages.stream()
					.filter(AssistantMessage.class::isInstance)
					.map(AssistantMessage.class::cast)
					.flatMap(a -> a.content().stream())
					.filter(ToolCall.class::isInstance)
					.map(ToolCall.class::cast)
					.anyMatch(tc -> tc.id().equals(callId));
				assertTrue("Tool result " + msg.id() + " lost its tool call", callPresent);
			}
		}
	}

	/**
	 * Four fat messages in the recent window must not come back verbatim: the compaction has
	 * to reach the budget it was handed.
	 *
	 * <p>This is the reproduction from the issue, inverted. Before the fix it returned 80050
	 * tokens against a 10000-token budget.</p>
	 */
	public void testCompactionReachesTheBudget() {
		List<Message> messages = List.of(
			userMsg("u1", MEDIUM_TEXT),
			assistantMsg("a1", MEDIUM_TEXT),
			userMsg("u2", FAT_TEXT),
			assistantMsg("a2", FAT_TEXT),
			userMsg("u3", FAT_TEXT),
			assistantMsg("a3", FAT_TEXT));

		int tokensBefore = _estimator.estimateTokens(messages);
		int targetTokens = 10_000;
		assertTrue("precondition: conversation starts over budget", tokensBefore > targetTokens);

		CompactionResult result =
			strategy(summaryProvider("Short summary.")).compact(messages, targetTokens, _estimator, tokensBefore);

		assertTrue("compaction must summarize something", result.messagesSummarized() > 0);
		assertTrue("compaction returned over the target budget: " + result.tokensAfter()
			+ " > " + targetTokens, result.tokensAfter() <= targetTokens);
		assertEquals("tokensAfter must be the estimate of what was returned",
			_estimator.estimateTokens(result.replacementMessages()), result.tokensAfter());
	}

	/**
	 * Repeated passes over a conversation that starts far over budget reach a fixed point,
	 * and no pass spends a summarizer call without freeing tokens.
	 */
	public void testRepeatedPassesConverge() {
		AtomicInteger calls = new AtomicInteger();
		SummarizingStrategy strategy = strategy(summaryProvider("Short summary.", calls));
		int targetTokens = 10_000;

		List<Message> current = List.of(
			userMsg("u1", FAT_TEXT),
			assistantMsg("a1", FAT_TEXT),
			userMsg("u2", FAT_TEXT),
			assistantMsg("a2", FAT_TEXT),
			userMsg("u3", FAT_TEXT),
			assistantMsg("a3", FAT_TEXT));

		for (int pass = 1; pass <= 5; pass++) {
			int before = _estimator.estimateTokens(current);
			int callsBefore = calls.get();
			CompactionResult result = strategy.compact(current, targetTokens, _estimator, before);
			if (calls.get() > callsBefore) {
				assertTrue("pass " + pass + " spent a summarizer call but freed no tokens: "
					+ before + " -> " + result.tokensAfter(), result.tokensAfter() < before);
			}
			current = result.replacementMessages();
		}

		assertTrue("still over budget after 5 passes: " + _estimator.estimateTokens(current),
			_estimator.estimateTokens(current) <= targetTokens);
		assertEquals("one pass has to do the work, the rest are no-ops", 1, calls.get());
	}

	/**
	 * A conversation whose floor-mandated preserve zone plus a summary cannot fit the budget
	 * still converges - and gets there without spending a summarizer call per pass.
	 */
	public void testFixedPointSpendsNoFurtherSummarizerCalls() {
		AtomicInteger calls = new AtomicInteger();
		SummarizingStrategy strategy = strategy(summaryProvider("Short summary.", calls));
		int targetTokens = 440;

		List<Message> current = new ArrayList<>();
		for (int i = 1; i <= 8; i++) {
			current.add(i % 2 == 1 ? userMsg("u" + i, MEDIUM_TEXT) : assistantMsg("a" + i, MEDIUM_TEXT));
		}

		List<Message> previous = null;
		for (int pass = 1; pass <= 5; pass++) {
			int before = _estimator.estimateTokens(current);
			int callsBefore = calls.get();
			CompactionResult result = strategy.compact(current, targetTokens, _estimator, before);
			if (calls.get() > callsBefore) {
				assertTrue("pass " + pass + " spent a summarizer call but freed no tokens: "
					+ before + " -> " + result.tokensAfter(), result.tokensAfter() < before);
			}
			previous = current;
			current = result.replacementMessages();
		}

		assertEquals("only the first pass may summarize", 1, calls.get());
		assertTrue("did not reach the budget: " + _estimator.estimateTokens(current),
			_estimator.estimateTokens(current) <= targetTokens);
		assertEquals("the last passes must be a fixed point", previous, current);
	}

	/**
	 * {@code minPreserve} is a floor: it is honoured even when the recent-token budget alone
	 * would have kept fewer messages.
	 */
	public void testMinPreserveIsAFloor() {
		List<Message> messages = new ArrayList<>();
		for (int i = 1; i <= 8; i++) {
			messages.add(i % 2 == 1 ? userMsg("u" + i, MEDIUM_TEXT) : assistantMsg("a" + i, MEDIUM_TEXT));
		}

		int targetTokens = 440;
		// Half of 440 pays for two of these messages, so only the floor can explain four.
		CompactionResult result = strategy(summaryProvider("Short summary."))
			.compact(messages, targetTokens, _estimator, _estimator.estimateTokens(messages));

		List<Message> preserved = preserved(result);
		assertEquals("floor of " + SummarizingStrategy.DEFAULT_MIN_PRESERVE + " must be honoured",
			SummarizingStrategy.DEFAULT_MIN_PRESERVE, preserved.size());
		assertEquals("u5", preserved.get(0).id());
		assertEquals("a8", preserved.get(3).id());
		assertEquals(4, result.messagesSummarized());
	}

	/**
	 * The split follows the budget, not a message count: a conversation of cheap messages
	 * keeps many more than {@code minPreserve}.
	 */
	public void testSplitIsBudgetDrivenNotCountDriven() {
		List<Message> messages = new ArrayList<>();
		for (int i = 1; i <= 30; i++) {
			messages.add(i % 2 == 1 ? userMsg("u" + i, "x".repeat(100)) : assistantMsg("a" + i, "x".repeat(100)));
		}

		int targetTokens = 800;
		int keepRecentTokens = (int) (targetTokens * SummarizingStrategy.DEFAULT_RECENT_TOKEN_FRACTION);
		CompactionResult result = strategy(summaryProvider("Short summary."))
			.compact(messages, targetTokens, _estimator, _estimator.estimateTokens(messages));

		List<Message> preserved = preserved(result);
		assertTrue("budget allows far more than the floor, only " + preserved.size() + " kept",
			preserved.size() > SummarizingStrategy.DEFAULT_MIN_PRESERVE);
		assertTrue("preserve zone must stay within the recent-token budget",
			_estimator.estimateTokens(preserved) <= keepRecentTokens);
		assertEquals("a30", preserved.get(preserved.size() - 1).id());
		assertTrue("result must fit the budget", result.tokensAfter() <= targetTokens);
	}

	/**
	 * The preserve zone never starts on a {@link ToolResultMessage}: the boundary moves back
	 * onto the assistant message that requested the results.
	 */
	public void testPreserveZoneNeverStartsOnToolResult() {
		List<Message> messages = List.of(
			userMsg("u1", MEDIUM_TEXT),
			assistantMsg("a1", MEDIUM_TEXT),
			userMsg("u2", MEDIUM_TEXT),
			toolCallMsg("a2", "call-1"),
			toolResultMsg("t1", "call-1", MEDIUM_TEXT),
			toolResultMsg("t2", "call-1", MEDIUM_TEXT),
			userMsg("u3", "recent"),
			assistantMsg("a3", "recent"));

		CompactionResult result = strategy(summaryProvider("Short summary."))
			.compact(messages, 250, _estimator, _estimator.estimateTokens(messages));

		List<Message> preserved = preserved(result);
		assertFalse("preserve zone must not start on a tool result",
			preserved.get(0) instanceof ToolResultMessage);
		assertEquals("a2", preserved.get(0).id());
		assertNoOrphanedToolResult(result.replacementMessages());
	}

	/**
	 * The same invariant holds when the hard budget limit has to shrink the preserve zone
	 * past a run of tool results - they join their tool call in the summarize zone.
	 */
	public void testBudgetShrinkNeverStartsOnToolResult() {
		List<Message> messages = List.of(
			userMsg("u1", FAT_TEXT),
			toolCallMsg("a1", "call-1"),
			toolResultMsg("t1", "call-1", FAT_TEXT),
			toolResultMsg("t2", "call-1", FAT_TEXT),
			userMsg("u2", "recent"),
			assistantMsg("a2", "recent"));

		int targetTokens = 1_000;
		CompactionResult result = strategy(summaryProvider("Short summary."))
			.compact(messages, targetTokens, _estimator, _estimator.estimateTokens(messages));

		List<Message> preserved = preserved(result);
		assertFalse("preserve zone must not start on a tool result",
			preserved.get(0) instanceof ToolResultMessage);
		assertEquals("u2", preserved.get(0).id());
		assertNoOrphanedToolResult(result.replacementMessages());
		assertTrue("result must fit the budget", result.tokensAfter() <= targetTokens);
	}

	/**
	 * A large body of {@link SystemMessage}s - what an injected sub-agent result looks like -
	 * is summarized away instead of being re-emitted verbatim forever.
	 */
	public void testSystemMessageBodyIsReduced() {
		List<Message> messages = List.of(
			new SystemMessage("s1", T0, FAT_TEXT),
			new SystemMessage("s2", T0, FAT_TEXT),
			userMsg("u1", "a"),
			assistantMsg("a1", "b"),
			userMsg("u2", "c"),
			assistantMsg("a2", "d"));

		int tokensBefore = _estimator.estimateTokens(messages);
		int targetTokens = 1_000;
		assertTrue("precondition: the system messages alone blow the budget", tokensBefore > targetTokens);

		CompactionResult result = strategy(summaryProvider("Short summary."))
			.compact(messages, targetTokens, _estimator, tokensBefore);

		assertEquals("both system messages must be summarized", 2, result.messagesSummarized());
		assertTrue("result must fit the budget: " + result.tokensAfter(),
			result.tokensAfter() <= targetTokens);
		assertFalse("no SystemMessage may survive compaction",
			result.replacementMessages().stream().anyMatch(SystemMessage.class::isInstance));
	}

	/** System messages reach the summarizer as user text, capped like a tool result. */
	public void testSystemMessageIsSentToSummarizerAsText() {
		AtomicReference<ProviderRequest> captured = new AtomicReference<>();
		LLMProvider capturingProvider = (request, handler) -> {
			captured.set(request);
			handler.onEvent(new StreamEvent.TextDelta(0, "Short summary."));
			handler.onEvent(new StreamEvent.MessageComplete("sum-1", "test-model", "end_turn",
				USAGE, List.of(new TextContent("Short summary."))));
		};

		List<Message> messages = List.of(
			new SystemMessage("s1", T0, FAT_TEXT),
			userMsg("u1", "a"),
			assistantMsg("a1", "b"),
			userMsg("u2", "c"),
			assistantMsg("a2", "d"));

		strategy(capturingProvider).compact(messages, 1_000, _estimator,
			_estimator.estimateTokens(messages));

		ProviderRequest request = captured.get();
		assertNotNull("the summarizer must have been called", request);
		assertFalse("a mid-conversation SystemMessage is dropped by the request builders",
			request.messages().stream().anyMatch(SystemMessage.class::isInstance));
		String sent = request.messages().stream()
			.filter(UserMessage.class::isInstance)
			.map(UserMessage.class::cast)
			.map(TestCompactionBudget::textOf)
			.reduce("", (a, b) -> a + b);
		assertTrue("system content must reach the summarizer as user text: " + sent,
			sent.contains("[System note: "));
		assertTrue("oversized system content must be capped: " + sent.length(),
			sent.contains("[truncated: " + FAT_TEXT.length() + " chars total]"));
	}

	/**
	 * The reactive {@code CONTEXT_OVERFLOW} path in
	 * {@link com.top_logic.tl.ai.core.agent.AgentLoop} calls
	 * {@link ConversationMemory#compact(String)} once per retry, up to
	 * {@code AgentTuning.MAX_REACTIVE_COMPACT_RETRIES} times. Driven that way, the first
	 * retry has to get the conversation under {@link ConversationMemory#usableTokens()} and
	 * the rest must cost nothing.
	 */
	public void testReactiveRetryLoopConverges() {
		AtomicInteger calls = new AtomicInteger();
		Conversation conversation = new Conversation();
		ConversationMemory memory = new ConversationMemory(conversation, _estimator,
			strategy(summaryProvider("Short summary.", calls)), 10_000, 5_000);

		// A sub-agent result injected between turns arrives as a SystemMessage.
		memory.add(new SystemMessage("s1", T0, FAT_TEXT));
		memory.add(new SystemMessage("s2", T0, FAT_TEXT));
		memory.add(userMsg("u1", "and then?"));
		memory.add(assistantMsg("a1", "working on it"));
		memory.add(userMsg("u2", "status?"));
		memory.add(assistantMsg("a2", "done"));

		assertTrue("precondition: memory is over its usable budget", memory.shouldCompact());

		for (int attempt = 0; attempt <= 2; attempt++) {
			memory.compact("reactive");
		}

		assertFalse("memory must be under budget after the retry loop: "
			+ memory.estimateTokens() + " of " + memory.usableTokens(), memory.shouldCompact());
		assertEquals("only the first retry may spend a summarizer call", 1, calls.get());
		assertFalse("no SystemMessage may survive compaction",
			memory.messages().stream().anyMatch(SystemMessage.class::isInstance));
	}
}
