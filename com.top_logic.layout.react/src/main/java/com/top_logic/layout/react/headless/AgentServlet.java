/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.TopLogicServlet;

/**
 * HTTP endpoint exposing the {@link AgentSession headless agent interface} of a live application
 * session.
 *
 * <p>
 * This is the integration point a non-browser consumer (a script-recorder/replay tool or an AI agent,
 * typically through a thin MCP wrapper) uses to drive a real session:
 * </p>
 * <ul>
 * <li>{@code GET  /agent-api/windows} &rarr; {@code {"windows":[...]}} — the windows that can be
 * observed.</li>
 * <li>{@code GET  /agent-api/observe?windowName=W} &rarr; the addressable state tree of window
 * {@code W} (an {@link AgentNodeView} as JSON).</li>
 * <li>{@code POST /agent-api/act} with body {@code {"windowName":W,"address":A,"command":C,"arguments":{…}}}
 * &rarr; resolves {@code A}, invokes {@code C}, and returns {@code {"success":b,"observation":{…}}}
 * with the resulting state tree.</li>
 * </ul>
 *
 * <p>
 * Acting dispatches through {@link com.top_logic.layout.react.control.ReactControl#executeCommand} —
 * the same path the browser command endpoint uses — under the same window
 * {@link ReactWindowRegistry#getRequestLock() request lock} and subsession context. After the command
 * runs, {@link ReactWindowRegistry#synthesizeModelEvents(String) model events are synthesized} so that
 * all derived state has settled before the response is built. The reply therefore carries a
 * <em>quiesced</em> observation: a headless caller never has to poll or await asynchronous SSE
 * delivery — it reads the fresh server state directly.
 * </p>
 *
 * <p>
 * The endpoint runs inside the authenticated {@link TopLogicServlet} context, so it acts as the
 * logged-in user with exactly the permissions the interactive UI would have.
 * </p>
 */
public class AgentServlet extends TopLogicServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String pathInfo = request.getPathInfo();
		try {
			if ("/windows".equals(pathInfo)) {
				handleWindows(response, session);
			} else if ("/observe".equals(pathInfo)) {
				handleObserve(request, response, session);
			} else {
				sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
			}
		} catch (IllegalArgumentException ex) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
		} catch (Exception ex) {
			Logger.error("Error handling agent request: " + pathInfo, ex, AgentServlet.class);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String pathInfo = request.getPathInfo();
		try {
			if ("/act".equals(pathInfo)) {
				handleAct(request, response, session);
			} else {
				sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
			}
		} catch (IllegalArgumentException ex) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
		} catch (Exception ex) {
			Logger.error("Error handling agent request: " + pathInfo, ex, AgentServlet.class);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error.");
		}
	}

	private void handleWindows(HttpServletResponse response, HttpSession session) throws IOException {
		List<String> windows = new ArrayList<>(ReactWindowRegistry.forSession(session).windowNames());
		write(response, "{\"windows\":" + JSON.toString(windows) + "}");
	}

	private void handleObserve(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String windowName = request.getParameter("windowName");
		SSEUpdateQueue queue = requireQueue(session, windowName);

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		installSubSession(displayContext, windowName);

		ReentrantLock requestLock = ReactWindowRegistry.forSession(session).getRequestLock();
		requestLock.lock();
		try {
			write(response, agentSession(queue).observeJson());
		} finally {
			requestLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private void handleAct(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		Object parsed;
		try {
			parsed = JSON.fromString(new String(request.getInputStream().readAllBytes(), "UTF-8"));
		} catch (JSON.ParseException ex) {
			throw new IllegalArgumentException("Invalid JSON: " + ex.getMessage());
		}
		if (!(parsed instanceof Map)) {
			throw new IllegalArgumentException("Expected a JSON object body.");
		}
		Map<String, Object> body = (Map<String, Object>) parsed;
		String windowName = (String) body.get("windowName");
		String address = (String) body.get("address");
		String command = (String) body.get("command");
		Map<String, Object> arguments = (Map<String, Object>) body.get("arguments");
		if (address == null || command == null) {
			throw new IllegalArgumentException("Missing 'address' or 'command'.");
		}

		SSEUpdateQueue queue = requireQueue(session, windowName);
		AgentSession agentSession = agentSession(queue);

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);

		ReentrantLock requestLock = ReactWindowRegistry.forSession(session).getRequestLock();
		requestLock.lock();
		try {
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			HandlerResult result;
			try {
				result = agentSession.act(address, command, arguments);
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}

			// Let derived/observable state settle before observing, so the returned tree is quiesced.
			ReactWindowRegistry.forSession(session).synthesizeModelEvents(windowName);

			String observation = agentSession.observeJson();
			write(response, "{\"success\":" + result.isSuccess() + ",\"observation\":" + observation + "}");
		} finally {
			requestLock.unlock();
		}
	}

	/**
	 * Builds a session rooted at the window's displayed root control.
	 */
	private static AgentSession agentSession(SSEUpdateQueue queue) {
		return AgentSession.forRoot(queue.getRootControl());
	}

	private SSEUpdateQueue requireQueue(HttpSession session, String windowName) {
		if (StringServices.isEmpty(windowName)) {
			throw new IllegalArgumentException("Missing 'windowName'.");
		}
		SSEUpdateQueue queue = ReactWindowRegistry.forSession(session).getQueue(windowName);
		if (queue == null) {
			throw new IllegalArgumentException("Unknown window: " + windowName);
		}
		return queue;
	}

	/**
	 * Installs the subsession context for the given window so command execution and label resolution
	 * have an ambient {@link com.top_logic.util.TLContext}. Mirrors the React command servlet.
	 *
	 * @return The {@link SubsessionHandler} if the window uses the traditional layout engine, else
	 *         {@code null}.
	 */
	private SubsessionHandler installSubSession(DisplayContext displayContext, String windowName) {
		if (StringServices.isEmpty(windowName)) {
			return null;
		}
		TLSessionContext sessionContext = TLContextManager.getSession();
		if (sessionContext == null) {
			return null;
		}
		TLSubSessionContext subSession = sessionContext.getSubSession(windowName);
		if (subSession != null) {
			displayContext.installSubSessionContext(subSession);
		} else {
			Logger.warn("No SubSession found for window '" + windowName
				+ "'. The view page may not have been loaded yet.", AgentServlet.class);
		}
		ContentHandlersRegistry handlersRegistry = sessionContext.getHandlersRegistry();
		return handlersRegistry.getContentHandler(windowName);
	}

	private static void write(HttpServletResponse response, String json) throws IOException {
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.flush();
	}

	private static void sendError(HttpServletResponse response, int status, String message)
			throws IOException {
		response.setStatus(status);
		write(response, "{\"error\":" + JSON.toString(message == null ? "" : message) + "}");
	}
}
