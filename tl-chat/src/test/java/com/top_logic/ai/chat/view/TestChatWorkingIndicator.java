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
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Checks {@code <chat-conversation working-indicator="…">}: what an application shows in place of
 * the three dots while the agent works, and the form the browser is told it in.
 */
public class TestChatWorkingIndicator extends TestCase {

	/** Nothing configured keeps the dots - which is every conversation until it is configured. */
	public void testDotsByDefault() {
		ChatConversationElement.Config config =
			TypedConfiguration.newConfigItem(ChatConversationElement.Config.class);

		assertNull(config.getWorkingIndicator());
		assertEquals("The empty string is what the browser reads as the dots.",
			"", ChatConversationControl.workingIndicator(config.getWorkingIndicator(), "/app"));
	}

	/** The attribute takes a theme image, the way every other icon of the chat is named. */
	public void testTheAttributeParses() throws Exception {
		ChatConversationElement.Config config = parse(" working-indicator='css:app-working'");

		assertEquals("css:app-working", config.getWorkingIndicator().toEncodedForm());
	}

	/** An icon class travels in its encoded form; the application's stylesheet draws it. */
	public void testAnIconClassTravelsAsItIs() {
		assertEquals("css:app-working",
			ChatConversationControl.workingIndicator(ThemeImage.icon("css:app-working"), "/app"));
		assertEquals("colored:app-working",
			ChatConversationControl.workingIndicator(ThemeImage.icon("colored:app-working"), "/app"));
	}

	/**
	 * An image travels as the URL it is served under.
	 *
	 * <p>
	 * Its encoded form is its path inside the application, which a browser resolves against the
	 * server root - right only for an application deployed there. The demo is not.
	 * </p>
	 */
	public void testAnImageTravelsWithTheContextPath() {
		String url = ChatConversationControl.workingIndicator(
			ThemeImage.icon("/images/app-working.svg"), "/tl-ai-demo");

		assertTrue("The context path is part of the URL, not left to the browser: " + url,
			url.startsWith("/tl-ai-demo/"));
		assertTrue(url, url.endsWith("/images/app-working.svg"));
	}

	private static ChatConversationElement.Config parse(String attributes) throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'"
			+ attributes + "/>";
		return (ChatConversationElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
	}

	/** Starts the theme, which resolving an image's file link asks. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(
			new TestSuite(TestChatWorkingIndicator.class), ThemeFactory.Module.INSTANCE));
	}
}
