/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session.persist;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.persist.messages.AssistantMessageDto;
import com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedContent;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage;
import com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto;
import com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto;
import com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto;
import com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto;

/**
 * Converts LLM {@link Message}s to and from their persistence DTOs.
 *
 * <p>
 * The conversation history itself is <em>not</em> stored this way - it is derived from the
 * canonical {@code ConversationLog} by {@code ConversationLogReplay}. This codec exists for
 * the messages a compaction produced, which cannot be derived from anything: they are the
 * only record of what the summarizer wrote.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.session.CompactionSidecar
 */
public final class MessageSerializer {

	private static final String TYPE_USER = "user";

	private static final String TYPE_ASSISTANT = "assistant";

	private static final String TYPE_TOOL_RESULT = "tool_result";

	private static final String TYPE_SYSTEM = "system";

	private static final String TYPE_COMPACTION_MARKER = "compaction_marker";

	private static final String CONTENT_TEXT = "text";

	private static final String CONTENT_THINKING = "thinking";

	private static final String CONTENT_TOOL_CALL = "tool_call";

	private static final String CONTENT_IMAGE = "image";

	private static final String CONTENT_PDF = "pdf";

	private static final String STATE_PENDING = "pending";

	private static final String STATE_RUNNING = "running";

	private static final String STATE_COMPLETED = "completed";

	private static final String STATE_FAILED = "failed";

	private MessageSerializer() {
		// Static utility
	}

	/** Converts a message to its DTO. */
	public static PersistedMessage toDto(Message message) {
		PersistedMessage dto = PersistedMessage.create();
		dto.setId(message.id());
		dto.setTimestamp(message.timestamp().toEpochMilli());

		if (message instanceof UserMessage user) {
			dto.setType(TYPE_USER);
			UserMessageDto data = UserMessageDto.create();
			data.setContent(toContentDtos(user.content()));
			dto.setUserData(data);
		} else if (message instanceof AssistantMessage assistant) {
			dto.setType(TYPE_ASSISTANT);
			AssistantMessageDto data = AssistantMessageDto.create();
			data.setContent(toContentDtos(assistant.content()));
			data.setUsage(ConversationLogSerializer.toUsageDto(assistant.usage()));
			data.setFinishReason(assistant.finishReason());
			dto.setAssistantData(data);
		} else if (message instanceof ToolResultMessage result) {
			dto.setType(TYPE_TOOL_RESULT);
			ToolResultMessageDto data = ToolResultMessageDto.create();
			data.setToolCallId(result.toolCallId());
			data.setToolName(result.toolName());
			data.setState(toStateDto(result.state()));
			dto.setToolResultData(data);
		} else if (message instanceof SystemMessage system) {
			dto.setType(TYPE_SYSTEM);
			SystemMessageDto data = SystemMessageDto.create();
			data.setContent(system.content());
			dto.setSystemData(data);
		} else if (message instanceof CompactionMarker marker) {
			dto.setType(TYPE_COMPACTION_MARKER);
			CompactionMarkerDto data = CompactionMarkerDto.create();
			data.setMessagesSummarized(marker.messagesSummarized());
			data.setTokensBefore(marker.tokensBefore());
			data.setTrigger(marker.trigger());
			dto.setMarkerData(data);
		}
		return dto;
	}

	/**
	 * Restores a message from its DTO.
	 *
	 * @throws IllegalArgumentException
	 *         If the DTO carries a message type this version does not know.
	 */
	public static Message fromDto(PersistedMessage dto) {
		String id = dto.getId();
		Instant timestamp = Instant.ofEpochMilli(dto.getTimestamp());

		switch (dto.getType()) {
			case TYPE_USER: {
				return new UserMessage(id, timestamp,
					fromContentDtos(dto.getUserData().getContent()));
			}
			case TYPE_ASSISTANT: {
				AssistantMessageDto data = dto.getAssistantData();
				return new AssistantMessage(id, timestamp,
					fromContentDtos(data.getContent()),
					ConversationLogSerializer.fromUsageDto(data.getUsage()),
					data.getFinishReason());
			}
			case TYPE_TOOL_RESULT: {
				ToolResultMessageDto data = dto.getToolResultData();
				return new ToolResultMessage(id, timestamp,
					data.getToolCallId(), data.getToolName(), fromStateDto(data.getState()));
			}
			case TYPE_SYSTEM: {
				return new SystemMessage(id, timestamp, dto.getSystemData().getContent());
			}
			case TYPE_COMPACTION_MARKER: {
				CompactionMarkerDto data = dto.getMarkerData();
				return new CompactionMarker(id, timestamp, data.getMessagesSummarized(),
					data.getTokensBefore(), data.getTrigger());
			}
			default:
				throw new IllegalArgumentException("Unknown message type: " + dto.getType());
		}
	}

	private static List<PersistedContent> toContentDtos(List<ContentPart> parts) {
		List<PersistedContent> dtos = new ArrayList<>(parts.size());
		for (ContentPart part : parts) {
			dtos.add(toContentDto(part));
		}
		return dtos;
	}

	private static PersistedContent toContentDto(ContentPart part) {
		PersistedContent dto = PersistedContent.create();
		if (part instanceof TextContent text) {
			dto.setType(CONTENT_TEXT);
			dto.setText(text.text());
		} else if (part instanceof ThinkingContent thinking) {
			dto.setType(CONTENT_THINKING);
			dto.setText(thinking.text());
			if (thinking.continuityToken() != null) {
				dto.setContinuityToken(thinking.continuityToken());
			}
		} else if (part instanceof ToolCall call) {
			dto.setType(CONTENT_TOOL_CALL);
			dto.setToolCallId(call.id());
			dto.setToolName(call.name());
			dto.setArguments(call.arguments());
		} else if (part instanceof ImageContent image) {
			dto.setType(CONTENT_IMAGE);
			dto.setUrl(image.url());
			dto.setMediaType(image.mediaType());
		} else if (part instanceof PdfContent pdf) {
			dto.setType(CONTENT_PDF);
			dto.setData(pdf.data());
			dto.setMediaType(pdf.mediaType());
		}
		return dto;
	}

	private static List<ContentPart> fromContentDtos(List<PersistedContent> dtos) {
		List<ContentPart> parts = new ArrayList<>(dtos.size());
		for (PersistedContent dto : dtos) {
			parts.add(fromContentDto(dto));
		}
		return parts;
	}

	private static ContentPart fromContentDto(PersistedContent dto) {
		switch (dto.getType()) {
			case CONTENT_TEXT:
				return new TextContent(dto.getText());
			case CONTENT_THINKING:
				return new ThinkingContent(dto.getText(), emptyToNull(dto.getContinuityToken()));
			case CONTENT_TOOL_CALL:
				return new ToolCall(dto.getToolCallId(), dto.getToolName(), dto.getArguments());
			case CONTENT_IMAGE:
				return new ImageContent(dto.getUrl(), dto.getMediaType());
			case CONTENT_PDF:
				return new PdfContent(dto.getData(), dto.getMediaType());
			default:
				throw new IllegalArgumentException("Unknown content type: " + dto.getType());
		}
	}

	private static ToolStateDto toStateDto(ToolState state) {
		ToolStateDto dto = ToolStateDto.create();
		if (state instanceof ToolState.Completed completed) {
			dto.setKind(STATE_COMPLETED);
			dto.setResult(completed.result());
			dto.setStartTime(completed.startTime().toEpochMilli());
			dto.setEndTime(completed.endTime().toEpochMilli());
			dto.setCompacted(completed.compacted());
			if (completed.summary() != null) {
				dto.setSummary(completed.summary());
			}
			dto.setTruncationHint(completed.truncationHint());
			dto.setContent(toContentDtos(completed.content()));
		} else if (state instanceof ToolState.Failed failed) {
			dto.setKind(STATE_FAILED);
			dto.setError(failed.error());
			dto.setStartTime(failed.startTime().toEpochMilli());
			dto.setEndTime(failed.endTime().toEpochMilli());
			dto.setSynthetic(failed.synthetic());
		} else if (state instanceof ToolState.Running running) {
			dto.setKind(STATE_RUNNING);
			dto.setArguments(running.arguments());
			dto.setStartTime(running.startTime().toEpochMilli());
		} else if (state instanceof ToolState.Pending pending) {
			dto.setKind(STATE_PENDING);
			dto.setArguments(pending.arguments());
		}
		return dto;
	}

	private static ToolState fromStateDto(ToolStateDto dto) {
		switch (dto.getKind()) {
			case STATE_COMPLETED:
				return new ToolState.Completed(dto.getResult(),
					Instant.ofEpochMilli(dto.getStartTime()),
					Instant.ofEpochMilli(dto.getEndTime()),
					dto.isCompacted(), emptyToNull(dto.getSummary()), dto.getTruncationHint(),
					fromContentDtos(dto.getContent()));
			case STATE_FAILED:
				return new ToolState.Failed(dto.getError(),
					Instant.ofEpochMilli(dto.getStartTime()),
					Instant.ofEpochMilli(dto.getEndTime()),
					dto.isSynthetic());
			case STATE_RUNNING:
				return new ToolState.Running(dto.getArguments(),
					Instant.ofEpochMilli(dto.getStartTime()));
			case STATE_PENDING:
				return new ToolState.Pending(dto.getArguments());
			default:
				throw new IllegalArgumentException("Unknown tool state: " + dto.getKind());
		}
	}

	private static String emptyToNull(String value) {
		return value != null && !value.isEmpty() ? value : null;
	}
}
