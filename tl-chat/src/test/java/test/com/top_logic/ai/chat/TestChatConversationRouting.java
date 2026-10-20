/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import java.io.ByteArrayInputStream;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.ai.chat.api.ChatServlet;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * That a window showing two conversations has a {@link TaskProgressTracker} for each, and that a
 * request reaches the one it names.
 *
 * <p>
 * A window can show more than one conversation - a page's chat beside a {@code <chat-dock>}'s, two
 * {@code <agent-chat>} elements, a chat in a dialog over the one on the page. Every {@code /chat/*}
 * call used to carry a {@code windowId} and nothing else, so the servlet resolved one tracker per
 * window: the two conversations shared one active session and one processing state, the second one
 * to send took the first one's event stream, and the first waited for an answer that never arrived.
 * Every request now also names its conversation, and this is what says that the name is obeyed.
 * </p>
 *
 * <p>
 * {@code doService} is driven here rather than {@code doPost}, because the conversation is resolved
 * there: it is the one place that sees the request, and the handlers read back what it found. That
 * is also why this test builds a real window id - the sub-session's registry key - instead of a
 * made-up one.
 * </p>
 */
public class TestChatConversationRouting extends BasicTestCase {

	private static final String PAGE = "chat";

	private static final String DOCK = "dock";

	private static Person user;

	private static ServletRunner runner;

	private Probe _servlet;

	private ServletUnitClient _client;

	private TLSubSessionContext _subSession;

	private String _windowId;

	/** Exposes the servlet's own entry point, which is where the conversation is resolved. */
	static final class Probe extends ChatServlet {

		void dispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doService(req, resp);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (user == null) {
			user = TestPerson.createPerson("user-with-two-conversations");
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_servlet = new Probe();

		// Sign in first, and read the window only afterwards: a login replaces the session's
		// sub-sessions, so a window id taken before it names nothing the servlet can resolve.
		_client = runner.newClient();
		Recording login = new Recording();
		InvocationContext invocation = _client.newInvocation(new GetMethodWebRequest("http://localhost/chat/updates"));
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(invocation.getRequest(), login.asResponse(), user);
		} finally {
			ThreadContext.popSuperUser();
		}
		_subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the trackers on.", _subSession);
		// The chat has no anonymous mode, and doService says so before it dispatches; the sub-session
		// the test hands it therefore has to be somebody's.
		_subSession.setPerson(user);
		// A window id is the key a sub-session is registered under in its session, which in the
		// application a layout window does on login. Nothing here starts one, so the test registers
		// its own sub-session under a name it then sends as the window id.
		TLSessionContext session = TLContextManager.getSession();
		_windowId = "test-window-" + System.identityHashCode(this);
		session.setIfAbsent(_windowId, _subSession);
		assertSame("The window id must resolve back to the sub-session the trackers hang on.",
			_subSession, ChatSessionRegistry.resolveSubSession(session, _windowId));
	}

	/** Two conversations rendered in one window are two trackers, not one. */
	public void testEachConversationOfAWindowHasItsOwnTracker() {
		TaskProgressTracker page = register(PAGE);
		TaskProgressTracker dock = register(DOCK);

		assertNotSame("The page's chat and the dock's must not share one tracker.", page, dock);
		assertSame(page, ChatSessionRegistry.tracker(_subSession, PAGE));
		assertSame(dock, ChatSessionRegistry.tracker(_subSession, DOCK));
		assertTrue("Both conversations belong to the window.",
			ChatSessionRegistry.trackers(_subSession).containsAll(java.util.List.of(page, dock)));
	}

	/**
	 * A request that names no conversation addresses the window's unnamed one.
	 *
	 * <p>
	 * That is what a host that shows one chat per window may send: it has no chat id to state. The
	 * classic {@code chat-client.js} was such a host until #618 replaced it.
	 * </p>
	 */
	public void testARequestNamingNoConversationGetsTheUnnamedOne() {
		TaskProgressTracker unnamed = register(null);

		assertSame(unnamed, ChatSessionRegistry.tracker(_subSession, ""));
		assertSame(unnamed, ChatSessionRegistry.tracker(_subSession, "   "));
	}

	/** A conversation this window never rendered resolves to nothing. */
	public void testAConversationNobodyRenderedIsUnknown() {
		register(PAGE);

		assertNull(ChatSessionRegistry.tracker(_subSession, "never-rendered"));
	}

	/**
	 * The servlet acts on the conversation the request names, and on no other.
	 *
	 * <p>
	 * {@code POST /chat/approval-mode} is the probe because it writes one field of the addressed
	 * tracker, besides the chat it names. With one tracker per window, switching the dock to
	 * auto-approve switched the page's chat with it - which is the same collision that let the two
	 * take each other's runs.
	 * </p>
	 */
	public void testTheServletActsOnTheNamedConversationOnly() throws Exception {
		TaskProgressTracker page = register(PAGE);
		TaskProgressTracker dock = register(DOCK);
		page.setApprovalMode(ApprovalMode.MANUAL);
		dock.setApprovalMode(ApprovalMode.MANUAL);
		TLObject chat = ChatHistoryService.createSession("Im Dock", user);
		HistoryUtils.updateSessionRevision();
		String chatId = chat.tHandle().getObjectName().toExternalForm();
		// The switch sits in a conversation that shows the chat; it does not open the chat itself.
		assertTrue(dock.setActiveSessionId(chatId, user));

		assertEquals(HttpServletResponse.SC_OK, signedInPost("/approval-mode", DOCK,
			"{\"mode\":\"AUTO_APPROVE\",\"sessionId\":\"" + chatId + "\"}")._status);

		assertEquals("The dock's conversation was the one addressed.",
			ApprovalMode.AUTO_APPROVE, dock.getApprovalMode());
		assertEquals("The page's conversation was not addressed and must not have changed.",
			ApprovalMode.MANUAL, page.getApprovalMode());
	}

	/**
	 * Rating an answer and switching auto-approve act on the chat they name, and leave the chat the
	 * conversation shows where it is.
	 *
	 * <p>
	 * Both used to switch the conversation to the named chat before they wrote, so a rating or a
	 * switch sent for a chat the conversation had moved away from pulled the window back to it.
	 * </p>
	 */
	public void testActingOnAnotherChatKeepsTheShownOne() throws Exception {
		TaskProgressTracker page = register(PAGE);
		String shown = idOf(ChatHistoryService.createSession("Gezeigt", user));
		String cached = idOf(ChatHistoryService.createSession("Vorher gezeigt", user));
		String unheld = idOf(ChatHistoryService.createSession("Nie geladen", user));
		HistoryUtils.updateSessionRevision();
		// The window held "cached" before it moved on to "shown".
		assertTrue(page.setActiveSessionId(cached, user));
		assertTrue(page.setActiveSessionId(shown, user));

		assertEquals(HttpServletResponse.SC_OK, signedInPost("/approval-mode", PAGE,
			"{\"mode\":\"AUTO_APPROVE\",\"sessionId\":\"" + cached + "\"}")._status);
		assertEquals(HttpServletResponse.SC_OK, signedInPost("/approval-mode", PAGE,
			"{\"mode\":\"AUTO_APPROVE\",\"sessionId\":\"" + unheld + "\"}")._status);
		assertEquals(HttpServletResponse.SC_OK, signedInPost("/feedback", PAGE,
			"{\"sessionId\":\"" + unheld + "\",\"turnIndex\":0,\"rating\":\"up\"}")._status);
		HistoryUtils.updateSessionRevision();

		assertEquals("The window still shows the chat it showed.", shown, page.getActiveSessionId());
		assertEquals("The shown chat's mode is untouched.", ApprovalMode.MANUAL, page.getApprovalMode());
		assertEquals("The window's copy of the switched chat follows the switch.",
			ApprovalMode.AUTO_APPROVE, page.getSessionManager().getChatState(cached).approvalMode());
		assertNull("A chat the window did not hold is not loaded into it.",
			page.getSessionManager().getChatState(unheld));
		String stored = ChatHistoryService.getMetadataJson(ChatHistoryService.findOwnedSessionById(unheld, user));
		assertTrue("The rating is stored with the chat it names: " + stored, stored.contains("\"rating\":\"up\""));
		assertTrue("The rating keeps the stored mode: " + stored, stored.contains("\"approvalMode\":\"AUTO_APPROVE\""));
	}

	private static String idOf(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	/** A request naming a conversation this window does not show is refused, not served. */
	public void testAnUnknownConversationIsRefusedAsAStaleWindow() throws Exception {
		register(PAGE);

		assertEquals(HttpServletResponse.SC_CONFLICT, get("/updates", "never-rendered")._status);
	}

	private TaskProgressTracker register(String conversation) {
		TaskProgressTracker tracker = new TaskProgressTracker();
		tracker.setWindowId(_windowId);
		ChatSessionRegistry.register(_subSession, conversation, tracker);
		return tracker;
	}

	private Recording get(String path, String conversation) throws Exception {
		Recording response = new Recording();
		_servlet.dispatch(_client.newInvocation(new GetMethodWebRequest(url(path, conversation))).getRequest(),
			response.asResponse());
		return response;
	}

	private Recording post(String path, String conversation, String body) throws Exception {
		Recording response = new Recording();
		HttpServletRequest request = _client.newInvocation(new PostMethodWebRequest(url(path, conversation),
			new ByteArrayInputStream(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
			"application/json")).getRequest();
		_servlet.dispatch(request, response.asResponse());
		return response;
	}

	/**
	 * A POST of a signed-in account, for a request that names a chat: the servlet asks the HTTP
	 * session whose chat it is. The login opens a session of its own, so this test's window is
	 * registered in it as well.
	 */
	private Recording signedInPost(String path, String conversation, String body) throws Exception {
		Recording response = new Recording();
		HttpServletRequest request = _client.newInvocation(new PostMethodWebRequest(url(path, conversation),
			new ByteArrayInputStream(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
			"application/json")).getRequest();
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, response.asResponse(), user);
		} finally {
			ThreadContext.popSuperUser();
		}
		SessionService.getInstance().getSession(SessionService.getInstance().getSession(request))
			.setIfAbsent(_windowId, _subSession);
		_subSession.setPerson(user);
		_servlet.dispatch(request, response.asResponse());
		return response;
	}

	/** A {@code /chat<path>} URL of one conversation in this test's window. */
	private String url(String path, String conversation) {
		return "http://localhost/chat" + path + "?windowId=" + _windowId + "&conversation=" + conversation;
	}

	/** Records what the servlet answered. */
	static final class Recording {

		int _status = HttpServletResponse.SC_OK;

		final StringWriter _body = new StringWriter();

		private HttpServletResponse _view;

		HttpServletResponse asResponse() {
			if (_view == null) {
				_view = (HttpServletResponse) Proxy.newProxyInstance(
					HttpServletResponse.class.getClassLoader(),
					new Class<?>[] { HttpServletResponse.class },
					new Handler());
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
						return new PrintWriter(_body);
					default:
						return null;
				}
			}
		}
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatConversationRouting.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
