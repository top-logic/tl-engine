/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.ChatServlet;
import com.top_logic.ai.chat.api.messages.InteractionResolvedEvent;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
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
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * That a pending approval or question belongs to the account, not to the browser window whose run
 * asked it (issue #393).
 *
 * <p>
 * A run parks its thread on a future in the {@link UserInteractionStore} of one
 * {@link AIProcessingState}, and that state hangs on the {@link TaskProgressTracker} of one window.
 * Before #393 {@code POST /chat/respond} looked the request up in the tracker of the window the
 * answer arrived from, so a second tab - or a reloaded page, which is a new window - could see the
 * pending request through {@code GET /chat/interactions} but could not answer it: the answer was
 * logged as late and dropped, and the run stayed parked until its timeout.
 * </p>
 *
 * <p>
 * Each test here gives window A a run with a pending approval, then answers from a
 * <em>different</em> tracker - which is what a second tab, a reload and a new login all are to this
 * servlet - and asserts against the future the agent thread would be parked on. Ownership is the
 * other half: an answer from another account must leave that future exactly as it was.
 * </p>
 *
 * <p>
 * The servlet is driven directly, exactly as {@link TestChatServletOwnership} drives it, and for
 * the same reason. The run is set up the way {@code ChatProcessor} sets one up - a state, a store,
 * and the registration that names the responder - because starting a real agent would need a
 * provider, a model and a tool catalog to ask about something the servlet decides on its own.
 * </p>
 */
public class TestChatPendingInteraction extends BasicTestCase {

	private static Person anna;

	private static Person ben;

	private static TLObject annasChat;

	private static ServletRunner runner;

	private TestChatServletOwnership.Probe _servlet;

	/** The window the run was started in. */
	private TaskProgressTracker _windowA;

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
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_servlet = new TestChatServletOwnership.Probe();

		_windowA = newWindow();

		UserInteractionStore store = new UserInteractionStore();
		_run = new AIProcessingState();
		_taskId = UUID.randomUUID().toString();
		_run.setCurrentTaskId(_taskId);
		_run.setSessionId(chatId());
		_run.setUserInteractionStore(store);
		_windowA.addProcessingState(_taskId, _run);

		_requestId = UUID.randomUUID().toString();
		_waiting = store.registerApproval(new ApprovalRequest(_requestId, "createObject",
			"call-1", Map.of(), ToolMode.DATA_WRITE, "Creates a contract.", Duration.ofMinutes(30)));

		PendingInteractionRegistry.register(_taskId, anna, _windowA, _run);

		// Every handler resolves its window's tracker before it does anything else; which window
		// that is, is what each test decides.
		inWindow(_windowA);
	}

	@Override
	protected void tearDown() throws Exception {
		PendingInteractionRegistry.unregister(_taskId);
		super.tearDown();
	}

	/** The window that started the run can still answer it - the fix takes nothing away. */
	public void testTheRunsOwnWindowAnswers() throws Exception {
		assertEquals(HttpServletResponse.SC_OK, respond(anna, "APPROVED"));

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
		inWindow(newWindow());

		assertEquals(HttpServletResponse.SC_OK, respond(anna, "APPROVED"));

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
		TaskProgressTracker afterReload = newWindow();
		_windowA.removeProcessingState(_taskId);
		inWindow(afterReload);

		assertEquals(HttpServletResponse.SC_OK, respond(anna, "REJECTED"));

		assertEquals("REJECTED", answer());
	}

	/** A rejection comment reaches the run from another window as it does from its own. */
	public void testAnotherWindowsRejectionCarriesItsComment() throws Exception {
		inWindow(newWindow());

		assertEquals(HttpServletResponse.SC_OK,
			post("{\"requestId\":\"" + _requestId + "\",\"response\":\"REJECTED\","
				+ "\"comment\":\"Falscher Vertrag.\"}", anna));

		assertEquals("REJECTED:Falscher Vertrag.", answer());
	}

	/** Another account may not answer - not from its own window, and not by naming the id. */
	public void testAnotherAccountMayNotAnswer() throws Exception {
		inWindow(newWindow());

		assertEquals(HttpServletResponse.SC_OK, respond(ben, "APPROVED"));

		assertFalse("Ben's answer must not have unparked Anna's run.", _waiting.isDone());
		assertTrue("Anna's request must still be hers to answer.",
			_run.getUserInteractionStore().isPending(_requestId));
	}

	/** What another account is told is what anyone is told about a request that is not pending. */
	public void testAForeignAnswerLooksLikeALateOne() throws Exception {
		inWindow(newWindow());
		String foreign = body(respondRecording(ben, "APPROVED"));

		runEnded();
		inWindow(_windowA);
		String late = body(respondRecording(anna, "APPROVED"));

		assertEquals("Telling the two apart would say which request ids exist.", late, foreign);
	}

	/** An answer to a run that has since ended is a no-op, not a failure. */
	public void testALateAnswerIsIgnored() throws Exception {
		runEnded();

		assertEquals(HttpServletResponse.SC_OK, respond(anna, "APPROVED"));

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
		assertTrue(_windowA.setActiveSessionId(chatId(), anna));
		_run.setChatState(_windowA.activeChat());
		TaskProgressTracker windowB = newWindow();
		inWindow(windowB);

		assertEquals(HttpServletResponse.SC_OK,
			post("{\"requestId\":\"" + _requestId + "\",\"response\":\"APPROVED_FOR_SESSION\","
				+ "\"toolName\":\"createObject\"}", anna));

		assertTrue("The run's own chat holds the set its next runs are given.",
			_windowA.activeChat().approvedTools().contains("createObject"));
		assertFalse("The answering window's set is not what the run consults.",
			windowB.activeChat().approvedTools().contains("createObject"));
	}

	/**
	 * A window that only listed the request is told when somebody else answers it.
	 *
	 * <p>
	 * The window the run belongs to hears the outcome from the run itself
	 * ({@code approval_resolved}), and is deliberately left out here; a window that listed the
	 * request through {@code GET /chat/interactions} has no run to hear it from, and would keep
	 * showing a card nobody is waiting on.
	 * </p>
	 */
	public void testTheOtherWindowsAreToldItWasAnswered() throws Exception {
		Watching watching = watch(newWindow(), anna);
		Watching runsOwnWindow = watch(_windowA, anna);
		try {
			inWindow(newWindow());

			assertEquals(HttpServletResponse.SC_OK, respond(anna, "APPROVED"));

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
		Watching bens = watch(newWindow(), ben);
		try {
			inWindow(newWindow());

			assertEquals(HttpServletResponse.SC_OK, respond(anna, "APPROVED"));

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
			PendingInteractionRegistry.subagentListener(anna, _windowA, chatId());
		listener.started(job);
		try {
			UserInteractionRequest question =
				job.interactionStore().createRequest("Which region?", "Clarify", "text", null, false);
			assertEquals("The question names the subagent that asks.",
				new InteractionAsker("sa-1", "researcher"), question.getAskedBy());
			assertEquals("The chat lists the subagent's store for its conversation.",
				List.of(job.interactionStore()), PendingInteractionRegistry.subagentStores(anna, chatId()));

			inWindow(newWindow());
			assertEquals(HttpServletResponse.SC_OK,
				post("{\"requestId\":\"" + question.getId() + "\",\"response\":\"Nordics\"}", anna));

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
			PendingInteractionRegistry.subagentListener(anna, _windowA, chatId());
		listener.started(job);
		try {
			UserInteractionRequest question =
				job.interactionStore().createRequest("Which region?", "Clarify", "text", null, false);

			assertEquals(List.of(), PendingInteractionRegistry.subagentStores(ben, chatId()));
			inWindow(newWindow());
			assertEquals(HttpServletResponse.SC_OK,
				post("{\"requestId\":\"" + question.getId() + "\",\"response\":\"Nordics\"}", ben));

			assertFalse("Ben's answer must not have unparked Anna's subagent.", question.getFuture().isDone());
		} finally {
			listener.finished(job);
		}
	}

	/** A subagent that has finished is no longer registered; its leftovers are not answered. */
	public void testAFinishedSubagentIsForgotten() throws Exception {
		SubagentJob job = new SubagentJob("sa-3", "researcher", "find X", new CancellationToken());
		SubagentJobRegistry.Listener listener =
			PendingInteractionRegistry.subagentListener(anna, _windowA, chatId());
		int before = PendingInteractionRegistry.subagentCount();
		listener.started(job);
		UserInteractionRequest question =
			job.interactionStore().createRequest("Which region?", "Clarify", "text", null, false);
		listener.finished(job);

		assertEquals(before, PendingInteractionRegistry.subagentCount());
		assertEquals(HttpServletResponse.SC_OK,
			post("{\"requestId\":\"" + question.getId() + "\",\"response\":\"Nordics\"}", anna));
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
				PendingInteractionRegistry.subagentListener(anna, _windowA, chatId());
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

	private int respond(Person caller, String response) throws Exception {
		return respondRecording(caller, response)._status;
	}

	private Recording respondRecording(Person caller, String response) throws Exception {
		return postRecording("{\"requestId\":\"" + _requestId + "\",\"response\":\"" + response + "\"}",
			caller);
	}

	private int post(String body, Person caller) throws Exception {
		return postRecording(body, caller)._status;
	}

	private Recording postRecording(String body, Person caller) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = client(body);
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, response.asResponse(), caller);
		} finally {
			ThreadContext.popSuperUser();
		}
		_servlet.post(request, response.asResponse());
		return response;
	}

	private static InvocationContext client(String body) throws Exception {
		return runner.newClient().newInvocation(new PostMethodWebRequest(
			"http://localhost/chat/respond",
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
			"application/json"));
	}

	private static String body(Recording response) {
		return response._body.toString();
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
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
