/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.util.TLContext;

/**
 * The chat speaks the language the session has now, not the one it had when the chat was first
 * rendered (Gitea #662).
 *
 * <p>
 * A binding lives as long as the sub-session, so a language captured in its constructor would
 * stay in force until the next login: a switch in the account menu would leave the chat in the
 * old language, while the rest of the window followed the new one.
 * </p>
 */
public class TestChatLanguage extends BasicTestCase {

	/** A language switched after the first render reaches the next one. */
	public void testTheNextRenderSpeaksTheSwitchedLanguage() {
		TLContext context = TLContext.getContext();
		Locale previous = context.getCurrentLocale();
		Function<String, ChatPreferencesDto> previousSource = ChatViewBinding.preferenceSource;
		ChatViewBinding.preferenceSource = chatId -> ChatPreferencesDto.create();
		try {
			TypedAnnotatable window = new LazyTypedAnnotatable();
			context.setCurrentLocale(Locale.GERMAN);
			assertEquals("de", render(ChatViewBinding.bind(window, "w1", "/app", "pietro")).get("language"));

			context.setCurrentLocale(Locale.ENGLISH);
			ChatViewBinding again = ChatViewBinding.bind(window, "w1", "/app", "pietro");
			assertEquals("The window's chat answers in the language chosen since.", "en",
				render(again).get("language"));
			Map<String, Object> reload = new HashMap<>();
			again.writeRenderState(reload::put);
			assertEquals("A reload re-renders the chat in the language chosen since.", "en", reload.get("language"));
		} finally {
			ChatViewBinding.preferenceSource = previousSource;
			context.setCurrentLocale(previous);
		}
	}

	private static Map<String, Object> render(ChatViewBinding binding) {
		Map<String, Object> state = new HashMap<>();
		binding.writeSharedState(state::put);
		return state;
	}

	/** Test suite. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(new TestSuite(TestChatLanguage.class));
	}

}
