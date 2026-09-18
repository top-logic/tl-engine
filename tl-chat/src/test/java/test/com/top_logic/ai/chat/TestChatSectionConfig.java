/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatSourcesElement;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Defaults and channel binding of the chat's own rail-section element.
 */
public class TestChatSectionConfig extends TestCase {

	/** It joins the default chat and counts nothing until a badge channel is named. */
	public void testDefaults() {
		ChatSourcesElement.Config sources =
			TypedConfiguration.newConfigItem(ChatSourcesElement.Config.class);
		assertNull("No id of its own: the enclosing <chat> names it, else it is the default chat.",
			sources.getChatId());
		assertNull(sources.getBadge());
		assertEquals("TLChatSources", sources.getReactElement());
	}

	/** The badge channel is what a section reports its count on; the rail reads the same one. */
	public void testBadgeChannelParses() throws Exception {
		ChatSourcesElement.Config config = (ChatSourcesElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent("<chat-sources"
				+ " xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + ChatSourcesElement.Config.class.getName() + "'"
				+ " chat-id='grouped' badge='sourcesBadge'/>"));
		assertEquals("grouped", config.getChatId());
		assertNotNull(config.getBadge());
	}

	/** A section with no badge channel is legitimate: not everything has something to count. */
	public void testSourcesWithoutBadge() throws Exception {
		ChatSourcesElement.Config config = (ChatSourcesElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent("<chat-sources"
				+ " xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + ChatSourcesElement.Config.class.getName() + "'"
				+ " chat-id='grouped'/>"));
		assertEquals("grouped", config.getChatId());
		assertNull(config.getBadge());
	}

	/** Boots the module system so typed configuration resolves the channel format via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatSectionConfig.class));
	}
}
