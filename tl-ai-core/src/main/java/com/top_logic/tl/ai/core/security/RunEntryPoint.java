/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

/**
 * How an agent run was started.
 *
 * <p>
 * Part of what a {@link SecurityContextFactory} decides from, for an application whose answer
 * depends on where the call came from rather than only on who makes it. It says nothing about
 * authority by itself: what a run may do follows from its principal, and whether a human can answer
 * an approval prompt is stated separately.
 * </p>
 *
 * @see RunPrincipal
 */
public enum RunEntryPoint {

	/** The chat, where a user typed the message and is waiting for the answer. */
	CHAT,

	/** A TL-Script call - {@code aiAgentChat}, {@code aiCallTool}, {@code aiAgentChatAsync}, {@code aiResearch}. */
	SCRIPT,

	/** A request that arrived over the MCP endpoint with a credential of its own. */
	MCP,

	/**
	 * A request of another agent over the A2A endpoint ({@code tl-ai-a2a}), with a credential of its
	 * own. The calling agent answers what the run asks, so such a run is not a background run.
	 */
	A2A,

	/** A run a scheduler started, at a time nobody chose to be present for. */
	SCHEDULED,

	/**
	 * A run the product started on the user's behalf, which the user neither asked for nor can
	 * choose - the chat's prompt improver is the case this exists for.
	 */
	INTERNAL;

}
