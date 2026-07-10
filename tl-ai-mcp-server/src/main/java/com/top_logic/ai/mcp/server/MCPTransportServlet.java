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

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MCPServerService service = MCPServerService.getInstance();
		if (service == null) {
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "MCP server not available");
			return;
		}

		// Delegate to the transport provider from the current service instance
		service.getTransportProvider().service(req, resp);
	}
}
