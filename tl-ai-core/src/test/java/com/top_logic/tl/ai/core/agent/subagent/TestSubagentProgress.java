/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.core.tools.interaction.AskUserTool;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.SubagentStatusTool;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;
import com.top_logic.util.model.ModelService;

/**
 * What the lead can see of a running subagent (issue #700).
 *
 * <p>
 * {@code subagent_status} used to answer "still running" for a child that had already written text
 * and called tools, and its run id was set only once it had ended. The child's events now fill a
 * bounded {@link SubagentProgress} on its job, which the status tool reads.
 * </p>
 */
public class TestSubagentProgress extends BasicTestCase {

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private SubagentJobRegistry _registry;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SubagentJobRegistry(2);
	}

	@Override
	protected void tearDown() throws Exception {
		_registry.cancelAll();
		_registry.shutdown();
		super.tearDown();
	}

	// --- The ring on its own. ---

	/** The ring keeps the newest calls and never more than its limit. */
	public void testTheRingIsBounded() {
		SubagentProgress progress = new SubagentProgress();
		for (int i = 0; i < SubagentProgress.MAX_STEPS + 15; i++) {
			progress.onEvent(new AgentEvent.ToolCallStarted("r", i + 1, Instant.now(), "c" + i, "fetch",
				"{\"page\":" + i + "}", ToolMode.READ));
		}

		List<SubagentProgress.Step> steps = progress.steps();
		assertEquals(SubagentProgress.MAX_STEPS, steps.size());
		assertEquals("The oldest calls are dropped, the newest kept.",
			"{\"page\":" + (SubagentProgress.MAX_STEPS + 14) + "}", steps.get(steps.size() - 1).arguments());
	}

	/** Outcomes, turns, tokens and the last text come from the events. */
	public void testTheRingFollowsTheEvents() {
		SubagentProgress progress = new SubagentProgress();
		progress.onEvent(new AgentEvent.TurnStarted("r", 1, Instant.now()));
		progress.onEvent(new AgentEvent.TextDelta("r", 1, Instant.now(), "Looking up "));
		progress.onEvent(new AgentEvent.TextDelta("r", 1, Instant.now(), "the archive."));
		progress.onEvent(new AgentEvent.ToolCallStarted("r", 1, Instant.now(), "c1", "lookup", "{}", ToolMode.READ));
		progress.onEvent(new AgentEvent.ToolCallFailed("r", 1, Instant.now(), "c1", "lookup", "boom", Duration.ZERO));
		progress.onEvent(new AgentEvent.TurnCompleted("r", 1, Instant.now(), USAGE, List.of()));
		progress.onEvent(new AgentEvent.TurnStarted("r", 2, Instant.now()));
		progress.onEvent(new AgentEvent.ToolCallStarted("r", 2, Instant.now(), "c2", "fetch", "{}", ToolMode.READ));

		assertEquals(2, progress.turns());
		assertEquals(USAGE.totalTokens(), progress.tokens());
		assertEquals("A turn without text keeps the last text there was.", "Looking up the archive.",
			progress.lastText());
		assertEquals(SubagentProgress.Outcome.FAILED, progress.steps().get(0).outcome());
		assertEquals(new SubagentProgress.Step(2, "fetch", "{}", SubagentProgress.Outcome.RUNNING),
			progress.currentStep());
	}

	// --- What the lead reads. ---

	/**
	 * A child inside its second tool call: the status names both calls and the child's last text,
	 * marked as data, and returns at once.
	 */
	public void testTheStatusShowsWhatARunningChildDoes() throws Exception {
		CountDownLatch inProbe = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		ToolCatalog tools = new ToolCatalog();
		tools.register(ToolStubs.builder("lookup").execute((args, context) -> ToolResult.of("found")).build());
		tools.register(ToolStubs.builder("probe").execute((args, context) -> {
			inProbe.countDown();
			try {
				release.await(30, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			return ToolResult.of("probed");
		}).build());
		SubagentJob job = spawn(tools,
			List.of(new TextContent("Step 1: I will search first."), new ToolCall("c1", "lookup", "{\"q\":\"X\"}")),
			List.of(new ToolCall("c2", "probe", "{\"deep\":true}")));
		try {
			assertTrue("The child must reach its second tool call.", inProbe.await(20, TimeUnit.SECONDS));
			assertNotNull("The run id is known while the child works.", job.childRunId());

			long before = System.nanoTime();
			String status = status(job, "steps");
			assertTrue("The status must not wait for the child.",
				TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - before) < 5);

			assertTrue(status, status.contains("(worker, RUNNING)"));
			assertTrue(status, status.contains("lookup {\"q\":\"X\"} - ok"));
			assertTrue(status, status.contains("probe {\"deep\":true} - running"));
			assertTrue(status, status.contains("Step 1: I will search first."));
			String tag = UntrustedEnvelope.SUBAGENT_REPORT.tag();
			assertTrue("What the child wrote is marked as data: " + status,
				status.indexOf("<" + tag) < status.indexOf("Step 1:")
					&& status.indexOf("Step 1:") < status.indexOf("</" + tag + ">"));
		} finally {
			release.countDown();
		}
	}

	/** A child parked on a question is reported as waiting, and for what. */
	public void testTheStatusSaysWhatAChildWaitsFor() throws Exception {
		ToolCatalog tools = new ToolCatalog();
		tools.register(new AskUserTool());
		SubagentJob job = spawn(tools, List.of(new ToolCall("q1", "askUser",
			"{\"question\":\"Which region?\",\"header\":\"Clarify\",\"type\":\"text\"}")));
		long deadline = System.currentTimeMillis() + 20_000;
		while (!job.interactionStore().hasPending() && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}

		assertEquals(SubagentJob.Waiting.USER_ANSWER, job.waitingFor());
		String status = status(job, "summary");
		assertTrue(status, status.contains("WAITING - for the user's answer"));
	}

	private String status(SubagentJob job, String detail) {
		try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(_registry)) {
			return new SubagentStatusTool().execute(
				Map.of("subagentIds", List.of(job.subagentId()), "detail", detail), ToolContext.EMPTY).text();
		}
	}

	/** Spawns a background child with the given tools that answers with the given replies in turn. */
	@SafeVarargs
	private SubagentJob spawn(ToolCatalog tools, List<ContentPart>... replies) {
		tools.freeze();
		AtomicInteger calls = new AtomicInteger();
		LLMProvider provider = (request, handler) -> {
			int idx = calls.getAndIncrement();
			if (idx >= replies.length) {
				throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400, "no more");
			}
			List<ContentPart> content = replies[idx];
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				}
			}
			handler.onEvent(new StreamEvent.MessageComplete("msg-" + idx, MODEL, "end_turn", USAGE, content));
		};
		AgentLoop child = new AgentLoop(new AgentParams(MODEL, "child", AgentParams.DEFAULT_MAX_TURNS,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), provider, tools);
		DelegationTool spawn = new DelegationTool("delegate_to_worker", "worker", "worker", "lead",
			(name, security) -> child);
		CancellationToken token = new CancellationToken();
		ToolContext lead = new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			new TransactionScope(token), token);
		try (SubagentRegistryBinding.Binding registry = SubagentRegistryBinding.bind(_registry)) {
			spawn.execute(Map.of("task", "research X"), lead);
		}
		List<SubagentJob> jobs = _registry.byStatus(SubagentJobStatus.values());
		return jobs.get(jobs.size() - 1);
	}

	/** */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSubagentProgress.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SubagentWorkers.Module.INSTANCE));
	}
}
