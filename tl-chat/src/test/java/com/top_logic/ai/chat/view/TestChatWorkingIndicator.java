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
import test.com.top_logic.basic.module.ServiceTestSetup;

import java.lang.reflect.Proxy;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;

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
			"", ChatIcons.encode(context("/app"), config.getWorkingIndicator()));
	}

	/** The attribute takes a theme image, the way every other icon of the chat is named. */
	public void testTheAttributeParses() throws Exception {
		ChatConversationElement.Config config = parse(" working-indicator='css:app-working'");

		assertEquals("css:app-working", config.getWorkingIndicator().toEncodedForm());
	}

	/** An icon class travels in its encoded form; the application's stylesheet draws it. */
	public void testAnIconClassTravelsAsItIs() {
		assertEquals("css:app-working",
			ChatIcons.encode(context("/app"), ThemeImage.icon("css:app-working")));
		assertEquals("colored:app-working",
			ChatIcons.encode(context("/app"), ThemeImage.icon("colored:app-working")));
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
		String url = ChatIcons.encode(context("/tl-ai-demo"), ThemeImage.icon("/images/app-working.svg"));

		assertTrue("The context path is part of the URL, not left to the browser: " + url,
			url.startsWith("/tl-ai-demo/"));
		assertTrue(url, url.endsWith("/images/app-working.svg"));
	}

	/**
	 * The invisible image draws nothing of its own, as no icon does - not the word {@code none} as an
	 * icon class.
	 */
	public void testTheInvisibleImageIsNoIcon() {
		assertEquals("", ChatIcons.encode(context("/app"), ThemeImage.none()));
	}

	/** A context that knows the application's context path and nothing else. */
	private static ReactContext context(String contextPath) {
		return (ReactContext) Proxy.newProxyInstance(TestChatWorkingIndicator.class.getClassLoader(),
			new Class<?>[] { ReactContext.class }, (proxy, method, args) -> {
				if (method.getName().equals("getContextPath")) {
					return contextPath;
				}
				throw new UnsupportedOperationException(method.getName());
			});
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
