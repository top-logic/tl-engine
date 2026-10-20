/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.InteractionState;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.InteractionType;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.InteractionRequest;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.util.model.ModelService;

/**
 * Regression tests for the {@code askUser} / human-in-the-loop interaction path against the
 * "created after access revision N" bug (tl-ai 1.0.12): the recorder must record and resolve an
 * interaction row <em>without</em> dereferencing a persistent {@link InteractionRequest} wrapper on
 * the calling agent/tool thread, whose KB view is pinned to a revision that predates the row.
 *
 * <p>
 * These run against a real {@link com.top_logic.knowledge.service.KnowledgeBase} because the fix
 * hinges on where KB reads happen (writer thread at HEAD vs. caller thread at a pinned revision) —
 * something only a booted store can exercise. They do not run in the bare Maven sandbox; see
 * {@link com.top_logic.tl.ai.core.agent.subagent.TestAsyncDelegationIntegration} for the same
 * harness.
 * </p>
 */
public class TestInteractionRecorderRevision extends BasicTestCase {

	/**
	 * {@link InteractionRecorder#begin} returns a plain {@code requestId} handle (never a persistent
	 * wrapper), and {@link InteractionRecorder#resolve} closes the row located by that handle. The
	 * row must transition to its terminal state and carry the resolution.
	 *
	 * <p>
	 * Before the fix, {@code resolve} took the persistent wrapper and its best-effort catch logged
	 * {@code request.getRequestId()}, dereferencing the stale wrapper on the caller thread and
	 * re-throwing "created after access revision N" — which surfaced as "Tool 'askUser' failed" and
	 * hung the chat.
	 * </p>
	 */
	public void testBeginReturnsIdAndResolveClosesRow() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "askuser-revision-test");
		AgentExecution execution = store.beginExecution(session, "run-askuser", "orchestrator",
			"test-model", null, null);

		String handle = InteractionRecorder.begin(execution, InteractionType.CHOICE,
			"req-choice-1", "Choose", "Pick one", "[{\"label\":\"Option A\"}]");

		assertEquals("begin returns the plain requestId handle, not a wrapper",
			"req-choice-1", handle);

		// Resolve on the (stale) caller thread. Must not throw: best-effort and revision-safe.
		InteractionRecorder.resolve(handle, InteractionState.RESOLVED, "Option A");

		// Advance this thread's stable view to see the writer-thread commit (the chat servlet does
		// this at request begin), then verify the row was closed with the resolution.
		HistoryUtils.updateSessionRevision();
		InteractionRequest row = findByRequestId("req-choice-1");
		assertNotNull("The interaction row must have been persisted", row);
		assertEquals("Row must be resolved", InteractionState.RESOLVED.name(), row.getState().getName());
		assertEquals("Resolution must be stored", "Option A", row.getResolution());
		assertNotNull("Resolved timestamp must be set", row.getResolvedAt());
	}

	/**
	 * Resolving a {@code requestId} that was never persisted (best-effort recording was off or the
	 * row is gone) is a silent no-op — never an exception that could abort the tool.
	 */
	public void testResolveUnknownIdIsBestEffortNoOp() {
		InteractionRecorder.resolve("does-not-exist", InteractionState.CANCELLED, null);
		// Reaching here without an exception is the assertion.
	}

	/**
	 * A {@code null} handle (recording was disabled because the run is not recorded) resolves to a
	 * no-op.
	 */
	public void testResolveNullHandleIsNoOp() {
		InteractionRecorder.resolve(null, InteractionState.RESOLVED, "ignored");
	}

	private static InteractionRequest findByRequestId(String requestId) {
		for (TLObject obj : MetaElementUtil.getAllInstancesOf(
				TlAiExecutionFactory.getInteractionRequestType(), TLObject.class)) {
			InteractionRequest request = (InteractionRequest) obj;
			if (requestId.equals(request.getRequestId())) {
				return request;
			}
		}
		return null;
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestInteractionRecorderRevision.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
