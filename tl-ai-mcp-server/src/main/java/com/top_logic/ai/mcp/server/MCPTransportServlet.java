/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import io.modelcontextprotocol.spec.HttpHeaders;

/**
 * Static servlet that delegates MCP protocol requests to the current transport provider from
 * {@link MCPServerService}.
 *
 * <p>
 * This servlet is statically registered in web-fragment.xml and remains active throughout the
 * application lifecycle. It dynamically dispatches requests to the transport provider of the
 * currently active {@link MCPServerService} instance, allowing the service to be restarted
 * without servlet re-registration issues.
 * </p>
 *
 * @author Bernhard Haumacher
 */
public class MCPTransportServlet extends HttpServlet {

	/**
	 * Answer for a request that arrives while {@link MCPServerService} is not started.
	 *
	 * <p>
	 * The servlet is registered statically, so {@code /mcp/*} is mapped in every application that has
	 * this module on its classpath - including the ones that leave the service switched off, which is
	 * the shipped state. Naming the switch in the answer keeps that state diagnosable: an
	 * administrator who did not mean to disable the endpoint learns what to turn on, instead of
	 * debugging a bare 503.
	 * </p>
	 *
	 * <p>
	 * It also names the second step, because turning the module on is not enough: the endpoint serves
	 * no request it cannot attribute to an account, so an application also has to configure an
	 * authentication for it (see the developer article {@code ai/security/mcp-endpoint}).
	 * </p>
	 */
	static final String SERVICE_DISABLED_MESSAGE =
		"MCP server not available: MCPServerService is not started. This endpoint is off by default,"
			+ " because publishing it is a deployment decision; enable the module"
			+ " com.top_logic.ai.mcp.server.MCPServerService$Module to switch it on, and configure an"
			+ " authentication for it - it serves no request it cannot attribute to an account. See"
			+ " the article ai/security/mcp-endpoint in the developer documentation.";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MCPServerService service = MCPServerService.getInstance();
		if (service == null) {
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, SERVICE_DISABLED_MESSAGE);
			return;
		}

		// Delegate to the transport provider from the current service instance
		service.noteSessionRequest(req.getHeader(HttpHeaders.MCP_SESSION_ID));
		service.getTransportProvider().service(endpointRequest(req), resp);
		// An initialize request has its session id only in the answer.
		service.noteSessionRequest(resp.getHeader(HttpHeaders.MCP_SESSION_ID));
	}

	/**
	 * The request as the transport accepts it: a request to the endpoint with a trailing slash
	 * ({@code <context>/mcp/}) is presented as a request to the endpoint itself
	 * ({@code <context>/mcp}).
	 *
	 * <p>
	 * The transport serves only a request whose URI ends with {@link MCPServerService#MCP_ENDPOINT}
	 * and answers 404 to anything else. The servlet mapping {@code /mcp/*} also delivers the URL
	 * with a trailing slash, and that is a form people type when they configure a client by hand.
	 * Both forms name the same endpoint, so both are served.
	 * </p>
	 *
	 * <p>
	 * A redirect to the form without the slash is no alternative: a client is not required to repeat
	 * the body of a POST, nor its {@code Authorization} header, after a redirect, so the first
	 * request of a session would fail anyway.
	 * </p>
	 *
	 * <p>
	 * Any other path below the endpoint ({@code <context>/mcp/other}) is passed on unchanged and
	 * stays unserved.
	 * </p>
	 *
	 * @param req
	 *        The request as the container delivered it.
	 * @return The given request, or a wrapper of it without the trailing slash.
	 */
	static HttpServletRequest endpointRequest(HttpServletRequest req) {
		if (!"/".equals(req.getPathInfo())) {
			return req;
		}
		String uri = req.getRequestURI();
		if (!uri.endsWith("/")) {
			return req;
		}
		return new HttpServletRequestWrapper(req) {
			@Override
			public String getRequestURI() {
				return uri.substring(0, uri.length() - 1);
			}

			@Override
			public StringBuffer getRequestURL() {
				StringBuffer url = super.getRequestURL();
				int last = url.length() - 1;
				if (last >= 0 && url.charAt(last) == '/') {
					url.setLength(last);
				}
				return url;
			}

			@Override
			public String getPathInfo() {
				return null;
			}

			@Override
			public String getPathTranslated() {
				return null;
			}
		};
	}
}
