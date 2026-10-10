/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.ai.chat.TestChatServletOwnership.Recording;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.ChatServlet;
import com.top_logic.ai.chat.api.messages.InteractionResolvedEvent;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * What a window sees when it opens a conversation whose run is still going (issues #512, #544).
 *
 * <p>
 * A turn is written to the database when its run ends. Until then the whole of it - the user's
 * message, what the agent has said so far, the tool it is about to call, the approval it is parked
 * on - lives in the {@link ConversationLog} of the window the run was started in. A second tab, a
 * reloaded page and a new login each load the conversation from what is persisted, so the transcript
 * they were handed stopped at the <em>previous</em> turn: the approval card floated above the
 * composer with no question above it, and once answered the decision was stitched under an
 * unrelated turn.
 * </p>
 *
 * <p>
 * The other half of the same defect is what happens when that run finishes. Its answer was sent to
 * the handler captured at run start, so a run whose window had closed logged "SSE handler closed, no
 * replacement found - dropping message_complete" and every window that did have the conversation
 * open went on showing a turn that had ended.
 * </p>
 *
 * <p>
 * The servlet is driven directly, exactly as {@link TestChatServletOwnership} drives it, and the run
 * is set up the way {@code ChatProcessor} sets one up: starting a real agent would need a provider,
 * a model and a tool catalog to ask about something the servlet decides on its own.
 * </p>
 */
public class TestChatInFlightWindow extends BasicTestCase {

	private static Person nina;

	private static Person otto;

	private static TLObject ninasChat;

	private static ServletRunner runner;

	private TestChatServletOwnership.Probe _servlet;

	/** The window the run was started in - the one that is gone by the time the tests look. */
	private TaskProgressTracker _windowA;

	/** The run of window A, as {@code ChatProcessor} would have left it while it works. */
	private AIProcessingState _run;

	/** The log the run records into: the previous turn, plus the one it is working on. */
	private ConversationLog _log;

	/** The listener holding the paragraph the run has streamed but not yet recorded. */
	private ConversationLogListener _listener;

	/** The run's task id, the key it is registered under. */
	private String _taskId;

	/** The correlation id of the approval the run is parked on. */
	private String _approvalId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (nina == null) {
			nina = TestPerson.createPerson("nina-with-a-running-chat");
			otto = TestPerson.createPerson("otto-who-is-not-nina");
			ninasChat = ChatHistoryService.createSession("Ninas Chat", nina);
			// Created on the store's writer thread: this thread would otherwise not see the chat
			// until something else moved its revision on, and whichever test ran first could not
			// open it.
			HistoryUtils.updateSessionRevision();
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_servlet = new TestChatServletOwnership.Probe();

		_log = new ConversationLog();
		_listener = new ConversationLogListener(_log);
		recordFinishedTurn();
		_approvalId = recordOpenTurn();

		_run = new AIProcessingState();
		_taskId = UUID.randomUUID().toString();
		_run.setCurrentTaskId(_taskId);
		_run.setSessionId(chatId());
		_run.setConversationLog(_log);
		_run.setLogListener(_listener);
		_run.startProcessing();

		_windowA = new TaskProgressTracker();
		_windowA.addProcessingState(_taskId, _run);
		PendingInteractionRegistry.register(_taskId, nina, _windowA, _run);

		// Every test here asks from a window that has no run of its own, which is what a second tab,
		// a reloaded page and a new login all are to this servlet.
		inWindow(new TaskProgressTracker());
	}

	@Override
	protected void tearDown() throws Exception {
		PendingInteractionRegistry.unregister(_taskId);
		super.tearDown();
	}

	/**
	 * The defect itself: the restored transcript ends at the previous turn.
	 *
	 * <p>
	 * Everything the running turn has produced has to be in it, and the user's own message first of
	 * all - it is the question the approval card is about, and without it the card belongs to
	 * nothing.
	 * </p>
	 */
	public void testTheRunningTurnIsPartOfTheRestoredTranscript() throws Exception {
		String body = restore();

		assertTrue("The message that started the running turn must be restored: " + body,
			body.contains("Bitte aendere die Beschreibung"));
		assertTrue("What the agent has already said must be restored: " + body,
			body.contains("Dafuer brauche ich eine Freigabe."));
		assertTrue("The approval the run is parked on must be restored: " + body,
			body.contains(_approvalId));
	}

	/**
	 * In order, and inside the turn it belongs to.
	 *
	 * <p>
	 * This is what decides where the recorded decision ends up: a card the client appends because
	 * the turn was missing lands under whatever turn happens to be last.
	 * </p>
	 */
	public void testTheOpenTurnComesLastAndHoldsItsApproval() throws Exception {
		String body = restore();

		int previousTurn = body.indexOf("Was war letzte Woche");
		int userMessage = body.indexOf("Bitte aendere die Beschreibung");
		int answer = body.indexOf("Dafuer brauche ich eine Freigabe.");
		int approval = body.indexOf(_approvalId);

		assertTrue("The finished turn must still come first: " + body,
			previousTurn >= 0 && previousTurn < userMessage);
		assertTrue("The answer belongs after the message it answers: " + body,
			userMessage < answer);
		assertTrue("The approval belongs after the answer that announced it: " + body,
			answer < approval);
	}

	/**
	 * The paragraph the run is in the middle of writing is part of what is restored.
	 *
	 * <p>
	 * Streamed text is recorded at message boundaries, so between them the log is one unfinished
	 * sentence short of what the reader was watching. It is added to the snapshot and to nothing
	 * else - recording it stays the agent's own business.
	 * </p>
	 */
	public void testTheUnrecordedStreamIsRestoredToo() throws Exception {
		_listener.onEvent(new AgentEvent.TextDelta("run-1", 7, Instant.now(), "Ich pruefe das "));
		_listener.onEvent(new AgentEvent.TextDelta("run-1", 8, Instant.now(), "gerade"));

		String body = restore();

		assertTrue("What has streamed since the last recorded part must be shown: " + body,
			body.contains("Ich pruefe das gerade"));
		assertEquals("The snapshot must not have written it into the log.",
			"Ich pruefe das gerade", _listener.bufferedText());
	}

	/** A run that has ended leaves the persisted transcript to answer, as it always did. */
	public void testAFinishedRunIsNotRestoredFromMemory() throws Exception {
		runEnded();

		String body = restore();

		assertFalse("A run nobody is working on must not be read out of a stale log: " + body,
			body.contains("Bitte aendere die Beschreibung"));
	}

	/** Another account's window reaches none of this. */
	public void testAnotherAccountSeesNothingOfTheRun() throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = runner.newClient().newInvocation(
			new GetMethodWebRequest("http://localhost/chat/sessions/" + chatId()));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, response.asResponse(), otto);
		} finally {
			ThreadContext.popSuperUser();
		}
		_servlet.get(request, response.asResponse());

		assertEquals("A chat that is not yours is not yours to read.",
			HttpServletResponse.SC_NOT_FOUND, response._status);
	}

	/**
	 * Whether the chat is working is a fact about the chat, not about the window asking.
	 *
	 * <p>
	 * A window told the conversation is idle offers a composer for a turn that is already under way,
	 * and no way to stop it.
	 * </p>
	 */
	public void testTheRestoredWindowIsToldTheChatIsWorking() throws Exception {
		restore();

		String body = updates();

		assertTrue("The run of another window must still make this chat busy: " + body,
			body.contains("\"processing\":true"));
	}

	/** A chat with no run is not reported as working. */
	public void testAnIdleChatStaysIdle() throws Exception {
		restore();
		runEnded();

		String body = updates();

		assertFalse("Nothing is running here: " + body, body.contains("\"processing\":true"));
	}

	/**
	 * The completion goes to whoever has the conversation open, not to the tab that started the run.
	 *
	 * <p>
	 * The "dropping message_complete" half of #512: window A is gone, window B has the chat open,
	 * and the answer has to reach B.
	 * </p>
	 */
	public void testTheCompletionReachesTheWindowThatIsAttached() {
		TaskProgressTracker windowB = new TaskProgressTracker();
		windowB.getSessionManager().switchSession(chatId(), nina);
		try (Watcher b = watch(windowB, nina)) {
			assertEquals("The window showing the chat is what the answer is addressed to.",
				List.of(b), ChatEventHandlerRegistry.findForSession(nina, chatId(), null));
		}
	}

	/** A window of the same account that has moved on to another chat is none of its business. */
	public void testAWindowShowingAnotherChatIsNotAddressed() {
		try (Watcher elsewhere = watch(new TaskProgressTracker(), nina)) {
			assertEquals("A window showing no chat of this run must not be told: ",
				List.of(), ChatEventHandlerRegistry.findForSession(nina, chatId()));
		}
	}

	/** And another account's window never is, whatever it is showing. */
	public void testAnotherAccountsWindowIsNotAddressed() {
		TaskProgressTracker ottosWindow = new TaskProgressTracker();
		ottosWindow.getSessionManager().switchSession(chatId(), nina);
		try (Watcher ottos = watch(ottosWindow, otto)) {
			assertEquals("The chat belongs to Nina, and so does what is said about it.",
				List.of(), ChatEventHandlerRegistry.findForSession(nina, chatId()));
		}
	}

	/**
	 * With nobody watching, the window the run was started in is the address - which is what every
	 * run had before this. The turn is recorded either way, so a dropped frame loses nothing.
	 */
	public void testWithNobodyAttachedTheRunsOwnWindowIsTheAddress() {
		Watcher own = new Watcher(_windowA, nina);

		assertEquals(List.of(own), ChatEventHandlerRegistry.findForSession(nina, chatId(), own));
		assertEquals(List.of(), ChatEventHandlerRegistry.findForSession(nina, chatId(), null));
	}

	/**
	 * The run streams to every window showing its conversation, not only to the one it was started
	 * in (issue #544).
	 *
	 * <p>
	 * Before, the second window was shown the snapshot and the completion, and nothing in between:
	 * it looked frozen for as long as the run wrote.
	 * </p>
	 */
	public void testTheRunStreamsToEveryWindowShowingTheChat() {
		_windowA.getSessionManager().switchSession(chatId(), nina);
		TaskProgressTracker windowB = new TaskProgressTracker();
		windowB.getSessionManager().switchSession(chatId(), nina);
		try (Watcher a = watch(_windowA, nina); Watcher b = watch(windowB, nina)) {
			stream(new AgentEvent.TurnStarted("run-1", 2, Instant.now()));
			stream(new AgentEvent.TextDelta("run-1", 2, Instant.now(), "Ich pruefe das "));
			stream(new AgentEvent.TextDelta("run-1", 2, Instant.now(), "gerade"));

			List<String> expected = List.of("TurnStarted", "Ich pruefe das @15", "gerade@21");
			assertEquals("The window that started the run.", expected, a._seen);
			assertEquals("The window that opened the chat later.", expected, b._seen);
		}
	}

	/**
	 * A window that is gone, or that shows another chat, is told nothing - and nothing is left
	 * registered for it.
	 */
	public void testAWindowThatIsGoneGetsNothing() {
		TaskProgressTracker windowB = new TaskProgressTracker();
		windowB.getSessionManager().switchSession(chatId(), nina);
		Watcher closed = watch(windowB, nina);
		closed.close();
		TaskProgressTracker windowC = new TaskProgressTracker();
		windowC.getSessionManager().switchSession(chatId(), nina);
		Watcher reloading = watch(windowC, nina);
		reloading._active = false;
		try (Watcher elsewhere = watch(new TaskProgressTracker(), nina); Watcher inactive = reloading) {
			stream(new AgentEvent.TextDelta("run-1", 2, Instant.now(), "Ich pruefe das "));

			assertEquals("A window whose stream was closed.", List.of(), closed._seen);
			assertEquals("A window whose stream is no longer active.", List.of(), inactive._seen);
			assertEquals("A window showing no chat of this run.", List.of(), elsewhere._seen);
		}
		assertEquals("A closed window is not addressed any more.",
			List.of(), ChatEventHandlerRegistry.findStreamTargets(_windowA, nina, chatId()));
	}

	/**
	 * A window that has just reconnected is one window: its newest stream gets the delta, once.
	 */
	public void testAReconnectedWindowGetsEachDeltaOnce() {
		TaskProgressTracker windowB = new TaskProgressTracker();
		windowB.getSessionManager().switchSession(chatId(), nina);
		try (Watcher old = watch(windowB, nina); Watcher current = watch(windowB, nina)) {
			stream(new AgentEvent.TextDelta("run-1", 2, Instant.now(), "gerade"));

			assertEquals(List.of(), old._seen);
			assertEquals(List.of("gerade@6"), current._seen);
		}
	}

	/**
	 * The snapshot says where in the run's text it ends, and the deltas say where they end: a window
	 * joining mid-run takes every delta beyond its snapshot and none within it.
	 */
	public void testTheSnapshotAndTheStreamJoinWithoutAGapOrAnOverlap() throws Exception {
		_listener.onEvent(new AgentEvent.TextDelta("run-1", 2, Instant.now(), "Ich pruefe das "));

		String body = restore();
		assertTrue("The snapshot holds the text so far: " + body, body.contains("Ich pruefe das "));
		assertTrue("The snapshot says where it ends: " + body,
			body.contains("\"inFlight\":{\"streamedChars\":15}"));

		TaskProgressTracker windowB = new TaskProgressTracker();
		windowB.getSessionManager().switchSession(chatId(), nina);
		try (Watcher b = watch(windowB, nina)) {
			stream(new AgentEvent.TextDelta("run-1", 2, Instant.now(), "gerade"));

			assertEquals("The first delta after the snapshot starts where the snapshot ends.",
				List.of("gerade@21"), b._seen);
		}
	}

	/** A conversation with no run in flight has no stream to join. */
	public void testAnIdleChatHasNoStreamPosition() throws Exception {
		runEnded();

		assertFalse(restore().contains("inFlight"));
	}

	/**
	 * What a run spent is counted once, in the window it belongs to, however many windows show it.
	 */
	public void testTheRunsTokensAreCountedOnce() {
		_windowA.getSessionManager().switchSession(chatId(), nina);
		TaskProgressTracker windowB = new TaskProgressTracker();
		windowB.getSessionManager().switchSession(chatId(), nina);
		long before = _windowA.getCumulativeUsage().outputTokens();
		try (Watcher a = watch(_windowA, nina); Watcher b = watch(windowB, nina)) {
			stream(new AgentEvent.RunCompleted("run-1", 2, Instant.now(), StopReason.COMPLETE,
				new TokenUsage(100, 40, 0, 0, 0), Money.zero(Money.USD)));

			assertEquals(before + 40, _windowA.getCumulativeUsage().outputTokens());
			assertEquals(0, windowB.getCumulativeUsage().outputTokens());
			assertEquals(List.of("RunCompleted"), b._seen);
		}
	}

	/**
	 * A window that only watched the run reads the conversation back when it ends.
	 *
	 * <p>
	 * It loaded the conversation before the run's turn was recorded, and the turn is written to the
	 * database and to the run's own window, not to it. Reading the chat back served that old copy,
	 * and the finished turn vanished from the window that had just watched it being written.
	 * </p>
	 */
	public void testAWatchingWindowDropsItsOldCopyOfTheChat() {
		TaskProgressTracker windowB = new TaskProgressTracker();
		windowB.getSessionManager().switchSession(chatId(), nina);
		int recorded = windowB.getSessionManager().getOrCreateConversationLog().turnCount();
		Turn stale = windowB.getSessionManager().getOrCreateConversationLog().startTurn();
		stale.setUserParts(List.of(new Part.TextPart(
			UUID.randomUUID().toString(), Instant.now(), "Nur in diesem Fenster")));

		windowB.getSessionManager().markChangedElsewhere(chatId());
		// The window's next request - reading the chat back - switches to it again.
		windowB.getSessionManager().switchSession(chatId(), nina);

		assertEquals("The chat is what the database holds, not what this window had.",
			recorded, windowB.getSessionManager().getOrCreateConversationLog().turnCount());
		assertEquals("It is still the chat this window is showing.",
			chatId(), windowB.getActiveSessionId());
	}

	/** Passes an event through the run's listeners in the order {@code ChatProcessor} chains them. */
	private void stream(AgentEvent event) {
		_listener.onEvent(event);
		ChatProcessor.streamForwarder(_windowA, nina, chatId(), _listener).onEvent(event);
	}

	/** The finished turn of the conversation, as a previous run left it behind. */
	private void recordFinishedTurn() {
		Turn done = _log.startTurn();
		done.setUserParts(List.of(new Part.TextPart(
			UUID.randomUUID().toString(), Instant.now(), "Was war letzte Woche?")));
		done.addAssistantPart(new Part.TextPart(
			UUID.randomUUID().toString(), Instant.now(), "Drei Belege sind eingegangen."));
		_log.completeTurn();
	}

	/**
	 * The turn the run is working on: the user's message, the answer so far, and the approval the
	 * agent thread is parked on.
	 *
	 * @return The approval's correlation id.
	 */
	private String recordOpenTurn() {
		Turn open = _log.startTurn();
		open.setUserParts(List.of(new Part.TextPart(
			UUID.randomUUID().toString(), Instant.now(), "Bitte aendere die Beschreibung")));
		open.addAssistantPart(new Part.TextPart(
			UUID.randomUUID().toString(), Instant.now(), "Dafuer brauche ich eine Freigabe."));
		String approvalId = UUID.randomUUID().toString();
		open.addAssistantPart(Part.ApprovalPart.pending(approvalId, Instant.now(), "call-1",
			"editText", "{\"objectId\":\"Invoice:1\"}", "Modifies shared application data.",
			ToolMode.DATA_WRITE));
		return approvalId;
	}

	/** Tears the run down the way {@code ChatProcessor} tears a finished one down. */
	private void runEnded() {
		_run.completeSuccessfully();
		PendingInteractionRegistry.unregister(_taskId);
		_windowA.removeProcessingState(_taskId);
	}

	/** What {@code GET /chat/sessions/{id}} answers Nina, as JSON. */
	private String restore() throws Exception {
		return asNina(new GetMethodWebRequest("http://localhost/chat/sessions/" + chatId()));
	}

	/** What {@code GET /chat/updates} answers Nina, as JSON. */
	private String updates() throws Exception {
		return asNina(new GetMethodWebRequest("http://localhost/chat/updates"));
	}

	private String asNina(GetMethodWebRequest get) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = runner.newClient().newInvocation(get);
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, response.asResponse(), nina);
		} finally {
			ThreadContext.popSuperUser();
		}
		_servlet.get(request, response.asResponse());
		assertEquals(HttpServletResponse.SC_OK, response._status);
		return response._body.toString();
	}

	/** Registers a chat UI of the given account for the given window. */
	private static Watcher watch(TaskProgressTracker window, Person account) {
		Watcher watcher = new Watcher(window, account);
		ChatEventHandlerRegistry.register(watcher);
		return watcher;
	}

	/** A chat UI that notes what a run shows it. */
	private static final class Watcher implements ChatEventHandler, AutoCloseable {

		private final TaskProgressTracker _window;

		private final Person _account;

		/** The events received: a delta as its text and end position, anything else by its kind. */
		final List<String> _seen = new ArrayList<>();

		boolean _active = true;

		Watcher(TaskProgressTracker window, Person account) {
			_window = window;
			_account = account;
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
			return _active;
		}

		@Override
		public void onEvent(AgentEvent event) {
			_seen.add(event.getClass().getSimpleName());
		}

		@Override
		public void onTextDelta(AgentEvent.TextDelta delta, long end) {
			_seen.add(delta.text() + "@" + end);
		}

		@Override
		public void sendMessageComplete(MessageCompleteEvent event) {
			// Not what this test asks about.
		}

		@Override
		public void sendProviderError(ProviderErrorEvent event) {
			// Not what this test asks about.
		}

		@Override
		public void sendInteractionResolved(InteractionResolvedEvent event) {
			// Not what this test asks about.
		}

		@Override
		public void close() {
			ChatEventHandlerRegistry.unregister(this);
		}
	}

	/** Makes the given window the one the next request arrives from. */
	private static void inWindow(TaskProgressTracker window) {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the tracker on.", subSession);
		ChatSessionRegistry.register(subSession, null, window);
	}

	private static String chatId() {
		return ninasChat.tHandle().getObjectName().toExternalForm();
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatInFlightWindow.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
