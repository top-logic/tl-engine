/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import junit.framework.TestCase;

import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * {@link ChatViewBinding#bind} is once per (sub-session, chat-id), while the layout preference is
 * read again every time the state is written.
 */
public class TestChatViewBinding extends TestCase {

	private Function<String, ChatPreferencesDto> _previous;

	@Override
	protected void setUp() {
		_previous = ChatViewBinding.preferenceSource;
	}

	@Override
	protected void tearDown() {
		ChatViewBinding.preferenceSource = _previous;
	}

	/** Two binds with the same scope and chat-id return one object. */
	public void testBindIsIdempotentPerScopeAndChatId() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ChatViewBinding first = ChatViewBinding.bind(scope, "w1", "/app", "main");
		ChatViewBinding second = ChatViewBinding.bind(scope, "w1", "/app", "main");
		assertSame(first, second);
	}

	/** A second chat-id on the same window is a second binding. */
	public void testDifferentChatIdsAreDifferentBindings() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ChatViewBinding main = ChatViewBinding.bind(scope, "w1", "/app", "main");
		ChatViewBinding other = ChatViewBinding.bind(scope, "w1", "/app", "other");
		assertNotSame(main, other);
	}

	/** Binding costs no preference load: the layout is read when it is written, not when bound. */
	public void testBindDoesNotLoadPreferences() {
		AtomicInteger loads = new AtomicInteger();
		ChatViewBinding.preferenceSource = chatId -> {
			loads.incrementAndGet();
			return ChatPreferencesDto.create();
		};
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ChatViewBinding.bind(scope, "w1", "/app", "main");
		ChatViewBinding.bind(scope, "w1", "/app", "other");
		assertEquals("Binding reads no preference.", 0, loads.get());
	}

	/**
	 * Every write of the layout state reads the stored layout again.
	 *
	 * <p>
	 * This is what makes a page reload replay the current rail state: the engine re-renders the
	 * control tree the window already holds, so a layout captured at construction time would be the
	 * first render's layout forever (Gitea #423).
	 * </p>
	 */
	public void testLayoutStateIsReadPerWrite() {
		AtomicBoolean railOpen = new AtomicBoolean(false);
		ChatViewBinding.preferenceSource =
			chatId -> ChatPreferencesDto.create().setRailOpen(Boolean.valueOf(railOpen.get()));
		ChatViewBinding binding = ChatViewBinding.bind(new LazyTypedAnnotatable(), "w1", "/app", "main");

		Map<String, Object> first = new HashMap<>();
		binding.writeLayoutState(first::put);
		assertEquals(Boolean.FALSE, first.get("railOpen"));

		railOpen.set(true);
		Map<String, Object> second = new HashMap<>();
		binding.writeLayoutState(second::put);
		assertEquals("The second render replays the layout stored now.", Boolean.TRUE,
			second.get("railOpen"));
	}

	/**
	 * The layout is looked up under the {@code chat-id}, so two chats on one page keep two layouts.
	 */
	public void testLayoutIsReadPerChatId() {
		ChatViewBinding.preferenceSource =
			chatId -> ChatPreferencesDto.create().setRailOpen(Boolean.valueOf("main".equals(chatId)));
		TypedAnnotatable scope = new LazyTypedAnnotatable();

		Map<String, Object> main = new HashMap<>();
		ChatViewBinding.bind(scope, "w1", "/app", "main").writeLayoutState(main::put);
		Map<String, Object> other = new HashMap<>();
		ChatViewBinding.bind(scope, "w1", "/app", "other").writeLayoutState(other::put);

		assertEquals("main", main.get("layoutKey"));
		assertEquals(Boolean.TRUE, main.get("railOpen"));
		assertEquals("other", other.get("layoutKey"));
		assertEquals(Boolean.FALSE, other.get("railOpen"));
	}

	/** A user who decided nothing leaves the element's configured default in force. */
	public void testUndecidedRailIsWrittenAsNull() {
		ChatViewBinding.preferenceSource = chatId -> ChatPreferencesDto.create();
		Map<String, Object> state = new HashMap<>();
		ChatViewBinding.bind(new LazyTypedAnnotatable(), "w1", "/app", "main").writeLayoutState(state::put);

		assertTrue("The key is written, so a patch can clear a stale value.", state.containsKey("railOpen"));
		assertNull("Null, not false: nothing decided is not 'closed'.", state.get("railOpen"));
	}

	/** Two windows do not share a binding even when they use the same chat-id. */
	public void testDifferentScopesDoNotShare() {
		ChatViewBinding left = ChatViewBinding.bind(new LazyTypedAnnotatable(), "w1", "/app", "main");
		ChatViewBinding right = ChatViewBinding.bind(new LazyTypedAnnotatable(), "w2", "/app", "main");
		assertNotSame(left, right);
	}

	// --- The allow list of the present tool: deny by default, per window. ---

	/** A window whose elements named no channel offers the agent nothing. */
	public void testNothingIsPresentableByDefault() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ChatViewBinding.bind(scope, "w1", "/app", "main");

		assertNull("Deny by default.", ChatViewBinding.presentable(scope, "paneObject"));
		assertEquals(Set.of(), ChatViewBinding.presentableNames(scope));
	}

	/** A window with no chat at all is the same answer, not an error. */
	public void testAWindowWithoutAChatOffersNothing() {
		assertNull(ChatViewBinding.presentable(new LazyTypedAnnotatable(), "paneObject"));
		assertNull(ChatViewBinding.presentable(null, "paneObject"));
		assertEquals(Set.of(), ChatViewBinding.presentableNames(null));
	}

	/** A channel an element registered is reachable by exactly the name it was registered under. */
	public void testARegisteredChannelIsReachableByName() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ViewChannel channel = new DefaultViewChannel("paneObject");
		ChatViewBinding.bind(scope, "w1", "/app", "main").allowPresenting("paneObject", channel);

		assertSame(channel, ChatViewBinding.presentable(scope, "paneObject"));
		assertNull("Another name is another channel.", ChatViewBinding.presentable(scope, "somethingElse"));
		assertEquals(Set.of("paneObject"), ChatViewBinding.presentableNames(scope));
	}

	/**
	 * The window is the unit, not the chat: a run knows the sub-session it is in, and not which of
	 * the chat ids rendered there it belongs to.
	 */
	public void testAChannelOfAnyChatInTheWindowIsReachable() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ViewChannel left = new DefaultViewChannel("paneObject");
		ViewChannel right = new DefaultViewChannel("detail");
		ChatViewBinding.bind(scope, "w1", "/app", "main").allowPresenting("paneObject", left);
		ChatViewBinding.bind(scope, "w1", "/app", "other").allowPresenting("detail", right);

		assertSame(left, ChatViewBinding.presentable(scope, "paneObject"));
		assertSame(right, ChatViewBinding.presentable(scope, "detail"));
		assertEquals(Set.of("detail", "paneObject"), ChatViewBinding.presentableNames(scope));
	}

	/** A disposed control takes its channel out again: a view that is gone is not writable. */
	public void testDenyRemovesTheChannelAgain() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ViewChannel channel = new DefaultViewChannel("paneObject");
		ChatViewBinding binding = ChatViewBinding.bind(scope, "w1", "/app", "main");
		binding.allowPresenting("paneObject", channel);

		binding.denyPresenting("paneObject", channel);

		assertNull(ChatViewBinding.presentable(scope, "paneObject"));
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
		ChatViewBinding binding = ChatViewBinding.bind(scope, "w1", "/app", "main");
		binding.allowPresenting("paneObject", old);
		binding.allowPresenting("paneObject", current);

		binding.denyPresenting("paneObject", old);

		assertSame(current, ChatViewBinding.presentable(scope, "paneObject"));
	}

	/** Two windows never share an allow list, whatever their chats are called. */
	public void testAnotherWindowIsNotReachable() {
		TypedAnnotatable mine = new LazyTypedAnnotatable();
		TypedAnnotatable theirs = new LazyTypedAnnotatable();
		ChatViewBinding.bind(mine, "w1", "/app", "main")
			.allowPresenting("paneObject", new DefaultViewChannel("paneObject"));
		ChatViewBinding.bind(theirs, "w2", "/app", "main");

		assertNull("A run in one window does not reach another's page.",
			ChatViewBinding.presentable(theirs, "paneObject"));
	}
}
