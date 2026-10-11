/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The chat's "auto-approve" switch is a setting of the conversation (#658).
 *
 * <p>
 * The switch set the mode on the window's tracker only. The conversation stored the mode only when a
 * turn was saved, and every window that opened the chat read the stored mode back - so a chat whose
 * last turn ran with auto-approve came back with it on after the user had turned it off, and the next
 * tool call ran without asking. Turning auto-approve off is how a user has every write confirmed; it
 * must not be undone by anything but the switch.
 * </p>
 */
public class TestApprovalModeSwitch extends BasicTestCase {

	private static Person anna;

	private static Person ben;

	private static ServletRunner runner;

	private Probe _servlet;

	private TaskProgressTracker _window;

	private TLObject _chat;

	private final List<ChatEventHandler> _handlers = new ArrayList<>();

	/** Exposes the servlet's own dispatch to the test. */
	static final class Probe extends ChatServlet {

		void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doPost(req, resp);
		}

		void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doGet(req, resp);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-switches-approval");
			ben = TestPerson.createPerson("ben-switches-approval");
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_servlet = new Probe();
		_chat = ChatHistoryService.createSession("Mit Freigabe", anna);
		HistoryUtils.updateSessionRevision();

		// The window the requests come from.
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the tracker on.", subSession);
		_window = new TaskProgressTracker();
		ChatSessionRegistry.register(subSession, null, _window);

		// A turn ran with auto-approve on, and was saved with it.
		ranATurn(_window, ApprovalMode.AUTO_APPROVE);
	}

	@Override
	protected void tearDown() throws Exception {
		for (ChatEventHandler handler : _handlers) {
			ChatEventHandlerRegistry.unregister(handler);
		}
		super.tearDown();
	}

	/** Turning auto-approve off reaches every window that opens the chat afterwards: a new login. */
	public void testSwitchingOffIsStoredAtOnce() throws Exception {
		assertEquals(HttpServletResponse.SC_OK, switchTo(ApprovalMode.MANUAL, anna).status());

		assertEquals(ApprovalMode.MANUAL, modeANewWindowOpens());
	}

	/**
	 * A window that opened the chat before the switch was turned off still holds the old mode in its
	 * tracker. Saving its metadata - a rating, a turn, moving to another chat - must not turn
	 * auto-approve back on.
	 */
	public void testAnOlderWindowDoesNotTurnItBackOn() throws Exception {
		TaskProgressTracker older = new TaskProgressTracker();
		older.setActiveSessionId(id(), anna);
		assertEquals(ApprovalMode.AUTO_APPROVE, older.getApprovalMode());

		switchTo(ApprovalMode.MANUAL, anna);
		older.getSessionManager().persistActiveSession();

		assertEquals(ApprovalMode.MANUAL, modeANewWindowOpens());
	}

	/**
	 * A window of the account that shows the chat is told the new mode at once, so its switch does
	 * not go on claiming a mode its runs no longer have, and its next request reads the chat again.
	 */
	public void testAWindowShowingTheChatIsTold() throws Exception {
		TaskProgressTracker other = new TaskProgressTracker();
		other.setActiveSessionId(id(), anna);
		Listening handler = listen(other, anna);

		switchTo(ApprovalMode.MANUAL, anna);

		assertEquals(List.of(ApprovalMode.MANUAL), handler._modes);
		other.setActiveSessionId(id(), anna);
		assertEquals("Its next request runs with the mode the chat now has.",
			ApprovalMode.MANUAL, other.getApprovalMode());
	}

	/**
	 * Opening a chat answers with its mode, which is what the browser's switch shows. A window that
	 * was opened before - showing the mode of its page load - would otherwise show the switch off
	 * while the chat's runs approve by themselves.
	 */
	public void testOpeningAChatAnswersItsMode() throws Exception {
		TaskProgressTracker newWindow = new TaskProgressTracker();
		ChatSessionRegistry.register(TLContextManager.getSubSession(), null, newWindow);
		assertEquals(ApprovalMode.MANUAL, newWindow.getApprovalMode());

		Answer opened = call(new GetMethodWebRequest("http://localhost/chat/sessions/" + id()), anna, true);

		assertEquals(HttpServletResponse.SC_OK, opened.status());
		assertTrue("The chat is opened with the mode it stores: " + opened.body(),
			opened.body().contains("\"approvalMode\":\"AUTO_APPROVE\""));
	}

	/** Somebody else's chat is not switched, and the answer says nothing about it existing. */
	public void testSomeoneElsesChatIsNotSwitched() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND, switchTo(ApprovalMode.MANUAL, ben).status());

		assertEquals(ApprovalMode.AUTO_APPROVE, modeANewWindowOpens());
	}

	/**
	 * Auto-approve is a setting of one chat (#808): a window that ran a chat with it on and then
	 * opens a new chat runs that chat manual, and the new chat's first save stores it manual.
	 */
	public void testANewChatStartsManual() throws Exception {
		assertEquals(ApprovalMode.AUTO_APPROVE, _window.getApprovalMode());

		TLObject created = ChatHistoryService.createSession("Neu", anna);
		HistoryUtils.updateSessionRevision();
		_window.setActiveSessionId(idOf(created), anna);
		assertEquals(ApprovalMode.MANUAL, _window.getApprovalMode());

		_window.getSessionManager().persistActiveSession();
		HistoryUtils.updateSessionRevision();
		assertTrue("The first save stores the new chat manual: " + ChatHistoryService.getMetadataJson(created),
			ChatHistoryService.getMetadataJson(created).contains("\"approvalMode\":\"MANUAL\""));
		TaskProgressTracker fresh = new TaskProgressTracker();
		fresh.setActiveSessionId(idOf(created), anna);
		assertEquals(ApprovalMode.MANUAL, fresh.getApprovalMode());
	}

	/**
	 * A conversation that has no chat yet keeps the mode in the browser, which names it when it
	 * creates the chat: the chat stores it at once.
	 */
	public void testAChatIsCreatedWithTheModeItNames() throws Exception {
		Answer answer = call(new PostMethodWebRequest("http://localhost/chat/sessions",
			new ByteArrayInputStream("{\"title\":\"Neu\",\"approvalMode\":\"AUTO_APPROVE\"}"
				.getBytes(StandardCharsets.UTF_8)), "application/json"), anna, false);
		assertEquals(HttpServletResponse.SC_OK, answer.status());
		Matcher id = Pattern.compile("\"id\":\"([^\"]+)\"").matcher(answer.body());
		assertTrue("The answer names the created chat: " + answer.body(), id.find());
		HistoryUtils.updateSessionRevision();

		TaskProgressTracker fresh = new TaskProgressTracker();
		fresh.setActiveSessionId(id.group(1), anna);
		assertEquals(ApprovalMode.AUTO_APPROVE, fresh.getApprovalMode());
	}

	/** A switch that names no chat has nothing to store the mode with, and is refused. */
	public void testASwitchWithoutAChatIsRefused() throws Exception {
		Answer answer = call(post("{\"mode\":\"AUTO_APPROVE\"}"), anna, false);

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, answer.status());
	}

	// --- Fixture ---

	private void ranATurn(TaskProgressTracker window, ApprovalMode mode) {
		window.setActiveSessionId(id(), anna);
		window.setApprovalMode(mode);
		window.getSessionManager().persistActiveSession();
		HistoryUtils.updateSessionRevision();
	}

	private ApprovalMode modeANewWindowOpens() {
		HistoryUtils.updateSessionRevision();
		TaskProgressTracker fresh = new TaskProgressTracker();
		fresh.setActiveSessionId(id(), anna);
		return fresh.getApprovalMode();
	}

	private Answer switchTo(ApprovalMode mode, Person caller) throws Exception {
		Answer answer =
			call(post("{\"mode\":\"" + mode.name() + "\",\"sessionId\":\"" + id() + "\"}"), caller, false);
		HistoryUtils.updateSessionRevision();
		return answer;
	}

	private static WebRequest post(String body) {
		return new PostMethodWebRequest("http://localhost/chat/approval-mode",
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), "application/json");
	}

	private Answer call(WebRequest webRequest, Person caller, boolean get) throws Exception {
		Answer answer = new Answer();
		HttpServletRequest request = runner.newClient().newInvocation(webRequest).getRequest();
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, answer.asResponse(), caller);
		} finally {
			ThreadContext.popSuperUser();
		}
		if (get) {
			_servlet.get(request, answer.asResponse());
		} else {
			_servlet.post(request, answer.asResponse());
		}
		return answer;
	}

	private Listening listen(TaskProgressTracker tracker, Person account) {
		Listening handler = new Listening(tracker, account);
		ChatEventHandlerRegistry.register(handler);
		_handlers.add(handler);
		return handler;
	}

	private String id() {
		return idOf(_chat);
	}

	private static String idOf(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	/** A chat UI of another window, recording the modes it is told. */
	private static final class Listening implements ChatEventHandler {

		private final TaskProgressTracker _tracker;

		private final Person _account;

		final List<ApprovalMode> _modes = new ArrayList<>();

		Listening(TaskProgressTracker tracker, Person account) {
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
			// Not what this test is about.
		}

		@Override
		public void sendMessageComplete(MessageCompleteEvent event) {
			// Not what this test is about.
		}

		@Override
		public void sendProviderError(ProviderErrorEvent event) {
			// Not what this test is about.
		}

		@Override
		public void sendApprovalMode(ApprovalMode mode) {
			_modes.add(mode);
		}
	}

	/** What the servlet answered. */
	private static final class Answer {

		private int _status = HttpServletResponse.SC_OK;

		private final StringWriter _body = new StringWriter();

		private HttpServletResponse _view;

		int status() {
			return _status;
		}

		String body() {
			return _body.toString();
		}

		HttpServletResponse asResponse() {
			if (_view == null) {
				_view = (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
					new Class<?>[] { HttpServletResponse.class }, new Handler());
			}
			return _view;
		}

		private final class Handler implements InvocationHandler {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws IOException {
				switch (method.getName()) {
					case "sendError":
					case "setStatus":
						_status = ((Integer) args[0]).intValue();
						return null;
					case "getWriter":
						return new PrintWriter(_body, true);
					default:
						return null;
				}
			}
		}
	}

	/** The knowledge base, the chat model and a login. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestApprovalModeSwitch.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
