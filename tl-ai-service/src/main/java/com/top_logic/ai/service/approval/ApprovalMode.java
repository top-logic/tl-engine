/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.approval;

/**
 * Controls how tool calls are approved in the human-in-the-loop workflow.
 */
public enum ApprovalMode {

	/** Each tool call requires manual user approval. */
	MANUAL,

	/** All tool calls are auto-approved without user interaction. */
	AUTO_APPROVE;
}
