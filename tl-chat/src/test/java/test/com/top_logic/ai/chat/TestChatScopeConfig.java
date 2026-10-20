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
import com.top_logic.ai.chat.view.ChatRailElement;
import com.top_logic.ai.chat.view.ChatScope;
import com.top_logic.ai.chat.view.ChatScopeElement;
import com.top_logic.ai.chat.view.ChatScopeElement.Config;
import com.top_logic.ai.chat.view.list.DataListElement;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.view.UIElement;

/**
 * The configuration surface of {@code <chat>}: the id it hands down and the children it lays out.
 */
public class TestChatScopeConfig extends TestCase {

	/** Unconfigured, a {@code <chat>} is the default conversation and an empty column. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals(ChatScope.DEFAULT_CHAT_ID, config.getChatId());
		assertTrue(config.getChildren().isEmpty());
		assertEquals("It lays out like a <stack>, which is a column unless told otherwise.",
			StackDirection.COLUMN, config.getDirection());
		assertEquals(ChatScopeElement.class, config.getImplementationClass());
	}

	/** The children are written directly, as in a {@code <stack>}, and keep their order. */
	public void testChildrenParseInOrder() throws Exception {
		Config config = parse("<chat %s chat-id='support' direction='row'>"
			+ "<chat-conversation/>"
			+ "<chat-rail><section name='sources'><chat-sources/></section></chat-rail>"
			+ "</chat>");

		assertEquals("support", config.getChatId());
		assertEquals(StackDirection.ROW, config.getDirection());

		List<PolymorphicConfiguration<? extends UIElement>> children = config.getChildren();
		assertEquals(2, children.size());
		assertEquals(ChatConversationElement.class, children.get(0).getImplementationClass());
		assertEquals(ChatRailElement.class, children.get(1).getImplementationClass());
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
