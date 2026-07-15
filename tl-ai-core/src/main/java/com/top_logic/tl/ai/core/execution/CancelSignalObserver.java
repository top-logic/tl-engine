/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * {@link AgentEventListener} that turns a persisted cancel request into a local
 * {@link CancellationToken#cancel() token cancellation}, making async runs cancellable across a
 * cluster.
 *
 * <p>
 * A cancel is requested from any node by committing the running execution's
 * {@link AgentExecution#getCancelRequested() cancel marker} (see
 * {@link AgentExecutionStore#requestCancel(AgentExecution)}). This observer runs on the very node
 * (and thread) executing the job - it holds that run's {@link CancellationToken} - and, at each
 * agent-loop turn boundary, reads the marker at KB HEAD (see
 * {@link AgentExecutionStore#isCancelRequested(AgentExecution)}). When it finds the marker set it
 * cancels the token, which the agent loop already honors: the run stops and finalizes as
 * {@code CANCELLED}.
 * </p>
 *
 * <p>
 * The per-turn cadence is deliberate: the HEAD read is a fresh interaction routed through the
 * store's writer thread, so it must stay coarse (one check per model round-trip), not fire on
 * every micro-step. The loop's own {@link CancellationToken} checks (between turns, during tool
 * execution and during backoff waits) provide finer-grained promptness once the token is flipped.
 * </p>
 */
public final class CancelSignalObserver implements AgentEventListener {

	private final ExecutionHandle _handle;

	private final CancellationToken _token;

	/**
	 * Creates a {@link CancelSignalObserver}.
	 *
	 * @param handle
	 *        The run's execution holder; the observed {@link AgentExecution} appears here once the
	 *        run has started. Must not be <code>null</code>.
	 * @param token
	 *        The local cancellation token of the run to flip when a cancel is observed. Must not be
	 *        <code>null</code>.
	 */
	public CancelSignalObserver(ExecutionHandle handle, CancellationToken token) {
		_handle = handle;
		_token = token;
	}

	@Override
	public void onEvent(AgentEvent event) {
		if (event instanceof AgentEvent.TurnStarted) {
			pollCancelMarker();
		}
	}

	private void pollCancelMarker() {
		if (_token.isCancelled()) {
			return;
		}
		AgentExecution execution = _handle.get();
		if (execution == null || !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return;
		}
		try {
			if (AgentExecutionStore.getInstance().isCancelRequested(execution)) {
				_token.cancel();
			}
		} catch (RuntimeException ex) {
			// Observing the marker is best-effort: a failed read must not disturb the run.
			Logger.warn("Failed to read cancel marker for run " + execution.getRunId() + ": "
				+ ex.getMessage(), ex, CancelSignalObserver.class);
		}
	}
}
