/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

/**
 * Terminal state of an approval flow, carried by {@link ApprovalResult}.
 */
public enum ApprovalOutcome {

	/** User approved this single tool call. */
	APPROVED,

	/** User approved this tool for the remainder of the session. */
	APPROVED_FOR_SESSION,

	/** User rejected the call. */
	REJECTED,

	/** No response arrived within the approval timeout. */
	TIMED_OUT;
}
