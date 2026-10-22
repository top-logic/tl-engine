/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.observe;

import java.util.List;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.tool.api.RunInfo;

/**
 * The {@link RunObserver}s of this application, installed by {@code AgentRuntimeService} from its
 * {@code <run-observers>}, and asked by {@code AgentLoop} for every run it starts - the one point
 * every run passes, a subagent's and a script's included.
 */
public final class RunObservers {

	private static volatile Installed _installed;

	private record Installed(List<RunObserver> observers, ObserverQueue queue) {
		// Pure value.
	}

	private RunObservers() {
		// Static utility.
	}

	/**
	 * Replaces the observers of the runs started from now on; an empty list removes them.
	 */
	public static synchronized void install(List<RunObserver> observers) {
		Installed before = _installed;
		_installed = observers.isEmpty() ? null
			: new Installed(List.copyOf(observers), new ObserverQueue("ai-run-observers"));
		if (before != null) {
			before.queue().close();
		}
	}

	/**
	 * The listener a run reports to: {@code listener} itself while no observer is installed, else
	 * {@code listener} followed by a hand-over of each event to the observers' thread.
	 *
	 * @param context
	 *        The run's context; who the run is for is read from it when the run starts.
	 */
	public static AgentEventListener observe(ToolContext context, AgentEventListener listener) {
		Installed installed = _installed;
		if (installed == null) {
			return listener;
		}
		return new AgentEventListener() {
			private ObservedRun _run;

			@Override
			public void onEvent(AgentEvent event) {
				listener.onEvent(event);
				if (event instanceof AgentEvent.TextDelta || event instanceof AgentEvent.ThinkingDelta) {
					return;
				}
				if (event instanceof AgentEvent.RunStarted started) {
					// Read after the run's own listener had the event: a recorded run learns its
					// conversation there.
					_run = describe(context, started);
				}
				ObservedRun run = _run;
				if (run == null) {
					return;
				}
				installed.queue().deliver(installed.observers(), observer -> observer.onEvent(run, event));
			}
		};
	}

	private static ObservedRun describe(ToolContext context, AgentEvent.RunStarted started) {
		RunInfo info = context.runInfo();
		RunPrincipal principal = context.security().principal();
		return new ObservedRun(started.runId(), info.agentName(),
			principal == null || principal.account() == null ? null : principal.account().getName(),
			principal == null ? null : principal.entryPoint(), info.conversationId(),
			info.parent() == null ? null : info.parent().runId(), started.model());
	}
}
