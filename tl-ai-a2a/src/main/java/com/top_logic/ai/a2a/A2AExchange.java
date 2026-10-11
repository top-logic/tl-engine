/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

import java.time.Instant;
import java.util.Map;

import com.top_logic.ai.a2a.protocol.TaskState;

/**
 * One step of an A2A conversation, as an {@link A2AObserver} learns of it.
 *
 * @param time
 *        When it happened.
 * @param direction
 *        Whether a remote client asked this application, or this application asked a remote agent.
 * @param kind
 *        What happened.
 * @param agent
 *        The local agent: the path of the exposed agent that serves, or the name of the agent that
 *        asks.
 * @param peer
 *        The other side: the calling application (or account) of an incoming conversation, the remote
 *        agent of an outgoing one.
 * @param contextId
 *        The A2A context id, or <code>null</code> while a new conversation has none yet.
 * @param taskId
 *        The A2A task id, or <code>null</code> while a new task has none yet.
 * @param state
 *        The task's state after this step, or <code>null</code> when not known.
 * @param runId
 *        The local run: the run serving the task, or the run whose agent asks; <code>null</code>
 *        before the serving run started.
 * @param content
 *        The A2A JSON of what was exchanged: the message, the status message, the artifact, or
 *        <code>{"ref", "section"}</code> of a fetched artifact; <code>null</code> if there is none.
 *        Not to be changed.
 */
public record A2AExchange(Instant time, Direction direction, Kind kind, String agent, String peer,
		String contextId, String taskId, TaskState state, String runId, Map<String, Object> content) {

	/**
	 * Who started the conversation.
	 */
	public enum Direction {
		/** A remote client asks an agent of this application. */
		INCOMING,

		/** An agent of this application asks a remote agent. */
		OUTGOING;
	}

	/**
	 * What happened.
	 */
	public enum Kind {
		/** A message arrived: the client's message on the serving side, a direct answer on the asking side. */
		MESSAGE_RECEIVED,

		/** The asking side sent a message. */
		MESSAGE_SENT,

		/** The task moved to the exchange's state; the status message, if any, is the content. */
		STATE_CHANGED,

		/** The task has a new artifact. */
		ARTIFACT_PRODUCED,

		/** An artifact was read by reference. */
		ARTIFACT_FETCHED;
	}
}
