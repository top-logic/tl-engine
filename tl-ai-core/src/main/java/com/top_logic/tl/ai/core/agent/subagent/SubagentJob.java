/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.time.Instant;

import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Mutable per-job state for one asynchronous subagent run. Holds a live
 * {@link CancellationToken}, so it is a runtime object (not a serializable DTO).
 */
public class SubagentJob {

	private final String _subagentId;

	private final String _targetAgentName;

	private final String _taskBrief;

	private final Instant _startedAt;

	private final CancellationToken _cancellationToken;

	private volatile SubagentJobStatus _status = SubagentJobStatus.QUEUED;

	private volatile SubagentResult _result;

	private volatile boolean _delivered;

	private volatile String _childRunId;

	/** */
	public SubagentJob(String subagentId, String targetAgentName, String taskBrief,
			CancellationToken cancellationToken) {
		_subagentId = subagentId;
		_targetAgentName = targetAgentName;
		_taskBrief = taskBrief;
		_cancellationToken = cancellationToken;
		_startedAt = Instant.now();
	}

	/** Unique identifier of this subagent job within its session. */
	public String subagentId() {
		return _subagentId;
	}

	/** Name of the delegated-to agent definition. */
	public String targetAgentName() {
		return _targetAgentName;
	}

	/** The task brief the parent gave this subagent. */
	public String taskBrief() {
		return _taskBrief;
	}

	/** When the job was registered. */
	public Instant startedAt() {
		return _startedAt;
	}

	/** Token observed cooperatively by the child loop to support cancellation. */
	public CancellationToken cancellationToken() {
		return _cancellationToken;
	}

	/** Current lifecycle state. */
	public SubagentJobStatus status() {
		return _status;
	}

	/** Terminal result envelope, or {@code null} while non-terminal. */
	public SubagentResult result() {
		return _result;
	}

	/** Whether the result has already been handed to the parent (injection or wait). */
	public boolean isDelivered() {
		return _delivered;
	}

	/**
	 * Run id of the recorded child {@link com.top_logic.tl.ai.core.execution.model.AgentExecution},
	 * or {@code null} when the run is not recorded. Lets the UI open the subagent's persisted
	 * transcript.
	 */
	public String childRunId() {
		return _childRunId;
	}

	/** Sets the {@link #childRunId() recorded child run id}. */
	public void setChildRunId(String childRunId) {
		_childRunId = childRunId;
	}

	/** Transition QUEUED -&gt; RUNNING. */
	public void markRunning() {
		_status = SubagentJobStatus.RUNNING;
	}

	/** Transition to a terminal state, storing the result envelope. */
	public void markTerminal(SubagentJobStatus terminal, SubagentResult result) {
		if (!terminal.isTerminal()) {
			throw new IllegalArgumentException("Not a terminal status: " + terminal);
		}
		_result = result;
		_status = terminal;
	}

	/** Marks the result as delivered to the parent (idempotent). */
	public void markDelivered() {
		_delivered = true;
	}
}
