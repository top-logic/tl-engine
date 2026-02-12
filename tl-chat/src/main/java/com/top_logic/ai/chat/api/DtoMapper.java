/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatEntry;
import com.top_logic.ai.chat.layout.ChatMessage;
import com.top_logic.ai.chat.layout.ChatTask;
import com.top_logic.ai.chat.layout.ChatTimelineEvent;
import com.top_logic.ai.chat.layout.ChatToolCall;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.service.scripting.Converters;

/**
 * Maps chat domain objects to JSON DTOs for the chat API.
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
		ChatUpdatesResponse response = new ChatUpdatesResponse();
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

		CurrentTaskDto currentTaskDto = null;
		if (currentTaskId != null) {
			ChatTask task = tracker.getTask(currentTaskId);
			if (task != null) {
				currentTaskDto = toCurrentTaskDto(task);
			}
		}
		response.setCurrentTask(currentTaskDto);
		return response;
	}

	public static MessageDto toMessageDto(ChatMessage msg, String id) {
		MessageDto dto = new MessageDto();
		dto.setId(id);
		String user = msg.getUser();
		dto.setUserName(user);
		dto.setContent(msg.getText());
		dto.setTime(TIME_FORMAT.format(msg.getTimestamp()));
		if ("System".equals(user)) {
			dto.setRole("system");
		} else if ("Agent".equals(user) || "AI".equals(user)) {
			dto.setRole("agent");
			// Pre-render markdown to HTML so the client can insert directly
			dto.setContentHtml(Converters.markdownToHtmlString(msg.getText()));
		} else {
			dto.setRole("user");
		}
		return dto;
	}

	public static CurrentTaskDto toCurrentTaskDto(ChatTask task) {
		CurrentTaskDto dto = new CurrentTaskDto();
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

	public static TimelineEventDto toTimelineEventDto(ChatTimelineEvent ev, String id) {
		TimelineEventDto dto = new TimelineEventDto();
		dto.setId(id);
		if (ev.getKind() == ChatTimelineEvent.Kind.TOOL_CALL && ev.getToolCall() != null) {
			ChatToolCall tc = ev.getToolCall();
			dto.setType("tool_call");
			dto.setToolName(tc.getToolName());
			dto.setName(tc.getToolName());
			dto.setArgs(tc.getArgs() != null ? Map.copyOf(tc.getArgs()) : Collections.emptyMap());
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
}
