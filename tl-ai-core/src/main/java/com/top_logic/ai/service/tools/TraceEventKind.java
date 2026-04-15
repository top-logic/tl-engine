/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

/**
 * Kind of entry in an execution trace timeline.
 */
public enum TraceEventKind {

	/** A tool was invoked ({@code toolName}, args, result). */
	TOOL_CALL,

	/** One agent delegated to another ({@code fromAgent}, {@code toAgent}, work order summary, result summary). */
	AGENT_CALL,

	/** Intermediate text output from the LLM (produced alongside tool calls, before the final answer). */
	OUTPUT
}
