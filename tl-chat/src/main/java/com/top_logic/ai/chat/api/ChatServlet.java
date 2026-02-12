/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.util.TLContext;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JSON API for the agentic chat: POST /chat/send, GET /chat/updates, GET /chat/events (SSE).
 * Uses session-scoped {@link TaskProgressTracker} via {@link ChatSessionRegistry}.
 */
public class ChatServlet extends HttpServlet {

	private static final String PATH_SEND = "/send";
	private static final String PATH_UPDATES = "/updates";
	private static final String PATH_EVENTS = "/events";
	private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
	private static final String CONTENT_TYPE_SSE = "text/event-stream";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing path: /chat/send or /chat/updates");
			return;
		}
		if (pathInfo.equals(PATH_SEND) || pathInfo.equals(PATH_SEND + "/")) {
			handleSend(req, resp);
			return;
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing path: /chat/updates or /chat/events");
			return;
		}
		if (pathInfo.equals(PATH_EVENTS) || pathInfo.equals(PATH_EVENTS + "/")) {
			handleSSE(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_UPDATES) || pathInfo.equals(PATH_UPDATES + "/")) {
			handleUpdates(req, resp);
			return;
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
	}

	private void handleSend(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ChatSendRequest body;
		try {
			body = MAPPER.readValue(req.getInputStream(), ChatSendRequest.class);
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + e.getMessage());
			return;
		}
		String text = body.getText();
		if (text == null || text.trim().isEmpty()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty 'text'");
			return;
		}
		final String messageText = text.trim();

		TaskProgressTracker tracker = ChatSessionRegistry.getOrCreate(req.getSession(false));

		// Get user name: try TLContext first (if behind TL filter), then tracker's stored name
		final String userName = getCurrentUserName(tracker);
		ChatProcessor.SendResult result = ThreadContextManager.inSystemInteraction(ChatServlet.class, () -> {
			return ChatProcessor.sendMessage(tracker, userName, messageText, null, null);
		});

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		if (result.getTaskId() == null) {
			MAPPER.writeValue(resp.getOutputStream(),
				new ChatSendResponse(null, result.isBusy() ? "busy" : "error"));
			return;
		}
		MAPPER.writeValue(resp.getOutputStream(), new ChatSendResponse(result.getTaskId(), "processing"));
	}

	private void handleUpdates(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		TaskProgressTracker tracker = ChatSessionRegistry.getOrCreate(req.getSession(true));
		Boolean[] processing = new Boolean[1];
		String[] currentTaskId = new String[1];
		ThreadContextManager.inSystemInteraction(ChatServlet.class, () -> {
			// Sync timeline events from ToolCallTracker to TaskProgressTracker before returning updates
			ChatProcessor.AIProcessingState state = tracker.getCurrentProcessingState();
			if (state != null) {
				ChatProcessor.applyPollUpdates(state, tracker.getChat(), tracker);
			}
			processing[0] = ChatProcessor.isProcessing(tracker);
			currentTaskId[0] = ChatProcessor.getCurrentTaskId(tracker);
			return null;
		});

		ChatUpdatesResponse response = DtoMapper.toUpdatesResponse(tracker, processing[0], currentTaskId[0]);

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		MAPPER.writeValue(resp.getOutputStream(), response);
	}

	private void handleSSE(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType(CONTENT_TYPE_SSE);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setHeader("Cache-Control", "no-cache");
		resp.setHeader("X-Accel-Buffering", "no");

		AsyncContext asyncContext = req.startAsync();
		asyncContext.setTimeout(0); // No timeout - connection stays open until client disconnects

		TaskProgressTracker tracker = ChatSessionRegistry.getOrCreate(req.getSession(true));
		ChatSSEHandler handler = new ChatSSEHandler(tracker, asyncContext, resp.getWriter());
		handler.start();
	}

	private static String getCurrentUserName(TaskProgressTracker tracker) {
		try {
			TLContext context = TLContext.getContext();
			if (context != null && context.getPerson() != null) {
				return context.getPerson().getName();
			}
		} catch (Exception ignored) {
			// fallback
		}
		// Fall back to the name stored on the tracker (set by ChatComponentBuilder in TL context)
		String storedName = tracker.getUserName();
		if (storedName != null) {
			return storedName;
		}
		return "Anonymous";
	}
}
