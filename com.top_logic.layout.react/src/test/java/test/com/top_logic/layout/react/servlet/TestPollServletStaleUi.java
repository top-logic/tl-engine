/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import junit.framework.TestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.control.ReactCommandAnswer;
import com.top_logic.layout.react.servlet.PollServlet;
import com.top_logic.layout.react.servlet.ReactServlet;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that {@link PollServlet} marks a poll it refuses for a missing session or an unknown window
 * with {@link ReactServlet#ERROR_CODE_STALE_UI}, and answers a poll of a known window without it.
 */
public class TestPollServletStaleUi extends TestCase {

	private static final String SERVLET = "poll";

	private static final String WINDOW = "vPoll";

	private ServletRunner _runner;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_runner = new ServletRunner();
		_runner.registerServlet(SERVLET, PollServlet.class.getName());
	}

	@Override
	protected void tearDown() throws Exception {
		_runner.shutDown();
		super.tearDown();
	}

	/**
	 * A poll without a session is refused with {@link HttpServletResponse#SC_UNAUTHORIZED} and
	 * {@link ReactServlet#ERROR_CODE_STALE_UI}.
	 */
	public void testNoSession() throws Exception {
		InvocationContext invocation = invocation("?windowName=" + WINDOW + "&peek=true");

		invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, invocation.getResponse().getStatus());
		assertEquals(ReactServlet.ERROR_CODE_STALE_UI, answer(invocation).get(ReactCommandAnswer.FIELD_ERROR_CODE));
	}

	/**
	 * A poll for a window the session does not know is refused with
	 * {@link HttpServletResponse#SC_GONE} and {@link ReactServlet#ERROR_CODE_STALE_UI}.
	 */
	public void testUnknownWindow() throws Exception {
		InvocationContext invocation = invocation("?windowName=" + WINDOW + "&peek=true");
		invocation.getRequest().getSession(true);

		invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());

		assertEquals(HttpServletResponse.SC_GONE, invocation.getResponse().getStatus());
		assertEquals(ReactServlet.ERROR_CODE_STALE_UI, answer(invocation).get(ReactCommandAnswer.FIELD_ERROR_CODE));
	}

	/**
	 * A poll with a token the server does not know is refused with
	 * {@link HttpServletResponse#SC_GONE} and {@link ReactServlet#ERROR_CODE_STALE_UI}.
	 */
	public void testUnknownToken() throws Exception {
		InvocationContext invocation = invocation("?token=unknown&after=1");

		invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());

		assertEquals(HttpServletResponse.SC_GONE, invocation.getResponse().getStatus());
		assertEquals(ReactServlet.ERROR_CODE_STALE_UI, answer(invocation).get(ReactCommandAnswer.FIELD_ERROR_CODE));
	}

	/**
	 * A peek of a window the session knows is answered with {@link HttpServletResponse#SC_OK} and
	 * the number of the last event, without an error code.
	 */
	public void testKnownWindowPeek() throws Exception {
		InvocationContext invocation = invocation("?windowName=" + WINDOW + "&peek=true");
		HttpSession session = invocation.getRequest().getSession(true);
		ReactWindowRegistry.forSession(session).getOrCreateQueue(WINDOW);

		invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());

		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		Map<?, ?> answer = answer(invocation);
		assertTrue("Peek names the last event: " + answer, answer.containsKey("last"));
		assertFalse("No error code on success: " + answer, answer.containsKey(ReactCommandAnswer.FIELD_ERROR_CODE));
	}

	private InvocationContext invocation(String query) throws Exception {
		ServletUnitClient client = _runner.newClient();
		// Lets the client read the body of an error answer.
		client.setExceptionsThrownOnErrorStatus(false);
		return client.newInvocation(new GetMethodWebRequest("http://ignore/" + SERVLET + query));
	}

	private static Map<?, ?> answer(InvocationContext invocation) throws Exception {
		return (Map<?, ?>) JSON.fromString(invocation.getServletResponse().getText());
	}

}
