/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse;
import com.top_logic.ai.chat.api.messages.ChatSessionDto;
import com.top_logic.ai.chat.api.messages.RunStateEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactCommandReply;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The chats of the account as the chat controls serve them: opening one
 * ({@link ChatControl#CMD_SELECT_SESSION}), deleting one ({@link ChatControl#CMD_DELETE_SESSION}),
 * reading a transcript ({@link ChatControl#getDownloadData(String)}), and the list and the
 * catalogue a conversation writes with every render.
 *
 * <p>
 * Every one of them takes a chat id from the browser, so every one of them is asked here for a chat
 * of another account - and answers exactly as for an id that names nothing, so that the answer says
 * nothing about which ids exist.
 * </p>
 */
public class TestChatSessionCommands extends BasicTestCase {

	private static final String WINDOW = "sessionsWindow";

	/** The chats and the two accounts, made once: no test here changes them but the deleting ones. */
	private static Person anna;

	private static Person ben;

	private static TLObject annasChat;

	private ReactWindowRegistry _registry;

	private DefaultViewContext _view;

	/** A chat id of its own per test: the bindings hang on the test thread's sub-session. */
	private String _chatId;

	private Person _previousPerson;

	/** Exposes what the control writes into its state, and its render hook. */
	static final class Probe extends ChatConversationControl {

		Probe(DefaultViewContext context, ChatConversationElement.Config config) {
			super(context, config);
		}

		Object state(String key) {
			return getState(key);
		}

		void render() {
			onBeforeWrite();
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-with-a-chat-to-open");
			ben = TestPerson.createPerson("ben-without-chats");
			annasChat = ChatHistoryService.createSession("Annas Chat", anna);
			HistoryUtils.updateSessionRevision();
		}
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		subSession.getSessionContext().setIfAbsent(WINDOW, subSession);
		_previousPerson = subSession.getPerson();
		_registry = new ReactWindowRegistry("sessions-session");
		SSEUpdateQueue queue = _registry.getOrCreateQueue(WINDOW);
		_view = new DefaultViewContext(new DefaultReactContext("/app", WINDOW, queue, _registry));
		_chatId = "sessions-" + getName();
	}

	@Override
	protected void tearDown() throws Exception {
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/** The owner opens their chat: the conversation runs it from then on, with its run state. */
	public void testTheOwnerOpensTheirChat() throws Exception {
		Probe conversation = conversation(anna);
		try {
			ReactCommandReply reply = reply(command(conversation, ChatControl.CMD_SELECT_SESSION, id()));

			assertEquals(Boolean.TRUE, reply.getFields().get(ChatControl.FIELD_OPENED));
			assertEquals(id(), conversation.binding().tracker().getActiveSessionId());
			Object runState = reply.getFields().get(ChatControl.FIELD_RUN_STATE);
			assertTrue("The open answers with the chat's run state: " + runState, runState instanceof RunStateEvent);
			assertEquals(id(), ((RunStateEvent) runState).getSessionId());
		} finally {
			conversation.cleanupTree();
		}
	}

	/** Opening another account's chat is refused, and the conversation keeps what it had. */
	public void testOpeningAnotherAccountsChatIsRefused() throws Exception {
		Probe conversation = conversation(ben);
		try {
			ReactCommandReply reply = reply(command(conversation, ChatControl.CMD_SELECT_SESSION, id()));

			assertEquals(Boolean.FALSE, reply.getFields().get(ChatControl.FIELD_OPENED));
			assertNull(reply.getFields().get(ChatControl.FIELD_RUN_STATE));
			assertNull(conversation.binding().tracker().getActiveSessionId());
		} finally {
			conversation.cleanupTree();
		}
	}

	/** The owner reads their chat's transcript, once the chat is open. */
	public void testTheOwnerReadsTheTranscript() throws Exception {
		Probe conversation = conversation(anna);
		try {
			command(conversation, ChatControl.CMD_SELECT_SESSION, id());

			String body = read(conversation.getDownloadData(sessionKey(id())));

			assertTrue(body, body.contains("\"id\":\"" + id() + "\""));
			assertTrue(body, body.contains("\"title\":\"Annas Chat\""));
			assertTrue("The window's copy of the chat states the mode: " + body, body.contains("\"approvalMode\":"));
			assertFalse("The raw transcript is not sent twice: " + body, body.contains("messagesJson"));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** Another account's transcript reads as nothing, exactly like an id that names nothing. */
	public void testAnotherAccountsTranscriptIsNotFound() throws Exception {
		Probe conversation = conversation(ben);
		try {
			assertNull(conversation.getDownloadData(sessionKey(id())));
			assertNull(conversation.getDownloadData(sessionKey("999999999")));
			assertNull("A run of another account's chat is not found by its id either.",
				conversation.getDownloadData(ChatControl.DATA_SUBAGENT + ChatControl.DATA_KEY_SEPARATOR + "sa-1"
					+ ChatControl.DATA_KEY_SEPARATOR + "no-such-run"));
			assertNull("A key of no known kind names nothing.", conversation.getDownloadData("other/" + id()));
			assertNull(conversation.getDownloadData(null));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** Deleting another account's chat is refused, and the chat is still there. */
	public void testDeletingAnotherAccountsChatIsRefused() throws Exception {
		Probe conversation = conversation(ben);
		try {
			ReactCommandReply reply = reply(command(conversation, ChatControl.CMD_DELETE_SESSION, id()));

			assertEquals(Boolean.FALSE, reply.getFields().get(ChatControl.FIELD_DELETED));
			assertNotNull("The chat must still be there.", ChatHistoryService.findOwnedSessionById(id(), anna));
			assertEquals("An id that names nothing is answered alike.", Boolean.FALSE,
				reply(command(conversation, ChatControl.CMD_DELETE_SESSION, "999999999")).getFields()
					.get(ChatControl.FIELD_DELETED));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** The owner deletes their chat. */
	public void testTheOwnerDeletesTheirChat() throws Exception {
		TLObject doomed = ChatHistoryService.createSession("Weg damit", anna);
		HistoryUtils.updateSessionRevision();
		String doomedId = ChatSessionIds.idOf(doomed);
		Probe conversation = conversation(anna);
		try {
			ReactCommandReply reply = reply(command(conversation, ChatControl.CMD_DELETE_SESSION, doomedId));
			HistoryUtils.updateSessionRevision();

			assertEquals(Boolean.TRUE, reply.getFields().get(ChatControl.FIELD_DELETED));
			assertNull(ChatHistoryService.findOwnedSessionById(doomedId, anna));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A render writes the account's chats and what it may choose from, and nobody else's chats. */
	public void testARenderWritesTheListAndTheCatalogue() throws Exception {
		Probe conversation = conversation(anna);
		try {
			conversation.render();

			List<?> sessions = (List<?>) conversation.state(SessionListWatch.SESSIONS);
			assertTrue("Anna's chat is listed: " + sessions, sessions.stream()
				.anyMatch(session -> id().equals(((ChatSessionDto) session).getId())));
			assertTrue(conversation.state(ChatConversationControl.STATE_CAPABILITIES) instanceof ChatCapabilitiesResponse);
		} finally {
			conversation.cleanupTree();
		}

		Probe others = conversation(ben);
		try {
			others.render();

			assertEquals(List.of(), others.state(SessionListWatch.SESSIONS));
		} finally {
			others.cleanupTree();
		}
	}

	/** Every chat control answers the commands that open and delete a chat, as it does the stream ones. */
	public void testEveryChatControlAnswersTheSessionCommands() throws Exception {
		Probe conversation = conversation(anna);
		try {
			for (String command : new String[] { ChatControl.CMD_SELECT_SESSION, ChatControl.CMD_DELETE_SESSION }) {
				assertTrue(command, conversation.commandNames().contains(command));
			}
		} finally {
			conversation.cleanupTree();
		}
	}

	private static String sessionKey(String id) {
		return ChatControl.DATA_SESSION + ChatControl.DATA_KEY_SEPARATOR + id;
	}

	private static String read(BinaryData data) throws IOException {
		assertNotNull("The data must be found.", data);
		try (InputStream in = data.getStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static ReactCommandReply reply(HandlerResult result) {
		return ReactCommandReply.of(result);
	}

	/** Runs a command as the browser sends it: within an interaction of the window. */
	private HandlerResult command(ChatConversationControl conversation, String name, String sessionId) {
		try (Interaction interaction = _registry.beginInteraction()) {
			return conversation.executeClientCommand(name, Map.of(ChatControl.ARG_SESSION_ID, sessionId));
		}
	}

	/** A conversation rendered for the given account. */
	private Probe conversation(Person person) throws Exception {
		TLContextManager.getSubSession().setPerson(person);
		Probe probe = new Probe(_view, (ChatConversationElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent("<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'"
				+ " chat-id='" + _chatId + "-" + person.getName() + "'/>")));
		probe.attach();
		return probe;
	}

	private static String id() {
		return ChatSessionIds.idOf(annasChat);
	}

	/** The model, a login and the services a chat control reads. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatSessionCommands.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE));
	}
}
