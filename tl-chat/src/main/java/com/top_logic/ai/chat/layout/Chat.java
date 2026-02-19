/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory container holding chat entries in order: messages and task traces.
 * <p>
 * Order is preserved so the UI can render: user message -> task trace -> agent response.
 * Obtained from {@link TaskProgressTracker#getChat()} so the same instance is used when adding
 * (in poll) and when rendering.
 * </p>
 */
public class Chat {

	/** Maximum number of entries to keep in memory. Oldest entries are evicted first. */
	private static final int MAX_ENTRIES = 500;

	private final List<ChatEntry> _entries = new CopyOnWriteArrayList<>();
	private boolean _welcomeAdded;

	/**
	 * Adds a message to this chat.
	 */
	public void addMessage(ChatMessage message) {
		_entries.add(new ChatEntry.MessageEntry(message));
		enforceMaxSize();
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
		enforceMaxSize();
	}

	/**
	 * Adds a completed task as a stored snapshot so the trace persists (no lookup at render time).
	 *
	 * <p>
	 * This deliberately duplicates the task data that also exists in
	 * {@link TaskProgressTracker#getTask(String)}. The snapshot freezes the task trace at
	 * completion time so that tracker LRU eviction (which removes old tasks to bound memory)
	 * does not cause rendered timeline entries to disappear.
	 * </p>
	 *
	 * @param task
	 *        The task to store; will be rendered as the tool/agent trace.
	 */
	public void addTaskSnapshot(ChatTask task) {
		if (task != null) {
			_entries.add(new ChatEntry.TaskSnapshotEntry(task));
			enforceMaxSize();
		}
	}

	/**
	 * Returns all entries in order (messages and task references) for rendering.
	 */
	public List<ChatEntry> getEntries() {
		return _entries;
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

	/**
	 * Removes all entries from this chat. Used when switching to a different session.
	 */
	public void clear() {
		_entries.clear();
		_welcomeAdded = false;
	}

	/**
	 * Replaces all entries with the given list. Used when loading a persisted session.
	 *
	 * @param entries
	 *        The entries to load.
	 */
	public void loadEntries(List<ChatEntry> entries) {
		_entries.clear();
		_entries.addAll(entries);
		_welcomeAdded = true;
	}

	/**
	 * Removes oldest entries when the list exceeds {@link #MAX_ENTRIES}.
	 */
	private void enforceMaxSize() {
		while (_entries.size() > MAX_ENTRIES) {
			_entries.remove(0);
		}
	}

}
