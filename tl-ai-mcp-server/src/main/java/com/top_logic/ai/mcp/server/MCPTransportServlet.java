/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
	 */
	static final String SERVICE_DISABLED_MESSAGE =
		"MCP server not available: MCPServerService is not started. This endpoint is off by default,"
			+ " because it authenticates nobody (issue #261); enable the module"
			+ " com.top_logic.ai.mcp.server.MCPServerService$Module to switch it on.";

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
		service.getTransportProvider().service(req, resp);
	}
}
