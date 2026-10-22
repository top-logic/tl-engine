/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.tools.ToolExecutions;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.RunInfo;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.util.model.ModelService;

/**
 * Reproduction of and regression test for Gitea #405 - "The run's {@code AgentExecution} cannot be
 * read from inside a tool".
 *
 * <p>
 * These run against a real {@link com.top_logic.knowledge.service.KnowledgeBase} because the bug is
 * entirely about <em>where</em> a row is read: {@link AgentExecutionStore} writes it on its writer
 * thread at a revision newer than the one the tool's own interaction is pinned to, so only a booted
 * store reproduces it. A test with no {@code AgentExecution} at all - which is every test that
 * existed when #405 was filed - takes the null path and never sees the throw; that is why an
 * application suite of 506 tests passed while the tool failed in every real agent run.
 * </p>
 *
 * @see TestInteractionRecorderRevision
 */
public class TestRunInfoRevisionSafety extends BasicTestCase {

	private static final String RUN_ID = "run-405";

	private static final String AGENT = "document-assistant";

	private static final String MODEL = "claude-opus-5";

	/**
	 * The bug, verbatim: the tool asks the handle for the run's execution and reads an attribute off
	 * it, and the read throws because the row was created after the revision the tool reads at.
	 *
	 * <p>
	 * This is a <b>characterization</b> test, not something the fix removes: the persistent wrapper
	 * stays unreadable from an execution thread, because nothing on the tool's side can make a row
	 * from a newer revision visible to a pinned view. What the fix changes is that a tool no longer
	 * has to reach for it - see
	 * {@link #testToolReadsTheRunAsPlainDataFromTheSameThread()}. The assertion is therefore on the
	 * throw and on its message, so that a future change which <em>does</em> make the wrapper
	 * readable fails here and gets the documentation updated rather than leaving it stale.
	 * </p>
	 */
	public void testDereferencingTheExecutionWrapperInAToolStillThrows() {
		ToolContext context = recordedRun();

		AtomicReference<Throwable> caught = new AtomicReference<>();
		ToolExecutions.execute(tool(ctx -> {
			try {
				AgentExecution execution = ((ToolContext) ctx).executionHandle().get();
				assertNotNull("the run is recorded, so the handle carries the row", execution);
				execution.getAgentName();
			} catch (RuntimeException ex) {
				caught.set(ex);
			}
			return ToolResult.of("done");
		}), new HashMap<>(), context);

		Throwable failure = caught.get();
		assertNotNull("Reading an attribute off the run's AgentExecution from inside a tool must"
			+ " still fail - if it no longer does, ExecutionHandle#get()'s contract needs revising.",
			failure);
		assertTrue("Expected the revision error of #405, but got: " + failure,
			String.valueOf(failure.getMessage()).contains("access revision"));
	}

	/**
	 * The fix: on that same thread, in that same tool call, the run answers for itself as plain
	 * data - run id, conversation, agent, model - with no {@code KnowledgeBase} access and therefore
	 * no revision to be wrong about.
	 */
	public void testToolReadsTheRunAsPlainDataFromTheSameThread() {
		ToolContext context = recordedRun();

		AtomicReference<RunInfo> seen = new AtomicReference<>();
		ToolExecutions.execute(tool(ctx -> {
			seen.set(ctx.runInfo());
			return ToolResult.of("done");
		}), new HashMap<>(), context);

		RunInfo run = seen.get();
		assertNotNull("the tool is handed a RunInfo", run);
		assertTrue("the tool knows it is in a run", run.isKnown());
		assertEquals("the run id the loop generated", RUN_ID, run.runId());
		assertEquals("the agent whose run this is - the read that threw in #405", AGENT,
			run.agentName());
		assertEquals("the model the run talks to", MODEL, run.model());
		assertNotNull("the conversation this run belongs to", run.conversationId());
		assertNull("a top-level run has no parent", run.parent());
		assertEquals("a top-level run is at depth 0", 0, run.delegationDepth());
	}

	/**
	 * The recorded row's id travels as plain data too, so a tool can hand the run's execution to
	 * something that resolves it on its own thread (the execution store does exactly that) without
	 * ever dereferencing the wrapper itself.
	 */
	public void testToolLearnsTheExecutionIdWithoutDereferencingTheRow() {
		ToolContext context = recordedRun();

		AtomicReference<String> seen = new AtomicReference<>();
		ToolExecutions.execute(tool(ctx -> {
			seen.set(ctx.runInfo().executionId());
			return ToolResult.of("done");
		}), new HashMap<>(), context);

		assertNotNull("a recorded run names its execution row", seen.get());
	}

	/**
	 * The one thing a tool <em>may</em> do with the unreadable wrapper: hand it back to the store,
	 * which resolves it on the thread that can see it. This is what the delegation tools need - the
	 * child run has to be recorded under the parent's session, and the parent's session is reachable
	 * only through the parent's execution row.
	 */
	public void testSessionOfResolvesTheRowWhereItIsVisible() {
		ToolContext context = recordedRun();
		AgentExecution execution = context.executionHandle().get();
		AgentExecutionStore store = AgentExecutionStore.getInstance();

		try {
			execution.getSession();
			fail("Reading the session off the wrapper on this thread must fail - that is the whole"
				+ " reason sessionOf exists.");
		} catch (RuntimeException expected) {
			assertTrue("Expected the revision error, but got: " + expected,
				String.valueOf(expected.getMessage()).contains("access revision"));
		}

		AgentSession session = store.sessionOf(execution);
		assertNotNull("the store resolves it on its writer thread", session);

		// And the result is usable as what it is - a token the store accepts back.
		AgentExecution child =
			store.beginExecution(session, "run-405-child", "child-agent", MODEL, null, execution);
		assertNotNull("a child run can be recorded under the resolved session", child);
	}

	/** A null execution resolves to no session rather than to an exception. */
	public void testSessionOfNullIsNull() {
		assertNull(AgentExecutionStore.getInstance().sessionOf(null));
	}

	/**
	 * The thread-bound view says the same thing as the context, for code beneath a tool that has no
	 * context to ask - and it is gone once the call returns, so a pooled thread never reports a
	 * stale run.
	 */
	public void testRunIsBoundForTheCallAndClearedAfterwards() {
		ToolContext context = recordedRun();

		assertFalse("nothing is bound before the call", RunInfoBinding.current().isKnown());

		AtomicReference<RunInfo> seen = new AtomicReference<>();
		ToolExecutions.execute(tool(ctx -> {
			seen.set(RunInfoBinding.current());
			return ToolResult.of("done");
		}), new HashMap<>(), context);

		assertEquals("the bound run is the context's run", RUN_ID, seen.get().runId());
		assertEquals("...including the agent", AGENT, seen.get().agentName());
		assertFalse("the binding is removed when the call returns",
			RunInfoBinding.current().isKnown());
	}

	/**
	 * A context for a run that has really been recorded: a session and an {@link AgentExecution} row
	 * written by the store's writer thread, published on the handle exactly as
	 * {@link ExecutionRecorder} publishes them during a live run.
	 */
	private static ToolContext recordedRun() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "run-info-test");

		// Pin this thread's view at the revision that has the session but not yet the execution -
		// exactly the situation a run is in. The chat servlet advances the session revision at
		// request begin and it then stays put for the whole request, while the store's writer
		// thread commits the AgentExecution row in a session of its own, at a newer revision.
		HistoryUtils.updateSessionRevision();

		CancellationToken cancellationToken = new CancellationToken();
		ToolContext context = new ToolContext(SecurityContext.UNRESTRICTED,
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellationToken), cancellationToken);

		AgentEventListener recorder = new ExecutionRecorder(session, AGENT, null,
			context.executionHandle(), event -> {
				// Nothing downstream: this test is about what the recorder publishes, not about
				// what a listener chain does with the events.
			});
		recorder.onEvent(new AgentEvent.RunStarted(RUN_ID, 0, Instant.now(), MODEL));
		return context;
	}

	private static Tool tool(Function<ToolExecutionContext, ToolResult> body) {
		return ToolStubs.builder("run_probe").description("Reports the run it executes in.")
			.schema(new ToolSchema("run_probe", "Reports the run it executes in.", "{}"))
			.execute((args, context) -> body.apply(context)).build();
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestRunInfoRevisionSafety.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
