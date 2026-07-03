/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.ChatUpdatesResponse;
import com.top_logic.ai.chat.api.messages.InteractionOptionDto;
import com.top_logic.ai.chat.api.messages.MessageDto;
import com.top_logic.ai.chat.api.messages.SessionMetadata;
import com.top_logic.ai.chat.api.messages.TokenUsageDto;
import com.top_logic.ai.chat.api.messages.UserInteractionEvent;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatMessage;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.core.tools.task.TaskStore;

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
		for (ChatMessage msg : chat.getMessages()) {
			messages.add(toMessageDto(msg, "m" + index));
			index++;
		}
		response.setMessages(messages);
		response.setProcessing(processing);
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
			try (JsonReader reader = new JsonReader(new StringR(optionsJson))) {
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
			}
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
	 * Parses the legacy message-only JSON format: [{role, text, timestamp}, ...].
	 */
	private static List<ChatMessage> deserializeLegacyMessages(String json) {
		List<ChatMessage> result = new ArrayList<>();
		try {
			try (JsonReader reader = new JsonReader(new StringR(json))) {
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
					result.add(new ChatMessage(role, text));
				}
			}
			reader.endArray();
			}
		} catch (Exception ignored) {
			// Return partial result on parse failure.
		}
		return result;
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
		meta.setApprovalMode(tracker.getApprovalMode().name());
		TaskStore taskStore = tracker.getActiveTaskStore();
		if (taskStore != null) {
			meta.setTasksJson(taskStore.toJson());
		}
		for (String skill : tracker.getLoadedSkills()) {
			meta.addLoadedSkill(skill);
		}
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
			String mode = meta.getApprovalMode();
			if (mode != null && !mode.isEmpty()) {
				tracker.setApprovalMode(ApprovalMode.valueOf(mode));
			}
			List<String> loadedSkills = meta.getLoadedSkills();
			if (loadedSkills != null && !loadedSkills.isEmpty()) {
				tracker.loadLoadedSkills(new HashSet<>(loadedSkills));
			}
		} catch (Exception e) {
			// Corrupted metadata — ignore and start fresh.
		}
	}

	/**
	 * Loads a {@link TaskStore} from the metadata JSON string.
	 *
	 * @param json
	 *        The metadata JSON; may be null or empty.
	 * @return A {@code TaskStore} populated from the metadata, or an empty store.
	 */
	public static TaskStore loadTaskStore(String json) {
		if (json == null || json.isEmpty()) {
			return new TaskStore();
		}
		try {
			SessionMetadata meta =
				SessionMetadata.readSessionMetadata(new JsonReader(new StringR(json)));
			String tasksJson = meta.getTasksJson();
			return TaskStore.fromJson(tasksJson);
		} catch (Exception e) {
			return new TaskStore();
		}
	}
}
