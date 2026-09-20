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
 * The layout is kept per {@code chat-id}, under one key: two chats an application composes are
 * different places with different sections, and a rail closed in one is no statement about the
 * other. The {@code chat-id} is the one identifier both the element configuration and the browser
 * agree on, and unlike a control id it survives a reload.
 * </p>
 *
 * <p>
 * What is stored is what the user decided, never what a page happened to show: a fact the user
 * never touched is absent, and the element's configured default applies to it (#423).
 * </p>
 *
 * <p>
 * Reading is not an endpoint: the chat controls put the stored values into their state with
 * everything else the chat boots from, on every render, so the first render already has them.
 * </p>
 */
public final class ChatPreferencesStore {

	/** {@link PersonalConfiguration} key. Namespaced so it cannot collide with a theme pick. */
	public static final String KEY = "tl-chat.layout";

	private ChatPreferencesStore() {
		// Utility
	}

	/**
	 * The layout stored for {@code chatId}, or an empty one when the user has decided nothing.
	 *
	 * @param chatId
	 *        The {@code chat-id} of the chat, as configured. {@code null} and blank share one key.
	 */
	public static ChatPreferencesDto load(String chatId) {
		ChatPreferencesDto prefs = ChatPreferencesDto.create();
		try {
			Map<?, ?> stored = storedLayout(chatId);
			if (stored == null) {
				return prefs;
			}
			Object railOpen = stored.get("railOpen");
			if (railOpen instanceof Boolean flag) {
				prefs.setRailOpen(flag);
			}
			prefs.setPaneWidth(intOf(stored.get("paneWidth")));
			prefs.setBadgeSeen(intOf(stored.get("badgeSeen")));
			if (stored.get("sections") instanceof Map<?, ?> sections) {
				for (Map.Entry<?, ?> entry : sections.entrySet()) {
					if (entry.getValue() instanceof Boolean open) {
						prefs.putSection(String.valueOf(entry.getKey()), open.booleanValue());
					}
				}
			}
			return prefs;
		} catch (RuntimeException ex) {
			Logger.debug("Chat preferences could not be read: " + ex.getMessage(), ChatPreferencesStore.class);
			return ChatPreferencesDto.create();
		}
	}

	/**
	 * Writes {@code prefs} for {@code chatId} and stores the personal configuration for the current
	 * user.
	 */
	public static void save(String chatId, ChatPreferencesDto prefs) {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null) {
				return;
			}
			Map<String, Object> layout = new LinkedHashMap<>();
			if (prefs.hasRailOpen()) {
				layout.put("railOpen", prefs.getRailOpen());
			}
			layout.put("paneWidth", Integer.valueOf(prefs.getPaneWidth()));
			layout.put("badgeSeen", Integer.valueOf(prefs.getBadgeSeen()));
			layout.put("sections", new LinkedHashMap<String, Boolean>(prefs.getSections()));

			Map<String, Object> byChat = new LinkedHashMap<>();
			if (pc.getJSONValue(KEY) instanceof Map<?, ?> existing) {
				for (Map.Entry<?, ?> entry : existing.entrySet()) {
					byChat.put(String.valueOf(entry.getKey()), entry.getValue());
				}
			}
			byChat.put(storageKey(chatId), layout);
			pc.setJSONValue(KEY, byChat);
			PersonalConfiguration.storePersonalConfiguration();
		} catch (RuntimeException ex) {
			Logger.warn("Chat preferences could not be stored: " + ex.getMessage(), ex,
				ChatPreferencesStore.class);
		}
	}

	/**
	 * The raw layout map of {@code chatId}, or {@code null} when nothing is stored for it.
	 *
	 * <p>
	 * A value written by an older build is one flat layout for every chat. It is not read: nothing
	 * of this is released, and a user whose rail state resets once loses nothing they would notice.
	 * </p>
	 */
	private static Map<?, ?> storedLayout(String chatId) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return null;
		}
		if (!(pc.getJSONValue(KEY) instanceof Map<?, ?> byChat)) {
			return null;
		}
		return byChat.get(storageKey(chatId)) instanceof Map<?, ?> layout ? layout : null;
	}

	/**
	 * The key {@code chatId} is stored under inside the {@link #KEY} map.
	 *
	 * <p>
	 * Prefixed, because {@link com.top_logic.basic.json.JSON} refuses to parse a map with an empty
	 * key - it then treats the whole value as a legacy format and drops it, taking every other
	 * chat's layout with it. No element hands down an empty chat-id since {@code <agent-chat/>}
	 * went (#487), but this is a <em>stored</em> format: changing it would orphan every layout
	 * already written. The prefix also keeps a chat-id from colliding with anything else put in
	 * this map.
	 * </p>
	 */
	static String storageKey(String chatId) {
		return "chat:" + (chatId == null ? "" : chatId.trim());
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
