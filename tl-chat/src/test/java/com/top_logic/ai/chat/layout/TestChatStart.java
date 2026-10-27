/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link ChatStart}: a conversation started on the server is about its subject from the
 * start, so that the backend resolved from the subject answers its first message, and the message
 * is sent through the chat of the window that the chat elements of that name show.
 */
public class TestChatStart extends BasicTestCase {

	private static final long TIMEOUT_MILLIS = 60_000;

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	private Person _owner;

	private Person _previousPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(_owner);
	}

	@Override
	protected void tearDown() throws Exception {
		ScriptedBackend.reset();
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/**
	 * The first message of a conversation started about an object is answered by the backend that
	 * answers conversations about that object, through the chat of the window.
	 */
	public void testFirstMessageIsAnsweredByTheSubjectsBackend() throws Exception {
		TLObject subject = persistedObject("the object the order is about");
		AtomicReference<BackendTurn> received = new AtomicReference<>();
		ConversationBackend backend = backend(turn -> {
			received.set(turn);
			return "Done.";
		});
		ScriptedBackend.install(session -> subject.equals(session.getSubject()) ? backend : null);

		TLSubSessionContext window = TLContextManager.getSubSession();
		AgentSession session = ChatStart.start(window, "orders", _owner, subject, "  Do the work.  ");

		assertEquals(subject, session.getSubject());
		assertEquals("Do the work.", session.getTitle());
		TaskProgressTracker tracker = ChatSessionRegistry.tracker(window, "orders");
		assertNotNull("The chat of the window is not registered.", tracker);
		assertEquals(session.tHandle().getObjectName().toExternalForm(), tracker.getActiveSessionId());
		awaitIdle(tracker);

		BackendTurn turn = received.get();
		assertNotNull("The first message was not answered by the subject's backend.", turn);
		assertEquals("Do the work.", turn.text());
		assertEquals(session, turn.session());
		List<String> texts = tracker.getChat().getMessages().stream().map(ChatMessage::getText).toList();
		assertTrue("Messages: " + texts, texts.contains("Done."));
		List<AgentExecution> executions = AgentExecutionStore.getInstance().executionsOf(session);
		assertEquals("Executions: " + executions, 1, executions.size());
		assertEquals("test-start-backend", executions.get(0).getAgentName());
	}

	/** A chat element of the window that is displayed later finds the chat the start sent through. */
	public void testUsesTheChatTheWindowShows() throws Exception {
		ScriptedBackend.install(session -> backend(turn -> "Ok."));
		TLSubSessionContext window = TLContextManager.getSubSession();
		TaskProgressTracker shown = new TaskProgressTracker();
		ChatSessionRegistry.register(window, "shown", shown);

		AgentSession session = ChatStart.start(window, "shown", _owner, null, "Hello");

		assertSame(shown, ChatSessionRegistry.tracker(window, "shown"));
		assertEquals(session.tHandle().getObjectName().toExternalForm(), shown.getActiveSessionId());
		assertNull(session.getSubject());
		awaitIdle(shown);
	}

	/** An empty order starts nothing. */
	public void testEmptyMessageIsRefused() {
		try {
			ChatStart.start(TLContextManager.getSubSession(), "empty", _owner, null, "   ");
			fail("An empty message was accepted.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_EMPTY_MESSAGE, ex.getErrorKey());
		}
	}

	/** A conversation is named after its first message, as the browser names one. */
	public void testTitle() {
		assertEquals("a b c", ChatStart.title(" a\n b \t c "));
		String longText = "x".repeat(100);
		assertEquals("x".repeat(72) + "…", ChatStart.title(longText));
	}

	private static void awaitIdle(TaskProgressTracker tracker) throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (ChatProcessor.isProcessing(tracker)) {
			assertTrue("The run did not end.", System.currentTimeMillis() < deadline);
			TimeUnit.MILLISECONDS.sleep(20);
		}
		HistoryUtils.updateSessionRevision();
	}

	private static ConversationBackend backend(Answer answer) {
		return new ConversationBackend() {
			@Override
			public String agentName() {
				return "test-start-backend";
			}

			@Override
			public BackendFeatures features() {
				return BackendFeatures.NONE;
			}

			@Override
			public String send(BackendTurn turn, AgentEventListener sink,
					CancellationToken cancel) {
				String text = answer.answer(turn);
				String runId = "start-" + System.nanoTime();
				sink.onEvent(new AgentEvent.RunStarted(runId, 0, Instant.now(), "test-model"));
				sink.onEvent(new AgentEvent.TurnStarted(runId, 1, Instant.now()));
				sink.onEvent(new AgentEvent.TextDelta(runId, 1, Instant.now(), text));
				sink.onEvent(new AgentEvent.TurnCompleted(runId, 1, Instant.now(), USAGE,
					List.of(new TextContent(text))));
				sink.onEvent(new AgentEvent.RunCompleted(runId, 1, Instant.now(), StopReason.COMPLETE, USAGE,
					new Money(0.001, Money.USD)));
				return text;
			}
		};
	}

	/** What the backend of a test answers to a message. */
	private interface Answer {
		String answer(BackendTurn turn);
	}

	/** Any persisted object will do as the host's own subject. */
	private TLObject persistedObject(String title) {
		TLObject object = ChatHistoryService.createSession(title, _owner);
		HistoryUtils.updateSessionRevision();
		return object;
	}

	/** The suite, on the services a chat run passes through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatStart.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				ConversationBackendService.Module.INSTANCE));
	}
}
