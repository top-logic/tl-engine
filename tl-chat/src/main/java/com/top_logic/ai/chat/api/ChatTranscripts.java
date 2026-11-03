/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse;
import com.top_logic.ai.chat.api.messages.ChatSessionDto;
import com.top_logic.ai.chat.api.messages.ContextCompositionEvent;
import com.top_logic.ai.chat.api.messages.ConversationFeaturesDto;
import com.top_logic.ai.chat.api.messages.InFlightRunDto;
import com.top_logic.ai.chat.api.messages.MessageFeedbackDto;
import com.top_logic.ai.chat.api.messages.RecordedRunDto;
import com.top_logic.ai.chat.api.messages.TokenUsageDto;
import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.cost.CostConversion;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedPart;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn;
import com.top_logic.tl.ai.core.session.persist.messages.TextPartDto;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * What the chat UI reads about the chats of an account: the list of its chats, the transcript of one
 * chat with what it needs to be shown again, and the transcript of one background subagent.
 *
 * <p>
 * Every read is checked against the account: a chat of another account, a run of another account's
 * chat and an id that names nothing are answered alike - with <code>null</code> - so that an answer
 * says nothing about which ids exist.
 * </p>
 */
public final class ChatTranscripts {

	private ChatTranscripts() {
		// Static utility.
	}

	/**
	 * The chats of the given account, as the chat list shows them: id, title and when each was last
	 * updated, most recent first.
	 */
	public static List<ChatSessionDto> sessionList(Person person) {
		List<ChatSessionDto> result = new ArrayList<>();
		for (TLObject session : ChatHistoryService.getSessionsForUser(person)) {
			ChatSessionDto dto = ChatSessionDto.create()
				.setId(idOf(session))
				.setTitle(ChatHistoryService.getTitle(session));
			Date lastUpdated = ChatHistoryService.getLastUpdated(session);
			if (lastUpdated != null) {
				dto.setLastUpdated(formatIso(lastUpdated));
			}
			result.add(dto);
		}
		return result;
	}

	/**
	 * One chat of the given account as the window shows it when it opens the chat.
	 *
	 * <p>
	 * What the conversation looks like right now, in three descending preferences. A run in flight is
	 * the first of them, and it is the only one that can answer for a window other than the one that
	 * started the run: the open turn - the user's message, what the agent has said so far, its steps,
	 * the approval it is parked on - lives in the {@link ConversationLog} of the run's own window. The
	 * database has it complete only when the run ends - before that, only as far as its last
	 * checkpoint: the run's start and every wait for a person (#598). A second tab, a reloaded page and
	 * a new login all load the conversation from what is persisted, and were then shown a transcript
	 * that ended before what they were looking at (issue #512). The live log of the window's own
	 * conversation comes second, the recorded runs last.
	 * </p>
	 *
	 * <p>
	 * The ratings, the effort and the approval mode come from the chat's metadata bag as the window
	 * holds it, and are therefore given only for the chat the window has open: the chat is opened
	 * first (a command of the chat controls), and read afterwards.
	 * </p>
	 *
	 * @param tracker
	 *        The window's conversation that reads the chat.
	 * @param person
	 *        The acting account.
	 * @param sessionId
	 *        The chat.
	 * @return The chat, or <code>null</code> when the account owns no chat of that id.
	 */
	public static ChatSessionDetailResponse sessionDetail(TaskProgressTracker tracker, Person person,
			String sessionId) throws IOException {
		TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, person);
		if (session == null) {
			return null;
		}

		ConversationBackend backend = backendOf(session);
		ChatSessionDetailResponse response = ChatSessionDetailResponse.create()
			.setId(sessionId)
			.setTitle(ChatHistoryService.getTitle(session))
			.setFeatures(featuresDto(backend));
		boolean open = tracker != null && sessionId.equals(tracker.getActiveSessionId());

		ChatProcessor.AIProcessingState running = RunStateFrame.runInFlight(person, sessionId);
		String messagesJson = null;
		if (running != null) {
			ConversationLog runLog = running.getConversationLog();
			if (runLog != null && runLog.turnCount() > 0) {
				messagesJson = ConversationLogSerializer.toJson(runLog);
			}
		}
		if (messagesJson == null && open) {
			// The live log of the open chat: this window's own, which holds the open turn when the
			// run was started here.
			ConversationLog convLog = tracker.getSessionManager().getOrCreateConversationLog();
			if (convLog.turnCount() > 0) {
				messagesJson = ConversationLogSerializer.toJson(convLog);
			}
		}
		if (messagesJson == null) {
			messagesJson = ChatHistoryService.loadConversationJson(session);
		}
		if (messagesJson != null && !messagesJson.isEmpty()) {
			PersistedConversationV2 conv = PersistedConversationV2.readPersistedConversationV2(
				new JsonReader(new StringR(messagesJson)));
			appendStreamedText(conv, running, response);
			for (PersistedTurn turn : conv.getTurns()) {
				computeContentHtml(turn);
			}
			response.setTurnsJson(serializeTurns(conv.getTurns()));
			describeLastPrompt(response, conv.getTurns());
			describeRuns(response, session, conv.getTurns());
		}
		if (backend != null) {
			// The model of a backend is not one of this application's: the window is the one the
			// backend reports, none while it reports none.
			response.setContextWindow(backend.contextWindow());
		}

		if (open) {
			for (MessageFeedbackDto entry : tracker.getFeedback()) {
				response.addFeedback(entry);
			}
			String effort = tracker.getEffort();
			if (effort != null) {
				response.setLastEffort(effort);
			}
			response.setApprovalMode(tracker.getApprovalMode().name());
		}
		return response;
	}

	/**
	 * The transcript of one background subagent's child run, as a {@code turnsJson} array in the
	 * same shape {@link #sessionDetail(TaskProgressTracker, Person, String)} answers with: its live
	 * log while it works, its recorded execution once it has ended.
	 *
	 * <p>
	 * The subagent is looked up in the open chat's {@link SubagentJobRegistry}. A subagent the window
	 * holds no job for any more - one of an earlier server life, or of a chat the window no longer
	 * holds - is found by the run its recorded part names. That run is checked to belong to a chat of
	 * the account, so naming one gains nothing.
	 * </p>
	 *
	 * @param tracker
	 *        The window's conversation that reads the transcript.
	 * @param person
	 *        The acting account.
	 * @param subagentId
	 *        The subagent.
	 * @param runId
	 *        The child run its recorded part names, <code>null</code> when the card names none.
	 * @return The transcript, or <code>null</code> when there is no such subagent or run of the
	 *         account.
	 */
	public static ChatSessionDetailResponse subagentTranscript(TaskProgressTracker tracker, Person person,
			String subagentId, String runId) throws IOException {
		if (person == null) {
			return null;
		}
		SubagentJobRegistry registry = tracker == null ? null : tracker.getActiveSubagentRegistry();
		SubagentJob job = registry == null || subagentId == null ? null : registry.get(subagentId);
		String childRun = job != null ? job.childRunId() : runId;
		if (childRun == null || childRun.isEmpty()) {
			return null;
		}

		String transcript;
		ConversationLog liveLog = job == null ? null : job.liveLog();
		if (job != null && !job.status().isTerminal() && liveLog != null) {
			// While the child works its execution holds no transcript yet; what it has done so far
			// is in its live log. The job came from this window's own registry, so it is the user's.
			transcript = ConversationLogSerializer.toJson(liveLog.turns());
		} else {
			AgentExecution execution = AgentExecutionStore.getInstance().findExecutionByRunId(childRun);
			AgentSession session = execution == null ? null : execution.getSession();
			if (execution == null || session == null || !ChatHistoryService.isOwnedBy(session, person)) {
				return null;
			}
			transcript = execution.getTranscript();
		}
		ChatSessionDetailResponse response = ChatSessionDetailResponse.create().setId(subagentId);
		if (transcript != null && !transcript.isEmpty()) {
			PersistedConversationV2 conv = PersistedConversationV2.readPersistedConversationV2(
				new JsonReader(new StringR(transcript)));
			for (PersistedTurn turn : conv.getTurns()) {
				computeContentHtml(turn);
			}
			response.setTurnsJson(serializeTurns(conv.getTurns()));
		}
		return response;
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
	 * Adds the recorded runs of the conversation, by the turn each of them ended with: when they
	 * started and ended, and what they cost in the currency the chat shows costs in
	 * ({@link CostConversion}).
	 */
	private static void describeRuns(ChatSessionDetailResponse response, TLObject session,
			List<PersistedTurn> turns) {
		Map<String, ChatHistoryService.RecordedRun> runs = ChatHistoryService.recordedRuns(session);
		if (runs.isEmpty()) {
			return;
		}
		for (PersistedTurn turn : turns) {
			ChatHistoryService.RecordedRun run = runs.get(turn.getId());
			if (run == null) {
				continue;
			}
			RecordedRunDto dto = RecordedRunDto.create()
				.setTurnId(turn.getId())
				.setStartedAt(run.startedAt() == null ? 0 : run.startedAt().getTime())
				.setEndedAt(run.endedAt() == null ? 0 : run.endedAt().getTime());
			if (run.cost() != null) {
				Money shown = CostConversion.shown(run.cost(), null);
				dto.setCost(shown.amount()).setCurrency(shown.currency());
			}
			response.addRun(dto);
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

	/**
	 * Serializes turns to a JSON array string for {@link ChatSessionDetailResponse#getTurnsJson()}.
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

	private static String idOf(TLObject obj) {
		return obj.tHandle().getObjectName().toExternalForm();
	}

	private static String formatIso(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}
	/** The backend that answers the given chat, or <code>null</code> for the framework's own agent. */
	public static ConversationBackend backendOf(TLObject session) {
		if (!(session instanceof AgentSession agentSession)) {
			return null;
		}
		try {
			return ConversationBackendService.backendFor(agentSession);
		} catch (RuntimeException ex) {
			// The run asks again and fails there, where the user is told; a request that only reads
			// the conversation must not break on it.
			Logger.warn("Could not ask which backend answers chat " + session.tHandle().getObjectName().toExternalForm() + ".", ex,
				ChatTranscripts.class);
			return null;
		}
	}

	/**
	 * What the chat offers in a conversation answered by the given backend, or by the framework's
	 * own agent for <code>null</code>.
	 */
	public static ConversationFeaturesDto featuresDto(ConversationBackend backend) {
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

}
