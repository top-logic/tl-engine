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

import com.top_logic.ai.chat.view.ChatProgressElement;
import com.top_logic.ai.chat.view.ChatSourcesElement;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Defaults and channel binding of the chat's own rail-section elements.
 */
public class TestChatSectionConfig extends TestCase {

	/** Both join the default chat and count nothing until a badge channel is named. */
	public void testDefaults() {
		ChatProgressElement.Config progress =
			TypedConfiguration.newConfigItem(ChatProgressElement.Config.class);
		assertEquals("default", progress.getChatId());
		assertNull(progress.getBadge());
		assertEquals("TLChatProgress", progress.getReactElement());

		ChatSourcesElement.Config sources =
			TypedConfiguration.newConfigItem(ChatSourcesElement.Config.class);
		assertEquals("default", sources.getChatId());
		assertNull(sources.getBadge());
		assertEquals("TLChatSources", sources.getReactElement());
	}

	/** The badge channel is what a section reports its count on; the rail reads the same one. */
	public void testBadgeChannelParses() throws Exception {
		ChatProgressElement.Config config = (ChatProgressElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent("<chat-progress"
				+ " xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + ChatProgressElement.Config.class.getName() + "'"
				+ " chat-id='grouped' badge='progressBadge'/>"));
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
