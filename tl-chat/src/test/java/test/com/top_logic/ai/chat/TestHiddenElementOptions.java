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

import com.top_logic.ai.chat.view.ChatConversationElement;
import com.top_logic.ai.chat.view.ChatDockElement;
import com.top_logic.ai.chat.view.ChatRailElement;
import com.top_logic.ai.chat.view.ChatScopeElement;
import com.top_logic.ai.chat.view.ChatSectionElement;
import com.top_logic.ai.chat.view.ChatSourcesElement;
import com.top_logic.ai.chat.view.list.DataListElement;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.layout.view.UIElement;

/**
 * Pins which options of the chat view elements the layout editor does not offer.
 *
 * <p>
 * The design view renders a property unless it carries {@link Hidden}, so the annotation is the
 * whole surface decision: an option that points at a TypeScript registration, or that overrides
 * what the user personalized, has no meaning for a designer. Losing one of these annotations puts
 * the option back into the form without anybody noticing, which is what this test exists to stop.
 * </p>
 */
public class TestHiddenElementOptions extends TestCase {

	/** {@code <data-list>} hides only its React binding, plus the two engine-inherited options. */
	public void testDataListHiddenOptions() {
		assertHidden(DataListElement.Config.class, DataListElement.Config.REACT_ELEMENT);
		assertEngineOptionsHidden(DataListElement.Config.class);

		assertVisible(DataListElement.Config.class, DataListElement.Config.ROWS);
		assertVisible(DataListElement.Config.class, DataListElement.Config.SELECTION);
		assertVisible(DataListElement.Config.class, DataListElement.Config.SEARCH);
		assertVisible(DataListElement.Config.class, DataListElement.Config.COUNT);
		assertVisible(DataListElement.Config.class, DataListElement.Config.CSS_CLASS);
		assertVisible(DataListElement.Config.class, DataListElement.Config.COLLAPSED);
		// Row actions are an administration decision, not a TypeScript name: the designer configures
		// which commands a row offers, what the local channel is called and when the trigger shows.
		assertVisible(DataListElement.Config.class, DataListElement.Config.ROW_COMMANDS);
		assertVisible(DataListElement.Config.class, DataListElement.Config.ROW_CHANNEL);
		assertVisible(DataListElement.Config.class, DataListElement.Config.ROW_ACTIONS);
	}

	/** {@code <chat-conversation>} hides the four names that only mean something in TypeScript. */
	public void testChatConversationHiddenOptions() {
		assertHidden(ChatConversationElement.Config.class, ChatConversationElement.Config.REACT_ELEMENT);
		assertHidden(ChatConversationElement.Config.class, ChatConversationElement.Config.CITATION_RENDERER);
		assertHidden(ChatConversationElement.Config.class, ChatConversationElement.Config.REFERENCE_SOURCE);
		assertHidden(ChatConversationElement.Config.class, ChatConversationElement.Config.MENU_ITEMS);
		assertHidden(ChatConversationElement.Actions.class, ChatConversationElement.Actions.HOST);
		assertEngineOptionsHidden(ChatConversationElement.Config.class);

		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.TRACE);
		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.CITATIONS);
		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.CHAT_ID);
		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.AGENT);
		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.MODEL);
		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.EFFORT);
		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.MCP_SERVERS);
		// Wiring a designer does, not a TypeScript name: which channel carries the composer's unsent
		// text, and which one puts a model object into it as a reference token.
		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.DRAFT);
		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.REFERENCE);
		assertVisible(ChatConversationElement.Actions.class, ChatConversationElement.Actions.COPY);
		// The composer's invitation is words an administration writes, not a TypeScript name (#420).
		assertVisible(ChatConversationElement.Config.class, ChatConversationElement.Config.PLACEHOLDER);
	}

	/** {@code <chat>} hides only the two engine-inherited options; the id it hands down is visible. */
	public void testChatScopeHiddenOptions() {
		assertEngineOptionsHidden(ChatScopeElement.Config.class);

		assertVisible(ChatScopeElement.Config.class, ChatScopeElement.Config.CHAT_ID);
	}

	/** {@code <chat-dock>} hides only its React binding, plus the two engine-inherited options. */
	public void testChatDockHiddenOptions() {
		assertHidden(ChatDockElement.Config.class, ChatDockElement.Config.REACT_ELEMENT);
		assertEngineOptionsHidden(ChatDockElement.Config.class);

		assertVisible(ChatDockElement.Config.class, ChatDockElement.Config.CHAT_ID);
		assertVisible(ChatDockElement.Config.class, ChatDockElement.Config.CONTENT);
		assertVisible(ChatDockElement.Config.class, ChatDockElement.Config.ICON);
		assertVisible(ChatDockElement.Config.class, ChatDockElement.Config.LABEL);
		assertVisible(ChatDockElement.Config.class, ChatDockElement.Config.OPEN);
	}

	/** {@code <chat-rail>} hides its React binding and the pane width the user drags. */
	public void testChatRailHiddenOptions() {
		assertHidden(ChatRailElement.Config.class, ChatRailElement.Config.REACT_ELEMENT);
		assertHidden(ChatRailElement.Config.class, ChatRailElement.Config.PANE_WIDTH);
		assertEngineOptionsHidden(ChatRailElement.Config.class);

		assertVisible(ChatRailElement.Config.class, ChatRailElement.Config.STRIP);
		assertVisible(ChatRailElement.Config.class, ChatRailElement.Config.PANE_MODE);
		assertVisible(ChatRailElement.Config.class, ChatRailElement.Config.SECTIONS);
	}

	/** The rail-section element hides its React binding, like every other element. */
	public void testChatSectionHiddenOptions() {
		assertHidden(ChatSourcesElement.Config.class, ChatSectionElement.Config.REACT_ELEMENT);
		assertEngineOptionsHidden(ChatSourcesElement.Config.class);

		assertVisible(ChatSourcesElement.Config.class, ChatSectionElement.Config.CHAT_ID);
		assertVisible(ChatSourcesElement.Config.class, ChatSectionElement.Config.BADGE);
	}

	/**
	 * The two properties every element inherits from the engine: the implementation class the tag
	 * already fixes, and the personalization key the view context derives.
	 */
	private static void assertEngineOptionsHidden(Class<? extends ConfigurationItem> configType) {
		assertHidden(configType, UIElement.Config.PERSONALIZATION_KEY);
		assertHidden(configType, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
	}

	private static void assertHidden(Class<? extends ConfigurationItem> configType, String propertyName) {
		PropertyDescriptor property = property(configType, propertyName);
		Hidden annotation = property.getAnnotation(Hidden.class);
		assertNotNull("Property '" + propertyName + "' of " + configType.getName()
			+ " must be hidden from the layout editor.", annotation);
		assertTrue("Property '" + propertyName + "' of " + configType.getName()
			+ " carries @Hidden(false).", annotation.value());
	}

	private static void assertVisible(Class<? extends ConfigurationItem> configType, String propertyName) {
		PropertyDescriptor property = property(configType, propertyName);
		Hidden annotation = property.getAnnotation(Hidden.class);
		assertTrue("Property '" + propertyName + "' of " + configType.getName()
			+ " is a designer option and must stay visible.", annotation == null || !annotation.value());
	}

	private static PropertyDescriptor property(Class<? extends ConfigurationItem> configType,
			String propertyName) {
		PropertyDescriptor property =
			TypedConfiguration.getConfigurationDescriptor(configType).getProperty(propertyName);
		assertNotNull("No property '" + propertyName + "' in " + configType.getName() + ".", property);
		return property;
	}

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestHiddenElementOptions.class));
	}
}
