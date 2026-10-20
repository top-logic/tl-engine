/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.observe;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;

/**
 * Learns of every agent run of this application, whatever started it - chat, TL-Script, scheduler,
 * MCP, A2A, a delegation or a subagent - from its start to its end.
 *
 * <p>
 * Configured in {@code <run-observers>} on {@code AgentRuntimeService}. An observer receives the
 * run's events from {@link AgentEvent.RunStarted} to {@link AgentEvent.RunCompleted} (outcome and
 * usage): every turn with the assistant's message, every tool call with its arguments, result and
 * duration, every approval and question. The streamed text and thinking deltas are left out; the
 * message they make up arrives with {@link AgentEvent.TurnCompleted}.
 * </p>
 *
 * <p>
 * Called on an observer thread of its own, never on the run's, in the order the run emitted the
 * events. An observer that throws is logged and called again with the next event; one that cannot
 * keep up loses events rather than slowing the run - see {@link ObserverQueue}.
 * </p>
 *
 * <p>
 * <b>Security:</b> an observer sees everything the run sees - what the user wrote, what the tools
 * read, under the rights of the run's account - and it runs in the application with no rights
 * check of its own. An observer that passes these events on to anything outside the application
 * must authenticate and authorize that itself.
 * </p>
 */
@FunctionalInterface
public interface RunObserver {

	/**
	 * One event of an observed run.
	 *
	 * @param run
	 *        The run the event belongs to; the same object for every event of one run.
	 * @param event
	 *        The event.
	 */
	void onEvent(ObservedRun run, AgentEvent event);
}
