/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.layout.view.ViewContext;

/**
 * The conversation an enclosing {@code <chat>} element hands down to the chat elements inside it.
 *
 * <p>
 * Several chat elements show one conversation when they agree on a {@code chat-id}, and that
 * agreement used to be an attribute repeated on every one of them - invisible wiring, where a typo
 * in one place silently opened a second, empty conversation. {@link ChatScopeElement} declares the
 * id once and puts it in reach of every descendant through
 * {@link ViewContext#withScope(Class, Object)}; each element {@link #resolve(ViewContext, String)
 * resolves} its own id against it.
 * </p>
 *
 * @param chatId
 *        The conversation the elements in this scope join. Never empty.
 */
public record ChatScope(String chatId) {

	/**
	 * The conversation an element joins when it names none and stands outside a {@code <chat>}.
	 *
	 * <p>
	 * A page with one chat therefore needs no configuration at all: every piece on it finds the
	 * others.
	 * </p>
	 */
	public static final String DEFAULT_CHAT_ID = "default";

	/** Creates a {@link ChatScope}, normalizing a missing or blank id to {@link #DEFAULT_CHAT_ID}. */
	public ChatScope {
		chatId = normalize(chatId);
	}

	/**
	 * The conversation an element joins: its own {@code chat-id} when it has one, otherwise the
	 * {@link ChatScope} of the nearest enclosing {@code <chat>}, otherwise
	 * {@link #DEFAULT_CHAT_ID}.
	 *
	 * @param context
	 *        The element's view context.
	 * @param configured
	 *        The element's own {@code chat-id}, {@code null} or blank when it has none.
	 * @return The id to bind and to push into the control state. Never empty.
	 */
	public static String resolve(ViewContext context, String configured) {
		String own = configured == null ? "" : configured.trim();
		if (!own.isEmpty()) {
			return own;
		}
		ChatScope scope = context == null ? null : context.getScope(ChatScope.class);
		return scope == null ? DEFAULT_CHAT_ID : scope.chatId();
	}

	private static String normalize(String chatId) {
		String trimmed = chatId == null ? "" : chatId.trim();
		return trimmed.isEmpty() ? DEFAULT_CHAT_ID : trimmed;
	}
}
