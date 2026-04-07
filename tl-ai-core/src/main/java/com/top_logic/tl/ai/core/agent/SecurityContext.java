/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.core.tool.Tool;

/**
 * Coarse-grained security context for tool execution.
 *
 * <p>The framework uses this for tool-level visibility checks (can this user invoke
 * this tool at all?). Fine-grained, data-level security (argument-dependent or
 * result-dependent filtering) is the tool's responsibility — the tool receives
 * the {@link SecurityContext} via {@link ToolContext} and uses it internally.</p>
 *
 * <p>Implemented in {@code tl-ai-service} against TopLogic's security system.
 * The core framework only depends on this interface.</p>
 *
 * @see ToolContext
 */
public interface SecurityContext {

	/**
	 * Whether the current user is allowed to invoke the given tool.
	 *
	 * <p>This is a coarse check — it answers "can this user use this tool at all?"
	 * not "can this user use this tool with these specific arguments?" The latter
	 * is handled inside the tool itself.</p>
	 *
	 * @param tool the tool to check
	 * @return {@code true} if the user may invoke the tool
	 */
	boolean canUseTool(Tool tool);

	/** Context with no restrictions — all tools are allowed. For tests and trusted internal use. */
	SecurityContext UNRESTRICTED = tool -> true;
}
