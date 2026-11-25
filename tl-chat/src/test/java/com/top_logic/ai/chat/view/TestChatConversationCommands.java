/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Tests the {@code <commands>} of a {@code <chat-conversation>}: buttons at the end of the head of
 * the transcript, e.g. the one opening and closing a {@code <side-rail>} beside the conversation.
 */
public class TestChatConversationCommands extends BasicTestCase {

	private static final String OPEN = "railOpen";

	/** A configured command becomes a toolbar in the head; without commands there is none. */
	public void testCommandsBecomeAToolbarInTheHead() throws Exception {
		DefaultViewChannel open = new DefaultViewChannel(OPEN);
		ChatConversationControl withCommand = conversation(open,
			"<commands><generic-command image='css:bi bi-layout-sidebar-reverse' label='rail'>"
				+ "<execute-script><inputs><input as='open' channel='" + OPEN + "'/></inputs>"
				+ "<function>!$open</function>"
				+ "<with-result><write-channel name='" + OPEN + "'/></with-result>"
				+ "</execute-script></generic-command></commands>");
		try {
			Object toolbar = state(withCommand).get(ChatConversationControl.HEADER_COMMANDS);
			assertNotNull("The head shows the commands.", toolbar);
		} finally {
			withCommand.cleanupTree();
		}

		ChatConversationControl without = conversation(new DefaultViewChannel(OPEN), "");
		try {
			assertNull("Without commands the head shows none.",
				state(without).get(ChatConversationControl.HEADER_COMMANDS));
		} finally {
			without.cleanupTree();
		}
	}

	private static Map<?, ?> state(ReactControl control) throws Exception {
		return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
	}

	private static ChatConversationControl conversation(DefaultViewChannel open, String children)
			throws Exception {
		String window = "commandsWindow";
		TLContextManager.getSubSession().getSessionContext().setIfAbsent(window, TLContextManager.getSubSession());
		ReactWindowRegistry registry = new ReactWindowRegistry("test-session");
		DefaultViewContext view = new DefaultViewContext(
			new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry));
		view.registerChannel(OPEN, open);
		return new ChatConversationControl(view, (ChatConversationElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent("<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'>"
				+ children + "</chat-conversation>")));
	}

	/** The model, a login and the services a chat control and its commands read. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatConversationCommands.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE,
				SessionService.Module.INSTANCE));
	}
}
