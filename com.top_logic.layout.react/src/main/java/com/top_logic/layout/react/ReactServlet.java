/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.json.JSON;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.event.infoservice.InfoServiceXMLStringConverter;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.TopLogicServlet;

/**
 * Servlet that handles React client commands.
 *
 * <p>
 * Extends {@link TopLogicServlet} to get a proper {@link DisplayContext} with session and subsession
 * setup. The client sends JSON-encoded command requests via POST. The servlet resolves the target
 * control from the session-scoped {@link SSEUpdateQueue} and dispatches the command. Any resulting
 * state updates are delivered via SSE.
 * </p>
 */
public class ReactServlet extends TopLogicServlet {

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
		if (pathInfo == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing path.");
			return;
		}

		try {
			switch (pathInfo) {
				case "/command":
					handleCommand(request, response, session);
					break;
				case "/state":
					handleState(request, response, session);
					break;
				default:
					sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
					break;
			}
		} catch (Exception ex) {
			Logger.error("Error handling React request: " + pathInfo, ex, ReactServlet.class);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error.");
		}
	}

	@SuppressWarnings("unchecked")
	private void handleCommand(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String body = new String(request.getInputStream().readAllBytes(), "UTF-8");
		Object parsed;
		try {
			parsed = JSON.fromString(body);
		} catch (JSON.ParseException ex) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + ex.getMessage());
			return;
		}
		if (!(parsed instanceof Map)) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Expected JSON object.");
			return;
		}

		Map<String, Object> commandData = (Map<String, Object>) parsed;
		String controlId = (String) commandData.get("controlId");
		String commandName = (String) commandData.get("command");
		String windowName = (String) commandData.get("windowName");
		Map<String, Object> arguments = (Map<String, Object>) commandData.get("arguments");

		if (controlId == null || commandName == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId or command.");
			return;
		}

		if (arguments == null) {
			arguments = Map.of();
		}

		SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
		CommandListener control = queue.getControl(controlId);
		if (control == null) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "Control not found: " + controlId);
			return;
		}

		// Obtain the real DisplayContext set up by TopLogicServlet.
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		// Install subsession context if the client sent the window name.
		if (!StringServices.isEmpty(windowName)) {
			TLSessionContext sessionContext = TLContextManager.getSession();
			if (sessionContext != null) {
				ContentHandlersRegistry handlersRegistry = sessionContext.getHandlersRegistry();
				SubsessionHandler rootHandler = handlersRegistry.getContentHandler(windowName);
				if (rootHandler != null) {
					TLSubSessionContext subSession = sessionContext.getSubSession(windowName);
					displayContext.installSubSessionContext(subSession);
				}
			}
		}

		HandlerResult result = control.executeCommand(displayContext, commandName, arguments);

		// Forward any InfoService messages that were added during command execution via SSE.
		forwardInfoServiceMessages(displayContext, queue);

		if (result.isSuccess()) {
			sendSuccess(response);
		} else {
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Command failed.");
		}
	}

	@SuppressWarnings("unchecked")
	private void handleState(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String controlId = request.getParameter("controlId");
		if (controlId == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId parameter.");
			return;
		}

		SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
		CommandListener control = queue.getControl(controlId);
		if (control instanceof ReactControl) {
			ReactControl reactControl = (ReactControl) control;
			PrintWriter writer = response.getWriter();
			writer.write(ReactControl.toJsonString(reactControl.getReactState()));
			writer.flush();
		} else {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "Control not found: " + controlId);
		}
	}

	@SuppressWarnings("unchecked")
	private void forwardInfoServiceMessages(DisplayContext displayContext, SSEUpdateQueue queue) {
		if (displayContext.isSet(InfoService.INFO_SERVICE_ENTRIES)) {
			List<HTMLFragment> entries = displayContext.get(InfoService.INFO_SERVICE_ENTRIES);
			if (!entries.isEmpty()) {
				String jsCode = InfoServiceXMLStringConverter.getJSInvocation(displayContext, entries);
				queue.enqueue(JSSnipplet.create().setCode(jsCode));
			}
		}
	}

	private void sendSuccess(HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		writer.write("{\"success\":true}");
		writer.flush();
	}

	private void sendError(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		writer.write("{\"success\":false,\"error\":\"" + message.replace("\"", "\\\"") + "\"}");
		writer.flush();
	}

}
