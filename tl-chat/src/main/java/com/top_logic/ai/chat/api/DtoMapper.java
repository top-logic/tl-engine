/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.chat.api.messages.ChatUpdatesResponse;
import com.top_logic.ai.chat.api.messages.CurrentTaskDto;
import com.top_logic.ai.chat.api.messages.MessageDto;
import com.top_logic.ai.chat.api.messages.TimelineEventDto;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatEntry;
import com.top_logic.ai.chat.layout.ChatMessage;
import com.top_logic.ai.chat.layout.ChatTask;
import com.top_logic.ai.chat.layout.ChatTimelineEvent;
import com.top_logic.ai.chat.layout.ChatToolCall;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.service.scripting.Converters;

import de.haumacher.msgbuf.data.DataObject;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Maps chat domain objects to msgbuf DTOs for the chat API.
 */
public final class DtoMapper {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
		.withZone(ZoneId.systemDefault());

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
		dto.setTime(TIME_FORMAT.format(msg.getTimestamp()));
		if ("System".equals(user)) {
			dto.setRole("system");
		} else if ("Agent".equals(user) || "AI".equals(user)) {
			dto.setRole("agent");
			dto.setContentHtml(Converters.markdownToHtmlString(msg.getText()));
		} else {
			dto.setRole("user");
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
	private static String mapToJsonString(Map<String, Object> map) {
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
}
