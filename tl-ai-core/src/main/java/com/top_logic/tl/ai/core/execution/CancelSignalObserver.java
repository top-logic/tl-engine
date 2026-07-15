/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerService;
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
 * executing the job - it holds that run's {@link CancellationToken} - and reads the marker at KB
 * HEAD (see {@link AgentExecutionStore#isCancelRequested(AgentExecution)}). When it finds the marker
 * set it cancels the token, which the agent loop already honors between turns, during tool execution
 * and during backoff waits: the run stops and finalizes as {@code CANCELLED}.
 * </p>
 *
 * <p>
 * The marker is polled on a fixed time cadence for the whole lifetime of the run (started on
 * {@code RunStarted}, stopped on {@code RunCompleted}), <em>not</em> only at turn boundaries. A
 * turn-boundary-only check is not enough: a run that sits inside a single long turn - one long LLM
 * stream or a long-running tool call - never crosses a boundary, so the committed marker would never
 * be read and the run would stay {@code RUNNING} forever. The time-based poll guarantees the token
 * flips within a bounded delay regardless of where the run currently is. As an optimization the
 * marker is additionally polled on each {@code TurnStarted}, so a cancel that lands right before a
 * boundary takes effect immediately rather than waiting for the next tick.
 * </p>
 *
 * <p>
 * Cluster-safe: the request crosses nodes via the persisted marker; the running node observes it at
 * HEAD (a read routed through the store's writer-thread system interaction) and stops its own run.
 * The poll is coarse (one HEAD read per {@link #POLL_INTERVAL_MS} plus one per turn), keeping the
 * writer thread lightly loaded.
 * </p>
 */
public final class CancelSignalObserver implements AgentEventListener {

	/**
	 * How often the persisted cancel marker is polled at HEAD while the run is in flight. Bounds
	 * the worst-case delay between a cancel request and the local token being flipped when the run
	 * is busy inside a single long turn.
	 */
	static final long POLL_INTERVAL_MS = 1000;

	private final ExecutionHandle _handle;

	private final CancellationToken _token;

	/** The recurring poll while the run is in flight; guarded by {@code this}. */
	private ScheduledFuture<?> _pollTask;

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
		if (event instanceof AgentEvent.RunStarted) {
			startPolling();
		} else if (event instanceof AgentEvent.RunCompleted) {
			stopPolling();
		} else if (event instanceof AgentEvent.TurnStarted) {
			pollCancelMarker();
		}
	}

	/**
	 * Starts the recurring marker poll (idempotent). Runs only while
	 * {@link SchedulerService} is available; without it the per-turn poll still applies.
	 */
	private synchronized void startPolling() {
		if (_pollTask != null || !SchedulerService.Module.INSTANCE.isActive()) {
			return;
		}
		_pollTask = SchedulerService.getInstance().scheduleWithFixedDelay(this::pollCancelMarker,
			POLL_INTERVAL_MS, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
	}

	/** Stops the recurring marker poll (idempotent). */
	private synchronized void stopPolling() {
		if (_pollTask != null) {
			_pollTask.cancel(false);
			_pollTask = null;
		}
	}

	private void pollCancelMarker() {
		if (_token.isCancelled()) {
			stopPolling();
			return;
		}
		AgentExecution execution = _handle.get();
		if (execution == null || !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return;
		}
		try {
			if (AgentExecutionStore.getInstance().isCancelRequested(execution)) {
				_token.cancel();
				stopPolling();
			}
		} catch (RuntimeException ex) {
			// Observing the marker is best-effort: a failed read must not disturb the run.
			Logger.warn("Failed to read cancel marker of a running agent execution: " + ex.getMessage(),
				ex, CancelSignalObserver.class);
		}
	}
}
