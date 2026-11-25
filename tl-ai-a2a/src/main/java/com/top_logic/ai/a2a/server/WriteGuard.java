/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.server;

import com.top_logic.ai.tool.api.Tool;
import com.top_logic.ai.core.agent.SecurityContext;
import com.top_logic.ai.core.security.SecurityContextProxy;

/**
 * The rights of an A2A run of an agent exposed with {@code allow-writes="false"}: those of the
 * background run, less every tool that changes the application.
 *
 * <p>
 * The guard only narrows. Reading, asking and {@code EXTERNAL} tools (calling another agent), whose
 * change lands in another system, keep the decision of the wrapped context: the grants of the
 * account and the {@code <background-allowlist>}. A run the agent delegates to keeps the rule
 * ({@link #copy}).
 * </p>
 */
final class WriteGuard extends SecurityContextProxy {

	WriteGuard(SecurityContext impl) {
		super(impl);
	}

	@Override
	protected SecurityContext copy(SecurityContext narrowed) {
		return new WriteGuard(narrowed);
	}

	@Override
	public boolean hasToolPermission(Tool tool) {
		return !tool.mode().writesApplicationState() && super.hasToolPermission(tool);
	}

	@Override
	public String toolDenialReason(Tool tool) {
		return tool.mode().writesApplicationState()
			? "Tool '" + tool.name() + "' is not available: this agent was asked by another agent, and"
				+ " this application does not let such requests change it. Answer with what you can"
				+ " read, and say what you would have changed."
			: super.toolDenialReason(tool);
	}
}
