/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.ChatCommandDriver;
import com.top_logic.ai.chat.view.ChatControl;
import com.top_logic.ai.chat.view.ChatConversationControl;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.model.ModelService;

/**
 * That a run belongs to the account, not to the browser window it was started in, when the user
 * stops it (issue #465).
 *
 * <p>
 * The stop used to start from the tracker of the window the stop arrived from, and a run's state
 * hangs on the tracker of the window that started it. A second tab, and a reloaded page - which is
 * a new window to the server - therefore pressed a stop button that did nothing:
 * the run went on to the end of its turns, spending the account's tokens, and the user had no way
 * left to stop it. The run is now found among <em>this account's</em> runs, by the chat the stop
 * names ({@link PendingInteractionRegistry#findBySession}).
 * </p>
 *
 * <p>
 * The run is set up the way {@code ChatProcessor} sets one up - a state with a session, a
 * cancellation token and the registration that names the owner - because starting a real agent
 * would need a provider, a model and a tool catalog to ask about something the command decides on
 * its own. The stop is the {@link ChatControl#CMD_CANCEL} command, run as the browser sends it
 * ({@link ChatCommandDriver}) from a conversation of the window the test names. What the stop does to the run is asserted on the token: it is what
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

	/** The windows of the test, each a conversation the commands are sent from. */
	private final List<Window> _windows = new ArrayList<>();

	/** The window the run was started in. */
	private Window _windowA;

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
		}

		_windowA = newWindow(anna);
		_taskId = UUID.randomUUID().toString();
		_run = runIn(_windowA.tracker(), _taskId, chatId());
		_token = _run.getCancellationToken();
	}

	@Override
	protected void tearDown() throws Exception {
		PendingInteractionRegistry.unregister(_taskId);
		for (Window window : _windows) {
			window.close();
		}
		_windows.clear();
		super.tearDown();
	}

	/** The window that started the run still stops it - the fix takes nothing away. */
	public void testTheStartingWindowStops() throws Exception {
		assertTrue(cancel(_windowA, anna, chatId()));

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
		assertTrue(cancel(_windowA, anna, null));

		assertTrue(_token.isCancelled());
	}

	/** A second tab of the same login stops the run the first tab started. */
	public void testASecondWindowStops() throws Exception {
		assertTrue(cancel(newWindow(anna), anna, chatId()));

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
		Window afterReload = newWindow(anna);
		_windowA.tracker().removeProcessingState(_taskId);

		assertTrue(cancel(afterReload, anna, chatId()));

		assertTrue(_token.isCancelled());
	}

	/** Another account may not stop the run - not from its own window, and not by naming the chat. */
	public void testAnotherAccountMayNotStop() throws Exception {
		assertFalse(cancel(newWindow(ben), ben, chatId()));

		assertFalse("Ben's stop must not have reached Anna's run.", _token.isCancelled());
	}

	/** What another account is told is what anyone is told about a chat that is not running. */
	public void testAForeignStopLooksLikeAStopOfNothing() throws Exception {
		Window bens = newWindow(ben);
		Map<String, Object> foreign = answer(bens, ben, chatId());
		Map<String, Object> unknown = answer(bens, ben, "999999999");
		Map<String, Object> notRunning = answer(newWindow(anna), anna, otherChatId());

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
		Window windowB = newWindow(anna);
		String otherTaskId = UUID.randomUUID().toString();
		CancellationToken otherToken = runIn(windowB.tracker(), otherTaskId, otherChatId()).getCancellationToken();
		try {
			assertTrue(cancel(windowB, anna, chatId()));

			assertTrue("The chat the stop named must have been stopped.", _token.isCancelled());
			assertFalse("The other chat's run must still be running.", otherToken.isCancelled());
		} finally {
			PendingInteractionRegistry.unregister(otherTaskId);
		}
	}

	/** A stop for a chat that is not running stops nothing, not the asking window's other run. */
	public void testAStopForAChatThatIsNotRunningStopsNothing() throws Exception {
		Window windowB = newWindow(anna);
		String otherTaskId = UUID.randomUUID().toString();
		CancellationToken otherToken = runIn(windowB.tracker(), otherTaskId, otherChatId()).getCancellationToken();
		try {
			// A third chat of Anna's would do as well; what matters is that nothing runs there.
			assertFalse(cancel(windowB, anna, "999999999"));

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

		assertFalse(cancel(newWindow(anna), anna, chatId()));
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
		_windowA.tracker().removeProcessingState(_taskId);
		_run.completeSuccessfully();
	}

	/**
	 * A fresh window of the given account, as a second tab or a reloaded page brings one: a
	 * conversation with a tracker of its own, with no run in it.
	 */
	private Window newWindow(Person account) throws Exception {
		Window window = new Window(account, "cancel-" + getName() + "-" + _windows.size());
		_windows.add(window);
		return window;
	}

	/** Whether the stop the given account sent from the given window stopped a run. */
	private static boolean cancel(Window window, Person caller, String sessionId) {
		return Boolean.TRUE.equals(answer(window, caller, sessionId).get(ChatControl.FIELD_CANCELLED));
	}

	/** What the stop of the given chat, sent by the given account from the given window, answered. */
	private static Map<String, Object> answer(Window window, Person caller, String sessionId) {
		return window.command(caller, ChatControl.CMD_CANCEL,
			sessionId == null ? Map.of() : Map.of(ChatControl.ARG_SESSION_ID, sessionId));
	}

	/** A browser window showing one conversation, which the commands of a test are sent from. */
	static final class Window {

		private final ChatCommandDriver _driver;

		private final ChatConversationControl _conversation;

		Window(Person account, String chatId) throws Exception {
			_driver = new ChatCommandDriver("window-" + chatId, chatId);
			_conversation = _driver.conversation(account);
		}

		/** The conversation's tracker, which a run of the window is registered on. */
		TaskProgressTracker tracker() {
			return ChatCommandDriver.tracker(_conversation);
		}

		/** Runs a command of the conversation as the given account, and answers with its reply. */
		Map<String, Object> command(Person caller, String name, Map<String, Object> arguments) {
			_driver.actAs(caller);
			return _driver.command(_conversation, name, arguments);
		}

		void close() {
			_conversation.cleanupTree();
			_driver.close();
		}
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
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
