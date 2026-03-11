/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.approval;

import java.util.List;
import java.util.Map;
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
	private final AtomicInteger _nextId = new AtomicInteger(1);
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
		String id = "interaction-" + _nextId.getAndIncrement();
		CompletableFuture<String> future = new CompletableFuture<>();
		UserInteractionRequest request = new UserInteractionRequest(
			id, question, header, type, options, multiSelect, System.currentTimeMillis(), future);
		_pending.put(id, request);
		return request;
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
