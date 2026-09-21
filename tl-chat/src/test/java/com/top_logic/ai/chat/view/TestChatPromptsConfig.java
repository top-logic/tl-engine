/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatConversationElement.Prompt;
import com.top_logic.ai.chat.view.ChatConversationElement.Prompts;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Checks the {@code <prompts>} configuration of {@code <chat-conversation>}: the first questions a
 * page offers the user (Gitea #529).
 */
public class TestChatPromptsConfig extends TestCase {

	/** A conversation that offers nothing - which is every one of them until it is configured. */
	public void testNoPromptsByDefault() {
		ChatConversationElement.Config config =
			TypedConfiguration.newConfigItem(ChatConversationElement.Config.class);

		assertNull("A conversation offers no card until a <prompts> is configured.",
			config.getPrompts());
	}

	/**
	 * The shape the ticket documents: inputs, then the cards as direct children.
	 */
	public void testTheDocumentedShapeParses() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'>"
			+ "<prompts>"
			+ "<inputs><input channel='paneObject'/></inputs>"
			+ "<prompt icon='css:bi bi-receipt'>"
			+ "<label><en>Summarise</en><de>Zusammenfassen</de></label>"
			+ "<value>open -> \"Please summarise the newest invoices.\"</value>"
			+ "</prompt>"
			+ "<prompt>"
			+ "<label><en>Who sent it?</en><de>Von wem?</de></label>"
			+ "<value>open -> \"Who is this invoice from?\"</value>"
			+ "</prompt>"
			+ "</prompts>"
			+ "</chat-conversation>";

		ChatConversationElement.Config config = (ChatConversationElement.Config) TypedConfiguration
			.parse(CharacterContents.newContent(xml));
		Prompts prompts = config.getPrompts();

		assertNotNull(prompts);
		assertEquals("One input channel, the leading argument of every card expression.",
			1, prompts.getInputs().size());
		assertEquals("paneObject", prompts.getInputs().get(0).getChannelName());
		assertEquals(2, prompts.getCards().size());

		Prompt summarise = prompts.getCards().get(0);
		assertNotNull("A card says what it is called.", summarise.getLabel());
		assertNotNull("A card with an icon keeps it.", summarise.getIcon());
		assertNotNull("A card says what clicking it puts into the composer.", summarise.getValue());

		Prompt sender = prompts.getCards().get(1);
		assertNull("A card without an icon shows its label alone.", sender.getIcon());
		assertNotNull(sender.getValue());
	}

	/**
	 * The cards are gone once the conversation has something in it, unless the page says otherwise.
	 *
	 * <p>
	 * The safe default: a first question is what an empty screen is short of, and a row of openers
	 * under the composer of a running conversation would compete with the transcript the user is
	 * actually reading.
	 * </p>
	 */
	public void testCardsAreEmptyOnlyByDefault() throws Exception {
		assertTrue("A page that says nothing offers its cards on the empty conversation alone.",
			parsePrompts("").getEmptyOnly());
		assertFalse("A page whose cards are standing commands switches that off.",
			parsePrompts(" empty-only='false'").getEmptyOnly());
	}

	/** A {@code <prompts>} with no {@code <inputs>} holds cards whose expressions take no arguments. */
	public void testCardsWithoutInputs() throws Exception {
		Prompts prompts = parsePrompts("");

		assertTrue(prompts.getInputs().isEmpty());
		assertEquals(1, prompts.getCards().size());
	}

	private static Prompts parsePrompts(String attributes) throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'>"
			+ "<prompts" + attributes + ">"
			+ "<prompt><label><en>Ask</en><de>Fragen</de></label><value>\"Ask this.\"</value></prompt>"
			+ "</prompts>"
			+ "</chat-conversation>";

		ChatConversationElement.Config config = (ChatConversationElement.Config) TypedConfiguration
			.parse(CharacterContents.newContent(xml));
		return config.getPrompts();
	}

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatPromptsConfig.class));
	}
}
