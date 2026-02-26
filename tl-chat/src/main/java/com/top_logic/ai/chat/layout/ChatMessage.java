/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Immutable message in a chat.
 *
 */
public class ChatMessage {

	/**
	 * Metadata about a file attached to a {@link ChatMessage}.
	 */
	public record AttachmentMetadata(String fileName, String mimeType, int size) {
	}

	private final String _user;

	private final String _text;

	private final Instant _timestamp;

	private final List<AttachmentMetadata> _attachments;

	/**
	 * Creates a new {@link ChatMessage} with the current time and no attachments.
	 */
	public ChatMessage(String user, String text) {
		this(user, text, Instant.now());
	}

	/**
	 * Creates a new {@link ChatMessage} with the given timestamp and no attachments.
	 */
	public ChatMessage(String user, String text, Instant timestamp) {
		this(user, text, timestamp, Collections.emptyList());
	}

	/**
	 * Creates a new {@link ChatMessage} with the given timestamp and attachments.
	 */
	public ChatMessage(String user, String text, Instant timestamp, List<AttachmentMetadata> attachments) {
		_user = user;
		_text = text;
		_timestamp = timestamp;
		_attachments = List.copyOf(attachments);
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

	/**
	 * The attachment metadata for files attached to this message, never null.
	 */
	public List<AttachmentMetadata> getAttachments() {
		return _attachments;
	}

}
