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
public sealed interface TraceEntry permits TraceEntry.ToolCallTraceEntry, TraceEntry.AgentCallTraceEntry,
		TraceEntry.MutableAgentCallTraceEntry {

	/**
	 * Whether this entry is a tool call or an agent call.
	 */
	TraceEventKind getKind();

	/**
	 * Tool-call entry: name, args, result reference.
	 *
	 * @param toolName
	 *        The name of the tool that was called.
	 * @param args
	 *        The arguments passed to the tool.
	 * @param resultRef
	 *        The result reference (e.g., "T1", "T2") for this tool call.
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
	 *
	 * @param fromAgent
	 *        Name of the calling agent (e.g. "Orchestrator").
	 * @param toAgent
	 *        Name of the called agent (e.g. "ModelExplorer").
	 * @param inputSummary
	 *        Short summary of the work order or input sent.
	 * @param resultSummary
	 *        Short summary of the result returned.
	 */
	record AgentCallTraceEntry(String fromAgent, String toAgent, String inputSummary, String resultSummary)
			implements TraceEntry {

		@Override
		public TraceEventKind getKind() {
			return TraceEventKind.AGENT_CALL;
		}
	}

	/**
	 * Mutable agent-call entry: recorded when the call starts (before the sub-agent runs),
	 * so it appears before that agent's tool calls in the timeline. Result summary is set when
	 * the call completes.
	 */
	final class MutableAgentCallTraceEntry implements TraceEntry {
		private final String _fromAgent;
		private final String _toAgent;
		private final String _inputSummary;
		private volatile String _resultSummary;

		MutableAgentCallTraceEntry(String fromAgent, String toAgent, String inputSummary, String resultSummary) {
			_fromAgent = fromAgent != null ? fromAgent : "";
			_toAgent = toAgent != null ? toAgent : "";
			_inputSummary = inputSummary != null ? inputSummary : "";
			_resultSummary = resultSummary != null ? resultSummary : "";
		}

		@Override
		public TraceEventKind getKind() {
			return TraceEventKind.AGENT_CALL;
		}

		/**
		 * Name of the calling agent (e.g. "Orchestrator").
		 */
		public String fromAgent() {
			return _fromAgent;
		}

		/**
		 * Name of the called agent (e.g. "ModelExplorer").
		 */
		public String toAgent() {
			return _toAgent;
		}

		/**
		 * Short summary of the work order or input sent.
		 */
		public String inputSummary() {
			return _inputSummary;
		}

		/**
		 * Short summary of the result returned.
		 */
		public String resultSummary() {
			return _resultSummary;
		}

		/**
		 * Sets the result summary when the agent call completes.
		 *
		 * @param resultSummary
		 *        Short summary of the result returned.
		 */
		public void setResultSummary(String resultSummary) {
			_resultSummary = resultSummary != null ? resultSummary : "";
		}
	}
}
