/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.observe;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.SecurityContextProxy;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Test case for {@link RunObservers}: an installed {@link RunObserver} learns of real runs, a child
 * run names its parent, and an observer that throws breaks nothing.
 */
public class TestRunObservers extends TestCase {

	/** The agent loop executes inside a TopLogic interaction. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestRunObservers.class)));
	}

	private static final String MODEL = "test-model";

	private final List<String> _seen = new CopyOnWriteArrayList<>();

	private final List<ObservedRun> _runs = new CopyOnWriteArrayList<>();

	private final CountDownLatch _ended = new CountDownLatch(2);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RunObservers.install(List.of(
			(run, event) -> {
				throw new IllegalStateException("A broken observer.");
			},
			(run, event) -> {
				_seen.add(run.runId() + ":" + event.getClass().getSimpleName());
				if (event instanceof AgentEvent.RunStarted) {
					_runs.add(run);
				}
				if (event instanceof AgentEvent.RunCompleted) {
					_ended.countDown();
				}
			}));
	}

	@Override
	protected void tearDown() throws Exception {
		RunObservers.install(List.of());
		super.tearDown();
	}

	/**
	 * A run and a run it delegates to are both observed from start to end, off the run's thread,
	 * with who they are for, and the child names its parent.
	 */
	public void testObserverLearnsOfRunAndChild() throws InterruptedException {
		ToolContext lead = new ToolContext(new Principal(new RunPrincipal(null, RunEntryPoint.A2A, false)),
			ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP, new CancellationToken());

		assertTrue(loop().run(asked("Hello"), lead, event -> {
			// The run's own listener.
		}).isComplete());
		assertTrue(loop().run(asked("Hello child"), lead.forDelegation(TransactionScope.NOOP), event -> {
			// The child's own listener.
		}).isComplete());

		assertTrue("Both runs must be observed to their end.", _ended.await(10, TimeUnit.SECONDS));
		assertEquals(2, _runs.size());
		ObservedRun parent = _runs.get(0);
		ObservedRun child = _runs.get(1);
		assertEquals(RunEntryPoint.A2A, parent.entryPoint());
		assertNull(parent.parentRunId());
		assertEquals(MODEL, parent.model());
		assertEquals(parent.runId(), child.parentRunId());
		assertEquals(RunEntryPoint.A2A, child.entryPoint());

		String start = parent.runId() + ":RunStarted";
		String turn = parent.runId() + ":TurnCompleted";
		String end = parent.runId() + ":RunCompleted";
		assertTrue(_seen.toString(), _seen.indexOf(start) < _seen.indexOf(turn));
		assertTrue(_seen.toString(), _seen.indexOf(turn) < _seen.indexOf(end));
		assertFalse("Streamed deltas are not observed.", _seen.toString().contains("TextDelta"));
	}

	/** Without an installed observer a run keeps its own listener. */
	public void testNothingInstalledKeepsTheListener() {
		RunObservers.install(List.of());
		AgentEventListener own = event -> {
			// Nothing.
		};
		assertSame(own, RunObservers.observe(ToolContext.EMPTY, own));
	}

	private static AgentLoop loop() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.freeze();
		return new AgentLoop(new AgentParams(MODEL, "You are a test assistant.", 5,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), (request, handler) -> {
				List<ContentPart> answer = List.of(new TextContent("Hi."));
				handler.onEvent(new StreamEvent.TextDelta(0, "Hi."));
				handler.onEvent(new StreamEvent.MessageComplete("msg", MODEL, "stop", TokenUsage.EMPTY, answer));
			}, catalog);
	}

	private static Conversation asked(String question) {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent(question))));
		return conversation;
	}

	/** A context that names the run it was built for. */
	private static final class Principal extends SecurityContextProxy {

		private final RunPrincipal _principal;

		Principal(RunPrincipal principal) {
			super(SecurityContext.UNRESTRICTED);
			_principal = principal;
		}

		@Override
		public RunPrincipal principal() {
			return _principal;
		}
	}
}
