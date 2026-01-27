/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.util.Map;

/**
 * One entry in an execution trace timeline: either a tool call or an agent-to-agent call.
 *
 * @see ToolCallTracker#getTimeline()
 */
public sealed interface TraceEntry permits TraceEntry.ToolCallTraceEntry, TraceEntry.AgentCallTraceEntry {

	/**
	 * Whether this entry is a tool call or an agent call.
	 */
	TraceEventKind getKind();

	/**
	 * Tool-call entry: name, args, result reference.
	 */
	record ToolCallTraceEntry(String toolName, Map<String, Object> args, String resultRef)
			implements TraceEntry {

		@Override
		public TraceEventKind getKind() {
			return TraceEventKind.TOOL_CALL;
		}
	}

	/**
	 * Agent-call entry: caller, callee, input summary, result summary.
	 */
	record AgentCallTraceEntry(String fromAgent, String toAgent, String inputSummary, String resultSummary)
			implements TraceEntry {

		@Override
		public TraceEventKind getKind() {
			return TraceEventKind.AGENT_CALL;
		}
	}
}
