/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import junit.framework.TestCase;

import com.top_logic.ai.chat.view.ChatSessionIds;
import com.top_logic.ai.chat.view.ChatSessionsElement.Config;
import com.top_logic.ai.chat.view.ChatSessionsElement.Grouping;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Defaults and XML names of {@code <chat-sessions/>}, plus the session-object to id mapping that
 * does not need a KnowledgeBase.
 */
public class TestChatSessionsConfig extends TestCase {

	/** Unconfigured attributes match the design: pieces find each other and every command is on. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals("default", config.getChatId());
		assertNull(config.getSelection());
		assertTrue(config.getSearch());
		assertTrue(config.getNewChat());
		assertTrue(config.getDelete());
		assertEquals(Grouping.BY_DATE, config.getGrouping());
		assertTrue(config.getCollapsible());
		assertEquals("TLChatSessions", config.getReactElement());
	}

	/** XML and React both consume the grouping's lowercase external names. */
	public void testGroupingExternalNames() {
		assertEquals("by-date", Grouping.BY_DATE.getExternalName());
		assertEquals("flat", Grouping.FLAT.getExternalName());
	}

	/** A bare tag parses with the same defaults. */
	public void testBareTagParses() throws Exception {
		String xml = "<chat-sessions xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("default", config.getChatId());
		assertTrue(config.getSearch());
		assertEquals(Grouping.BY_DATE, config.getGrouping());
	}

	/** Named attributes override the defaults. */
	public void testAttributesParse() throws Exception {
		String xml = "<chat-sessions xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'"
			+ " chat-id='main' search='false' new-chat='false' delete='false'"
			+ " grouping='flat' collapsible='false' selection='selectedChat'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("main", config.getChatId());
		assertFalse(config.getSearch());
		assertFalse(config.getNewChat());
		assertFalse(config.getDelete());
		assertEquals(Grouping.FLAT, config.getGrouping());
		assertFalse(config.getCollapsible());
		assertEquals("selectedChat", config.getSelection().getChannelName());
	}

	/** No session maps to an empty id, never {@code null}. */
	public void testEmptyIdWhenThereIsNoSession() {
		assertEquals("", ChatSessionIds.idOf(null));
		assertNull(ChatSessionIds.sessionOf(null));
		assertEquals("", ChatSessionIds.selectedId(null, null));
		assertNull(ChatSessionIds.ownedSession("", null));
		assertNull(ChatSessionIds.ownedSession(null, null));
	}

	/** A one-item collection is the session a multi-select table writes for a single row. */
	public void testOneItemCollectionIsTheSession() {
		assertNull(ChatSessionIds.sessionOf(java.util.List.of()));
		assertNull(ChatSessionIds.sessionOf(java.util.List.of("not-a-session")));
	}
}
