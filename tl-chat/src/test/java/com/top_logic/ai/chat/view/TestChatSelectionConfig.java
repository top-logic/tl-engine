/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatConversationElement.Selection;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * The {@code <selection/>} of {@code <chat-conversation>}: the one line a page writes to tell the
 * agent which object the user has selected (Gitea #494).
 */
public class TestChatSelectionConfig extends TestCase {

	/** A conversation states no selection until a page names the channel holding one. */
	public void testNoSelectionByDefault() {
		ChatConversationElement.Config config =
			TypedConfiguration.newConfigItem(ChatConversationElement.Config.class);

		assertNull("A conversation states no selection until it is configured.", config.getSelection());
	}

	/** The one-line form the ticket is about, with everything else left to the default. */
	public void testTheOneLineFormParses() throws Exception {
		Selection selection = parse("<selection channel='selectedInvoice'/>");

		assertEquals("selectedInvoice", selection.getChannel().getChannelName());
		assertEquals("The default name is what the model reads unless the page says otherwise.",
			ChatSelection.DEFAULT_NAME, selection.getName());
		assertNull("Without an expression the application's own label is used.", selection.getLabel());
	}

	/** A page that knows a better word for what it holds says it. */
	public void testAPageMayNameTheFactAndTheLabel() throws Exception {
		Selection selection = parse("<selection channel='paneObject' name='Open event'>"
			+ "<label>e -> $e.get(`demo.events:Named#name`)</label>"
			+ "</selection>");

		assertEquals("paneObject", selection.getChannel().getChannelName());
		assertEquals("Open event", selection.getName());
		assertNotNull(selection.getLabel());
	}

	private static Selection parse(String selectionTag) throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'>"
			+ selectionTag
			+ "</chat-conversation>";
		ChatConversationElement.Config config = (ChatConversationElement.Config) TypedConfiguration
			.parse(CharacterContents.newContent(xml));
		return config.getSelection();
	}

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatSelectionConfig.class));
	}
}
