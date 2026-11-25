/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * {@link ChatViewBinding#bind} is once per (sub-session, chat-id).
 */
public class TestChatViewBinding extends TestCase {

	/** Two binds with the same scope and chat-id return one object. */
	public void testBindIsIdempotentPerScopeAndChatId() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ChatViewBinding first = ChatViewBinding.bind(scope, "main");
		ChatViewBinding second = ChatViewBinding.bind(scope, "main");
		assertSame(first, second);
	}

	/** A second chat-id on the same window is a second binding. */
	public void testDifferentChatIdsAreDifferentBindings() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ChatViewBinding main = ChatViewBinding.bind(scope, "main");
		ChatViewBinding other = ChatViewBinding.bind(scope, "other");
		assertNotSame(main, other);
	}

	/** Two windows do not share a binding even when they use the same chat-id. */
	public void testDifferentScopesDoNotShare() {
		ChatViewBinding left = ChatViewBinding.bind(new LazyTypedAnnotatable(), "main");
		ChatViewBinding right = ChatViewBinding.bind(new LazyTypedAnnotatable(), "main");
		assertNotSame(left, right);
	}

	// --- The allow list of the present tool: deny by default, per window. ---

	/** A window whose elements named no channel offers the agent nothing. */
	public void testNothingIsPresentableByDefault() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ChatViewBinding.bind(scope, "main");

		assertNull("Deny by default.", ChatViewBinding.presentable(scope, "paneObject", null));
		assertEquals(Set.of(), ChatViewBinding.presentableNames(scope, null));
	}

	/** A window with no chat at all is the same answer, not an error. */
	public void testAWindowWithoutAChatOffersNothing() {
		assertNull(ChatViewBinding.presentable(new LazyTypedAnnotatable(), "paneObject", null));
		assertNull(ChatViewBinding.presentable(null, "paneObject", null));
		assertEquals(Set.of(), ChatViewBinding.presentableNames(null, null));
	}

	/** A channel an element registered is reachable by exactly the name it was registered under. */
	public void testARegisteredChannelIsReachableByName() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ViewChannel channel = new DefaultViewChannel("paneObject");
		ChatViewBinding.bind(scope, "main").allowPresenting("paneObject", channel);

		assertSame(channel, ChatViewBinding.presentable(scope, "paneObject", null));
		assertNull("Another name is another channel.", ChatViewBinding.presentable(scope, "somethingElse", null));
		assertEquals(Set.of("paneObject"), ChatViewBinding.presentableNames(scope, null));
	}

	/**
	 * The window is the unit, not the chat: a run knows the sub-session it is in, and not which of
	 * the chat ids rendered there it belongs to - so a window's allow list is the union of its
	 * chats' lists, which is what a page plus a {@code <chat-dock>} produces.
	 */
	public void testAChannelOfAnyChatInTheWindowIsReachable() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ViewChannel left = new DefaultViewChannel("paneObject");
		ViewChannel right = new DefaultViewChannel("detail");
		ChatViewBinding.bind(scope, "main").allowPresenting("paneObject", left);
		ChatViewBinding.bind(scope, "other").allowPresenting("detail", right);

		assertSame(left, ChatViewBinding.presentable(scope, "paneObject", null));
		assertSame(right, ChatViewBinding.presentable(scope, "detail", null));
		assertEquals(Set.of("detail", "paneObject"), ChatViewBinding.presentableNames(scope, null));
	}

	/** A disposed control takes its channel out again: a view that is gone is not writable. */
	public void testDenyRemovesTheChannelAgain() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ViewChannel channel = new DefaultViewChannel("paneObject");
		ChatViewBinding binding = ChatViewBinding.bind(scope, "main");
		binding.allowPresenting("paneObject", channel);

		binding.denyPresenting("paneObject", channel);

		assertNull(ChatViewBinding.presentable(scope, "paneObject", null));
	}

	/**
	 * A control that goes away after a second one took the name over leaves that second one alone.
	 *
	 * <p>
	 * The order of a re-render is "the new control registers, the old one is cleaned up", so a
	 * removal by name alone would close a channel that is in force.
	 * </p>
	 */
	public void testDenyDoesNotRemoveAnotherControlsChannel() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ViewChannel old = new DefaultViewChannel("paneObject");
		ViewChannel current = new DefaultViewChannel("paneObject");
		ChatViewBinding binding = ChatViewBinding.bind(scope, "main");
		binding.allowPresenting("paneObject", old);
		binding.allowPresenting("paneObject", current);

		binding.denyPresenting("paneObject", old);

		assertSame(current, ChatViewBinding.presentable(scope, "paneObject", null));
	}

	/** Two windows never share an allow list, whatever their chats are called. */
	public void testAnotherWindowIsNotReachable() {
		TypedAnnotatable mine = new LazyTypedAnnotatable();
		TypedAnnotatable theirs = new LazyTypedAnnotatable();
		ChatViewBinding.bind(mine, "main")
			.allowPresenting("paneObject", new DefaultViewChannel("paneObject"));
		ChatViewBinding.bind(theirs, "main");

		assertNull("A run in one window does not reach another's page.",
			ChatViewBinding.presentable(theirs, "paneObject", null));
	}
}
