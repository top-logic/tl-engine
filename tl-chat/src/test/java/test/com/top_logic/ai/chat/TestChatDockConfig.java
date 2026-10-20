/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatDockElement.Config;
import com.top_logic.ai.chat.view.ChatScopeElement;
import com.top_logic.ai.chat.view.list.DataListElement;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * The configuration surface of {@code <chat-dock/>}: its defaults and the content it frames.
 */
public class TestChatDockConfig extends TestCase {

	/** Unconfigured: a closed launcher with the chat glyph, onto the default conversation. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertNull("Nullable: a dock that names no chat reports on the default conversation.",
			config.getChatId());
		assertTrue("The dock is a frame; it ships no content of its own.", config.getContent().isEmpty());
		assertNotNull("The launcher always has a glyph, configured or not.", config.getIcon());
		assertEquals("css:bi bi-chat-dots", config.getIcon().toEncodedForm());
		assertNull("Without a label the browser names the assistant itself.", config.getLabel());
		assertFalse("A dock starts closed: it exists so that the page stays visible.", config.getOpen());
		assertEquals("TLChatDock", config.getReactElement());
	}

	/** An empty dock is a legitimate configuration, not a parse failure. */
	public void testEmptyDockParses() throws Exception {
		Config config = parse("<chat-dock %s/>");
		assertTrue(config.getContent().isEmpty());
		assertNull(config.getChatId());
	}

	/**
	 * The dock contains the chat rather than sitting in it, so it repeats the id.
	 *
	 * A chat scope reaches descendants only: the nested {@code <chat>} cannot hand its id up to the
	 * element that frames it, which is why this is the one chat element normally configured with a
	 * {@code chat-id} of its own.
	 */
	public void testNestedChatParses() throws Exception {
		Config config = parse("<chat-dock %s chat-id='assistant' icon='css:bi bi-stars' open='true'>"
			+ "<label>Albert</label>"
			+ "<chat chat-id='assistant' direction='row'>"
			+ "<data-list selection='dockSession'><rows>all(`tl.ai.execution:AgentSession`)</rows></data-list>"
			+ "</chat>"
			+ "</chat-dock>");

		assertEquals("assistant", config.getChatId());
		assertEquals("css:bi bi-stars", config.getIcon().toEncodedForm());
		assertNotNull(config.getLabel());
		assertTrue(config.getOpen());
		assertEquals(1, config.getContent().size());
		assertEquals("Anything may be nested; a <chat> is only the usual thing.",
			ChatScopeElement.class, config.getContent().get(0).getImplementationClass());
	}

	/** The content is the default container, so any element is written as a plain child. */
	public void testAnyElementIsContent() throws Exception {
		Config config = parse("<chat-dock %s>"
			+ "<data-list><rows>all(`tl.ai.execution:AgentSession`)</rows></data-list>"
			+ "</chat-dock>");
		assertEquals(DataListElement.class, config.getContent().get(0).getImplementationClass());
	}

	private static Config parse(String xml) throws Exception {
		String attributes = "xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'";
		return (Config) TypedConfiguration.parse(CharacterContents.newContent(String.format(xml, attributes)));
	}

	/** Boots the module system so typed configuration resolves the nested element tags. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatDockConfig.class));
	}
}
