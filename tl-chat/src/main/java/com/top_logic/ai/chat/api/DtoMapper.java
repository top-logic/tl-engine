/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.LinkedHashMap;

import com.top_logic.ai.service.approval.UserInteractionRequest;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.ChatUpdatesResponse;
import com.top_logic.ai.chat.api.messages.CurrentTaskDto;
import com.top_logic.ai.chat.api.messages.InteractionOptionDto;
import com.top_logic.ai.chat.api.messages.MessageDto;
import com.top_logic.ai.chat.api.messages.PersistedChat;
import com.top_logic.ai.chat.api.messages.PersistedChatEntry;
import com.top_logic.ai.chat.api.messages.SessionMetadata;
import com.top_logic.ai.chat.api.messages.TimelineEventDto;
import com.top_logic.ai.chat.api.messages.TokenUsageDto;
import com.top_logic.ai.chat.api.messages.UserInteractionEvent;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatEntry;
import com.top_logic.ai.chat.layout.ChatMessage;
import com.top_logic.ai.chat.layout.ChatTask;
import com.top_logic.ai.chat.layout.ChatTimelineEvent;
import com.top_logic.ai.chat.layout.ChatToolCall;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.service.scripting.Converters;
import com.top_logic.tl.ai.core.message.TokenUsage;

import de.haumacher.msgbuf.data.DataObject;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Maps chat domain objects to msgbuf DTOs for the chat API.
 */
public final class DtoMapper {

	private DtoMapper() {
	}

	/**
	 * Builds the updates response from the tracker and current processing state.
	 *
	 * @param tracker
	 *        The task progress tracker for this session.
	 * @param processing
	 *        Whether an AI task is currently in progress.
	 * @param currentTaskId
	 *        The current task ID, or null if none.
	 */
	public static ChatUpdatesResponse toUpdatesResponse(TaskProgressTracker tracker, boolean processing,
			String currentTaskId) {
		ChatUpdatesResponse response = ChatUpdatesResponse.create();
		Chat chat = tracker.getChat();
		List<MessageDto> messages = new ArrayList<>();
		int index = 0;
		for (ChatEntry entry : chat.getEntries()) {
			if (entry instanceof ChatEntry.MessageEntry me) {
				messages.add(toMessageDto(me.message(), "m" + index));
				index++;
			}
		}
		response.setMessages(messages);
		response.setProcessing(processing);

		if (currentTaskId != null) {
			ChatTask task = tracker.getTask(currentTaskId);
			if (task != null) {
				response.setCurrentTask(toCurrentTaskDto(task));
			}
		}
		return response;
	}

	/**
	 * Converts a {@link ChatMessage} to a {@link MessageDto}.
	 *
	 * @param msg
	 *        The chat message to convert.
	 * @param id
	 *        The message ID.
	 * @return The message DTO.
	 */
	public static MessageDto toMessageDto(ChatMessage msg, String id) {
		MessageDto dto = MessageDto.create();
		dto.setId(id);
		String user = msg.getUser();
		dto.setUserName(user);
		dto.setContent(msg.getText());
		dto.setTime(msg.getTimestamp().toString());
		if ("System".equals(user)) {
			dto.setRole("system");
		} else if ("Agent".equals(user) || "AI".equals(user)) {
			dto.setRole("agent");
			dto.setContentHtml(Converters.markdownToHtmlString(msg.getText()));
		} else {
			dto.setRole("user");
		}
		List<ChatMessage.AttachmentMetadata> attachments = msg.getAttachments();
		if (attachments != null && !attachments.isEmpty()) {
			for (ChatMessage.AttachmentMetadata att : attachments) {
				dto.addAttachment(AttachmentDto.create()
					.setFileName(att.fileName())
					.setMimeType(att.mimeType())
					.setSize(att.size()));
			}
		}
		return dto;
	}

	/**
	 * Converts a {@link ChatTask} to a {@link CurrentTaskDto}.
	 *
	 * @param task
	 *        The chat task to convert.
	 * @return The current task DTO.
	 */
	public static CurrentTaskDto toCurrentTaskDto(ChatTask task) {
		CurrentTaskDto dto = CurrentTaskDto.create();
		dto.setTaskId(task.getId());
		dto.setStatus(task.getStatus().name().toLowerCase());
		dto.setDescription(descriptionFor(task));
		dto.setErrorMessage(task.getErrorMessage());
		List<TimelineEventDto> timeline = new ArrayList<>();
		int i = 0;
		for (ChatTimelineEvent ev : task.getTimelineEvents()) {
			timeline.add(toTimelineEventDto(ev, "e" + i));
			i++;
		}
		dto.setTimeline(timeline);
		return dto;
	}

	private static String descriptionFor(ChatTask task) {
		ChatTask.TaskStatus status = task.getStatus();
		if (status == ChatTask.TaskStatus.COMPLETED) {
			return "Completed";
		}
		if (status == ChatTask.TaskStatus.ERROR) {
			return "Failed";
		}
		return task.getDescription();
	}

	/**
	 * Converts a {@link ChatTimelineEvent} to a {@link TimelineEventDto}.
	 *
	 * @param ev
	 *        The timeline event to convert.
	 * @param id
	 *        The event ID.
	 * @return The timeline event DTO.
	 */
	public static TimelineEventDto toTimelineEventDto(ChatTimelineEvent ev, String id) {
		TimelineEventDto dto = TimelineEventDto.create();
		dto.setId(id);
		if (ev.getKind() == ChatTimelineEvent.Kind.TOOL_CALL && ev.getToolCall() != null) {
			ChatToolCall tc = ev.getToolCall();
			dto.setType("tool_call");
			dto.setToolName(tc.getToolName());
			dto.setName(tc.getToolName());
			Map<String, Object> args = tc.getArgs();
			dto.setArgsJson(args != null ? mapToJsonString(args) : "{}");
			dto.setResult(tc.getResultJson());
		} else {
			dto.setType("agent_call");
			dto.setFromAgent(ev.getFromAgent());
			dto.setToAgent(ev.getToAgent());
			dto.setInputSummary(ev.getInputSummary());
			dto.setResultSummary(ev.getResultSummary());
		}
		return dto;
	}

	/**
	 * Converts a {@link UserInteractionRequest} to a {@link UserInteractionEvent}.
	 *
	 * @param req
	 *        The pending interaction request.
	 * @return The event DTO ready for SSE serialization.
	 */
	public static UserInteractionEvent toUserInteractionEvent(UserInteractionRequest req) {
		UserInteractionEvent event = UserInteractionEvent.create()
			.setRequestId(req.getId())
			.setQuestion(req.getQuestion())
			.setQuestionHtml(Converters.markdownToHtmlString(req.getQuestion()))
			.setHeader(req.getHeader())
			.setType(req.getType())
			.setMultiSelect(req.isMultiSelect());
		String options = req.getOptions();
		if (options != null && !options.isEmpty()) {
			parseInteractionOptions(options, event);
		}
		return event;
	}

	/**
	 * Parses a JSON array of options (e.g. {@code [{"label":"A","description":"B"}]}) and adds
	 * them to the event.
	 */
	private static void parseInteractionOptions(String optionsJson, UserInteractionEvent event) {
		try {
			JsonReader reader = new JsonReader(new StringR(optionsJson));
			reader.beginArray();
			while (reader.hasNext()) {
				InteractionOptionDto option = InteractionOptionDto.create();
				reader.beginObject();
				while (reader.hasNext()) {
					String name = reader.nextName();
					switch (name) {
						case "label":
							option.setLabel(reader.nextString());
							break;
						case "description":
							option.setDescription(reader.nextString());
							break;
						default:
							reader.skipValue();
							break;
					}
				}
				reader.endObject();
				event.addOption(option);
			}
			reader.endArray();
		} catch (Exception ex) {
			// Silently ignore malformed options JSON from LLM output.
		}
	}

	/**
	 * Serializes a msgbuf {@link DataObject} to a JSON string.
	 *
	 * @param dataObject
	 *        The data object to serialize.
	 * @return JSON string representation.
	 */
	public static String toJson(DataObject dataObject) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			dataObject.writeTo(json);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to serialize data object to JSON", ex);
		}
		return buffer.toString();
	}

	/**
	 * Converts a {@link Map} to a JSON object string for embedding in msgbuf DTOs.
	 */
	static String mapToJsonString(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return "{}";
		}
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				json.name(entry.getKey());
				writeValue(json, entry.getValue());
			}
			json.endObject();
		} catch (IOException ex) {
			return "{}";
		}
		return buffer.toString();
	}

	private static void writeValue(JsonWriter json, Object value) throws IOException {
		if (value == null) {
			json.nullValue();
		} else if (value instanceof String s) {
			json.value(s);
		} else if (value instanceof Number n) {
			json.value(n.doubleValue());
		} else if (value instanceof Boolean b) {
			json.value(b);
		} else {
			json.value(String.valueOf(value));
		}
	}

	/**
	 * Serializes a list of {@link ChatEntry} objects to a JSON string for persistence.
	 *
	 * @param entries
	 *        The chat entries (messages and task snapshots).
	 * @return JSON string suitable for storing in the <code>ChatSession.messages</code> CLOB.
	 */
	public static String serializeEntries(List<ChatEntry> entries) {
		PersistedChat chat = PersistedChat.create();
		int messageIndex = 0;
		for (ChatEntry entry : entries) {
			PersistedChatEntry dto = PersistedChatEntry.create();
			if (entry instanceof ChatEntry.MessageEntry me) {
				dto.setType("message");
				dto.setMessageData(toMessageDto(me.message(), "m" + messageIndex));
				messageIndex++;
			} else if (entry instanceof ChatEntry.TaskSnapshotEntry tse) {
				dto.setType("task_snapshot");
				dto.setTaskData(toCurrentTaskDto(tse.task()));
			} else {
				continue;
			}
			chat.addEntry(dto);
		}
		return toJson(chat);
	}

	/**
	 * Deserializes a JSON string (from <code>ChatSession.messages</code> CLOB) back into {@link ChatEntry}
	 * objects.
	 *
	 * @param json
	 *        The JSON string. May be null or empty.
	 * @return List of chat entries, never null.
	 */
	public static List<ChatEntry> deserializeEntries(String json) {
		if (json == null || json.isEmpty()) {
			return new ArrayList<>();
		}

		List<ChatEntry> result = new ArrayList<>();
		try {
			PersistedChat chat = PersistedChat.readPersistedChat(new JsonReader(new StringR(json)));
			for (PersistedChatEntry entry : chat.getEntries()) {
				if ("message".equals(entry.getType()) && entry.getMessageData() != null) {
					MessageDto msg = entry.getMessageData();
					Instant timestamp = parseTimestamp(msg.getTime());
					List<ChatMessage.AttachmentMetadata> attachments = new ArrayList<>();
					for (AttachmentDto att : msg.getAttachments()) {
						attachments.add(new ChatMessage.AttachmentMetadata(
							att.getFileName(), att.getMimeType(), att.getSize()));
					}
					ChatMessage chatMsg = new ChatMessage(
						msg.getUserName() != null ? msg.getUserName() : msg.getRole(),
						msg.getContent(),
						timestamp,
						attachments);
					result.add(new ChatEntry.MessageEntry(chatMsg));
				} else if ("task_snapshot".equals(entry.getType()) && entry.getTaskData() != null) {
					ChatTask task = fromCurrentTaskDto(entry.getTaskData());
					result.add(new ChatEntry.TaskSnapshotEntry(task));
				}
			}
		} catch (Exception ex) {
			// Fall back to legacy format (plain message array)
			result.addAll(deserializeLegacyMessages(json));
		}
		return result;
	}

	/**
	 * Converts a {@link CurrentTaskDto} back to a {@link ChatTask} (for loading persisted
	 * snapshots).
	 */
	static ChatTask fromCurrentTaskDto(CurrentTaskDto dto) {
		ChatTask task = new ChatTask(dto.getDescription() != null ? dto.getDescription() : "");
		ChatTask.Builder builder = task.toBuilder();
		builder.withStatus(parseTaskStatus(dto.getStatus()));
		if (dto.getErrorMessage() != null) {
			builder.withError(dto.getErrorMessage());
		}
		for (TimelineEventDto evDto : dto.getTimeline()) {
			ChatTimelineEvent event = fromTimelineEventDto(evDto);
			if (event != null) {
				builder.addTimelineEvent(event);
			}
		}
		return builder.build();
	}

	private static ChatTask.TaskStatus parseTaskStatus(String status) {
		if (status == null) {
			return ChatTask.TaskStatus.COMPLETED;
		}
		switch (status) {
			case "in_progress":
				return ChatTask.TaskStatus.IN_PROGRESS;
			case "error":
				return ChatTask.TaskStatus.ERROR;
			case "pending":
				return ChatTask.TaskStatus.PENDING;
			default:
				return ChatTask.TaskStatus.COMPLETED;
		}
	}

	private static ChatTimelineEvent fromTimelineEventDto(TimelineEventDto dto) {
		if ("tool_call".equals(dto.getType())) {
			Map<String, Object> args = new LinkedHashMap<>();
			if (dto.getArgsJson() != null && !dto.getArgsJson().isEmpty()) {
				try {
					args = parseJsonToMap(dto.getArgsJson());
				} catch (Exception ignored) {
					// Use empty args on parse failure.
				}
			}
			ChatToolCall toolCall = new ChatToolCall(
				dto.getToolName() != null ? dto.getToolName() : dto.getName(),
				args,
				dto.getResult());
			return ChatTimelineEvent.toolCall(toolCall);
		} else if ("agent_call".equals(dto.getType())) {
			return ChatTimelineEvent.agentCall(
				dto.getFromAgent(), dto.getToAgent(),
				dto.getInputSummary(), dto.getResultSummary());
		}
		return null;
	}

	private static Map<String, Object> parseJsonToMap(String jsonStr) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			JsonReader reader = new JsonReader(new StringR(jsonStr));
			reader.beginObject();
			while (reader.hasNext()) {
				String key = reader.nextName();
				Object value = readJsonValue(reader);
				result.put(key, value);
			}
			reader.endObject();
		} catch (Exception ignored) {
			// Return partial result on parse failure.
		}
		return result;
	}

	private static Object readJsonValue(JsonReader reader) throws IOException {
		switch (reader.peek()) {
			case STRING:
				return reader.nextString();
			case NUMBER:
				return reader.nextDouble();
			case BOOLEAN:
				return reader.nextBoolean();
			case NULL:
				reader.nextNull();
				return null;
			default:
				reader.skipValue();
				return null;
		}
	}

	/**
	 * Parses the legacy message-only JSON format: [{role, text, timestamp}, ...].
	 */
	private static List<ChatEntry> deserializeLegacyMessages(String json) {
		List<ChatEntry> result = new ArrayList<>();
		try {
			JsonReader reader = new JsonReader(new StringR(json));
			reader.beginArray();
			while (reader.hasNext()) {
				reader.beginObject();
				String role = null, text = null;
				while (reader.hasNext()) {
					String name = reader.nextName();
					switch (name) {
						case "role":
							role = reader.nextString();
							break;
						case "text":
							text = reader.nextString();
							break;
						default:
							reader.skipValue();
							break;
					}
				}
				reader.endObject();
				if (role != null && text != null) {
					result.add(new ChatEntry.MessageEntry(new ChatMessage(role, text)));
				}
			}
			reader.endArray();
		} catch (Exception ignored) {
			// Return partial result on parse failure.
		}
		return result;
	}

	/**
	 * Parses a timestamp string (ISO-8601) to an {@link Instant}. Falls back to
	 * {@link Instant#now()} if parsing fails or the value is null.
	 */
	private static Instant parseTimestamp(String time) {
		if (time == null || time.isEmpty()) {
			return Instant.now();
		}
		try {
			return Instant.parse(time);
		} catch (Exception ex) {
			return Instant.now();
		}
	}

	/**
	 * Serializes session metadata (approved tools, cumulative token usage) to JSON.
	 *
	 * @param tracker
	 *        The task progress tracker holding session state.
	 * @return JSON string for persistence.
	 */
	public static String serializeMetadata(TaskProgressTracker tracker) {
		SessionMetadata meta = SessionMetadata.create();
		for (String tool : tracker.getSessionApprovedTools()) {
			meta.addApprovedTool(tool);
		}
		TokenUsage usage = tracker.getCumulativeUsage();
		meta.setCumulativeUsage(TokenUsageDto.create()
			.setInputTokens(usage.inputTokens())
			.setOutputTokens(usage.outputTokens())
			.setThinkingTokens(usage.thinkingTokens())
			.setCacheReadTokens(usage.cacheReadTokens())
			.setCacheWriteTokens(usage.cacheWriteTokens()));
		return toJson(meta);
	}

	/**
	 * Loads session metadata from JSON into the tracker, restoring approved tools and cumulative
	 * token usage.
	 *
	 * @param json
	 *        The JSON string from persistence. May be null or empty.
	 * @param tracker
	 *        The task progress tracker to populate.
	 */
	public static void loadMetadata(String json, TaskProgressTracker tracker) {
		if (json == null || json.isEmpty()) {
			return;
		}
		try {
			SessionMetadata meta =
				SessionMetadata.readSessionMetadata(new JsonReader(new StringR(json)));
			tracker.loadSessionApprovedTools(new HashSet<>(meta.getApprovedTools()));
			TokenUsageDto usage = meta.getCumulativeUsage();
			if (usage != null) {
				tracker.addTokenUsage(new TokenUsage(
					usage.getInputTokens(), usage.getOutputTokens(),
					usage.getThinkingTokens(), usage.getCacheReadTokens(),
					usage.getCacheWriteTokens()));
			}
		} catch (Exception e) {
			// Corrupted metadata — ignore and start fresh.
		}
	}
}
