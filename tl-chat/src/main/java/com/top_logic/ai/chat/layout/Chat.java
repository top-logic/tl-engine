/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory container holding chat messages in order.
 * <p>
 * Obtained from {@link TaskProgressTracker#getChat()} so the same instance is used when adding
 * (in poll) and when rendering.
 * </p>
 */
public class Chat {

	/** Maximum number of messages to keep in memory. Oldest messages are evicted first. */
	private static final int MAX_MESSAGES = 500;

	private final List<ChatMessage> _messages = new CopyOnWriteArrayList<>();

	/**
	 * Adds a message to this chat.
	 */
	public void addMessage(ChatMessage message) {
		_messages.add(message);
		enforceMaxSize();
	}

	/**
	 * Returns all messages in order.
	 */
	public List<ChatMessage> getMessages() {
		return Collections.unmodifiableList(_messages);
	}

	/**
	 * Removes all messages from this chat. Used when switching to a different session.
	 */
	public void clear() {
		_messages.clear();
	}

	/**
	 * Replaces all messages with the given list. Used when loading a persisted session.
	 *
	 * @param messages
	 *        The messages to load.
	 */
	public void loadMessages(List<ChatMessage> messages) {
		_messages.clear();
		_messages.addAll(messages);
	}

	/**
	 * Removes oldest messages when the list exceeds {@link #MAX_MESSAGES}.
	 */
	private void enforceMaxSize() {
		while (_messages.size() > MAX_MESSAGES) {
			_messages.remove(0);
		}
	}

}
