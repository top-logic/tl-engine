/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

/**
 * Receives lifecycle events from an agent run.
 *
 * <p>The framework wraps the caller-supplied listener in an internal serializing
 * wrapper before emitting any event, which guarantees:</p>
 * <ul>
 *   <li>{@link #onEvent(AgentEvent)} is never called concurrently for a given
 *       listener instance, even when tools run on parallel virtual threads.</li>
 *   <li>Exceptions thrown by a listener are logged and swallowed — a buggy
 *       listener cannot abort an agent run.</li>
 * </ul>
 *
 * <p>Consumers typically use a pattern-matching switch over the sealed
 * {@link AgentEvent} hierarchy:</p>
 *
 * <pre>{@code
 * AgentEventListener listener = event -> {
 *     switch (event) {
 *         case AgentEvent.TextDelta d       -> ui.appendText(d.text());
 *         case AgentEvent.ToolCallStarted s -> ui.showToolCall(s.toolName());
 *         case AgentEvent.RunCompleted r    -> ui.finalize(r.reason());
 *         default -> { /* ignore *\/ }
 *     }
 * };
 * }</pre>
 *
 * @see AgentEvent
 * @see CompositeAgentEventListener
 */
@FunctionalInterface
public interface AgentEventListener {

	/**
	 * Called once per emitted event. Implementations should be fast and must not
	 * block the loop thread on I/O that could stall indefinitely.
	 */
	void onEvent(AgentEvent event);

	/**
	 * Whether events delivered to this listener also reach {@code target}, either because this
	 * listener <em>is</em> {@code target} or because it forwards to it (directly or transitively).
	 *
	 * <p>
	 * Lets a caller test whether a listener is already part of an assembled chain before wiring it
	 * in again, so the same listener is not invoked twice per event.
	 * </p>
	 *
	 * @param target
	 *        The listener to look for.
	 * @return {@code true} if this listener dispatches events to {@code target}.
	 */
	default boolean dispatchesTo(AgentEventListener target) {
		return this == target;
	}

	/**
	 * Wraps the given listener in a framework-internal wrapper that serializes
	 * dispatch and swallows listener exceptions.
	 *
	 * <p>{@link com.top_logic.tl.ai.core.agent.AgentLoop} calls this at the
	 * start of every run.</p>
	 */
	static AgentEventListener serializing(AgentEventListener delegate) {
		return new SerializingListener(delegate);
	}
}
