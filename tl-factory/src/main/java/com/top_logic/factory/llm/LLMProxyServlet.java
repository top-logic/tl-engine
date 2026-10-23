/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@link LLMProxyHandler LLM proxy} in the web application of the control application, mounted
 * at {@link LLMProxyHandler#PATH}.
 *
 * <p>
 * Requests reach it through the port of the control application, with all other requests. The
 * {@link LLMProxyService} lets it answer only if {@link LLMProxyService.Config#isServletEnabled()}
 * is set; otherwise it answers with 404, and the proxy is reached through the
 * {@link LLMProxyListener} on a port of its own.
 * </p>
 */
public class LLMProxyServlet extends HttpServlet {

	private final LLMProxyHandler _handler;

	/**
	 * Creates a {@link LLMProxyServlet} using the {@link LLMProxyService} of the application if it
	 * {@link LLMProxyService#servletProxy() serves the servlet}.
	 */
	public LLMProxyServlet() {
		this(LLMProxyService::servletProxy);
	}

	/**
	 * Creates a {@link LLMProxyServlet}.
	 *
	 * @param proxy
	 *        Supplier of the {@link LLMProxy} asked for each request; it may supply {@code null}
	 *        when there is none, which answers requests with 404.
	 */
	public LLMProxyServlet(Supplier<LLMProxy> proxy) {
		_handler = new LLMProxyHandler(proxy);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		_handler.handle(new ServletExchange(req, resp));
	}

	@Override
	public void destroy() {
		_handler.close();
		super.destroy();
	}

	/**
	 * {@link LLMProxyExchange} of a servlet request.
	 */
	private static final class ServletExchange implements LLMProxyExchange {

		private final HttpServletRequest _req;

		private final HttpServletResponse _resp;

		ServletExchange(HttpServletRequest req, HttpServletResponse resp) {
			_req = req;
			_resp = resp;
		}

		@Override
		public String method() {
			return _req.getMethod();
		}

		@Override
		public String path() {
			return _req.getPathInfo();
		}

		@Override
		public String query() {
			return _req.getQueryString();
		}

		@Override
		public String remoteAddress() {
			return _req.getRemoteAddr();
		}

		@Override
		public List<String> headerNames() {
			return Collections.list(_req.getHeaderNames());
		}

		@Override
		public List<String> headers(String name) {
			return Collections.list(_req.getHeaders(name));
		}

		@Override
		public InputStream body() throws IOException {
			return _req.getInputStream();
		}

		@Override
		public void addHeader(String name, String value) {
			_resp.addHeader(name, value);
		}

		@Override
		public void sendHeaders(int status, long length) throws IOException {
			_resp.setStatus(status);
			if (length >= 0) {
				_resp.setContentLengthLong(length);
			} else {
				// Commits status and headers, so that a streamed answer starts at once.
				_resp.flushBuffer();
			}
		}

		@Override
		public OutputStream responseBody() throws IOException {
			return _resp.getOutputStream();
		}
	}

}
