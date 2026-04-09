/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;

/**
 * {@link AgentListener} that logs tool executions and permission denials for auditing.
 *
 * <p>Logs tool execution events at INFO level and errors/denials at WARNING level.</p>
 *
 * @see AgentListener
 */
public class AuditListener implements AgentListener {

	private static final Logger LOG = Logger.getLogger(AuditListener.class.getName());

	/** */
	@Override
	public void onToolCallComplete(ToolCall toolCall, ToolResultMessage result) {
		if (result.state() instanceof ToolState.Completed completed) {
			Duration duration = Duration.between(completed.startTime(), completed.endTime());
			LOG.log(Level.INFO,
				"Tool executed: " + toolCall.name() + " (" + duration.toMillis() + "ms)");
		}
	}

	/** */
	@Override
	public void onToolError(ToolCall toolCall, String error) {
		LOG.log(Level.WARNING,
			"Tool failed: " + toolCall.name() + " \u2014 " + error);
	}

	/** */
	@Override
	public void onPermissionDenied(ToolCall toolCall, String reason) {
		LOG.log(Level.WARNING,
			"Permission denied: " + toolCall.name() + " \u2014 " + reason);
	}
}
