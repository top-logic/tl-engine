/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.ai.chat.api.messages.ChatSessionDto;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tl.ai.core.execution.model.AgentSession;

/**
 * Keeps the acting account's chat list in the browser's state, and follows its changes.
 *
 * <p>
 * The browser's store shows the account's chats in the rail and reads the title of the open chat,
 * which the conversation's header shows, from that list. The list is written with every render of
 * the element ({@link #write()}), and again whenever the account's chats change on the server - a
 * chat renamed in the row menu of a {@code <data-list>}, by {@code aiSessionRename} from a script or
 * in another tab, a chat created by a first send, a chat deleted anywhere (#596).
 * </p>
 *
 * <p>
 * The server knows of such a change: {@code aiSessionRename} commits through the
 * {@code AgentExecutionStore}, the engine delivers that commit to every window whose model
 * listeners it affects, and the events reach this listener on the window's own model scope. This
 * reads the list again for a change that concerns the account's chats, and writes it into
 * {@link #SESSIONS} when it differs from the one the browser was told.
 * </p>
 *
 * <p>
 * <b>What is read again for.</b> A chat of the account that was created, and one whose title is not
 * the title this watch saw last. Not every update: a running agent writes its chat's bookkeeping
 * several times per send, and reading the list for each write made one send cost four list reads
 * instead of one. The first update of a chat this watch has not seen yet counts, because nothing
 * says what its title was before - so a run costs at most one extra read per chat, and a rename is
 * never missed. A deletion always counts: a deleted object can no longer be asked whose it was, and
 * a list read again for somebody else's deletion costs one query and sends nothing.
 * </p>
 */
final class SessionListWatch implements ModelListener {

	/** State key: the chats of the acting account, most recently updated first. */
	static final String SESSIONS = "sessions";

	private final TLStructuredType _type;

	private final Supplier<Person> _user;

	private final Function<Person, List<ChatSessionDto>> _list;

	private final BiConsumer<String, Object> _state;

	private ModelScope _scope;

	private boolean _attachedBefore;

	/**
	 * What the browser was last told, one entry per chat, to tell a list that changed from one read
	 * again for nothing.
	 */
	private List<List<String>> _written;

	/** The title each chat of the account had when this watch last saw it change. */
	private final Map<ObjectKey, String> _titles = new HashMap<>();

	/**
	 * Creates a {@link SessionListWatch}.
	 *
	 * @param type
	 *        The chat session type, {@code tl.ai.execution:AgentSession}.
	 * @param user
	 *        The acting account; only its chats count.
	 * @param list
	 *        Reads the chat list of an account.
	 * @param state
	 *        Where the browser's state is written.
	 */
	SessionListWatch(TLStructuredType type, Supplier<Person> user, Function<Person, List<ChatSessionDto>> list,
			BiConsumer<String, Object> state) {
		_type = type;
		_user = user;
		_list = list;
		_state = state;
	}

	/** Writes the account's chat list as it is now, for a render of the element. */
	void write() {
		List<ChatSessionDto> list = _list.apply(_user.get());
		_written = keys(list);
		_state.accept(SESSIONS, list);
	}

	/**
	 * Starts listening on the window's model scope.
	 *
	 * <p>
	 * A second attach - the element comes back after the user was on another page of the shell -
	 * reads the list again: what happened meanwhile was not heard, and a store another element kept
	 * alive would still hold the list it had.
	 * </p>
	 */
	void attach(ModelScope scope) {
		if (_scope != null) {
			return;
		}
		_scope = scope;
		_scope.addModelListener(_type, this);
		if (_attachedBefore) {
			changed();
		}
		_attachedBefore = true;
	}

	/** Stops listening. */
	void detach() {
		if (_scope == null) {
			return;
		}
		_scope.removeModelListener(_type, this);
		_scope = null;
	}

	@Override
	public void notifyChange(ModelChangeEvent event) {
		if (concernsUser(event)) {
			changed();
		}
	}

	/**
	 * Whether {@code event} created or retitled a chat of the acting account, or deleted any chat.
	 */
	boolean concernsUser(ModelChangeEvent event) {
		if (event.getDeleted(_type).findAny().isPresent()) {
			return true;
		}
		Person user = _user.get();
		boolean changed = false;
		for (TLObject created : event.getCreated(_type).toList()) {
			if (ownedBy(created, user)) {
				rememberTitle(created);
				changed = true;
			}
		}
		// Every updated chat is looked at, not only up to the first hit, so that each one's title is
		// remembered for the next event.
		for (TLObject updated : event.getUpdated(_type).toList()) {
			if (ownedBy(updated, user) && rememberTitle(updated)) {
				changed = true;
			}
		}
		return changed;
	}

	private static boolean ownedBy(TLObject object, Person user) {
		return object instanceof AgentSession session && Objects.equals(session.getOwner(), user);
	}

	/**
	 * Remembers the title of {@code object} and answers whether it differs from the one remembered
	 * before, which it does for a chat this watch had not seen.
	 */
	private boolean rememberTitle(TLObject object) {
		ObjectKey key = object.tId();
		boolean seen = _titles.containsKey(key);
		String before = _titles.put(key, ((AgentSession) object).getTitle());
		return !seen || !Objects.equals(before, _titles.get(key));
	}

	/** Reads the list again and tells the browser, if it is not what the browser was told. */
	private void changed() {
		List<ChatSessionDto> list = _list.apply(_user.get());
		List<List<String>> keys = keys(list);
		if (keys.equals(_written)) {
			return;
		}
		_written = keys;
		_state.accept(SESSIONS, list);
	}

	private static List<List<String>> keys(List<ChatSessionDto> list) {
		List<List<String>> result = new ArrayList<>(list.size());
		for (ChatSessionDto session : list) {
			result.add(Arrays.asList(session.getId(), session.getTitle(), session.getLastUpdated()));
		}
		return result;
	}

}
