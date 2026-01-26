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
 * In-memory container holding chat messages.
 *
 */
public class Chat {

	private final List<ChatMessage> _messages = new ArrayList<>();

	/**
	 * Adds a message to this chat.
	 */
	public void addMessage(ChatMessage message) {
		_messages.add(message);
	}

	/**
	 * Returns an unmodifiable view of all messages in this chat.
	 */
	public List<ChatMessage> getMessages() {
		return Collections.unmodifiableList(_messages);
	}

}
