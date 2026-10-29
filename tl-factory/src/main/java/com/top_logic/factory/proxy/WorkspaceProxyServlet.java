/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.io.IOException;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@link WorkspaceProxyHandler workspace proxy} in the web application of the control
 * application.
 *
 * <p>
 * The servlet is mapped to the {@link ProxyRoute#getPath() paths of the routes} it serves (in the
 * {@code web-fragment.xml} of the module), each with {@code /*}; the path of a request is its path
 * within the web application. Requests reach it through the port of the control application, with
 * all other requests. The {@link WorkspaceProxyService} lets it answer only if
 * {@link WorkspaceProxyService.Config#isServletEnabled()} is set; otherwise it answers with 404, and
 * the proxy is reached through the {@link WorkspaceProxyListener} on a port of its own.
 * </p>
 */
public class WorkspaceProxyServlet extends HttpServlet {

	private final WorkspaceProxyHandler _handler;

	/**
	 * Creates a {@link WorkspaceProxyServlet} using the {@link WorkspaceProxyService} of the
	 * application if it {@link WorkspaceProxyService#servletProxy() serves the servlet}.
	 */
	public WorkspaceProxyServlet() {
		this(WorkspaceProxyService::servletProxy);
	}

	/**
	 * Creates a {@link WorkspaceProxyServlet}.
	 *
	 * @param proxy
	 *        Supplier of the {@link WorkspaceProxy} asked for each request; it may supply
	 *        {@code null} when there is none, which answers requests with 404.
	 */
	public WorkspaceProxyServlet(Supplier<? extends WorkspaceProxy> proxy) {
		_handler = new WorkspaceProxyHandler(proxy);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		_handler.handle(new ServletProxyExchange(req, resp));
	}

}
