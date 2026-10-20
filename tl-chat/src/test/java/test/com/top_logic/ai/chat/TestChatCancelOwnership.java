/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.ai.chat.api.ChatServlet;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * That a run belongs to the account, not to the browser window it was started in, when the user
 * stops it (issue #465).
 *
 * <p>
 * {@code POST /chat/cancel} used to start from the tracker of the window the stop arrived from, and
 * a run's state hangs on the tracker of the window that started it. A second tab, and a reloaded
 * page - which is a new window to this servlet - therefore pressed a stop button that did nothing:
 * the run went on to the end of its turns, spending the account's tokens, and the user had no way
 * left to stop it. The run is now found among <em>this account's</em> runs, by the chat the stop
 * names ({@link PendingInteractionRegistry#findBySession}).
 * </p>
 *
 * <p>
 * The run is set up the way {@code ChatProcessor} sets one up - a state with a session, a
 * cancellation token and the registration that names the owner - because starting a real agent
 * would need a provider, a model and a tool catalog to ask about something the servlet decides on
 * its own. What the stop does to the run is asserted on the token: it is what
 * {@code AgentLoop} and every tool of the run watch, and the only thing a stop does to the run.
 * </p>
 *
 * @see TestChatPendingInteraction The other half of the same rule: answering what a run asks.
 */
public class TestChatCancelOwnership extends BasicTestCase {

	private static Person anna;

	private static Person ben;

	private static TLObject annasChat;

	private static TLObject annasOtherChat;

	private static ServletRunner runner;

	private TestChatServletOwnership.Probe _servlet;

	/** The window the run was started in. */
	private TaskProgressTracker _windowA;

	/** The run of window A, as {@code ChatProcessor} would have left it while it works. */
	private AIProcessingState _run;

	/** What the run watches, and what the stop must reach. */
	private CancellationToken _token;

	/** The run's task id, the key it is registered under. */
	private String _taskId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-who-stops");
			ben = TestPerson.createPerson("ben-who-may-not-stop");
			annasChat = ChatHistoryService.createSession("Annas Chat", anna);
			annasOtherChat = ChatHistoryService.createSession("Annas zweiter Chat", anna);
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_servlet = new TestChatServletOwnership.Probe();

		_windowA = newWindow();
		_taskId = UUID.randomUUID().toString();
		_run = runIn(_windowA, _taskId, chatId());
		_token = _run.getCancellationToken();

		// Every handler resolves its window's tracker before it does anything else; which window
		// that is, is what each test decides.
		inWindow(_windowA);
	}

	@Override
	protected void tearDown() throws Exception {
		PendingInteractionRegistry.unregister(_taskId);
		super.tearDown();
	}

	/** The window that started the run still stops it - the fix takes nothing away. */
	public void testTheStartingWindowStops() throws Exception {
		assertEquals("{\"cancelled\":true}", cancel(anna, chatId()));

		assertTrue("The run must have been told to stop.", _token.isCancelled());
	}

	/**
	 * A client that names no chat stops what its own window is running.
	 *
	 * <p>
	 * That is every client from before this fix, and the answer it gets must be the one it got
	 * then: the stop button is not a place to start refusing requests whose body is not the newest.
	 * </p>
	 */
	public void testAClientThatNamesNoChatStopsItsOwnWindowsRun() throws Exception {
		assertEquals("{\"cancelled\":true}", cancel(anna, null));

		assertTrue(_token.isCancelled());
	}

	/** A second tab of the same login stops the run the first tab started. */
	public void testASecondWindowStops() throws Exception {
		inWindow(newWindow());

		assertEquals("{\"cancelled\":true}", cancel(anna, chatId()));

		assertTrue("The other window's stop must have reached the run.", _token.isCancelled());
	}

	/**
	 * A reloaded page stops the run that was started before the reload.
	 *
	 * <p>
	 * The window the run belongs to is gone, so nothing but the account leads back to the run - and
	 * this is the case the user meets most often, because reloading is what they try when the page
	 * looks stuck.
	 * </p>
	 */
	public void testAReloadedPageStops() throws Exception {
		TaskProgressTracker afterReload = newWindow();
		_windowA.removeProcessingState(_taskId);
		inWindow(afterReload);

		assertEquals("{\"cancelled\":true}", cancel(anna, chatId()));

		assertTrue(_token.isCancelled());
	}

	/** Another account may not stop the run - not from its own window, and not by naming the chat. */
	public void testAnotherAccountMayNotStop() throws Exception {
		inWindow(newWindow());

		cancel(ben, chatId());

		assertFalse("Ben's stop must not have reached Anna's run.", _token.isCancelled());
	}

	/** What another account is told is what anyone is told about a chat that is not running. */
	public void testAForeignStopLooksLikeAStopOfNothing() throws Exception {
		inWindow(newWindow());
		String foreign = cancel(ben, chatId());
		String unknown = cancel(ben, "999999999");
		String notRunning = cancel(anna, otherChatId());

		assertEquals("Telling the two apart would say which of the account's chats are running.",
			notRunning, foreign);
		assertEquals("Telling the two apart would say which chat ids exist.", notRunning, unknown);
	}

	/**
	 * The stop hits the chat it names, not whatever else the asking window is running.
	 *
	 * <p>
	 * A window runs one chat at a time in front of the user, but a run it started keeps going while
	 * the user reads another chat. Stopping the one they are looking at must not stop the one they
	 * are not.
	 * </p>
	 */
	public void testTheNamedChatIsStoppedAndNotTheOtherOne() throws Exception {
		TaskProgressTracker windowB = newWindow();
		String otherTaskId = UUID.randomUUID().toString();
		CancellationToken otherToken = runIn(windowB, otherTaskId, otherChatId()).getCancellationToken();
		inWindow(windowB);
		try {
			assertEquals("{\"cancelled\":true}", cancel(anna, chatId()));

			assertTrue("The chat the stop named must have been stopped.", _token.isCancelled());
			assertFalse("The other chat's run must still be running.", otherToken.isCancelled());
		} finally {
			PendingInteractionRegistry.unregister(otherTaskId);
		}
	}

	/** A stop for a chat that is not running stops nothing, not the asking window's other run. */
	public void testAStopForAChatThatIsNotRunningStopsNothing() throws Exception {
		TaskProgressTracker windowB = newWindow();
		String otherTaskId = UUID.randomUUID().toString();
		CancellationToken otherToken = runIn(windowB, otherTaskId, otherChatId()).getCancellationToken();
		inWindow(windowB);
		try {
			// A third chat of Anna's would do as well; what matters is that nothing runs there.
			assertEquals("{\"cancelled\":false}", cancel(anna, "999999999"));

			assertFalse("The window's own run is not what the user asked to stop.",
				otherToken.isCancelled());
			assertFalse(_token.isCancelled());
		} finally {
			PendingInteractionRegistry.unregister(otherTaskId);
		}
	}

	/** A run that has ended leaves nothing to stop, and saying so is not an error. */
	public void testAStopAfterTheRunEndedIsANoOp() throws Exception {
		runEnded();
		inWindow(newWindow());

		assertEquals("{\"cancelled\":false}", cancel(anna, chatId()));
	}

	/** A run of the given chat, in the given window, registered as {@code ChatProcessor} does. */
	private static AIProcessingState runIn(TaskProgressTracker window, String taskId, String sessionId) {
		AIProcessingState state = new AIProcessingState();
		state.setCurrentTaskId(taskId);
		state.setSessionId(sessionId);
		state.startProcessing();
		window.addProcessingState(taskId, state);
		PendingInteractionRegistry.register(taskId, anna, window, state);
		return state;
	}

	/** Tears the run down the way {@code ChatProcessor} tears a finished one down. */
	private void runEnded() {
		PendingInteractionRegistry.unregister(_taskId);
		_windowA.removeProcessingState(_taskId);
		_run.completeSuccessfully();
	}

	/**
	 * A fresh window, as a second tab or a reloaded page brings one: its own tracker, with no run
	 * of its own in it.
	 */
	private static TaskProgressTracker newWindow() {
		return new TaskProgressTracker();
	}

	/** Makes the given window the one the next request arrives from. */
	private static void inWindow(TaskProgressTracker window) {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the tracker on.", subSession);
		ChatSessionRegistry.register(subSession, null, window);
	}

	/** What the servlet answered a stop of the given chat, sent by the given account. */
	private String cancel(Person caller, String sessionId) throws Exception {
		String body = sessionId == null ? "" : "{\"sessionId\":\"" + sessionId + "\"}";
		Recording response = new Recording();
		InvocationContext invocation = runner.newClient().newInvocation(new PostMethodWebRequest(
			"http://localhost/chat/cancel",
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
			"application/json"));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, response.asResponse(), caller);
		} finally {
			ThreadContext.popSuperUser();
		}
		_servlet.post(request, response.asResponse());
		assertEquals(HttpServletResponse.SC_OK, response._status);
		return response._body.toString();
	}

	private static String chatId() {
		return annasChat.tHandle().getObjectName().toExternalForm();
	}

	private static String otherChatId() {
		return annasOtherChat.tHandle().getObjectName().toExternalForm();
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatCancelOwnership.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
