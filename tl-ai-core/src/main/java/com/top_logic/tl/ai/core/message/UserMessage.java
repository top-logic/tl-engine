/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A message in the user's role.
 *
 * <p>
 * Not every message in the user's role was written by the user. No provider transmits a system
 * message in the middle of a conversation, so the framework puts its notices, a subagent's report
 * and a lead's instruction to its subagent in the user's role too. {@link #origin()} says who wrote
 * a message, for the checks that must take only the user's own words - see
 * {@code MemoryConsent}. The provider is never told: the text of such a message says itself who
 * wrote it.
 * </p>
 *
 * @param id        unique identifier
 * @param timestamp when this message was created
 * @param content   the message's input (text and/or images)
 * @param origin    who wrote the message
 */
public record UserMessage(String id, Instant timestamp,
		List<ContentPart> content, Origin origin) implements Message {

	/**
	 * Who wrote a {@link UserMessage}.
	 */
	public enum Origin {

		/** The user, in their own words: what they typed or what a script sent for them. */
		USER,

		/**
		 * Another agent: the task a lead wrote for its subagent or its delegate, or a message the lead
		 * sent a running subagent.
		 */
		AGENT,

		/**
		 * The framework: a notice, a delivered subagent report or question, or the summary of a
		 * compaction.
		 */
		FRAMEWORK;
	}

	/**
	 * Creates a {@link UserMessage}.
	 */
	public UserMessage {
		Objects.requireNonNull(id, "Id must not be null.");
		Objects.requireNonNull(timestamp, "Timestamp must not be null.");
		Objects.requireNonNull(content, "Content must not be null.");
		Objects.requireNonNull(origin, "Origin must not be null.");
		content = List.copyOf(content);
	}

	/**
	 * Creates a {@link UserMessage} the user wrote.
	 */
	public UserMessage(String id, Instant timestamp, List<ContentPart> content) {
		this(id, timestamp, content, Origin.USER);
	}

	/**
	 * Whether the user wrote this message.
	 */
	public boolean fromUser() {
		return origin == Origin.USER;
	}
}
