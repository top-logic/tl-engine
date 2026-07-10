/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.ChatSendRequest;
import com.top_logic.ai.chat.api.messages.ChatSendResponse;
import com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse;
import com.top_logic.ai.chat.api.messages.ChatSessionDto;
import com.top_logic.ai.chat.api.messages.ChatSessionListResponse;
import com.top_logic.ai.chat.api.messages.ChatUpdatesResponse;
import com.top_logic.ai.chat.api.messages.CreateSessionRequest;
import com.top_logic.ai.chat.api.messages.PendingInteractionDto;
import com.top_logic.ai.chat.api.messages.PendingInteractionsResponse;
import com.top_logic.ai.chat.api.messages.UpdateTitleRequest;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.InteractionRequest;
import com.top_logic.tl.ai.core.execution.model.TlAiAgentFactory;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedPart;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.TopLogicServlet;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

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
public class ChatServlet extends TopLogicServlet {

	private static final String PATH_SEND = "/send";
	private static final String PATH_RESPOND = "/respond";
	private static final String PATH_CANCEL = "/cancel";
	private static final String PATH_APPROVAL_MODE = "/approval-mode";
	private static final String PATH_UPDATES = "/updates";
	private static final String PATH_EVENTS = "/events";
	private static final String PATH_SESSIONS = "/sessions";
	private static final String PATH_INTERACTIONS = "/interactions";
	private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
	private static final String CONTENT_TYPE_SSE = "text/event-stream";

	private static final int MAX_ATTACHMENTS = 5;
	private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10 MB

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		advanceSessionRevision();
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing path: /chat/send or /chat/updates");
			return;
		}
		if (pathInfo.equals(PATH_SEND) || pathInfo.equals(PATH_SEND + "/")) {
			handleSend(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_RESPOND) || pathInfo.equals(PATH_RESPOND + "/")) {
			handleRespond(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_CANCEL) || pathInfo.equals(PATH_CANCEL + "/")) {
			handleCancel(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_APPROVAL_MODE) || pathInfo.equals(PATH_APPROVAL_MODE + "/")) {
			handleApprovalMode(req, resp);
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
		advanceSessionRevision();
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
		if (pathInfo.equals(PATH_INTERACTIONS) || pathInfo.equals(PATH_INTERACTIONS + "/")) {
			handleInteractions(req, resp);
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
		advanceSessionRevision();
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
		advanceSessionRevision();
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
		List<AttachmentDto> attachments = body.getAttachments();
		boolean hasAttachments = attachments != null && !attachments.isEmpty();
		if ((text == null || text.trim().isEmpty()) && !hasAttachments) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty 'text'");
			return;
		}
		final String messageText = text != null ? text.trim() : "";
		String sessionId = body.getSessionId();
		if (sessionId != null && sessionId.trim().isEmpty()) {
			sessionId = null;
		}
		final String activeSessionId = sessionId;
		if (attachments != null && !attachments.isEmpty()) {
			String validationError = validateAttachments(attachments);
			if (validationError != null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, validationError);
				return;
			}
		}

		TaskProgressTracker tracker = currentTracker();
		TLSubSessionContext subSession = TLContextManager.getSubSession();

		if (activeSessionId != null) {
			tracker.setActiveSessionId(activeSessionId);
		}

		String storedName = tracker.getUserName();
		if (storedName == null) {
			Person person = resolveCurrentPerson(req);
			if (person != null) {
				try {
					storedName = person.getName();
				} catch (Exception ex) {
					Logger.error("Failed to resolve person name", ex, ChatServlet.class);
				}
				if (storedName != null) {
					tracker.setUserName(storedName);
				}
			}
		}
		final String userName = storedName != null ? storedName : "Anonymous";
		ChatProcessor.SendResult result =
			ChatProcessor.sendMessage(tracker, subSession, userName, messageText, attachments);

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

	private void handleRespond(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

		String requestId = null;
		String response = null;
		String toolName = null;
		String comment = null;
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> parsed = (Map<String, Object>) com.top_logic.basic.json.JSON.fromString(jsonBody);
			requestId = (String) parsed.get("requestId");
			response = (String) parsed.get("response");
			toolName = (String) parsed.get("toolName");
			comment = (String) parsed.get("comment");
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
			return;
		}

		if (requestId == null || response == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing requestId or response");
			return;
		}

		TaskProgressTracker tracker = currentTracker();
		ChatProcessor.AIProcessingState state = tracker.getAnyActiveProcessingState();
		if (state == null) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, "No active processing state");
			return;
		}

		UserInteractionStore store = state.getUserInteractionStore();
		if (store == null) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, "No interaction store available");
			return;
		}

		// Include rejection comment in the response string so the ApprovalCallback receives it
		String resolvedResponse = response;
		if ("REJECTED".equals(response) && comment != null && !comment.isEmpty()) {
			resolvedResponse = "REJECTED:" + comment;
		}
		store.resolve(requestId, resolvedResponse);

		// Record the approval decision in the conversation log
		ConversationLogListener logListener = state.getLogListener();
		if (logListener != null) {
			if ("APPROVED_FOR_SESSION".equals(response)) {
				logListener.resolveApproval(requestId,
					Part.ApprovalDecision.APPROVED_FOR_SESSION, null);
			} else if (resolvedResponse.startsWith("REJECTED")) {
				logListener.resolveApproval(requestId,
					Part.ApprovalDecision.DENIED, comment);
			} else {
				logListener.resolveApproval(requestId,
					Part.ApprovalDecision.APPROVED, null);
			}
		}

		if ("APPROVED_FOR_SESSION".equals(response) && toolName != null && !toolName.isEmpty()) {
			tracker.addSessionApprovedTool(toolName);
		}

		resp.setContentType("application/json");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		String safeRequestId = requestId.replace("\\", "\\\\").replace("\"", "\\\"");
		resp.getWriter().write("{\"status\":\"ok\",\"requestId\":\"" + safeRequestId + "\"}");
	}

	private void handleApprovalMode(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

		String mode = null;
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> parsed = (Map<String, Object>) com.top_logic.basic.json.JSON.fromString(jsonBody);
			mode = (String) parsed.get("mode");
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
			return;
		}

		if (mode == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'mode'");
			return;
		}

		com.top_logic.tl.ai.core.agent.approval.ApprovalMode approvalMode;
		try {
			approvalMode = com.top_logic.tl.ai.core.agent.approval.ApprovalMode.valueOf(mode);
		} catch (IllegalArgumentException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid mode: " + mode);
			return;
		}

		TaskProgressTracker tracker = currentTracker();
		tracker.setApprovalMode(approvalMode);

		resp.setContentType("application/json");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"status\":\"ok\",\"mode\":\"" + approvalMode.name() + "\"}");
	}

	private void handleCancel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		TaskProgressTracker tracker = currentTracker();
		ChatProcessor.AIProcessingState state = tracker.getAnyActiveProcessingState();
		if (state == null) {
			resp.setContentType(CONTENT_TYPE_JSON);
			resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
			resp.getWriter().write("{\"cancelled\":false}");
			return;
		}

		// Record cancellation in the conversation log before interrupting
		ConversationLogListener logListener = state.getLogListener();
		if (logListener != null) {
			logListener.recordCancellation();
		}

		Thread agentThread = state.getAgentThread();
		if (agentThread != null) {
			agentThread.interrupt();
		}
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"cancelled\":true}");
	}

	private void handleUpdates(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		TaskProgressTracker tracker = currentTracker();

		ChatProcessor.AIProcessingState state = tracker.getAnyActiveProcessingState();
		if (state != null) {
			// Flush into the run's own captured chat, not the currently active one: with
			// concurrent runs a background run's messages must not leak into another session.
			Chat stateChat = state.getChat();
			ChatProcessor.applyPollUpdates(state, stateChat != null ? stateChat : tracker.getChat(), tracker);
		}
		boolean processing = tracker.hasActiveProcessingStateFor(tracker.getActiveSessionId());
		String currentTaskId = ChatProcessor.getCurrentTaskId(tracker);

		ChatUpdatesResponse response = DtoMapper.toUpdatesResponse(tracker, processing, currentTaskId);

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(DtoMapper.toJson(response));
	}

	/**
	 * GET /chat/interactions — pending human-in-the-loop requests (approvals/questions) awaiting the
	 * current user across all of their chat sessions.
	 *
	 * <p>
	 * Lets the client surface a background run's pending approval while the user is viewing a
	 * different chat: the run in chat A blocks on an approval while the user reads chat B, and this
	 * feed drives a badge on chat A's session-list item. Responding still happens via the existing
	 * /chat/respond once the user switches to that chat.
	 * </p>
	 */
	private void handleInteractions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Person person = resolveCurrentPerson(req);
		if (person == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

		TLClassifier chatKind = TlAiAgentFactory.getCHATSessionKindClassifier();
		PendingInteractionsResponse response = PendingInteractionsResponse.create();
		for (InteractionRequest request : AgentExecutionStore.getInstance().pendingInteractionsFor(person)) {
			AgentExecution execution = request.getExecution();
			AgentSession session = execution == null ? null : execution.getSession();
			// Only surface requests that map to a chat session the client can navigate to; runs of
			// other kinds (script, background) have no session-list item to badge.
			if (session == null || !chatKind.equals(session.getKind())) {
				continue;
			}
			PendingInteractionDto dto = PendingInteractionDto.create()
				.setRequestId(request.getRequestId())
				.setType(interactionTypeName(request.getType()))
				.setHeader(request.getHeader())
				.setPrompt(request.getPrompt())
				.setPayload(request.getPayload())
				.setSessionId(getObjectId(session))
				.setSessionTitle(session.getTitle());
			Date createdAt = request.getCreatedAt();
			if (createdAt != null) {
				dto.setCreatedAt(formatIso(createdAt));
			}
			response.addInteraction(dto);
		}

		resp.getWriter().write(DtoMapper.toJson(response));
	}

	/** Lowercased classifier name ("approval"/"question"/"choice") of an interaction type. */
	private static String interactionTypeName(TLClassifier type) {
		return type == null ? null : type.getName().toLowerCase(java.util.Locale.ROOT);
	}

	private void handleSSE(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		TaskProgressTracker tracker = currentTracker();

		resp.setContentType(CONTENT_TYPE_SSE);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setHeader("Cache-Control", "no-cache, no-store, no-transform");
		resp.setHeader("X-Accel-Buffering", "no");
		resp.setHeader("Connection", "keep-alive");
		resp.setBufferSize(0);

		AsyncContext asyncContext = req.startAsync();
		asyncContext.setTimeout(0); // No timeout - connection stays open until client disconnects

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

			TaskProgressTracker tracker = currentTracker();

			// Activate session on the tracker (switches Chat + ChatMemory)
			if (tracker != null) {
				tracker.getSessionManager().switchSession(sessionId);
			}

			// Build response with structured entries
			ChatSessionDetailResponse response = ChatSessionDetailResponse.create()
				.setId(sessionId)
				.setTitle(ChatHistoryService.getTitle(session));

			// Prefer the live ConversationLog of the now-active session: it includes the
			// open turn of an in-flight run, whose transcript is only persisted at run end.
			// Only sessions without server-side state fall back to the persisted
			// per-execution transcripts.
			String messagesJson = null;
			if (tracker != null) {
				ConversationLog convLog = tracker.getSessionManager().getOrCreateConversationLog();
				if (convLog.turnCount() > 0) {
					messagesJson = ConversationLogSerializer.toJson(convLog);
				}
			}
			if (messagesJson == null) {
				messagesJson = ChatHistoryService.loadConversationJson(session);
			}
			if (messagesJson != null && !messagesJson.isEmpty()) {
				response.setMessagesJson(messagesJson);
				PersistedConversationV2 conv = PersistedConversationV2.readPersistedConversationV2(
					new JsonReader(new StringR(messagesJson)));
				for (PersistedTurn turn : conv.getTurns()) {
					computeContentHtml(turn);
				}
				response.setTurnsJson(serializeTurns(conv.getTurns()));
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

		// PUT /chat/sessions/{id}/messages was removed: the server is the only persistence
		// writer (per-execution transcripts written by the agent runner). Clients must not
		// push conversation state.
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

	private static final java.util.Set<String> ALLOWED_MIME_TYPES = java.util.Set.of(
		"image/png", "image/jpeg", "image/gif", "image/webp", "application/pdf");

	private static String validateAttachments(List<AttachmentDto> attachments) {
		if (attachments.size() > MAX_ATTACHMENTS) {
			return "Too many attachments (max " + MAX_ATTACHMENTS + ")";
		}
		for (AttachmentDto att : attachments) {
			if (att.getMimeType() == null || !ALLOWED_MIME_TYPES.contains(att.getMimeType())) {
				return "Unsupported file type: " + att.getMimeType()
					+ ". Allowed: PNG, JPEG, GIF, WebP, and PDF.";
			}
			if (att.getBase64Data() == null || att.getBase64Data().isEmpty()) {
				return "Missing file data for: " + att.getFileName();
			}
			long decodedSize = att.getBase64Data().length() * 3L / 4;
			if (decodedSize > MAX_ATTACHMENT_SIZE) {
				return "File too large: " + att.getFileName() + " (max 10 MB)";
			}
		}
		return null;
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

	/** The client-provided window id ({@code windowName}) of the originating sub-session, or null. */
	private static String windowId(HttpServletRequest req) {
		String id = req.getParameter("windowId");
		return (id == null || id.isEmpty()) ? null : id;
	}

	/**
	 * Binds the application-window sub-session addressed by the request's {@code windowId} to the
	 * current thread before dispatching, so every chat endpoint runs in the user's UI context.
	 *
	 * <p>
	 * A single login can host several sub-sessions (cloned tabs, popped-out views); the plain HTTP
	 * request only identifies the login. The client therefore echoes the window id of its
	 * application window, which we resolve to the originating sub-session here - once, centrally
	 * - so the handlers (and the <code>KnowledgeBase</code> operations they trigger) see a valid, correct
	 * session context, and the background agent acts on the user's UI. If the window cannot be
	 * resolved (unknown id, closed window, server restart) the request is rejected so the client
	 * reloads its application window.
	 * </p>
	 */
	@Override
	protected void doService(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		TLSubSessionContext subSession =
			ChatSessionRegistry.resolveSubSession(TLContextManager.getSession(), windowId(req));
		if (subSession == null || subSession.get(ChatSessionRegistry.CHAT_TRACKER) == null) {
			sendStaleWindow(resp);
			return;
		}
		// Install the resolved sub-session directly into this request's interaction, the same way
		// the layout AJAXServlet binds the addressed sub-session. The interaction is request-scoped
		// (set up by TopLogicServlet without a sub-session) and torn down at the end of the request,
		// so no explicit restore is needed. Going through ThreadContextManager.inContext(...) would
		// instead log an "Overriding subsession" info on every request.
		TLContextManager.getInteraction().installSubSessionContext(subSession);
		super.doService(req, resp);
	}

	/**
	 * The {@link TaskProgressTracker} bound to the sub-session of the current request.
	 *
	 * <p>
	 * Never null inside a handler: {@link #doService} rejects requests whose sub-session or tracker
	 * cannot be resolved before any handler runs.
	 * </p>
	 */
	private static TaskProgressTracker currentTracker() {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		return subSession == null ? null : subSession.get(ChatSessionRegistry.CHAT_TRACKER);
	}

	/**
	 * Rejects a request whose originating sub-session could not be resolved, instructing the client
	 * to reload the application window.
	 */
	private static void sendStaleWindow(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_CONFLICT);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"status\":\"error\",\"error\":\"stale_window\"}");
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

	/**
	 * Advances the subsession's KB revision at request begin so this request sees all commits
	 * made so far — in particular the {@code AgentExecutionStore} writer thread's commits from a
	 * previous request (session created, execution recorded), which a subsession's stable view
	 * would otherwise not include (read-your-own-writes).
	 *
	 * <p>
	 * Request begin is a sanctioned advance point: no KB transaction is open on this thread and
	 * no rendering is in progress, so the update cannot merge into (and poison) an open business
	 * transaction or produce an inconsistent view.
	 * </p>
	 */
	private static void advanceSessionRevision() {
		if (ThreadContextManager.getInteraction() == null) {
			return;
		}
		try {
			HistoryUtils.updateSessionRevision();
		} catch (RuntimeException ex) {
			Logger.info("Could not advance session revision: " + ex.getMessage(), ChatServlet.class);
		}
	}

	private static String getObjectId(TLObject obj) {
		return obj.tHandle().getObjectName().toExternalForm();
	}

	private static String formatIso(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	/**
	 * Serializes turns to a JSON array string for {@code ChatSessionDetailResponse.turnsJson}.
	 */
	private static String serializeTurns(List<PersistedTurn> turns) throws IOException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginArray();
			for (PersistedTurn turn : turns) {
				turn.writeTo(json);
			}
			json.endArray();
		}
		return buffer.toString();
	}

	/**
	 * Extracts text from assistant parts after the last tool call and sets server-rendered
	 * {@code contentHtml} on the turn. Pre-tool narration is excluded so that only the
	 * agent's final response is rendered.
	 */
	private static void computeContentHtml(PersistedTurn turn) {
		List<PersistedPart> parts = turn.getAssistantParts();

		int lastToolIndex = -1;
		for (int i = parts.size() - 1; i >= 0; i--) {
			if ("tool_call".equals(parts.get(i).getType())) {
				lastToolIndex = i;
				break;
			}
		}

		StringBuilder text = new StringBuilder();
		for (int i = lastToolIndex + 1; i < parts.size(); i++) {
			PersistedPart part = parts.get(i);
			if ("text".equals(part.getType()) && part.getTextData() != null) {
				text.append(part.getTextData().getText());
			}
		}
		if (text.length() > 0) {
			turn.setContentHtml(Converters.markdownToHtmlString(text.toString()));
		}
	}

}
