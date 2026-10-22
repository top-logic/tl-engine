/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.tools.interaction.AskUserTool;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Regression tests for the subagent cancellation lifecycle.
 *
 * <p>
 * Encodes the failure the user hit: a subagent parked on a human-in-the-loop question that is
 * cancelled (directly, or transitively when the orchestrator is cancelled via
 * {@link SubagentJobRegistry#cancelAll()}) must terminate promptly instead of hanging until the
 * interaction timeout. The mechanism under test is real: {@link AskUserTool#askUser} registers a
 * {@link CancellationToken#onCancel(Runnable)} listener that resolves its pending request.
 * </p>
 *
 * <p>
 * No LLM, KB, or started module is required: without an active execution store the interaction
 * <em>persistence</em> is a no-op, while the in-memory {@link UserInteractionStore} future flow
 * (the part being cancelled) is fully exercised.
 * </p>
 */
public class TestSubagentCancellation extends TestCase {

	/** Generous ceiling that is still far below the 5-minute interaction timeout. */
	private static final long TERMINATE_TIMEOUT_MS = 10_000L;

	/**
	 * A blocked {@link AskUserTool#askUser} call unblocks promptly when its context's token is
	 * cancelled, returning the "cancelled" outcome rather than hanging (Bug 2 core, and the
	 * subagent side of Bug 1).
	 */
	public void testAskUserUnblocksOnCancel() throws Exception {
		UserInteractionStore store = new UserInteractionStore();
		CancellationToken token = new CancellationToken();
		ToolContext context = new ToolContext(SecurityContext.UNRESTRICTED,
			ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP, token);

		AtomicReference<String> response = new AtomicReference<>();
		Thread worker = new Thread(() -> {
			try (UserInteractionStoreBinding.Binding b = UserInteractionStoreBinding.bind(store)) {
				response.set(new AskUserTool().askUser(
					"Which option?", "Choose", "text", null, null, context));
			}
		}, "test-askUser");
		worker.setDaemon(true);
		worker.start();

		awaitPending(store);
		token.cancel();

		worker.join(TERMINATE_TIMEOUT_MS);
		assertFalse("askUser must return promptly after cancel, not hang", worker.isAlive());
		assertNotNull("askUser returned a response", response.get());
		assertTrue("cancelled response; got: " + response.get(),
			response.get().toLowerCase().contains("cancelled"));
		assertFalse("the pending request was resolved on cancel", store.hasPending());
	}

	/**
	 * Cancelling the orchestrator (modelled by {@link SubagentJobRegistry#cancelAll()}) terminates
	 * a subagent that is parked on a user question; the job reaches a terminal state well within
	 * the interaction timeout, and its pending question is resolved (Bug 1).
	 */
	public void testCancelAllTerminatesSubagentAwaitingQuestion() throws Exception {
		UserInteractionStore store = new UserInteractionStore();
		SubagentJobRegistry registry = new SubagentJobRegistry(2);
		try {
			SubagentJob job = registry.register("asker", "ask something", new CancellationToken());
			registry.spawn(job, j -> {
				try (UserInteractionStoreBinding.Binding b = UserInteractionStoreBinding.bind(store)) {
					ToolContext context = new ToolContext(SecurityContext.UNRESTRICTED,
						ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP, j.cancellationToken());
					String answer = new AskUserTool().askUser(
						"Proceed?", "Confirm", "text", null, null, context);
					return new SubagentResult(answer, TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
				}
			});

			// The subagent is now parked on its question.
			awaitPending(store);
			assertEquals("subagent is running while it waits for the answer",
				SubagentJobStatus.RUNNING, job.status());

			// Orchestrator cancel: propagate to the still-running subagent.
			assertEquals(1, registry.cancelAll());

			List<SubagentResult> results = registry.awaitBlocking(
				List.of(job.subagentId()), TERMINATE_TIMEOUT_MS);
			assertTrue("subagent must reach a terminal state, not hang; status=" + job.status(),
				job.status().isTerminal());
			assertEquals("its result was delivered", 1, results.size());
			assertFalse("the pending question was resolved on cancel", store.hasPending());
		} finally {
			registry.shutdown();
		}
	}

	/** Waits until the store has a pending request (the worker parked on {@code askUser}). */
	private static void awaitPending(UserInteractionStore store) throws InterruptedException {
		long deadline = System.currentTimeMillis() + TERMINATE_TIMEOUT_MS;
		while (!store.hasPending() && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertTrue("the worker must have raised a pending question", store.hasPending());
	}
}
