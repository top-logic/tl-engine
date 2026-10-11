/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * What the agent that started a background subagent, or the user watching it, has sent it and the
 * subagent has not read yet.
 *
 * <p>
 * A subagent reads its inbox at the start of each of its turns - see
 * {@code AgentLoop#injectInboxMessages} - so a message sent while it is inside a tool call is read once
 * that call has returned. Steering is text and nothing else: a message cannot change the subagent's
 * tools, rights or model.
 * </p>
 *
 * <p>
 * Thread-safe: the lead's thread and the user's request post, the subagent's thread drains. A message
 * the inbox accepts is read or reported, never lost: the subagent's run ends only through
 * {@link #closeIfEmpty()}, which takes another turn for what arrived during its last one, and what a run
 * that ended otherwise - at its limit, failed, cancelled - did not read is taken by
 * {@link #closeAndDrain()} and named in its result. From then on a message is refused.
 * </p>
 */
public final class SubagentInbox {

	/** Who wrote a message. */
	public enum Sender {
		/** The agent that started the subagent; it writes through {@code send_to_subagent}. */
		LEAD,

		/** The user, from the subagent's card in the chat. */
		USER;
	}

	/**
	 * One message for the subagent.
	 *
	 * @param id
	 *        Identifies the message, so that whoever sent it can be told when it was read.
	 * @param sender
	 *        Who wrote it.
	 * @param text
	 *        What it says.
	 */
	public record Message(String id, Sender sender, String text) {

		/** A message of the given sender, with a new id. */
		public static Message of(Sender sender, String text) {
			return new Message(UUID.randomUUID().toString(), sender, text);
		}
	}

	private final ConcurrentLinkedQueue<Message> _messages = new ConcurrentLinkedQueue<>();

	private volatile boolean _closed;

	/**
	 * Leaves a message for the subagent.
	 *
	 * @return Whether the message was accepted; <code>false</code> once the subagent has ended.
	 */
	public synchronized boolean post(Message message) {
		if (_closed) {
			return false;
		}
		_messages.add(message);
		return true;
	}

	/** Takes every message not read yet, oldest first; empty when there is none. */
	public List<Message> drain() {
		List<Message> messages = new ArrayList<>();
		Message message;
		while ((message = _messages.poll()) != null) {
			messages.add(message);
		}
		return messages;
	}

	/**
	 * Refuses every further message, if no message waits to be read.
	 *
	 * <p>
	 * Called by a subagent's run that is about to end: in one step with {@link #post(Message)}, so that
	 * no message can arrive between the check and the close.
	 * </p>
	 *
	 * @return Whether the inbox is closed now; <code>false</code> when a message waits, and the
	 *         subagent has to take one more turn to read it.
	 */
	public synchronized boolean closeIfEmpty() {
		if (!_messages.isEmpty()) {
			return false;
		}
		_closed = true;
		return true;
	}

	/**
	 * Refuses every further message and takes the ones not read, oldest first.
	 *
	 * <p>
	 * Called when the subagent has ended. What it returns arrived when no turn was left to read it,
	 * and is reported in the subagent's result instead.
	 * </p>
	 */
	public synchronized List<Message> closeAndDrain() {
		_closed = true;
		return drain();
	}

	/** Whether the inbox refuses messages. */
	public boolean isClosed() {
		return _closed;
	}
}
