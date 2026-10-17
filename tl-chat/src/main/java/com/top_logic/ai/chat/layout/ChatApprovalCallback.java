/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.RunCancellationBinding;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;

/**
 * The approval callback of a chat run: asks the person in the chat, or approves by itself while the
 * chat is switched to {@link ApprovalMode#AUTO_APPROVE}.
 *
 * <p>
 * The switch is read at every call, not when the run starts: a person who switches auto-approve on
 * during a turn is not asked for the next write of that turn, and one who switches it off is
 * (#943). {@code POST /chat/approval-mode} brings the run's own {@link ChatState} in line, from
 * whatever window of the account it is sent.
 * </p>
 *
 * <p>
 * The wait is registered in {@link #open(ApprovalRequest)}, which the agent loop calls before it
 * announces the request, so an answer that comes the moment the card is shown finds the run waiting
 * (#944).
 * </p>
 */
final class ChatApprovalCallback implements ApprovalCallback {

	private final UserInteractionStore _runStore;

	private final ChatState _chat;

	/**
	 * Creates a {@link ChatApprovalCallback}.
	 *
	 * @param runStore
	 *        The store of the run's turn; used where the asking thread has no store bound.
	 * @param chat
	 *        The run's own chat, whose switch decides at every call.
	 */
	ChatApprovalCallback(UserInteractionStore runStore, ChatState chat) {
		_runStore = runStore;
		_chat = chat;
	}

	@Override
	public ApprovalResult requestApproval(ApprovalRequest request) {
		try (ApprovalWait wait = open(request)) {
			return wait.await();
		}
	}

	@Override
	public ApprovalWait open(ApprovalRequest request) {
		if (_chat.approvalMode() == ApprovalMode.AUTO_APPROVE) {
			return ApprovalResult.Approved::new;
		}
		Logger.info("Approval requested for tool '" + request.toolName()
			+ "' (mode=" + request.mode() + ", approvalRequestId="
			+ request.approvalRequestId() + "). Blocking until user responds.",
			ChatApprovalCallback.class);
		// The store bound on the asking thread, not the one this run was started with: a background
		// subagent inherits this callback, and it asks through its job's own store, which outlives
		// this run's turn. On the run's own thread both are the same.
		UserInteractionStore bound = UserInteractionStoreBinding.getStore();
		UserInteractionStore store = bound != null ? bound : _runStore;
		CompletableFuture<String> future = store.registerApproval(request);
		return new ApprovalWait() {
			@Override
			public ApprovalResult await() {
				return awaitAnswer(request, future);
			}

			@Override
			public void close() {
				// Nobody waits on this future any more. Answering removes the registration already,
				// this drops it on every other exit so that a timed-out approval does not linger as a
				// phantom in getPendingApprovals().
				store.discardApproval(request.approvalRequestId());
			}
		};
	}

	private static ApprovalResult awaitAnswer(ApprovalRequest request, CompletableFuture<String> future) {
		try {
			// Stopping the asking run ends this wait - the lead, or the background subagent that
			// asks on its own thread with its own token.
			String response = RunCancellationBinding.await(future,
				request.timeout().toMillis(), TimeUnit.MILLISECONDS);
			Logger.info("Approval response for '" + request.toolName()
				+ "': " + response, ChatApprovalCallback.class);
			return mapApprovalResponse(response);
		} catch (TimeoutException e) {
			Logger.warn("Approval timed out for tool '" + request.toolName() + "'.",
				ChatApprovalCallback.class);
			return new ApprovalResult.TimedOut();
		} catch (CancellationException e) {
			// The run was stopped; the loop sees its token and ends the turn.
			return new ApprovalResult.TimedOut();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return new ApprovalResult.TimedOut();
		} catch (ExecutionException e) {
			Logger.error("Approval future failed for tool '" + request.toolName()
				+ "': " + e.getMessage(), e, ChatApprovalCallback.class);
			return new ApprovalResult.TimedOut();
		}
	}

	/**
	 * Maps the raw response string from the UI to an {@link ApprovalResult}.
	 */
	private static ApprovalResult mapApprovalResponse(String response) {
		if (response == null) {
			return new ApprovalResult.TimedOut();
		}
		return switch (response) {
			case "APPROVED" -> new ApprovalResult.Approved();
			case "APPROVED_FOR_SESSION" -> new ApprovalResult.ApprovedForSession();
			default -> {
				if (response.startsWith("REJECTED")) {
					String comment = response.length() > "REJECTED".length()
						? response.substring("REJECTED:".length()).trim()
						: "";
					yield new ApprovalResult.Rejected(comment);
				}
				yield new ApprovalResult.Rejected(response);
			}
		};
	}
}
