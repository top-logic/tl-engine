/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.layout.view.ViewContext;

/**
 * Marks the view context of the elements a {@code <chat-dock/>} frames.
 *
 * <p>
 * <b>The chat transport carries one conversation per window.</b> Every {@code /chat/*} call is keyed
 * by {@code windowId} alone, so the server holds one {@code TaskProgressTracker} - one active
 * session, one processing state - per window, and every chat element of that window attaches to it.
 * A dock that offered a second view of the conversation the page is already showing would therefore
 * have the two of them taking each other's turns.
 * </p>
 *
 * <p>
 * So a dock stands down while such a page is open, and the browser needs to tell the two apart: the
 * conversation <em>inside</em> the dock must not be what makes the dock hide itself. That is what
 * this scope says - a {@link ChatConversationControl} built under it reports {@code insideDock} and
 * is not counted. It is read from the view context rather than from the React tree, because a
 * nested element is a control of its own and nothing about its React parent is guaranteed.
 * </p>
 *
 * @see ChatScope
 */
public record ChatDockScope() {

	/** The one instance; this scope carries no data beyond being present. */
	public static final ChatDockScope INSTANCE = new ChatDockScope();

	/**
	 * Whether the given context is inside a {@code <chat-dock/>}.
	 *
	 * @param context
	 *        The element's view context; may be {@code null}.
	 */
	public static boolean isInsideDock(ViewContext context) {
		return context != null && context.getScope(ChatDockScope.class) != null;
	}
}
