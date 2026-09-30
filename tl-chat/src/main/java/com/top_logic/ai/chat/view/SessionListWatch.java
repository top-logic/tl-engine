/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tl.ai.core.execution.model.AgentSession;

/**
 * Tells the browser that the acting account's chats changed on the server, so its store reads the
 * list again.
 *
 * <p>
 * The browser's store keeps its own copy of the chat list - from {@code GET /chat/sessions} - and
 * reads the title of the open chat, which the conversation's header shows, from that copy. It read
 * the list when it booted and after its own sends and deletes, and at no other time. A chat renamed
 * anywhere else - the row menu of a {@code <data-list>}, {@code aiSessionRename} from a script,
 * another tab - therefore kept its old title in the header until the page was loaded again, and
 * switching to another chat and back did not help, because that reads the same copy (#596).
 * </p>
 *
 * <p>
 * The server does know: {@code aiSessionRename} commits through the {@code AgentExecutionStore},
 * whose change notification makes {@link AgentViewRefreshService} synthesize the model events in
 * every window of the owner, and those reach this listener on the window's own model scope. This
 * counts the changes that concern the account's chats into {@link #REVISION}; the browser re-reads
 * the list whenever that count moves. The count, not the titles, because the list is what the
 * browser has to follow - a rename moves a chat to "today" in the rail as well.
 * </p>
 *
 * <p>
 * <b>What counts.</b> A chat of the account that was created, and one whose title is not the title
 * this watch saw last. Not every update: a running agent writes its chat's bookkeeping several
 * times per send, and counting each write made one send cost four list reads instead of one. The
 * first update of a chat this watch has not seen yet counts, because nothing says what its title
 * was before - so a run costs at most one extra read per chat, and a rename is never missed. A
 * deletion always counts: a deleted object can no longer be asked whose it was, and a list read
 * again for somebody else's deletion costs one request.
 * </p>
 */
final class SessionListWatch implements ModelListener {

	/** State key: how often the account's chats changed while this element was showing. */
	static final String REVISION = "sessionsRevision";

	private final TLStructuredType _type;

	private final Supplier<Person> _user;

	private final BiConsumer<String, Object> _state;

	private ModelScope _scope;

	private boolean _attachedBefore;

	private int _revision;

	/** The title each chat of the account had when this watch last saw it change. */
	private final Map<ObjectKey, String> _titles = new HashMap<>();

	/**
	 * Creates a {@link SessionListWatch}.
	 *
	 * @param type
	 *        The chat session type, {@code tl.ai.execution:AgentSession}.
	 * @param user
	 *        The acting account; only its chats count.
	 * @param state
	 *        Where the browser's state is written.
	 */
	SessionListWatch(TLStructuredType type, Supplier<Person> user, BiConsumer<String, Object> state) {
		_type = type;
		_user = user;
		_state = state;
		_state.accept(REVISION, Integer.valueOf(_revision));
	}

	/**
	 * Starts listening on the window's model scope.
	 *
	 * <p>
	 * A second attach - the element comes back after the user was on another page of the shell -
	 * counts as a change: what happened meanwhile was not heard, and a store another element kept
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

	private void changed() {
		_state.accept(REVISION, Integer.valueOf(++_revision));
	}

}
