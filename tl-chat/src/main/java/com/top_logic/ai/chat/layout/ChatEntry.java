/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

/**
 * One entry in the chat timeline: either a message or a task (tool/agent trace).
 * <p>
 * Used to render in order: user message → task trace → agent response.
 * </p>
 */
public sealed interface ChatEntry permits ChatEntry.MessageEntry, ChatEntry.TaskEntry, ChatEntry.TaskSnapshotEntry {

	/**
	 * A chat message (user or agent).
	 *
	 * @param message
	 *        The chat message.
	 */
	record MessageEntry(ChatMessage message) implements ChatEntry {
	}

	/**
	 * Reference to a task by id (for in-progress task rendered at end).
	 *
	 * @param taskId
	 *        The task identifier.
	 */
	record TaskEntry(String taskId) implements ChatEntry {
	}

	/**
	 * Stored copy of a completed task so the trace persists regardless of tracker lookups.
	 * Rendered as the tool/agent trace (same as {@code TaskEntry} but data lives in the chat).
	 *
	 * @param task
	 *        The completed task snapshot.
	 */
	record TaskSnapshotEntry(ChatTask task) implements ChatEntry {
	}
}
