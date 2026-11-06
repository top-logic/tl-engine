/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.InteractionContext;
import com.top_logic.model.search.expr.config.operations.RequestFunctions;
import com.top_logic.util.NoContextServlet;

/**
 * {@link HttpServlet} endpoint for the {@link OpenApiServer}.
 * 
 * <p>
 * A request is handled in an {@link InteractionContext} carrying the HTTP request, so that the
 * implementations of the operations see the request, e.g. a TL-Script implementation through the
 * {@link RequestFunctions}. The interaction gets a sub-session of its own; no HTTP session is
 * created for it.
 * </p>
 * 
 * @see OpenApiServer#handleRequest(HttpServletRequest, HttpServletResponse)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OpenApiServlet extends NoContextServlet {

	@Override
	protected void doService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getPathInfo() == null) {
			// Redirect "/myApp/api" to "/myApp/api/"
			resp.sendRedirect(req.getRequestURI() + "/");
			return;
		}
		if (!OpenApiServer.Module.INSTANCE.isActive()) {
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OpenAPI service not started.");
			return;
		}
		OpenApiServer.getInstance().handleRequest(req, resp);
	}

}
