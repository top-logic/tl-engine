/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory container holding chat entries in order: messages and task traces.
 * <p>
 * Order is preserved so the UI can render: user message → task trace → agent response.
 * Obtained from {@link TaskProgressTracker#getChat()} so the same instance is used when adding
 * (in poll) and when rendering.
 * </p>
 */
public class Chat {

	private final List<ChatEntry> _entries = new ArrayList<>();
	private boolean _welcomeAdded;

	/**
	 * Adds a message to this chat.
	 */
	public void addMessage(ChatMessage message) {
		_entries.add(new ChatEntry.MessageEntry(message));
	}

	/**
	 * Ensures the welcome message is added once per chat instance.
	 */
	public void ensureWelcomeMessage() {
		if (_welcomeAdded) {
			return;
		}
		_welcomeAdded = true;
		_entries.add(new ChatEntry.MessageEntry(new ChatMessage("System", "Welcome to TopLogic Chat Demo!")));
	}

	/**
	 * Adds a task (tool/agent trace) by id. Used for in-progress task rendered at end.
	 */
	public void addTask(String taskId) {
		_entries.add(new ChatEntry.TaskEntry(taskId));
	}

	/**
	 * Adds a completed task as a stored snapshot so the trace persists (no lookup at render time).
	 *
	 * @param task
	 *        The task to store; will be rendered as the tool/agent trace.
	 */
	public void addTaskSnapshot(ChatTask task) {
		if (task != null) {
			_entries.add(new ChatEntry.TaskSnapshotEntry(task));
		}
	}

	/**
	 * Returns all entries in order (messages and task references) for rendering.
	 */
	public List<ChatEntry> getEntries() {
		return Collections.unmodifiableList(_entries);
	}

	/**
	 * Returns all messages in order (for backward compatibility if needed).
	 */
	public List<ChatMessage> getMessages() {
		List<ChatMessage> messages = new ArrayList<>();
		for (ChatEntry e : _entries) {
			if (e instanceof ChatEntry.MessageEntry me) {
				messages.add(me.message());
			}
		}
		return Collections.unmodifiableList(messages);
	}

}
