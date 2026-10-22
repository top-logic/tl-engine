/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.memory.TruncatingStrategy;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * A child run compacts its conversation instead of overflowing its model's window (issue #698).
 *
 * <p>
 * The scenario is a worker that fetches forty large pages, one per turn, on a model whose window
 * cannot hold them all. The scripted provider refuses a request that does not fit, as a real one
 * does. A child used to run without any memory, so it was refused and ended
 * {@link StopReason#CONTEXT_OVERFLOW}; it now compacts at the start of the turn that would not fit.
 * </p>
 */
public class TestChildCompaction extends TestCase {

	/** The loop and the spawn execute inside a TopLogic interaction, as in {@code TestAgentLoop}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestChildCompaction.class)));
	}

	private static final String MODEL = "test-model";

	/** Small enough that forty pages cannot fit, large enough that a few do. */
	private static final int CONTEXT_WINDOW = 40_000;

	private static final int FETCHES = 40;

	/** About 1500 tokens each, so forty of them are half again as much as the window. */
	private static final String PAGE = "x".repeat(6_000);

	/** How many requests the scripted provider refused for not fitting. */
	private final AtomicInteger _refused = new AtomicInteger();

	/** The loop itself: compacts proactively and never sends a request that does not fit. */
	public void testTheLoopCompactsBeforeTheTurnThatWouldNotFit() {
		List<AgentEvent> events = new CopyOnWriteArrayList<>();
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Read all forty pages and summarize them."))));

		AgentResult result = worker().run(conversation, new TruncatingStrategy(), ToolContext.unrestricted(),
			events::add);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("No request may have been sent that does not fit.", 0, _refused.get());
		List<AgentEvent.CompactionStarted> compactions = events.stream()
			.filter(AgentEvent.CompactionStarted.class::isInstance)
			.map(AgentEvent.CompactionStarted.class::cast)
			.toList();
		assertFalse("The worker must have compacted on its way through forty pages.", compactions.isEmpty());
		for (AgentEvent.CompactionStarted compaction : compactions) {
			assertEquals("Compacted at the start of a turn, not after a refusal.", "proactive",
				compaction.trigger());
		}
	}

	/** A synchronous {@code delegate_to_*} child finishes its forty pages. */
	public void testASynchronousDelegateFinishes() {
		DelegationTool delegation = new DelegationTool("delegate_to_worker", "worker", "worker", "lead",
			(name, security) -> worker());

		try (SubagentRegistryBinding.Binding none = SubagentRegistryBinding.bind(null)) {
			ToolResult answer = delegation.execute(Map.of("task", "Read all forty pages."), ToolContext.unrestricted());

			assertTrue("The delegate must have completed: " + answer.text(),
				answer.text().contains("status=" + StopReason.COMPLETE));
		}
		assertEquals(0, _refused.get());
	}

	/** A background subagent finishes its forty pages. */
	public void testABackgroundSubagentFinishes() throws InterruptedException {
		SubagentJobRegistry registry = new SubagentJobRegistry(1);
		try {
			DelegationTool spawn = new DelegationTool("delegate_to_worker", "worker", "worker", "lead",
				(name, security) -> worker());
			try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(registry)) {
				spawn.execute(Map.of("task", "Read all forty pages."), ToolContext.unrestricted());
			}
			SubagentJob job = registry.byStatus(SubagentJobStatus.values()).get(0);
			long deadline = System.currentTimeMillis() + 30_000;
			while (!job.status().isTerminal() && System.currentTimeMillis() < deadline) {
				Thread.sleep(20);
			}

			assertEquals(SubagentJobStatus.DONE, job.status());
			assertEquals("The subagent must have completed: " + job.result().finalText(),
				StopReason.COMPLETE, job.result().stopReason());
			assertEquals(0, _refused.get());
		} finally {
			registry.shutdown();
		}
	}

	/**
	 * A worker on a model with {@link #CONTEXT_WINDOW}, whose provider fetches a page per turn and
	 * refuses a request that is larger than the window.
	 */
	private AgentLoop worker() {
		AtomicInteger calls = new AtomicInteger();
		LLMProvider provider = (request, handler) -> {
			// The same rough measure the estimator uses: four characters to a token.
			int inputTokens = String.valueOf(request.messages()).length() / 4;
			if (inputTokens > CONTEXT_WINDOW) {
				_refused.incrementAndGet();
				throw new ProviderException(ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED, 400,
					"prompt is too long");
			}
			int idx = calls.getAndIncrement();
			List<ContentPart> content = idx < FETCHES
				? List.of(new ToolCall("c" + idx, "fetchPage", "{\"page\":" + idx + "}"))
				: List.of(new TextContent("All forty pages read."));
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				}
			}
			// Reports what it read, as a real provider does: the memory measures from that baseline.
			handler.onEvent(new StreamEvent.MessageComplete("msg-" + idx, MODEL, "end_turn",
				new TokenUsage(inputTokens, 50, 0, 0, 0), content));
		};
		ToolCatalog tools = new ToolCatalog();
		tools.register(ToolStubs.builder("fetchPage")
			.execute((args, context) -> ToolResult.of("Page " + args.get("page") + ": " + PAGE)).build());
		tools.freeze();
		AgentParams params = new AgentParams(MODEL, "worker", AgentParams.DEFAULT_MAX_TURNS,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS).withModel(MODEL, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS,
				CONTEXT_WINDOW, Map.of());
		return new AgentLoop(params, provider, tools);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_refused.set(0);
	}
}
