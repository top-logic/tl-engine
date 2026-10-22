/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import junit.framework.TestCase;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;

/**
 * How a chat element finds its conversation: its own {@code chat-id}, else the enclosing
 * {@code <chat>}, else {@code default}.
 */
public class TestChatScope extends TestCase {

	/** An element outside every {@code <chat>} that names no id joins the default chat. */
	public void testDefaultWithoutScope() {
		assertEquals(ChatScope.DEFAULT_CHAT_ID, ChatScope.resolve(root(), null));
		assertEquals(ChatScope.DEFAULT_CHAT_ID, ChatScope.resolve(root(), "  "));
	}

	/** An element inside a {@code <chat>} that names no id joins that chat. */
	public void testScopeIsInherited() {
		assertEquals("support", ChatScope.resolve(scoped("support"), null));
	}

	/** A descendant of a descendant still sees the scope: nesting does not lose it. */
	public void testScopeReachesNestedContexts() {
		ViewContext section = scoped("support").childContext("rail").childContext("section");
		assertEquals("support", ChatScope.resolve(section, null));
	}

	/** An id on the element itself is the override. */
	public void testOwnIdWins() {
		assertEquals("other", ChatScope.resolve(scoped("support"), "other"));
		assertEquals("other", ChatScope.resolve(root(), " other "));
	}

	/** A {@code <chat>} with a blank id is the default chat, not a chat called "". */
	public void testBlankScopeIsTheDefaultChat() {
		assertEquals(ChatScope.DEFAULT_CHAT_ID, new ChatScope("  ").chatId());
		assertEquals(ChatScope.DEFAULT_CHAT_ID, new ChatScope(null).chatId());
	}

	/** Two elements nested in one {@code <chat>}, neither naming an id, get one binding. */
	public void testNestedElementsShareOneBinding() {
		ViewContext chat = scoped("support");
		TypedAnnotatable window = new LazyTypedAnnotatable();

		ChatViewBinding conversation = bind(window, ChatScope.resolve(chat.childContext("panel"), null));
		ChatViewBinding rail = bind(window, ChatScope.resolve(chat.childContext("rail"), null));

		assertSame("Both inherit 'support', so both reach the same server binding.", conversation, rail);
	}

	/** An element that names its own id leaves the enclosing chat. */
	public void testExplicitIdLeavesTheScope() {
		ViewContext chat = scoped("support");
		TypedAnnotatable window = new LazyTypedAnnotatable();

		ChatViewBinding inherited = bind(window, ChatScope.resolve(chat.childContext("panel"), null));
		ChatViewBinding overridden = bind(window, ChatScope.resolve(chat.childContext("rail"), "other"));

		assertNotSame("An explicit chat-id is a second conversation.", inherited, overridden);
	}

	private static ChatViewBinding bind(TypedAnnotatable window, String chatId) {
		return ChatViewBinding.bind(window, "w1", "/app", chatId);
	}

	private static ViewContext root() {
		return new DefaultViewContext(null);
	}

	private static ViewContext scoped(String chatId) {
		return root().withScope(ChatScope.class, new ChatScope(chatId));
	}
}
