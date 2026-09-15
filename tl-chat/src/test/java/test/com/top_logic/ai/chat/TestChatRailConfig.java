/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.ai.chat.view.ChatRailElement;
import com.top_logic.ai.chat.view.ChatRailElement.Config;
import com.top_logic.ai.chat.view.ChatRailElement.PaneMode;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Defaults and nested {@code <section/>} list of {@code <chat-rail/>}.
 */
public class TestChatRailConfig extends TestCase {

	/** Unconfigured attributes match the design: pieces find each other and the pane is a card. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals("default", config.getChatId());
		assertNull(config.getInput());
		assertTrue(config.getSections() == null || config.getSections().isEmpty());
		assertTrue(config.getStrip());
		assertEquals(PaneMode.CARD, config.getPaneMode());
		assertEquals(0, config.getPaneWidth());
		assertEquals("TLChatRail", config.getReactElement());
		assertEquals(List.of("fortschritt", "kontext", "quellen"), ChatRailElement.defaultSectionNames());
	}

	/** XML and React both consume the pane-mode's lowercase external names. */
	public void testPaneModeExternalNames() {
		assertEquals("card", PaneMode.CARD.getExternalName());
		assertEquals("document", PaneMode.DOCUMENT.getExternalName());
	}

	/** Framework XML names map onto the ids {@code App} already uses. */
	public void testInternalNames() {
		assertEquals("progress", ChatRailElement.internalName("fortschritt"));
		assertEquals("context", ChatRailElement.internalName("kontext"));
		assertEquals("sources", ChatRailElement.internalName("quellen"));
		assertEquals("host-belege", ChatRailElement.internalName("host-belege"));
	}

	/** An empty section list is the framework default order, not a parse failure. */
	public void testEmptySectionsMeanDefaultOrder() throws Exception {
		String xml = "<chat-rail xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertTrue(config.getSections() == null || config.getSections().isEmpty());
		assertEquals(List.of("fortschritt", "kontext", "quellen"), ChatRailElement.defaultSectionNames());
	}

	/** A nested section list parses in the written order. */
	public void testSectionListParses() throws Exception {
		String xml = "<chat-rail xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'"
			+ " chat-id='main' strip='false' pane-mode='document' pane-width='480'>"
			+ "<section name='quellen' open='true' badge='false'/>"
			+ "<section name='host-belege' open='true'/>"
			+ "<section name='fortschritt'/>"
			+ "</chat-rail>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("main", config.getChatId());
		assertFalse(config.getStrip());
		assertEquals(PaneMode.DOCUMENT, config.getPaneMode());
		assertEquals(480, config.getPaneWidth());
		assertEquals(3, config.getSections().size());
		assertEquals("quellen", config.getSections().get(0).getName());
		assertTrue(config.getSections().get(0).getOpen());
		assertFalse(config.getSections().get(0).getBadge());
		assertEquals("host-belege", config.getSections().get(1).getName());
		assertTrue(config.getSections().get(1).getOpen());
		assertTrue(config.getSections().get(1).getBadge());
		assertEquals("fortschritt", config.getSections().get(2).getName());
		assertFalse(config.getSections().get(2).getOpen());
	}

	/**
	 * An unknown section name does not fail the view; it is dropped with a debug line when
	 * resolved against the registered names.
	 */
	public void testUnknownSectionIsIgnored() throws Exception {
		String xml = "<chat-rail xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'>"
			+ "<section name='not-a-section'/>"
			+ "</chat-rail>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("The view still loads.", "not-a-section", config.getSections().get(0).getName());
		assertEquals(1, ChatRailElement.ignoreUnknownSections(List.of("not-a-section"), List.of()));
		assertEquals(0, ChatRailElement.ignoreUnknownSections(List.of("fortschritt", "host-belege"),
			List.of("host-belege")));
	}
}
