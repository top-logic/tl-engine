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

import com.top_logic.ai.chat.view.ChatConversationElement.Context;
import com.top_logic.ai.chat.view.ChatConversationElement.Fact;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Checks the {@code <context>} configuration of {@code <chat-conversation>}: what a page states
 * about the situation the user is asking in (Gitea #437).
 */
public class TestChatContextConfig extends TestCase {

	/** A conversation that states nothing - which is every one of them until it is configured. */
	public void testNoContextByDefault() {
		ChatConversationElement.Config config =
			TypedConfiguration.newConfigItem(ChatConversationElement.Config.class);

		assertNull("A conversation states nothing until a <context> is configured.",
			config.getContext());
	}

	/**
	 * The shape the ticket documents: inputs, then the facts as direct children.
	 */
	public void testTheDocumentedShapeParses() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'>"
			+ "<context>"
			+ "<inputs><input channel='project'/></inputs>"
			+ "<fact name='Project' stable='true'><value>p -> f -> $p.label()</value></fact>"
			+ "<fact name='Selection' stable='false'><value>p -> f -> $f.label()</value></fact>"
			+ "</context>"
			+ "</chat-conversation>";

		ChatConversationElement.Config config = (ChatConversationElement.Config) TypedConfiguration
			.parse(CharacterContents.newContent(xml));
		Context context = config.getContext();

		assertNotNull(context);
		assertEquals("One input channel, the leading argument of every fact.",
			1, context.getInputs().size());
		assertEquals("project", context.getInputs().get(0).getChannelName());
		assertEquals(2, context.getFacts().size());

		Fact project = context.getFacts().get(0);
		assertEquals("Project", project.getName());
		assertTrue("The project a chat belongs to holds for the whole conversation.",
			project.getStable());
		assertNotNull(project.getValue());

		Fact selection = context.getFacts().get(1);
		assertEquals("Selection", selection.getName());
		assertFalse("What the user has selected changes while the conversation runs.",
			selection.getStable());
	}

	/**
	 * A fact is stable unless it says otherwise.
	 *
	 * <p>
	 * The safe default: a fact a host states without saying more is one about the situation as a
	 * whole, and the system prompt is where such a statement costs nothing per turn.
	 * </p>
	 */
	public void testAFactIsStableByDefault() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'>"
			+ "<context><fact name='Tenant'><value>\"ACME\"</value></fact></context>"
			+ "</chat-conversation>";

		ChatConversationElement.Config config = (ChatConversationElement.Config) TypedConfiguration
			.parse(CharacterContents.newContent(xml));

		assertTrue(config.getContext().getFacts().get(0).getStable());
		assertTrue("A context without <inputs> is a context whose facts take no arguments.",
			config.getContext().getInputs().isEmpty());
	}

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatContextConfig.class));
	}
}
