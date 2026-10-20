/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.SummarizingStrategy;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * When {@link AgentLoop} checks the compaction budget, relative to the request it protects.
 *
 * <p>
 * The contract of Gitea #367: a conversation that no longer fits its budget is compacted
 * <em>before</em> the turn's request is built, so it never spends a round trip on a request the
 * model cannot take. The provider below refuses an oversized request the way a real API does, and
 * every test here counts what reached it.
 * </p>
 */
public class TestCompactionOrder extends TestCase {

	/** Tool execution runs in a system interaction, so the thread context has to be up. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestCompactionOrder.class)));
	}

	private static final Instant T0 = Instant.parse("2026-09-19T12:00:00Z");

	private static final String MODEL = "test-model";

	/** Tokens the model can take. A request over this is refused, as a real API would. */
	private static final int HARD_CONTEXT_LIMIT = 700;

	/** Tool output large enough that keeping it costs more than the whole budget. */
	private static final String FAT_RESULT = "y".repeat(6000);

	private static final ToolSchema READ_REPORT =
		new ToolSchema("readReport", "Reads the report.", "{\"type\":\"object\"}");

	/**
	 * An over-budget conversation is compacted before anything is sent.
	 *
	 * <p>
	 * This is the regression: the check used to run at the <em>end</em> of a turn, so the first
	 * request of such a conversation was built, sent, refused with a context overflow, and only then
	 * compacted and retried - two requests where one does.
	 * </p>
	 */
	public void testAnOverBudgetConversationIsNotSentBeforeItIsCompacted() {
		ConversationMemory memory = memory();
		fillBeyondBudget(memory);
		assertTrue("Premise: the conversation must be over budget before the run starts.",
			memory.shouldCompact());

		Recorder provider = new Recorder(answering());
		List<AgentEvent> events = run(memory, provider);

		assertEquals("The over-budget conversation must reach the provider exactly once.",
			1, provider.requests.size());
		assertEquals("No request may have been refused.", 0, provider.refused);
		assertEquals(List.of("proactive"), compactionTriggers(events));
		assertEquals(StopReason.COMPLETE, resultOf(events));
	}

	/** The compaction is reported before the request it spares, not after it. */
	public void testTheCompactionIsReportedBeforeTheRequestItSpares() {
		ConversationMemory memory = memory();
		fillBeyondBudget(memory);

		List<AgentEvent> events = run(memory, new Recorder(answering()));

		assertEquals("Turn, compaction, request - in that order.",
			List.of("TurnStarted", "CompactionStarted", "CompactionCompleted", "ContextComposition"),
			outline(events));
	}

	/**
	 * A turn whose tool results blew the budget is compacted before the next turn's request.
	 *
	 * <p>
	 * That is the other half of the same question: tool results are appended during the turn, and
	 * the next moment they are actually used is the request of the turn that follows. Nothing is
	 * refused in between.
	 * </p>
	 */
	public void testAGrownTurnIsCompactedBeforeTheNextRequest() {
		ConversationMemory memory = memory();
		memory.add(new UserMessage("u1", T0, List.of(new TextContent("Read the report."))));

		Recorder provider = new Recorder(callsTheToolThenAnswers());
		List<AgentEvent> events = run(memory, provider);

		assertEquals("One request per turn, and no retry.", 2, provider.requests.size());
		assertEquals("No request may have been refused.", 0, provider.refused);
		assertEquals(List.of("proactive"), compactionTriggers(events));
		assertEquals("The compaction belongs to the turn that would have sent the fat history.",
			List.of(2), compactionTurns(events));
		assertEquals("Turn one sends, turn two compacts and only then sends.",
			List.of("TurnStarted", "ContextComposition", "TurnStarted",
				"CompactionStarted", "CompactionCompleted", "ContextComposition"),
			outline(events));
		assertEquals(StopReason.COMPLETE, resultOf(events));
	}

	/** A conversation that fits its budget is left alone - the ordinary session is untouched. */
	public void testAConversationWithinBudgetIsNotCompacted() {
		ConversationMemory memory = memory();
		memory.add(new UserMessage("u1", T0, List.of(new TextContent("Anything to do?"))));

		Recorder provider = new Recorder(answering());
		List<AgentEvent> events = run(memory, provider);

		assertEquals(1, provider.requests.size());
		assertEquals(List.of(), compactionTriggers(events));
		assertEquals(StopReason.COMPLETE, resultOf(events));
	}

	/**
	 * A run that ends over budget does not compact on its way out; the next run does, before its
	 * first request.
	 *
	 * <p>
	 * This is the deliberate part of the change. The end-of-turn check used to fire once more after
	 * the last turn, spending a summarizer call on a conversation that may never be continued. The
	 * work it did is not lost: whoever continues the conversation compacts it at the start of their
	 * first turn, which is the point at which it would otherwise cost a refused request.
	 * </p>
	 */
	public void testTheCompactionOfALastTurnIsDeferredToTheNextRun() {
		ConversationMemory memory = memory();
		memory.add(new UserMessage("u1", T0, List.of(new TextContent("Read the report."))));

		List<AgentEvent> first = run(memory, new Recorder(callsTheToolThenAnswers()), 1);
		assertEquals("A single turn that only calls a tool cannot finish.",
			StopReason.MAX_TURNS, resultOf(first));
		assertEquals("Nothing is compacted after the last turn of a run.",
			List.of(), compactionTriggers(first));
		assertTrue("Premise: that run left the conversation over budget.", memory.shouldCompact());

		memory.add(new UserMessage("u2", T0, List.of(new TextContent("And now?"))));
		Recorder provider = new Recorder(answering());
		List<AgentEvent> second = run(memory, provider);

		assertEquals(List.of("proactive"), compactionTriggers(second));
		assertEquals("The deferred compaction still costs no request.", 0, provider.refused);
		assertEquals(1, provider.requests.size());
		assertEquals(StopReason.COMPLETE, resultOf(second));
	}

	// ---- Harness ----

	/**
	 * Records every request and refuses the oversized ones, the way a provider API does.
	 */
	private static final class Recorder implements LLMProvider {

		final List<ProviderRequest> requests = new ArrayList<>();

		/** How many requests were refused for being larger than the model's window. */
		int refused;

		private final LLMProvider _answers;

		Recorder(LLMProvider answers) {
			_answers = answers;
		}

		@Override
		public void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
			requests.add(request);
			int requested = new ByteBasedTokenEstimator().estimateTokens(request.messages());
			if (requested > HARD_CONTEXT_LIMIT) {
				refused++;
				throw new ProviderException(ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED, 400,
					"Request of " + requested + " tokens exceeds the context window.");
			}
			_answers.stream(request, handler);
		}
	}

	/** Answers every request with the same plain text. */
	private static LLMProvider answering() {
		return (request, handler) -> handler.onEvent(new StreamEvent.MessageComplete(
			"msg", MODEL, "end_turn", usage(200), List.of(new TextContent("Done."))));
	}

	/** Calls the fat tool on the first request and answers on every later one. */
	private static LLMProvider callsTheToolThenAnswers() {
		AtomicInteger call = new AtomicInteger();
		return (request, handler) -> {
			if (call.getAndIncrement() > 0) {
				handler.onEvent(new StreamEvent.MessageComplete("msg", MODEL, "end_turn",
					usage(200), List.of(new TextContent("Done."))));
				return;
			}
			handler.onEvent(new StreamEvent.ToolCallStart(0, "tc1", READ_REPORT.name()));
			handler.onEvent(new StreamEvent.ToolCallDelta(0, "{}"));
			handler.onEvent(new StreamEvent.MessageComplete("msg-0", MODEL, "tool_use",
				usage(400), List.of(new ToolCall("tc1", READ_REPORT.name(), "{}"))));
		};
	}

	private static List<AgentEvent> run(ConversationMemory memory, Recorder provider) {
		return run(memory, provider, 5);
	}

	private static List<AgentEvent> run(ConversationMemory memory, Recorder provider, int maxTurns) {
		List<AgentEvent> events = new ArrayList<>();
		AgentEventListener capture = events::add;
		new AgentLoop(params(maxTurns), provider, fatResultRegistry(), memory)
			.run(ToolContext.EMPTY, capture);
		return events;
	}

	/**
	 * The events this issue is about, in the order they were emitted: the turns, the compactions
	 * and the requests. Everything else a run emits is noise here.
	 */
	private static List<String> outline(List<AgentEvent> events) {
		List<String> outline = new ArrayList<>();
		for (AgentEvent event : events) {
			if (event instanceof AgentEvent.TurnStarted) {
				outline.add("TurnStarted");
			} else if (event instanceof AgentEvent.CompactionStarted) {
				outline.add("CompactionStarted");
			} else if (event instanceof AgentEvent.CompactionCompleted) {
				outline.add("CompactionCompleted");
			} else if (event instanceof AgentEvent.ContextComposition) {
				outline.add("ContextComposition");
			}
		}
		return outline;
	}

	private static List<String> compactionTriggers(List<AgentEvent> events) {
		return events.stream()
			.filter(AgentEvent.CompactionStarted.class::isInstance)
			.map(event -> ((AgentEvent.CompactionStarted) event).trigger())
			.toList();
	}

	private static List<Integer> compactionTurns(List<AgentEvent> events) {
		return events.stream()
			.filter(AgentEvent.CompactionStarted.class::isInstance)
			.map(event -> ((AgentEvent.CompactionStarted) event).turnNumber())
			.toList();
	}

	private static StopReason resultOf(List<AgentEvent> events) {
		return events.stream()
			.filter(AgentEvent.RunCompleted.class::isInstance)
			.map(event -> ((AgentEvent.RunCompleted) event).reason())
			.findFirst().orElseThrow();
	}

	/** Enough exchanges to push the conversation past its budget before a run starts. */
	private static void fillBeyondBudget(ConversationMemory memory) {
		memory.add(new UserMessage("u0", T0, List.of(new TextContent(FAT_RESULT))));
		memory.add(new UserMessage("u1", T0, List.of(new TextContent("Anything else to do?"))));
	}

	/**
	 * A window of 1400 tokens with 700 reserved, so compaction triggers at the same 700 tokens the
	 * provider refuses to exceed.
	 */
	private static ConversationMemory memory() {
		return new ConversationMemory(new Conversation(), new ByteBasedTokenEstimator(),
			new SummarizingStrategy(summarizer(), MODEL, 512, 1),
			2 * HARD_CONTEXT_LIMIT, HARD_CONTEXT_LIMIT);
	}

	/** Stands in for the summarizer model, which is a plain provider call like any other. */
	private static LLMProvider summarizer() {
		return (request, handler) -> {
			handler.onEvent(new StreamEvent.TextDelta(0, "The report was read."));
			handler.onEvent(new StreamEvent.MessageComplete("summary", MODEL, "end_turn",
				usage(10), List.of(new TextContent("The report was read."))));
		};
	}

	private static AgentParams params(int maxTurns) {
		return new AgentParams(MODEL, "You are a test assistant.", maxTurns,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	private static TokenUsage usage(int inputTokens) {
		return new TokenUsage(inputTokens, 20, 0, 0, 0);
	}

	private static ToolCatalog fatResultRegistry() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(ToolStubs.builder(READ_REPORT.name()).description(READ_REPORT.description())
			.schema(READ_REPORT).result(FAT_RESULT).build());
		catalog.freeze();
		return catalog;
	}

	/** Guards the premise these tests rest on: the fat result is worth more than the budget. */
	public void testFatResultDoesNotFitTheBudget() {
		int fat = new ByteBasedTokenEstimator().estimateTokens(
			List.of(new UserMessage("x", T0, List.of(new TextContent(FAT_RESULT)))));
		assertTrue("The fat result has to be worth more than the whole budget, or nothing in "
			+ "this test compacts: " + fat, fat > HARD_CONTEXT_LIMIT);
	}
}
