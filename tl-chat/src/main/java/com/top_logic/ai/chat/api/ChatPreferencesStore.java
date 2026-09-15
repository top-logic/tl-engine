/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;

/**
 * Per-user chat layout preferences stored on {@link PersonalConfiguration}.
 *
 * <p>
 * The React chat cannot call {@link PersonalConfiguration} itself, so {@code PUT}
 * {@code /chat/preferences} wraps it. {@link PersonalConfiguration#setJSONValue(String, Object)}
 * without {@link PersonalConfiguration#storePersonalConfiguration()} is lost when the session
 * unbinds, which is why every write stores.
 * </p>
 *
 * <p>
 * Reading is not an endpoint: {@code AgentChatControl} puts the stored values into the control state
 * with everything else the chat boots from, so the first render already has them.
 * </p>
 */
public final class ChatPreferencesStore {

	/** {@link PersonalConfiguration} key. Namespaced so it cannot collide with a theme pick. */
	public static final String KEY = "tl-chat.layout";

	private ChatPreferencesStore() {
		// Utility
	}

	/**
	 * The stored layout, or the defaults when none has been written.
	 */
	public static ChatPreferencesDto load() {
		ChatPreferencesDto defaults = ChatPreferencesDto.create();
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null) {
				return defaults;
			}
			Object raw = pc.getJSONValue(KEY);
			if (!(raw instanceof Map<?, ?> map)) {
				return defaults;
			}
			ChatPreferencesDto prefs = ChatPreferencesDto.create();
			prefs.setRailOpen(Boolean.TRUE.equals(map.get("railOpen")));
			prefs.setPaneWidth(intOf(map.get("paneWidth")));
			prefs.setBadgeSeen(intOf(map.get("badgeSeen")));
			return prefs;
		} catch (RuntimeException ex) {
			Logger.debug("Chat preferences could not be read: " + ex.getMessage(), ChatPreferencesStore.class);
			return defaults;
		}
	}

	/**
	 * Writes {@code prefs} and stores the personal configuration for the current user.
	 */
	public static void save(ChatPreferencesDto prefs) {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null) {
				return;
			}
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("railOpen", prefs.isRailOpen());
			map.put("paneWidth", prefs.getPaneWidth());
			map.put("badgeSeen", prefs.getBadgeSeen());
			pc.setJSONValue(KEY, map);
			PersonalConfiguration.storePersonalConfiguration();
		} catch (RuntimeException ex) {
			Logger.warn("Chat preferences could not be stored: " + ex.getMessage(), ex,
				ChatPreferencesStore.class);
		}
	}

	private static int intOf(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}
		if (value instanceof String text) {
			try {
				return Integer.parseInt(text.trim());
			} catch (NumberFormatException ex) {
				return 0;
			}
		}
		return 0;
	}
}
