/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import com.top_logic.tl.ai.core.agent.SecurityContext;

/**
 * Callback for requesting user approval before executing a tool.
 *
 * <p>The agent loop calls this when a tool requires user approval (based on
 * {@link SecurityContext#checkApproval}). The implementation blocks until the user
 * responds - the agent loop thread is already blocking by design. The loop opens the wait
 * first ({@link #open(ApprovalRequest)}), so that no answer can arrive before it.</p>
 *
 * <p>Core defines this interface; integrators implement it against their UI
 * (SSE, {@code WebSocket}, CLI prompt, etc.).</p>
 *
 * @see ApprovalRequest
 * @see ApprovalResult
 */
@FunctionalInterface
public interface ApprovalCallback {

	/**
	 * Request user approval for a tool call.
	 *
	 * <p>Blocks until the user approves or rejects.</p>
	 *
	 * @param request details of the tool call needing approval
	 * @return the user's decision
	 */
	ApprovalResult requestApproval(ApprovalRequest request);

	/**
	 * Opens the wait for an approval, before anybody is told that it is needed.
	 *
	 * <p>
	 * The agent loop calls this first, then announces the request
	 * ({@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ApprovalRequired}), records it, and
	 * only then {@link ApprovalWait#await() waits}. A callback whose answer arrives from somewhere
	 * else - a person clicking in a browser that heard the announcement - must be ready to take
	 * that answer from the moment it is announced: an answer that arrives before the wait is
	 * registered finds nobody to give it to and is lost, and the run waits for the timeout.
	 * Such a callback registers the wait here.
	 * </p>
	 *
	 * <p>
	 * The default opens nothing and asks {@link #requestApproval(ApprovalRequest)} when the loop
	 * waits - right for a callback that answers on the calling thread.
	 * </p>
	 *
	 * @param request
	 *        Details of the tool call needing approval.
	 * @return The wait. The caller closes it on every path, also when it never waited.
	 */
	default ApprovalWait open(ApprovalRequest request) {
		return () -> requestApproval(request);
	}

	/**
	 * An approval that is registered and may already have been answered.
	 *
	 * @see ApprovalCallback#open(ApprovalRequest)
	 */
	interface ApprovalWait extends AutoCloseable {

		/**
		 * Blocks until the user has answered, or until the wait ends without an answer.
		 *
		 * @return The user's decision.
		 */
		ApprovalResult await();

		/**
		 * Drops the registration, if the wait still holds one. Answering removes it already.
		 */
		@Override
		default void close() {
			// Nothing registered.
		}
	}

	/** Auto-approves all requests. For tests and auto-approve mode. */
	ApprovalCallback AUTO_APPROVE = request -> new ApprovalResult.Approved();
}
