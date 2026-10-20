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
 * registers the connection with the window's {@link SSEUpdateQueue} obtained from the
 * {@link ReactWindowRegistry}.
 * </p>
 */
public class SSEServlet extends HttpServlet {

	/**
	 * The comment that opens a stream, padded to 2 KB: a proxy that forwards a response only once
	 * it holds a few kilobytes releases the start of the stream at once. A proxy that holds back
	 * more than that is answered by polling, see {@link PollServlet}.
	 */
	private static final String CONNECTED_COMMENT = ": connected" + " ".repeat(2048) + "\n\n";

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

		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("X-Accel-Buffering", "no");

		AsyncContext asyncContext = request.startAsync();
		asyncContext.setTimeout(0);

		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		// Establishing the connection restarts the session's inactivity timeout just as any other
		// request does, so a control counting down to the end of the session has to hear about it.
		// Nothing further arrives through this connection: the heartbeats travel down the one
		// request opened here and never touch the session again.
		registry.noteActivity(session);
		Logger.info("SSEServlet: windowName='" + windowName + "', registry@" + System.identityHashCode(registry),
			SSEServlet.class);
		TLSessionContext sessionContext = SessionService.getInstance().getSession(session);

		// Sending the state of the whole tree attaches it, which changes the display like any command
		// does, so it is an interaction. It ends before this request hands its thread back: the
		// connection outlives the request, the interaction does not.
		SSEUpdateQueue queue;
		try (Interaction interaction = registry.beginInteraction()) {
			queue = registry.getOrCreateQueue(windowName);
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
				queue.setConnection(asyncContext);
			});
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
