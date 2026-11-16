/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.ViewContext;

/**
 * Marks the view context of the elements a {@code <chat-dock/>} frames, and says whether that dock
 * is open.
 *
 * <p>
 * <b>A dock stands down while a page shows its conversation.</b> Not because the transport could
 * not carry both - since Gitea #502 every chat command goes to a control of its own conversation and the
 * server holds a {@code TaskProgressTracker} for each - but because these two are the <em>same</em>
 * conversation in two places: two composers on one {@code draft} channel overwrite each other's
 * text, and two rails on one {@code chat-id} fight over the one remembered layout.
 * </p>
 *
 * <p>
 * The browser needs to tell the two apart, then: the
 * conversation <em>inside</em> the dock must not be what makes the dock hide itself. That is what
 * this scope says - a {@link ChatConversationControl} built under it reports {@code insideDock} and
 * is not counted. It is read from the view context rather than from the React tree, because a
 * nested element is a control of its own and nothing about its React parent is guaranteed.
 * </p>
 *
 * <p>
 * <b>A conversation in a closed dock offers the agent nothing.</b> The panel of a closed dock is
 * hidden, not gone, so its content stays displayed; what the conversation inside offers the agent -
 * the channels it may present on, the page commands, the subject, the facts - follows
 * {@link #isOpen()} instead, which the dock sets ({@link ChatDockControl#CMD_SET_OPEN}) and the
 * conversation listens to.
 * </p>
 *
 * @see ChatScope
 */
public final class ChatDockScope {

	private boolean _open;

	private final List<Runnable> _listeners = new ArrayList<>();

	/**
	 * Creates a {@link ChatDockScope}.
	 *
	 * @param open
	 *        Whether the dock's panel is open at first.
	 */
	public ChatDockScope(boolean open) {
		_open = open;
	}

	/**
	 * Whether the dock's panel is open.
	 */
	public boolean isOpen() {
		return _open;
	}

	/**
	 * Opens or closes the dock's panel and tells the listeners when that changes it.
	 */
	public void setOpen(boolean open) {
		if (open == _open) {
			return;
		}
		_open = open;
		for (Runnable listener : new ArrayList<>(_listeners)) {
			listener.run();
		}
	}

	/**
	 * Adds a listener told whenever {@link #isOpen()} changes.
	 */
	public void addListener(Runnable listener) {
		_listeners.add(listener);
	}

	/**
	 * Removes a listener added by {@link #addListener(Runnable)}.
	 */
	public void removeListener(Runnable listener) {
		_listeners.remove(listener);
	}

	/**
	 * The scope of the dock the given context is in, <code>null</code> outside a dock.
	 *
	 * @param context
	 *        The element's view context; may be {@code null}.
	 */
	public static ChatDockScope of(ViewContext context) {
		return context == null ? null : context.getScope(ChatDockScope.class);
	}

	/**
	 * Whether the given context is inside a {@code <chat-dock/>}.
	 *
	 * @param context
	 *        The element's view context; may be {@code null}.
	 */
	public static boolean isInsideDock(ViewContext context) {
		return of(context) != null;
	}
}
