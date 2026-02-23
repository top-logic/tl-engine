/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;

/**
 * Immutable message in a chat.
 *
 */
public class ChatMessage {

	private final String _user;

	private final String _text;

	private final Instant _timestamp;

	/**
	 * Creates a new {@link ChatMessage} with the current time.
	 */
	public ChatMessage(String user, String text) {
		this(user, text, Instant.now());
	}

	/**
	 * Creates a new {@link ChatMessage} with the given timestamp.
	 */
	public ChatMessage(String user, String text, Instant timestamp) {
		_user = user;
		_text = text;
		_timestamp = timestamp;
	}

	/**
	 * The user who sent this message.
	 */
	public String getUser() {
		return _user;
	}

	/**
	 * The message text.
	 */
	public String getText() {
		return _text;
	}

	/**
	 * The timestamp when this message was created.
	 */
	public Instant getTimestamp() {
		return _timestamp;
	}

}
