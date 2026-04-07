/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mutable container for a conversation's message history.
 *
 * <p>The agent loop is the single writer. The chat UI can safely read
 * the {@link #messages()} snapshot for display without synchronization.
 * Internally, a volatile reference to an immutable list ensures that
 * readers always see a consistent snapshot, even during compaction.</p>
 *
 * <p><b>Thread safety contract:</b> Only one thread (the agent loop) may call
 * {@link #add(Message)} or {@link #replaceRange(int, int, List)}. Any thread
 * may call the read methods ({@link #messages()}, {@link #size()},
 * {@link #lastAssistantUsage()}, {@link #totalUsage()}) concurrently.</p>
 */
public class Conversation {

	private volatile List<Message> _messages = List.of();

	/**
	 * Add a message to the end of the conversation.
	 */
	public void add(Message message) {
		Objects.requireNonNull(message, "Message must not be null.");
		List<Message> updated = new ArrayList<>(_messages);
		updated.add(message);
		_messages = List.copyOf(updated);
	}

	/**
	 * Replace a range of messages (for compaction).
	 *
	 * @param fromIndex start index (inclusive)
	 * @param toIndex   end index (exclusive)
	 * @param replacement messages to insert in place of the removed range
	 * @throws IndexOutOfBoundsException if range is invalid
	 */
	public void replaceRange(int fromIndex, int toIndex, List<Message> replacement) {
		Objects.requireNonNull(replacement, "Replacement must not be null.");
		for (Message msg : replacement) {
			Objects.requireNonNull(msg, "Replacement message must not be null.");
		}
		List<Message> current = _messages;
		if (fromIndex < 0 || toIndex > current.size() || fromIndex > toIndex) {
			throw new IndexOutOfBoundsException(
				"Invalid range [" + fromIndex + ", " + toIndex + ") for size " + current.size());
		}

		List<Message> updated = new ArrayList<>(current.size() - (toIndex - fromIndex) + replacement.size());
		updated.addAll(current.subList(0, fromIndex));
		updated.addAll(replacement);
		updated.addAll(current.subList(toIndex, current.size()));

		_messages = List.copyOf(updated);
	}

	/**
	 * Unmodifiable snapshot of all messages.
	 */
	public List<Message> messages() {
		return _messages;
	}

	/**
	 * Number of messages.
	 */
	public int size() {
		return _messages.size();
	}

	/**
	 * Token usage from the most recent {@link AssistantMessage}, if any.
	 */
	public Optional<TokenUsage> lastAssistantUsage() {
		List<Message> snapshot = _messages;
		for (int i = snapshot.size() - 1; i >= 0; i--) {
			if (snapshot.get(i) instanceof AssistantMessage a) {
				return Optional.of(a.usage());
			}
		}
		return Optional.empty();
	}

	/**
	 * Sum of all {@link AssistantMessage} token usages in this conversation.
	 */
	public TokenUsage totalUsage() {
		TokenUsage total = TokenUsage.EMPTY;
		for (Message msg : _messages) {
			if (msg instanceof AssistantMessage a) {
				total = total.add(a.usage());
			}
		}
		return total;
	}

}
