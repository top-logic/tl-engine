/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

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
	 * Stores the session ID the agent reported.
	 *
	 * @param sessionId
	 *        The session ID, {@code null} for a conversation the agent no longer has: the next
	 *        process starts a new one.
	 */
	void storeSessionId(String sessionId);

}
