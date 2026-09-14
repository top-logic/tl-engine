/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest;
import com.top_logic.ai.chat.api.messages.ChatSendRequest;
import com.top_logic.ai.chat.api.messages.ChatSendResponse;
import com.top_logic.ai.chat.api.messages.ContextCompositionEvent;
import com.top_logic.ai.chat.api.messages.MessageFeedbackDto;
import com.top_logic.ai.chat.api.messages.MessageFeedbackRequest;
import com.top_logic.ai.chat.api.messages.RegenerateRequest;
import com.top_logic.ai.chat.api.messages.TruncateRequest;
import com.top_logic.ai.chat.api.messages.TruncateResponse;
import com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse;
import com.top_logic.ai.chat.api.messages.TokenUsageDto;
import com.top_logic.ai.chat.api.messages.ChatSessionDto;
import com.top_logic.ai.chat.api.messages.ChatSessionListResponse;
import com.top_logic.ai.chat.api.messages.ChatUpdatesResponse;
import com.top_logic.ai.chat.api.messages.CreateSessionRequest;
import com.top_logic.ai.chat.api.messages.ImprovePromptRequest;
import com.top_logic.ai.chat.api.messages.ImprovePromptResponse;
import com.top_logic.ai.chat.api.messages.PendingInteractionDto;
import com.top_logic.ai.chat.api.messages.PendingInteractionsResponse;
import com.top_logic.ai.chat.api.messages.UpdateTitleRequest;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor.PromptImprovementException;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor.Result;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RunAuthorization;
import com.top_logic.tl.ai.core.security.ToolSecurityException;
import com.top_logic.util.Resources;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.InteractionTimeoutService;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.InteractionRequest;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedPart;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn;
import com.top_logic.util.TLContext;
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
	private static final String PATH_IMPROVE_PROMPT = "/improve-prompt";
	private static final String PATH_CANCEL_PROMPT_IMPROVEMENT = "/improve-prompt/cancel";
	private static final String PATH_APPROVAL_MODE = "/approval-mode";
	private static final String PATH_UPDATES = "/updates";
	private static final String PATH_EVENTS = "/events";
	private static final String PATH_SESSIONS = "/sessions";
	private static final String PATH_INTERACTIONS = "/interactions";
	private static final String PATH_SUBAGENTS = "/subagents";
	private static final String PATH_CAPABILITIES = "/capabilities";

	private static final String PATH_REGENERATE = "/regenerate";

	private static final String PATH_TRUNCATE = "/truncate";

	private static final String PATH_FEEDBACK = "/feedback";
	private static final String TRANSCRIPT_SUFFIX = "/transcript";
	private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

	/**
	 * The status of a request the acting account may not make.
	 *
	 * <p>
	 * A status of its own rather than the generic "error": the client shows the reason that comes with
	 * it, and the two are not the same event - one is a failure, the other is an answer.
	 * </p>
	 */
	private static final String FORBIDDEN = "forbidden";
	private static final String CONTENT_TYPE_SSE = "text/event-stream";

	/** The {@link #interactionTypeName(TLClassifier)} of a tool approval. */
	private static final String TYPE_APPROVAL = "approval";

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
		if (pathInfo.equals(PATH_IMPROVE_PROMPT) || pathInfo.equals(PATH_IMPROVE_PROMPT + "/")) {
			handleImprovePrompt(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_CANCEL_PROMPT_IMPROVEMENT)
				|| pathInfo.equals(PATH_CANCEL_PROMPT_IMPROVEMENT + "/")) {
			handleCancelPromptImprovement(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_APPROVAL_MODE) || pathInfo.equals(PATH_APPROVAL_MODE + "/")) {
			handleApprovalMode(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_REGENERATE) || pathInfo.equals(PATH_REGENERATE + "/")) {
			handleRegenerate(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_TRUNCATE) || pathInfo.equals(PATH_TRUNCATE + "/")) {
			handleTruncate(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_FEEDBACK) || pathInfo.equals(PATH_FEEDBACK + "/")) {
			handleFeedback(req, resp);
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
		if (pathInfo.startsWith(PATH_SUBAGENTS + "/") && pathInfo.endsWith(TRANSCRIPT_SUFFIX)) {
			String id = pathInfo.substring((PATH_SUBAGENTS + "/").length(),
				pathInfo.length() - TRANSCRIPT_SUFFIX.length());
			handleSubagentTranscript(req, resp, id);
			return;
		}
		if (pathInfo.equals(PATH_SUBAGENTS) || pathInfo.equals(PATH_SUBAGENTS + "/")) {
			handleListSubagents(req, resp);
			return;
		}
		if (pathInfo.equals(PATH_CAPABILITIES) || pathInfo.equals(PATH_CAPABILITIES + "/")) {
			handleCapabilities(req, resp);
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
		if (pathInfo.startsWith(PATH_SUBAGENTS + "/")) {
			String id = pathInfo.substring((PATH_SUBAGENTS + "/").length());
			handleCancelSubagent(id, resp);
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
		Person person = resolveCurrentPerson(req);

		if (activeSessionId != null) {
			// The same gate the three turn-removing endpoints use, for the endpoint that spends the
			// API budget and writes into a transcript: the id arrives in the request body, so
			// without this it names whichever chat the caller cares to type.
			if (ownedSessionId(req, resp, activeSessionId) == null) {
				return;
			}
			if (!tracker.setActiveSessionId(activeSessionId, person)) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}

		String storedName = tracker.getUserName();
		if (storedName == null) {
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
		String effort = blankToNull(body.getEffort());
		tracker.setEffort(effort);
		ChatProcessor.ChatRunOptions options = new ChatProcessor.ChatRunOptions(
			blankToNull(body.getAgent()),
			blankToNull(body.getModel()),
			body.isMcpOverride(),
			body.getMcpServers(),
			effort);
		String refusal = runRefusal(person, options);
		if (refusal != null) {
			writeForbidden(resp, ChatSendResponse.create().setStatus(FORBIDDEN).setReason(refusal));
			return;
		}
		ChatProcessor.SendResult result =
			ChatProcessor.sendMessage(tracker, subSession, userName, messageText, attachments, options);

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

	/**
	 * Answers the last message again.
	 *
	 * <p>
	 * The previous answer is not kept beside the new one: the user asked for that turn to be done
	 * differently, so the turn is removed - from the session's memory and from its recorded
	 * transcript - and the same user message is sent once more. What the client then streams is the
	 * replacement, in the place the old answer occupied.
	 * </p>
	 */
	private void handleRegenerate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		RegenerateRequest body;
		try {
			String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			body = RegenerateRequest.readRegenerateRequest(new JsonReader(new StringR(jsonBody)));
		} catch (Exception ex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + ex.getMessage());
			return;
		}
		TaskProgressTracker tracker = currentTracker();
		String sessionId = ownedSessionId(req, resp, body.getSessionId());
		if (sessionId == null) {
			return;
		}
		tracker.setActiveSessionId(sessionId, resolveCurrentPerson(req));
		if (tracker.hasActiveProcessingState()) {
			writeTruncateResponse(resp, TruncateResponse.create().setStatus("busy"));
			return;
		}

		ChatProcessor.ChatRunOptions options = new ChatProcessor.ChatRunOptions(
			blankToNull(body.getAgent()), blankToNull(body.getModel()),
			body.isMcpOverride(), body.getMcpServers(), blankToNull(body.getEffort()));
		// Before the turn is removed, not after: a regenerate that is refused must leave the answer it
		// was going to replace where it is.
		String refusal = runRefusal(resolveCurrentPerson(req), options);
		if (refusal != null) {
			writeForbidden(resp, TruncateResponse.create().setStatus(FORBIDDEN).setReason(refusal));
			return;
		}

		ConversationLog log = tracker.getSessionManager().getOrCreateConversationLog();
		int turnCount = log.turnCount();
		if (turnCount == 0) {
			writeTruncateResponse(resp, TruncateResponse.create().setStatus("error"));
			return;
		}
		Removed removed = removeTurnsFrom(tracker, resolveCurrentPerson(req), sessionId, turnCount - 1);
		if (removed.text().isEmpty()) {
			writeTruncateResponse(resp, TruncateResponse.create().setStatus("error"));
			return;
		}

		tracker.setEffort(blankToNull(body.getEffort()));
		String userName = resolveUserName(req, tracker);
		ChatProcessor.SendResult result = ChatProcessor.sendMessage(tracker,
			TLContextManager.getSubSession(), userName, removed.text(), List.of(), options);

		TruncateResponse response = TruncateResponse.create()
			.setRemovedTurns(removed.turns())
			.setText(removed.text());
		if (result.getTaskId() == null) {
			writeTruncateResponse(resp, response.setStatus(result.isBusy() ? "busy" : "error"));
			return;
		}
		writeTruncateResponse(resp, response.setStatus("ok").setTaskId(result.getTaskId()));
	}

	/**
	 * Drops a turn and everything after it, so the user can say it differently.
	 *
	 * <p>
	 * The removed message comes back in the response rather than being re-sent: editing means the
	 * user wants to change it before it goes again, which only they can do.
	 * </p>
	 */
	private void handleTruncate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		TruncateRequest body;
		try {
			String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			body = TruncateRequest.readTruncateRequest(new JsonReader(new StringR(jsonBody)));
		} catch (Exception ex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + ex.getMessage());
			return;
		}
		TaskProgressTracker tracker = currentTracker();
		String sessionId = ownedSessionId(req, resp, body.getSessionId());
		if (sessionId == null) {
			return;
		}
		tracker.setActiveSessionId(sessionId, resolveCurrentPerson(req));
		if (tracker.hasActiveProcessingState()) {
			writeTruncateResponse(resp, TruncateResponse.create().setStatus("busy"));
			return;
		}
		Removed removed = removeTurnsFrom(tracker, resolveCurrentPerson(req), sessionId, body.getFromTurn());
		writeTruncateResponse(resp, TruncateResponse.create()
			.setStatus("ok")
			.setRemovedTurns(removed.turns())
			.setText(removed.text()));
	}

	/**
	 * The session named in a request body, once it is established that it belongs to the caller.
	 *
	 * <p>
	 * Session IDs are small sequential integers taken straight from the request, so a handler that
	 * acts on one without asking acts on whichever chat the caller names. Answers 404 for a session
	 * that does not exist and for one that is not theirs alike: which of the two it is, is not the
	 * caller's business.
	 * </p>
	 *
	 * @return the session ID to work with, or {@code null} when the response has already been
	 *         answered and the handler must return
	 */
	private static String ownedSessionId(HttpServletRequest req, HttpServletResponse resp, String requested)
			throws IOException {
		Person person = resolveCurrentPerson(req);
		if (person == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		String sessionId = blankToNull(requested);
		if (sessionId == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'sessionId'.");
			return null;
		}
		if (ChatHistoryService.findOwnedSessionById(sessionId, person) == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		return sessionId;
	}

	/** What a truncation took away. */
	private record Removed(int turns, String text) {
		// Pure data.
	}

	/**
	 * Removes turn {@code fromTurn} and everything after it from the live session and from what is
	 * recorded for it.
	 *
	 * <p>
	 * Three things hold a copy of a conversation and all three have to forget it: the conversation
	 * memory the model is given, the conversation log the UI is rebuilt from, and the per-run
	 * transcripts in the knowledge base. Ratings of removed turns go too - they describe answers
	 * that no longer exist.
	 * </p>
	 *
	 * @return how many turns went, and the user text of the first of them
	 */
	private static Removed removeTurnsFrom(TaskProgressTracker tracker, Person person, String sessionId,
			int fromTurn) {
		ConversationLog log = tracker.getSessionManager().getOrCreateConversationLog();
		List<Turn> turns = log.turns();
		if (fromTurn < 0 || fromTurn >= turns.size()) {
			return new Removed(0, "");
		}
		String text = userTextOf(turns.get(fromTurn));
		int removed = log.removeFrom(fromTurn);

		ConversationMemory memory = tracker.getConversationMemory();
		if (memory != null) {
			ConversationLogReplay.rebuildMemory(log, memory.conversation());
		}
		tracker.dropFeedbackFrom(fromTurn);

		if (sessionId != null) {
			// Guarded again here, not only in the handler: this is the call that deletes, and it must
			// be impossible to reach it through a path that forgot to ask whose session it is.
			TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, person);
			if (session != null) {
				ChatHistoryService.truncateConversation(session, fromTurn);
			}
		}
		tracker.getSessionManager().persistActiveSession();
		return new Removed(removed, text);
	}

	/** The text the user contributed to a turn, joined over its parts. */
	private static String userTextOf(Turn turn) {
		StringBuilder text = new StringBuilder();
		for (Part part : turn.userParts()) {
			if (part instanceof Part.TextPart textPart) {
				if (text.length() > 0) {
					text.append('\n');
				}
				text.append(textPart.text());
			}
		}
		return text.toString();
	}

	/**
	 * Records what the user thought of one answer.
	 *
	 * <p>
	 * The rating lands in the session's metadata bag (see {@code SessionMetadata.feedback}) rather
	 * than in the transcript: a transcript is written per run and a rating arrives after it, while
	 * the metadata is the session's own and is rewritten whole on every persist. That is also where
	 * an evaluation reads the ratings back from, keyed by the turn they belong to.
	 * </p>
	 */
	private void handleFeedback(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MessageFeedbackRequest body;
		try {
			String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			body = MessageFeedbackRequest.readMessageFeedbackRequest(new JsonReader(new StringR(jsonBody)));
		} catch (Exception ex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + ex.getMessage());
			return;
		}
		TaskProgressTracker tracker = currentTracker();
		String sessionId = ownedSessionId(req, resp, body.getSessionId());
		if (sessionId == null) {
			return;
		}
		tracker.setActiveSessionId(sessionId, resolveCurrentPerson(req));
		tracker.setFeedback(body.getTurnIndex(), blankToNull(body.getRating()), body.getNote());
		tracker.getSessionManager().persistActiveSession();

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"status\":\"ok\"}");
	}

	/**
	 * Why the acting account may not make this request, in their language.
	 *
	 * <p>
	 * The friendly half of the agent security policy: {@code AgentFactory} refuses the run whatever
	 * this says, and this exists so that the refusal reaches the user as a sentence rather than as a
	 * turn that failed. Answers <code>null</code> while nothing can be asked - no account behind the
	 * request, or the policy service not running - and the run is then refused where it starts.
	 * </p>
	 */
	private String runRefusal(Person person, ChatProcessor.ChatRunOptions options) {
		if (person == null || !AiToolAccessService.Module.INSTANCE.isActive()) {
			return null;
		}
		RunAuthorization access;
		String agentName;
		try {
			access = AiToolAccessService.effectiveRunAccessFor(person);
			agentName = options.agent() != null && !options.agent().isBlank()
				? options.agent().trim()
				: AgentFactory.defaultAgentFor(access, hiddenAgents());
		} catch (RuntimeException ex) {
			// A pre-check is a courtesy; a failure in it must not stop a request the run would accept.
			Logger.warn("Could not pre-check what this account may run.", ex, ChatServlet.class);
			return null;
		}
		if (agentName == null) {
			// A request that names no agent, from an account that may run none. Its own sentence: the
			// name of the agent they may not have says nothing they can act on.
			return Resources.getInstance().getString(
				com.top_logic.tl.ai.core.security.I18NConstants.NO_AGENT_AVAILABLE);
		}
		List<String> mcpOverride = options.mcpOverride() ? options.mcpServers() : null;
		AgentFactory.RunOverrides overrides =
			new AgentFactory.RunOverrides(options.model(), mcpOverride, options.effort());
		Decision.Denied denied;
		try {
			denied = AgentFactory.runRefusal(access, agentName, overrides);
		} catch (RuntimeException ex) {
			Logger.warn("Could not pre-check what '" + agentName + "' may run.", ex, ChatServlet.class);
			return null;
		}
		return denied == null ? null : Resources.getInstance().getString(denied.reason());
	}

	/** The agents this chat runs for itself and never offers as a conversation partner. */
	private Set<String> hiddenAgents() {
		TaskProgressTracker tracker = currentTracker();
		String improver = tracker == null ? null : tracker.getPromptImproverAgentName();
		return improver == null || improver.isBlank() ? Set.of() : Set.of(improver);
	}

	/** Answers a send the acting account may not make, with the reason the client shows. */
	private static void writeForbidden(HttpServletResponse resp, ChatSendResponse response)
			throws IOException {
		forbidden(resp);
		resp.getWriter().write(DtoMapper.toJson(response));
	}

	/** Answers a regenerate the acting account may not make, with the reason the client shows. */
	private static void writeForbidden(HttpServletResponse resp, TruncateResponse response)
			throws IOException {
		forbidden(resp);
		resp.getWriter().write(DtoMapper.toJson(response));
	}

	/**
	 * Sets up a refusal the client can read.
	 *
	 * <p>
	 * A JSON body rather than {@code sendError}, whose HTML page the client cannot parse and would
	 * report as a generic failure - which is the opposite of what a refusal with a reason is for.
	 * </p>
	 */
	private static void forbidden(HttpServletResponse resp) {
		resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
	}

	private static void writeTruncateResponse(HttpServletResponse resp, TruncateResponse response)
			throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(DtoMapper.toJson(response));
	}

	/** The display name of the requesting user, resolved once and remembered on the tracker. */
	private static String resolveUserName(HttpServletRequest req, TaskProgressTracker tracker) {
		String stored = tracker.getUserName();
		if (stored != null) {
			return stored;
		}
		Person person = resolveCurrentPerson(req);
		if (person == null) {
			return "Anonymous";
		}
		try {
			String name = person.getName();
			if (name != null) {
				tracker.setUserName(name);
				return name;
			}
		} catch (RuntimeException ex) {
			Logger.error("Failed to resolve person name", ex, ChatServlet.class);
		}
		return "Anonymous";
	}

	private void handleImprovePrompt(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ImprovePromptRequest body;
		try {
			String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			body = ImprovePromptRequest.readImprovePromptRequest(new JsonReader(new StringR(jsonBody)));
		} catch (Exception ex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + ex.getMessage());
			return;
		}

		try {
			Result result = PromptImprovementProcessor.improve(
				currentTracker(), body.getRequestId(), body.getDraft());
			writePromptImprovementResponse(resp, result);
		} catch (PromptImprovementException ex) {
			int status = switch (ex.getFailure()) {
				case INVALID_REQUEST -> HttpServletResponse.SC_BAD_REQUEST;
				case DISABLED -> HttpServletResponse.SC_FORBIDDEN;
				case MISSING_AGENT -> HttpServletResponse.SC_SERVICE_UNAVAILABLE;
			};
			resp.sendError(status, ex.getMessage());
		} catch (ToolSecurityException ex) {
			// A refusal, not a failure: it gets the answer /chat/send gets and no ERROR line. The
			// improver itself is exempt from the agent allowlist - see PromptImprovementProcessor -
			// so reaching this means the model or an MCP server was refused, which is a decision an
			// administrator made and the user can read.
			Logger.info("Prompt improvement refused: " + ex.getMessage(), ChatServlet.class);
			forbidden(resp);
			resp.getWriter().write(DtoMapper.toJson(ImprovePromptResponse.create()
				.setRequestId(body.getRequestId())
				.setStatus(FORBIDDEN)
				.setReason(ex.reason() == null
					? ex.getMessage()
					: Resources.getInstance().getString(ex.reason()))));
		} catch (RuntimeException ex) {
			Logger.error("Prompt improvement failed: " + ex.getMessage(), ex, ChatServlet.class);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Prompt improvement failed.");
		}
	}

	private void handleCancelPromptImprovement(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		CancelPromptImprovementRequest body;
		try {
			String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			body = CancelPromptImprovementRequest.readCancelPromptImprovementRequest(
				new JsonReader(new StringR(jsonBody)));
		} catch (Exception ex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + ex.getMessage());
			return;
		}

		String requestId = body.getRequestId();
		if (requestId == null || requestId.isBlank()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty 'requestId'.");
			return;
		}
		boolean cancelled = currentTracker().cancelPromptImprovement(requestId.trim());
		writePromptImprovementResponse(resp, new Result(requestId.trim(), "",
			cancelled ? PromptImprovementProcessor.STATUS_CANCELLED
				: PromptImprovementProcessor.STATUS_STALE));
	}

	private static void writePromptImprovementResponse(HttpServletResponse resp, Result result)
			throws IOException {
		ImprovePromptResponse response = ImprovePromptResponse.create()
			.setRequestId(result.requestId())
			.setImprovedText(result.improvedText())
			.setStatus(result.status());
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(DtoMapper.toJson(response));
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
		UserInteractionStore store = state == null ? null : state.getUserInteractionStore();
		if (store == null) {
			// A late answer to a request whose run already ended (completed, cancelled, or timed
			// out): the interaction is no longer pending, so there is nothing to resolve. Treat
			// it as a benign no-op rather than an error - the user must not see a failure for
			// answering a question that the system already tore down (e.g. after cancelling the
			// orchestrator, which cancels its subagents' pending questions).
			Logger.info("Ignoring late interaction response for request " + requestId
				+ ": no active interaction store.", ChatServlet.class);
			resp.setContentType(CONTENT_TYPE_JSON);
			resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
			resp.getWriter().write("{\"status\":\"ignored\"}");
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

		// Propagate cancellation to still-running subagents: cancelling the orchestrator must
		// terminate the work it spawned instead of leaving it running in the background. Each
		// subagent's cancellation token also unblocks a subagent parked on a user question, so
		// its ask-user wait ends (and its pending interaction is resolved) rather than hanging
		// until the interaction timeout.
		SubagentJobRegistry subagentRegistry = tracker.getActiveSubagentRegistry();
		if (subagentRegistry != null) {
			int cancelledSubagents = subagentRegistry.cancelAll();
			if (cancelledSubagents > 0) {
				Logger.info("Cancelled " + cancelledSubagents + " running subagent(s) on orchestrator "
					+ "cancel.", ChatServlet.class);
			}
		}

		// Fire cooperative cancellation before interrupting: this notifies onCancel listeners so
		// teardown happens deterministically on abort, not only via the thread interrupt below.
		com.top_logic.tl.ai.tool.api.CancellationToken cancellationToken = state.getCancellationToken();
		if (cancellationToken != null) {
			cancellationToken.cancel();
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

		TLClassifier chatKind = TlAiExecutionFactory.getCHATSessionKindClassifier();
		PendingInteractionsResponse response = PendingInteractionsResponse.create();
		for (InteractionRequest request : AgentExecutionStore.getInstance().pendingInteractionsFor(person)) {
			AgentExecution execution = request.getExecution();
			AgentSession session = execution == null ? null : execution.getSession();
			// Only surface requests that map to a chat session the client can navigate to; runs of
			// other kinds (script, background) have no session-list item to badge.
			if (session == null || !chatKind.equals(session.getKind())) {
				continue;
			}
			String type = interactionTypeName(request.getType());
			PendingInteractionDto dto = PendingInteractionDto.create()
				.setRequestId(request.getRequestId())
				.setType(type)
				.setHeader(request.getHeader())
				.setPrompt(request.getPrompt())
				.setPayload(request.getPayload())
				.setSessionId(getObjectId(session))
				.setSessionTitle(session.getTitle());
			Date createdAt = request.getCreatedAt();
			if (createdAt != null) {
				dto.setCreatedAt(formatIso(createdAt));
				dto.setRemainingMs(DtoMapper.remainingMs(createdAt.getTime(), responseDeadline(type)));
			}
			response.addInteraction(dto);
		}

		resp.getWriter().write(DtoMapper.toJson(response));
	}

	/**
	 * How long the responder has for a request of the given kind.
	 *
	 * <p>
	 * The deadline is not persisted with the request, so it is taken from the configuration the
	 * waiting agent thread is using.
	 * </p>
	 *
	 * @param type
	 *        Lowercased interaction type as sent to the client.
	 */
	private static Duration responseDeadline(String type) {
		return TYPE_APPROVAL.equals(type)
			? InteractionTimeoutService.approvalTimeout()
			: InteractionTimeoutService.questionTimeout();
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
			TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, person);
			if (session == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			TaskProgressTracker tracker = currentTracker();

			// Activate session on the tracker (switches Chat + ChatMemory)
			if (tracker != null) {
				tracker.getSessionManager().switchSession(sessionId, person);
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
				describeLastPrompt(response, conv.getTurns());
			}

			// The ratings and the effort come from the session's metadata bag, which the switch
			// above has just loaded onto the tracker.
			if (tracker != null) {
				for (MessageFeedbackDto entry : tracker.getFeedback()) {
					response.addFeedback(entry);
				}
				String effort = tracker.getEffort();
				if (effort != null) {
					response.setLastEffort(effort);
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
			TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, person);
			if (session != null) {
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

	/**
	 * GET /chat/subagents/{id}/transcript - the persisted transcript of one background subagent's
	 * recorded child run, as a {@code turnsJson} array in the same shape a session load returns.
	 *
	 * <p>
	 * Lets the background-tasks panel open a subagent's full transcript (turns, tool calls). The
	 * subagent id is resolved against the active session's {@link SubagentJobRegistry} to its
	 * recorded child run; access is restricted to executions whose session the user owns.
	 * </p>
	 */
	private void handleSubagentTranscript(HttpServletRequest req, HttpServletResponse resp, String id)
			throws IOException {
		Person person = resolveCurrentPerson(req);
		if (person == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

		SubagentJobRegistry registry = currentTracker().getActiveSubagentRegistry();
		SubagentJob job = registry == null ? null : registry.get(id);
		String runId = job == null ? null : job.childRunId();
		if (runId == null || runId.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		AgentExecution execution = AgentExecutionStore.getInstance().findExecutionByRunId(runId);
		AgentSession session = execution == null ? null : execution.getSession();
		if (execution == null || session == null || !ChatHistoryService.isOwnedBy(session, person)) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String transcript = execution.getTranscript();
		ChatSessionDetailResponse response = ChatSessionDetailResponse.create().setId(id);
		if (transcript != null && !transcript.isEmpty()) {
			response.setMessagesJson(transcript);
			PersistedConversationV2 conv = PersistedConversationV2.readPersistedConversationV2(
				new JsonReader(new StringR(transcript)));
			for (PersistedTurn turn : conv.getTurns()) {
				computeContentHtml(turn);
			}
			response.setTurnsJson(serializeTurns(conv.getTurns()));
		}
		resp.getWriter().write(DtoMapper.toJson(response));
	}

	private void handleListSubagents(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		SubagentJobRegistry registry = currentTracker().getActiveSubagentRegistry();
		StringBuilder json = new StringBuilder("{\"subagents\":[");
		if (registry != null) {
			boolean first = true;
			for (SubagentJob job : registry.byStatus(SubagentJobStatus.values())) {
				if (!first) {
					json.append(",");
				}
				first = false;
				long tokens = job.result() != null ? job.result().tokenUsage().totalTokens() : 0;
				long durationMs = job.result() != null ? job.result().durationMs() : 0;
				// finalText doubles as the error summary for failed jobs; sending it lets the
				// client rebuild expandable finished rows after a reload, matching the live
				// subagent_completed/subagent_failed SSE events. Running jobs show their task.
				String detail = job.result() != null ? job.result().finalText() : job.taskBrief();
				// hasTranscript lets the client show a "view transcript" affordance only when the
				// recorded child run is available (GET /chat/subagents/{id}/transcript).
				boolean hasTranscript = job.childRunId() != null && !job.childRunId().isEmpty();
				json.append("{\"subagentId\":\"").append(jsonEscape(job.subagentId()))
					.append("\",\"targetAgentName\":\"").append(jsonEscape(job.targetAgentName()))
					.append("\",\"status\":\"").append(job.status().name())
					.append("\",\"totalTokens\":").append(tokens)
					.append(",\"durationMs\":").append(durationMs)
					.append(",\"hasTranscript\":").append(hasTranscript)
					.append(",\"detail\":\"").append(jsonEscape(detail != null ? detail : ""))
					.append("\"}");
			}
		}
		json.append("]}");
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(json.toString());
	}

	private void handleCancelSubagent(String id, HttpServletResponse resp) throws IOException {
		SubagentJobRegistry registry = currentTracker().getActiveSubagentRegistry();
		boolean cancelled = false;
		if (registry != null) {
			SubagentJob job = registry.get(id);
			if (job != null) {
				job.cancellationToken().cancel();
				cancelled = true;
			}
		}
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"cancelled\":" + cancelled + "}");
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
		TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, person);
		if (session != null) {
			ChatHistoryService.deleteSession(session);
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * The attachment types an upload may declare: every image a provider accepts, plus PDF.
	 *
	 * <p>
	 * Derived from {@link ImageContent#ACCEPTED_MEDIA_TYPES} rather than restated, so this check and
	 * the places that build the payload cannot drift apart - a type refused here must be exactly a
	 * type nothing downstream could have sent.
	 * </p>
	 */
	private static final Set<String> ALLOWED_MIME_TYPES = allowedMimeTypes();

	private static Set<String> allowedMimeTypes() {
		Set<String> allowed = new LinkedHashSet<>(ImageContent.ACCEPTED_MEDIA_TYPES);
		allowed.add("application/pdf");
		return Collections.unmodifiableSet(allowed);
	}

	/**
	 * Restores the context-usage figures of a re-opened session from its transcript.
	 *
	 * <p>
	 * Only the last turn that actually reached the model contributes: its last agent-loop
	 * iteration's prompt size is the exact context that model saw, and the window is looked up from
	 * the model that ran it. Turns written before the transcript carried a per-iteration usage
	 * leave the figures unset, so the client falls back to its own estimate rather than showing a
	 * summed prompt size as if it were one.
	 * </p>
	 */
	private static void describeLastPrompt(ChatSessionDetailResponse response, List<PersistedTurn> turns) {
		for (int i = turns.size() - 1; i >= 0; i--) {
			PersistedTurn turn = turns.get(i);
			if (!turn.hasLastIterationUsage()) {
				continue;
			}
			com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto usage = turn.getLastIterationUsage();
			if (promptTokens(usage) <= 0) {
				continue;
			}
			response.setLastPromptUsage(TokenUsageDto.create()
				.setInputTokens(usage.getInputTokens())
				.setOutputTokens(usage.getOutputTokens())
				.setThinkingTokens(usage.getThinkingTokens())
				.setCacheReadTokens(usage.getCacheReadTokens())
				.setCacheWriteTokens(usage.getCacheWriteTokens()));
			if (turn.hasContextComposition()) {
				var composition = turn.getContextComposition();
				response.setLastComposition(ContextCompositionEvent.create()
					.setSystemPromptTokens(composition.getSystemPromptTokens())
					.setToolDefinitionTokens(composition.getToolDefinitionTokens())
					.setMessageTokens(composition.getMessageTokens())
					.setToolResultTokens(composition.getToolResultTokens())
					.setToolCount(composition.getToolCount()));
			}
			String model = turn.getModel();
			if (model != null && !model.isEmpty()) {
				response.setLastModel(model);
				response.setContextWindow(contextWindowOf(model));
			}
			return;
		}
	}

	private static int promptTokens(
			com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto usage) {
		return usage.getInputTokens() + usage.getCacheReadTokens() + usage.getCacheWriteTokens();
	}

	/** Context window of a configured model, or zero when the model is unknown to this deployment. */
	private static int contextWindowOf(String model) {
		if (!LLMService.Module.INSTANCE.isActive()) {
			return 0;
		}
		ModelInfo info = LLMService.getInstance().getModel(model);
		return info == null || info.limits() == null ? 0 : info.limits().contextWindow();
	}

	private static String validateAttachments(List<AttachmentDto> attachments) {
		if (attachments.size() > MAX_ATTACHMENTS) {
			return "Too many attachments (max " + MAX_ATTACHMENTS + ")";
		}
		for (AttachmentDto att : attachments) {
			// Normalized first, so an upload declaring "image/jpg" or a charset parameter is judged by
			// the same rule that decides what the payload is finally built as.
			if (!ALLOWED_MIME_TYPES.contains(ImageContent.normalizeMediaType(att.getMimeType()))) {
				return "Unsupported file type: " + att.getMimeType()
					+ ". Allowed: " + String.join(", ", ALLOWED_MIME_TYPES) + ".";
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
	 *
	 * <p>
	 * A request that does not come from a signed-in user is refused here as well. The servlet
	 * filter in front of this one requires a TopLogic session, not an authenticated one, and an
	 * application that lets visitors in - as the demo does - gives every visitor a session as the
	 * anonymous account. Without this check a visitor could open the chat, run the agent and spend
	 * the installation's API budget, and every such conversation would be stored against the
	 * anonymous account where the next visitor reads it. The chat has no anonymous mode, so
	 * authentication is demanded once, centrally, rather than in each of the handlers below.
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
		// Only now is there a user to ask about: the interaction this request started with carries
		// no sub-session, and therefore no person.
		if (TLContext.isAnonymous()) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sign in to use the chat.");
			return;
		}
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

	/**
	 * Minimal JSON string escaping for inline JSON values.
	 */
	private static String jsonEscape(String s) {
		if (s == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(s.length() + 8);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"': sb.append("\\\""); break;
				case '\\': sb.append("\\\\"); break;
				case '\n': sb.append("\\n"); break;
				case '\r': sb.append("\\r"); break;
				case '\t': sb.append("\\t"); break;
				case '\b': sb.append("\\b"); break;
				case '\f': sb.append("\\f"); break;
				default:
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
			}
		}
		return sb.toString();
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

	/**
	 * The agents, models and MCP servers the caller may use.
	 *
	 * <p>
	 * Takes the request because the answer depends on who asks: the policy of {@code #392} filters
	 * this list to what a run of that account would actually accept.
	 * </p>
	 */
	private void handleCapabilities(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		TaskProgressTracker tracker = currentTracker();
		String improver = tracker == null ? null : tracker.getPromptImproverAgentName();
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(
			DtoMapper.toJson(ChatCapabilities.build(resolveCurrentPerson(req), improver)));
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

}
