/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.git;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.proxy.WorkspaceProxy;
import com.top_logic.factory.proxy.WorkspaceProxyServlet;

/**
 * The tests of {@link TestGitRoute} against the proxy in the web application, the
 * {@link WorkspaceProxyServlet} in an embedded Jetty, mapped to {@link GitRoute#DEFAULT_PATH}.
 */
public class TestGitRouteServlet extends TestGitRoute {

	private static final String CONTEXT_PATH = "/app";

	private Server _jetty;

	@Override
	protected String startProxy(WorkspaceProxy proxy) throws Exception {
		_jetty = new Server();
		ServerConnector connector = new ServerConnector(_jetty);
		connector.setHost(LOOPBACK);
		connector.setPort(0);
		_jetty.addConnector(connector);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath(CONTEXT_PATH);
		ServletHolder proxyHolder = new ServletHolder(new WorkspaceProxyServlet(() -> proxy));
		// The proxy answers asynchronously.
		proxyHolder.setAsyncSupported(true);
		context.addServlet(proxyHolder, GitRoute.DEFAULT_PATH + "/*");
		_jetty.setHandler(context);
		_jetty.start();
		return "http://" + LOOPBACK + ":" + connector.getLocalPort() + CONTEXT_PATH + GitRoute.DEFAULT_PATH;
	}

	@Override
	protected void stopProxy() throws Exception {
		_jetty.stop();
	}

	/**
	 * The servlet container resolves or refuses dot and empty segments before the servlet sees
	 * the path; what reaches the route is checked by {@link #testOtherRequestsNotFound()}.
	 */
	@Override
	public void testDotSegmentsNotFound() {
		// Not the route's decision in a servlet container.
	}

}
