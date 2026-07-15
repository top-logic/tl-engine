/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Integration tests for the async agent TL-Script functions
 * {@link AIScriptFunctions#agentChatAsync(String, Object)},
 * {@link AIScriptFunctions#agentRunStatus(Object)} and
 * {@link AIScriptFunctions#agentRunResult(Object)}.
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
 * {@link #testKickoffReturnsHandleAndRecordsError()} exercises the end-to-end kickoff: it either
 * verifies the immediate handle and eventual {@code ERROR} recording (when the test runs in an
 * interactive user context) or the fail-fast guard (when there is no usable sub-session).
 * </p>
 */
public class TestAsyncAgentScriptFunctions extends BasicTestCase {

	private static final String TRANSCRIPT = "{\"turns\":[]}";

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

		assertEquals(ExecutionStatus.RUNNING.name(), AIScriptFunctions.agentRunStatus(session));
		assertNull(AIScriptFunctions.agentRunResult(session));
	}

	/**
	 * A run that finishes {@code COMPLETE} exposes its status and transcript through the handle.
	 */
	public void testCompleteRunExposesTranscript() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "complete-run");
		AgentExecution execution =
			_store.beginExecution(session, "run-complete", "test-agent", "test-model", null, null);
		advance();
		assertEquals(ExecutionStatus.RUNNING.name(), AIScriptFunctions.agentRunStatus(session));
		assertNull("result not available while running", AIScriptFunctions.agentRunResult(session));

		_store.finishExecution(execution, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null,
			TRANSCRIPT);
		advance();

		assertEquals(ExecutionStatus.COMPLETE.name(), AIScriptFunctions.agentRunStatus(session));
		assertEquals(TRANSCRIPT, AIScriptFunctions.agentRunResult(session));
	}

	/**
	 * A run that finishes {@code ERROR} is retrievable as such and yields no result transcript.
	 */
	public void testErroredRunRecordedAsError() throws Exception {
		AgentSession session = _store.createSession(SessionKind.BACKGROUND, null, "errored-run");
		AgentExecution execution =
			_store.beginExecution(session, "run-error", "test-agent", "test-model", null, null);
		_store.finishExecution(execution, ExecutionStatus.ERROR, StopReason.ERROR, "boom", null,
			TRANSCRIPT);
		advance();

		assertEquals(ExecutionStatus.ERROR.name(), AIScriptFunctions.agentRunStatus(session));
		assertNull("no result for a non-COMPLETE run", AIScriptFunctions.agentRunResult(session));
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
			TRANSCRIPT);
		advance();

		assertEquals(ExecutionStatus.COMPLETE.name(), AIScriptFunctions.agentRunStatus("run-42"));
		assertEquals(TRANSCRIPT, AIScriptFunctions.agentRunResult("run-42"));
		assertEquals(ExecutionStatus.COMPLETE.name(), AIScriptFunctions.agentRunStatus(execution));
		assertEquals(TRANSCRIPT, AIScriptFunctions.agentRunResult(execution));
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
		assertEquals(ExecutionStatus.RUNNING.name(), AIScriptFunctions.agentRunStatus(sessionId));
		assertNull(AIScriptFunctions.agentRunResult(sessionId));
		assertSame(session, _store.findSessionById(sessionId));

		AgentExecution execution =
			_store.beginExecution(session, "run-by-session-id", "test-agent", "test-model", null, null);
		_store.finishExecution(execution, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null,
			TRANSCRIPT);
		advance();

		assertEquals(ExecutionStatus.COMPLETE.name(), AIScriptFunctions.agentRunStatus(sessionId));
		assertEquals(TRANSCRIPT, AIScriptFunctions.agentRunResult(sessionId));
	}

	/**
	 * An unresolvable handle (unknown type, or an unknown run id) yields {@code null} rather than
	 * throwing.
	 */
	public void testUnknownHandleResolvesNull() {
		assertNull(AIScriptFunctions.agentRunStatus(Integer.valueOf(7)));
		assertNull(AIScriptFunctions.agentRunResult(Integer.valueOf(7)));
		assertNull(AIScriptFunctions.agentRunStatus("no-such-run-id"));
		assertNull(AIScriptFunctions.agentRunResult("no-such-run-id"));
		assertNull(AIScriptFunctions.agentRunStatus(null));
		assertNull(AIScriptFunctions.agentRunResult(null));
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
		assertNull(AIScriptFunctions.agentRunResult(handle));
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
			status = AIScriptFunctions.agentRunStatus(handle);
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
