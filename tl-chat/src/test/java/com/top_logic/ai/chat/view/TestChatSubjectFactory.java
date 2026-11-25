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
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.messages.ChatSendReply;
import com.top_logic.ai.chat.api.messages.ConversationFeaturesDto;
import com.top_logic.ai.chat.api.messages.DraftAttachmentDto;
import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ScriptedBackend;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.ChatCommandDriver.Conversation;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.agent.event.AgentEvent;
import com.top_logic.ai.core.agent.event.AgentEventListener;
import com.top_logic.ai.core.cost.Money;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.message.TokenUsage;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * The first message of a {@code <chat-conversation>} with a {@code subject-factory} creates the
 * subject of the conversation it creates ({@link ChatConversationElement.Config#getSubjectFactory()}).
 *
 * <p>
 * The commands are run as the browser sends them ({@link ChatCommandDriver}). Conversations about a
 * subject of {@link #SUBJECT_TYPE} are answered by the test backend {@link ScriptedBackend}.
 * </p>
 */
public class TestChatSubjectFactory extends SecurityCheckedTestCase {

	private static final String SUBJECT = "chatSubject";

	/** The type the factories of this test create; one the module's model has. */
	private static final String SUBJECT_TYPE = "tl.ai.execution:UserMemory";

	/** A type of the module's model other than {@link #SUBJECT_TYPE}. */
	private static final String OTHER_TYPE = "tl.ai.execution:ArchivedPage";

	/** A factory creating a subject that holds the message, with every mandatory value. */
	private static final String CREATE = "text -> attachments -> {"
		+ " subject = new(`" + SUBJECT_TYPE + "`);"
		+ " $subject.set(`" + SUBJECT_TYPE + "#text`, $text);"
		+ " $subject.set(`" + SUBJECT_TYPE + "#owner`, currentUser());"
		+ " $subject.set(`" + SUBJECT_TYPE + "#kind`, `tl.ai.execution:MemoryKind#FACT`);"
		+ " $subject.set(`" + SUBJECT_TYPE + "#createdAt`, now());"
		+ " $subject; }";

	/** The text a factory refuses a message with. */
	private static final String REFUSAL = "No subject for this message.";

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G' };

	private static final String TL_PROMPT_IMPROVER = "TL_PromptImprover";

	private static final long TIMEOUT_MILLIS = 60_000;

	private static final BackendFeatures WITHOUT_ATTACHMENTS =
		new BackendFeatures(true, true, false, false, false, false, false, false);

	/** An account the model-based security grants nothing, in particular not to create a subject. */
	private static Person anna;

	/** The subjects the resolver was asked about, in the order it was asked. */
	private final List<TLObject> _asked = new CopyOnWriteArrayList<>();

	/** The messages the backend received, in the order they arrived. */
	private final List<BackendTurn> _turns = new CopyOnWriteArrayList<>();

	/** Whether the backend refuses files; see {@link #testFailureAfterTheFactoryKeepsTheChat()}. */
	private final AtomicBoolean _backendRefusesFiles = new AtomicBoolean();

	private ChatCommandDriver _driver;

	private ViewChannel _subject;

	private Conversation _conversation;

	private TaskProgressTracker _window;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-subject-factory");
		}
		_driver = new ChatCommandDriver("subjectFactoryWindow", "subject-factory-" + getName());
		_subject = new DefaultViewChannel(SUBJECT);
		_driver.registerChannel(SUBJECT, _subject);
		answerSubjectsOf(BackendFeatures.INTERNAL);
	}

	@Override
	protected void tearDown() throws Exception {
		if (_conversation != null) {
			awaitIdle();
			_conversation.cleanupTree();
		}
		ScriptedBackend.reset();
		_driver.close();
		super.tearDown();
	}

	/**
	 * The first message creates the subject: the conversation is about it, its backend is asked
	 * about it, and the subject channel shows it.
	 */
	public void testFirstMessageCreatesTheSubject() throws Exception {
		open(CREATE);

		Map<String, Object> reply = send(null, "Plan the trip");

		assertEquals(reply.toString(), ChatControl.STATUS_PROCESSING, reply.get(ChatSendReply.STATUS__PROP));
		TLObject created = (TLObject) _subject.get();
		assertNotNull("The channel shows the created subject.", created);
		assertTrue(TLModelUtil.isCompatibleInstance(type(SUBJECT_TYPE), created));
		assertEquals("Plan the trip", created.tValueByName("text"));
		assertEquals(created, session(reply).getSubject());
		assertTrue("The resolver decides from the created subject: " + _asked, _asked.contains(created));
	}

	/**
	 * The factory runs as the signed-in account: an account that may not create the subject is
	 * refused with the reason of the security, and nothing is created.
	 */
	public void testFactoryRunsAsTheSignedInAccount() throws Exception {
		open(anna, CREATE);
		long before = lastRevision();

		Map<String, Object> reply = send(null, "Plan the trip");

		assertEquals(reply.toString(), ChatControl.STATUS_FORBIDDEN, reply.get(ChatSendReply.STATUS__PROP));
		assertNull("No chat: " + reply, reply.get(ChatSendReply.SESSION_ID__PROP));
		assertEquals("Nothing was written.", before, lastRevision());
		assertNull(_subject.get());
	}

	/** A factory that refuses the message: refused with its text, nothing created. */
	public void testFactoryRefusesTheMessage() throws Exception {
		open("text -> attachments -> { new(`" + SUBJECT_TYPE + "`); throw(\"" + REFUSAL + "\"); }");
		long before = lastRevision();

		Map<String, Object> reply = send(null, "Plan the trip");

		assertEquals(reply.toString(), ChatControl.STATUS_FORBIDDEN, reply.get(ChatSendReply.STATUS__PROP));
		assertEquals(REFUSAL, reply.get(ChatSendReply.REASON__PROP));
		assertNull("No chat: " + reply, reply.get(ChatSendReply.SESSION_ID__PROP));
		assertEquals("Nothing was written, not even the object the factory created.", before, lastRevision());
		assertNull(_subject.get());
	}

	/** A factory that creates an object of another type: refused, the object rolled back. */
	public void testFactoryCreatesAnObjectOfAnotherType() throws Exception {
		open("text -> attachments -> new(`" + OTHER_TYPE + "`)");
		long before = lastRevision();

		Map<String, Object> reply = send(null, "Plan the trip");

		assertEquals(reply.toString(), ChatControl.STATUS_FORBIDDEN, reply.get(ChatSendReply.STATUS__PROP));
		assertEquals(Resources.getInstance().getString(I18NConstants.ERROR_SUBJECT_NOT_OF_TYPE__TYPE.fill(SUBJECT_TYPE)),
			reply.get(ChatSendReply.REASON__PROP));
		assertNull("No chat: " + reply, reply.get(ChatSendReply.SESSION_ID__PROP));
		assertEquals("Nothing was written.", before, lastRevision());
	}

	/** A first message the agent policy refuses: the factory is not called, nothing is created. */
	public void testRefusedByThePolicyBeforeTheFactory() throws Exception {
		// No resolver answers the subject type, so the conversation runs an agent of this application.
		ScriptedBackend.reset();
		open(CREATE);
		long before = lastRevision();

		Map<String, Object> reply = _driver.command(_conversation, ChatControl.CMD_SEND,
			Map.of(ChatControl.ARG_TEXT, "Plan the trip", ChatControl.ARG_AGENT, TL_PROMPT_IMPROVER));

		assertEquals(reply.toString(), ChatControl.STATUS_FORBIDDEN, reply.get(ChatSendReply.STATUS__PROP));
		assertEquals("The factory was not called.", before, lastRevision());
		assertNull(_subject.get());
	}

	/**
	 * Files for a subject type whose backend takes none: refused before the factory, nothing is
	 * created.
	 */
	public void testFilesRefusedByTheFeaturesOfTheSubjectType() throws Exception {
		answerSubjectsOf(WITHOUT_ATTACHMENTS);
		open(CREATE);
		assertFalse("The empty chat offers what a conversation about the subject type offers.",
			((ConversationFeaturesDto) _conversation.state(ChatConversationControl.STATE_SUBJECT_FEATURES))
				.isAttachments());
		_driver.upload(_conversation, "plan.png", "image/png", PNG);
		long before = lastRevision();

		Map<String, Object> reply = send(null, "Plan the trip");

		assertEquals(reply.toString(), ChatControl.STATUS_FORBIDDEN, reply.get(ChatSendReply.STATUS__PROP));
		assertEquals("The factory was not called.", before, lastRevision());
		assertEquals("The files stay on the message.", List.of("plan.png"), shownNames(ChatViewBinding.NEW_CONVERSATION));
	}

	/** A chat that is shown and never sent into creates nothing. */
	public void testNoSendCreatesNothing() throws Exception {
		long before = lastRevision();
		open(CREATE);
		_conversation.render();

		assertEquals(before, lastRevision());
		assertNull(_subject.get());
	}

	/** An object in the subject channel is the subject; the factory is not called. */
	public void testSubjectInTheChannelWins() throws Exception {
		TLObject given =
			ChatHistoryService.createSession("Given subject " + getName(), PersonManager.getManager().getRoot());
		HistoryUtils.updateSessionRevision();
		_subject.set(given);
		open("text -> attachments -> new(`" + OTHER_TYPE + "`)");

		Map<String, Object> reply = send(null, "Plan the trip");

		assertEquals(reply.toString(), ChatControl.STATUS_PROCESSING, reply.get(ChatSendReply.STATUS__PROP));
		assertEquals(given, session(reply).getSubject());
		assertEquals(given, _subject.get());
	}

	/**
	 * A send that fails after the factory keeps the chat with its subject and its message's files;
	 * a send again goes into that chat and carries them.
	 */
	public void testFailureAfterTheFactoryKeepsTheChat() throws Exception {
		_backendRefusesFiles.set(true);
		open(CREATE);
		_driver.upload(_conversation, "plan.png", "image/png", PNG);

		Map<String, Object> reply = send(null, "Plan the trip");

		assertEquals(reply.toString(), ChatControl.STATUS_FORBIDDEN, reply.get(ChatSendReply.STATUS__PROP));
		String kept = (String) reply.get(ChatSendReply.SESSION_ID__PROP);
		assertNotNull("The reply names the kept chat: " + reply, kept);
		HistoryUtils.updateSessionRevision();
		TLObject subject = (TLObject) _subject.get();
		assertNotNull("The channel shows the created subject.", subject);
		assertEquals(subject, ((AgentSession) ChatHistoryService.findAnySessionById(kept)).getSubject());
		assertEquals(kept, _window.getActiveSessionId());
		assertEquals("The files moved to the kept chat.", List.of("plan.png"), shownNames(kept));
		assertEquals("The new conversation holds no files.", List.of(),
			shownNames(ChatViewBinding.NEW_CONVERSATION));

		_backendRefusesFiles.set(false);
		Map<String, Object> again = send(kept, "Plan the trip");

		assertEquals(again.toString(), ChatControl.STATUS_PROCESSING, again.get(ChatSendReply.STATUS__PROP));
		assertEquals(kept, again.get(ChatSendReply.SESSION_ID__PROP));
		awaitIdle();
		assertEquals(1, _turns.size());
		assertEquals(kept, _turns.get(0).sessionId());
		assertEquals(List.of("plan.png"),
			_turns.get(0).attachments().stream().map(file -> file.getFileName()).toList());
		assertEquals("The subject is the one created first.", subject,
			((AgentSession) ChatHistoryService.findAnySessionById(kept)).getSubject());
	}

	/**
	 * A conversation of the given factory, for an administrator, whom the model-based security lets
	 * create the subjects.
	 */
	private void open(String factory) throws Exception {
		open(PersonManager.getManager().getRoot(), factory);
	}

	private void open(Person person, String factory) throws Exception {
		_conversation = _driver.conversation(person, "subject='" + SUBJECT + "' subject-type='" + SUBJECT_TYPE
			+ "' subject-factory='" + factory.replace("'", "&apos;") + "'", "");
		_window = ChatCommandDriver.tracker(_conversation);
	}

	private Map<String, Object> send(String sessionId, String text) {
		return _driver.command(_conversation, ChatControl.CMD_SEND, sessionId == null
			? Map.of(ChatControl.ARG_TEXT, text)
			: Map.of(ChatControl.ARG_TEXT, text, ChatControl.ARG_SESSION_ID, sessionId));
	}

	private static AgentSession session(Map<String, Object> reply) {
		String id = (String) reply.get(ChatSendReply.SESSION_ID__PROP);
		assertNotNull(reply.toString(), id);
		HistoryUtils.updateSessionRevision();
		return (AgentSession) ChatHistoryService.findAnySessionById(id);
	}

	/** The names of the files the conversation's state shows for the given chat. */
	private List<String> shownNames(String sessionId) {
		_conversation.render();
		return TestChatDraftCommands.shown(_conversation, sessionId).stream()
			.map(file -> ((DraftAttachmentDto) file).getFileName())
			.toList();
	}

	private static long lastRevision() {
		return PersistencyLayer.getKnowledgeBase().getHistoryManager().getLastRevision();
	}

	private static TLStructuredType type(String name) {
		return (TLStructuredType) TLModelUtil.findType(name);
	}

	/**
	 * Lets the test backend answer every conversation about a subject of {@link #SUBJECT_TYPE},
	 * naming the given features for the type.
	 */
	private void answerSubjectsOf(BackendFeatures typeFeatures) {
		TLStructuredType subjectType = type(SUBJECT_TYPE);
		ConversationBackend backend = new ConversationBackend() {
			@Override
			public String agentName() {
				return "test-backend";
			}

			@Override
			public BackendFeatures features() {
				return _backendRefusesFiles.get() ? WITHOUT_ATTACHMENTS : typeFeatures;
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
		ScriptedBackend.install(subject -> {
			_asked.add(subject);
			return TLModelUtil.isCompatibleInstance(subjectType, subject) ? backend : null;
		}, type -> type.equals(subjectType) ? typeFeatures : null);
	}

	/** Waits until no run of the window is in flight, so that none outlives its test. */
	private void awaitIdle() throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (_window != null && _window.hasActiveProcessingState()) {
			assertTrue("The run did not end.", System.currentTimeMillis() < deadline);
			Thread.sleep(20);
		}
	}

	/** The suite, on the services a chat request and a backend's run pass through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatSubjectFactory.class),
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
