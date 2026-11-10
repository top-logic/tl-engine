/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.limits.CapacityConfig;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.limits.RunLimitsConfig;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
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
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * The sub-session the calls of a parallel batch of {@link AgentLoop} run in.
 *
 * <p>
 * The caller of the batch - an agent run in the sub-session of a window, say - keeps its
 * interaction open while the calls run on workers of their own. A call therefore runs in a
 * sub-session derived from the caller's: as the caller's user, naming the caller's sub-session as
 * its {@link SubSessionContext#getOrigin() origin}, but not in the caller's sub-session itself.
 * </p>
 */
public class TestParallelCallContext extends BasicTestCase {

	private static final Instant T0 = Instant.parse("2026-11-09T10:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	private static final long TIMEOUT_SECONDS = 60;

	private Person _person;

	private TLContext _window;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_person = TestPerson.createPerson("parallelCallUser");

		ThreadContextManager manager = TLContextManager.getManager();
		_window = (TLContext) manager.newSubSessionContext();
		_window.setSessionContext(manager.newSessionContext());
		_window.setPerson(_person);
	}

	@Override
	protected void tearDown() throws Exception {
		TestPerson.deletePersonAndUser(_person);
		_person = null;
		_window = null;
		super.tearDown();
	}

	/**
	 * Every call of a parallel batch runs in a sub-session of its own, derived from the caller's,
	 * as the caller's user.
	 */
	public void testCallsRunInSubSessionDerivedFromTheCaller() throws Exception {
		List<TLContext> seen = new CopyOnWriteArrayList<>();

		ToolCatalog tools = new ToolCatalog();
		tools.register(recordingTool("read_a", seen));
		tools.register(recordingTool("read_b", seen));
		tools.freeze();

		runInWindow(loop(tools, List.of(new ToolCall("c1", "read_a", "{}"), new ToolCall("c2", "read_b", "{}"))));

		assertEquals(2, seen.size());
		for (TLContext context : seen) {
			assertNotSame("A parallel call must not share the caller's sub-session.", _window, context);
			assertSame("A parallel call runs for the caller's sub-session.", _window, context.getOrigin());
			assertSame(_person, context.getPerson());
		}
	}

	/**
	 * A batch of an agent run that a call of a parallel batch started runs in sub-sessions derived
	 * from the call's, which still name the sub-session of the outermost caller as their origin.
	 */
	public void testNestedBatchNamesTheOutermostCaller() throws Exception {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(toolWorkers(0))) {
			List<TLContext> outer = new CopyOnWriteArrayList<>();
			List<TLContext> inner = new CopyOnWriteArrayList<>();

			ToolCatalog innerTools = new ToolCatalog();
			innerTools.register(recordingTool("inner_a", inner));
			innerTools.register(recordingTool("inner_b", inner));
			innerTools.freeze();

			ToolCatalog outerTools = new ToolCatalog();
			outerTools.register(nestingTool(outer, innerTools));
			outerTools.register(recordingTool("sibling", outer));
			outerTools.freeze();

			runInWindow(
				loop(outerTools, List.of(new ToolCall("o1", "nest", "{}"), new ToolCall("o2", "sibling", "{}"))));

			assertEquals(2, outer.size());
			assertEquals(2, inner.size());
			for (TLContext context : inner) {
				assertFalse("A nested call does not share the sub-session of the call that started it.",
					outer.contains(context));
				assertSame("The origin of a nested call is the outermost caller's sub-session.", _window,
					context.getOrigin());
				assertSame(_person, context.getPerson());
			}
		}
	}

	/**
	 * A nested batch that runs inline on the worker of the call that started it - a pool with a
	 * ceiling - runs in the sub-session of that call.
	 */
	public void testInlineNestedBatchRunsInTheCallersSubSession() throws Exception {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(toolWorkers(1))) {
			List<TLContext> outer = new CopyOnWriteArrayList<>();
			List<TLContext> inner = new CopyOnWriteArrayList<>();

			ToolCatalog innerTools = new ToolCatalog();
			innerTools.register(recordingTool("inner_a", inner));
			innerTools.register(recordingTool("inner_b", inner));
			innerTools.freeze();

			ToolCatalog outerTools = new ToolCatalog();
			outerTools.register(nestingTool(outer, innerTools));
			outerTools.register(recordingTool("sibling", outer));
			outerTools.freeze();

			runInWindow(
				loop(outerTools, List.of(new ToolCall("o1", "nest", "{}"), new ToolCall("o2", "sibling", "{}"))));

			assertEquals(2, inner.size());
			TLContext nest = outer.get(0);
			assertSame(nest, inner.get(0));
			assertSame(nest, inner.get(1));
			assertSame(_window, nest.getOrigin());
		}
	}

	/** Runs the given loop in a request of {@link #_window} on a thread of its own. */
	private void runInWindow(AgentLoop loop) throws Exception {
		inNewThread(() -> ThreadContextManager.inContext(_window,
			(ComputationEx2<Void, RuntimeException, RuntimeException>) () -> {
				loop.run(conversation(), context(), event -> {
					// The events are not what this test is about.
				});
				return null;
			}));
	}

	private static Tool recordingTool(String name, List<TLContext> seen) {
		return ToolStubs.builder(name).description("Records its context.")
			.schema(new ToolSchema(name, "Records its context.", "{}")).execute((args, context) -> {
				seen.add(TLContext.getContext());
				return ToolResult.of(name);
			}).build();
	}

	private static Tool nestingTool(List<TLContext> seen, ToolCatalog innerTools) {
		return ToolStubs.builder("nest").description("Runs a nested agent loop.")
			.schema(new ToolSchema("nest", "Runs a nested agent loop.", "{}"))
			.execute((args, context) -> {
				seen.add(TLContext.getContext());
				loop(innerTools, List.of(new ToolCall("i1", "inner_a", "{}"), new ToolCall("i2", "inner_b", "{}")))
					.run(conversation(), context(), event -> {
						// The events are not what this test is about.
					});
				return ToolResult.of("nested done");
			}).build();
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

	private static RunLimitsConfig toolWorkers(int workers) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(CapacityConfig.TOOL_WORKERS), Integer.valueOf(workers));
		return config;
	}

	private static <T> T inNewThread(Callable<T> job) throws Exception {
		FutureTask<T> task = new FutureTask<>(job);
		Thread thread = new Thread(task, TestParallelCallContext.class.getSimpleName());
		thread.setDaemon(true);
		thread.start();
		try {
			return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof Error error) {
				throw error;
			}
			throw (Exception) cause;
		}
	}

	/** A knowledge base with persons, the user the calls run for. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestParallelCallContext.class));
	}
}
