/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.Map;

/**
 * Immutable result returned by a background agent after processing a trigger.
 *
 * <p>
 * The result carries a {@link Status} indicating whether the agent passed, found an issue, or wants
 * to block the current turn. Additional context is available via {@link #message()},
 * {@link #reason()}, and {@link #details()}.
 * </p>
 *
 * @param status
 *        The outcome status, never {@code null}.
 * @param message
 *        A human-readable message describing the result.
 * @param reason
 *        The reason for blocking, empty if not a block result.
 * @param details
 *        Additional structured details, never {@code null}.
 *
 * @see ResultParser
 */
public record AgentResult(Status status, String message, String reason, Map<String, Object> details) {

	/**
	 * The outcome status of a background agent run.
	 */
	public enum Status {

		/** The agent found no issues. */
		PASS,

		/** The agent found an issue that should be reported but does not block the turn. */
		ISSUE_FOUND,

		/** The agent wants to block the current turn from proceeding. */
		BLOCK
	}

	/**
	 * Creates a passing result with no message or details.
	 *
	 * @return A new {@link AgentResult} with {@link Status#PASS}.
	 */
	public static AgentResult pass() {
		return new AgentResult(Status.PASS, "", "", Map.of());
	}

	/**
	 * Creates an issue-found result with the given message.
	 *
	 * @param message
	 *        A human-readable description of the issue found.
	 * @return A new {@link AgentResult} with {@link Status#ISSUE_FOUND}.
	 */
	public static AgentResult issueFound(String message) {
		return new AgentResult(Status.ISSUE_FOUND, message, "", Map.of());
	}

	/**
	 * Creates a block result with the given reason and optional message template.
	 *
	 * <p>
	 * If the {@code blockMessageTemplate} contains the placeholder {@code {reason}}, it is replaced
	 * with the given reason. If the template is {@code null} or empty, the reason itself is used as
	 * the message.
	 * </p>
	 *
	 * @param reason
	 *        The reason for blocking.
	 * @param blockMessageTemplate
	 *        An optional message template containing {@code {reason}}, or empty/null.
	 * @return A new {@link AgentResult} with {@link Status#BLOCK}.
	 */
	public static AgentResult block(String reason, String blockMessageTemplate) {
		String safeReason = reason != null ? reason : "";
		String msg;
		if (blockMessageTemplate != null && !blockMessageTemplate.isEmpty()) {
			msg = blockMessageTemplate.replace("{reason}", safeReason);
		} else {
			msg = safeReason;
		}
		return new AgentResult(Status.BLOCK, msg, safeReason, Map.of());
	}

	/**
	 * Returns a copy of this result with a different message.
	 *
	 * @param newMessage
	 *        The new message to use.
	 * @return A new {@link AgentResult} with the same status, reason, and details but a different
	 *         message.
	 */
	public AgentResult withBlockMessage(String newMessage) {
		return new AgentResult(status, newMessage, reason, details);
	}
}
