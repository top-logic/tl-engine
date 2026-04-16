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

import com.top_logic.ai.service.approval.ApprovalMode;
import com.top_logic.ai.service.approval.UserInteractionRequest;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.ChatUpdatesResponse;
import com.top_logic.ai.chat.api.messages.InteractionOptionDto;
import com.top_logic.ai.chat.api.messages.MessageDto;
import com.top_logic.ai.chat.api.messages.PersistedChat;
import com.top_logic.ai.chat.api.messages.PersistedChatEntry;
import com.top_logic.ai.chat.api.messages.SessionMetadata;
import com.top_logic.ai.chat.api.messages.TokenUsageDto;
import com.top_logic.ai.chat.api.messages.UserInteractionEvent;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatMessage;
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
	 * Serializes a list of {@link ChatMessage} objects to a JSON string for persistence.
	 * This is the V1 format used as a fallback; new sessions use {@link ConversationLogSerializer}.
	 *
	 * @param messages
	 *        The chat messages.
	 * @return JSON string suitable for storing in the {@code ChatSession.messages} CLOB.
	 */
	public static String serializeMessages(List<ChatMessage> messages) {
		PersistedChat chat = PersistedChat.create();
		int messageIndex = 0;
		for (ChatMessage msg : messages) {
			PersistedChatEntry dto = PersistedChatEntry.create();
			dto.setType("message");
			dto.setMessageData(toMessageDto(msg, "m" + messageIndex));
			messageIndex++;
			chat.addEntry(dto);
		}
		return toJson(chat);
	}

	/**
	 * Deserializes a JSON string (V1 format) back into {@link ChatMessage} objects.
	 *
	 * @param json
	 *        The JSON string. May be null or empty.
	 * @return List of chat messages, never null.
	 */
	public static List<ChatMessage> deserializeMessages(String json) {
		if (json == null || json.isEmpty()) {
			return new ArrayList<>();
		}

		List<ChatMessage> result = new ArrayList<>();
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
					result.add(chatMsg);
				}
				// task_snapshot entries from V1 format are silently skipped
			}
		} catch (Exception ex) {
			// Fall back to legacy format (plain message array)
			result.addAll(deserializeLegacyMessages(json));
		}
		return result;
	}

	/**
	 * Parses the legacy message-only JSON format: [{role, text, timestamp}, ...].
	 */
	private static List<ChatMessage> deserializeLegacyMessages(String json) {
		List<ChatMessage> result = new ArrayList<>();
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
					result.add(new ChatMessage(role, text));
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
		meta.setApprovalMode(tracker.getApprovalMode().name());
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
		} catch (Exception e) {
			// Corrupted metadata — ignore and start fresh.
		}
	}
}
