/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.stream;

import java.io.IOException;
import java.util.regex.Pattern;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.util.TLContextManager;

/**
 * Delivers the events of a React window by polling, for a browser whose event stream a proxy holds
 * back or refuses - see {@link PolledWindow}. Mapped to {@code /window-poll}.
 *
 * <p>
 * <b>Temporary workaround (#940)</b>, together with {@code script/tl-window-stream.js}, which
 * decides in the browser when to poll. Remove both when the engine polls by itself.
 * </p>
 *
 * <ul>
 * <li>{@code GET ?windowName=<w>&start=true} - with the session cookie: connects a
 * {@link PolledWindow} to the window's queue instead of its stream, set up as the engine's
 * {@code SSEServlet} sets up a stream, and answers the first events (the full state of the controls)
 * with the token for the next polls.</li>
 * <li>{@code GET ?token=<t>&after=<n>} - without the cookie: the events after number {@code n}.</li>
 * <li>{@code POST ?windowName=<w>&probe=<nonce>} - puts a small patch for the listener
 * {@value #PROBE_LISTENER} on the window's stream, so that the browser can tell whether its stream
 * delivers.</li>
 * </ul>
 *
 * <p>
 * Answers {@code 401} without a session and {@code 410} for a window or token the server does not
 * know: the page has to be loaded again.
 * </p>
 */
public class WindowPollServlet extends HttpServlet {

	/** The listener id the probe patches are addressed to; no control has it. */
	public static final String PROBE_LISTENER = "tl-window-probe";

	private static final Pattern NONCE = Pattern.compile("[A-Za-z0-9]{1,32}");

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String token = request.getParameter("token");
		if (token != null && !token.isEmpty()) {
			PolledWindow window = PolledWindow.forToken(token);
			if (window == null) {
				response.sendError(HttpServletResponse.SC_GONE, "Unknown window.");
				return;
			}
			window.poll(startPoll(request, response), after(request));
			return;
		}

		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}
		String windowName = request.getParameter("windowName");
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		SSEUpdateQueue queue = windowName == null ? null : registry.getQueue(windowName);
		if (queue == null) {
			response.sendError(HttpServletResponse.SC_GONE, "Unknown window.");
			return;
		}
		registry.noteActivity(session);
		TLSessionContext sessionContext = SessionService.getInstance().getSession(session);
		PolledWindow window = PolledWindow.create(queue);
		response.setHeader("X-TL-Window-Token", window.token());
		AsyncContext context = startPoll(request, response);
		// Connecting sends the full state of the window's controls, which renders them: the window's
		// interaction and sub-session, as for the connection of a stream.
		try (Interaction interaction = registry.beginInteraction()) {
			TLContextManager.inInteraction(sessionContext, getServletContext(), request, response, () -> {
				DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
				ReactWindowReplay.installSubSession(displayContext, windowName);
				queue.setConnection(window);
			});
			queue.setWindowContext(windowName, sessionContext, registry);
		}
		window.poll(context, 0);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}
		String windowName = request.getParameter("windowName");
		String nonce = request.getParameter("probe");
		SSEUpdateQueue queue =
			windowName == null ? null : ReactWindowRegistry.forSession(session).getQueue(windowName);
		if (queue == null || nonce == null || !NONCE.matcher(nonce).matches()) {
			response.sendError(HttpServletResponse.SC_GONE, "Unknown window.");
			return;
		}
		queue.enqueue(PatchEvent.create().setControlId(PROBE_LISTENER).setPatch("{\"nonce\":\"" + nonce + "\"}"));
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	private static long after(HttpServletRequest request) {
		try {
			return Long.parseLong(request.getParameter("after"));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	private static AsyncContext startPoll(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache, no-store");
		AsyncContext context = request.startAsync();
		// The window answers within POLL_HOLD_MILLIS; the container's limit is only the backstop.
		context.setTimeout(PolledWindow.POLL_HOLD_MILLIS + 20_000);
		return context;
	}

}
