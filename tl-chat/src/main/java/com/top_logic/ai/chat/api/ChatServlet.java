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

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.ChatSendRequest;
import com.top_logic.ai.chat.api.messages.ChatSendResponse;
import com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse;
import com.top_logic.ai.chat.api.messages.ChatSessionDto;
import com.top_logic.ai.chat.api.messages.ChatSessionListResponse;
import com.top_logic.ai.chat.api.messages.ChatUpdatesResponse;
import com.top_logic.ai.chat.api.messages.CreateSessionRequest;
import com.top_logic.ai.chat.api.messages.PersistedChatEntry;
import com.top_logic.ai.chat.api.messages.SaveMessagesRequest;
import com.top_logic.ai.chat.api.messages.UpdateTitleRequest;
import com.top_logic.ai.chat.layout.ChatEntry;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * JSON API for the agentic chat: POST /chat/send, GET /chat/updates, GET /chat/events (SSE).
 *
 * <p>
 * Uses {@link SessionService} to resolve the authenticated user from the HTTP session. All
 * <code>KnowledgeBase</code> operations are performed via {@link ChatHistoryService}, which manages its own
 * thread context internally.
 * </p>
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

	private static final int MAX_ATTACHMENTS = 5;
	private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10 MB

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

		List<AttachmentDto> attachments = body.getAttachments();
		if (attachments != null && !attachments.isEmpty()) {
			String validationError = validateAttachments(attachments);
			if (validationError != null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, validationError);
				return;
			}
		}

		TaskProgressTracker tracker = ChatSessionRegistry.getOrCreate(req.getSession(false));

		if (activeSessionId != null) {
			tracker.setActiveSessionId(activeSessionId);
		}

		String storedName = tracker.getUserName();
		if (storedName == null) {
			Person person = resolveCurrentPerson(req);
			if (person != null) {
				try {
					storedName = ThreadContextManager.inSystemInteraction(ChatServlet.class,
						() -> person.getName());
				} catch (Exception ex) {
					Logger.error("Failed to resolve person name", ex, ChatServlet.class);
				}
				if (storedName != null) {
					tracker.setUserName(storedName);
				}
			}
		}
		final String userName = storedName != null ? storedName : "Anonymous";
		ChatProcessor.SendResult result = ChatProcessor.sendMessage(tracker, userName, messageText, attachments, null, null);

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

		// Sync timeline events from ToolCallTracker to TaskProgressTracker before returning updates
		ChatProcessor.AIProcessingState state = tracker.getCurrentProcessingState();
		if (state != null) {
			ChatProcessor.applyPollUpdates(state, tracker.getChat(), tracker);
		}
		boolean processing = ChatProcessor.isProcessing(tracker);
		String currentTaskId = ChatProcessor.getCurrentTaskId(tracker);

		ChatUpdatesResponse response = DtoMapper.toUpdatesResponse(tracker, processing, currentTaskId);

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
		Person person = resolveCurrentPerson(req);
		if (person == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

		// GET /chat/sessions — list all
		if (pathInfo.equals(PATH_SESSIONS) || pathInfo.equals(PATH_SESSIONS + "/")) {
			List<TLObject> sessions = ChatHistoryService.getSessionsForUser(person);
			ChatSessionListResponse response = ChatSessionListResponse.create();
			for (TLObject session : sessions) {
				ChatSessionDto dto = ChatSessionDto.create()
					.setId(getObjectId(session))
					.setTitle(ChatHistoryService.getTitle(session));
				Date lastUpdated = ChatHistoryService.getLastUpdated(session);
				if (lastUpdated != null) {
					dto.setLastUpdated(formatIso(lastUpdated));
				}
				response.addSession(dto);
			}
			resp.getWriter().write(DtoMapper.toJson(response));
			return;
		}

		// GET /chat/sessions/{id} — load single session + activate server-side
		String sessionId = extractSessionId(pathInfo);
		if (sessionId != null && !sessionId.contains("/")) {
			TLObject session = ChatHistoryService.findSessionById(sessionId);
			if (session == null || !ChatHistoryService.isOwnedBy(session, person)) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			TaskProgressTracker tracker = ChatSessionRegistry.getOrCreate(req.getSession(false));

			// Activate session on the tracker (switches Chat + ChatMemory)
			if (tracker != null) {
				tracker.getSessionManager().switchSession(sessionId);
			}

			// Build response with structured entries
			ChatSessionDetailResponse response = ChatSessionDetailResponse.create()
				.setId(sessionId)
				.setTitle(ChatHistoryService.getTitle(session));

			// Return serialized entries from the now-active Chat
			if (tracker != null) {
				String entriesJson = DtoMapper.serializeEntries(tracker.getChat().getEntries());
				response.setMessagesJson(entriesJson);
			} else {
				response.setMessagesJson(ChatHistoryService.getMessagesJson(session));
			}

			// Also populate structured entries field
			String messagesJson = ChatHistoryService.getMessagesJson(session);
			if (messagesJson != null) {
				List<ChatEntry> entries = DtoMapper.deserializeEntries(messagesJson);
				int msgIndex = 0;
				for (ChatEntry entry : entries) {
					PersistedChatEntry dto = PersistedChatEntry.create();
					if (entry instanceof ChatEntry.MessageEntry me) {
						dto.setType("message");
						dto.setMessageData(DtoMapper.toMessageDto(me.message(), "m" + msgIndex));
						msgIndex++;
					} else if (entry instanceof ChatEntry.TaskSnapshotEntry tse) {
						dto.setType("task_snapshot");
						dto.setTaskData(DtoMapper.toCurrentTaskDto(tse.task()));
					}
					response.addEntry(dto);
				}
			}

			resp.getWriter().write(DtoMapper.toJson(response));
			return;
		}

		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void handleCreateSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Person person = resolveCurrentPerson(req);
		if (person == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

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

		TLObject session = ChatHistoryService.createSession(sessionTitle, person);
		if (session == null) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create session");
			return;
		}

		ChatSessionDto result = ChatSessionDto.create()
			.setId(getObjectId(session))
			.setTitle(sessionTitle);

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(DtoMapper.toJson(result));
	}

	private void handleSessionPut(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
			throws IOException {
		Person person = resolveCurrentPerson(req);
		if (person == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

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
			TLObject session = ChatHistoryService.findSessionById(sessionId);
			if (session != null && ChatHistoryService.isOwnedBy(session, person)) {
				ChatHistoryService.updateTitle(session, newTitle);
				resp.setStatus(HttpServletResponse.SC_OK);
			} else {
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
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
			TLObject session = ChatHistoryService.findSessionById(sessionId);
			if (session != null && ChatHistoryService.isOwnedBy(session, person)) {
				ChatHistoryService.saveMessages(session, messagesJson);
				resp.setStatus(HttpServletResponse.SC_OK);
			} else {
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return;
		}

		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void handleDeleteSession(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
			throws IOException {
		Person person = resolveCurrentPerson(req);
		if (person == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String sessionId = extractSessionId(pathInfo);
		if (sessionId == null || sessionId.contains("/")) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		TLObject session = ChatHistoryService.findSessionById(sessionId);
		if (session != null && ChatHistoryService.isOwnedBy(session, person)) {
			ChatHistoryService.deleteSession(session);
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private static String validateAttachments(List<AttachmentDto> attachments) {
		if (attachments.size() > MAX_ATTACHMENTS) {
			return "Too many attachments (max " + MAX_ATTACHMENTS + ")";
		}
		for (AttachmentDto att : attachments) {
			if (att.getMimeType() == null || !isAllowedMimeType(att.getMimeType())) {
				return "Unsupported file type: " + att.getMimeType()
					+ ". Allowed: images (PNG, JPEG, GIF, WebP) and PDF.";
			}
			if (att.getBase64Data() != null) {
				long decodedSize = att.getBase64Data().length() * 3L / 4;
				if (decodedSize > MAX_ATTACHMENT_SIZE) {
					return "File too large: " + att.getFileName() + " (max 10 MB)";
				}
			}
		}
		return null;
	}

	private static boolean isAllowedMimeType(String mimeType) {
		return mimeType.startsWith("image/") || "application/pdf".equals(mimeType);
	}

	/**
	 * Resolves the current user from the HTTP session via TopLogic's {@link SessionService}.
	 *
	 * @return the authenticated person, or null if not available.
	 */
	private static Person resolveCurrentPerson(HttpServletRequest req) {
		HttpSession httpSession = SessionService.getInstance().getSession(req);
		if (httpSession == null) {
			return null;
		}
		TLSessionContext sessionContext = SessionService.getInstance().getSession(httpSession);
		if (sessionContext == null) {
			return null;
		}
		return sessionContext.getOriginalUser();
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
		return ((KnowledgeObject) obj.tHandle()).getObjectName().toExternalForm();
	}

	private static String formatIso(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

}
