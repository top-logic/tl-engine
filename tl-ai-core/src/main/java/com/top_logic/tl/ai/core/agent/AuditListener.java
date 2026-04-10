/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;

/**
 * {@link AgentEventListener} that logs tool executions, permission denials, and
 * transaction/lock lifecycle events for auditing.
 *
 * <p>Tool execution events are logged at INFO; denials and rollbacks at WARNING.
 * Every other event is ignored so the audit log stays focused on security-relevant
 * activity.</p>
 */
public class AuditListener implements AgentEventListener {

	private static final Logger LOG = Logger.getLogger(AuditListener.class.getName());

	/** */
	@Override
	public void onEvent(AgentEvent event) {
		switch (event) {
			case AgentEvent.ToolCallCompleted c ->
				LOG.log(Level.INFO,
					"Tool executed: " + c.toolName() + " (" + c.duration().toMillis() + "ms)");
			case AgentEvent.ToolCallFailed f ->
				LOG.log(Level.WARNING, "Tool failed: " + f.toolName() + " \u2014 " + f.errorMessage());
			case AgentEvent.ToolCallDenied d ->
				LOG.log(Level.WARNING, "Permission denied: " + d.toolName() + " \u2014 " + d.reason());
			case AgentEvent.TransactionBegan t ->
				LOG.log(Level.INFO, "Transaction started.");
			case AgentEvent.TransactionCommitted c -> {
				if (c.revisionNumber() > 0) {
					LOG.log(Level.INFO, "Transaction committed (revision " + c.revisionNumber() + ").");
				} else {
					LOG.log(Level.INFO, "Transaction committed (no changes).");
				}
			}
			case AgentEvent.TransactionRolledBack r ->
				LOG.log(Level.WARNING, "Transaction rolled back: " + r.reason());
			case AgentEvent.LockAcquired l ->
				LOG.log(Level.INFO, "Edit lock acquired.");
			case AgentEvent.LockFailed l ->
				LOG.log(Level.WARNING, "Edit lock failed: " + l.reason());
			default -> {
				// Other events are not audit-relevant.
			}
		}
	}
}
