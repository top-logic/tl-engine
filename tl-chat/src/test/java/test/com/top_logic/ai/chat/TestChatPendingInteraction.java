/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.ai.chat.TestChatCancelOwnership.Window;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.messages.ChatStatusReply;
import com.top_logic.ai.chat.api.messages.InteractionResolvedEvent;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.ChatCommandDriver;
import com.top_logic.ai.chat.view.ChatControl;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.InteractionAsker;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.model.ModelService;

/**
 * That a pending approval or question belongs to the account, not to the browser window whose run
 * asked it (issue #393).
 *
 * <p>
 * A run parks its thread on a future in the {@link UserInteractionStore} of one
 * {@link AIProcessingState}, and that state hangs on the {@link TaskProgressTracker} of one window.
 * Before #393 the answer looked the request up in the tracker of the window the
 * answer arrived from, so a second tab - or a reloaded page, which is a new window - could see the
 * pending request in its {@code run_state} frame but could not answer it: the answer was
 * logged as late and dropped, and the run stayed parked until its timeout.
 * </p>
 *
 * <p>
 * Each test here gives window A a run with a pending approval, then answers from a
 * <em>different</em> conversation - which is what a second tab, a reload and a new login all are to
 * the server - and asserts against the future the agent thread would be parked on. Ownership is the
 * other half: an answer from another account must leave that future exactly as it was.
 * </p>
 *
 * <p>
 * The answer is the {@link ChatControl#CMD_RESPOND} command, run as the browser sends it
 * ({@link ChatCommandDriver}). The run is set up the way {@code ChatProcessor} sets one up - a
 * state, a store, and the registration that names the responder - because starting a real agent
 * would need a provider, a model and a tool catalog to ask about something the command decides on
 * its own.
 * </p>
 */
public class TestChatPendingInteraction extends BasicTestCase {

	private static Person anna;

	private static Person ben;

	private static TLObject annasChat;

	/** The windows of the test, each a conversation the commands are sent from. */
	private final List<Window> _windows = new ArrayList<>();

	/** The window the run was started in. */
	private Window _windowA;

	/** The run of window A, as {@code ChatProcessor} would have left it while it waits. */
	private AIProcessingState _run;

	/** The future the agent thread is parked on. */
	private CompletableFuture<String> _waiting;

	/** The id of the approval the run is waiting for. */
	private String _requestId;

	/** The run's task id, the key it is registered under. */
	private String _taskId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-who-approves");
			ben = TestPerson.createPerson("ben-who-may-not");
			annasChat = ChatHistoryService.createSession("Annas Chat", anna);
		}

		_windowA = newWindow(anna);

		UserInteractionStore store = new UserInteractionStore();
		_run = new AIProcessingState();
		_taskId = UUID.randomUUID().toString();
		_run.setCurrentTaskId(_taskId);
		_run.setSessionId(chatId());
		_run.setUserInteractionStore(store);
		_windowA.tracker().addProcessingState(_taskId, _run);

		_requestId = UUID.randomUUID().toString();
		_waiting = store.registerApproval(new ApprovalRequest(_requestId, "createObject",
			"call-1", Map.of(), ToolMode.DATA_WRITE, "Creates a contract.", Duration.ofMinutes(30)));

		PendingInteractionRegistry.register(_taskId, anna, _windowA.tracker(), _run);
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

	/** The window that started the run can still answer it - the fix takes nothing away. */
	public void testTheRunsOwnWindowAnswers() throws Exception {
		assertEquals(ChatControl.STATUS_OK, respond(_windowA, anna, "APPROVED"));

		assertEquals("APPROVED", answer());
	}

	/**
	 * A second tab of the same login answers what the first tab's run is waiting for.
	 *
	 * <p>
	 * The bug of #393: window B has its own tracker with no run in it, so the lookup that started
	 * from the tracker found no store and dropped the answer.
	 * </p>
	 */
	public void testASecondWindowAnswers() throws Exception {
		assertEquals(ChatControl.STATUS_OK, respond(newWindow(anna), anna, "APPROVED"));

		assertEquals("The run must have been unparked by the other window's answer.",
			"APPROVED", answer());
	}

	/**
	 * A reloaded page answers what the run before the reload is waiting for.
	 *
	 * <p>
	 * A reload that lands in a new window is the same thing to this servlet as a second tab, with
	 * one difference worth pinning: the window the run belongs to is gone, so nothing but the
	 * account can lead back to the parked run.
	 * </p>
	 */
	public void testAReloadedPageAnswers() throws Exception {
		Window afterReload = newWindow(anna);
		_windowA.tracker().removeProcessingState(_taskId);

		assertEquals(ChatControl.STATUS_OK, respond(afterReload, anna, "REJECTED"));

		assertEquals("REJECTED", answer());
	}

	/** A rejection comment reaches the run from another window as it does from its own. */
	public void testAnotherWindowsRejectionCarriesItsComment() throws Exception {
		Map<String, Object> arguments = answering(_requestId, "REJECTED");
		arguments.put(ChatControl.ARG_COMMENT, "Falscher Vertrag.");

		assertEquals(ChatControl.STATUS_OK, status(newWindow(anna).command(anna, ChatControl.CMD_RESPOND, arguments)));

		assertEquals("REJECTED:Falscher Vertrag.", answer());
	}

	/** Another account may not answer - not from its own window, and not by naming the id. */
	public void testAnotherAccountMayNotAnswer() throws Exception {
		assertEquals(ChatControl.STATUS_IGNORED, respond(newWindow(ben), ben, "APPROVED"));

		assertFalse("Ben's answer must not have unparked Anna's run.", _waiting.isDone());
		assertTrue("Anna's request must still be hers to answer.",
			_run.getUserInteractionStore().isPending(_requestId));
	}

	/** What another account is told is what anyone is told about a request that is not pending. */
	public void testAForeignAnswerLooksLikeALateOne() throws Exception {
		Map<String, Object> foreign =
			newWindow(ben).command(ben, ChatControl.CMD_RESPOND, answering(_requestId, "APPROVED"));

		runEnded();
		Map<String, Object> late =
			_windowA.command(anna, ChatControl.CMD_RESPOND, answering(_requestId, "APPROVED"));

		assertEquals("Telling the two apart would say which request ids exist.", late, foreign);
	}

	/** An answer to a run that has since ended is a no-op, not a failure. */
	public void testALateAnswerIsIgnored() throws Exception {
		runEnded();

		assertEquals(ChatControl.STATUS_IGNORED, respond(_windowA, anna, "APPROVED"));

		assertFalse("Nothing is waiting for this any more.", _waiting.isDone());
	}

	/**
	 * "Approve for this session" is remembered in the run's own chat, not in the answering window.
	 *
	 * <p>
	 * The set the chat's next runs are given is the one of the chat the run was sent in. Recording
	 * the decision against the window that happened to answer would leave the chat asking about the
	 * same tool again.
	 * </p>
	 */
	public void testSessionApprovalIsRememberedInTheRunsChat() throws Exception {
		// What a send gives its run: the state of the chat the window showed.
		HistoryUtils.updateSessionRevision();
		assertTrue(_windowA.tracker().setActiveSessionId(chatId(), anna));
		_run.setChatState(_windowA.tracker().activeChat());
		Window windowB = newWindow(anna);
		Map<String, Object> arguments = answering(_requestId, "APPROVED_FOR_SESSION");
		arguments.put(ChatControl.ARG_TOOL_NAME, "createObject");

		assertEquals(ChatControl.STATUS_OK, status(windowB.command(anna, ChatControl.CMD_RESPOND, arguments)));

		assertTrue("The run's own chat holds the set its next runs are given.",
			_windowA.tracker().activeChat().approvedTools().contains("createObject"));
		assertFalse("The answering window's set is not what the run consults.",
			windowB.tracker().activeChat().approvedTools().contains("createObject"));
	}

	/**
	 * A window that only listed the request is told when somebody else answers it.
	 *
	 * <p>
	 * The window the run belongs to hears the outcome from the run itself
	 * ({@code approval_resolved}), and is deliberately left out here; a window that listed the
	 * request from its {@code run_state} frame has no run to hear it from, and would keep
	 * showing a card nobody is waiting on.
	 * </p>
	 */
	public void testTheOtherWindowsAreToldItWasAnswered() throws Exception {
		Watching watching = watch(new TaskProgressTracker(), anna);
		Watching runsOwnWindow = watch(_windowA.tracker(), anna);
		try {
			assertEquals(ChatControl.STATUS_OK, respond(newWindow(anna), anna, "APPROVED"));

			assertEquals("The window showing the request must be told it is answered.",
				List.of(_requestId), watching.resolved());
			assertEquals("The run's own window hears this from the run.",
				List.of(), runsOwnWindow.resolved());
		} finally {
			watching.close();
			runsOwnWindow.close();
		}
	}

	/** Another account's window is not told what this account answered. */
	public void testAnotherAccountsWindowIsNotTold() throws Exception {
		Watching bens = watch(new TaskProgressTracker(), ben);
		try {
			assertEquals(ChatControl.STATUS_OK, respond(newWindow(anna), anna, "APPROVED"));

			assertEquals(List.of(), bens.resolved());
		} finally {
			bens.close();
		}
	}

	/**
	 * A background subagent's question, asked after the turn that started it has ended, is answered
	 * from any window of the account (issue #695).
	 *
	 * <p>
	 * The run is gone from the register by then, and its store with it; the question waits in the
	 * job's own store, which {@link PendingInteractionRegistry#subagentListener} registered when the
	 * job started.
	 * </p>
	 */
	public void testASubagentsQuestionAfterTheTurnEndedIsAnswered() throws Exception {
		runEnded();
		SubagentJob job = new SubagentJob("sa-1", "researcher", "find X", new CancellationToken());
		SubagentJobRegistry.Listener listener =
			PendingInteractionRegistry.subagentListener(anna, _windowA.tracker(), chatId());
		listener.started(job);
		try {
			UserInteractionRequest question =
				job.interactionStore().createRequest("Which region?", "Clarify", "text", null, false);
			assertEquals("The question names the subagent that asks.",
				new InteractionAsker("sa-1", "researcher"), question.getAskedBy());
			assertEquals("The chat lists the subagent's store for its conversation.",
				List.of(job.interactionStore()), PendingInteractionRegistry.subagentStores(anna, chatId()));

			assertEquals(ChatControl.STATUS_OK, status(newWindow(anna).command(anna, ChatControl.CMD_RESPOND,
				answering(question.getId(), "Nordics"))));

			assertEquals("The subagent must have been unparked by the answer.", "Nordics",
				question.getFuture().get(5, TimeUnit.SECONDS));
		} finally {
			listener.finished(job);
		}
	}

	/** Another account may not answer a subagent's question, and does not see its store. */
	public void testAnotherAccountMayNotAnswerASubagent() throws Exception {
		SubagentJob job = new SubagentJob("sa-2", "researcher", "find X", new CancellationToken());
		SubagentJobRegistry.Listener listener =
			PendingInteractionRegistry.subagentListener(anna, _windowA.tracker(), chatId());
		listener.started(job);
		try {
			UserInteractionRequest question =
				job.interactionStore().createRequest("Which region?", "Clarify", "text", null, false);

			assertEquals(List.of(), PendingInteractionRegistry.subagentStores(ben, chatId()));
			assertEquals(ChatControl.STATUS_IGNORED, status(newWindow(ben).command(ben, ChatControl.CMD_RESPOND,
				answering(question.getId(), "Nordics"))));

			assertFalse("Ben's answer must not have unparked Anna's subagent.", question.getFuture().isDone());
		} finally {
			listener.finished(job);
		}
	}

	/** A subagent that has finished is no longer registered; its leftovers are not answered. */
	public void testAFinishedSubagentIsForgotten() throws Exception {
		SubagentJob job = new SubagentJob("sa-3", "researcher", "find X", new CancellationToken());
		SubagentJobRegistry.Listener listener =
			PendingInteractionRegistry.subagentListener(anna, _windowA.tracker(), chatId());
		int before = PendingInteractionRegistry.subagentCount();
		listener.started(job);
		UserInteractionRequest question =
			job.interactionStore().createRequest("Which region?", "Clarify", "text", null, false);
		listener.finished(job);

		assertEquals(before, PendingInteractionRegistry.subagentCount());
		assertEquals(ChatControl.STATUS_IGNORED, status(_windowA.command(anna, ChatControl.CMD_RESPOND,
			answering(question.getId(), "Nordics"))));
		assertFalse(question.getFuture().isDone());
	}

	/**
	 * A subagent that ends after the window that registered it stopped listening is dropped all the
	 * same: the window's listener is gone by then, and nothing sweeps the register on a poll any more.
	 */
	public void testASubagentEndingAfterItsWindowLeftIsForgotten() throws Exception {
		SubagentJobRegistry registry = new SubagentJobRegistry(1);
		try {
			PendingInteractionRegistry.forgetEndedOf(registry);
			SubagentJobRegistry.Listener window =
				PendingInteractionRegistry.subagentListener(anna, _windowA.tracker(), chatId());
			registry.addListener(window);
			int before = PendingInteractionRegistry.subagentCount();

			CompletableFuture<Void> release = new CompletableFuture<>();
			SubagentJob job = registry.register("researcher", "find X", new CancellationToken());
			registry.spawn(job, j -> {
				release.get(5, TimeUnit.SECONDS);
				return null;
			});
			awaitCount(before + 1);
			assertEquals(List.of(job.interactionStore()),
				PendingInteractionRegistry.subagentStores(anna, chatId()));

			// The window leaves while the subagent works on.
			registry.removeListener(window);
			release.complete(null);

			awaitCount(before);
			assertEquals(List.of(), PendingInteractionRegistry.subagentStores(anna, chatId()));
		} finally {
			registry.cancelAll();
			registry.shutdown();
		}
	}

	private static void awaitCount(int expected) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 5_000;
		while (PendingInteractionRegistry.subagentCount() != expected && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertEquals(expected, PendingInteractionRegistry.subagentCount());
	}

	/** Registers a recording chat UI of the given account for the given window. */
	private static Watching watch(TaskProgressTracker window, Person account) {
		Watching watching = new Watching(window, account);
		ChatEventHandlerRegistry.register(watching);
		return watching;
	}

	/** A chat UI that records nothing but which requests it was told are answered. */
	private static final class Watching implements ChatEventHandler, AutoCloseable {

		private final TaskProgressTracker _window;

		private final Person _account;

		private final List<String> _resolved = new ArrayList<>();

		Watching(TaskProgressTracker window, Person account) {
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
			return true;
		}

		@Override
		public void onEvent(AgentEvent event) {
			// Not what this test asks about.
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
			_resolved.add(event.getRequestId());
		}

		List<String> resolved() {
			return _resolved;
		}

		@Override
		public void close() {
			ChatEventHandlerRegistry.unregister(this);
		}
	}

	/** Tears the run down the way {@code ChatProcessor} tears a finished one down. */
	private void runEnded() {
		PendingInteractionRegistry.unregister(_taskId);
		_run.setLogListener(null);
		_run.setUserInteractionStore(null);
	}

	/** The answer the agent thread would have received, or a failure when it received none. */
	private String answer() throws Exception {
		assertTrue("The run is still parked; the answer never reached it.", _waiting.isDone());
		return _waiting.get(5, TimeUnit.SECONDS);
	}

	/**
	 * A fresh window of the given account, as a second tab or a reloaded page brings one: a
	 * conversation with a tracker of its own, with no run in it.
	 */
	private Window newWindow(Person account) throws Exception {
		Window window = new Window(account, "respond-" + getName() + "-" + _windows.size());
		_windows.add(window);
		return window;
	}

	/** The status the given account's answer to the run's approval, sent from the given window, got. */
	private String respond(Window window, Person caller, String response) {
		return status(window.command(caller, ChatControl.CMD_RESPOND, answering(_requestId, response)));
	}

	/** The arguments of an answer to the given request. */
	private static Map<String, Object> answering(String requestId, String response) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(ChatControl.ARG_REQUEST_ID, requestId);
		arguments.put(ChatControl.ARG_RESPONSE, response);
		return arguments;
	}

	private static String status(Map<String, Object> reply) {
		return (String) reply.get(ChatStatusReply.STATUS__PROP);
	}

	private static String chatId() {
		return annasChat.tHandle().getObjectName().toExternalForm();
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatPendingInteraction.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
