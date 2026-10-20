/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.SummarizingStrategy;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * End-to-end test of issue #341: a session that really compacted, was really persisted and
 * reloaded runs its next turn at its compacted size.
 *
 * <p>
 * The pieces are the production ones throughout - {@link AgentLoop} with a
 * {@link ConversationMemory}, {@link ConversationLogListener} recording into a
 * {@link ConversationLog}, {@link ConversationLogSerializer} for the storage round trip and
 * {@link ConversationLogReplay} for the reload - so that the seam this issue is about is
 * exercised where it actually lives rather than simulated.
 * </p>
 */
public class TestCompactionSurvivesReload extends TestCase {

	/** Tool execution runs in a system interaction, so the thread context has to be up. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestCompactionSurvivesReload.class)));
	}

	private static final String MODEL = "test-model";

	/** Tokens the model can take. A request over this is rejected, as a real API would. */
	private static final int HARD_CONTEXT_LIMIT = 700;

	/** Tool output large enough that keeping it costs more than the whole budget. */
	private static final String FAT_RESULT = "y".repeat(6000);

	private static final ToolSchema READ_REPORT =
		new ToolSchema("readReport", "Reads the report.", "{\"type\":\"object\"}");

	/**
	 * A reloaded session runs its next turn without compacting again.
	 *
	 * <p>
	 * The control at the end is the point of the test: with the compaction's sidecar removed
	 * from the very same log - which is exactly the state every log was in before this issue
	 * was fixed - the reloaded session comes back at its full size and its first turn does
	 * have to compact. So the assertion above is not passing by accident.
	 * </p>
	 *
	 * <p>
	 * That control compacts <em>proactively</em>, before the turn's request rather than after a
	 * refused one: since #367 the budget is checked at the start of every turn, so even the
	 * fallback path costs no round trip. The provider below refuses an oversized request the way a
	 * real API would and is never asked to.
	 * </p>
	 */
	public void testReloadedSessionDoesNotOverflowOnItsFirstTurn() {
		ConversationLog log = runSessionThatCompacts();

		String json = ConversationLogSerializer.toJson(log);
		List<AgentEvent> withSidecar = runNextTurn(reload(ConversationLogSerializer.fromJson(json)));

		assertEquals("The turn after a reload must simply succeed.",
			StopReason.COMPLETE, resultOf(withSidecar));
		assertEquals("A session reloaded at its compacted size has no reason to compact again.",
			List.of(), compactionTriggers(withSidecar));

		ConversationLog stripped = LegacyLogs.withoutSidecars(ConversationLogSerializer.fromJson(json));
		List<AgentEvent> withoutSidecar = runNextTurn(reload(stripped));
		assertEquals("Control: without the sidecar the reload is back at full size and the "
			+ "first turn has to compact - which is the defect this test guards.",
			List.of("proactive"), compactionTriggers(withoutSidecar));
		assertEquals("Even the fallback path must not spend a refused request on the compaction.",
			StopReason.COMPLETE, resultOf(withoutSidecar));
	}

	/** The reloaded memory reports the compacted size, not the original one. */
	public void testReloadedSessionReportsCompactedTokenCount() {
		ConversationLog log = runSessionThatCompacts();
		String json = ConversationLogSerializer.toJson(log);

		ConversationMemory reloaded = reload(ConversationLogSerializer.fromJson(json));
		ConversationMemory full = reload(LegacyLogs.withoutSidecars(ConversationLogSerializer.fromJson(json)));

		assertFalse("A reloaded compacted session must not be over budget on arrival: "
			+ reloaded.estimateTokens() + " of " + reloaded.usableTokens() + " usable.",
			reloaded.shouldCompact());
		assertTrue("Without the sidecar the same log reloads over budget: "
			+ full.estimateTokens() + " of " + full.usableTokens() + " usable.",
			full.shouldCompact());
	}

	/**
	 * Runs one session that reads a fat tool result and therefore compacts, recording
	 * everything into a {@link ConversationLog} the way the chat does.
	 */
	private static ConversationLog runSessionThatCompacts() {
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", Instant.now(),
			"Read the report and tell me it is done.")));

		ConversationMemory memory = memory(new Conversation());
		memory.add(new UserMessage("u1", Instant.now(),
			List.of(new TextContent("Read the report and tell me it is done."))));

		AtomicInteger call = new AtomicInteger();
		LLMProvider provider = (request, handler) -> {
			if (call.getAndIncrement() == 0) {
				ToolCall toolCall = new ToolCall("tc1", "readReport", "{}");
				handler.onEvent(new StreamEvent.ToolCallStart(0, "tc1", "readReport"));
				handler.onEvent(new StreamEvent.ToolCallDelta(0, "{}"));
				handler.onEvent(new StreamEvent.MessageComplete("msg-0", MODEL, "tool_use",
					usage(400), List.of(toolCall)));
			} else {
				handler.onEvent(new StreamEvent.TextDelta(0, "Done."));
				handler.onEvent(new StreamEvent.MessageComplete("msg-1", MODEL, "end_turn",
					usage(200), List.of(new TextContent("Done."))));
			}
		};

		List<AgentEvent> events = new ArrayList<>();
		AgentEventListener capture = event -> {
			events.add(event);
			listener.onEvent(event);
		};

		AgentResult result = new AgentLoop(params(), provider, fatResultRegistry(), memory)
			.run(ToolContext.EMPTY, capture);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("The session under test has to have compacted for the reload to mean "
			+ "anything.", List.of("proactive"), compactionTriggers(events));
		return log;
	}

	/** Reloads a session from its log, exactly as {@code ChatSessionManager} does. */
	private static ConversationMemory reload(ConversationLog log) {
		ConversationMemory memory = memory(new Conversation());
		ConversationLogReplay.rebuildMemory(log, memory.conversation());
		return memory;
	}

	/**
	 * Runs one further turn on the reloaded memory against a provider that rejects an
	 * oversized request the way a real API does.
	 *
	 * @return Every event the turn emitted.
	 */
	private static List<AgentEvent> runNextTurn(ConversationMemory memory) {
		memory.add(new UserMessage("u2", Instant.now(),
			List.of(new TextContent("Anything else to do?"))));

		LLMProvider provider = (request, handler) -> {
			int requested = new ByteBasedTokenEstimator().estimateTokens(request.messages());
			if (requested > HARD_CONTEXT_LIMIT) {
				throw new ProviderException(ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED, 400,
					"Request of " + requested + " tokens exceeds the context window");
			}
			handler.onEvent(new StreamEvent.TextDelta(0, "Nothing else."));
			handler.onEvent(new StreamEvent.MessageComplete("msg-2", MODEL, "end_turn",
				usage(200), List.of(new TextContent("Nothing else."))));
		};

		List<AgentEvent> events = new ArrayList<>();
		new AgentLoop(params(), provider, fatResultRegistry(), memory)
			.run(ToolContext.EMPTY, events::add);
		return events;
	}

	private static StopReason resultOf(List<AgentEvent> events) {
		return events.stream()
			.filter(AgentEvent.RunCompleted.class::isInstance)
			.map(event -> ((AgentEvent.RunCompleted) event).reason())
			.findFirst().orElseThrow();
	}

	private static List<String> compactionTriggers(List<AgentEvent> events) {
		return events.stream()
			.filter(AgentEvent.CompactionStarted.class::isInstance)
			.map(event -> ((AgentEvent.CompactionStarted) event).trigger())
			.toList();
	}


	/**
	 * A window of 1400 tokens with 700 reserved, so compaction triggers at the same 700
	 * tokens the provider refuses to exceed.
	 */
	private static ConversationMemory memory(Conversation conversation) {
		return new ConversationMemory(conversation, new ByteBasedTokenEstimator(),
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

	private static AgentParams params() {
		return new AgentParams(MODEL, "You are a test assistant.", 5,
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

	/** Guards the premise the tests above rest on: the fat result forces a compaction. */
	public void testFatResultDoesNotFitTheBudget() {
		int fat = new ByteBasedTokenEstimator().estimateTokens(
			List.of((Message) new UserMessage("x", Instant.now(),
				List.of(new TextContent(FAT_RESULT)))));
		assertTrue("The fat tool result has to be worth more than the whole budget, or "
			+ "nothing in this test compacts: " + fat, fat > HARD_CONTEXT_LIMIT);
	}
}
