/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.limits.CapacityConfig;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.limits.RunLimitsConfig;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ScriptedModel;
import com.top_logic.tl.ai.core.provider.ScriptedModel.Reply;
import com.top_logic.tl.ai.core.provider.ScriptedModel.Script;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * The ceiling {@link CapacityConfig#getToolWorkers()} on the pool the calls of a parallel READ
 * batch of {@link AgentLoop} run on.
 */
public class TestToolWorkerCeiling extends TestCase {

	private static final Instant T0 = Instant.parse("2026-11-04T10:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	private static final int BATCH = 5;

	/** Generous upper bound for anything a test waits for. */
	private static final long TIMEOUT_SECONDS = 30;

	private final AtomicInteger _running = new AtomicInteger();

	private final AtomicInteger _maxRunning = new AtomicInteger();

	private final AtomicInteger _started = new AtomicInteger();

	private final CountDownLatch _release = new CountDownLatch(1);

	private ExecutorService _caller;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_caller = Executors.newSingleThreadExecutor();
	}

	@Override
	protected void tearDown() throws Exception {
		_release.countDown();
		_caller.shutdownNow();
		super.tearDown();
	}

	/** With {@code tool-workers="2"} at most two calls of a batch of five run at once. */
	public void testCeilingBoundsTheBatch() throws Exception {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(toolWorkers(2))) {
			Future<Conversation> run = startBlockingBatch();

			awaitStarted(2);
			// Long enough for a third call to start if the pool let it.
			Thread.sleep(300);
			assertEquals("Only the ceiling's worth of calls runs.", 2, _started.get());

			_release.countDown();
			Conversation conversation = run.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

			assertEquals("Every call of the batch ran.", BATCH, _started.get());
			assertEquals("Never more than two at a time.", 2, _maxRunning.get());
			assertResultsInBatchOrder(conversation);
		}
	}

	/** Without a ceiling every call of the batch runs at the same time. */
	public void testWithoutCeilingAllRunAtOnce() throws Exception {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(toolWorkers(0))) {
			Future<Conversation> run = startBlockingBatch();

			awaitStarted(BATCH);

			_release.countDown();
			Conversation conversation = run.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

			assertEquals(BATCH, _maxRunning.get());
			assertResultsInBatchOrder(conversation);
		}
	}

	/**
	 * With {@code tool-workers="1"}, a READ tool of a parallel batch that runs an agent loop with a
	 * parallel batch of its own completes: the nested batch runs on the tool's worker, one call after
	 * the other, instead of waiting for a worker the ceiling does not allow.
	 */
	public void testNestedBatchDoesNotDeadlock() throws Exception {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(toolWorkers(1))) {
			List<Thread> innerThreads = new CopyOnWriteArrayList<>();
			List<Thread> nestThread = new CopyOnWriteArrayList<>();

			ToolCatalog inner = new ToolCatalog();
			inner.register(readTool("inner_a", () -> innerThreads.add(Thread.currentThread())));
			inner.register(readTool("inner_b", () -> innerThreads.add(Thread.currentThread())));
			inner.freeze();

			ToolCatalog outer = new ToolCatalog();
			outer.register(ToolStubs.builder("nest").description("Runs a nested agent loop.")
				.schema(new ToolSchema("nest", "Runs a nested agent loop.", "{}"))
				.execute((args, context) -> {
					nestThread.add(Thread.currentThread());
					AgentLoop nested = loop(inner,
						List.of(new ToolCall("i1", "inner_a", "{}"), new ToolCall("i2", "inner_b", "{}")));
					nested.run(conversation(), context(), ignoreEvents());
					return ToolResult.of("nested done");
				}).build());
			outer.register(readTool("sibling", () -> {
				// Only takes the second place of the outer batch.
			}));
			outer.freeze();

			AgentLoop loop = loop(outer,
				List.of(new ToolCall("o1", "nest", "{}"), new ToolCall("o2", "sibling", "{}")));
			Conversation conversation = conversation();
			_caller.submit(() -> loop.run(conversation, context(), ignoreEvents()))
				.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

			assertEquals("Both nested calls ran.", 2, innerThreads.size());
			assertSame("The nested calls ran on the worker of the tool that started them.",
				nestThread.get(0), innerThreads.get(0));
			assertSame(nestThread.get(0), innerThreads.get(1));
			assertEquals(List.of("o1", "o2"), resultIds(conversation));
		}
	}

	private Future<Conversation> startBlockingBatch() {
		ToolCatalog registry = new ToolCatalog();
		List<ToolCall> calls = new ArrayList<>();
		for (int i = 1; i <= BATCH; i++) {
			String name = "block_" + i;
			registry.register(blockingTool(name));
			calls.add(new ToolCall("tc" + i, name, "{}"));
		}
		registry.freeze();

		AgentLoop loop = loop(registry, calls);
		Conversation conversation = conversation();
		return _caller.submit(() -> {
			loop.run(conversation, context(), ignoreEvents());
			return conversation;
		});
	}

	private Tool blockingTool(String name) {
		return ToolStubs.builder(name).description("Blocks until released.")
			.schema(new ToolSchema(name, "Blocks until released.", "{}")).execute((args, context) -> {
				_started.incrementAndGet();
				int now = _running.incrementAndGet();
				_maxRunning.accumulateAndGet(now, Math::max);
				try {
					_release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				} finally {
					_running.decrementAndGet();
				}
				return ToolResult.of(name);
			}).build();
	}

	private static Tool readTool(String name, Runnable action) {
		return ToolStubs.builder(name).description("Records its call.")
			.schema(new ToolSchema(name, "Records its call.", "{}")).execute((args, context) -> {
				action.run();
				return ToolResult.of(name);
			}).build();
	}

	private void awaitStarted(int expected) throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_SECONDS * 1000;
		while (_started.get() < expected && System.currentTimeMillis() < deadline) {
			Thread.sleep(5);
		}
		assertEquals("The calls did not start.", expected, _started.get());
	}

	private static void assertResultsInBatchOrder(Conversation conversation) {
		List<String> expected = new ArrayList<>();
		for (int i = 1; i <= BATCH; i++) {
			expected.add("tc" + i);
		}
		assertEquals("The results are in the order of the batch.", expected, resultIds(conversation));
	}

	private static List<String> resultIds(Conversation conversation) {
		List<String> ids = new ArrayList<>();
		for (Message message : conversation.messages()) {
			if (message instanceof ToolResultMessage result) {
				ids.add(result.toolCallId());
			}
		}
		return ids;
	}

	private static AgentLoop loop(ToolCatalog registry, List<ToolCall> calls) {
		List<ContentPart> toolTurn = new ArrayList<>(calls);
		return new AgentLoop(new AgentParams("test-model", "system", 10, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS),
			new ScriptedModel(Script.of(Reply.of(toolTurn).withUsage(USAGE),
				Reply.of(List.of(new TextContent("Done."))).withUsage(USAGE))),
			registry);
	}

	private static Conversation conversation() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));
		return conversation;
	}

	private static ToolContext context() {
		CancellationToken cancellationToken = new CancellationToken();
		return new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			new TransactionScope(cancellationToken), cancellationToken);
	}

	private static AgentEventListener ignoreEvents() {
		return event -> {
			// The events are not what this test is about.
		};
	}

	private static RunLimitsConfig toolWorkers(int workers) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(CapacityConfig.TOOL_WORKERS), Integer.valueOf(workers));
		return config;
	}

	/**
	 * Boots the {@code ThreadContextManager} module, which the concurrent batch dispatch needs to
	 * establish an interaction for a tool.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestToolWorkerCeiling.class)));
	}
}
