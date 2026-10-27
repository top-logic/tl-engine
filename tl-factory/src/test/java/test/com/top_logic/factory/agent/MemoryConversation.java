/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.factory.agent.ConversationTarget;

/**
 * A {@link ConversationTarget} kept in memory, recording the session IDs stored.
 */
public class MemoryConversation implements ConversationTarget {

	private final String _workspaceId;

	private final String _workingDir;

	private volatile String _sessionId;

	private volatile String _instructions;

	private final List<String> _stored = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link MemoryConversation}.
	 */
	public MemoryConversation(String workspaceId, String workingDir) {
		_workspaceId = workspaceId;
		_workingDir = workingDir;
	}

	@Override
	public String workspaceId() {
		return _workspaceId;
	}

	@Override
	public Object displayName() {
		return _workspaceId;
	}

	@Override
	public String workingDir() {
		return _workingDir;
	}

	@Override
	public String instructions() {
		return _instructions;
	}

	/**
	 * Sets what the agent is told in addition to the instructions of its harness.
	 *
	 * @see #instructions()
	 */
	public void setInstructions(String instructions) {
		_instructions = instructions;
	}

	@Override
	public String sessionId() {
		return _sessionId;
	}

	@Override
	public void storeSessionId(String sessionId) {
		_sessionId = sessionId;
		_stored.add(sessionId);
	}

	/**
	 * The session IDs stored, in their order.
	 */
	public List<String> stored() {
		return _stored;
	}

}
