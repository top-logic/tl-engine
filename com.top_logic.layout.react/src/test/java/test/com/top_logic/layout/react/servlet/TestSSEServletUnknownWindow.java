/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import junit.framework.TestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.layout.react.servlet.SSEServlet;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that {@link SSEServlet} answers an event stream for a window its session does not know -
 * one that was torn down - as gone, and creates no window for it.
 */
public class TestSSEServletUnknownWindow extends TestCase {

	private static final String SERVLET = "events";

	private static final String WINDOW = "vGone";

	/**
	 * A stream for an unknown window is refused with {@link HttpServletResponse#SC_GONE}.
	 */
	public void testUnknownWindowIsGone() throws Exception {
		ServletRunner runner = new ServletRunner();
		try {
			runner.registerServlet(SERVLET, SSEServlet.class.getName());
			InvocationContext invocation = runner.newClient()
				.newInvocation(new GetMethodWebRequest("http://ignore/" + SERVLET + "?windowName=" + WINDOW));
			HttpSession session = invocation.getRequest().getSession(true);

			invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());

			assertEquals(HttpServletResponse.SC_GONE, invocation.getResponse().getStatus());
			assertNull("No window must be created by a connection.",
				ReactWindowRegistry.forSession(session).getWindow(WINDOW));
		} finally {
			runner.shutDown();
		}
	}

}
