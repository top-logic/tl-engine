/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.servlet;

import java.io.IOException;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.util.TLContextManager;

/**
 * Delivers the events of a window to a client whose event stream ({@link SSEServlet}) does not
 * arrive - a proxy holds it back or refuses it.
 *
 * <p>
 * {@code GET react-api/poll?after=<n>} answers the events after number {@code n}, see
 * {@link SSEUpdateQueue#poll(AsyncContext, long, boolean)}. The first poll of a window names it
 * ({@code windowName}), sends the session cookie and asks for the full state ({@code full=true}); it
 * is set up like the connection of a stream, because the full state renders controls. Every later
 * poll carries the token of the window ({@code token}) instead of the cookie, so that polling does
 * not keep the session alive.
 * </p>
 *
 * <p>
 * {@code peek=true} answers only the number of the last event written, without changing how the
 * client is served: the client asks it to check that its stream delivers.
 * </p>
 *
 * <p>
 * Answers: {@code 401} without a session, {@code 410} for a window or token the server no longer
 * knows - the page has to be loaded again -, {@code 409} for a token poll that needs the full state,
 * which the client then asks for with the cookie.
 * </p>
 */
public class PollServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		long after;
		try {
			after = Long.parseLong(request.getParameter("after"));
		} catch (NumberFormatException ex) {
			after = 0;
		}
		String token = request.getParameter("token");
		if (token != null && !token.isEmpty()) {
			SSEUpdateQueue queue = SSEUpdateQueue.forPollToken(token);
			if (queue == null) {
				response.sendError(HttpServletResponse.SC_GONE, "Unknown window.");
				return;
			}
			if (queue.needsFullState(after)) {
				response.sendError(HttpServletResponse.SC_CONFLICT, "Full state required.");
				return;
			}
			queue.poll(startPoll(request, response), after, false);
			return;
		}

		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}
		String windowName = request.getParameter("windowName");
		if (windowName == null || windowName.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing windowName parameter.");
			return;
		}
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		SSEUpdateQueue queue = registry.getQueue(windowName);
		if (queue == null) {
			response.sendError(HttpServletResponse.SC_GONE, "Unknown window.");
			return;
		}
		if ("true".equals(request.getParameter("peek"))) {
			// The client checks whether its stream delivers: the number of the last event, nothing more.
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Cache-Control", "no-cache, no-store");
			response.getWriter().write("{\"last\":" + queue.lastSeq() + "}");
			return;
		}
		// A request of the session like any other, see SSEServlet.
		registry.noteActivity(session);
		TLSessionContext sessionContext = SessionService.getInstance().getSession(session);
		queue.getPollToken();
		AsyncContext context = startPoll(request, response);
		long position = after;
		boolean full = "true".equals(request.getParameter("full"));
		// The full state renders controls, which needs the window's interaction and sub-session, as
		// the connection of a stream does.
		try (Interaction interaction = registry.beginInteraction()) {
			TLContextManager.inInteraction(sessionContext, getServletContext(), request, response, () -> {
				DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
				ReactWindowReplay.installSubSession(displayContext, windowName);
				queue.poll(context, position, full);
			});
			queue.setWindowContext(windowName, sessionContext, registry);
		}
	}

	private static AsyncContext startPoll(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache, no-store");
		AsyncContext context = request.startAsync();
		// The queue answers within POLL_HOLD_MILLIS; the container's limit is only the backstop.
		context.setTimeout(SSEUpdateQueue.POLL_HOLD_MILLIS + 20_000);
		return context;
	}

}
