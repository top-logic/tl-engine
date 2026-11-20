/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.basic.json.JSON;

/**
 * The chat layout survives the round trip through {@code PersonalConfiguration}.
 *
 * <p>
 * {@link com.top_logic.knowledge.wrap.person.PersonalConfiguration#setJSONValue} writes the value
 * with {@link JSON} and reads it back with the same parser - and on a parse failure it does not
 * merely return nothing, it <em>drops</em> the stored value as a legacy format. One chat-id that
 * cannot be written as a key therefore silently deletes the layout of every other chat, and the
 * empty string is such a key - which is why {@link ChatPreferencesStore#storageKey(String)}
 * prefixes. No element hands down an empty chat-id any more, but the prefix is a stored format and
 * the hazard is one character away, so both stay pinned.
 * </p>
 */
public class TestChatPreferenceStorage extends TestCase {

	/** Every chat-id, an empty one included, is a key this parser accepts. */
	public void testEveryChatIdSurvivesTheJsonRoundTrip() throws Exception {
		Map<String, Object> byChat = new LinkedHashMap<>();
		byChat.put(ChatPreferencesStore.storageKey(""), layout(true));
		byChat.put(ChatPreferencesStore.storageKey("grouped"), layout(false));

		Object parsed = JSON.fromString(JSON.toString(byChat));

		assertTrue("The stored value must parse; a failure drops every chat's layout.",
			parsed instanceof Map<?, ?>);
		Map<?, ?> read = (Map<?, ?>) parsed;
		assertEquals(Boolean.TRUE, layoutOf(read, "").get("railOpen"));
		assertEquals(Boolean.FALSE, layoutOf(read, "grouped").get("railOpen"));
		assertEquals(Boolean.FALSE, ((Map<?, ?>) layoutOf(read, "").get("sections")).get("progress"));
	}

	/** Two chats do not share a key, so one does not overwrite the other. */
	public void testChatIdsGetTheirOwnKey() {
		assertNotSame(ChatPreferencesStore.storageKey(""), ChatPreferencesStore.storageKey("grouped"));
		assertFalse(ChatPreferencesStore.storageKey("").isEmpty());
		assertEquals("Blanks around a configured id are not a second chat.",
			ChatPreferencesStore.storageKey("grouped"), ChatPreferencesStore.storageKey("  grouped "));
	}

	/** The badge counts seen are kept per section, through the JSON round trip. */
	public void testSeenCountsPerSection() throws Exception {
		Map<String, Object> layout = layout(true);
		Map<String, Object> seen = new LinkedHashMap<>();
		seen.put("progress", Integer.valueOf(3));
		seen.put("sources", Integer.valueOf(100));
		layout.put("badgeSeen", seen);

		ChatPreferencesDto read =
			ChatPreferencesStore.read((Map<?, ?>) JSON.fromString(JSON.toString(layout)));
		assertEquals(Map.of("progress", Integer.valueOf(3), "sources", Integer.valueOf(100)), read.getBadgeSeen());
	}

	/** A seen count that is not a map per section is nothing seen. */
	public void testSeenCountOfAnotherFormIsNothingSeen() {
		Map<String, Object> layout = layout(true);
		layout.put("badgeSeen", Integer.valueOf(13));

		ChatPreferencesDto read = ChatPreferencesStore.read(layout);
		assertTrue(read.getBadgeSeen().isEmpty());
		assertEquals(Boolean.TRUE, read.getRailOpen());
	}

	private static Map<?, ?> layoutOf(Map<?, ?> byChat, String chatId) {
		return (Map<?, ?>) byChat.get(ChatPreferencesStore.storageKey(chatId));
	}

	private static Map<String, Object> layout(boolean railOpen) {
		Map<String, Object> sections = new LinkedHashMap<>();
		sections.put("progress", Boolean.FALSE);
		Map<String, Object> layout = new LinkedHashMap<>();
		layout.put("railOpen", Boolean.valueOf(railOpen));
		layout.put("paneWidth", Integer.valueOf(0));
		layout.put("badgeSeen", new LinkedHashMap<>());
		layout.put("sections", sections);
		return layout;
	}
}
