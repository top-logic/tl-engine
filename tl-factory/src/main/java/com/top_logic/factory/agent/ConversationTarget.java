/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * The conversation a {@link WorkspaceAgent} holds: the workspace its agent works in and the
 * session ID under which the agent keeps it.
 */
public interface ConversationTarget {

	/**
	 * The workspace the agent process runs in.
	 */
	String workspaceId();

	/**
	 * Display name of the workspace in messages to the user, e.g. its project.
	 */
	Object displayName();

	/**
	 * The directory in the workspace the agent works in.
	 */
	String workingDir();

	/**
	 * The session ID of the conversation, {@code null} before the agent answered for the first
	 * time.
	 */
	String sessionId();

	/**
	 * What the agent is told about the workspace in addition to the instructions of its harness,
	 * e.g. how its work reaches the application; no secret.
	 *
	 * @return The text, {@code null} for nothing.
	 */
	default String instructions() {
		return null;
	}

	/**
	 * Ensures that the given user may give the agent a message in this conversation.
	 *
	 * @param user
	 *        The user writing the message.
	 * @throws com.top_logic.util.error.TopLogicException
	 *         If the user may not.
	 */
	void checkContinue(Person user);

	/**
	 * Stores the session ID the agent reported.
	 *
	 * @param sessionId
	 *        The session ID, {@code null} for a conversation the agent no longer has: the next
	 *        process starts a new one.
	 */
	void storeSessionId(String sessionId);

}
