/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

/**
 * Result of a user approval request.
 *
 * <p>When the agent loop pauses for user approval on a tool call, the outcome is one of:</p>
 * <ul>
 *   <li>{@link Approved} — tool executes this once</li>
 *   <li>{@link ApprovedForSession} — tool executes, and future calls to this tool
 *       are auto-approved for the remainder of this agent loop run</li>
 *   <li>{@link Rejected} — tool fails; optional comment forwarded to the LLM</li>
 *   <li>{@link TimedOut} — no response within the timeout; treated as rejection</li>
 * </ul>
 *
 * @see ApprovalCallback
 */
public sealed interface ApprovalResult {

	/** User approved the tool call. Execution proceeds. */
	record Approved() implements ApprovalResult {
	}

	/**
	 * User approved the tool call and all future calls to this tool in this session.
	 *
	 * <p>The agent loop caches this decision and skips approval for subsequent
	 * calls to the same tool name within the current run.</p>
	 */
	record ApprovedForSession() implements ApprovalResult {
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

	/**
	 * Approval timed out — no user response within the allotted time.
	 *
	 * <p>Treated as a rejection by the agent loop.</p>
	 */
	record TimedOut() implements ApprovalResult {
	}
}
