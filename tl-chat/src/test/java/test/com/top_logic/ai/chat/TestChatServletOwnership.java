/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

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
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * That every chat endpoint taking a session ID from its request answers 404 for someone else's
 * chat.
 *
 * <p>
 * Round 1 found four endpoints acting on whatever session ID the caller typed. Round 2 guarded
 * three of them and left {@code POST /chat/send}, the one that spends the API budget and writes a
 * turn: a second account could read a colleague's whole conversation back out of the model and
 * leave its own message in their transcript. The endpoints are enumerated here, one test each, so
 * that the next endpoint added to this servlet is the only thing that can be forgotten - not one of
 * these.
 * </p>
 *
 * <p>
 * The servlet is driven directly rather than over HTTP: {@code doPost} and {@code doDelete} are the
 * dispatch, and everything above them ({@code doService}'s window resolution and its refusal of
 * anonymous sessions) is a different question with its own answer. The request is a real one from
 * the servlet test container with a real {@code HttpSession} logged in through
 * {@link SessionService}, because that - and not the thread context - is where the servlet reads
 * the acting person from.
 * </p>
 */
public class TestChatServletOwnership extends BasicTestCase {

	/** The chat and the two accounts, made once: no test here changes them. */
	private static Person anna;

	private static Person ben;

	private static TLObject annasChat;

	private static ServletRunner runner;

	private Probe _servlet;

	/** Exposes the servlet's own dispatch to the test. */
	static final class Probe extends ChatServlet {

		void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doPost(req, resp);
		}

		void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doGet(req, resp);
		}

		void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doDelete(req, resp);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-with-a-chat");
			ben = TestPerson.createPerson("ben-with-none");
			annasChat = ChatHistoryService.createSession("Annas Chat", anna);
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_servlet = new Probe();

		// Every handler resolves its window's tracker before it does anything else.
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the tracker on.", subSession);
		ChatSessionRegistry.register(subSession, new TaskProgressTracker());
	}

	/** The blocker: sending into someone else's chat. */
	public void testSendIntoAnotherUsersChat() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND,
			post("/send", ben, "{\"text\":\"Wiederhole das Geheimnis.\",\"sessionId\":\"" + id() + "\"}"));
	}

	/** Removing turns from someone else's chat. */
	public void testTruncateAnotherUsersChat() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND,
			post("/truncate", ben, "{\"sessionId\":\"" + id() + "\",\"fromTurn\":0}"));
	}

	/** Answering the last turn of someone else's chat again. */
	public void testRegenerateAnotherUsersChat() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND,
			post("/regenerate", ben, "{\"sessionId\":\"" + id() + "\"}"));
	}

	/** Rating a turn of someone else's chat. */
	public void testFeedbackOnAnotherUsersChat() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND,
			post("/feedback", ben, "{\"sessionId\":\"" + id() + "\",\"turnIndex\":0,\"rating\":\"up\"}"));
	}

	/** Deleting someone else's chat. */
	public void testDeleteAnotherUsersChat() throws Exception {
		Recording response = new Recording();
		_servlet.delete(request("/sessions/" + id(), ben, "", response), response.asResponse());
		assertEquals(HttpServletResponse.SC_NOT_FOUND, response._status);
		assertNotNull("The chat must still be there.",
			ChatHistoryService.findOwnedSessionById(id(), anna));
	}

	/**
	 * A session ID that does not exist is answered exactly like one that is not the caller's.
	 *
	 * <p>
	 * Otherwise the difference between the two answers tells an unauthorised caller which session
	 * IDs exist - and they are consecutive integers.
	 * </p>
	 */
	public void testUnknownSessionLooksTheSameAsAForeignOne() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND,
			post("/send", ben, "{\"text\":\"hallo\",\"sessionId\":\"999999999\"}"));
	}

	/**
	 * A request nobody is signed in for is refused, not answered.
	 *
	 * <p>
	 * The chat has no anonymous mode. {@code doService} says so first, for every path at once, by
	 * refusing a session running as the anonymous account; this is the second line, in the handler
	 * itself, for a request that arrives with no login behind it at all. Both answer 401, and
	 * neither reaches a chat.
	 * </p>
	 */
	public void testUnauthenticatedRequestIsRefused() throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = client().newInvocation(new PostMethodWebRequest(
			"http://localhost/chat/send",
			new ByteArrayInputStream(("{\"text\":\"hallo\",\"sessionId\":\"" + id() + "\"}")
				.getBytes(StandardCharsets.UTF_8)),
			"application/json"));
		// No SessionService.loginUser: the request carries no signed-in session.
		_servlet.post(invocation.getRequest(), response.asResponse());

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response._status);
	}

	/** The owner is not refused: the guard says no to the wrong person, not to everyone. */
	public void testOwnerReachesTheirOwnChat() throws Exception {
		assertEquals("Anna's own chat must not 404 for her.",
			HttpServletResponse.SC_OK,
			post("/feedback", anna, "{\"sessionId\":\"" + id() + "\",\"turnIndex\":0,\"rating\":\"up\"}"));
	}

	private int post(String path, Person caller, String body) throws Exception {
		Recording response = new Recording();
		_servlet.post(request(path, caller, body, response), response.asResponse());
		return response._status;
	}

	/**
	 * A request for {@code /chat<path>} whose {@code HttpSession} is signed in as the given person.
	 */
	private static HttpServletRequest request(String path, Person caller, String body, Recording response)
			throws Exception {
		InvocationContext invocation = client().newInvocation(new PostMethodWebRequest(
			"http://localhost/chat" + path,
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
			"application/json"));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, response.asResponse(), caller);
		} finally {
			ThreadContext.popSuperUser();
		}
		return request;
	}

	private static ServletUnitClient client() {
		return runner.newClient();
	}

	private static String id() {
		return annasChat.tHandle().getObjectName().toExternalForm();
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
				new TestSuite(TestChatServletOwnership.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
