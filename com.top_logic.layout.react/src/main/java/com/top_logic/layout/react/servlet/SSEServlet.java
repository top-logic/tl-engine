/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.util.TLContextManager;

/**
 * Servlet that establishes an SSE (Server-Sent Events) connection for delivering React state
 * updates to the client.
 *
 * <p>
 * The servlet validates the session, sets SSE headers, starts an asynchronous context, and
 * registers the connection with the window's {@link SSEUpdateQueue}, see
 * {@link ReactWindowRegistry#connectQueue(String)}.
 * </p>
 *
 * <p>
 * A connection never creates a window: the window is created by rendering its page. A window the
 * registry does not know - it was torn down after its client went away, or its session was replaced
 * - is answered as gone ({@link HttpServletResponse#SC_GONE}). The client then switches to polling,
 * whose first poll is answered as gone as well, upon which the client reloads the page.
 * </p>
 */
public class SSEServlet extends HttpServlet {

	/**
	 * The comment that opens a stream, padded to 2 KB: a proxy that forwards a response only once
	 * it holds a few kilobytes releases the start of the stream at once. A proxy that holds back
	 * more than that is answered by polling, see {@link PollServlet}.
	 */
	private static final String CONNECTED_COMMENT = ": connected" + " ".repeat(2048) + "\n\n";

	/**
	 * The header in which the browser names the last event a stream brought when it reconnects the
	 * stream by itself.
	 */
	public static final String LAST_EVENT_ID_HEADER = "Last-Event-ID";

	/**
	 * The parameter in which the client names the last event it received when it opens a stream
	 * again by itself, which sends no {@link #LAST_EVENT_ID_HEADER}. A page that has received no
	 * event leaves it out.
	 */
	public static final String AFTER_PARAM = "after";

	/**
	 * The number of the last event the client of the given stream request received, from the
	 * {@link #LAST_EVENT_ID_HEADER}, else from the {@link #AFTER_PARAM}.
	 *
	 * @return The number, negative if the request names none: the client is a page that has not
	 *         received an event yet, or a number cannot be read.
	 */
	public static long lastEventId(HttpServletRequest request) {
		long header = parseNumber(request.getHeader(LAST_EVENT_ID_HEADER));
		if (header >= 0) {
			return header;
		}
		long after = parseNumber(request.getParameter(AFTER_PARAM));
		// No event of a page has the number 0: a page starts above it.
		return after > 0 ? after : -1;
	}

	private static long parseNumber(String value) {
		if (value == null || value.isEmpty()) {
			return -1;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
		if (registry.getQueue(windowName) == null) {
			Logger.info("SSEServlet: window '" + windowName + "' is unknown, answered as gone.", SSEServlet.class);
			response.sendError(HttpServletResponse.SC_GONE, "Unknown window.");
			return;
		}

		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("X-Accel-Buffering", "no");

		AsyncContext asyncContext = request.startAsync();
		asyncContext.setTimeout(0);
		Logger.info("SSEServlet: windowName='" + windowName + "', registry@" + System.identityHashCode(registry),
			SSEServlet.class);
		TLSessionContext sessionContext = SessionService.getInstance().getSession(session);
		long lastEventId = lastEventId(request);

		// Sending the state of the whole tree attaches it, which changes the display like any command
		// does, so it is an interaction. It ends before this request hands its thread back: the
		// connection outlives the request, the interaction does not.
		SSEUpdateQueue queue;
		boolean[] accepted = { false };
		try (Interaction interaction = registry.beginInteraction()) {
			queue = registry.connectQueue(windowName);
			if (queue == null) {
				// Torn down since it was looked up above.
				Logger.info("SSEServlet: window '" + windowName + "' was torn down, stream closed.",
					SSEServlet.class);
				asyncContext.complete();
				return;
			}
			Logger.info("SSEServlet: queue@" + System.identityHashCode(queue) + " for windowName='" + windowName + "'",
				SSEServlet.class);

			// Establishing the connection sends the state of the whole tree, and rendering a control
			// attaches it: a control catching up with its model as it attaches - a table re-reading
			// its rows - works on the knowledge base, and a control resolving its labels needs the
			// user's locale. Both need the interaction with the window's subsession that every other
			// request touching the tree has.
			TLContextManager.inInteraction(sessionContext, getServletContext(), request, response, () -> {
				DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
				ReactWindowReplay.installSubSession(displayContext, windowName);
				accepted[0] = queue.setConnection(asyncContext, lastEventId);
			});
			if (!accepted[0]) {
				// The queue closed the stream: its window is torn down.
				return;
			}
			queue.setWindowContext(windowName, sessionContext, registry);
		}

		asyncContext.addListener(new StreamEndListener(queue, asyncContext));

		try {
			PrintWriter writer = response.getWriter();
			writer.write(CONNECTED_COMMENT);
			writer.flush();
		} catch (IOException ex) {
			Logger.warn("Failed to write SSE connection comment.", ex, SSEServlet.class);
			queue.clearConnection(asyncContext);
		}
	}

	/**
	 * Clears the connection of a window's {@link SSEUpdateQueue} when its stream ends.
	 *
	 * <p>
	 * The container may keep the listener with an idle connection after the stream ended; it must
	 * not keep the window with it. The listener therefore lets go of the queue as soon as the stream
	 * has ended.
	 * </p>
	 */
	static final class StreamEndListener implements AsyncListener {

		private volatile SSEUpdateQueue _queue;

		private final AsyncContext _asyncContext;

		/**
		 * Creates a {@link StreamEndListener}.
		 *
		 * @param queue
		 *        The queue whose connection is the stream of the given context.
		 * @param asyncContext
		 *        The context of the stream.
		 */
		StreamEndListener(SSEUpdateQueue queue, AsyncContext asyncContext) {
			_queue = queue;
			_asyncContext = asyncContext;
		}

		@Override
		public void onComplete(AsyncEvent event) {
			streamEnded();
		}

		@Override
		public void onTimeout(AsyncEvent event) {
			streamEnded();
		}

		@Override
		public void onError(AsyncEvent event) {
			streamEnded();
		}

		@Override
		public void onStartAsync(AsyncEvent event) {
			// Nothing to do.
		}

		private void streamEnded() {
			SSEUpdateQueue queue = _queue;
			if (queue != null) {
				queue.clearConnection(_asyncContext);
				_queue = null;
			}
		}
	}

}
