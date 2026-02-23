/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.Logger;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Servlet that handles React client commands.
 *
 * <p>
 * The client sends JSON-encoded command requests via POST. The servlet validates the session,
 * resolves the target control from the session-scoped {@link SSEUpdateQueue}, and dispatches the
 * command. Any resulting state updates are delivered via SSE.
 * </p>
 */
public class ReactServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

		DisplayContext displayContext = new DummyDisplayContext(request.getServletContext());
		HandlerResult result = control.executeCommand(displayContext, commandName, arguments);

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
