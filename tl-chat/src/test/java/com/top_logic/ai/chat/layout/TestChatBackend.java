/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.RunFailure;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.I18NConstants;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * A conversation claimed by a {@link ConversationBackend} is answered by that backend inside the
 * chat's own run frame: the message and the answer are persisted, the run is recorded, the
 * windows receive its events, a stop reaches the backend, and a backend that fails or breaks its
 * contract ends the run in an error without leaving the chat busy.
 */
public class TestChatBackend extends BasicTestCase {

	private static final String BACKEND = "test-backend";

	private static final String MODEL = "test-scripted-model";

	private static final long TIMEOUT_SECONDS = 60;

	private static final TokenUsage USAGE = new TokenUsage(100, 20, 0, 0, 0);

	/** A refusal that has a message for the user. */
	private static final ResKey REFUSAL = I18NConstants.ERROR_NO_PROVIDER_CONFIGURED;

	private Person _owner;

	private Person _previousPerson;

	private AgentSession _session;

	private String _sessionId;

	private TaskProgressTracker _window;

	private Handler _handler;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(_owner);
		_session = (AgentSession) ChatHistoryService.createSession("Backend", _owner);
		_sessionId = _session.tHandle().getObjectName().toExternalForm();
		HistoryUtils.updateSessionRevision();

		_window = new TaskProgressTracker();
		assertTrue(_window.setActiveSessionId(_sessionId, _owner));
		_handler = new Handler(_window, _owner);
		ChatEventHandlerRegistry.register(_handler);
	}

	@Override
	protected void tearDown() throws Exception {
		ChatEventHandlerRegistry.unregister(_handler);
		ScriptedBackend.reset();
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/**
	 * The user's message and the backend's answer are in the chat and in the stored transcript, and
	 * the run is recorded with the backend's name, its tokens and its cost.
	 */
	public void testAnswerIsPersistedAndRecorded() throws Exception {
		AtomicReference<BackendTurn> received = new AtomicReference<>();
		claim((turn, sink, cancel) -> {
			received.set(turn);
			return answer(sink, "Hello from outside.");
		});

		ChatProcessor.AIProcessingState run = send("Hello?");
		assertNull("The run failed: " + run.getError(), run.getError());

		BackendTurn turn = received.get();
		assertNotNull("The backend was not asked.", turn);
		assertEquals("Hello?", turn.text());
		assertEquals(_sessionId, turn.sessionId());
		assertEquals(_session, turn.session());
		assertEquals(_owner, turn.owner());
		assertTrue(turn.attachments().isEmpty());

		List<String> texts = _window.getChat().getMessages().stream().map(ChatMessage::getText).toList();
		assertTrue("Messages: " + texts, texts.contains("Hello?"));
		assertTrue("Messages: " + texts, texts.contains("Hello from outside."));

		HistoryUtils.updateSessionRevision();
		List<Turn> turns =
			ConversationLogSerializer.fromJson(ChatHistoryService.loadConversationJson(_session)).turns();
		assertEquals(1, turns.size());
		assertEquals("Hello?", ((Part.TextPart) turns.get(0).userParts().get(0)).text());
		List<Part> parts = turns.get(0).assistantParts();
		assertEquals("Parts: " + parts, 1, parts.size());
		assertEquals("Hello from outside.", ((Part.TextPart) parts.get(0)).text());

		AgentExecution execution = onlyExecution();
		assertEquals(BACKEND, execution.getAgentName());
		assertEquals("COMPLETE", execution.getStatus().getName());
		assertEquals(Long.valueOf(100), execution.getInputTokens());
		assertEquals(Long.valueOf(20), execution.getOutputTokens());
		assertEquals(0.002, execution.getCost().doubleValue(), 1e-9);
		assertEquals(MODEL, execution.getModel());

		assertFalse(_window.hasActiveProcessingStateFor(_sessionId));
	}

	/** The window showing the chat receives the run's events and the final answer. */
	public void testWindowReceivesTheStream() throws Exception {
		claim((turn, sink, cancel) -> answer(sink, "Streamed."));

		ChatProcessor.AIProcessingState run = send("Stream it.");
		assertNull("The run failed: " + run.getError(), run.getError());

		List<AgentEvent> events = _handler._events;
		assertTrue("Events: " + events, events.stream().anyMatch(AgentEvent.RunStarted.class::isInstance));
		assertTrue("Events: " + events, events.stream()
			.anyMatch(e -> e instanceof AgentEvent.TextDelta delta && delta.text().equals("Streamed.")));
		assertTrue("Events: " + events, events.stream().anyMatch(AgentEvent.RunCompleted.class::isInstance));
		assertEquals(List.of("Streamed."), _handler._completed);
		assertTrue("Errors: " + _handler._errors, _handler._errors.isEmpty());
	}

	/**
	 * A stop of the user reaches the backend's token while it works, and the run ends as a stopped
	 * run: no error, recorded as cancelled, with what the backend answered so far.
	 */
	public void testStopReachesTheBackend() throws Exception {
		CountDownLatch working = new CountDownLatch(1);
		AtomicBoolean heard = new AtomicBoolean();
		claim((turn, sink, cancel) -> {
			String runId = start(sink);
			sink.onEvent(new AgentEvent.TextDelta(runId, 1, Instant.now(), "Partial"));
			// What a backend does: the stop arrives on the thread of the stop request, the events of
			// the run stay on this one.
			CountDownLatch stopped = new CountDownLatch(1);
			cancel.onCancel(() -> {
				heard.set(true);
				stopped.countDown();
			});
			working.countDown();
			assertTrue("No stop arrived.", stopped.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			sink.onEvent(new AgentEvent.TurnCompleted(runId, 1, Instant.now(), USAGE, List.of()));
			sink.onEvent(new AgentEvent.RunCompleted(runId, 1, Instant.now(), StopReason.ABORTED, USAGE,
				new Money(0.001, Money.USD)));
			return "Partial";
		});

		ChatProcessor.SendResult sent = sendOnly("Take your time.");
		ChatProcessor.AIProcessingState run = _window.getProcessingState(sent.getTaskId());
		assertTrue("The backend did not start.", working.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

		// The lookup the stop request makes.
		PendingInteractionRegistry.PendingRun pending =
			PendingInteractionRegistry.findBySession(_owner, _sessionId);
		assertNotNull("The run is not registered with the account.", pending);
		pending.state().getCancellationToken().cancel();

		run._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		assertTrue("The backend did not hear the stop.", heard.get());
		assertNull("A stop is no error: " + run.getError(), run.getError());
		assertEquals("Partial", run.getLastAgentResponse());

		AgentExecution execution = onlyExecution();
		assertEquals("CANCELLED", execution.getStatus().getName());
		assertEquals(StopReason.ABORTED.name(), execution.getStopReason());
		assertFalse(_window.hasActiveProcessingStateFor(_sessionId));
		assertNull(PendingInteractionRegistry.findBySession(_owner, _sessionId));
	}

	/**
	 * A backend that throws ends the run in an error, reported to the window; the chat is not busy
	 * afterwards and the next message is answered.
	 */
	public void testFailingBackend() throws Exception {
		claim((turn, sink, cancel) -> {
			start(sink);
			throw new IllegalStateException("Container is gone.");
		});

		ChatProcessor.AIProcessingState run = send("Will it fail?");
		assertNotNull("The run did not fail.", run.getError());
		assertEquals("Container is gone.", run.getError().getMessage());
		assertEquals(List.of(RunFailure.APPLICATION_ERROR), _handler.liveErrorKinds());
		assertEquals("ERROR", onlyExecution().getStatus().getName());
		assertFalse("The chat stays busy.", _window.hasActiveProcessingStateFor(_sessionId));

		claim((turn, sink, cancel) -> answer(sink, "Back again."));
		ChatProcessor.AIProcessingState next = send("And now?");
		assertNull("The next run failed: " + next.getError(), next.getError());
		assertEquals("Back again.", next.getLastAgentResponse());
	}

	/** A backend that returns without completing the run ends it in an error. */
	public void testReturnWithoutCompletion() throws Exception {
		claim((turn, sink, cancel) -> {
			String runId = start(sink);
			sink.onEvent(new AgentEvent.TextDelta(runId, 1, Instant.now(), "Half"));
			return "Half";
		});

		ChatProcessor.AIProcessingState run = send("Finish, please.");
		assertNotNull("A run without completion passed.", run.getError());
		assertTrue(run.getError().getMessage(), run.getError().getMessage().contains("without completing"));
		assertEquals(1, _handler.liveErrorKinds().size());
		assertEquals("ERROR", onlyExecution().getStatus().getName());
		assertFalse("The chat stays busy.", _window.hasActiveProcessingStateFor(_sessionId));
	}

	/**
	 * An event from a thread of the backend's own is refused, and so is everything after it: the
	 * run ends in an error, also in its recording, even when the backend ignores the refusal.
	 */
	public void testEventFromAnotherThreadIsRefused() throws Exception {
		AtomicReference<Throwable> refusal = new AtomicReference<>();
		claim((turn, sink, cancel) -> {
			String runId = start(sink);
			Thread reader = new Thread(() -> {
				try {
					sink.onEvent(new AgentEvent.TextDelta(runId, 1, Instant.now(), "From the reader"));
				} catch (Throwable ex) {
					refusal.set(ex);
				}
			});
			reader.start();
			reader.join();
			complete(sink, runId, StopReason.COMPLETE);
			return "Done.";
		});

		ChatProcessor.AIProcessingState run = send("Read it.");
		assertTrue("Refusal: " + refusal.get(), refusal.get() instanceof IllegalStateException);
		assertNotNull("The run passed.", run.getError());
		assertTrue(run.getError().getMessage(),
			run.getError().getMessage().contains("instead of the thread that called send()"));
		assertFalse(_handler._events.stream()
			.anyMatch(e -> e instanceof AgentEvent.TextDelta delta && delta.text().equals("From the reader")));
		assertEquals("ERROR", onlyExecution().getStatus().getName());
	}

	/**
	 * A backend that refuses the message before its run started leaves the user's message and the
	 * refusal in the conversation, in the user's language, also after a reload; the window shows
	 * the error once.
	 */
	public void testRefusalBeforeTheRunStartedIsRecorded() throws Exception {
		claim((turn, sink, cancel) -> {
			throw new TopLogicException(REFUSAL);
		});

		ChatProcessor.AIProcessingState run = inGerman(() -> send("Läuft der Arbeitsbereich?"));
		assertNotNull("The run did not fail.", run.getError());
		assertEquals(List.of(RunFailure.APPLICATION_ERROR), _handler.liveErrorKinds());

		assertRecordedError("Läuft der Arbeitsbereich?");
		assertEquals("ERROR", onlyExecution().getStatus().getName());
		assertEquals(BACKEND, onlyExecution().getAgentName());
		assertFalse("The chat stays busy.", _window.hasActiveProcessingStateFor(_sessionId));
	}

	/**
	 * A run of the framework's own agent whose loop throws leaves the user's message and the error
	 * in the conversation, in the user's language, also after a reload; the window shows the error
	 * once.
	 */
	public void testFailingAgentLoopIsRecorded() throws Exception {
		ScriptedBackend.install(session -> null);
		ScriptedModel.install((request, turn) -> {
			throw new TopLogicException(REFUSAL);
		});

		ChatProcessor.AIProcessingState run = inGerman(() -> send("Wer antwortet?", "TestChatPlanner"));
		assertNotNull("The run did not fail.", run.getError());
		assertEquals(List.of(RunFailure.APPLICATION_ERROR), _handler.liveErrorKinds());

		assertRecordedError("Wer antwortet?");
		assertEquals("ERROR", onlyExecution().getStatus().getName());
	}

	/**
	 * The conversation as a reload reads it holds one turn: the question, and one error part with
	 * the text of {@link #REFUSAL} in German.
	 */
	private void assertRecordedError(String question) {
		String message = Resources.getInstance(Locale.GERMAN).getString(REFUSAL);
		assertFalse("No German text was resolved.",
			message.equals(Resources.getInstance(Locale.ENGLISH).getString(REFUSAL)));
		HistoryUtils.updateSessionRevision();
		List<Turn> turns =
			ConversationLogSerializer.fromJson(ChatHistoryService.loadConversationJson(_session)).turns();
		assertEquals("Turns: " + turns, 1, turns.size());
		assertEquals(question, ((Part.TextPart) turns.get(0).userParts().get(0)).text());
		List<Part.ErrorPart> errors = turns.get(0).assistantParts().stream()
			.filter(Part.ErrorPart.class::isInstance).map(Part.ErrorPart.class::cast).toList();
		assertEquals("Parts: " + turns.get(0).assistantParts(), 1, errors.size());
		assertEquals(RunFailure.APPLICATION_ERROR, errors.get(0).kind());
		assertEquals(message, errors.get(0).message());
	}

	/** Runs the given code with the user speaking German. */
	private static <T> T inGerman(Callable<T> code) throws Exception {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		Locale previous = subSession.getCurrentLocale();
		subSession.setCurrentLocale(Locale.GERMAN);
		try {
			return code.call();
		} finally {
			subSession.setCurrentLocale(previous);
		}
	}

	/** A conversation no resolver claims is answered by the framework's own agent. */
	public void testUnclaimedConversationRunsTheAgent() throws Exception {
		AtomicBoolean asked = new AtomicBoolean();
		ScriptedBackend.install(session -> {
			asked.set(true);
			return null;
		});
		ScriptedModel.install((request, turn) -> List.<ContentPart> of(new TextContent("Internal answer.")));

		ChatProcessor.AIProcessingState run = send("Who answers?", "TestChatPlanner");
		assertNull("The run failed: " + run.getError(), run.getError());
		assertTrue("The resolvers were not asked.", asked.get());
		assertEquals("Internal answer.", run.getLastAgentResponse());
		assertEquals("TestChatPlanner", onlyExecution().getAgentName());
	}

	private void claim(Script script) {
		ConversationBackend backend = new ConversationBackend() {
			@Override
			public String agentName() {
				return BACKEND;
			}

			@Override
			public BackendFeatures features() {
				return BackendFeatures.NONE;
			}

			@Override
			public String send(BackendTurn turn, AgentEventListener sink, CancellationToken cancel) throws Exception {
				return script.send(turn, sink, cancel);
			}
		};
		ScriptedBackend.install(session -> session.equals(_session) ? backend : null);
	}

	private static String answer(AgentEventListener sink, String text) {
		String runId = start(sink);
		sink.onEvent(new AgentEvent.TextDelta(runId, 1, Instant.now(), text));
		sink.onEvent(new AgentEvent.TurnCompleted(runId, 1, Instant.now(), USAGE, List.of(new TextContent(text))));
		complete(sink, runId, StopReason.COMPLETE);
		return text;
	}

	private static String start(AgentEventListener sink) {
		String runId = "backend-" + System.nanoTime();
		sink.onEvent(new AgentEvent.RunStarted(runId, 0, Instant.now(), MODEL));
		sink.onEvent(new AgentEvent.TurnStarted(runId, 1, Instant.now()));
		return runId;
	}

	private static void complete(AgentEventListener sink, String runId, StopReason reason) {
		sink.onEvent(new AgentEvent.RunCompleted(runId, 1, Instant.now(), reason, USAGE,
			new Money(0.002, Money.USD)));
	}

	private ChatProcessor.AIProcessingState send(String text) throws Exception {
		return send(text, null);
	}

	private ChatProcessor.AIProcessingState send(String text, String agent) throws Exception {
		ChatProcessor.SendResult sent = sendOnly(text, agent);
		ChatProcessor.AIProcessingState run = _window.getProcessingState(sent.getTaskId());
		run._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		HistoryUtils.updateSessionRevision();
		return run;
	}

	private ChatProcessor.SendResult sendOnly(String text) {
		return sendOnly(text, null);
	}

	private ChatProcessor.SendResult sendOnly(String text, String agent) {
		ChatProcessor.SendResult sent = ChatProcessor.sendMessage(_window, TLContextManager.getSubSession(),
			_owner.getName(), text, null, new ChatProcessor.ChatRunOptions(agent, null, false, List.of()));
		assertNotNull("The send was refused.", sent.getTaskId());
		return sent;
	}

	private AgentExecution onlyExecution() {
		HistoryUtils.updateSessionRevision();
		List<AgentExecution> executions = AgentExecutionStore.getInstance().executionsOf(_session);
		assertFalse("No execution recorded.", executions.isEmpty());
		return executions.get(executions.size() - 1);
	}

	/** What the backend of a test does with one message. */
	private interface Script {
		String send(BackendTurn turn, AgentEventListener sink, CancellationToken cancel) throws Exception;
	}

	/** The window's open event stream, keeping what it was sent. */
	private static final class Handler implements ChatEventHandler {

		private final TaskProgressTracker _tracker;

		private final Person _account;

		final List<AgentEvent> _events = new CopyOnWriteArrayList<>();

		final List<String> _completed = new CopyOnWriteArrayList<>();

		final List<ProviderErrorEvent> _errors = new CopyOnWriteArrayList<>();

		Handler(TaskProgressTracker tracker, Person account) {
			_tracker = tracker;
			_account = account;
		}

		@Override
		public TaskProgressTracker getTracker() {
			return _tracker;
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
			_events.add(event);
		}

		@Override
		public void sendMessageComplete(MessageCompleteEvent event) {
			_completed.add(event.getContentText());
		}

		@Override
		public void sendProviderError(ProviderErrorEvent event) {
			_errors.add(event);
		}

		/**
		 * The kinds of the errors the window was shown, in the order it was: each one an error card,
		 * whether it came with the run's events or after the run died.
		 */
		List<String> liveErrorKinds() {
			List<String> result = new ArrayList<>();
			for (AgentEvent event : _events) {
				if (event instanceof AgentEvent.ProviderError error) {
					result.add(error.kind());
				}
			}
			for (ProviderErrorEvent error : _errors) {
				result.add(error.getKind());
			}
			return result;
		}
	}

	/** The suite, on the services a chat run passes through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatBackend.class),
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
