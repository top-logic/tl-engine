/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.Logger;

/**
 * Servlet that establishes an SSE (Server-Sent Events) connection for delivering React state
 * updates to the client.
 *
 * <p>
 * The servlet validates the session, sets SSE headers, starts an asynchronous context, and
 * registers the connection with the session's {@link SSEUpdateQueue}.
 * </p>
 */
public class SSEServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}

		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("X-Accel-Buffering", "no");

		AsyncContext asyncContext = request.startAsync();
		asyncContext.setTimeout(0);

		SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
		queue.addConnection(asyncContext);

		asyncContext.addListener(new AsyncListener() {
			@Override
			public void onComplete(AsyncEvent event) {
				queue.removeConnection(asyncContext);
			}

			@Override
			public void onTimeout(AsyncEvent event) {
				queue.removeConnection(asyncContext);
			}

			@Override
			public void onError(AsyncEvent event) {
				queue.removeConnection(asyncContext);
			}

			@Override
			public void onStartAsync(AsyncEvent event) {
				// Nothing to do.
			}
		});

		try {
			PrintWriter writer = response.getWriter();
			writer.write(": connected\n\n");
			writer.flush();
		} catch (IOException ex) {
			Logger.warn("Failed to write SSE connection comment.", ex, SSEServlet.class);
			queue.removeConnection(asyncContext);
		}
	}

}
