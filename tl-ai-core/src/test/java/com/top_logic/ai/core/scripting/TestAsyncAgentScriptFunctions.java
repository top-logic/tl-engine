/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.scripting;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.agent.event.AgentEvent;
import com.top_logic.ai.core.cost.Money;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.CancelSignalObserver;
import com.top_logic.ai.core.execution.ExecutionHandle;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Integration tests for the async agent TL-Script functions
 * {@link AIScriptFunctions#agentChatAsync(String, Object)},
 * {@link AIScriptFunctions#agentRunStatus(Object, boolean)} and
 * {@link AIScriptFunctions#agentRunResult(Object, boolean)}.
 *
 * <p>
 * The status/result tests drive the exact persistence path an async run writes to - the
 * {@link AgentExecutionStore} - and assert that a handle resolves to the recorded status and
 * transcript. Because the store commits on its own writer-thread session, each read first advances
 * the caller's session revision via {@link HistoryUtils#updateSessionRevision()}, mirroring how the
 * chat servlet advances the revision per request so polling sees the background writes.
 * </p>
 *
 * <p>
 * The functions are called without access checks, as a scheduler task calls them: these tests are
 * about what a handle resolves to. Who may read and stop a run is the subject of
 * {@link com.top_logic.ai.core.security.TestRunHandleAccessPolicy}.
 * </p>
 *
 * <p>
 * {@link #testKickoffReturnsHandleAndRecordsError()} exercises the end-to-end kickoff: it either
 * verifies the immediate handle and eventual {@code ERROR} recording (when the test runs in an
 * interactive user context) or the fail-fast guard (when there is no usable sub-session).
 * </p>
 */
public class TestAsyncAgentScriptFunctions extends BasicTestCase {

	private static final String TRANSCRIPT = "{\"turns\":[]}";

	private static final String RESPONSE = "The final answer.";

	private AgentExecutionStore _store;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_store = AgentExecutionStore.getInstance();
	}

	/**
	 * A freshly created session with no execution recorded yet - the state right after
	 * {@link AIScriptFunctions#agentChatAsync(String, Object)} returns - resolves to
	 * {@code RUNNING} with no result.
	 */
	public void testHandleWithoutExecutionIsRunning() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "no-execution-yet");
		advance();

		assertEquals(ExecutionStatus.RUNNING.name(), AIScriptFunctions.agentRunStatus(session, false));
		assertNull(AIScriptFunctions.agentRunResult(session, false));
	}

	/**
	 * A run that finishes {@code COMPLETE} exposes its status, final answer and transcript through
	 * the handle - and {@code agentRunResult} returns the final answer, not the transcript.
	 */
	public void testCompleteRunExposesResultAndTranscript() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "complete-run");
		AgentExecution execution =
			_store.beginExecution(session, "run-complete", "test-agent", "test-model", null, null);
		advance();
		assertEquals(ExecutionStatus.RUNNING.name(), AIScriptFunctions.agentRunStatus(session, false));
		assertNull("result not available while running", AIScriptFunctions.agentRunResult(session, false));

		_store.finishExecution(execution, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null,
			TRANSCRIPT, RESPONSE);
		advance();

		assertEquals(ExecutionStatus.COMPLETE.name(), AIScriptFunctions.agentRunStatus(session, false));
		assertEquals("result is the final answer", RESPONSE, AIScriptFunctions.agentRunResult(session, false));
		assertEquals("transcript is the full log", TRANSCRIPT,
			AIScriptFunctions.agentRunTranscript(session, false));
		assertFalse("result must not be the transcript",
			TRANSCRIPT.equals(AIScriptFunctions.agentRunResult(session, false)));
	}

	/**
	 * A run that finishes {@code ERROR} is retrievable as such, and asking it for its answer fails
	 * with the recorded message instead of reading as an absent answer (Gitea #389).
	 */
	public void testErroredRunRecordedAsError() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "errored-run");
		AgentExecution execution =
			_store.beginExecution(session, "run-error", "test-agent", "test-model", null, null);
		_store.finishExecution(execution, ExecutionStatus.ERROR, StopReason.ERROR, "boom", null,
			TRANSCRIPT);
		advance();

		assertEquals(ExecutionStatus.ERROR.name(), AIScriptFunctions.agentRunStatus(session, false));
		assertEquals("the error is readable without being raised", "boom",
			AIScriptFunctions.agentRunError(session, false));
		try {
			AIScriptFunctions.agentRunResult(session, false);
			fail("A failed run must not report its absent answer as a value.");
		} catch (TopLogicException ex) {
			assertTrue("The provider's own message belongs in the failure: " + ex.getMessage(),
				ex.getMessage().contains("boom"));
		}
		assertEquals("the transcript stays readable for diagnosis", TRANSCRIPT,
			AIScriptFunctions.agentRunTranscript(session, false));
	}

	/** A run that is still going reports no error, and no answer either. */
	public void testRunningRunHasNoErrorAndNoResult() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "still-running");
		_store.beginExecution(session, "run-still-going", "test-agent", "test-model", null, null);
		advance();

		assertEquals(ExecutionStatus.RUNNING.name(), AIScriptFunctions.agentRunStatus(session, false));
		assertNull(AIScriptFunctions.agentRunResult(session, false));
		assertNull(AIScriptFunctions.agentRunError(session, false));
	}

	/**
	 * The status/result functions also accept a run-id string and an {@link AgentExecution}
	 * directly, not only the {@link AgentSession} handle.
	 */
	public void testResolveByRunIdAndExecution() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "by-run-id");
		AgentExecution execution =
			_store.beginExecution(session, "run-42", "test-agent", "test-model", null, null);
		_store.finishExecution(execution, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null,
			TRANSCRIPT, RESPONSE);
		advance();

		assertEquals(ExecutionStatus.COMPLETE.name(), AIScriptFunctions.agentRunStatus("run-42", false));
		assertEquals(RESPONSE, AIScriptFunctions.agentRunResult("run-42", false));
		assertEquals(TRANSCRIPT, AIScriptFunctions.agentRunTranscript("run-42", false));
		assertEquals(ExecutionStatus.COMPLETE.name(), AIScriptFunctions.agentRunStatus(execution, false));
		assertEquals(RESPONSE, AIScriptFunctions.agentRunResult(execution, false));
		assertEquals(TRANSCRIPT, AIScriptFunctions.agentRunTranscript(execution, false));
	}

	/**
	 * The string session-id handle returned by {@code agentChatAsync} - a persistent object id, not
	 * a run id - resolves through the poll functions: {@code RUNNING} before any execution is
	 * visible, then the recorded status and transcript once the run finishes. This is the path the
	 * fix relies on to avoid returning a live {@link AgentSession} wrapper (which throws
	 * {@code DeletedObjectAccess} across revisions).
	 */
	public void testResolveByStringSessionId() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "by-session-id");
		String sessionId = KBUtils.getWrappedObjectName(session).toString();
		advance();

		// Before any execution row is visible, the string session id still reads as RUNNING (queued),
		// unlike an unknown string which is null.
		assertEquals(ExecutionStatus.RUNNING.name(), AIScriptFunctions.agentRunStatus(sessionId, false));
		assertNull(AIScriptFunctions.agentRunResult(sessionId, false));
		assertSame(session, _store.findSessionById(sessionId));

		AgentExecution execution =
			_store.beginExecution(session, "run-by-session-id", "test-agent", "test-model", null, null);
		_store.finishExecution(execution, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null,
			TRANSCRIPT, RESPONSE);
		advance();

		assertEquals(ExecutionStatus.COMPLETE.name(), AIScriptFunctions.agentRunStatus(sessionId, false));
		assertEquals(RESPONSE, AIScriptFunctions.agentRunResult(sessionId, false));
		assertEquals(TRANSCRIPT, AIScriptFunctions.agentRunTranscript(sessionId, false));
	}

	/**
	 * A handle resolves to the session's <em>top-level</em> execution ({@code parentExecution ==
	 * null}), not merely its most recent one: a session that accrues a later sub-agent (child)
	 * execution must still report the top-level run's answer.
	 */
	public void testResolutionHitsTopLevelExecution() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "with-subagent");
		String sessionId = KBUtils.getWrappedObjectName(session).toString();

		AgentExecution topLevel =
			_store.beginExecution(session, "run-top", "top-agent", "test-model", null, null);
		_store.finishExecution(topLevel, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null,
			TRANSCRIPT, RESPONSE);
		// A sub-agent execution recorded later under the same session, with the top-level run as
		// its parent. It started after the top-level, so a "latest execution" resolution would pick
		// it - the top-level resolution must not.
		AgentExecution child =
			_store.beginExecution(session, "run-child", "sub-agent", "test-model", null, topLevel);
		_store.finishExecution(child, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null,
			"{\"turns\":[{}]}", "child answer");
		advance();

		assertEquals("handle must resolve to the top-level run's answer", RESPONSE,
			AIScriptFunctions.agentRunResult(sessionId, false));
		assertEquals(TRANSCRIPT, AIScriptFunctions.agentRunTranscript(sessionId, false));
	}

	/**
	 * {@code agentRunCancel} sets and commits the persisted cancel marker of a still-running
	 * top-level execution (a normal KB write, hence cluster-safe), and is a no-op for a terminal
	 * or unknown run.
	 */
	public void testCancelSetsAndCommitsMarker() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "cancel-marker");
		String sessionId = KBUtils.getWrappedObjectName(session).toString();
		AgentExecution execution =
			_store.beginExecution(session, "run-cancel", "test-agent", "test-model", null, null);
		advance();

		assertTrue("a running run is signalled", AIScriptFunctions.agentRunCancel(sessionId, false));
		// The marker is committed: it is visible at HEAD (the store reads it on its writer thread)
		// and, after advancing the caller revision, on the execution wrapper itself.
		assertTrue("marker read at HEAD", _store.isCancelRequested(execution));
		advance();
		assertTrue("marker visible on the wrapper after advancing", execution.getCancelRequested());

		assertFalse("re-cancelling an already-signalled run is a no-op",
			AIScriptFunctions.agentRunCancel(sessionId, false));

		_store.finishExecution(execution, ExecutionStatus.CANCELLED, StopReason.ABORTED, null, null,
			TRANSCRIPT, null);
		advance();
		assertFalse("cancelling a terminal run is a no-op",
			AIScriptFunctions.agentRunCancel(sessionId, false));
		assertFalse("cancelling an unknown handle is a no-op",
			AIScriptFunctions.agentRunCancel("no-such-run", false));
	}

	/**
	 * The cluster-safe cancel path end to end at the observer level: a cancel requested through the
	 * persisted marker (as {@code agentRunCancel} does, possibly from another node) is observed by
	 * the {@link CancelSignalObserver} running on the job's node at its next turn and flips the
	 * local {@link CancellationToken}, which the agent loop honors to stop the run.
	 */
	public void testCancelObserverFlipsTokenFromMarker() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "cancel-observer");
		AgentExecution execution =
			_store.beginExecution(session, "run-observed", "test-agent", "test-model", null, null);

		ExecutionHandle handle = new ExecutionHandle();
		handle.set(execution);
		CancellationToken token = new CancellationToken();
		CancelSignalObserver observer = new CancelSignalObserver(handle, token);

		Instant now = Instant.now();
		// No cancel requested yet: a turn boundary leaves the token untouched.
		observer.onEvent(new AgentEvent.TurnStarted("run-observed", 1, now));
		assertFalse(token.isCancelled());

		// Request cancel (the marker is committed) and let the next turn boundary observe it.
		assertTrue(AIScriptFunctions.agentRunCancel(execution, false));
		observer.onEvent(new AgentEvent.TurnStarted("run-observed", 2, now));
		assertTrue("observer flips the local token from the committed marker", token.isCancelled());
	}

	/**
	 * A cancel requested while the run sits inside a single long turn - no {@code TurnStarted}
	 * boundary occurs while a long LLM stream or tool call is in flight - must still take effect
	 * within a bounded delay. This is the regression guard for the bug where cancellation was only
	 * observed at turn boundaries: a busy run never crossed one, so the committed marker was never
	 * read and the local token never flipped (the run stayed {@code RUNNING} forever).
	 */
	public void testCancelTakesEffectWithoutTurnBoundary() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "cancel-long-turn");
		AgentExecution execution =
			_store.beginExecution(session, "run-long-turn", "test-agent", "test-model", null, null);

		ExecutionHandle handle = new ExecutionHandle();
		handle.set(execution);
		CancellationToken token = new CancellationToken();
		CancelSignalObserver observer = new CancelSignalObserver(handle, token);

		Instant now = Instant.now();
		try {
			// The run starts, then sits inside one long turn: only RunStarted is emitted, never a
			// subsequent TurnStarted.
			observer.onEvent(new AgentEvent.RunStarted("run-long-turn", 0, now, "test-model"));

			// A cancel is requested from (possibly) another node: the marker is committed.
			assertTrue(AIScriptFunctions.agentRunCancel(execution, false));

			// The observer must flip the local token within a bounded delay despite no turn boundary.
			assertTrue("cancel must take effect without a turn boundary",
				awaitCancelled(token, 15_000));
		} finally {
			observer.onEvent(new AgentEvent.RunCompleted("run-long-turn", 1, now, StopReason.ABORTED,
				com.top_logic.ai.core.message.TokenUsage.EMPTY, Money.zero(Money.USD)));
		}
	}

	/**
	 * The timer of the observer does not wait for a busy writer of the store. It runs on the few
	 * threads that the scheduler shares with the whole application: while the writer is held and
	 * more runs observe their markers than the scheduler has threads, a task given to the scheduler
	 * still runs at once.
	 */
	public void testBusyWriterDoesNotHoldTheScheduler() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "busy-writer");
		AgentExecution execution =
			_store.beginExecution(session, "run-busy-writer", "test-agent", "test-model", null, null);
		ExecutionHandle handle = new ExecutionHandle();
		handle.set(execution);

		CountDownLatch writerBusy = new CountDownLatch(1);
		CountDownLatch releaseWriter = new CountDownLatch(1);
		// The supplier of a checkpoint is asked on the writer thread: it holds the writer.
		assertTrue(_store.checkpointTranscript(execution, () -> {
			writerBusy.countDown();
			try {
				releaseWriter.await(60, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			return null;
		}));
		List<CancelSignalObserver> observers = new ArrayList<>();
		try {
			assertTrue("The writer did not start the checkpoint.", writerBusy.await(10, TimeUnit.SECONDS));
			// More runs than the scheduler has threads: ticks that waited for the writer would hold
			// every thread.
			int runs = SchedulerService.getInstance().getConfig().getCorePoolSize() + 2;
			for (int i = 0; i < runs; i++) {
				CancelSignalObserver observer = new CancelSignalObserver(handle, new CancellationToken());
				observers.add(observer);
				observer.onEvent(new AgentEvent.RunStarted("run-busy-writer", 0, Instant.now(), "test-model"));
			}
			// Past the first two ticks of every timer.
			Thread.sleep(2500);

			CountDownLatch ran = new CountDownLatch(1);
			SchedulerService.getInstance().submit(ran::countDown);
			assertTrue("The marker reads of the runs hold the threads of the scheduler.",
				ran.await(5, TimeUnit.SECONDS));
		} finally {
			releaseWriter.countDown();
			for (CancelSignalObserver observer : observers) {
				observer.close();
			}
		}
	}

	/** Waits up to {@code timeoutMs} for the token to become cancelled. */
	private static boolean awaitCancelled(CancellationToken token, long timeoutMs) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			if (token.isCancelled()) {
				return true;
			}
			Thread.sleep(50);
		}
		return token.isCancelled();
	}

	/**
	 * An unresolvable handle (unknown type, or an unknown run id) yields {@code null} rather than
	 * throwing.
	 */
	public void testUnknownHandleResolvesNull() {
		assertNull(AIScriptFunctions.agentRunStatus(Integer.valueOf(7), false));
		assertNull(AIScriptFunctions.agentRunResult(Integer.valueOf(7), false));
		assertNull(AIScriptFunctions.agentRunTranscript(Integer.valueOf(7), false));
		assertFalse(AIScriptFunctions.agentRunCancel(Integer.valueOf(7), false));
		assertNull(AIScriptFunctions.agentRunStatus("no-such-run-id", false));
		assertNull(AIScriptFunctions.agentRunResult("no-such-run-id", false));
		assertNull(AIScriptFunctions.agentRunTranscript("no-such-run-id", false));
		assertFalse(AIScriptFunctions.agentRunCancel("no-such-run-id", false));
		assertNull(AIScriptFunctions.agentRunStatus(null, false));
		assertNull(AIScriptFunctions.agentRunResult(null, false));
		assertNull(AIScriptFunctions.agentRunTranscript(null, false));
		assertFalse(AIScriptFunctions.agentRunCancel(null, false));
	}

	/**
	 * End-to-end kickoff: in an interactive user context, {@code agentChatAsync} returns
	 * immediately with a {@code BACKGROUND} handle and the background run of an unknown agent is
	 * recorded as {@code ERROR}. Without a usable sub-session it fails fast with a clear error
	 * instead of silently running as a system user.
	 */
	public void testKickoffReturnsHandleAndRecordsError() throws Exception {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		Person person = subSession != null ? subSession.getPerson() : null;

		if (subSession == null || person == null) {
			try {
				AIScriptFunctions.agentChatAsync("__no_such_agent__", "hello");
				fail("Expected agentChatAsync to fail fast without a usable sub-session.");
			} catch (IllegalStateException expected) {
				// Fail-closed guard: no silent fallback to a system user.
			}
			return;
		}

		String handle = AIScriptFunctions.agentChatAsync("__no_such_agent__", "hello");
		assertNotNull("async kickoff must return a handle immediately", handle);

		String status = pollUntilTerminal(handle);
		assertEquals("failed background run must be recorded as ERROR",
			ExecutionStatus.ERROR.name(), status);
		assertNotNull("the failure must be readable through the handle",
			AIScriptFunctions.agentRunError(handle, false));
		try {
			AIScriptFunctions.agentRunResult(handle, false);
			fail("A failed run must not report its absent answer as a value.");
		} catch (TopLogicException expected) {
			// The run never answered; asking for its answer says so.
		}
	}

	/**
	 * Advances the caller's session revision so subsequently visible writes include everything the
	 * store's writer thread has committed, then polls the run status until it leaves
	 * {@code RUNNING} or a timeout elapses.
	 */
	private static String pollUntilTerminal(String handle) throws Exception {
		String status = null;
		for (int i = 0; i < 150; i++) {
			advance();
			status = AIScriptFunctions.agentRunStatus(handle, false);
			if (status != null && !ExecutionStatus.RUNNING.name().equals(status)) {
				return status;
			}
			Thread.sleep(100);
		}
		return status;
	}

	/** Advances the caller's session revision to KB HEAD so writer-thread commits become visible. */
	private static void advance() {
		HistoryUtils.updateSessionRevision();
	}

	/**
	 * Creates the test suite with the required service dependencies (KB + ModelService +
	 * AgentExecutionStore + SchedulerService).
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAsyncAgentScriptFunctions.class),
				ModelService.Module.INSTANCE,
				com.top_logic.basic.sched.SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
