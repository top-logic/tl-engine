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
import com.top_logic.layout.react.servlet.ReactServlet;
import com.top_logic.layout.react.servlet.SSEServlet;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that {@link SSEServlet} answers an event stream for a window its session does not know -
 * one that was torn down - as gone, and creates no window for it, and that it marks a stream it
 * refuses for a missing session or window with {@link ReactServlet#ERROR_CODE_STALE_UI}.
 */
public class TestSSEServletUnknownWindow extends TestCase {

	private static final String SERVLET = "events";

	private static final String WINDOW = "vGone";

	/**
	 * A stream for an unknown window is refused with {@link HttpServletResponse#SC_GONE} and
	 * {@link ReactServlet#ERROR_CODE_STALE_UI}.
	 */
	public void testUnknownWindowIsGone() throws Exception {
		ServletRunner runner = new ServletRunner();
		try {
			runner.registerServlet(SERVLET, SSEServlet.class.getName());
			ServletUnitClient client = runner.newClient();
			// Lets the client read the body of an error answer.
			client.setExceptionsThrownOnErrorStatus(false);
			InvocationContext invocation = client
				.newInvocation(new GetMethodWebRequest("http://ignore/" + SERVLET + "?windowName=" + WINDOW));
			HttpSession session = invocation.getRequest().getSession(true);

			invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());

			assertEquals(HttpServletResponse.SC_GONE, invocation.getResponse().getStatus());
			assertEquals(ReactServlet.ERROR_CODE_STALE_UI, errorCode(invocation));
			assertNull("No window must be created by a connection.",
				ReactWindowRegistry.forSession(session).getWindow(WINDOW));
		} finally {
			runner.shutDown();
		}
	}

	/**
	 * A stream without a session is refused with {@link HttpServletResponse#SC_UNAUTHORIZED} and
	 * {@link ReactServlet#ERROR_CODE_STALE_UI}.
	 */
	public void testNoSessionIsStaleUi() throws Exception {
		ServletRunner runner = new ServletRunner();
		try {
			runner.registerServlet(SERVLET, SSEServlet.class.getName());
			ServletUnitClient client = runner.newClient();
			// Lets the client read the body of an error answer.
			client.setExceptionsThrownOnErrorStatus(false);
			InvocationContext invocation = client
				.newInvocation(new GetMethodWebRequest("http://ignore/" + SERVLET + "?windowName=" + WINDOW));

			invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());

			assertEquals(HttpServletResponse.SC_UNAUTHORIZED, invocation.getResponse().getStatus());
			assertEquals(ReactServlet.ERROR_CODE_STALE_UI, errorCode(invocation));
		} finally {
			runner.shutDown();
		}
	}

	/**
	 * The {@link ReactCommandAnswer#FIELD_ERROR_CODE} of the JSON answer of the given invocation.
	 */
	private static Object errorCode(InvocationContext invocation) throws Exception {
		Map<?, ?> answer = (Map<?, ?>) JSON.fromString(invocation.getServletResponse().getText());
		return answer.get(ReactCommandAnswer.FIELD_ERROR_CODE);
	}

}
