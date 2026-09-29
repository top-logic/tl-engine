/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session.persist;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.CompactionSidecar;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto;
import com.top_logic.tl.ai.core.session.persist.messages.CitationDto;
import com.top_logic.tl.ai.core.session.persist.messages.CompactionPartDto;
import com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto;
import com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto;
import com.top_logic.tl.ai.core.session.persist.messages.ErrorPartDto;
import com.top_logic.tl.ai.core.session.persist.messages.InteractionPartDto;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedPart;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn;
import com.top_logic.tl.ai.core.session.persist.messages.SystemPartDto;
import com.top_logic.tl.ai.core.session.persist.messages.TextPartDto;
import com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto;
import com.top_logic.tl.ai.core.session.persist.messages.ContextCompositionDto;
import com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto;
import com.top_logic.tl.ai.core.session.persist.messages.ToolCallPartDto;
import com.top_logic.tl.ai.core.session.persist.messages.TransactionPartDto;
import com.top_logic.tl.ai.tool.api.ToolMode;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Serializes and deserializes {@link ConversationLog} to and from JSON
 * using the msgbuf-generated {@link PersistedConversationV2} DTOs.
 */
public final class ConversationLogSerializer {

	private static final String TYPE_TEXT = "text";

	private static final String TYPE_THINKING = "thinking";

	private static final String TYPE_TOOL_CALL = "tool_call";

	private static final String TYPE_APPROVAL = "approval";

	private static final String TYPE_INTERACTION = "interaction";

	private static final String TYPE_COMPACTION = "compaction";

	private static final String TYPE_ERROR = "error";

	private static final String TYPE_TRANSACTION = "transaction";

	private static final String TYPE_DELEGATION = "delegation";

	private static final String TYPE_SYSTEM = "system";

	private ConversationLogSerializer() {
		// Static utility
	}

	/** Serializes a conversation log to JSON. */
	public static String toJson(ConversationLog log) {
		return toJson(log.turns());
	}

	/**
	 * Serializes the given turns to JSON in the same format as {@link #toJson(ConversationLog)}.
	 *
	 * <p>
	 * Used to persist a single run's slice of a longer conversation, e.g. as the transcript of
	 * one agent execution.
	 * </p>
	 */
	public static String toJson(List<Turn> turns) {
		PersistedConversationV2 dto = PersistedConversationV2.create();
		for (Turn turn : turns) {
			dto.addTurn(toTurnDto(turn));
		}
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			dto.writeTo(json);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to serialize conversation log", ex);
		}
		return buffer.toString();
	}

	/** Deserializes a conversation log from JSON. */
	public static ConversationLog fromJson(String json) {
		if (json == null || json.isEmpty()) {
			return new ConversationLog();
		}
		PersistedConversationV2 dto;
		try {
			dto = PersistedConversationV2.readPersistedConversationV2(
				new JsonReader(new StringR(json)));
		} catch (IOException ex) {
			throw new RuntimeException("Failed to deserialize conversation log", ex);
		}
		ConversationLog log = new ConversationLog();
		for (PersistedTurn turnDto : dto.getTurns()) {
			// The turn began when it began, not when it was read back (#568). A transcript that
			// carries no start - none written by this class - falls back to now, as before.
			long started = turnDto.getTimestamp();
			Turn turn = started > 0 ? log.startTurn(Instant.ofEpochMilli(started)) : log.startTurn();
			turn.setModel(turnDto.getModel());
			turn.setProvider(turnDto.getProvider());
			if (turnDto.hasUsage()) {
				turn.addUsage(fromUsageDto(turnDto.getUsage()));
			}
			// Overwritten unconditionally, because addUsage above also seeds the last-iteration
			// figure: a transcript written before the field existed carries the turn's summed usage
			// there, which is not a prompt size, so it must be dropped rather than shown.
			turn.setLastIterationUsage(turnDto.hasLastIterationUsage()
				? fromUsageDto(turnDto.getLastIterationUsage())
				: TokenUsage.EMPTY);
			if (turnDto.hasContextComposition()) {
				ContextCompositionDto composition = turnDto.getContextComposition();
				turn.setContextComposition(new AgentEvent.ContextComposition(turnDto.getId(),
					turnDto.getTurnNumber(), Instant.ofEpochMilli(turnDto.getTimestamp()),
					composition.getSystemPromptTokens(), composition.getToolDefinitionTokens(),
					composition.getMessageTokens(), composition.getToolResultTokens(),
					composition.getToolCount(), composition.getLoadedSkills()));
			}
			turn.setTurnContext(emptyToNull(turnDto.getTurnContext()));
			turn.setUserParts(turnDto.getUserParts().stream()
				.map(ConversationLogSerializer::fromPartDto).toList());
			for (PersistedPart partDto : turnDto.getAssistantParts()) {
				turn.addAssistantPart(fromPartDto(partDto));
			}
			log.completeTurn();
		}
		return log;
	}

	private static PersistedTurn toTurnDto(Turn turn) {
		PersistedTurn dto = PersistedTurn.create();
		dto.setId(turn.id());
		dto.setTurnNumber(turn.turnNumber());
		dto.setTimestamp(turn.timestamp().toEpochMilli());
		dto.setModel(nullToEmpty(turn.model()));
		dto.setProvider(nullToEmpty(turn.provider()));
		dto.setUsage(toUsageDto(turn.cumulativeUsage()));
		dto.setLastIterationUsage(toUsageDto(turn.lastIterationUsage()));
		AgentEvent.ContextComposition composition = turn.contextComposition();
		if (composition != null) {
			dto.setContextComposition(ContextCompositionDto.create()
				.setSystemPromptTokens(composition.systemPromptTokens())
				.setToolDefinitionTokens(composition.toolDefinitionTokens())
				.setMessageTokens(composition.messageTokens())
				.setToolResultTokens(composition.toolResultTokens())
				.setToolCount(composition.toolCount())
				.setLoadedSkills(composition.loadedSkills()));
		}
		dto.setTurnContext(nullToEmpty(turn.turnContext()));
		for (Part part : turn.userParts()) {
			dto.addUserPart(toPartDto(part));
		}
		for (Part part : turn.assistantParts()) {
			dto.addAssistantPart(toPartDto(part));
		}
		return dto;
	}

	private static PersistedPart toPartDto(Part part) {
		PersistedPart dto = PersistedPart.create();
		dto.setId(part.id());
		dto.setTimestamp(part.timestamp().toEpochMilli());

		if (part instanceof Part.TextPart tp) {
			dto.setType(TYPE_TEXT);
			TextPartDto data = TextPartDto.create();
			data.setText(tp.text());
			for (Citation citation : tp.citations()) {
				data.addCitation(toCitationDto(citation));
			}
			dto.setTextData(data);
		} else if (part instanceof Part.ThinkingPart tp) {
			dto.setType(TYPE_THINKING);
			ThinkingPartDto data = ThinkingPartDto.create();
			data.setText(tp.text());
			if (tp.provider() != null) {
				data.setProvider(tp.provider());
			}
			if (tp.continuityToken() != null) {
				data.setContinuityToken(tp.continuityToken());
			}
			dto.setThinkingData(data);
		} else if (part instanceof Part.ToolCallPart tc) {
			dto.setType(TYPE_TOOL_CALL);
			ToolCallPartDto data = ToolCallPartDto.create();
			data.setToolName(tc.toolName());
			data.setArgsJson(nullToEmpty(tc.argsJson()));
			data.setMode(tc.mode() != null ? tc.mode().name() : "");
			data.setStatus(tc.status().name());
			data.setResult(nullToEmpty(tc.result()));
			data.setErrorMessage(nullToEmpty(tc.errorMessage()));
			data.setDenyReason(nullToEmpty(tc.denyReason()));
			data.setDurationMs(tc.durationMs());
			dto.setToolCallData(data);
		} else if (part instanceof Part.ApprovalPart ap) {
			dto.setType(TYPE_APPROVAL);
			ApprovalPartDto data = ApprovalPartDto.create();
			data.setToolCallId(nullToEmpty(ap.toolCallId()));
			data.setToolName(nullToEmpty(ap.toolName()));
			data.setArgsJson(nullToEmpty(ap.argsJson()));
			data.setDisplayReason(nullToEmpty(ap.displayReason()));
			data.setMode(ap.mode() != null ? ap.mode().name() : "");
			data.setDecision(ap.decision() != null ? ap.decision().name() : "");
			data.setUserComment(nullToEmpty(ap.userComment()));
			data.setResolvedAt(ap.resolvedAt() != null ? ap.resolvedAt().toEpochMilli() : 0L);
			dto.setApprovalData(data);
		} else if (part instanceof Part.InteractionPart ip) {
			dto.setType(TYPE_INTERACTION);
			InteractionPartDto data = InteractionPartDto.create();
			data.setInteractionType(nullToEmpty(ip.interactionType()));
			data.setQuestion(nullToEmpty(ip.question()));
			data.setHeader(nullToEmpty(ip.header()));
			if (ip.options() != null) {
				data.setOptions(ip.options());
			}
			data.setMultiSelect(ip.multiSelect());
			data.setUserResponse(nullToEmpty(ip.userResponse()));
			data.setResolvedAt(ip.resolvedAt() != null ? ip.resolvedAt().toEpochMilli() : 0L);
			dto.setInteractionData(data);
		} else if (part instanceof Part.CompactionPart cp) {
			dto.setType(TYPE_COMPACTION);
			CompactionPartDto data = CompactionPartDto.create();
			data.setTokensBefore(cp.tokensBefore());
			data.setTokensAfter(cp.tokensAfter());
			data.setTrigger(nullToEmpty(cp.trigger()));
			if (cp.sidecar() != null) {
				data.setSidecar(toSidecarDto(cp.sidecar()));
			}
			dto.setCompactionData(data);
		} else if (part instanceof Part.ErrorPart ep) {
			dto.setType(TYPE_ERROR);
			ErrorPartDto data = ErrorPartDto.create();
			data.setKind(nullToEmpty(ep.kind()));
			data.setErrorMessage(nullToEmpty(ep.message()));
			dto.setErrorData(data);
		} else if (part instanceof Part.TransactionPart txp) {
			dto.setType(TYPE_TRANSACTION);
			TransactionPartDto data = TransactionPartDto.create();
			data.setType(txp.type() != null ? txp.type().name() : "");
			data.setRevisionNumber(txp.revisionNumber());
			data.setReason(nullToEmpty(txp.reason()));
			dto.setTransactionData(data);
		} else if (part instanceof Part.DelegationPart dp) {
			dto.setType(TYPE_DELEGATION);
			DelegationPartDto data = DelegationPartDto.create();
			data.setFromAgent(nullToEmpty(dp.fromAgent()));
			data.setToAgent(nullToEmpty(dp.toAgent()));
			data.setInputSummary(nullToEmpty(dp.inputSummary()));
			data.setResultSummary(nullToEmpty(dp.resultSummary()));
			data.setChildSessionId(nullToEmpty(dp.childSessionId()));
			data.setDurationMs(dp.durationMs());
			if (dp.childParts() != null) {
				for (Part childPart : dp.childParts()) {
					data.addChildPart(toPartDto(childPart));
				}
			}
			data.setTurnsUsed(dp.turnsUsed());
			if (dp.totalUsage() != null) {
				data.setTotalUsage(toUsageDto(dp.totalUsage()));
			}
			data.setStopReason(dp.stopReason() != null ? dp.stopReason().name() : "");
			dto.setDelegationData(data);
		} else if (part instanceof Part.SystemPart sp) {
			dto.setType(TYPE_SYSTEM);
			SystemPartDto data = SystemPartDto.create();
			data.setEventType(sp.eventType() != null ? sp.eventType().name() : "");
			data.setDetails(nullToEmpty(sp.details()));
			dto.setSystemData(data);
		}
		return dto;
	}

	private static Part fromPartDto(PersistedPart dto) {
		String id = dto.getId();
		Instant timestamp = Instant.ofEpochMilli(dto.getTimestamp());
		String type = dto.getType();

		switch (type) {
			case TYPE_TEXT: {
				TextPartDto data = dto.getTextData();
				List<Citation> citations = new ArrayList<>();
				if (data != null) {
					for (CitationDto item : data.getCitations()) {
						citations.add(fromCitationDto(item));
					}
				}
				return new Part.TextPart(id, timestamp, data == null ? "" : data.getText(), citations);
			}
			case TYPE_THINKING: {
				ThinkingPartDto data = dto.getThinkingData();
				return new Part.ThinkingPart(id, timestamp, data.getText(),
					emptyToNull(data.getContinuityToken()), emptyToNull(data.getProvider()));
			}
			case TYPE_TOOL_CALL: {
				ToolCallPartDto data = dto.getToolCallData();
				return new Part.ToolCallPart(
					id, timestamp,
					data.getToolName(),
					emptyToNull(data.getArgsJson()),
					data.getMode().isEmpty() ? null : ToolMode.valueOf(data.getMode()),
					Part.ToolCallStatus.valueOf(data.getStatus()),
					emptyToNull(data.getResult()),
					emptyToNull(data.getErrorMessage()),
					emptyToNull(data.getDenyReason()),
					data.getDurationMs());
			}
			case TYPE_APPROVAL: {
				ApprovalPartDto data = dto.getApprovalData();
				return new Part.ApprovalPart(
					id, timestamp,
					emptyToNull(data.getToolCallId()),
					emptyToNull(data.getToolName()),
					emptyToNull(data.getArgsJson()),
					emptyToNull(data.getDisplayReason()),
					data.getMode().isEmpty() ? null : ToolMode.valueOf(data.getMode()),
					data.getDecision().isEmpty() ? null
						: Part.ApprovalDecision.valueOf(data.getDecision()),
					emptyToNull(data.getUserComment()),
					data.getResolvedAt() > 0
						? Instant.ofEpochMilli(data.getResolvedAt()) : null);
			}
			case TYPE_INTERACTION: {
				InteractionPartDto data = dto.getInteractionData();
				return new Part.InteractionPart(
					id, timestamp,
					emptyToNull(data.getInteractionType()),
					emptyToNull(data.getQuestion()),
					emptyToNull(data.getHeader()),
					new ArrayList<>(data.getOptions()),
					data.isMultiSelect(),
					emptyToNull(data.getUserResponse()),
					data.getResolvedAt() > 0
						? Instant.ofEpochMilli(data.getResolvedAt()) : null);
			}
			case TYPE_COMPACTION: {
				CompactionPartDto data = dto.getCompactionData();
				return new Part.CompactionPart(
					id, timestamp,
					data.getTokensBefore(),
					data.getTokensAfter(),
					emptyToNull(data.getTrigger()),
					data.hasSidecar() ? fromSidecarDto(data.getSidecar()) : null);
			}
			case TYPE_ERROR: {
				ErrorPartDto data = dto.getErrorData();
				return new Part.ErrorPart(
					id, timestamp,
					emptyToNull(data.getKind()),
					emptyToNull(data.getErrorMessage()));
			}
			case TYPE_TRANSACTION: {
				TransactionPartDto data = dto.getTransactionData();
				return new Part.TransactionPart(
					id, timestamp,
					data.getType().isEmpty() ? null
						: Part.TransactionType.valueOf(data.getType()),
					data.getRevisionNumber(),
					emptyToNull(data.getReason()));
			}
			case TYPE_DELEGATION: {
				DelegationPartDto data = dto.getDelegationData();
				List<Part> childParts = new ArrayList<>();
				for (PersistedPart childDto : data.getChildParts()) {
					childParts.add(fromPartDto(childDto));
				}
				TokenUsage delegationUsage = data.hasTotalUsage()
					? fromUsageDto(data.getTotalUsage()) : null;
				StopReason delegationStopReason = data.getStopReason().isEmpty()
					? null : StopReason.valueOf(data.getStopReason());
				return new Part.DelegationPart(
					id, timestamp,
					emptyToNull(data.getFromAgent()),
					emptyToNull(data.getToAgent()),
					emptyToNull(data.getInputSummary()),
					emptyToNull(data.getResultSummary()),
					childParts,
					emptyToNull(data.getChildSessionId()),
					data.getDurationMs(),
					data.getTurnsUsed(),
					delegationUsage,
					delegationStopReason);
			}
			case TYPE_SYSTEM: {
				SystemPartDto data = dto.getSystemData();
				return new Part.SystemPart(
					id, timestamp,
					data.getEventType().isEmpty() ? null
						: Part.SystemEventType.valueOf(data.getEventType()),
					emptyToNull(data.getDetails()));
			}
			default:
				throw new IllegalArgumentException("Unknown part type: " + type);
		}
	}

	private static CompactionSidecarDto toSidecarDto(CompactionSidecar sidecar) {
		CompactionSidecarDto dto = CompactionSidecarDto.create();
		dto.setFingerprintVersion(sidecar.fingerprintVersion());
		dto.setFingerprint(sidecar.fingerprint());
		for (Message message : sidecar.replacementMessages()) {
			dto.addReplacementMessage(MessageSerializer.toDto(message));
		}
		return dto;
	}

	/**
	 * Restores a compaction's sidecar, or <code>null</code> if it cannot be read.
	 *
	 * <p>
	 * A sidecar is a cache over a canonical log that is still complete, so an unreadable one
	 * costs a session its compacted size and nothing else - whereas letting the failure out
	 * of here would cost it the whole log. That is the difference between a session that is
	 * large and a session that is gone, so a message shape this version does not know is
	 * logged and dropped rather than thrown.
	 * </p>
	 */
	private static CompactionSidecar fromSidecarDto(CompactionSidecarDto dto) {
		List<Message> messages = new ArrayList<>();
		try {
			for (PersistedMessage messageDto : dto.getReplacementMessages()) {
				messages.add(MessageSerializer.fromDto(messageDto));
			}
		} catch (RuntimeException ex) {
			Logger.warn("Dropping an unreadable compaction sidecar; the session will be"
				+ " rebuilt from its full history: " + ex.getMessage(), ex,
				ConversationLogSerializer.class);
			return null;
		}
		return new CompactionSidecar(dto.getFingerprintVersion(), dto.getFingerprint(), messages);
	}

	/** Converts a token usage to its DTO. */
	static TokenUsageDto toUsageDto(TokenUsage usage) {
		TokenUsageDto dto = TokenUsageDto.create();
		dto.setInputTokens(usage.inputTokens());
		dto.setOutputTokens(usage.outputTokens());
		dto.setThinkingTokens(usage.thinkingTokens());
		dto.setCacheReadTokens(usage.cacheReadTokens());
		dto.setCacheWriteTokens(usage.cacheWriteTokens());
		return dto;
	}

	/** Restores a token usage from its DTO. */
	static TokenUsage fromUsageDto(TokenUsageDto dto) {
		return new TokenUsage(
			dto.getInputTokens(),
			dto.getOutputTokens(),
			dto.getThinkingTokens(),
			dto.getCacheReadTokens(),
			dto.getCacheWriteTokens());
	}

	private static CitationDto toCitationDto(Citation citation) {
		CitationDto dto = CitationDto.create();
		dto.setIndex(citation.index());
		dto.setToolName(citation.toolName());
		dto.setToolCallId(citation.toolCallId());
		dto.setKind(citation.kind());
		dto.setKindLabel(citation.kindLabel());
		dto.setId(citation.id());
		dto.setLabel(citation.label());
		dto.setUrl(citation.url());
		dto.setPassage(citation.passage());
		dto.setExcerpt(citation.excerpt());
		return dto;
	}

	private static Citation fromCitationDto(CitationDto dto) {
		return new Citation(dto.getIndex(), dto.getToolName(), dto.getToolCallId(), dto.getKind(),
			dto.getKindLabel(), dto.getId(), dto.getLabel(), dto.getUrl(), dto.getPassage(),
			dto.getExcerpt());
	}

	private static String nullToEmpty(String value) {
		return value != null ? value : "";
	}

	private static String emptyToNull(String value) {
		return value != null && !value.isEmpty() ? value : null;
	}
}
