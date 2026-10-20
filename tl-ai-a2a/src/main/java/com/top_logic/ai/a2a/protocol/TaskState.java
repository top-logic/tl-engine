/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.protocol;

/**
 * The A2A v1.0 task states; on the wire {@code TASK_STATE_COMPLETED}.
 */
public enum TaskState {

	/** Accepted, not started. */
	SUBMITTED,

	/** The agent is running. */
	WORKING,

	/** The agent asked a question and waits for the answer. */
	INPUT_REQUIRED,

	/** The agent needs credentials; not produced by this module. */
	AUTH_REQUIRED,

	/** Finished with an answer. */
	COMPLETED,

	/** Finished without an answer. */
	FAILED,

	/** Stopped on request. */
	CANCELED,

	/** Not accepted by the agent. */
	REJECTED;

	private static final String PREFIX = "TASK_STATE_";

	/**
	 * The wire name, e.g. {@code TASK_STATE_INPUT_REQUIRED}.
	 */
	public String wireName() {
		return PREFIX + name();
	}

	/**
	 * Whether nothing can happen to the task any more.
	 */
	public boolean isTerminal() {
		return this == COMPLETED || this == FAILED || this == CANCELED || this == REJECTED;
	}

	/**
	 * Whether the task waits for its client.
	 */
	public boolean isInterrupted() {
		return this == INPUT_REQUIRED || this == AUTH_REQUIRED;
	}

	/**
	 * Whether a blocking call or a stream ends at this state.
	 */
	public boolean endsWait() {
		return isTerminal() || isInterrupted();
	}

	/**
	 * Whether a task in this state may move to {@code next}: terminal states are final, nothing goes
	 * back to {@link #SUBMITTED}, an interrupted task resumes to {@link #WORKING} or ends, and a
	 * repeated {@link #WORKING} carries a progress message.
	 */
	public boolean canMoveTo(TaskState next) {
		if (isTerminal() || next == SUBMITTED) {
			return false;
		}
		if (next == this) {
			return this == WORKING;
		}
		return !isInterrupted() || next == WORKING || next.isTerminal();
	}

	/**
	 * Reads a state, with or without the {@code TASK_STATE_} prefix.
	 *
	 * @return The state, or <code>null</code> if the text names none.
	 */
	public static TaskState parse(String text) {
		if (text == null) {
			return null;
		}
		String name = text.trim();
		try {
			return valueOf(name.startsWith(PREFIX) ? name.substring(PREFIX.length()) : name);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}
}
