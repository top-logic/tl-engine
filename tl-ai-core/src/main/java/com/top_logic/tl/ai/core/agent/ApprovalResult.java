/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Result of a user approval request.
 *
 * <p>When the agent loop pauses for user approval on a tool call, the user either
 * approves (tool executes) or rejects (tool call fails with feedback to the LLM).
 * Rejection may include a comment that is forwarded to the LLM so it can adapt.</p>
 *
 * @see ApprovalCallback
 */
public sealed interface ApprovalResult {

	/** User approved the tool call. Execution proceeds. */
	record Approved() implements ApprovalResult {
	}

	/**
	 * User rejected the tool call.
	 *
	 * @param userComment optional comment from the user explaining the rejection;
	 *                    may be empty but never {@code null}
	 */
	record Rejected(String userComment) implements ApprovalResult {

		/** */
		public Rejected {
			if (userComment == null) {
				userComment = "";
			}
		}
	}
}
