/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The {@code input} channel of a {@code <chat-conversation>}, in both directions: which
 * conversation the browser is told to show, and what becomes of the one it reports.
 *
 * <p>
 * <b>The channel is the selection.</b> The browser shows what the channel holds, and a session it
 * reports - one it created on a first send, or one the user opened in it - is only a proposal. A host
 * that binds a conversation to something else on the page (one per project, say) answers the write
 * from its own channel listener: it refuses the reported session and writes its own, or none. That
 * answer has to win, so what is pushed to the browser afterwards is read from the channel, never
 * taken from the report (#576).
 * </p>
 *
 * <p>
 * A session that becomes the window's active one on the server - a chat started by a command of the
 * page - is {@link #adopt(String) written onto the channel} the same way, as a proposal a host may
 * refuse.
 * </p>
 *
 * <p>
 * <b>A refusal is a write the browser must follow even when nothing changes.</b> A host that writes
 * back the empty channel it had leaves {@code selected} at {@code ""} - the value it had before the
 * browser reported its session. Unchanged state is no news to the browser, so every
 * refusal also counts up {@code selectedCorrections}, and the browser follows the channel whenever
 * that count moves.
 * </p>
 */
final class SessionSelection {

	/** State key: the id of the conversation the channel holds, empty for a fresh one. */
	static final String SELECTED = "selected";

	/** State key: how often the channel refused a session the browser reported. */
	static final String CORRECTIONS = "selectedCorrections";

	private final ViewChannel _channel;

	private final BiConsumer<String, Object> _state;

	private final Supplier<Person> _user;

	private final Consumer<TLObject> _selected;

	/** Whether the channel is being written with what the browser reported. */
	private boolean _publishing;

	private int _corrections;

	/**
	 * Creates a {@link SessionSelection}.
	 *
	 * @param channel
	 *        The {@code input} channel.
	 * @param state
	 *        Where the browser's state is written.
	 * @param user
	 *        The acting account; a session it may not read is never selected.
	 * @param selected
	 *        Told the session the browser is told to show, <code>null</code> for a fresh
	 *        conversation, whenever it is told.
	 */
	SessionSelection(ViewChannel channel, BiConsumer<String, Object> state, Supplier<Person> user,
			Consumer<TLObject> selected) {
		_channel = channel;
		_state = state;
		_user = user;
		_selected = selected;
	}

	/**
	 * The listener the channel is followed with.
	 *
	 * <p>
	 * It reads the channel instead of taking the value it is handed: a listener that writes the
	 * channel again notifies the listeners behind it before they are told of the first write, so the
	 * value a later listener is handed may already be overwritten.
	 * </p>
	 */
	ViewChannel.ChannelListener listener() {
		return (sender, oldValue, newValue) -> {
			if (!_publishing) {
				apply();
			}
		};
	}

	/** Tells the browser what the channel holds now. */
	void apply() {
		select(selectedId());
	}

	/** The session the browser is told to show, <code>null</code> for a fresh conversation. */
	TLObject selected() {
		String id = selectedId();
		return id.isEmpty() ? null : ChatSessionIds.sessionOf(_channel.get());
	}

	private void select(String id) {
		_state.accept(SELECTED, id);
		_selected.accept(id.isEmpty() ? null : ChatSessionIds.sessionOf(_channel.get()));
	}

	/**
	 * Writes the session the browser reported onto the channel, and tells the browser what the
	 * channel holds after every listener has answered.
	 *
	 * @param id
	 *        The id the browser reported, empty for a fresh conversation.
	 * @param session
	 *        The session that id names, if the acting account may read it, otherwise
	 *        <code>null</code>.
	 */
	void publish(String id, TLObject session) {
		_publishing = true;
		try {
			_channel.set(session);
		} finally {
			_publishing = false;
		}
		String held = selectedId();
		select(held);
		if (!held.equals(id)) {
			_state.accept(CORRECTIONS, Integer.valueOf(++_corrections));
		}
	}

	/**
	 * Writes a session that became the window's active one on the server - a chat started by a
	 * command of the page - onto the channel, as if the user had picked it.
	 *
	 * <p>
	 * Nothing is written when the channel already holds that session, or when the acting account may
	 * not read the session. A host that refuses the session answers as it answers a session the browser
	 * reported, and its answer wins, see {@link #publish(String, TLObject)}.
	 * </p>
	 *
	 * @param id
	 *        The id of the session that became the active one.
	 */
	void adopt(String id) {
		TLObject session = ChatSessionIds.accessibleSession(id, _user.get(), SimpleBoundCommandGroup.READ);
		if (session == null || session.equals(ChatSessionIds.sessionOf(_channel.get()))) {
			return;
		}
		publish(id, session);
	}

	private String selectedId() {
		return ChatSessionIds.selectedId(_channel.get(), _user.get());
	}

}
