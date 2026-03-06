/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.approval;

import java.util.concurrent.CompletableFuture;

/**
 * A pending user interaction request that blocks agent execution until the user responds.
 *
 * <p>
 * Generalizes the former {@code ApprovalRequest} to support multiple interaction types:
 * approval (approve/reject), free text input, and option selection.
 * </p>
 */
public class UserInteractionRequest {

	private final String _id;
	private final String _question;
	private final String _header;
	private final String _type;
	private final String _options;
	private final boolean _multiSelect;
	private final long _createdAt;
	private final CompletableFuture<String> _future;

	/**
	 * Creates a new {@link UserInteractionRequest}.
	 *
	 * @param id        Unique request identifier (e.g. {@code "interaction-1"}).
	 * @param question  The question to present to the user.
	 * @param header    Short label (e.g. {@code "Approval"}, {@code "Clarify"}).
	 * @param type      Interaction type: {@code "approval"}, {@code "text"}, or {@code "choice"}.
	 * @param options     For {@code "choice"}: JSON array of options. {@code null} for other types.
	 * @param multiSelect For {@code "choice"}: whether multiple selections are allowed.
	 * @param createdAt   Creation timestamp in milliseconds.
	 * @param future      The future the agent thread blocks on. Completed with the user's response string.
	 */
	public UserInteractionRequest(String id, String question, String header, String type,
			String options, boolean multiSelect, long createdAt, CompletableFuture<String> future) {
		_id = id;
		_question = question;
		_header = header;
		_type = type;
		_options = options;
		_multiSelect = multiSelect;
		_createdAt = createdAt;
		_future = future;
	}

	/** Unique request identifier. */
	public String getId() { return _id; }

	/** The question to present. */
	public String getQuestion() { return _question; }

	/** Short label for the interaction. */
	public String getHeader() { return _header; }

	/** Interaction type: "approval", "text", or "choice". */
	public String getType() { return _type; }

	/** For "choice": JSON array of options. Null for other types. */
	public String getOptions() { return _options; }

	/** For "choice": whether multiple selections are allowed. */
	public boolean isMultiSelect() { return _multiSelect; }

	/** Creation timestamp in milliseconds. */
	public long getCreatedAt() { return _createdAt; }

	/** The future the agent thread blocks on. */
	public CompletableFuture<String> getFuture() { return _future; }
}
