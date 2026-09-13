/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.ChatUpdatesResponse;
import com.top_logic.ai.chat.api.messages.InteractionOptionDto;
import com.top_logic.ai.chat.api.messages.MessageDto;
import com.top_logic.ai.chat.api.messages.MessageFeedbackDto;
import com.top_logic.ai.chat.api.messages.SessionMetadata;
import com.top_logic.ai.chat.api.messages.TokenUsageDto;
import com.top_logic.ai.chat.api.messages.UserInteractionEvent;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatMessage;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.approval.InteractionTimeoutService;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.core.tools.task.TaskStore;

import de.haumacher.msgbuf.data.DataObject;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonToken;
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
	 * <p>
	 * The remaining time is computed from the request's creation time rather than passed on as the
	 * full timeout: a question is re-emitted when the chat is reopened, and the clock the user then
	 * sees has to be the one the waiting agent thread is actually running.
	 * </p>
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
			.setMultiSelect(req.isMultiSelect())
			.setRemainingMs(remainingMs(req.getCreatedAt(),
				InteractionTimeoutService.questionTimeout()));
		String options = req.getOptions();
		if (options != null && !options.isEmpty()) {
			parseInteractionOptions(options, event);
		}
		return event;
	}

	/**
	 * Milliseconds left of a deadline that started at {@code startedAt}, never negative.
	 *
	 * @param startedAt
	 *        When the request was raised, in milliseconds since the epoch.
	 * @param timeout
	 *        How long the request waits in total.
	 */
	public static long remainingMs(long startedAt, Duration timeout) {
		long elapsed = System.currentTimeMillis() - startedAt;
		return Math.max(0L, timeout.toMillis() - elapsed);
	}

	/**
	 * Parses a JSON array of choice options and adds them to the event.
	 *
	 * <p>
	 * Both element shapes the model may emit are accepted: bare strings
	 * ({@code ["Approve", "Revise"]}, the form the agent prompts instruct) and objects
	 * ({@code [{"label":"Approve","description":"..."}]}, the form documented on the tool). A bare
	 * string becomes the option label. Mixing the two shapes in one array is tolerated.
	 * </p>
	 */
	private static void parseInteractionOptions(String optionsJson, UserInteractionEvent event) {
		try {
			try (JsonReader reader = new JsonReader(new StringR(optionsJson))) {
			reader.beginArray();
			while (reader.hasNext()) {
				InteractionOptionDto option = InteractionOptionDto.create();
				if (reader.peek() == JsonToken.STRING) {
					option.setLabel(reader.nextString());
				} else {
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
				}
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
		for (MessageFeedbackDto entry : tracker.getFeedback()) {
			meta.addFeedback(entry);
		}
		String effort = tracker.getEffort();
		if (effort != null) {
			meta.setLastEffort(effort);
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
			tracker.loadFeedback(meta.getFeedback());
			tracker.setEffort(meta.getLastEffort());
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
