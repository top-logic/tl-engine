/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The placeholder of the prompt field follows the page with {@code <placeholder-by>}.
 *
 * <p>
 * One chat element on every page of an application asked "about this project" also where no
 * project was open, because the placeholder was one fixed text.
 * </p>
 */
public class TestChatPlaceholderBy extends BasicTestCase {

	private static final String SCOPE = "scope";

	/** The text follows its input, and an empty one leaves the fixed placeholder in force. */
	public void testThePlaceholderFollowsTheChannel() throws Exception {
		DefaultViewChannel scope = new DefaultViewChannel(SCOPE);
		scope.set("Netzausbau");
		ChatConversationControl conversation = conversation(scope,
			"<placeholder><en>Ask anything.</en></placeholder>"
				+ "<placeholder-by><inputs><input channel='" + SCOPE + "'/></inputs>"
				+ "<value>s -> if($s == null, \"Ask about all projects.\", \"Ask about \" + $s + \".\")</value>"
				+ "</placeholder-by>");
		try {
			assertEquals("Ask about Netzausbau.", placeholder(conversation));

			scope.set(null);
			assertEquals("Ask about all projects.", placeholder(conversation));
		} finally {
			conversation.cleanupTree();
		}

		DefaultViewChannel empty = new DefaultViewChannel(SCOPE);
		ChatConversationControl fallback = conversation(empty,
			"<placeholder><en>Ask anything.</en></placeholder>"
				+ "<placeholder-by><inputs><input channel='" + SCOPE + "'/></inputs>"
				+ "<value>s -> \"\"</value></placeholder-by>");
		try {
			assertEquals("Ask anything.", placeholder(fallback));
		} finally {
			fallback.cleanupTree();
		}
	}

	private static ChatConversationControl conversation(DefaultViewChannel scope, String children)
			throws Exception {
		String window = "placeholderWindow";
		TLContextManager.getSubSession().getSessionContext().setIfAbsent(window, TLContextManager.getSubSession());
		ReactWindowRegistry registry = new ReactWindowRegistry("test-session");
		DefaultViewContext view = new DefaultViewContext(
			new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry));
		view.registerChannel(SCOPE, scope);
		return new ChatConversationControl(view, (ChatConversationElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent("<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'>"
				+ children + "</chat-conversation>")));
	}

	private static Object placeholder(ChatConversationControl conversation) {
		return conversation.scriptingScalarState().get("placeholder");
	}

	/** The model, a login and the services a chat control reads. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatPlaceholderBy.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE));
	}
}
