/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.provider.LLMProviderRegistry;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.model.ModelService;

/**
 * Concurrency harness: runs N parallel {@code testAgent} invocations through
 * {@link AgentFactory#runAgent}.
 *
 * <p>Each parallel run sends the prompt {@value #USER_PROMPT} to the agent named
 * {@value #AGENT_NAME} and records its outcome. The test fails if any run throws.
 * Used to exercise the agent loop, lock model and provider streaming under
 * concurrent load — if N concurrent runs take roughly N× a single run, look for
 * serialization in {@link com.top_logic.tl.ai.core.agent.TransactionScope} or in
 * provider-side rate limits.</p>
 *
 * <h3>Configuration</h3>
 * <ul>
 *   <li>Parallel run count: system property {@value #CONCURRENCY_PROPERTY}
 *       (default {@value #DEFAULT_PARALLEL_COUNT}). Run with
 *       {@code -Dagents.concurrency=20} to scale up.</li>
 *   <li>Per-run timeout: system property {@value #TIMEOUT_SECONDS_PROPERTY}
 *       (default {@value #DEFAULT_TIMEOUT_SECONDS} seconds).</li>
 * </ul>
 *
 * <h3>Setup</h3>
 * <p>{@link #setUp()} creates a fresh {@link Agent} named {@value #AGENT_NAME} in
 * the test KB with model {@value #AGENT_MODEL} and a minimal system prompt — no
 * tools, no skills, no MCP servers — so the test exercises only the agent loop +
 * provider streaming. {@link #tearDown()} removes it again. The model
 * {@value #AGENT_MODEL} must be configured in
 * {@code tl-ai-demo.test.config.xml}'s {@code LLMProviderRegistry} (it currently
 * is). Real LLM calls are made — running this test consumes API credits.</p>
 *
 * @see AgentFactory
 * @see com.top_logic.tl.ai.core.agent.TransactionScope
 */
public class TestConcurrentAgents extends BasicTestCase {

	/** Default number of parallel agent runs when {@value #CONCURRENCY_PROPERTY} is not set. */
	public static final int DEFAULT_PARALLEL_COUNT = 5;

	/** System property to override the parallel run count. */
	public static final String CONCURRENCY_PROPERTY = "agents.concurrency";

	/** Default per-run timeout in seconds. */
	public static final int DEFAULT_TIMEOUT_SECONDS = 120;

	/** System property to override the per-run timeout. */
	public static final String TIMEOUT_SECONDS_PROPERTY = "agents.timeoutSeconds";

	/** KB name of the agent under test. */
	public static final String AGENT_NAME = "testAgent";

	/** Model the agent uses. Must be configured in the test {@code LLMProviderRegistry}. */
	public static final String AGENT_MODEL = "claude-haiku-4-5-20251001";

	/** Minimal system prompt — keeps responses short and deterministic. */
	public static final String AGENT_SYSTEM_PROMPT =
		"ALWAYS REPLY: Hello. ";

	/** Single-turn user message every parallel run sends. */
	public static final String USER_PROMPT = "Hi";

	/** */
	public TestConcurrentAgents(String testName) {
		super(testName);
	}

	/** */
	public TestConcurrentAgents() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Seed " + AGENT_NAME))) {
			Agent existing = AgentDefinitionQuery.getInstance().findAgentByName(AGENT_NAME);
			if (existing == null) {
				Agent agent = TlAiCoreFactory.getInstance().createAgent();
				agent.setName(AGENT_NAME);
				agent.setModel(AGENT_MODEL);
				agent.setSystemPrompt(AGENT_SYSTEM_PROMPT);
			}
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Remove " + AGENT_NAME))) {
			Agent existing = AgentDefinitionQuery.getInstance().findAgentByName(AGENT_NAME);
			if (existing != null) {
				existing.tDelete();
			}
			tx.commit();
		} finally {
			super.tearDown();
		}
	}

	/**
	 * Runs {@value #AGENT_NAME} N times in parallel and asserts every run succeeds.
	 */
	public void testRunConcurrent() throws Exception {
		int parallelCount = Integer.getInteger(CONCURRENCY_PROPERTY, DEFAULT_PARALLEL_COUNT);
		long timeoutSeconds = Long.getLong(TIMEOUT_SECONDS_PROPERTY, (long) DEFAULT_TIMEOUT_SECONDS);

		System.out.println("[TestConcurrentAgents] starting " + parallelCount
			+ " parallel runs of agent '" + AGENT_NAME + "' (per-run timeout "
			+ timeoutSeconds + "s)");

		ExecutorService pool = Executors.newFixedThreadPool(parallelCount);
		List<Future<RunOutcome>> futures = new ArrayList<>(parallelCount);
		long batchStart = System.nanoTime();

		try {
			for (int i = 0; i < parallelCount; i++) {
				int idx = i;
				futures.add(pool.submit(() -> runOneAgent(idx)));
			}

			List<RunOutcome> outcomes = new ArrayList<>(parallelCount);
			for (Future<RunOutcome> f : futures) {
				try {
					outcomes.add(f.get(timeoutSeconds, TimeUnit.SECONDS));
				} catch (TimeoutException timeout) {
					f.cancel(true);
					outcomes.add(new RunOutcome(outcomes.size(), false, timeoutSeconds * 1000,
						null, timeout));
				}
			}

			long totalMs = (System.nanoTime() - batchStart) / 1_000_000;
			report(outcomes, totalMs);

			List<RunOutcome> failures = outcomes.stream().filter(o -> !o.success()).toList();
			assertTrue("Expected all " + parallelCount + " runs to succeed, but "
				+ failures.size() + " failed (see report above)", failures.isEmpty());
		} finally {
			pool.shutdownNow();
		}
	}

	private RunOutcome runOneAgent(int idx) {
		long started = System.nanoTime();
		try {
			String response = ThreadContextManager.inSystemInteraction(
				TestConcurrentAgents.class,
				() -> {
					Conversation conversation = new Conversation();
					conversation.add(new UserMessage(
						UUID.randomUUID().toString(),
						Instant.now(),
						List.of(new TextContent(USER_PROMPT))));
					AgentEventListener noopListener = event -> { /* discard */ };
					return AgentFactory.getInstance().runAgent(
						AGENT_NAME, conversation, /* memory */ null, noopListener);
				});
			long ms = (System.nanoTime() - started) / 1_000_000;
			return new RunOutcome(idx, true, ms, response, null);
		} catch (Throwable t) {
			long ms = (System.nanoTime() - started) / 1_000_000;
			return new RunOutcome(idx, false, ms, null, t);
		}
	}

	private static void report(List<RunOutcome> outcomes, long totalWallMs) {
		long succeeded = outcomes.stream().filter(RunOutcome::success).count();
		long minMs = outcomes.stream().mapToLong(RunOutcome::elapsedMs).min().orElse(0);
		long maxMs = outcomes.stream().mapToLong(RunOutcome::elapsedMs).max().orElse(0);
		long avgMs = (long) outcomes.stream().mapToLong(RunOutcome::elapsedMs).average().orElse(0d);

		System.out.println("[TestConcurrentAgents] ===== summary =====");
		System.out.println("  total wall-clock: " + totalWallMs + " ms");
		System.out.println("  succeeded:        " + succeeded + "/" + outcomes.size());
		System.out.println("  per-run latency:  min=" + minMs + " avg=" + avgMs + " max=" + maxMs + " ms");

		for (RunOutcome o : outcomes) {
			if (o.success()) {
				String response = o.response();
				String snippet = response == null ? "<null>"
					: response.length() > 80 ? response.substring(0, 80) + "…"
					: response;
				System.out.println("  [run " + o.idx() + "] OK   " + o.elapsedMs() + " ms  => " + snippet);
			} else {
				Throwable error = o.error();
				System.out.println("  [run " + o.idx() + "] FAIL " + o.elapsedMs() + " ms  => "
					+ error.getClass().getSimpleName() + ": " + error.getMessage());
			}
		}
	}

	/** Per-run outcome captured for the summary report. */
	private record RunOutcome(int idx, boolean success, long elapsedMs, String response, Throwable error) {
	}

	/**
	 * Wraps the test in {@link KBSetup} + {@link ServiceTestSetup} so the H2 KB
	 * and the registries needed by {@link AgentFactory} are available.
	 */
	public static Test suite() {
		BasicRuntimeModule<?>[] modules = {
			ModelService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE,
			LLMProviderRegistry.Module.INSTANCE,
			ToolRegistryService.Module.INSTANCE,
		};
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestConcurrentAgents.class), modules));
	}
}
