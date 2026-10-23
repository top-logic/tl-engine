/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import com.top_logic.basic.util.I18NBundle;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;

/**
 * Translator of the protocol of one agent process: encodes what is written to the process and
 * maps the lines it writes to {@link AgentEvent}s.
 *
 * <p>
 * Every method is called on the thread of the run, one at a time - the chat's worker thread in
 * {@link WorkspaceAgent#send(com.top_logic.ai.chat.backend.BackendTurn, AgentEventListener,
 * com.top_logic.tl.ai.tool.api.CancellationToken)}, which drains the lines the process wrote.
 * </p>
 *
 * <p>
 * A run starts with {@link #startRun(String, I18NBundle, AgentEventListener)} and ends either with a line for
 * which {@link #onLine(String)} answers {@code true}, or with
 * {@link #endRun(StopReason, String, String)}. Either way the run
 * has emitted {@link AgentEvent.RunStarted} first and {@link AgentEvent.RunCompleted} last.
 * </p>
 */
public interface AgentProtocol {

	/**
	 * The input handing the agent a message of the user, ending with a line end.
	 */
	String userMessage(String text);

	/**
	 * The input asking the agent to stop its current run, ending with a line end.
	 *
	 * @return The input, {@code null} when the agent cannot be asked; its process is terminated
	 *         then.
	 */
	String interrupt();

	/**
	 * Starts a run: the lines that follow belong to it.
	 *
	 * @param runId
	 *        The ID of the run, carried by all its events.
	 * @param messages
	 *        The language the messages of the run to the user are written in.
	 * @param sink
	 *        Receives the events of the run.
	 */
	void startRun(String runId, I18NBundle messages, AgentEventListener sink);

	/**
	 * Notes that the user asked to stop the run; the run ends as stopped.
	 */
	void cancelRequested();

	/**
	 * Maps a line the process wrote while the run lasts.
	 *
	 * @return Whether the line ended the run.
	 */
	boolean onLine(String line);

	/**
	 * Ends the run the process cannot complete any more, e.g. because it ended.
	 *
	 * @param reason
	 *        Why the run stopped.
	 * @param errorKind
	 *        Short kind of the error, {@code null} if the run did not fail.
	 * @param errorMessage
	 *        What happened, {@code null} if the run did not fail.
	 */
	void endRun(StopReason reason, String errorKind, String errorMessage);

	/**
	 * Whether the process can serve no further run: the run that ended found that the agent cannot
	 * reach its model with the token of the process, e.g. because the LLM proxy refuses it.
	 *
	 * <p>
	 * The process is terminated when such a run has ended; the next message starts a new one with
	 * a new token.
	 * </p>
	 */
	boolean isSpent();

	/**
	 * The answer of the last run: its final text, what it answered so far for a run that did not
	 * complete.
	 */
	String answer();

	/**
	 * The ID under which the agent keeps the conversation, as the process reported it.
	 *
	 * @return The ID, {@code null} before the process reported one.
	 */
	String sessionId();

	/**
	 * The context window, in tokens, of the model that answered the last run, as the process
	 * reported it.
	 *
	 * @return The window, zero before the process reported one.
	 */
	int contextWindow();

}
