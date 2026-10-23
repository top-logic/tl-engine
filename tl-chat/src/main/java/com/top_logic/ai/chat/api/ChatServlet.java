/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest;
import com.top_logic.ai.chat.api.messages.ChatSendRequest;
import com.top_logic.ai.chat.api.messages.ChatSendResponse;
import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.ai.chat.api.messages.ContextCompositionEvent;
import com.top_logic.ai.chat.api.messages.ConversationFeaturesDto;
import com.top_logic.ai.chat.api.messages.FactDto;
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
import com.top_logic.ai.chat.api.messages.InFlightRunDto;
import com.top_logic.ai.chat.api.messages.InteractionOptionDto;
import com.top_logic.ai.chat.api.messages.InteractionResolvedEvent;
import com.top_logic.ai.chat.api.messages.PendingInteractionDto;
import com.top_logic.ai.chat.api.messages.PendingInteractionsResponse;
import com.top_logic.ai.chat.api.messages.RunCostDto;
import com.top_logic.ai.chat.api.messages.SubagentBudgetDto;
import com.top_logic.ai.chat.api.messages.SubagentJobDto;
import com.top_logic.ai.chat.api.messages.SubagentListResponse;
import com.top_logic.ai.chat.api.messages.SubagentMessageRequest;
import com.top_logic.ai.chat.api.messages.SubagentMessageResponse;
import com.top_logic.ai.chat.api.messages.UpdateTitleRequest;
import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatSessionManager;
import com.top_logic.ai.chat.layout.ChatState;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor.PromptImprovementException;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor.Result;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.ChatViewBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInbox;
import com.top_logic.tl.ai.core.cost.CostConversion;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.limits.BudgetAllowance;
import com.top_logic.tl.ai.core.limits.RunLimits;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentFact;
import com.top_logic.tl.ai.core.security.ToolSecurityException;
import com.top_logic.util.Resources;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.approval.InteractionAsker;
import com.top_logic.tl.ai.core.agent.approval.InteractionTimeoutService;
import com.top_logic.tl.ai.core.agent.approval.PendingApproval;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
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
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedPart;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn;
import com.top_logic.tl.ai.core.session.persist.messages.TextPartDto;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.TopLogicServlet;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * JSON API for the agentic chat: POST /chat/send, GET /chat/updates, POST /chat/stream (the chat's
 * frames on the window's event stream).
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
	private static final String PATH_STREAM = "/stream";
	private static final String PATH_SESSIONS = "/sessions";
	private static final String PATH_INTERACTIONS = "/interactions";
	private static final String PATH_SUBAGENTS = "/subagents";
	private static final String PATH_CAPABILITIES = "/capabilities";

	private static final String PATH_REGENERATE = "/regenerate";

	private static final String PATH_TRUNCATE = "/truncate";

	private static final String PATH_FEEDBACK = "/feedback";

	private static final String PATH_PREFERENCES = "/preferences";
	private static final String TRANSCRIPT_SUFFIX = "/transcript";
	private static final String MESSAGE_SUFFIX = "/message";

	/** The longest message the user may write to a subagent, in characters. */
	static final int MAX_SUBAGENT_MESSAGE_CHARS = 8000;
	private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

	/**
	 * The conversation this request named, put here by {@link #doService} so that the handlers below
	 * do not each have to read the parameter again.
	 *
	 * <p>
	 * Its default is the window's unnamed conversation, which is both what a request naming none
	 * addresses and what a handler driven without {@link #doService} - a servlet test - gets.
	 * </p>
	 */
	private static final Property<String> REQUEST_CONVERSATION =
		TypedAnnotatable.property(String.class, "tl.chat.request.conversation", "");

	/**
	 * The status of a request the acting account may not make.
	 *
	 * <p>
	 * A status of its own rather than the generic "error": the client shows the reason that comes with
	 * it, and the two are not the same event - one is a failure, the other is an answer.
	 * </p>
	 */
	private static final String FORBIDDEN = "forbidden";

	/**
	 * The {@code status} of a send this installation has no room for right now.
	 *
	 * <p>
	 * Distinct from {@code busy}, which says that <em>this chat</em> is answering something else, and
	 * from {@code error}, which says that something went wrong. The client shows the {@code reason}
	 * and lets the user try again.
	 * </p>
	 */
	private static final String REJECTED = "rejected";

	/**
	 * HTTP 429, which {@code HttpServletResponse} has no constant for.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc6585#section-4">RFC 6585, section 4</a>
	 */
	private static final int SC_TOO_MANY_REQUESTS = 429;

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
		if (pathInfo.equals(PATH_STREAM) || pathInfo.equals(PATH_STREAM + "/")) {
			handleAttachStream(req, resp);
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
		if (pathInfo.startsWith(PATH_SUBAGENTS + "/") && pathInfo.endsWith(MESSAGE_SUFFIX)) {
			String id = pathInfo.substring((PATH_SUBAGENTS + "/").length(),
				pathInfo.length() - MESSAGE_SUFFIX.length());
			handleSubagentMessage(req, resp, id);
			return;
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		advanceSessionRevision();
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing path");
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
		if (pathInfo.equals(PATH_PREFERENCES) || pathInfo.equals(PATH_PREFERENCES + "/")) {
			handlePutPreferences(req, resp);
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
		if (pathInfo.equals(PATH_STREAM) || pathInfo.equals(PATH_STREAM + "/")) {
			handleDetachStream(req, resp);
			return;
		}
		if (pathInfo.startsWith(PATH_SUBAGENTS + "/")) {
			String id = pathInfo.substring((PATH_SUBAGENTS + "/").length());
			handleCancelSubagent(req, resp, id);
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

		// The turn runs with the mode the switch shows at the send, whatever reached the server
		// before: a switch request still on its way, or one that failed, must not make the turn ask
		// while the browser answers by itself, or approve while the person sees the switch off.
		String shownMode = blankToNull(body.getApprovalMode());
		if (shownMode != null) {
			ApprovalMode shown;
			try {
				shown = ApprovalMode.valueOf(shownMode);
			} catch (IllegalArgumentException e) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid approvalMode: " + shownMode);
				return;
			}
			if (tracker.getApprovalMode() != shown) {
				if (activeSessionId != null) {
					applyApprovalMode(tracker, person, activeSessionId, shown);
				} else {
					tracker.setApprovalMode(shown);
				}
			}
			ApprovalChoice.save(shown);
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
		// The chat the message goes to: the one it names, else the one the window shows.
		String chatId = activeSessionId != null ? activeSessionId : tracker.getActiveSessionId();
		ConversationBackend backend = backendOf(chatId, person);
		// A conversation a backend answers runs no agent of this application, so what the agent
		// policy says about the account's agents does not apply to it; what the backend supports does.
		SendRefusal refusal = backend == null
			? SendRefusal.forRun(person, options)
			: (hasAttachments ? SendRefusal.unsupported(backend.features().attachments(),
				com.top_logic.ai.chat.backend.I18NConstants.ATTACHMENTS_NOT_SUPPORTED) : null);
		if (refusal != null) {
			writeRefused(resp, refusal,
				ChatSendResponse.create().setStatus(FORBIDDEN).setReason(refusal.reason()));
			return;
		}
		ChatProcessor.SendResult result = ChatProcessor.sendMessage(tracker, subSession, userName,
			messageText, attachments, options, facts(body.getFacts()));
		ConversationFeaturesDto features = chatId != null ? featuresDto(backend) : null;

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		if (result.isRejected()) {
			// Neither forbidden nor broken: this installation has no room for the run right now, and
			// the client shows the sentence rather than a failure.
			writeRefused(resp, new SendRefusal(SC_TOO_MANY_REQUESTS, result.getReason()),
				ChatSendResponse.create().setStatus(REJECTED).setReason(result.getReason()));
			return;
		}
		if (result.getTaskId() == null) {
			ChatSendResponse sendResponse = ChatSendResponse.create()
				.setStatus(result.isBusy() ? "busy" : "error");
			resp.getWriter().write(DtoMapper.toJson(sendResponse));
			return;
		}
		ChatSendResponse sendResponse = ChatSendResponse.create()
			.setTaskId(result.getTaskId())
			.setStatus("processing")
			.setFeatures(features);
		resp.getWriter().write(DtoMapper.toJson(sendResponse));
	}

	/**
	 * What the page stated about the situation this message is asked in.
	 *
	 * <p>
	 * The values were computed on the server - {@code ChatConversationControl} evaluates the chat
	 * element's {@code <context>} expressions in this account's context - and the browser echoes them
	 * back here, because the run happens on a background thread long after those channels were read.
	 * A client that sends something else therefore states something about its own page to its own
	 * agent; it gains no authority by it, because every tool still asks the security context, and it
	 * cannot reach another account's conversation.
	 * </p>
	 */
	private static List<EnvironmentFact> facts(List<FactDto> dtos) {
		if (dtos == null || dtos.isEmpty()) {
			return List.of();
		}
		List<EnvironmentFact> facts = new ArrayList<>(dtos.size());
		for (FactDto dto : dtos) {
			String name = blankToNull(dto.getName());
			if (name == null) {
				continue;
			}
			facts.add(new EnvironmentFact(name, dto.getValue(), dto.isStable()));
		}
		return List.copyOf(facts);
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
		// Unlike the other endpoints that name a chat, this one switches the window to it, as a send
		// does: it starts a run, and a run is sent into the chat the window shows and streams there.
		// The regenerate button sits under an answer of the chat on screen, so this is the chat the
		// window shows already.
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
		ConversationBackend backend = backendOf(sessionId, resolveCurrentPerson(req));
		SendRefusal refusal = backend == null
			? SendRefusal.forRun(resolveCurrentPerson(req), options)
			: SendRefusal.unsupported(backend.features().regenerate(),
				com.top_logic.ai.chat.backend.I18NConstants.REGENERATE_NOT_SUPPORTED);
		if (refusal != null) {
			writeRefused(resp, refusal,
				TruncateResponse.create().setStatus(FORBIDDEN).setReason(refusal.reason()));
			return;
		}

		// Before the turns are dropped: a regenerate refused afterwards would have removed the
		// answer the user still has and given them nothing in its place.
		// One reading of the account's spend for the pre-check here, the send's and the run's (#835).
		RunLimits.SpendReading spendReading = RunLimits.spendReading(tracker.getSessionOwner());
		String noRoom = ChatProcessor.noRoomReason(tracker, spendReading);
		if (noRoom != null) {
			writeRefused(resp, new SendRefusal(SC_TOO_MANY_REQUESTS, noRoom),
				TruncateResponse.create().setStatus(REJECTED).setReason(noRoom));
			return;
		}

		ChatState chat = tracker.getSessionManager().getActiveChatState();
		int turnCount = chat.log().turnCount();
		if (turnCount == 0) {
			writeTruncateResponse(resp, TruncateResponse.create().setStatus("error"));
			return;
		}
		Removed removed = removeTurnsFrom(tracker, chat, resolveCurrentPerson(req), turnCount - 1);
		if (removed.text().isEmpty()) {
			writeTruncateResponse(resp, TruncateResponse.create().setStatus("error"));
			return;
		}

		tracker.setEffort(blankToNull(body.getEffort()));
		String userName = resolveUserName(req, tracker);
		ChatProcessor.SendResult result = ChatProcessor.sendMessage(tracker,
			TLContextManager.getSubSession(), userName, removed.text(), List.of(), options,
			facts(body.getFacts()), spendReading);

		TruncateResponse response = TruncateResponse.create()
			.setRemovedTurns(removed.turns())
			.setText(removed.text());
		if (result.isRejected()) {
			writeRefused(resp, new SendRefusal(SC_TOO_MANY_REQUESTS, result.getReason()),
				response.setStatus(REJECTED).setReason(result.getReason()));
			return;
		}
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
		// Acts on the chat it names without switching the window to it: dropping turns starts no run,
		// and which chat the window shows is not this request's business. Busy means a run of that
		// chat is writing into the turns that would go; a run of another chat does not touch them.
		if (tracker.hasActiveProcessingStateFor(sessionId)) {
			writeTruncateResponse(resp, TruncateResponse.create().setStatus("busy"));
			return;
		}
		Person person = resolveCurrentPerson(req);
		ConversationBackend backend = backendOf(sessionId, person);
		SendRefusal refusal = backend == null ? null
			: SendRefusal.unsupported(backend.features().editMessages(),
				com.top_logic.ai.chat.backend.I18NConstants.EDIT_NOT_SUPPORTED);
		if (refusal != null) {
			writeRefused(resp, refusal,
				TruncateResponse.create().setStatus(FORBIDDEN).setReason(refusal.reason()));
			return;
		}
		ChatState chat = tracker.getSessionManager().chatStateFor(sessionId, person);
		Removed removed = removeTurnsFrom(tracker, chat, person, body.getFromTurn());
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
	 * @param chat
	 *        The chat to remove the turns from, whether or not the window shows it.
	 * @return how many turns went, and the user text of the first of them
	 */
	private static Removed removeTurnsFrom(TaskProgressTracker tracker, ChatState chat, Person person,
			int fromTurn) {
		ConversationLog log = chat.log();
		List<Turn> turns = log.turns();
		if (fromTurn < 0 || fromTurn >= turns.size()) {
			return new Removed(0, "");
		}
		String text = userTextOf(turns.get(fromTurn));
		int removed = log.removeFrom(fromTurn);

		ConversationMemory memory = chat.memory();
		if (memory != null) {
			ConversationLogReplay.rebuildMemory(log, memory.conversation());
		}
		chat.dropFeedbackFrom(fromTurn);

		String sessionId = chat.sessionId();
		if (sessionId != null) {
			// Guarded again here, not only in the handler: this is the call that deletes, and it must
			// be impossible to reach it through a path that forgot to ask whose session it is.
			TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, person);
			if (session != null) {
				ChatHistoryService.truncateConversation(session, fromTurn);
			}
		}
		tracker.getSessionManager().persist(chat);
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
		// The rated chat, not the one the window shows: a rating is no reason to switch it.
		ChatState chat = tracker.getSessionManager().chatStateFor(sessionId, resolveCurrentPerson(req));
		chat.setFeedback(body.getTurnIndex(), blankToNull(body.getRating()), body.getNote());
		tracker.getSessionManager().persist(chat);

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"status\":\"ok\"}");
	}

	/**
	 * The backend that answers the given chat of the given account, or <code>null</code> when the
	 * framework's own agent answers it - also for a chat that is not the account's.
	 */
	private static ConversationBackend backendOf(String sessionId, Person person) {
		if (sessionId == null || person == null) {
			return null;
		}
		return backendOf(ChatHistoryService.findOwnedSessionById(sessionId, person));
	}

	/** The backend that answers the given chat, or <code>null</code> for the framework's own agent. */
	private static ConversationBackend backendOf(TLObject session) {
		if (!(session instanceof AgentSession agentSession)) {
			return null;
		}
		try {
			return ConversationBackendService.backendFor(agentSession);
		} catch (RuntimeException ex) {
			// The run asks again and fails there, where the user is told; a request that only reads
			// the conversation must not break on it.
			Logger.warn("Could not ask which backend answers chat " + getObjectId(session) + ".", ex,
				ChatServlet.class);
			return null;
		}
	}

	/**
	 * What the chat offers in a conversation answered by the given backend, or by the framework's
	 * own agent for <code>null</code>.
	 */
	static ConversationFeaturesDto featuresDto(ConversationBackend backend) {
		BackendFeatures features = backend == null ? BackendFeatures.INTERNAL : backend.features();
		return ConversationFeaturesDto.create()
			.setRegenerate(features.regenerate())
			.setEditMessages(features.editMessages())
			.setAgentPicker(features.agentPicker())
			.setModelPicker(features.modelPicker())
			.setEffortPicker(features.effortPicker())
			.setMcpPicker(features.mcpPicker())
			.setAttachments(features.attachments())
			.setApprovals(features.approvals());
	}

	/** Answers a send that is refused, with the reason the client shows. */
	private static void writeRefused(HttpServletResponse resp, SendRefusal refusal, ChatSendResponse response)
			throws IOException {
		refused(resp, refusal.status());
		resp.getWriter().write(DtoMapper.toJson(response));
	}

	/** Answers a regenerate that is refused, with the reason the client shows. */
	private static void writeRefused(HttpServletResponse resp, SendRefusal refusal, TruncateResponse response)
			throws IOException {
		refused(resp, refusal.status());
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
	private static void refused(HttpServletResponse resp, int status) {
		resp.setStatus(status);
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
			refused(resp, HttpServletResponse.SC_FORBIDDEN);
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

	/**
	 * POST /chat/respond - the user's answer to an approval or a question.
	 *
	 * <p>
	 * The request is looked up by its correlation id among the runs <em>this account</em> has
	 * parked on a human ({@link PendingInteractionRegistry}), not among the runs of the window the
	 * answer arrives from. That is what lets a second tab, a reloaded page or a new login answer a
	 * request the first window started (issue #393): the run is parked on a future in its own
	 * process, and the account - not the window - is who the request is addressed to.
	 * </p>
	 *
	 * <p>
	 * A request of another account is not found here, and an answer to one is therefore worth
	 * exactly what an answer to a request that never existed is worth: nothing happens, and the
	 * caller is told the same thing in both cases, so the answer cannot be used to find out which
	 * ids exist.
	 * </p>
	 */
	private void handleRespond(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

		String requestId = null;
		String response = null;
		String toolName = null;
		String comment = null;
		String ownWords = null;
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> parsed = (Map<String, Object>) com.top_logic.basic.json.JSON.fromString(jsonBody);
			requestId = (String) parsed.get("requestId");
			response = (String) parsed.get("response");
			toolName = (String) parsed.get("toolName");
			comment = (String) parsed.get("comment");
			// What the user typed for a choice question, beside the labels in "response" (#679).
			ownWords = (String) parsed.get("ownWords");
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
			return;
		}

		if (requestId == null || response == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing requestId or response");
			return;
		}

		Person responder = resolveCurrentPerson(req);
		PendingInteractionRegistry.Waiting run = PendingInteractionRegistry.findWaiting(responder, requestId);
		if (run == null) {
			// Either a late answer to a request whose run already ended (completed, cancelled, or
			// timed out), or a request this account is not the responder of. Both are a benign
			// no-op rather than an error - the user must not see a failure for answering a question
			// that the system already tore down (e.g. after cancelling the orchestrator, which
			// cancels its subagents' pending questions), and the two must not be told apart.
			Logger.info("Ignoring interaction response for request " + requestId
				+ ": no run of this account is waiting for it.", ChatServlet.class);
			resp.setContentType(CONTENT_TYPE_JSON);
			resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
			resp.getWriter().write("{\"status\":\"ignored\"}");
			return;
		}
		UserInteractionStore store = run.store();

		// Include rejection comment in the response string so the ApprovalCallback receives it
		String resolvedResponse = response;
		if ("REJECTED".equals(response) && comment != null && !comment.isEmpty()) {
			resolvedResponse = "REJECTED:" + comment;
		}

		// Everything the decision changes is written before the waiting thread is let go: resolving
		// the future unparks the agent mid-turn, and it may call the next tool before this request
		// has returned. A tool approved "for this session" that the run learns of only afterwards
		// would be asked about again.
		ConversationLogListener logListener = run.logListener();
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

		// Into the run's own chat, not the answering window's nor the one its own window shows by
		// now: that chat's next runs are given the set. A subagent whose window has ended has no
		// such set any more.
		if ("APPROVED_FOR_SESSION".equals(response) && toolName != null && !toolName.isEmpty()
			&& run.chat() != null) {
			run.chat().addApprovedTool(toolName);
		}

		store.resolve(requestId, resolvedResponse, ownWords);

		// The run's own window learns the outcome from the run (approval_resolved). Every other
		// window of this account that listed the request has no run to hear it from.
		notifyInteractionResolved(responder, run.tracker(), requestId, resolvedResponse);

		resp.setContentType("application/json");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		String safeRequestId = requestId.replace("\\", "\\\\").replace("\"", "\\\"");
		resp.getWriter().write("{\"status\":\"ok\",\"requestId\":\"" + safeRequestId + "\"}");
	}

	/**
	 * Tells the account's other chat windows that a request they may be showing has been answered.
	 *
	 * <p>
	 * Best effort, and deliberately silent about failures: the answer has already reached the run,
	 * and a window that misses this frame shows a stale card until it next lists what is pending -
	 * which is what every window did before the frame existed.
	 * </p>
	 */
	private static void notifyInteractionResolved(Person responder, TaskProgressTracker runWindow,
			String requestId, String outcome) {
		InteractionResolvedEvent event = InteractionResolvedEvent.create()
			.setRequestId(requestId)
			.setOutcome(outcome);
		for (ChatEventHandler handler : ChatEventHandlerRegistry.findOthers(responder, runWindow)) {
			try {
				handler.sendInteractionResolved(event);
			} catch (RuntimeException ex) {
				Logger.info("Could not tell a window that interaction " + requestId
					+ " was answered: " + ex.getMessage(), ChatServlet.class);
			}
		}
	}

	/**
	 * POST /chat/approval-mode - turns "auto-approve" on or off for one conversation.
	 *
	 * <p>
	 * The switch is a setting of the conversation, and turning it off is how a user has every write
	 * confirmed again. So the body names the chat ({@code sessionId}), and the mode is stored in that
	 * chat's metadata at once - not only on this window's tracker, from where it reached the
	 * database only with the next turn and was read back as the mode of the last turn by every
	 * window that opened the chat in the meantime (#658). The account's other windows showing the
	 * chat are told the new mode and read the chat's metadata afresh with their next request, so
	 * none of them writes its older mode back.
	 * </p>
	 *
	 * <p>
	 * A run of the chat that is working right now takes the new mode with its next approval, in
	 * both directions, whichever window of the account started it (#943).
	 * </p>
	 *
	 * <p>
	 * Every switch is also the person's last choice ({@link ApprovalChoice}), which a new chat starts
	 * with. A body without {@code sessionId} comes from a conversation that has no chat yet: it only
	 * stores that choice, and the send that creates the chat names the mode (POST /chat/sessions).
	 * </p>
	 */
	private void handleApprovalMode(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

		String mode = null;
		String requestedSession = null;
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> parsed = (Map<String, Object>) com.top_logic.basic.json.JSON.fromString(jsonBody);
			mode = (String) parsed.get("mode");
			requestedSession = blankToNull((String) parsed.get("sessionId"));
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
			return;
		}

		if (mode == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'mode'");
			return;
		}

		ApprovalMode approvalMode;
		try {
			approvalMode = ApprovalMode.valueOf(mode);
		} catch (IllegalArgumentException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid mode: " + mode);
			return;
		}

		if (requestedSession != null) {
			String sessionId = ownedSessionId(req, resp, requestedSession);
			if (sessionId == null) {
				return;
			}
			applyApprovalMode(currentTracker(), resolveCurrentPerson(req), sessionId, approvalMode);
		}
		ApprovalChoice.save(approvalMode);

		resp.setContentType("application/json");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"status\":\"ok\",\"mode\":\"" + approvalMode.name() + "\"}");
	}

	/**
	 * Sets the approval mode of the chat {@code sessionId}: on this window's copy, on the copies its
	 * running runs hold, in its stored metadata, and on the account's other windows showing it.
	 */
	private static void applyApprovalMode(TaskProgressTracker tracker, Person person, String sessionId,
			ApprovalMode approvalMode) {
		// The chat named, not the one the window shows, and without switching to it. The mode is
		// stored below; a copy of the chat this window holds is brought in line here, and one it does
		// not hold reads the stored mode when it is loaded.
		ChatState held = tracker.getSessionManager().getChatState(sessionId);
		if (held != null) {
			held.setApprovalMode(approvalMode);
		}
		// A run of the chat reads the switch at every approval, from the copy it was started with,
		// which is another window's when another window started it (#943).
		for (ChatState running : PendingInteractionRegistry.chatsOf(person, sessionId)) {
			running.setApprovalMode(approvalMode);
		}
		// Written on its own rather than with the tracker's whole bag: a persisted bag keeps the
		// stored mode, so that no window writes back the one it opened the chat with.
		ChatHistoryService.updateMetadata(ChatHistoryService.findOwnedSessionById(sessionId, person),
			stored -> DtoMapper.withApprovalMode(stored, approvalMode));
		for (ChatEventHandler other : ChatEventHandlerRegistry.findForSession(person, sessionId)) {
			TaskProgressTracker window = other.getTracker();
			if (window == tracker) {
				continue;
			}
			window.getSessionManager().markChangedElsewhere(sessionId);
			try {
				other.sendApprovalMode(approvalMode);
			} catch (RuntimeException ex) {
				Logger.info("Could not tell a window that the approval mode of chat " + sessionId
					+ " changed: " + ex.getMessage(), ChatServlet.class);
			}
		}
	}

	/**
	 * POST /chat/cancel - stop the run the user is watching.
	 *
	 * <p>
	 * The body names the chat the stop button was pressed in ({@code sessionId}); a body naming
	 * none is a client from before issue #465 and is answered as it always was, with whatever run
	 * the asking window holds.
	 * </p>
	 *
	 * <p>
	 * A named chat is looked up among the runs of <em>this account</em>
	 * ({@link PendingInteractionRegistry#findBySession}), not among the runs of the window the stop
	 * arrives from. That is what lets a second tab, a reloaded page or a new login stop a run the
	 * first window started: the run's state hangs on that window's tracker, which the user has no
	 * way to get back to, while the run goes on spending the account's tokens (issue #465). It is
	 * also what makes the stop hit the chat the user is looking at rather than whatever else that
	 * window happens to be running.
	 * </p>
	 *
	 * <p>
	 * A chat of another account is not found here, and neither is one that does not exist: both
	 * answer {@code {"cancelled":false}}, so the answer cannot be used to find out which chats
	 * exist or which of them are running.
	 * </p>
	 */
	private void handleCancel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		TaskProgressTracker tracker = currentTracker();
		ChatProcessor.AIProcessingState state = tracker.getAnyActiveProcessingState();
		TaskProgressTracker runWindow = tracker;

		String sessionId = cancelledSessionId(req);
		if (sessionId != null) {
			PendingInteractionRegistry.PendingRun run =
				PendingInteractionRegistry.findBySession(resolveCurrentPerson(req), sessionId);
			if (run != null) {
				state = run.state();
				runWindow = run.tracker();
			} else if (state != null && !sessionId.equals(state.getSessionId())) {
				// This window is running something, but not the chat the user pressed stop in -
				// and the account has no run there. Stopping the other one would stop a chat the
				// user is not even looking at.
				state = null;
			}
		}

		if (state == null) {
			resp.setContentType(CONTENT_TYPE_JSON);
			resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
			resp.getWriter().write("{\"cancelled\":false}");
			return;
		}

		// Propagate cancellation to still-running subagents: cancelling the orchestrator must
		// terminate the work it spawned instead of leaving it running in the background. Each
		// subagent's cancellation token also unblocks a subagent parked on a user question, so
		// its ask-user wait ends (and its pending interaction is resolved) rather than hanging
		// until the interaction timeout.
		//
		// The registry belongs to a window's *active* chat, which is the only one a tracker hands
		// out; the run's window may have moved on to another chat since. Asked only while the two
		// are the same chat, because cancelling the subagents of a chat nobody asked about would
		// be worse than leaving this one's running until they finish.
		SubagentJobRegistry subagentRegistry = holdsRunsChat(runWindow, state)
			? runWindow.getActiveSubagentRegistry() : null;
		if (subagentRegistry != null) {
			// Stopped means stopped: what the cancelled subagents report does not start the lead
			// again.
			runWindow.holdSubagentWakes(state.getSessionId());
			int cancelledSubagents = subagentRegistry.cancelAll();
			if (cancelledSubagents > 0) {
				Logger.info("Cancelled " + cancelledSubagents + " running subagent(s) on orchestrator "
					+ "cancel.", ChatServlet.class);
			}
		}

		// Cooperative cancellation only, never an interrupt of the agent thread: that thread may be
		// reading the database, and an interrupt during a read closes H2's file for the whole
		// application. The token ends the stream, every wait of the run and the loop itself.
		com.top_logic.tl.ai.tool.api.CancellationToken cancellationToken = state.getCancellationToken();
		if (cancellationToken != null) {
			cancellationToken.cancel();
		}
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"cancelled\":true}");
	}

	/**
	 * The chat a {@code POST /chat/cancel} names, or <code>null</code> when it names none.
	 *
	 * <p>
	 * Unreadable is the same as unnamed here, and deliberately not an error: the stop button is the
	 * way out of a run that is going wrong, and a client whose body this servlet cannot read must
	 * still get the stop it had before the body existed.
	 * </p>
	 */
	private static String cancelledSessionId(HttpServletRequest req) throws IOException {
		String body = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
		if (body.isEmpty()) {
			return null;
		}
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> parsed = (Map<String, Object>) com.top_logic.basic.json.JSON.fromString(body);
			return blankToNull((String) parsed.get("sessionId"));
		} catch (Exception ex) {
			Logger.info("Ignoring an unreadable body of a cancel request: " + ex.getMessage(),
				ChatServlet.class);
			return null;
		}
	}

	/** Whether the given window is still on the chat the given run belongs to. */
	private static boolean holdsRunsChat(TaskProgressTracker window, ChatProcessor.AIProcessingState run) {
		return Objects.equals(run.getSessionId(), window.getActiveSessionId());
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
		String activeSessionId = tracker.getActiveSessionId();
		boolean processing = tracker.hasActiveProcessingStateFor(activeSessionId);
		String currentTaskId = ChatProcessor.getCurrentTaskId(tracker);
		if (!processing) {
			// Whether the chat is working is a fact about the chat, not about this window: the run
			// may have been started in a tab that is now closed, and a window that says the
			// conversation is idle offers a composer for a turn that is already under way and no
			// way to stop it (issue #512).
			ChatProcessor.AIProcessingState elsewhere = runInFlight(resolveCurrentPerson(req), activeSessionId);
			if (elsewhere != null) {
				processing = true;
				currentTaskId = elsewhere.getCurrentTaskId();
			}
		}

		ChatUpdatesResponse response = DtoMapper.toUpdatesResponse(tracker, processing, currentTaskId);

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(DtoMapper.toJson(response));
	}

	/**
	 * The state of the run this account has in flight in the given chat, or <code>null</code>.
	 *
	 * <p>
	 * A run belongs to a conversation, not to the window it was started in, and everything about a
	 * turn that is still being worked on lives in memory until the run ends. Asked through
	 * {@link PendingInteractionRegistry}, so a second tab, a reloaded page and a new login find the
	 * run exactly as the window that started it does - and an account that has no run there, or is
	 * looking at somebody else's chat, finds nothing.
	 * </p>
	 *
	 * @param owner
	 *        The acting account; <code>null</code> finds nothing.
	 * @param sessionId
	 *        The id of the chat; <code>null</code> finds nothing.
	 */
	private static ChatProcessor.AIProcessingState runInFlight(Person owner, String sessionId) {
		PendingInteractionRegistry.PendingRun run = PendingInteractionRegistry.findBySession(owner, sessionId);
		if (run == null || !run.state().isProcessing()) {
			return null;
		}
		return run.state();
	}

	/**
	 * Adds the paragraph the running turn is in the middle of writing to the last turn.
	 *
	 * <p>
	 * The log records streamed text at message boundaries, so between them it holds everything the
	 * turn has said except its unfinished last sentence. A window opening the chat mid-stream has to
	 * be shown that sentence too, or it renders a turn that stops earlier than the one the reader
	 * was watching. It is appended to the snapshot that is about to be sent and never written into
	 * the log - recording it is the agent's own business.
	 * </p>
	 *
	 * @param conv
	 *        The snapshot being sent; a no-op when it has no turn.
	 * @param running
	 *        The run in flight, or <code>null</code> when the conversation has none.
	 * @param response
	 *        The answer the snapshot is sent in; told where in the run's text the snapshot ends.
	 */
	private static void appendStreamedText(PersistedConversationV2 conv, ChatProcessor.AIProcessingState running,
			ChatSessionDetailResponse response) {
		if (running == null) {
			return;
		}
		ConversationLogListener listener = running.getLogListener();
		ConversationLogListener.StreamedText streamed =
			listener == null ? new ConversationLogListener.StreamedText("", 0) : listener.streamedText();
		// Where this snapshot ends in the run's text, so the window can tell which of the deltas it
		// is about to receive it already shows (issue #544).
		response.setInFlight(InFlightRunDto.create().setStreamedChars(streamed.end()));
		String pending = streamed.pending();
		if (pending.isEmpty() || conv.getTurns().isEmpty()) {
			return;
		}
		PersistedTurn last = conv.getTurns().get(conv.getTurns().size() - 1);
		last.addAssistantPart(PersistedPart.create()
			.setId("streaming-" + last.getId())
			.setType("text")
			.setTimestamp(System.currentTimeMillis())
			.setTextData(TextPartDto.create().setText(pending)));
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
	 *
	 * <p>
	 * It is also what a reloaded page rebuilds an open card from, and a card must offer what the
	 * agent asked. The persisted row does not hold everything the card is drawn from - an
	 * approval's tool mode, and whether a choice takes more than one answer - so those are read off
	 * the run that is still waiting, through {@link PendingInteractionRegistry} (issues #393,
	 * #466). Nothing about the shape is persisted: it matters only while a request is pending, and
	 * a request nobody is parked on can no longer be answered.
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
			// What the still-waiting run holds and the persisted row does not: an approval's tool
			// mode, and a question's shape.
			UserInteractionStore waiting = waitingStore(person, request.getRequestId());
			boolean approval = TYPE_APPROVAL.equals(type);
			UserInteractionRequest question = approval || waiting == null
				? null : waiting.pendingRequest(request.getRequestId());
			// A question nobody is parked on any more - the run ended, or this is the process after
			// a restart - is not offered: its shape is unknown, and an answer would reach nothing.
			// An approval row is kept as it always was, so this feed still badges a chat.
			if (!approval && question == null) {
				continue;
			}
			PendingInteractionDto dto = PendingInteractionDto.create()
				.setRequestId(request.getRequestId())
				.setType(type)
				.setHeader(request.getHeader())
				.setPrompt(request.getPrompt())
				.setPayload(request.getPayload())
				.setToolMode(toolMode(waiting, request.getRequestId()))
				.setSessionId(getObjectId(session))
				.setSessionTitle(session.getTitle());
			InteractionAsker asker = waiting == null ? null : waiting.asker();
			if (asker != null) {
				dto.setAskedBySubagentId(asker.subagentId()).setAskedByAgent(asker.agentName());
			}
			if (question != null) {
				dto.setMultiSelect(question.isMultiSelect());
				for (InteractionOptionDto option : DtoMapper.parseInteractionOptions(question.getOptions())) {
					dto.addOption(option);
				}
			}
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

	/**
	 * The interaction store of the run or the background subagent still parked on the given
	 * request, or <code>null</code>.
	 *
	 * <p>
	 * Everything a persisted {@link InteractionRequest} row does not record - an approval's tool
	 * mode, a question's shape, the subagent that asks - lives here, and only for as long as the run is waiting, which is
	 * also the only time a client may still answer.
	 * </p>
	 *
	 * @param responder
	 *        The account the request is addressed to.
	 * @param requestId
	 *        The correlation id of the approval or question.
	 */
	private static UserInteractionStore waitingStore(Person responder, String requestId) {
		PendingInteractionRegistry.Waiting waiting = PendingInteractionRegistry.findWaiting(responder, requestId);
		return waiting == null ? null : waiting.store();
	}

	/**
	 * The mode of the tool a still-waiting approval is about, or the empty string.
	 *
	 * <p>
	 * The persisted request records the reason the security context wrote, in English; the mode is
	 * what lets the client phrase the question in the reader's language, exactly as it does for the
	 * card the live event drew.
	 * </p>
	 *
	 * @param store
	 *        The store of the run still waiting, as {@link #waitingStore(Person, String)} found it;
	 *        <code>null</code> when no run is.
	 * @param requestId
	 *        The correlation id of the approval.
	 */
	private static String toolMode(UserInteractionStore store, String requestId) {
		PendingApproval approval = store == null ? null : store.pendingApproval(requestId);
		if (approval == null || approval.request().mode() == null) {
			return "";
		}
		return approval.request().mode().name();
	}

	/** Lowercased classifier name ("approval"/"question"/"choice") of an interaction type. */
	private static String interactionTypeName(TLClassifier type) {
		return type == null ? null : type.getName().toLowerCase(java.util.Locale.ROOT);
	}

	/**
	 * Starts sending this conversation's frames over the event stream the browser window already
	 * holds, to the listener id the request names ({@code stream}).
	 *
	 * <p>
	 * The chat has no event stream of its own: a page then holds one connection to the server, not
	 * two - see {@link ChatSSEHandler}. The browser subscribes to the listener id before it asks, so
	 * it misses nothing, and it chooses a new id for each attach, so the frames of a handler this one
	 * replaces reach no listener.
	 * </p>
	 *
	 * <p>
	 * A window the React layer does not know is answered as stale, like every request of a window
	 * this servlet does not know.
	 * </p>
	 */
	private void handleAttachStream(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String token = req.getParameter("stream");
		if (!ChatSSEHandler.isToken(token)) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid stream id.");
			return;
		}
		String window = windowId(req);
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(req.getSession());
		SSEUpdateQueue queue = window == null ? null : registry.getQueue(window);
		if (queue == null) {
			sendStaleWindow(resp);
			return;
		}
		ChatSSEHandler handler =
			new ChatSSEHandler(currentTracker(), resolveCurrentPerson(req), registry, window, queue, token);
		handler.start();

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"status\":\"ok\"}");
	}

	/**
	 * Stops the frames a {@code POST /chat/stream} with the same {@code stream} id started.
	 *
	 * <p>
	 * Sent when the last chat element of the conversation leaves the page. A handler started by a
	 * later attach of the same window is left alone. A tab that closes sends nothing; its handler
	 * stops when the window's page is unloaded.
	 * </p>
	 */
	private void handleDetachStream(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String token = req.getParameter("stream");
		if (ChatSSEHandler.isToken(token)
			&& ChatEventHandlerRegistry.find(currentTracker()) instanceof ChatSSEHandler handler
			&& handler.writesTo(ChatSSEHandler.listenerId(token))) {
			handler.stop();
		}
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write("{\"status\":\"ok\"}");
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
			ConversationBackend backend = backendOf(session);
			ChatSessionDetailResponse response = ChatSessionDetailResponse.create()
				.setId(sessionId)
				.setTitle(ChatHistoryService.getTitle(session))
				.setFeatures(featuresDto(backend));

			// What this conversation looks like right now, in three descending preferences.
			//
			// A run in flight is the first of them, and it is the only one that can answer for a
			// window other than the one that started the run: the open turn - the user's message,
			// what the agent has said so far, its steps, the approval it is parked on - lives in
			// the ConversationLog of the run's own window. The database has it complete only when
			// the run ends - before that, only as far as its last checkpoint: the run's start and
			// every wait for a person (#598). A second tab, a reloaded page and a new login all load
			// the conversation from what is persisted, and were then shown a transcript that ended
			// before what they were looking at (issue #512).
			ChatProcessor.AIProcessingState running = runInFlight(person, sessionId);
			String messagesJson = null;
			if (running != null) {
				ConversationLog runLog = running.getConversationLog();
				if (runLog != null && runLog.turnCount() > 0) {
					messagesJson = ConversationLogSerializer.toJson(runLog);
				}
			}
			if (messagesJson == null && tracker != null) {
				// The live log of the now-active session: this window's own, which holds the open
				// turn when the run was started here.
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
				appendStreamedText(conv, running, response);
				for (PersistedTurn turn : conv.getTurns()) {
					computeContentHtml(turn);
				}
				response.setTurnsJson(serializeTurns(conv.getTurns()));
				describeLastPrompt(response, conv.getTurns());
				describeRunCosts(response, session, conv.getTurns());
			}
			if (backend != null) {
				// The model of a backend is not one of this application's: the window is the one the
				// backend reports, none while it reports none.
				response.setContextWindow(backend.contextWindow());
			}

			// The ratings, the effort and the approval mode come from the session's metadata bag,
			// which the switch above has just loaded onto the tracker.
			if (tracker != null) {
				for (MessageFeedbackDto entry : tracker.getFeedback()) {
					response.addFeedback(entry);
				}
				String effort = tracker.getEffort();
				if (effort != null) {
					response.setLastEffort(effort);
				}
				response.setApprovalMode(tracker.getApprovalMode().name());
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
		ApprovalMode approvalMode = ApprovalMode.MANUAL;
		String requestedMode = blankToNull(body.getApprovalMode());
		if (requestedMode != null) {
			try {
				approvalMode = ApprovalMode.valueOf(requestedMode);
			} catch (IllegalArgumentException e) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid approvalMode: " + requestedMode);
				return;
			}
		}

		// What the page shows the chat beside, stamped before the browser sends the first message.
		InteractionContext interaction = TLContextManager.getInteraction();
		TLObject subject = ChatViewBinding.subjectOf(TLContextManager.getSubSession(),
			interaction == null ? "" : interaction.get(REQUEST_CONVERSATION));
		TLObject session = ChatHistoryService.createSession(sessionTitle, person, approvalMode, subject);
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
	 * GET /chat/subagents/{id}/transcript - the transcript of one background subagent's child run, as
	 * a {@code turnsJson} array in the same shape a session load returns: its live log while it works,
	 * its recorded execution once it has ended. A subagent this window holds no job for any more is
	 * found by the {@code runId} parameter its recorded part names.
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
		// A subagent of an earlier server life, or of a chat this window no longer holds, has no job
		// any more; the card that asks names the run its part recorded. The run is still checked to
		// belong to this user below, so naming one gains nothing.
		String runId = job != null ? job.childRunId() : req.getParameter("runId");
		if (runId == null || runId.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String transcript;
		ConversationLog liveLog = job == null ? null : job.liveLog();
		if (job != null && !job.status().isTerminal() && liveLog != null) {
			// While the child works its execution holds no transcript yet; what it has done so far
			// is in its live log. The job came from this window's own registry, so it is the user's.
			transcript = ConversationLogSerializer.toJson(liveLog.turns());
		} else {
			AgentExecution execution = AgentExecutionStore.getInstance().findExecutionByRunId(runId);
			AgentSession session = execution == null ? null : execution.getSession();
			if (execution == null || session == null || !ChatHistoryService.isOwnedBy(session, person)) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			transcript = execution.getTranscript();
		}
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
		SubagentListResponse response = SubagentListResponse.create();
		if (registry != null) {
			for (SubagentJob job : registry.byStatus(SubagentJobStatus.values())) {
				long tokens = job.result() != null ? job.result().tokenUsage().totalTokens() : 0;
				long durationMs = job.result() != null ? job.result().durationMs() : 0;
				// finalText doubles as the error summary for failed jobs; sending it lets the
				// client rebuild expandable finished rows after a reload, matching the live
				// subagent_completed/subagent_failed SSE events. Running jobs show their task.
				String detail = job.result() != null ? job.result().finalText() : job.taskBrief();
				// hasTranscript lets the client show a "view transcript" affordance only when the
				// recorded child run is available (GET /chat/subagents/{id}/transcript).
				boolean hasTranscript = job.childRunId() != null && !job.childRunId().isEmpty();
				response.addSubagent(withCost(SubagentJobDto.create()
					.setSubagentId(Objects.toString(job.subagentId(), ""))
					.setTargetAgentName(Objects.toString(job.targetAgentName(), ""))
					.setStatus(job.status().name())
					.setTotalTokens(tokens)
					.setDurationMs(durationMs)
					.setHasTranscript(hasTranscript)
					.setParentToolCallId(Objects.toString(job.parentToolCallId(), ""))
					.setTask(Objects.toString(job.taskBrief(), ""))
					.setChildRunId(Objects.toString(job.childRunId(), ""))
					.setStopReason(job.result() != null && job.result().stopReason() != null
						? job.result().stopReason().name() : "")
					.setDetail(Objects.toString(detail, "")), job.cost()));
			}
			response.setBudget(budgetOf(registry));
		}
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(DtoMapper.toJson(response));
	}

	/**
	 * POST /chat/subagents/{id}/message - the user writes to one running background subagent from its
	 * card.
	 *
	 * <p>
	 * The message goes into the subagent's inbox, which {@code send_to_subagent} fills too, and the
	 * subagent reads it at the start of its next turn. Any window of the account reaches the subagent,
	 * whichever window's run started it - but only in a chat of this account: another account's chat
	 * and a subagent that does not exist are answered alike, so the answer says nothing about which
	 * exist. A subagent that has ended reads no more messages, and is answered so.
	 * </p>
	 */
	private void handleSubagentMessage(HttpServletRequest req, HttpServletResponse resp, String id)
			throws IOException {
		Person person = resolveCurrentPerson(req);
		if (person == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		SubagentMessageRequest body;
		try {
			String json = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			body = SubagentMessageRequest.readSubagentMessageRequest(new JsonReader(new StringR(json)));
		} catch (Exception ex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + ex.getMessage());
			return;
		}
		String text = body.getText() == null ? "" : body.getText().strip();
		if (text.isEmpty()) {
			writeSubagentMessageAnswer(resp, HttpServletResponse.SC_BAD_REQUEST, "empty", "");
			return;
		}
		if (text.length() > MAX_SUBAGENT_MESSAGE_CHARS) {
			text = text.substring(0, MAX_SUBAGENT_MESSAGE_CHARS);
		}
		SubagentJob job = findOwnedSubagent(person, body.getSessionId(), id, body.getParentToolCallId());
		if (job == null) {
			writeSubagentMessageAnswer(resp, HttpServletResponse.SC_NOT_FOUND, "unknown", "");
			return;
		}
		SubagentInbox.Message message = SubagentInbox.Message.of(SubagentInbox.Sender.USER, text);
		if (job.status().isTerminal() || !job.inbox().post(message)) {
			writeSubagentMessageAnswer(resp, HttpServletResponse.SC_CONFLICT, "ended", "");
			return;
		}
		writeSubagentMessageAnswer(resp, HttpServletResponse.SC_OK, "sent", message.id());
	}

	/**
	 * The subagent a request to one subagent names, found in the named chat of this account,
	 * whichever of the account's windows started it; without a chat named, in the chat this window
	 * shows.
	 *
	 * @return The subagent, or <code>null</code> when there is no account, no such chat of this
	 *         account, or no such subagent in it - which callers answer alike, so that the answer says
	 *         nothing about which exist.
	 */
	private static SubagentJob findOwnedSubagent(Person person, String sessionId, String subagentId,
			String parentToolCallId) {
		if (person == null) {
			return null;
		}
		String chat = blankToNull(sessionId);
		if (chat == null) {
			chat = currentTracker().getActiveSessionId();
		}
		if (chat == null || ChatHistoryService.findOwnedSessionById(chat, person) == null) {
			return null;
		}
		return ChatSessionManager.findSubagent(chat, subagentId, parentToolCallId);
	}

	private static void writeSubagentMessageAnswer(HttpServletResponse resp, int code, String status,
			String messageId) throws IOException {
		resp.setStatus(code);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(DtoMapper.toJson(SubagentMessageResponse.create()
			.setStatus(status)
			.setMessageId(messageId)));
	}

	/**
	 * The job with its cost, in the currency the chat shows costs in - the system currency at
	 * today's rate, or US dollars without one ({@link CostConversion}); without one while its run has
	 * not reported one.
	 */
	private static SubagentJobDto withCost(SubagentJobDto job, Money cost) {
		if (cost == null) {
			return job;
		}
		Money shown = CostConversion.shown(cost, null);
		return job.setCost(shown.amount()).setCurrency(shown.currency());
	}

	/**
	 * The budget the subagents of this list spend from: the allowance of the run that started them,
	 * which they share (#783). Its headroom when that run started, per kind; -1 for a kind it does
	 * not bound. <code>null</code> when no subagent's run is bounded.
	 */
	private static SubagentBudgetDto budgetOf(SubagentJobRegistry registry) {
		for (SubagentJob job : registry.byStatus(SubagentJobStatus.values())) {
			BudgetAllowance budget = job.budget();
			if (!budget.isLimited()) {
				continue;
			}
			BudgetAllowance.MoneyBudget money = budget.moneyBudget();
			return SubagentBudgetDto.create()
				.setTokens(budget.isTokenLimited() ? budget.tokenHeadroom() : -1)
				.setMoney(money != null ? money.headroom() : -1)
				.setCurrency(money != null ? money.currency() : "");
		}
		return null;
	}

	/**
	 * DELETE /chat/subagents/{id}?sessionId=&parentToolCallId= - stops one background subagent, from
	 * its card or from the panel above the composer.
	 *
	 * <p>
	 * Found like the subagent a message goes to: in the named chat of this account, whichever of the
	 * account's windows started it - the window that asks need not be the one whose run started the
	 * subagent. Without a chat named, the chat this window shows. A chat of another account and an
	 * unknown subagent both answer <code>{"cancelled":false}</code>.
	 * </p>
	 */
	private void handleCancelSubagent(HttpServletRequest req, HttpServletResponse resp, String id)
			throws IOException {
		SubagentJob job = findOwnedSubagent(resolveCurrentPerson(req), req.getParameter("sessionId"), id,
			req.getParameter("parentToolCallId"));
		boolean cancelled = job != null && !job.status().isTerminal();
		if (cancelled) {
			job.cancellationToken().cancel();
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
					.setToolCount(composition.getToolCount())
					.setLoadedSkills(composition.getLoadedSkills()));
			}
			String model = turn.getModel();
			if (model != null && !model.isEmpty()) {
				response.setLastModel(model);
				response.setContextWindow(contextWindowOf(model));
			}
			return;
		}
	}

	/**
	 * Adds what the recorded runs of the conversation cost, by the turn each of them ended with, in
	 * the currency the chat shows costs in ({@link CostConversion}).
	 */
	private static void describeRunCosts(ChatSessionDetailResponse response, TLObject session,
			List<PersistedTurn> turns) {
		Map<String, Money> costs = ChatHistoryService.runCosts(session);
		if (costs.isEmpty()) {
			return;
		}
		for (PersistedTurn turn : turns) {
			Money cost = costs.get(turn.getId());
			if (cost == null) {
				continue;
			}
			Money shown = CostConversion.shown(cost, null);
			response.addRunCost(RunCostDto.create()
				.setTurnId(turn.getId())
				.setCost(shown.amount())
				.setCurrency(shown.currency()));
		}
	}

	private static int promptTokens(
			com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto usage) {
		return usage.getInputTokens() + usage.getCacheReadTokens() + usage.getCacheWriteTokens();
	}

	/** Context window of a configured model, or zero when the model is unknown to this deployment. */
	private static int contextWindowOf(String model) {
		if (!LLMProviderService.Module.INSTANCE.isActive()) {
			return 0;
		}
		ModelInfo info = LLMProviderService.getInstance().getModel(model);
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
	 * The conversation this request addresses, as the client's {@code conversation} parameter.
	 *
	 * <p>
	 * A window can show more than one: a page's chat beside a {@code <chat-dock>}'s, two
	 * {@code <agent-chat>} elements, a chat in a dialog over the one on the page. A request that
	 * names none addresses the window's unnamed conversation, which is what a host showing one chat
	 * per window may use (the classic {@code chat-client.js} did until #618).
	 * </p>
	 *
	 * <p>
	 * Not {@code chatId}: that parameter is taken, by {@code PUT /chat/preferences}, for the key a
	 * <em>layout</em> is stored under. The two are the same string for every element of a chat and
	 * differ for an {@code <agent-chat>}, whose layout is the one-element chat's and whose
	 * conversation is its own.
	 * </p>
	 */
	private static String conversation(HttpServletRequest req) {
		return ChatSessionRegistry.key(req.getParameter("conversation"));
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
		String conversation = conversation(req);
		if (ChatSessionRegistry.tracker(subSession, conversation) == null) {
			sendStaleWindow(resp);
			return;
		}
		// Install the resolved sub-session directly into this request's interaction, the same way
		// the layout AJAXServlet binds the addressed sub-session. The interaction is request-scoped
		// (set up by TopLogicServlet without a sub-session) and torn down at the end of the request,
		// so no explicit restore is needed. Going through ThreadContextManager.inContext(...) would
		// instead log an "Overriding subsession" info on every request.
		TLContextManager.getInteraction().installSubSessionContext(subSession);
		// Which conversation of the window this is, read once: the handlers below resolve their
		// tracker through currentTracker(), which has no request to read the parameter from.
		TLContextManager.getInteraction().set(REQUEST_CONVERSATION, conversation);
		// Only now is there a user to ask about: the interaction this request started with carries
		// no sub-session, and therefore no person.
		if (TLContext.isAnonymous()) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sign in to use the chat.");
			return;
		}
		super.doService(req, resp);
	}

	/**
	 * The {@link TaskProgressTracker} of the conversation the current request addresses.
	 *
	 * <p>
	 * Never null inside a handler: {@link #doService} rejects a request whose sub-session or
	 * conversation cannot be resolved before any handler runs, and puts what it resolved onto the
	 * interaction.
	 * </p>
	 */
	private static TaskProgressTracker currentTracker() {
		InteractionContext interaction = TLContextManager.getInteraction();
		String conversation = interaction == null ? "" : interaction.get(REQUEST_CONVERSATION);
		return ChatSessionRegistry.tracker(TLContextManager.getSubSession(), conversation);
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

	/**
	 * {@code PUT /chat/preferences?chatId=…} - the layout of one chat.
	 *
	 * <p>
	 * The {@code chatId} is the key the layout is stored under. The body is what the user decided,
	 * so a rail the user never touched arrives without {@code railOpen} and keeps following the
	 * configured default.
	 * </p>
	 */
	private void handlePutPreferences(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ChatPreferencesDto body;
		try {
			String jsonBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			body = ChatPreferencesDto.readChatPreferencesDto(new JsonReader(new StringR(jsonBody)));
		} catch (Exception ex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + ex.getMessage());
			return;
		}
		String chatId = req.getParameter("chatId");
		ChatPreferencesStore.save(chatId, body);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.getWriter().write(DtoMapper.toJson(ChatPreferencesStore.load(chatId)));
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

}
