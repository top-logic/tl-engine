/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * What produces the answers of a conversation in place of the framework's own agent loop - a
 * process outside of this application, such as a coding agent in a container or a remote agent.
 *
 * <p>
 * The chat keeps everything around the run as it does for its own agent: the send lock and the
 * busy refusal, the user's message persisted before the run, the delivery of the run's events to
 * every window showing the conversation, stop, the recorded execution with its transcript and its
 * cost. A backend only answers: it is handed one user message and reports its work as
 * {@link AgentEvent}s.
 * </p>
 *
 * <p>
 * Which conversations a backend answers is decided by a {@link ConversationBackendResolver} of the
 * {@link ConversationBackendService}.
 * </p>
 *
 * <h2>Threading</h2>
 *
 * <p>
 * {@link #send(BackendTurn, AgentEventListener, CancellationToken)} is called on the chat's worker
 * thread, which carries the run's context: the user's sub-session, the run's conversation log
 * binding, the window's processing state. Every event the backend hands to the sink has to be
 * delivered on that thread, in the order of the run, before
 * {@link #send(BackendTurn, AgentEventListener, CancellationToken) send} returns. A backend that
 * reads its process's output on a thread of its own queues what it reads and drains that queue
 * inside {@link #send(BackendTurn, AgentEventListener, CancellationToken) send}. The sink refuses an
 * event from another thread, one after the call returned, and every event after a refused one, with
 * an {@link IllegalStateException}; the run then ends in an error.
 * </p>
 */
public interface ConversationBackend {

	/**
	 * The agent name the runs of this backend are recorded under, e.g. {@code claude-code}.
	 *
	 * @return Never <code>null</code>.
	 */
	String agentName();

	/**
	 * Which functions of the chat beyond sending a message this backend supports.
	 */
	BackendFeatures features();

	/**
	 * Answers one user message.
	 *
	 * <p>
	 * Blocks the calling chat worker thread until the run has ended. The events of the run go to
	 * {@code sink}: first an {@link AgentEvent.RunStarted}, last an {@link AgentEvent.RunCompleted}
	 * carrying the stop reason, the token usage and the cost. A run that returns without having
	 * emitted {@link AgentEvent.RunCompleted}, and a run that throws, ends in an error.
	 * </p>
	 *
	 * <p>
	 * A stop of the user cancels {@code cancel}, on the thread of the stop request; the backend
	 * listens with {@link CancellationToken#onCancel(Runnable)}, ends its process's work, emits
	 * {@link AgentEvent.RunCompleted} with {@link com.top_logic.tl.ai.core.agent.StopReason#ABORTED}
	 * and returns what it answered so far.
	 * </p>
	 *
	 * @param turn
	 *        The message to answer and the conversation it belongs to.
	 * @param sink
	 *        Receives the run's events, on the calling thread only.
	 * @param cancel
	 *        The run's cancellation token.
	 * @return The final answer text, shown as the agent's message; may be <code>null</code> or
	 *         empty for a run that answered nothing.
	 * @throws Exception
	 *         When the run failed; the chat reports it as an error.
	 */
	String send(BackendTurn turn, AgentEventListener sink, CancellationToken cancel) throws Exception;

}
