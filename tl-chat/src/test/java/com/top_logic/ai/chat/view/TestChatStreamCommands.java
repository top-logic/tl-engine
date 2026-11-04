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

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.ChatSSEHandler;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactCommandReply;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The commands a chat control answers for the conversation's stream ({@link ChatControl}), and how
 * long the stream they start lives.
 *
 * <p>
 * The browser holds one stream per conversation for all its chat elements and attaches it through
 * whichever of them mounted first. The stream therefore belongs to the conversation: it outlives
 * the control it was attached through while another chat control of the conversation is displayed,
 * and ends when none is - which is also how a page that is reloaded or closed ends it, since such a
 * page sends no command any more.
 * </p>
 */
public class TestChatStreamCommands extends BasicTestCase {

	private static final String WINDOW = "streamWindow";

	private ReactWindowRegistry _registry;

	private SSEUpdateQueue _queue;

	private DefaultViewContext _view;

	/** A chat id of its own per test: the bindings hang on the test thread's sub-session. */
	private String _chatId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLContextManager.getSubSession().getSessionContext().setIfAbsent(WINDOW, TLContextManager.getSubSession());
		_registry = new ReactWindowRegistry("stream-session");
		_queue = _registry.getOrCreateQueue(WINDOW);
		_view = new DefaultViewContext(new DefaultReactContext("/app", WINDOW, _queue, _registry));
		_chatId = "streams-" + getName();
	}

	/** An attach starts the conversation's frames and says so; an unusable token starts nothing. */
	public void testAttachStartsTheFrames() throws Exception {
		ChatConversationControl conversation = conversation();
		conversation.attach();
		try {
			int before = _queue.pendingEventCount();
			assertEquals(Boolean.TRUE, attach(conversation, "tok1"));
			assertTrue("The conversation's stream is the one a run finds.", stream(conversation).writesTo("tok1"));
			assertEquals("The first frames are delivered with the command.", before + 1, _queue.pendingEventCount());

			assertEquals("A token the browser may not choose starts nothing.", Boolean.FALSE,
				attach(conversation, "a:b"));
			assertTrue(stream(conversation).writesTo("tok1"));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A later attach replaces the stream; a detach of the replaced one leaves the new one alone. */
	public void testDetachStopsOnlyItsOwnStream() throws Exception {
		ChatConversationControl conversation = conversation();
		conversation.attach();
		try {
			attach(conversation, "tok1");
			ChatSSEHandler first = stream(conversation);
			attach(conversation, "tok2");
			ChatSSEHandler second = stream(conversation);

			assertFalse("The replaced stream stops.", first.isActive());
			command(conversation, ChatControl.CMD_DETACH_STREAM, Map.of(ChatControl.ARG_STREAM, "tok1"));
			assertTrue("A detach of the replaced stream leaves the new one alone.", second.isActive());

			command(conversation, ChatControl.CMD_DETACH_STREAM, Map.of(ChatControl.ARG_STREAM, "tok2"));
			assertFalse(second.isActive());
			assertNull(ChatEventHandlerRegistry.find(conversation.binding().tracker()));
		} finally {
			conversation.cleanupTree();
		}
	}

	/**
	 * The stream lives while any chat control of the conversation is displayed, not only the one it
	 * was attached through.
	 */
	public void testTheStreamOutlivesTheControlItWasAttachedThrough() throws Exception {
		ChatConversationControl page = conversation();
		ChatConversationControl dock = conversation();
		page.attach();
		dock.attach();
		try {
			assertSame("Two elements of one chat are one conversation.", page.binding(), dock.binding());
			attach(page, "tok1");
			ChatSSEHandler stream = stream(page);

			page.detach();
			assertTrue("Another element of the conversation is still displayed.", stream.isActive());

			dock.detach();
			assertFalse("No element of the conversation is displayed any more.", stream.isActive());
		} finally {
			page.cleanupTree();
			dock.cleanupTree();
		}
	}

	/** A window whose controls are disposed - a closed window - ends the stream with them. */
	public void testDisposingTheLastControlEndsTheStream() throws Exception {
		ChatConversationControl conversation = conversation();
		conversation.attach();
		attach(conversation, "tok1");
		ChatSSEHandler stream = stream(conversation);

		conversation.cleanupTree();

		assertFalse(stream.isActive());
	}

	/** Asked again, the run state goes out on the stream, with the command's interaction. */
	public void testRefreshSendsTheRunStateAgain() throws Exception {
		ChatConversationControl conversation = conversation();
		conversation.attach();
		try {
			int before = _queue.pendingEventCount();
			command(conversation, ChatControl.CMD_REFRESH_RUN_STATE, Map.of());
			assertEquals("Without a stream there is nothing to send it on.", before, _queue.pendingEventCount());

			attach(conversation, "tok1");
			command(conversation, ChatControl.CMD_REFRESH_RUN_STATE, Map.of());
			assertEquals("The first frames, then the run state again.", before + 2, _queue.pendingEventCount());
		} finally {
			conversation.cleanupTree();
		}
	}

	/** The stream commands are chrome: neither advertised to a headless agent nor recorded. */
	public void testTheStreamCommandsAreTechnical() throws Exception {
		ChatConversationControl conversation = conversation();
		try {
			for (String command : new String[] { ChatControl.CMD_ATTACH_STREAM, ChatControl.CMD_DETACH_STREAM,
				ChatControl.CMD_REFRESH_RUN_STATE }) {
				assertTrue(command, conversation.commandNames().contains(command));
				assertFalse(command, conversation.scriptingCommands().contains(command));
				assertFalse(command, conversation.isRecordable(command));
			}
		} finally {
			conversation.cleanupTree();
		}
	}

	private Object attach(ChatConversationControl conversation, String token) {
		HandlerResult result =
			command(conversation, ChatControl.CMD_ATTACH_STREAM, Map.of(ChatControl.ARG_STREAM, token));
		return ReactCommandReply.of(result).getFields().get(ChatControl.FIELD_ATTACHED);
	}

	/** Runs a command as the browser sends it: within an interaction of the window. */
	private HandlerResult command(ChatConversationControl conversation, String name, Map<String, Object> args) {
		try (Interaction interaction = _registry.beginInteraction()) {
			return conversation.executeClientCommand(name, args);
		}
	}

	private static ChatSSEHandler stream(ChatConversationControl conversation) {
		ChatEventHandler handler = ChatEventHandlerRegistry.find(conversation.binding().tracker());
		assertTrue("The conversation has a stream: " + handler, handler instanceof ChatSSEHandler);
		return (ChatSSEHandler) handler;
	}

	private ChatConversationControl conversation() throws Exception {
		return new ChatConversationControl(_view, (ChatConversationElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent("<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'"
				+ " chat-id='" + _chatId + "'/>")));
	}

	/** The model, a login and the services a chat control reads. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatStreamCommands.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE));
	}
}
