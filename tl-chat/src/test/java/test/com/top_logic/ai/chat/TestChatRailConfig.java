/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatRailElement.Config;
import com.top_logic.ai.chat.view.ChatRailElement.PaneMode;
import com.top_logic.ai.chat.view.ChatRailElement.SectionConfig;
import com.top_logic.ai.chat.view.ChatSourcesElement;
import com.top_logic.ai.chat.view.list.DataListElement;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * The configuration surface of {@code <chat-rail/>}: its defaults, its nested sections and its pane.
 */
public class TestChatRailConfig extends TestCase {

	/** Unconfigured: pieces find each other, the rail is open, and it holds nothing. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals("default", config.getChatId());
		assertTrue(config.getSections().isEmpty());
		assertTrue("The shell ships no sections of its own.", config.getPane().isEmpty());
		assertNull(config.getInput());
		assertTrue(config.getStrip());
		assertTrue(config.getOpen());
		assertEquals(PaneMode.CARD, config.getPaneMode());
		assertEquals(0, config.getPaneWidth());
		assertEquals("TLChatRail", config.getReactElement());
	}

	/** XML and React both consume the pane-mode's lowercase external names. */
	public void testPaneModeExternalNames() {
		assertEquals("card", PaneMode.CARD.getExternalName());
		assertEquals("document", PaneMode.DOCUMENT.getExternalName());
	}

	/** An empty rail is a legitimate configuration, not a parse failure. */
	public void testEmptyRailParses() throws Exception {
		Config config = parse("<chat-rail %s/>");
		assertTrue(config.getSections().isEmpty());
		assertTrue(config.getPane().isEmpty());
	}

	/** The sections parse in the order written, each with its own content element. */
	public void testSectionsParseInOrder() throws Exception {
		Config config = parse("<chat-rail %s chat-id='main' strip='false' open='false'>"
			+ "<section name='progress' icon='css:bi bi-list-check' open='true' badge='progressBadge'>"
			+ "<label>Progress</label>"
			+ "<data-list count='progressBadge'><rows>all(`tl.ai.execution:AgentTask`)</rows></data-list>"
			+ "</section>"
			+ "<section name='invoices'>"
			+ "<label>Invoices</label>"
			+ "<data-list><rows>all(`tl.ai.execution:AgentSession`)</rows></data-list>"
			+ "</section>"
			+ "</chat-rail>");

		assertEquals("main", config.getChatId());
		assertFalse(config.getStrip());
		assertFalse(config.getOpen());

		List<SectionConfig> sections = config.getSections();
		assertEquals(2, sections.size());

		SectionConfig progress = sections.get(0);
		assertEquals("progress", progress.getName());
		assertTrue(progress.getOpen());
		assertNotNull("The strip icon is a theme image, as on a <nav-item>.", progress.getIcon());
		assertEquals("css:bi bi-list-check", progress.getIcon().toEncodedForm());
		assertNotNull("The badge is a channel the content element writes.", progress.getBadge());
		assertEquals(1, progress.getContent().size());
		assertEquals("The progress section is an ordinary list of the agent's tasks.",
			DataListElement.class, progress.getContent().get(0).getImplementationClass());

		SectionConfig invoices = sections.get(1);
		assertEquals("invoices", invoices.getName());
		assertFalse("A section starts collapsed unless it says otherwise.", invoices.getOpen());
		assertNull(invoices.getIcon());
		assertNull(invoices.getBadge());
		assertEquals("Any view element is a section: nothing here is chat-specific.",
			DataListElement.class, invoices.getContent().get(0).getImplementationClass());
	}

	/** The chat's own sections are elements like any other; nothing registers them by name. */
	public void testChatSectionsAreOrdinaryElements() throws Exception {
		Config config = parse("<chat-rail %s>"
			+ "<section name='sources'><chat-sources chat-id='main'/></section>"
			+ "</chat-rail>");
		assertEquals(ChatSourcesElement.class,
			config.getSections().get(0).getContent().get(0).getImplementationClass());
	}

	/** The pane is a child of its own, shown while the input channel holds an object. */
	public void testPaneAndInputChannelParse() throws Exception {
		Config config = parse("<chat-rail %s input='selected'>"
			+ "<section name='sources'><chat-sources/></section>"
			+ "<pane><form input='selected'/></pane>"
			+ "</chat-rail>");
		assertNotNull(config.getInput());
		assertEquals(1, config.getPane().size());
		assertEquals(1, config.getSections().size());
	}

	private static Config parse(String xml) throws Exception {
		String attributes = "xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'";
		return (Config) TypedConfiguration.parse(CharacterContents.newContent(String.format(xml, attributes)));
	}

	/** Boots the module system so typed configuration resolves the nested element tags. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatRailConfig.class));
	}
}
