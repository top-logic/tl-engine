/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Tests for {@link ExecutionRecorder} outside a running application: events are forwarded
 * unchanged, run id and terminal result are captured, and completing without an active
 * {@link AgentExecutionStore} is a safe no-op.
 */
public class TestExecutionRecorder extends TestCase {

	public void testForwardsAllEventsAndCapturesResult() {
		List<AgentEvent> forwarded = new ArrayList<>();
		ExecutionHandle handle = new ExecutionHandle();
		ExecutionRecorder recorder = new ExecutionRecorder(null, "test-agent", null, handle, forwarded::add);

		Instant now = Instant.now();
		TokenUsage usage = new TokenUsage(100, 50, 10, 0, 0);
		recorder.onEvent(new AgentEvent.RunStarted("run-1", 0, now, "test-model"));
		recorder.onEvent(new AgentEvent.TurnStarted("run-1", 1, now));
		recorder.onEvent(new AgentEvent.RunCompleted("run-1", 1, now, StopReason.COMPLETE, usage,
			Money.zero(Money.USD)));

		assertEquals(3, forwarded.size());
		assertEquals("run-1", recorder.runId());
		assertNotNull(recorder.result());
		assertEquals(StopReason.COMPLETE, recorder.result().stopReason());
		assertEquals(usage, recorder.result().totalUsage());
		assertEquals(1, recorder.result().turnsUsed());
	}

	public void testNoRecordingWithoutStore() {
		ExecutionHandle handle = new ExecutionHandle();
		ExecutionRecorder recorder = new ExecutionRecorder(null, "test-agent", null, handle, e -> {
			// ignore
		});

		recorder.onEvent(new AgentEvent.RunStarted("run-2", 0, Instant.now(), "test-model"));

		// The AgentExecutionStore module is not active in unit tests: no row is created,
		// the handle stays empty, and completing is a no-op instead of a failure.
		assertNull(recorder.execution());
		assertNull(handle.get());
		recorder.complete("{}", null);
	}

	public void testNoConversationIdWithoutSession() {
		// A recorder constructed without a session (the unrecorded path) publishes no conversation id;
		// the browser tool then keys per run, as before. Deriving it must not throw for a null session.
		assertNull("no conversation id derivable from a null session",
			ExecutionRecorder.conversationIdOf(null));

		ExecutionHandle handle = new ExecutionHandle();
		new ExecutionRecorder(null, "test-agent", null, handle, e -> {
			// ignore
		});
		assertNull("handle carries no conversation id when no session is given", handle.conversationId());
	}

	public void testCapturesProviderErrorAsFailureDetail() {
		ExecutionRecorder recorder = new ExecutionRecorder(null, "test-agent", null,
			new ExecutionHandle(), e -> {
				// ignore
			});

		Instant now = Instant.now();
		recorder.onEvent(new AgentEvent.RunStarted("run-3", 0, now, "test-model"));
		recorder.onEvent(new AgentEvent.ProviderError("run-3", 1, now, "RATE_LIMITED", "too many requests"));
		recorder.onEvent(new AgentEvent.RunCompleted("run-3", 1, now, StopReason.ERROR, TokenUsage.EMPTY,
			Money.zero(Money.USD)));

		assertEquals(StopReason.ERROR, recorder.result().stopReason());
	}

	/**
	 * A delegation the parent waits for writes the parent's transcript before its child asks for an
	 * approval - the turn on the user's screen is the parent's (#598). A detached subagent does not:
	 * the parent turn it came from has ended, and the subagent records its own transcript.
	 */
	public void testWaitedForDelegationCheckpointsTheParent() {
		AtomicInteger parentCheckpoints = new AtomicInteger();
		ToolContext parent = new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE);
		parent.executionHandle().setCheckpoint(parentCheckpoints::incrementAndGet);

		parent.forDelegation(TransactionScope.NOOP).executionHandle().checkpoint();
		assertEquals(1, parentCheckpoints.get());

		parent.forDetachedSubagent(TransactionScope.NOOP, new CancellationToken()).executionHandle().checkpoint();
		assertEquals("A detached subagent does not write its parent's transcript", 1, parentCheckpoints.get());
	}
}
