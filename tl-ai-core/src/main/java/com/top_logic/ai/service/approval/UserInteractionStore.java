/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.approval;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Session-scoped store for pending user interaction requests.
 *
 * <p>
 * Thread-safe: the agent thread creates requests, the servlet thread resolves them.
 * Generalizes the former {@code ApprovalStore} to support all interaction types.
 * </p>
 */
public class UserInteractionStore {

	private final Map<String, UserInteractionRequest> _pending = new ConcurrentHashMap<>();
	private final Map<String, CompletableFuture<String>> _approvalFutures = new ConcurrentHashMap<>();
	private final AtomicInteger _nextId = new AtomicInteger(1);
	private final String _storeId = UUID.randomUUID().toString().substring(0, 8);
	private volatile ApprovalMode _mode;

	/** Creates a new store with {@link ApprovalMode#MANUAL} mode. */
	public UserInteractionStore() {
		this(ApprovalMode.MANUAL);
	}

	/** Creates a new store with the given approval mode. */
	public UserInteractionStore(ApprovalMode mode) {
		_mode = mode;
	}

	/** Returns the approval mode for this store. */
	public ApprovalMode getMode() {
		return _mode;
	}

	/** Updates the approval mode at runtime. */
	public void setMode(ApprovalMode mode) {
		_mode = mode;
	}

	/**
	 * Creates a new pending interaction request.
	 *
	 * @param question    The question to present.
	 * @param header      Short label.
	 * @param type        Interaction type: "approval", "text", or "choice".
	 * @param options     For "choice": JSON array of options. Null for other types.
	 * @param multiSelect For "choice": whether multiple selections are allowed.
	 * @return The created request with a fresh {@link CompletableFuture}.
	 */
	public UserInteractionRequest createRequest(String question, String header, String type,
			String options, boolean multiSelect) {
		String id = "interaction-" + _storeId + "-" + _nextId.getAndIncrement();
		CompletableFuture<String> future = new CompletableFuture<>();
		UserInteractionRequest request = new UserInteractionRequest(
			id, question, header, type, options, multiSelect, System.currentTimeMillis(), future);
		_pending.put(id, request);
		return request;
	}

	/**
	 * Registers a pending approval request under a caller-supplied ID.
	 *
	 * <p>
	 * Used by the {@link com.top_logic.tl.ai.core.agent.ApprovalCallback} bridge: the agent loop
	 * generates the approval request ID and emits it via SSE, then the callback registers a future
	 * under that same ID so the servlet can resolve it when the user responds.
	 * </p>
	 *
	 * @param approvalRequestId The ID to register under (matches the SSE event's
	 *                          {@code approvalRequestId}).
	 * @return A future that completes with the user's response string.
	 */
	public CompletableFuture<String> registerApproval(String approvalRequestId) {
		CompletableFuture<String> future = new CompletableFuture<>();
		_approvalFutures.put(approvalRequestId, future);
		return future;
	}

	/**
	 * Resolves a pending request with the user's response.
	 *
	 * @param requestId The request ID to resolve.
	 * @param response  The user's response string.
	 */
	public void resolve(String requestId, String response) {
		UserInteractionRequest request = _pending.remove(requestId);
		if (request != null) {
			request.getFuture().complete(response);
			return;
		}
		CompletableFuture<String> approvalFuture = _approvalFutures.remove(requestId);
		if (approvalFuture != null) {
			approvalFuture.complete(response);
		}
	}

	/** Returns all currently pending requests. */
	public List<UserInteractionRequest> getPendingRequests() {
		return List.copyOf(_pending.values());
	}

	/** Whether any requests are currently pending. */
	public boolean hasPending() {
		return !_pending.isEmpty();
	}
}
