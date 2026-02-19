/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.top_logic.ai.chat.api.messages.ChatSendRequest;
import com.top_logic.ai.chat.api.messages.ChatSendResponse;
import com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse;
import com.top_logic.ai.chat.api.messages.ChatSessionDto;
import com.top_logic.ai.chat.api.messages.ChatSessionListResponse;
import com.top_logic.ai.chat.api.messages.ChatUpdatesResponse;
import com.top_logic.ai.chat.api.messages.CreateSessionRequest;
import com.top_logic.ai.chat.api.messages.SaveMessagesRequest;
import com.top_logic.ai.chat.api.messages.UpdateTitleRequest;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLObject;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JSON API for the agentic chat: POST /chat/send, GET /chat/updates, GET /chat/events (SSE).
 *
 * <p>
 * Uses session-scoped {@link TaskProgressTracker} via {@link ChatSessionRegistry}.
 * Authentication is enforced by TopLogic's {@code SessionCheckingFilter} mapped to {@code /chat/*}
 * in the {@code web-fragment.xml}, so only authenticated users can reach this servlet.
 * </p>
 */
public class ChatServlet extends HttpServlet {

	private static final String PATH_SEND = "/send";
	private static final String PATH_UPDATES = "/updates";
	private static final String PATH_EVENTS = "/events";
	private static final String PATH_SESSIONS = "/sessions";
	private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
	private static final String CONTENT_TYPE_SSE = "text/event-stream";

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
		if (pathInfo.equals(PATH_SESSIONS) || pathInfo.equals(PATH_SESSIONS + "/")) {
			handleCreateSession(req, resp);
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
		if (pathInfo.startsWith(PATH_SESSIONS)) {
			handleSessionGet(req, resp, pathInfo);
			return;
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		if (pathInfo.startsWith(PATH_SESSIONS)) {
			handleSessionPut(req, resp, pathInfo);
			return;
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		if (pathInfo.startsWith(PATH_SESSIONS + "/")) {
			handleDeleteSession(req, resp, pathInfo);
			return;
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void handleSend(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ChatSendRequest body;
		try {
			String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			JsonReader reader = new JsonReader(new StringR(jsonBody));
			body = ChatSendRequest.readChatSendRequest(reader);
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
		String sessionId = body.getSessionId();
		if (sessionId != null && sessionId.trim().isEmpty()) {
			sessionId = null;
		}
		final String activeSessionId = sessionId;

		TaskProgressTracker tracker = ChatSessionRegistry.getOrCreate(req.getSession(false));

		if (activeSessionId != null) {
			tracker.setActiveSessionId(activeSessionId);
		}

		String storedName = tracker.getUserName();
		final String userName = storedName != null ? storedName : "Anonymous";
		ChatProcessor.SendResult result = ThreadContextManager.inSystemInteraction(ChatServlet.class, () -> {
			return ChatProcessor.sendMessage(tracker, userName, messageText, null, null);
		});

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		if (result.getTaskId() == null) {
			ChatSendResponse sendResponse = ChatSendResponse.create()
				.setStatus(result.isBusy() ? "busy" : "error");
			resp.getWriter().write(DtoMapper.toJson(sendResponse));
			return;
		}
		ChatSendResponse sendResponse = ChatSendResponse.create()
			.setTaskId(result.getTaskId())
			.setStatus("processing");
		resp.getWriter().write(DtoMapper.toJson(sendResponse));
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
		resp.getWriter().write(DtoMapper.toJson(response));
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

	private void handleSessionGet(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
			throws IOException {
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

		// GET /chat/sessions — list all
		if (pathInfo.equals(PATH_SESSIONS) || pathInfo.equals(PATH_SESSIONS + "/")) {
			final ChatSessionListResponse[] result = new ChatSessionListResponse[1];
			ThreadContextManager.inSystemInteraction(ChatServlet.class, () -> {
				List<TLObject> sessions = ChatHistoryService.getSessionsForCurrentUser();
				ChatSessionListResponse response = ChatSessionListResponse.create();
				for (TLObject session : sessions) {
					ChatSessionDto dto = ChatSessionDto.create()
						.setId(getObjectId(session))
						.setTitle(ChatHistoryService.getTitle(session));
					Date lastUpdated = (Date) session.tValueByName("lastUpdated");
					if (lastUpdated != null) {
						dto.setLastUpdated(formatIso(lastUpdated));
					}
					response.addSession(dto);
				}
				result[0] = response;
				return null;
			});
			resp.getWriter().write(DtoMapper.toJson(result[0]));
			return;
		}

		// GET /chat/sessions/{id} — load single session
		String sessionId = extractSessionId(pathInfo);
		if (sessionId != null && !sessionId.contains("/")) {
			final ChatSessionDetailResponse[] result = new ChatSessionDetailResponse[1];
			ThreadContextManager.inSystemInteraction(ChatServlet.class, () -> {
				TLObject session = ChatHistoryService.findSessionById(sessionId);
				if (session == null || !ChatHistoryService.isOwnedByCurrentUser(session)) {
					return null;
				}
				result[0] = ChatSessionDetailResponse.create()
					.setId(sessionId)
					.setTitle(ChatHistoryService.getTitle(session))
					.setMessagesJson(ChatHistoryService.getMessagesJson(session));
				return null;
			});
			if (result[0] == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			resp.getWriter().write(DtoMapper.toJson(result[0]));
			return;
		}

		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void handleCreateSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		CreateSessionRequest body;
		try {
			body = CreateSessionRequest.readCreateSessionRequest(new JsonReader(new StringR(jsonBody)));
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
			return;
		}
		String title = body.getTitle();
		if (title == null || title.trim().isEmpty()) {
			title = "New Chat";
		}
		final String sessionTitle = title.trim();
		final ChatSessionDto[] result = new ChatSessionDto[1];

		ThreadContextManager.inSystemInteraction(ChatServlet.class, () -> {
			TLObject session = ChatHistoryService.createSession(sessionTitle);
			if (session != null) {
				result[0] = ChatSessionDto.create()
					.setId(getObjectId(session))
					.setTitle(sessionTitle);
			}
			return null;
		});

		if (result[0] == null) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create session");
			return;
		}
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(DtoMapper.toJson(result[0]));
	}

	private void handleSessionPut(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
			throws IOException {
		String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

		// PUT /chat/sessions/{id}/title
		if (pathInfo.endsWith("/title")) {
			String sessionId = extractSessionIdBeforeSuffix(pathInfo, "/title");
			UpdateTitleRequest body;
			try {
				body = UpdateTitleRequest.readUpdateTitleRequest(new JsonReader(new StringR(jsonBody)));
			} catch (Exception e) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
				return;
			}
			final String newTitle = body.getTitle();
			final boolean[] success = {false};
			ThreadContextManager.inSystemInteraction(ChatServlet.class, () -> {
				TLObject session = ChatHistoryService.findSessionById(sessionId);
				if (session != null && ChatHistoryService.isOwnedByCurrentUser(session)) {
					ChatHistoryService.updateTitle(session, newTitle);
					success[0] = true;
				}
				return null;
			});
			resp.setStatus(success[0] ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// PUT /chat/sessions/{id}/messages
		if (pathInfo.endsWith("/messages")) {
			String sessionId = extractSessionIdBeforeSuffix(pathInfo, "/messages");
			SaveMessagesRequest body;
			try {
				body = SaveMessagesRequest.readSaveMessagesRequest(new JsonReader(new StringR(jsonBody)));
			} catch (Exception e) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
				return;
			}
			final String messagesJson = body.getMessagesJson();
			final boolean[] success = {false};
			ThreadContextManager.inSystemInteraction(ChatServlet.class, () -> {
				TLObject session = ChatHistoryService.findSessionById(sessionId);
				if (session != null && ChatHistoryService.isOwnedByCurrentUser(session)) {
					ChatHistoryService.saveMessages(session, messagesJson);
					success[0] = true;
				}
				return null;
			});
			resp.setStatus(success[0] ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void handleDeleteSession(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
			throws IOException {
		String sessionId = extractSessionId(pathInfo);
		if (sessionId == null || sessionId.contains("/")) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		final boolean[] success = {false};
		ThreadContextManager.inSystemInteraction(ChatServlet.class, () -> {
			TLObject session = ChatHistoryService.findSessionById(sessionId);
			if (session != null && ChatHistoryService.isOwnedByCurrentUser(session)) {
				ChatHistoryService.deleteSession(session);
				success[0] = true;
			}
			return null;
		});
		resp.setStatus(success[0] ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
	}

	/** Extracts session ID from path like "/sessions/123". */
	private static String extractSessionId(String pathInfo) {
		String rest = pathInfo.substring(PATH_SESSIONS.length());
		if (rest.startsWith("/")) {
			rest = rest.substring(1);
		}
		if (rest.endsWith("/")) {
			rest = rest.substring(0, rest.length() - 1);
		}
		return rest.isEmpty() ? null : rest;
	}

	/** Extracts session ID from path like "/sessions/123/title". */
	private static String extractSessionIdBeforeSuffix(String pathInfo, String suffix) {
		String withoutSuffix = pathInfo.substring(0, pathInfo.length() - suffix.length());
		return extractSessionId(withoutSuffix);
	}

	private static String getObjectId(TLObject obj) {
		return String.valueOf(((KnowledgeObject) obj.tHandle()).getObjectName());
	}

	private static String formatIso(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

}
