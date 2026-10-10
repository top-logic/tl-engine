/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Session-scoped store for the interactions an agent run is waiting on.
 *
 * <p>
 * Holds two kinds, both discoverable and both resolvable through
 * {@link #resolve(String, String)}: questions the agent asked
 * ({@link #getPendingRequests()}) and tool approvals it needs
 * ({@link #getPendingApprovals()}).
 * </p>
 *
 * <p>
 * Thread-safe: the agent thread creates requests, the servlet thread resolves them.
 * Generalizes the former {@code ApprovalStore} to support all interaction types.
 * </p>
 *
 * <p>
 * A background subagent asks through a store of its own, which lives as long as the subagent rather
 * than as long as the turn that started it, and which names it as the {@link #asker()} of everything
 * it asks.
 * </p>
 */
public class UserInteractionStore {

	private final Map<String, UserInteractionRequest> _pending = new ConcurrentHashMap<>();
	private final Map<String, PendingApproval> _approvals = new ConcurrentHashMap<>();
	private final AtomicInteger _nextId = new AtomicInteger(1);
	private final String _storeId = UUID.randomUUID().toString().substring(0, 8);
	private final InteractionAsker _asker;

	/** Creates a store for the run the user started. */
	public UserInteractionStore() {
		this(null);
	}

	/**
	 * Creates a store whose every request names who asks.
	 *
	 * @param asker
	 *        The background subagent asking through this store, or <code>null</code> for the run the
	 *        user started.
	 */
	public UserInteractionStore(InteractionAsker asker) {
		_asker = asker;
	}

	/**
	 * The background subagent asking through this store, or <code>null</code> when it is the run the
	 * user started.
	 *
	 * @see UserInteractionRequest#getAskedBy()
	 */
	public InteractionAsker asker() {
		return _asker;
	}

	/**
	 * Creates a new pending interaction request.
	 *
	 * @param question    The question to present.
	 * @param header      Short label.
	 * @param type        Interaction type: "text" or "choice".
	 * @param options     For "choice": JSON array of options. Null for other types.
	 * @param multiSelect For "choice": whether multiple selections are allowed.
	 * @return The created request with a fresh {@link CompletableFuture}.
	 */
	public UserInteractionRequest createRequest(String question, String header, String type,
			String options, boolean multiSelect) {
		String id = "interaction-" + _storeId + "-" + _nextId.getAndIncrement();
		CompletableFuture<String> future = new CompletableFuture<>();
		UserInteractionRequest request = new UserInteractionRequest(
			id, question, header, type, options, multiSelect, System.currentTimeMillis(), future, _asker);
		_pending.put(id, request);
		return request;
	}

	/**
	 * Registers a pending approval the agent thread is about to block on.
	 *
	 * <p>
	 * Used by the {@link ApprovalCallback} bridge: the agent loop generates the approval request
	 * ID and emits it as an
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ApprovalRequired} event, then the
	 * callback registers a future under that same ID so the UI can resolve it when the user
	 * responds. Registering the whole {@link ApprovalRequest} - not just its ID - is what makes
	 * the approval discoverable through {@link #getPendingApprovals()} by a UI that never saw
	 * that event.
	 * </p>
	 *
	 * <p>
	 * The caller must drop the registration through {@link #discardApproval(String)} when its
	 * wait ends without a user response (timeout, interruption), so that a dead approval does not
	 * linger in {@link #getPendingApprovals()}.
	 * </p>
	 *
	 * @param request The approval to register. Its {@link ApprovalRequest#approvalRequestId()} is
	 *                the ID {@link #resolve(String, String)} expects.
	 * @return A future that completes with the user's response string.
	 */
	public CompletableFuture<String> registerApproval(ApprovalRequest request) {
		CompletableFuture<String> future = new CompletableFuture<>();
		_approvals.put(request.approvalRequestId(),
			new PendingApproval(request, System.currentTimeMillis(), future));
		return future;
	}

	/**
	 * Drops a registered approval without completing its future.
	 *
	 * <p>
	 * For the caller of {@link #registerApproval(ApprovalRequest)} to call when it stops waiting
	 * on its own (timeout, interruption). Resolving an approval through
	 * {@link #resolve(String, String)} already removes it, so this is a no-op then.
	 * </p>
	 *
	 * @param approvalRequestId The ID the approval was registered under.
	 */
	public void discardApproval(String approvalRequestId) {
		_approvals.remove(approvalRequestId);
	}

	/**
	 * Resolves a pending interaction request or approval with the user's response.
	 *
	 * @param requestId The ID of the {@link UserInteractionRequest} or {@link PendingApproval} to
	 *                  resolve.
	 * @param response  The user's response string.
	 */
	public void resolve(String requestId, String response) {
		resolve(requestId, response, null);
	}

	/**
	 * Resolves a pending interaction request with the options the user picked and what the user
	 * wrote in their own words.
	 *
	 * <p>
	 * For a choice question that the user answered outside its options, or beside them. An approval
	 * has no own words, and they are ignored for one.
	 * </p>
	 *
	 * @param requestId The ID of the {@link UserInteractionRequest} or {@link PendingApproval} to
	 *                  resolve.
	 * @param response  The user's response string: for a choice, the picked labels; may be empty
	 *                  when the user only wrote in their own words.
	 * @param ownWords  What the user typed, or <code>null</code>.
	 *
	 * @see UserInteractionRequest#getOwnWords()
	 */
	public void resolve(String requestId, String response, String ownWords) {
		UserInteractionRequest request = _pending.remove(requestId);
		if (request != null) {
			request.setOwnWords(ownWords);
			request.getFuture().complete(response);
			return;
		}
		PendingApproval approval = _approvals.remove(requestId);
		if (approval != null) {
			approval.future().complete(response);
		}
	}

	/**
	 * Whether this store is waiting for an answer to the given request.
	 *
	 * <p>
	 * The non-destructive half of {@link #resolve(String, String)}: it says whether this store is
	 * the one a given {@code requestId} belongs to, without taking the request. A surface that
	 * holds several runs - the chat servlet, which must find the run a window other than the one
	 * that started it is answering - asks this to pick the right store before resolving.
	 * </p>
	 *
	 * @param requestId
	 *        The ID of a {@link UserInteractionRequest} or a {@link PendingApproval}.
	 */
	public boolean isPending(String requestId) {
		return _pending.containsKey(requestId) || _approvals.containsKey(requestId);
	}

	/**
	 * The approval waiting under the given id, or <code>null</code> if none is.
	 *
	 * <p>
	 * What a caller asks when it needs more about a pending approval than its id: the
	 * {@link ApprovalRequest} carries the tool's {@link com.top_logic.tl.ai.tool.api.ToolMode},
	 * which is what a surface phrases the question from, and which no persisted record of the
	 * request holds.
	 * </p>
	 *
	 * @param approvalRequestId
	 *        The ID the approval was registered under.
	 */
	public PendingApproval pendingApproval(String approvalRequestId) {
		return approvalRequestId == null ? null : _approvals.get(approvalRequestId);
	}

	/**
	 * The question waiting under the given id, or <code>null</code> if none is.
	 *
	 * <p>
	 * The counterpart of {@link #pendingApproval(String)} for a question, and asked for the same
	 * reason: the {@link UserInteractionRequest} carries the shape the card is drawn from - whether
	 * a choice takes one answer or several - and no persisted record of the request holds it. A
	 * surface that has to rebuild the card without having seen the event that first drew it - a
	 * reloaded page - reads the shape from here.
	 * </p>
	 *
	 * @param requestId
	 *        The ID the request was created under.
	 */
	public UserInteractionRequest pendingRequest(String requestId) {
		return requestId == null ? null : _pending.get(requestId);
	}

	/**
	 * Returns all currently pending interaction requests, i.e. the questions the agent asked
	 * through {@link com.top_logic.tl.ai.core.tools.interaction.AskUserTool}.
	 *
	 * <p>
	 * Pending tool approvals are <em>not</em> part of this list; they have their own shape and are
	 * returned by {@link #getPendingApprovals()}.
	 * </p>
	 */
	public List<UserInteractionRequest> getPendingRequests() {
		return List.copyOf(_pending.values());
	}

	/**
	 * Returns all tool approvals currently waiting for a user decision.
	 *
	 * <p>
	 * Each entry can be answered through {@link #resolve(String, String)} using its
	 * {@link PendingApproval#id()} and one of the response strings the registering
	 * {@link ApprovalCallback} understands (for the chat UI: {@code "APPROVED"},
	 * {@code "APPROVED_FOR_SESSION"}, {@code "REJECTED"} or {@code "REJECTED:<comment>"}).
	 * </p>
	 */
	public List<PendingApproval> getPendingApprovals() {
		return List.copyOf(_approvals.values());
	}

	/** Whether any interaction requests are currently pending. */
	public boolean hasPending() {
		return !_pending.isEmpty();
	}

	/** Whether any tool approvals are currently waiting for a user decision. */
	public boolean hasPendingApprovals() {
		return !_approvals.isEmpty();
	}
}
