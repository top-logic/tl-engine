/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.model.ModelService;

/**
 * End-to-end integration tests for the asynchronous subagent delegation path.
 *
 * <p>
 * These tests exercise the real {@link DelegationTool#executeAsync} code path, which calls
 * {@code ThreadContextManager.inContext} / {@code inSystemInteraction} on the background worker
 * thread. This requires a started TopLogic module, provided by the {@link #suite()} harness.
 * </p>
 *
 * <p>
 * A mock {@link LLMProvider} is used so no real LLM credentials are needed and responses are
 * deterministic.
 * </p>
 *
 * <p>
 * NOTE: These tests do NOT run in the Maven sandbox (bare {@code mvn test}) because the
 * TopLogic module cannot be started there. They are intended to run in the full CI/app
 * environment where {@link KBSetup} and {@link ServiceTestSetup} boot the framework.
 * This is consistent with the pre-existing {@code TestPublishHtmlPageTool} behavior on
 * this branch.
 * </p>
 */
public class TestAsyncDelegationIntegration extends BasicTestCase {

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	// -----------------------------------------------------------------------
	// Helpers
	// -----------------------------------------------------------------------

	/**
	 * Creates an {@link AgentParams} with sensible defaults for testing.
	 */
	private static AgentParams testParams() {
		return new AgentParams(MODEL, "You are a test subagent.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/**
	 * Creates a frozen empty {@link ToolCatalog}.
	 */
	private static ToolCatalog emptyRegistry() {
		ToolCatalog r = new ToolCatalog();
		r.freeze();
		return r;
	}

	/**
	 * Creates a mock {@link LLMProvider} that emits the given response sequences in order.
	 *
	 * <p>
	 * Each element in {@code responses} is the content for one
	 * {@link com.top_logic.tl.ai.core.provider.StreamEvent.MessageComplete}. Calls beyond the
	 * configured list throw a {@link ProviderException}.
	 * </p>
	 */
	@SafeVarargs
	private static LLMProvider mockProvider(List<ContentPart>... responses) {
		AtomicInteger callIndex = new AtomicInteger(0);
		List<List<ContentPart>> responseList = List.of(responses);
		return (request, handler) -> {
			int idx = callIndex.getAndIncrement();
			if (idx >= responseList.size()) {
				throw new ProviderException(
					ProviderException.Kind.INVALID_REQUEST, 400,
					"Mock provider: no more responses (call " + idx + ")");
			}
			List<ContentPart> content = responseList.get(idx);
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				}
			}
			String finishReason = "end_turn";
			handler.onEvent(new StreamEvent.MessageComplete(
				"msg-" + idx, MODEL, finishReason, USAGE, content));
		};
	}

	/**
	 * Builds a {@link DelegationTool} whose child loop always returns the given reply text.
	 *
	 * @param toolName        tool name exposed to the LLM (e.g. {@code "delegate_to_helper"})
	 * @param targetAgentName logical agent name used for registry / log labelling
	 * @param childReplyText  the single text response the child mock LLM will produce
	 */
	private static DelegationTool delegationTool(String toolName, String targetAgentName,
			String childReplyText) {
		LLMProvider childProvider = mockProvider(List.of(new TextContent(childReplyText)));
		AgentLoop childLoop = new AgentLoop(testParams(), childProvider, emptyRegistry());
		return new DelegationTool(toolName, "test delegate", targetAgentName, "parent",
			name -> childLoop);
	}

	// -----------------------------------------------------------------------
	// Tests
	// -----------------------------------------------------------------------

	/**
	 * The async path returns an "started" envelope immediately and the subagent eventually
	 * completes with DONE status carrying the child's final text.
	 *
	 * <p>
	 * Exercises: registry binding, {@link DelegationTool#execute} in async mode,
	 * {@link SubagentJobRegistry#awaitBlocking}, {@link SubagentResult#finalText} and
	 * {@link SubagentResult#tokenUsage} contents.
	 * </p>
	 */
	public void testAsyncDelegationReturnsEnvelope() throws Exception {
		SubagentJobRegistry registry = new SubagentJobRegistry(3);
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			DelegationTool tool = delegationTool("delegate_to_helper", "helper", "child answer");

			ToolResult handle = tool.execute(Map.of("task", "do x"), ToolContext.EMPTY);
			assertTrue("execute returns a 'started' message; got: " + handle.text(),
				handle.text().contains("started"));

			// Extract the job id from the returned text so we don't have to guess.
			List<SubagentJob> jobs = registry.byStatus(
				SubagentJobStatus.QUEUED, SubagentJobStatus.RUNNING, SubagentJobStatus.DONE);
			assertEquals("One job must be registered", 1, jobs.size());
			SubagentJob job = jobs.get(0);
			String jobId = job.subagentId();

			List<SubagentResult> results = registry.awaitBlocking(List.of(jobId), 30_000);
			assertEquals("Result must be returned", 1, results.size());
			SubagentResult result = results.get(0);

			assertEquals("Job status must be DONE", SubagentJobStatus.DONE, job.status());
			assertTrue("finalText must contain child reply; got: " + result.finalText(),
				result.finalText().contains("child answer"));
			assertNotNull("tokenUsage must not be null", result.tokenUsage());
			// The mock provider emits USAGE (100 input, 50 output).
			assertTrue("Token usage must be captured; totalTokens=" + result.tokenUsage().totalTokens(),
				result.tokenUsage().totalTokens() > 0);
		} finally {
			registry.shutdown();
		}
	}

	/**
	 * Fan-out: spawn two independent subagents and verify each carries its own result and
	 * token attribution after joining.
	 *
	 * <p>
	 * Exercises: concurrent job registration, {@link SubagentJobRegistry#awaitBlocking} on
	 * multiple ids, per-job token isolation.
	 * </p>
	 */
	public void testFanOutThenJoin() throws Exception {
		SubagentJobRegistry registry = new SubagentJobRegistry(3);
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			DelegationTool tool1 = delegationTool("delegate_to_alpha", "alpha", "alpha result");
			DelegationTool tool2 = delegationTool("delegate_to_beta", "beta", "beta result");

			tool1.execute(Map.of("task", "task one"), ToolContext.EMPTY);
			tool2.execute(Map.of("task", "task two"), ToolContext.EMPTY);

			List<String> ids = new ArrayList<>();
			for (SubagentJob j : registry.byStatus(
					SubagentJobStatus.QUEUED, SubagentJobStatus.RUNNING, SubagentJobStatus.DONE)) {
				ids.add(j.subagentId());
			}
			assertEquals("Two jobs must be registered", 2, ids.size());

			List<SubagentResult> results = registry.awaitBlocking(ids, 30_000);
			assertEquals("Both results must be returned", 2, results.size());

			for (int i = 0; i < results.size(); i++) {
				SubagentResult r = results.get(i);
				assertEquals("Each job must complete as DONE", SubagentJobStatus.DONE,
					registry.get(ids.get(i)).status());
				assertTrue("Each subagent attributed its own tokens; totalTokens="
					+ r.tokenUsage().totalTokens(), r.tokenUsage().totalTokens() > 0);
			}

			// Verify each carries its distinct text.
			boolean hasAlpha = results.stream().anyMatch(r -> r.finalText().contains("alpha result"));
			boolean hasBeta = results.stream().anyMatch(r -> r.finalText().contains("beta result"));
			assertTrue("Alpha subagent result must be present", hasAlpha);
			assertTrue("Beta subagent result must be present", hasBeta);
		} finally {
			registry.shutdown();
		}
	}

	/**
	 * Completed subagent results are taken from the registry and marked delivered when the
	 * parent loop's injection point calls {@link SubagentJobRegistry#takeUndeliveredResults}.
	 *
	 * <p>
	 * This test exercises the public registry contract that {@link AgentLoop}'s private
	 * {@code injectSubagentResults} relies on: once a terminal job is taken, subsequent
	 * calls return an empty list (idempotency). The {@code injectSubagentResultsForTest}
	 * helper is package-private to {@code com.top_logic.tl.ai.core.agent}, so this test
	 * verifies the underlying contract via the public {@link SubagentJobRegistry} API instead.
	 * </p>
	 *
	 * <p>
	 * Exercises: {@link SubagentJobRegistry#takeUndeliveredResults}, delivery idempotency,
	 * and job final-text availability after completion.
	 * </p>
	 */
	public void testCompletedResultInjectedNextTurn() throws Exception {
		SubagentJobRegistry registry = new SubagentJobRegistry(3);
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			// Use a pre-completed job so we don't need to wait for the background thread.
			SubagentJob job = registry.register("injector", "compute x",
				new com.top_logic.tl.ai.tool.api.CancellationToken());
			job.markTerminal(SubagentJobStatus.DONE,
				new SubagentResult("injected child text", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE));

			// First call: job is terminal and undelivered — must be returned.
			List<SubagentJob> firstBatch = registry.takeUndeliveredResults();
			assertEquals("One undelivered job must be returned", 1, firstBatch.size());
			assertTrue("Result must contain the child text",
				firstBatch.get(0).result().finalText().contains("injected child text"));
			assertTrue("Job must be marked delivered after takeUndeliveredResults",
				job.isDelivered());

			// Second call: job is already delivered — must return empty list.
			List<SubagentJob> secondBatch = registry.takeUndeliveredResults();
			assertTrue("Delivery must be idempotent — second call returns empty", secondBatch.isEmpty());
		} finally {
			registry.shutdown();
		}
	}

	/**
	 * An async subagent run is persisted as its own {@link AgentExecution} with
	 * {@code parentExecution} set, mirroring the synchronous delegation path.
	 *
	 * <p>
	 * Exercises: {@code SubagentSpawner}'s {@code ExecutionRecorder} wiring — session
	 * resolution from the parent execution on the caller thread, row creation on the worker
	 * thread, and terminal finalization after the child loop completes.
	 * </p>
	 */
	public void testAsyncRunRecordedAsChildExecution() throws Exception {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "async-recording-test");
		AgentExecution parent =
			store.beginExecution(session, "parent-run", "parent", MODEL, null, null);

		SubagentJobRegistry registry = new SubagentJobRegistry(3);
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			DelegationTool tool = delegationTool("delegate_to_rec", "rec", "recorded child answer");
			ToolContext context = ToolContext.createUnrestricted();
			context.executionHandle().set(parent);

			tool.execute(Map.of("task", "record me"), context);

			List<SubagentJob> jobs = registry.byStatus(
				SubagentJobStatus.QUEUED, SubagentJobStatus.RUNNING, SubagentJobStatus.DONE);
			assertEquals("One job must be registered", 1, jobs.size());
			registry.awaitBlocking(List.of(jobs.get(0).subagentId()), 30_000);

			// The child's row was committed by the store's writer thread; advance this
			// thread's stable view so the query below can see it (in production the chat
			// servlet does this at request begin).
			HistoryUtils.updateSessionRevision();

			List<AgentExecution> executions = store.executionsOf(session);
			assertEquals("Parent and child execution must be recorded; got: " + executions,
				2, executions.size());
			AgentExecution child = executions.stream()
				.filter(e -> !"parent-run".equals(e.getRunId()))
				.findFirst().orElse(null);
			assertNotNull("Async subagent execution must exist", child);
			assertEquals("Child is attributed to the target agent", "rec", child.getAgentName());
			assertEquals("Child must reference the delegating execution as parent",
				parent, child.getParentExecution());
			assertNotNull("Child status must be set", child.getStatus());
			assertEquals("Child must be finalized as COMPLETE",
				ExecutionStatus.COMPLETE.name(), child.getStatus().getName());
		} finally {
			registry.shutdown();
		}
	}

	// -----------------------------------------------------------------------
	// Suite
	// -----------------------------------------------------------------------

	/**
	 * Creates the test suite with the minimal set of service dependencies required for the async
	 * delegation path.
	 *
	 * <p>
	 * Module selection rationale:
	 * </p>
	 * <ul>
	 * <li>{@link KBSetup} — provides the KnowledgeBase and its DBContext so that the
	 * per-transaction begin/commit/rollback calls in
	 * {@link com.top_logic.tl.ai.core.agent.TransactionScope} work correctly.</li>
	 * <li>{@link com.top_logic.util.model.ModelService.Module#INSTANCE} — required by
	 * {@link KBSetup} and ensures the TL model is loaded; also needed so
	 * {@code ThreadContextManager} can resolve a proper
	 * {@link com.top_logic.basic.SubSessionContext} on the background worker threads (the async
	 * spawn path calls {@code ThreadContextManager.inSystemInteraction}).</li>
	 * </ul>
	 *
	 * <p>
	 * No {@code AgentFactory}, {@code LLMProviderRegistry}, or {@code McpServerRegistry} is needed
	 * because the tests inject mock {@link AgentLoop} instances directly via the
	 * {@link com.top_logic.tl.ai.core.tools.interaction.DelegationTool.AgentLoopFactory} lambda.
	 * </p>
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAsyncDelegationIntegration.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
