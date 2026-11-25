/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.RunStateFrame;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.api.messages.RunStateEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.view.SecurityCheckedTestCase;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.agent.event.AgentEvent;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.core.session.ConversationLog;
import com.top_logic.ai.core.session.ConversationLogListener;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A window of an account that may read a chat but not write it watches the chat's run live: it is
 * streamed the answer and told that the run ended, and its {@code run_state} says the chat is
 * working - but it is not shown what the run asks its account.
 *
 * <p>
 * {@link #anna} owns the chat and sends in it; {@link #ben} holds the role {@link #READER_ROLE} on
 * it, which the test configuration grants Read only, and has the chat open in a window of his own.
 * The run is a real one, on a scripted model.
 * </p>
 */
public class TestReaderWatchesTheRun extends SecurityCheckedTestCase {

	/** The role of the test configuration that may read a conversation but not write it. */
	private static final String READER_ROLE = "TestChatReader";

	private static final String AGENT = "TestChatPlanner";

	private static final String ANSWER = "Die Antwort für alle.";

	private static final long TIMEOUT_SECONDS = 60;

	private static Person anna;

	private static Person ben;

	private static String chat;

	private Person _previousPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-runs-in-a-shared-chat");
			ben = TestPerson.createPerson("ben-watches-it");
			TLObject session = ChatHistoryService.createSession("Annas Lauf", anna);
			HistoryUtils.updateSessionRevision();
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				BoundedRole.assignRole(session, ben, BoundedRole.getRoleByName(READER_ROLE));
				tx.commit();
			}
			chat = session.tHandle().getObjectName().toExternalForm();
		}
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(anna);
	}

	@Override
	protected void tearDown() throws Exception {
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/**
	 * Anna sends: Ben's window is streamed the answer and told that the run ended, and while the run
	 * works its {@code run_state} says so, with nothing for Ben to answer.
	 */
	public void testTheReaderSeesTheAnswerLive() throws Exception {
		TaskProgressTracker annasWindow = new TaskProgressTracker();
		assertTrue(annasWindow.setActiveSessionId(chat, anna));
		TaskProgressTracker bensWindow = new TaskProgressTracker();
		assertTrue("A reader opens the chat.", bensWindow.setActiveSessionId(chat, ben));

		CountDownLatch working = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		ScriptedModel.install((request, turn) -> {
			working.countDown();
			await(release);
			return List.<ContentPart> of(new TextContent(ANSWER));
		});

		Recorder annas = new Recorder(annasWindow, anna);
		Recorder bens = new Recorder(bensWindow, ben);
		ChatEventHandlerRegistry.register(annas);
		ChatEventHandlerRegistry.register(bens);
		try {
			ChatProcessor.SendResult sent = ChatProcessor.sendMessage(annasWindow, TLContextManager.getSubSession(),
				anna.getName(), "Erkläre es.", null, new ChatProcessor.ChatRunOptions(AGENT, null, false, List.of()));
			assertNotNull("The send was refused.", sent.getTaskId());
			ChatProcessor.AIProcessingState run = annasWindow.getProcessingState(sent.getTaskId());
			try {
				assertTrue("The run did not reach the model.", working.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

				RunStateEvent state = RunStateFrame.of(bensWindow, ben);
				assertEquals(chat, state.getSessionId());
				assertTrue("The reader's window knows the chat is working.", state.isProcessing());
				assertTrue("Nothing waits for the reader.", state.getInteractions().isEmpty());
			} finally {
				release.countDown();
			}
			run._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertNull("The run failed: " + run.getError(), run.getError());
		} finally {
			ChatEventHandlerRegistry.unregister(annas);
			ChatEventHandlerRegistry.unregister(bens);
		}

		assertTrue("The reader is streamed the answer: " + bens.seen(), bens.seen().contains(Recorder.TEXT_DELTA));
		assertTrue("The reader is told that the run ended: " + bens.seen(),
			bens.seen().contains(Recorder.MESSAGE_COMPLETE));
		assertTrue(annas.seen().contains(Recorder.MESSAGE_COMPLETE));
	}

	/**
	 * What the run asks goes to the windows of the run's account alone; what it says goes to every
	 * window showing the chat.
	 */
	public void testOnlyTheRunsAccountIsAsked() throws Exception {
		TaskProgressTracker annasWindow = new TaskProgressTracker();
		assertTrue(annasWindow.setActiveSessionId(chat, anna));
		TaskProgressTracker bensWindow = new TaskProgressTracker();
		assertTrue(bensWindow.setActiveSessionId(chat, ben));

		Recorder annas = new Recorder(annasWindow, anna);
		Recorder bens = new Recorder(bensWindow, ben);
		ChatEventHandlerRegistry.register(annas);
		ChatEventHandlerRegistry.register(bens);
		try {
			ConversationLogListener log = new ConversationLogListener(new ConversationLog());
			var forwarder = ChatProcessor.streamForwarder(annasWindow,
				annasWindow.getSessionManager().getChatState(chat), anna, log);
			String requestId = UUID.randomUUID().toString();
			AgentEvent asked = new AgentEvent.ApprovalRequired("run-1", 1, Instant.now(), "call-1", requestId,
				"object-edit.updateObject", "{}", "Writes an object.", Duration.ofMinutes(5));
			forwarder.onEvent(asked);
			forwarder.onEvent(new AgentEvent.DelegatedEvent("run-1", 1, Instant.now(), "sa-1", "call-2", asked));
			forwarder.onEvent(new AgentEvent.TurnStarted("run-1", 2, Instant.now()));
		} finally {
			ChatEventHandlerRegistry.unregister(annas);
			ChatEventHandlerRegistry.unregister(bens);
		}

		assertEquals(List.of("ApprovalRequired", "DelegatedEvent", "TurnStarted"), annas.seen());
		assertEquals("The reader is not asked.", List.of("TurnStarted"), bens.seen());
	}

	/**
	 * The reader's window is not registered to answer what a subagent of the chat asks, and wakes no
	 * lead turn: it only watches.
	 */
	public void testTheReadersWindowAnswersNoSubagent() {
		TaskProgressTracker annasWindow = new TaskProgressTracker();
		assertTrue(annasWindow.setActiveSessionId(chat, anna));
		TaskProgressTracker bensWindow = new TaskProgressTracker();
		assertTrue(bensWindow.setActiveSessionId(chat, ben));

		assertNotNull(annasWindow.getActiveSubagentRegistry());
		assertNotNull("The owner's window wakes the lead turn.", annasWindow.getSessionManager().getSubagentWaker(chat));
		assertNotNull(bensWindow.getActiveSubagentRegistry());
		assertNull("A reader's window wakes no lead turn.", bensWindow.getSessionManager().getSubagentWaker(chat));
	}

	private static void await(CountDownLatch latch) {
		try {
			assertTrue("The test did not release the model.", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new AssertionError(ex);
		}
	}

	/** An open event stream of a window, which writes down what reached it. */
	private static final class Recorder implements ChatEventHandler {

		static final String TEXT_DELTA = "text_delta";

		static final String MESSAGE_COMPLETE = "message_complete";

		private final TaskProgressTracker _window;

		private final Person _account;

		private final List<String> _seen = Collections.synchronizedList(new ArrayList<>());

		Recorder(TaskProgressTracker window, Person account) {
			_window = window;
			_account = account;
		}

		List<String> seen() {
			synchronized (_seen) {
				return new ArrayList<>(_seen);
			}
		}

		@Override
		public TaskProgressTracker getTracker() {
			return _window;
		}

		@Override
		public Person getAccount() {
			return _account;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public void onEvent(AgentEvent event) {
			_seen.add(event.getClass().getSimpleName());
		}

		@Override
		public void onTextDelta(AgentEvent.TextDelta delta, long end) {
			_seen.add(TEXT_DELTA);
		}

		@Override
		public void sendMessageComplete(MessageCompleteEvent event) {
			_seen.add(MESSAGE_COMPLETE);
		}

		@Override
		public void sendProviderError(ProviderErrorEvent event) {
			_seen.add("provider_error");
		}
	}

	/** The suite, on the services a chat run passes through, and the model-based security. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestReaderWatchesTheRun.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}
