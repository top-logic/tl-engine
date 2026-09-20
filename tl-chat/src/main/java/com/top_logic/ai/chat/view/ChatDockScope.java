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
 * <b>A dock stands down while a page shows its conversation.</b> Not because the transport could
 * not carry both - since Gitea #502 every {@code /chat/*} call names its conversation and the
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
