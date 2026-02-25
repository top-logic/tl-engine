/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.util.Map;

/**
 * One entry in an execution trace timeline: either a tool call or an
 * agent-to-agent call.
 *
 * @see ToolCallTracker#getTimeline()
 */
public interface TraceEntry {

	/**
	 * Whether this entry is a tool call or an agent call.
	 */
	TraceEventKind getKind();

	/**
	 * Tool-call entry: name, args, result reference.
	 *
	 * @param toolName
	 *                  The name of the tool that was called.
	 * @param args
	 *                  The arguments passed to the tool.
	 * @param resultRef
	 *                  The result reference (e.g., "T1", "T2") for this tool call.
	 */
	public static final class ToolCallTraceEntry implements TraceEntry {
		private final String toolName;
		private final Map<String, Object> args;
		private final String resultRef;

		public ToolCallTraceEntry(String toolName, Map<String, Object> args, String resultRef) {
			this.toolName = toolName;
			this.args = args;
			this.resultRef = resultRef;
		}

		public String toolName() {
			return toolName;
		}

		public Map<String, Object> args() {
			return args;
		}

		public String resultRef() {
			return resultRef;
		}

		@Override
		public TraceEventKind getKind() {
			return TraceEventKind.TOOL_CALL;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ToolCallTraceEntry that = (ToolCallTraceEntry) o;
			return java.util.Objects.equals(toolName, that.toolName) &&
					java.util.Objects.equals(args, that.args) &&
					java.util.Objects.equals(resultRef, that.resultRef);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(toolName, args, resultRef);
		}

		@Override
		public String toString() {
			return "ToolCallTraceEntry[" +
					"toolName=" + toolName + ", " +
					"args=" + args + ", " +
					"resultRef=" + resultRef + ']';
		}
	}

	/**
	 * Agent-call entry: caller, callee, input summary, result summary.
	 *
	 * @param fromAgent
	 *                      Name of the calling agent (e.g. "Orchestrator").
	 * @param toAgent
	 *                      Name of the called agent (e.g. "ModelExplorer").
	 * @param inputSummary
	 *                      Short summary of the work order or input sent.
	 * @param resultSummary
	 *                      Short summary of the result returned.
	 */
	public static final class AgentCallTraceEntry implements TraceEntry {
		private final String fromAgent;
		private final String toAgent;
		private final String inputSummary;
		private final String resultSummary;

		public AgentCallTraceEntry(String fromAgent, String toAgent, String inputSummary, String resultSummary) {
			this.fromAgent = fromAgent;
			this.toAgent = toAgent;
			this.inputSummary = inputSummary;
			this.resultSummary = resultSummary;
		}

		public String fromAgent() {
			return fromAgent;
		}

		public String toAgent() {
			return toAgent;
		}

		public String inputSummary() {
			return inputSummary;
		}

		public String resultSummary() {
			return resultSummary;
		}

		@Override
		public TraceEventKind getKind() {
			return TraceEventKind.AGENT_CALL;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AgentCallTraceEntry that = (AgentCallTraceEntry) o;
			return java.util.Objects.equals(fromAgent, that.fromAgent) &&
					java.util.Objects.equals(toAgent, that.toAgent) &&
					java.util.Objects.equals(inputSummary, that.inputSummary) &&
					java.util.Objects.equals(resultSummary, that.resultSummary);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(fromAgent, toAgent, inputSummary, resultSummary);
		}

		@Override
		public String toString() {
			return "AgentCallTraceEntry[" +
					"fromAgent=" + fromAgent + ", " +
					"toAgent=" + toAgent + ", " +
					"inputSummary=" + inputSummary + ", " +
					"resultSummary=" + resultSummary + ']';
		}
	}

	/**
	 * Mutable agent-call entry: recorded when the call starts (before the sub-agent
	 * runs),
	 * so it appears before that agent's tool calls in the timeline. Result summary
	 * is set when
	 * the call completes.
	 */
	public static final class MutableAgentCallTraceEntry implements TraceEntry {
		private final String _fromAgent;
		private final String _toAgent;
		private final String _inputSummary;
		private volatile String _resultSummary;

		public MutableAgentCallTraceEntry(String fromAgent, String toAgent, String inputSummary, String resultSummary) {
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
		 *                      Short summary of the result returned.
		 */
		public void setResultSummary(String resultSummary) {
			_resultSummary = resultSummary != null ? resultSummary : "";
		}
	}
}
