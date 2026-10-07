/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import junit.framework.Test;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.util.CachePolicy;
import com.top_logic.util.TopLogicServlet;

/**
 * Test that {@link TopLogicServlet} answers a request whose parameters cannot be parsed with
 * {@link HttpServletResponse#SC_BAD_REQUEST}, without processing it any further.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTopLogicServletBadRequest extends BasicTestCase {

	/**
	 * Message of the exception by which {@link UnparsableRequest} refuses its parameters.
	 */
	static final String PARSE_ERROR = "400: Unable to parse URI query";

	/**
	 * {@link TopLogicServlet} that finds no session and records that a request reached its
	 * {@link #handleNoSession(HttpServletRequest, HttpServletResponse)}.
	 */
	public static class TestingServlet extends TopLogicServlet {

		boolean _handled;

		@Override
		protected boolean isCookieCheckRequired() {
			return false;
		}

		@Override
		protected TLSessionContext getSession(HttpServletRequest request, HttpServletResponse response) {
			return null;
		}

		@Override
		protected void handleNoSession(HttpServletRequest request, HttpServletResponse response) {
			_handled = true;
		}

	}

	/**
	 * Request whose parameters the servlet container refuses to parse, as for the query string
	 * <code>?q=50%</code>.
	 */
	public static class UnparsableRequest extends HttpServletRequestWrapper {

		/**
		 * Creates a {@link UnparsableRequest}.
		 */
		public UnparsableRequest(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getParameter(String name) {
			throw parseError();
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			throw parseError();
		}

		@Override
		public Enumeration<String> getParameterNames() {
			throw parseError();
		}

		@Override
		public String[] getParameterValues(String name) {
			throw parseError();
		}

		private static RuntimeException parseError() {
			return new IllegalArgumentException(PARSE_ERROR);
		}

	}

	/**
	 * Response recording the status code of an error sent through it.
	 */
	public static class RecordingResponse extends HttpServletResponseWrapper {

		int _errorStatus;

		/**
		 * Creates a {@link RecordingResponse}.
		 */
		public RecordingResponse(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendError(int sc) throws IOException {
			_errorStatus = sc;
			super.sendError(sc);
		}

		@Override
		public void sendError(int sc, String msg) throws IOException {
			_errorStatus = sc;
			super.sendError(sc, msg);
		}

	}

	private static final String SERVLET_NAME = "servlet";

	private ServletUnitClient _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ServletRunner runner = new ServletRunner();
		runner.registerServlet(SERVLET_NAME, TestingServlet.class.getName());
		_client = runner.newClient();
	}

	@Override
	protected void tearDown() throws Exception {
		_client = null;

		super.tearDown();
	}

	/**
	 * A request whose parameters cannot be parsed is answered with
	 * {@link HttpServletResponse#SC_BAD_REQUEST} and not processed any further.
	 */
	public void testUnparsableParametersRejected() throws Exception {
		InvocationContext invocation = invocation();
		TestingServlet servlet = (TestingServlet) invocation.getServlet();
		RecordingResponse response = new RecordingResponse(invocation.getResponse());

		servlet.service(new UnparsableRequest(invocation.getRequest()), response);

		assertEquals("An unparsable request is a client error.", HttpServletResponse.SC_BAD_REQUEST,
			response._errorStatus);
		assertFalse("An unparsable request must not be processed.", servlet._handled);
	}

	/**
	 * A well-formed request is processed as usual.
	 */
	public void testWellFormedRequestProcessed() throws Exception {
		InvocationContext invocation = invocation();
		TestingServlet servlet = (TestingServlet) invocation.getServlet();
		RecordingResponse response = new RecordingResponse(invocation.getResponse());

		servlet.service(invocation.getRequest(), response);

		assertEquals("A well-formed request is not answered with an error.", 0, response._errorStatus);
		assertTrue("A well-formed request must be processed.", servlet._handled);
	}

	private InvocationContext invocation() throws IOException, ServletException {
		return _client.newInvocation(new GetMethodWebRequest("http://ignore/" + SERVLET_NAME + "?filter=all&q=50"));
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(TestTopLogicServletBadRequest.class, CachePolicy.Module.INSTANCE));
	}

}
