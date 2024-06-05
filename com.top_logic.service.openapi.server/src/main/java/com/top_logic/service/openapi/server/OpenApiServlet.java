/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.util.RunnableEx2;
import com.top_logic.util.TopLogicServlet;

/**
 * {@link HttpServlet} endpoint for the {@link OpenApiServer}.
 * 
 * @see OpenApiServer#handleRequest(HttpServletRequest, HttpServletResponse)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OpenApiServlet extends HttpServlet {

	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		/* The type parameters are necessary here. Without them, Eclipse reports an error. */
		TopLogicServlet.<IOException, ServletException> withSessionIdLogMark(req,
			() -> serviceWithLogMark(req, resp));
	}

	/**
	 * The implementation of {@link #serviceWithLogMark(HttpServletRequest, HttpServletResponse)}
	 * but with an enclosing log mark for the session id. See
	 * {@link TopLogicServlet#withSessionIdLogMark(HttpServletRequest, RunnableEx2)} for details.
	 */
	protected void serviceWithLogMark(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
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
