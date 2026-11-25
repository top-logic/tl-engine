/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatConversationElement;
import com.top_logic.ai.chat.view.ChatScope;
import com.top_logic.ai.chat.view.ChatScopeElement;
import com.top_logic.ai.chat.view.ChatScopeElement.Config;
import com.top_logic.layout.view.list.DataListElement;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.element.SideRailElement;
import com.top_logic.layout.view.element.SplitPanelElement;

/**
 * The configuration surface of {@code <chat>}: the id it hands down and the one element it holds.
 */
public class TestChatScopeConfig extends TestCase {

	/** Unconfigured, a {@code <chat>} is the default conversation without content. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals(ChatScope.DEFAULT_CHAT_ID, config.getChatId());
		assertTrue(config.getChildren().isEmpty());
		assertEquals(ChatScopeElement.class, config.getImplementationClass());
	}

	/** The content is written directly inside the {@code <chat>}; a layout element holds several. */
	public void testContentParses() throws Exception {
		Config config = parse("<chat %s chat-id='support'>"
			+ "<split-panel orientation='horizontal' resizable='true'><panes>"
			+ "<pane><chat-conversation/></pane>"
			+ "<pane><side-rail><section name='sources'><chat-sources/></section></side-rail></pane>"
			+ "</panes></split-panel>"
			+ "</chat>");

		assertEquals("support", config.getChatId());

		List<UIElement.Config> children = config.getChildren();
		assertEquals(1, children.size());
		assertEquals(SplitPanelElement.class, children.get(0).getImplementationClass());
	}

	/** A layout option of {@code <stack>} is no option of {@code <chat>}. */
	public void testNoLayoutOptions() {
		assertNull(TypedConfiguration.getConfigurationDescriptor(Config.class).getProperty("direction"));
		assertNull(TypedConfiguration.getConfigurationDescriptor(Config.class).getProperty("gap"));
		assertNull(TypedConfiguration.getConfigurationDescriptor(Config.class).getProperty("align"));
	}

	/** The side rail of a chat is a chat element's neighbour, not a property of {@code <chat>}. */
	public void testRailIsContent() throws Exception {
		Config config = parse("<chat %s><side-rail><section name='sources'><chat-sources/></section></side-rail></chat>");
		assertEquals(SideRailElement.class, config.getChildren().get(0).getImplementationClass());
	}

	/** The point of the element: the nested elements say nothing about which chat they show. */
	public void testNestedElementsNameNoChatId() throws Exception {
		Config config = parse("<chat %s chat-id='support'><chat-conversation/></chat>");
		ChatConversationElement.Config conversation =
			(ChatConversationElement.Config) config.getChildren().get(0);
		assertNull("Inheriting from <chat> is the normal case, so there is no default to fall into.",
			conversation.getChatId());
	}

	/** A nested element may still break out, which is what an explicit id means. */
	public void testNestedElementMayOverride() throws Exception {
		Config config = parse("<chat %s chat-id='support'><chat-conversation chat-id='other'/></chat>");
		ChatConversationElement.Config conversation =
			(ChatConversationElement.Config) config.getChildren().get(0);
		assertEquals("other", conversation.getChatId());
	}

	/** Nothing about it is chat-specific: any view element may sit inside. */
	public void testAnyElementMayBeNested() throws Exception {
		Config config = parse("<chat %s>"
			+ "<data-list><rows>all(`tl.ai.execution:AgentSession`)</rows></data-list>"
			+ "</chat>");
		assertEquals(DataListElement.class, config.getChildren().get(0).getImplementationClass());
	}

	private static Config parse(String xml) throws Exception {
		String attributes = "xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'";
		return (Config) TypedConfiguration.parse(CharacterContents.newContent(String.format(xml, attributes)));
	}

	/** Boots the module system so typed configuration resolves the nested element tags. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatScopeConfig.class));
	}
}
