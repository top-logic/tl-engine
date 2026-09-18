/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import junit.framework.TestCase;

import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;

/**
 * {@link ChatViewBinding#bind} is once per (sub-session, chat-id), while the layout preference is
 * read again every time the state is written.
 */
public class TestChatViewBinding extends TestCase {

	private Supplier<ChatPreferencesDto> _previous;

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
		ChatViewBinding.preferenceSource = () -> {
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
			() -> ChatPreferencesDto.create().setRailOpen(railOpen.get());
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

	/** Two windows do not share a binding even when they use the same chat-id. */
	public void testDifferentScopesDoNotShare() {
		ChatViewBinding left = ChatViewBinding.bind(new LazyTypedAnnotatable(), "w1", "/app", "main");
		ChatViewBinding right = ChatViewBinding.bind(new LazyTypedAnnotatable(), "w2", "/app", "main");
		assertNotSame(left, right);
	}
}
