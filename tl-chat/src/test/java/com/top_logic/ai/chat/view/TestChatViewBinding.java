/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import junit.framework.TestCase;

import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;

/**
 * {@link ChatViewBinding#bind} is once per (sub-session, chat-id) and does not reload preferences
 * on a second join.
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

	/** The second bind on the same key does not read {@code PersonalConfiguration} again. */
	public void testSecondBindDoesNotReloadPreferences() {
		AtomicInteger loads = new AtomicInteger();
		ChatViewBinding.preferenceSource = () -> {
			loads.incrementAndGet();
			return ChatPreferencesDto.create();
		};
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		ChatViewBinding first = ChatViewBinding.bind(scope, "w1", "/app", "main");
		ChatViewBinding second = ChatViewBinding.bind(scope, "w1", "/app", "main");
		assertSame(first, second);
		assertEquals("Preferences load once per (scope, chat-id).", 1, loads.get());
		assertSame(first.preferences(), second.preferences());

		ChatViewBinding.bind(scope, "w1", "/app", "other");
		assertEquals("A new chat-id loads once for itself.", 2, loads.get());
	}

	/** Two windows do not share a binding even when they use the same chat-id. */
	public void testDifferentScopesDoNotShare() {
		ChatViewBinding left = ChatViewBinding.bind(new LazyTypedAnnotatable(), "w1", "/app", "main");
		ChatViewBinding right = ChatViewBinding.bind(new LazyTypedAnnotatable(), "w2", "/app", "main");
		assertNotSame(left, right);
	}
}
