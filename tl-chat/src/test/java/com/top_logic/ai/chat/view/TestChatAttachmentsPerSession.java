/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.messages.AttachmentUploadReply;
import com.top_logic.ai.chat.api.messages.ChatSendReply;
import com.top_logic.ai.chat.api.messages.DraftAttachmentDto;
import com.top_logic.ai.chat.api.messages.SessionDeleteReply;
import com.top_logic.ai.chat.api.messages.SessionOpenReply;
import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ScriptedBackend;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.agent.event.AgentEvent;
import com.top_logic.ai.core.agent.event.AgentEventListener;
import com.top_logic.ai.core.cost.Money;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.message.TokenUsage;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The files attached to the message being written belong to the chat session they were attached in,
 * not to the place that shows the chat ({@link ChatViewBinding#attachments(String)}).
 *
 * <p>
 * The commands are run as the browser sends them ({@link ChatCommandDriver}). Every chat is
 * answered by the test backend {@link ScriptedBackend}, which records the files each message
 * carried.
 * </p>
 */
public class TestChatAttachmentsPerSession extends SecurityCheckedTestCase {

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G' };

	private static final long TIMEOUT_MILLIS = 60_000;

	private static Person anna;

	private static Person ben;

	/** The messages the backend received, in the order they arrived. */
	private final List<BackendTurn> _turns = new CopyOnWriteArrayList<>();

	private ChatCommandDriver _driver;

	private ChatCommandDriver.Conversation _conversation;

	private TaskProgressTracker _window;

	private String _chatA;

	private String _chatB;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-attaching-per-chat");
			ben = TestPerson.createPerson("ben-reading-annas-chat");
		}
		_chatA = idOf(ChatHistoryService.createSession("A " + getName(), anna));
		_chatB = idOf(ChatHistoryService.createSession("B " + getName(), anna));
		HistoryUtils.updateSessionRevision();
		_driver = new ChatCommandDriver("attachmentsWindow", "attachments-" + getName());
		_conversation = _driver.conversation(anna);
		_window = ChatCommandDriver.tracker(_conversation);
	}

	@Override
	protected void tearDown() throws Exception {
		awaitIdle();
		ScriptedBackend.reset();
		_conversation.cleanupTree();
		_driver.close();
		super.tearDown();
	}

	/**
	 * A file attached in chat A is not shown in chat B, does not go with a message sent in B, and is
	 * there again when the place shows A again.
	 */
	public void testAFileStaysWithItsChat() throws Exception {
		answerWith(BackendFeatures.INTERNAL);
		select(_chatA);
		assertNoRefusal(_driver.upload(_conversation, _chatA, "a.png", "image/png", PNG));
		assertEquals(List.of("a.png"), shownNames(_chatA));

		select(_chatB);
		assertEquals("Chat B has no file.", List.of(), shownNames(_chatB));
		assertEquals("The new conversation has no file.", List.of(), shownNames(ChatViewBinding.NEW_CONVERSATION));

		Map<String, Object> reply = send(_chatB, "Ohne Anhang");
		assertEquals(reply.toString(), ChatControl.STATUS_PROCESSING, reply.get(ChatSendReply.STATUS__PROP));
		awaitIdle();
		assertEquals(1, _turns.size());
		assertEquals("The message in B carries none of A's files.", List.of(), _turns.get(0).attachments());

		select(_chatA);
		assertEquals("Back in A, its file is there.", List.of("a.png"), shownNames(_chatA));
		assertEquals(1, ChatCommandDriver.attachments(_conversation, _chatA).entries().size());
	}

	/** A file is taken off only in the chat the command names. */
	public void testAFileIsTakenOffInItsChat() throws Exception {
		_driver.upload(_conversation, _chatA, "a.png", "image/png", PNG);
		String file = ChatCommandDriver.attachments(_conversation, _chatA).entries().get(0).id();

		_driver.command(_conversation, ChatControl.CMD_REMOVE_ATTACHMENT,
			Map.of(ChatControl.ARG_ATTACHMENT_ID, file, ChatControl.ARG_SESSION_ID, _chatB));
		assertEquals("Naming another chat takes nothing off.", List.of("a.png"), shownNames(_chatA));

		_driver.command(_conversation, ChatControl.CMD_REMOVE_ATTACHMENT,
			Map.of(ChatControl.ARG_ATTACHMENT_ID, file, ChatControl.ARG_SESSION_ID, _chatA));
		assertEquals(List.of(), shownNames(_chatA));
	}

	/** The files of the conversation not yet created go with its first message. */
	public void testTheFirstSendTakesTheNewConversationsFiles() throws Exception {
		answerWith(BackendFeatures.INTERNAL);
		_driver.upload(_conversation, _chatA, "a.png", "image/png", PNG);
		assertNoRefusal(_driver.upload(_conversation, "neu.png", "image/png", PNG));
		assertEquals(List.of("neu.png"), shownNames(ChatViewBinding.NEW_CONVERSATION));

		Map<String, Object> reply = send(null, "Sieh dir das an");

		assertEquals(reply.toString(), ChatControl.STATUS_PROCESSING, reply.get(ChatSendReply.STATUS__PROP));
		awaitIdle();
		assertEquals(1, _turns.size());
		assertEquals(List.of("neu.png"),
			_turns.get(0).attachments().stream().map(file -> file.getFileName()).toList());
		assertEquals("The files went with the message.", List.of(), shownNames(ChatViewBinding.NEW_CONVERSATION));
		assertEquals("The created chat starts without files.", List.of(),
			shownNames((String) reply.get(ChatSendReply.SESSION_ID__PROP)));
		assertEquals("A's file stays in A.", List.of("a.png"), shownNames(_chatA));
	}

	/**
	 * A first message that is refused deletes the chat it created; its files stay with the
	 * conversation not yet created.
	 */
	public void testARefusedFirstSendKeepsTheFiles() throws Exception {
		answerWith(BackendFeatures.NONE);
		_driver.upload(_conversation, "neu.png", "image/png", PNG);

		Map<String, Object> reply = send(null, "Sieh dir das an");

		assertEquals(reply.toString(), ChatControl.STATUS_FORBIDDEN, reply.get(ChatSendReply.STATUS__PROP));
		assertEquals(List.of(), _turns);
		assertEquals("The file waits for the next try.", List.of("neu.png"),
			shownNames(ChatViewBinding.NEW_CONVERSATION));
	}

	/**
	 * The files of a deleted chat are dropped; those of other chats stay.
	 *
	 * <p>
	 * The driver's window is none of the live windows {@link ChatWindowService#onSessionDeleted}
	 * walks - those are the windows of HTTP sessions -, so the test does for it what the service
	 * does for each of them.
	 * </p>
	 */
	public void testTheFilesOfADeletedChatAreDropped() throws Exception {
		_driver.upload(_conversation, _chatA, "a.png", "image/png", PNG);
		_driver.upload(_conversation, _chatB, "b.png", "image/png", PNG);

		Map<String, Object> deleted =
			_driver.command(_conversation, ChatControl.CMD_DELETE_SESSION, Map.of(ChatControl.ARG_SESSION_ID, _chatA));
		assertEquals(deleted.toString(), Boolean.TRUE, deleted.get(SessionDeleteReply.DELETED__PROP));
		ChatViewBinding.dropAttachments(TLContextManager.getSubSession(), _chatA);

		Map<String, List<DraftAttachmentDto>> left = _conversation.binding().attachmentLists();
		assertFalse("The deleted chat's files are gone: " + left, left.containsKey(_chatA));
		assertEquals(1, left.get(_chatB).size());
	}

	/**
	 * An account that may not write a chat attaches nothing in it, while it may attach in a
	 * conversation not yet created.
	 */
	public void testAnAccountThatMayNotWriteTheChatAttachesNothing() throws Exception {
		ChatCommandDriver.Conversation foreign = _driver.conversation(ben);
		try {
			assertEquals("The upload is refused.", Map.of(),
				_driver.upload(foreign, _chatA, "fremd.png", "image/png", PNG));
			assertEquals(List.of(), ChatCommandDriver.attachments(foreign, _chatA).entries());

			assertNoRefusal(_driver.upload(foreign, "eigen.png", "image/png", PNG));
			assertEquals(1, ChatCommandDriver.attachments(foreign, null).entries().size());
		} finally {
			foreign.cleanupTree();
			_driver.actAs(anna);
		}
	}

	private void select(String sessionId) {
		Map<String, Object> reply =
			_driver.command(_conversation, ChatControl.CMD_SELECT_SESSION, Map.of(ChatControl.ARG_SESSION_ID, sessionId));
		assertEquals(reply.toString(), Boolean.TRUE, reply.get(SessionOpenReply.OPENED__PROP));
	}

	private Map<String, Object> send(String sessionId, String text) {
		return _driver.command(_conversation, ChatControl.CMD_SEND, sessionId == null
			? Map.of(ChatControl.ARG_TEXT, text)
			: Map.of(ChatControl.ARG_TEXT, text, ChatControl.ARG_SESSION_ID, sessionId));
	}

	/** The names of the files the conversation's state shows for the given chat. */
	private List<String> shownNames(String sessionId) {
		_conversation.render();
		return TestChatDraftCommands.shown(_conversation, sessionId).stream()
			.map(file -> ((DraftAttachmentDto) file).getFileName())
			.toList();
	}

	private static void assertNoRefusal(Map<String, Object> reply) {
		assertEquals("Nothing was refused: " + reply, List.of(), reply.get(AttachmentUploadReply.REASON__PROP));
	}

	/** Lets every chat be answered by a backend with the given features, recording each message. */
	private void answerWith(BackendFeatures features) {
		ConversationBackend backend = new ConversationBackend() {
			@Override
			public String agentName() {
				return "test-backend";
			}

			@Override
			public BackendFeatures features() {
				return features;
			}

			@Override
			public String send(BackendTurn turn, AgentEventListener sink, CancellationToken cancel) {
				_turns.add(turn);
				String runId = "backend-" + System.nanoTime();
				TokenUsage usage = new TokenUsage(10, 5, 0, 0, 0);
				sink.onEvent(new AgentEvent.RunStarted(runId, 0, Instant.now(), null));
				sink.onEvent(new AgentEvent.TurnStarted(runId, 1, Instant.now()));
				sink.onEvent(new AgentEvent.TurnCompleted(runId, 1, Instant.now(), usage, List.of()));
				sink.onEvent(new AgentEvent.RunCompleted(runId, 1, Instant.now(), StopReason.COMPLETE, usage,
					new Money(0.001, Money.USD)));
				return "";
			}
		};
		ScriptedBackend.install(session -> backend);
	}

	/** Waits until no run of the window is in flight, so that none outlives its test. */
	private void awaitIdle() throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (_window.hasActiveProcessingState()) {
			assertTrue("The run did not end.", System.currentTimeMillis() < deadline);
			Thread.sleep(20);
		}
	}

	private static String idOf(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	/** The suite, on the services a chat request and a backend's run pass through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatAttachmentsPerSession.class),
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
				Login.Module.INSTANCE,
				ConversationBackendService.Module.INSTANCE,
				ChatWindowService.Module.INSTANCE));
	}
}
