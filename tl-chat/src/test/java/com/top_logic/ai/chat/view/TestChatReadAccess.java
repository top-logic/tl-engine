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
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.messages.ChatSendReply;
import com.top_logic.ai.chat.api.messages.ChatStatusReply;
import com.top_logic.ai.chat.api.messages.SessionDeleteReply;
import com.top_logic.ai.chat.api.messages.SessionOpenReply;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.approval.ApprovalMode;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * That the chat decides the access to a conversation by the rights of the model-based security, not
 * by its owner, and that the {@code writable} function of a {@code <chat-conversation>} narrows the
 * right to write.
 *
 * <p>
 * Three accounts: {@link #anna} owns the chat (the shipped policy gives her Read and Write),
 * {@link #ben} holds the role {@link #READER_ROLE} on it, which the test configuration grants Read
 * only, and {@link #carl} holds nothing. The commands are run as the browser sends them
 * ({@link ChatCommandDriver}), with the security checks on.
 * </p>
 */
public class TestChatReadAccess extends BasicTestCase {

	/** The role of the test configuration that may read a conversation but not write it. */
	private static final String READER_ROLE = "TestChatReader";

	private static final String WINDOW = "readAccessWindow";

	/** The accounts and the chat, made once: no test here changes the chat but in its metadata. */
	private static Person anna;

	private static Person ben;

	private static Person carl;

	private static TLObject annasChat;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-shares-a-chat");
			ben = TestPerson.createPerson("ben-reads-it");
			carl = TestPerson.createPerson("carl-may-not");
			annasChat = ChatHistoryService.createSession("Annas geteilter Chat", anna);
			HistoryUtils.updateSessionRevision();
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				BoundedRole.assignRole(annasChat, ben, BoundedRole.getRoleByName(READER_ROLE));
				tx.commit();
			}
		}
		ChatSessionRegistry.register(TLContextManager.getSubSession(), null, new TaskProgressTracker());
	}

	/** A reader opens the chat and reads its history, and the conversation shows it read-only. */
	public void testAReaderOpensTheChatReadOnly() throws Exception {
		ChatCommandDriver driver = driver();
		ChatCommandDriver.Conversation conversation = driver.conversation(ben);
		try {
			assertEquals(Boolean.TRUE, open(driver, conversation).get(SessionOpenReply.OPENED__PROP));
			assertEquals(id(), ChatCommandDriver.tracker(conversation).getActiveSessionId());
			assertNotNull("The history is delivered over the data route.",
				driver.data(conversation, sessionKey()));

			assertEquals(Boolean.TRUE, conversation.state(ChatConversationControl.STATE_READ_ONLY));
			conversation.render();
			assertEquals("A render writes it again.", Boolean.TRUE,
				conversation.state(ChatConversationControl.STATE_READ_ONLY));
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
	}

	/** Every command that changes the chat is refused for the reader, and the chat stays as it was. */
	public void testAReaderChangesNothing() throws Exception {
		ChatCommandDriver driver = driver();
		ChatCommandDriver.Conversation conversation = driver.conversation(ben);
		try {
			open(driver, conversation);

			assertEquals(ChatControl.STATUS_FORBIDDEN, status(driver.command(conversation, ChatControl.CMD_SEND,
				Map.of(ChatControl.ARG_TEXT, "Darf ich?", ChatControl.ARG_SESSION_ID, id()))));
			assertEquals(ChatControl.STATUS_FORBIDDEN, status(driver.command(conversation,
				ChatControl.CMD_EDIT_SEND, Map.of(ChatControl.ARG_SESSION_ID, id(),
					ChatControl.ARG_FROM_TURN, Integer.valueOf(0), ChatControl.ARG_TEXT, "Anders"))));
			assertEquals(ChatControl.STATUS_FORBIDDEN, status(driver.command(conversation,
				ChatControl.CMD_REGENERATE, Map.of(ChatControl.ARG_SESSION_ID, id()))));
			assertEquals(ChatControl.STATUS_FORBIDDEN, status(driver.command(conversation, ChatControl.CMD_RATE,
				Map.of(ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_TURN_INDEX, Integer.valueOf(0),
					ChatControl.ARG_RATING, "up"))));
			assertEquals(ChatControl.STATUS_FORBIDDEN, status(driver.command(conversation,
				ChatControl.CMD_SET_APPROVAL_MODE, Map.of(ChatControl.ARG_SESSION_ID, id(),
					ChatControl.ARG_MODE, ApprovalMode.AUTO_APPROVE.name()))));
			assertFalse("A reader answers no question of the chat's runs.",
				ChatControl.STATUS_OK.equals(status(driver.command(conversation, ChatControl.CMD_RESPOND,
					Map.of(ChatControl.ARG_REQUEST_ID, "no-such-request", ChatControl.ARG_RESPONSE, "APPROVED")))));
			assertEquals(Boolean.FALSE, driver.command(conversation, ChatControl.CMD_DELETE_SESSION,
				Map.of(ChatControl.ARG_SESSION_ID, id())).get(SessionDeleteReply.DELETED__PROP));
			HistoryUtils.updateSessionRevision();

			assertNotNull("The chat must still be there.", ChatHistoryService.findAnySessionById(id()));
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
	}

	/**
	 * A reader's window stores nothing on the chat: neither that it was seen when it is opened, nor
	 * the window's copy of its metadata when the window moves on to another chat.
	 */
	public void testAReadersWindowStoresNothingOnTheChat() throws Exception {
		TLObject bensOwn = ChatHistoryService.createSession("Bens eigener Chat", ben);
		HistoryUtils.updateSessionRevision();
		String before = ChatHistoryService.getMetadataJson(annasChat);
		ChatCommandDriver driver = driver();
		ChatCommandDriver.Conversation conversation = driver.conversation(ben);
		try {
			open(driver, conversation);
			assertEquals(Boolean.TRUE, driver.command(conversation, ChatControl.CMD_SELECT_SESSION,
				Map.of(ChatControl.ARG_SESSION_ID, ChatSessionIds.idOf(bensOwn))).get(SessionOpenReply.OPENED__PROP));
			HistoryUtils.updateSessionRevision();

			assertEquals(before, ChatHistoryService.getMetadataJson(annasChat));
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
	}

	/**
	 * An account without any right on the chat cannot open it, read it or change it, and is answered
	 * exactly as for a chat that does not exist.
	 */
	public void testAnAccountWithoutRightsFindsNothing() throws Exception {
		ChatCommandDriver driver = driver();
		ChatCommandDriver.Conversation conversation = driver.conversation(carl);
		try {
			assertEquals(Boolean.FALSE, open(driver, conversation).get(SessionOpenReply.OPENED__PROP));
			assertNull(ChatCommandDriver.tracker(conversation).getActiveSessionId());
			assertNull(driver.data(conversation, sessionKey()));

			assertEquals(ChatControl.STATUS_NOT_FOUND, status(driver.command(conversation, ChatControl.CMD_SEND,
				Map.of(ChatControl.ARG_TEXT, "Hallo?", ChatControl.ARG_SESSION_ID, id()))));
			assertEquals(ChatControl.STATUS_NOT_FOUND, status(driver.command(conversation, ChatControl.CMD_RATE,
				Map.of(ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_TURN_INDEX, Integer.valueOf(0),
					ChatControl.ARG_RATING, "up"))));
			assertEquals(Boolean.FALSE, driver.command(conversation, ChatControl.CMD_DELETE_SESSION,
				Map.of(ChatControl.ARG_SESSION_ID, id())).get(SessionDeleteReply.DELETED__PROP));

			driver.command(conversation, ChatConversationControl.CMD_PUBLISH_SESSION,
				Map.of(ChatConversationControl.ARG_PUBLISHED_ID, id()));
			assertEquals("A chat the account may not read is not selected.", "",
				conversation.state(SessionSelection.SELECTED));
			assertEquals(Boolean.FALSE, conversation.state(ChatConversationControl.STATE_READ_ONLY));
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
	}

	/** The owner writes the chat, and the conversation offers it for writing. */
	public void testTheOwnerWrites() throws Exception {
		ChatCommandDriver driver = driver();
		ChatCommandDriver.Conversation conversation = driver.conversation(anna);
		try {
			open(driver, conversation);

			assertEquals(Boolean.FALSE, conversation.state(ChatConversationControl.STATE_READ_ONLY));
			assertEquals(ChatControl.STATUS_OK, status(driver.command(conversation, ChatControl.CMD_SET_APPROVAL_MODE,
				Map.of(ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_MODE, ApprovalMode.MANUAL.name()))));
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
	}

	/**
	 * A {@code writable} function that refuses the chat makes it read-only for its owner too, while a
	 * new conversation, which has no chat yet, can still be started.
	 */
	public void testWritableNarrowsTheOwnersWrite() throws Exception {
		ChatCommandDriver driver = driver();
		ChatCommandDriver.Conversation conversation =
			driver.conversation(anna, ChatConversationElement.Config.WRITABLE + "='s -> false'", "");
		try {
			assertEquals("A new conversation is never read-only.", Boolean.FALSE,
				conversation.state(ChatConversationControl.STATE_READ_ONLY));
			Map<String, Object> first = driver.command(conversation, ChatControl.CMD_SEND,
				Map.of(ChatControl.ARG_TEXT, "Ein neuer Anfang", ChatControl.ARG_TITLE, "Neu " + getName()));
			assertEquals(first.toString(), ChatControl.STATUS_PROCESSING, status(first));

			open(driver, conversation);
			assertEquals(Boolean.TRUE, conversation.state(ChatConversationControl.STATE_READ_ONLY));
			assertEquals(ChatControl.STATUS_FORBIDDEN, status(driver.command(conversation, ChatControl.CMD_SEND,
				Map.of(ChatControl.ARG_TEXT, "Noch etwas", ChatControl.ARG_SESSION_ID, id()))));
			assertEquals(ChatControl.STATUS_FORBIDDEN, status(driver.command(conversation, ChatControl.CMD_RATE,
				Map.of(ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_TURN_INDEX, Integer.valueOf(0),
					ChatControl.ARG_RATING, "up"))));
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
	}

	/**
	 * Opens {@link #annasChat} in the conversation as the browser does: the pick, then the report of
	 * what it shows.
	 */
	private static Map<String, Object> open(ChatCommandDriver driver, ChatCommandDriver.Conversation conversation) {
		Map<String, Object> reply = driver.command(conversation, ChatControl.CMD_SELECT_SESSION,
			Map.of(ChatControl.ARG_SESSION_ID, id()));
		if (Boolean.TRUE.equals(reply.get(SessionOpenReply.OPENED__PROP))) {
			driver.command(conversation, ChatConversationControl.CMD_PUBLISH_SESSION,
				Map.of(ChatConversationControl.ARG_PUBLISHED_ID, id()));
		}
		return reply;
	}

	private ChatCommandDriver driver() {
		return new ChatCommandDriver(WINDOW, "read-access-" + getName());
	}

	private static String status(Map<String, Object> reply) {
		Object status = reply.get(ChatSendReply.STATUS__PROP);
		return status == null ? (String) reply.get(ChatStatusReply.STATUS__PROP) : (String) status;
	}

	private static String sessionKey() {
		return ChatControl.DATA_SESSION + ChatControl.DATA_KEY_SEPARATOR + id();
	}

	private static String id() {
		return ChatSessionIds.idOf(annasChat);
	}

	/** The model, the model-based security, a login, the agents and the services a chat control reads. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatReadAccess.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
