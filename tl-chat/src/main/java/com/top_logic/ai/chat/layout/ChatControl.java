/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.scripting.Converters;
import com.top_logic.ai.service.tools.util.JsonPrettyPrint;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Chat control that renders messages, task traces, and an input section.
 *
 * <p>
 * All background processing logic (AI execution, polling, state management) lives in
 * {@link ChatProcessor}. This class is responsible only for rendering and the thin
 * {@link SendMessage} command fallback.
 * </p>
 */
public class ChatControl extends AbstractControlBase {

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(SendMessage.INSTANCE);

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final Chat _chat;

	/** Shared state for background processing (null when not processing) */
	private ChatProcessor.AIProcessingState _processingState = null;

	/** Session-scoped task progress tracker */
	private final TaskProgressTracker _taskTracker;

	/**
	 * Creates a new {@link ChatControl}.
	 */
	public ChatControl(Chat chat, TaskProgressTracker taskTracker) {
		super(COMMANDS);
		_chat = chat;
		_taskTracker = taskTracker;
	}

	@Override
	public Object getModel() {
		return _chat;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// No incremental updates
	}

	@Override
	protected void internalDetach() {
		// Do NOT cancel processing on detach: state lives in tracker so navigation away and back
		// still shows in-progress task and agent answer when it arrives.
		_processingState = null;
		super.internalDetach();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.writeAttribute("data-tl-chat", "v1");
		out.writeAttribute("data-chat-messages-id", getID() + "-messages");
		out.writeAttribute("data-chat-input-id", getID() + "-input");
		out.writeAttribute("data-chat-button-id", getID() + "-button");
		out.writeAttribute("data-chat-file-input-id", getID() + "-file-input");
		out.writeAttribute("data-chat-attach-btn-id", getID() + "-attach-btn");
		out.writeAttribute("data-chat-preview-id", getID() + "-attachment-preview");
		out.writeAttribute("data-chat-user-name",
			_taskTracker.getUserName() != null ? _taskTracker.getUserName() : "User");
		out.endBeginTag();
		{
			// Main chat area (welcome + messages + input)
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-main");
			out.endBeginTag();
			{
				writeWelcomeScreen(out);
				writeMessages(context, out);
				writeInputSection(out);
			}
			out.endTag(DIV);

			// History panel (right side, collapsible)
			writeHistoryPanel(out);
		}
		out.endTag(DIV);

		// Load chat client via inline script so it runs when fragment is AJAX-injected
		String contextPath = context.getContextPath();
		out.beginScript();
		out.append("(function(){var s=document.createElement('script');s.src='");
		out.append(contextPath);
		out.append("/script/chat-client.js';s.type='text/javascript';(document.head||document.documentElement).appendChild(s);})();");
		out.endScript();
	}

	private void writeHistoryPanel(TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-history-panel");
		out.endBeginTag();
		{
			// Sidebar header with toggle and new chat
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-sidebar-header");
			out.endBeginTag();
			{
				// Toggle button (hamburger → X animation via CSS)
				out.beginBeginTag(BUTTON);
				out.writeAttribute(TYPE_ATTR, "button");
				out.writeAttribute(CLASS_ATTR, "chat-sidebar-toggle");
				out.endBeginTag();
				{
					writeToggleBar(out);
					writeToggleBar(out);
					writeToggleBar(out);
				}
				out.endTag(BUTTON);

				// New Chat button with icon + label
				out.beginBeginTag(BUTTON);
				out.writeAttribute(TYPE_ATTR, "button");
				out.writeAttribute(CLASS_ATTR, "chat-new-session-btn");
				out.endBeginTag();
				{
					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, "chat-new-session-icon");
					out.endBeginTag();
					out.endTag(SPAN);
					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, "chat-new-session-label");
					out.endBeginTag();
					out.writeText("New Chat");
					out.endTag(SPAN);
				}
				out.endTag(BUTTON);
			}
			out.endTag(DIV);

			// Session list container (populated by JS)
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-history-list");
			out.writeAttribute("id", getID() + "-history-list");
			out.endBeginTag();
			out.endTag(DIV);
		}
		out.endTag(DIV);
	}

	private void writeToggleBar(TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "chat-toggle-bar");
		out.endBeginTag();
		out.endTag(SPAN);
	}

	private void writeWelcomeScreen(TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-welcome");
		out.endBeginTag();
		{
			// Heading
			out.beginBeginTag("h1");
			out.writeAttribute(CLASS_ATTR, "chat-welcome-heading");
			out.endBeginTag();
			String welcomeName = _taskTracker.getWelcomeName();
			out.writeText(welcomeName != null ? "Welcome, " + welcomeName : "Welcome");
			out.endTag("h1");

			// Subtitle
			out.beginBeginTag("p");
			out.writeAttribute(CLASS_ATTR, "chat-welcome-subtitle");
			out.endBeginTag();
			out.writeText("How can I help you today?");
			out.endTag("p");

			// Example prompts
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-welcome-prompts");
			out.endBeginTag();
			{
				writePromptChip(out, "Show me the data model");
				writePromptChip(out, "Find all Person objects");
				writePromptChip(out, "Explain the current schema");
			}
			out.endTag(DIV);

			// Large textarea input
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-welcome-input-section");
			out.endBeginTag();
			{
				// Attachment preview strip (populated by JS)
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, "chat-attachment-preview chat-welcome-attachment-preview");
				out.endBeginTag();
				out.endTag(DIV);

				out.beginBeginTag("textarea");
				out.writeAttribute(CLASS_ATTR, "chat-welcome-input");
				out.writeAttribute("rows", "4");
				out.writeAttribute(PLACEHOLDER_ATTR, "Ask anything...");
				out.endBeginTag();
				out.endTag("textarea");

				// Hidden file input
				out.beginBeginTag(INPUT);
				out.writeAttribute(TYPE_ATTR, "file");
				out.writeAttribute(CLASS_ATTR, "chat-welcome-file-input");
				out.writeAttribute("accept", "image/*,.pdf");
				out.writeAttribute("multiple", "multiple");
				out.writeAttribute("style", "display:none");
				out.endEmptyTag();

				// Button row: attach + send
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, "chat-welcome-button-row");
				out.endBeginTag();
				{
					out.beginBeginTag(BUTTON);
					out.writeAttribute(TYPE_ATTR, "button");
					out.writeAttribute(CLASS_ATTR, "chat-attach-button chat-welcome-attach-btn");
					out.writeAttribute("title", "Attach files");
					out.endBeginTag();
					writePaperclipSvg(out);
					out.endTag(BUTTON);

					out.beginBeginTag(BUTTON);
					out.writeAttribute(TYPE_ATTR, "button");
					out.writeAttribute(CLASS_ATTR, "chat-welcome-send-btn");
					out.endBeginTag();
					out.writeText("\u2191");
					out.endTag(BUTTON);
				}
				out.endTag(DIV);
			}
			out.endTag(DIV);
		}
		out.endTag(DIV);
	}

	private void writePromptChip(TagWriter out, String text) throws IOException {
		out.beginBeginTag(BUTTON);
		out.writeAttribute(TYPE_ATTR, "button");
		out.writeAttribute(CLASS_ATTR, "chat-prompt-chip");
		out.writeAttribute("data-prompt", text);
		out.endBeginTag();
		out.writeText(text);
		out.endTag(BUTTON);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		com.top_logic.mig.html.HTMLUtil.appendCSSClass(out, "chat-control");
	}

	private void writeMessages(DisplayContext context, TagWriter out) throws IOException {
		String messagesId = getID() + "-messages";
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-messages");
		out.writeAttribute(ID_ATTR, messagesId);
		out.endBeginTag();
		{
			// Render entries in order: user message -> task trace -> agent response
			int messageIndex = 0;
			for (ChatEntry entry : _chat.getEntries()) {
				if (entry instanceof ChatEntry.MessageEntry me) {
					writeMessage(context, out, me.message(), messageIndex);
					messageIndex++;
				} else if (entry instanceof ChatEntry.TaskEntry te) {
					ChatTask task = _taskTracker.getTask(te.taskId());
					if (task != null) {
						writeTask(context, out, task);
					}
				} else if (entry instanceof ChatEntry.TaskSnapshotEntry tse) {
					writeTask(context, out, tse.task());
				}
			}
			// Show in-progress task (not yet in entries) so trace appears after user message.
			ChatProcessor.AIProcessingState state = _processingState != null ? _processingState
				: _taskTracker.getAnyActiveProcessingState();
			if (state != null) {
				String currentTaskId = state.getCurrentTaskId();
				if (currentTaskId != null) {
					ChatTask task = _taskTracker.getTask(currentTaskId);
					if (task != null && task.getStatus() == ChatTask.TaskStatus.IN_PROGRESS) {
						writeTask(context, out, task);
					}
				}
			}
		}
		out.endTag(DIV);

		// Auto-scroll to bottom after rendering
		out.beginScript();
		out.append("(function() {");
		out.append("var container = document.getElementById('");
		out.append(messagesId);
		out.append("');");
		out.append("if (container) { container.scrollTop = container.scrollHeight; }");
		out.append("})();");
		out.endScript();
	}

	private void writeMessage(DisplayContext context, TagWriter out, ChatMessage msg, int messageIndex)
			throws IOException {
		String role = getRoleForUser(msg.getUser());

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-message chat-message-" + role);
		out.writeAttribute("data-message-id", "m" + messageIndex);
		out.endBeginTag();
		{
			// User name
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-user");
			out.endBeginTag();
			out.writeText(msg.getUser());
			out.endTag(SPAN);

			// Bubble wrapper around message text
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-bubble");
			out.endBeginTag();
			{
				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, "chat-text");
				out.endBeginTag();
				Converters.markdownToHtml(msg.getText()).write(context, out);
				out.endTag(SPAN);
			}
			out.endTag(DIV);

			// Timestamp
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-time");
			out.endBeginTag();
			out.writeText(TIME_FORMAT.format(msg.getTimestamp().atZone(ZoneId.systemDefault())));
			out.endTag(SPAN);
		}
		out.endTag(DIV);
	}

	private static String getRoleForUser(String user) {
		if ("Agent".equals(user) || "AI".equals(user)) {
			return "agent";
		}
		if ("System".equals(user)) {
			return "system";
		}
		return "user";
	}

	private void writeTask(DisplayContext context, TagWriter out, ChatTask task) throws IOException {
		String taskDivId = "task-" + task.getId();

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-task");
		out.writeAttribute("data-task-id", task.getId());
		out.writeAttribute("data-status", task.getStatus().name().toLowerCase());
		out.endBeginTag();
		{
			writeTaskHeader(out, task, taskDivId);
			writeTaskBody(context, out, task, taskDivId);
		}
		out.endTag(DIV);
	}

	private void writeTaskHeader(TagWriter out, ChatTask task, String taskDivId) throws IOException {
		boolean isFinished =
			task.getStatus() == ChatTask.TaskStatus.COMPLETED || task.getStatus() == ChatTask.TaskStatus.ERROR;

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-task-header");
		if (isFinished) {
			String bodyId = taskDivId + "-body";
			String arrowId = taskDivId + "-arrow";
			out.writeAttribute(ONCLICK_ATTR,
				"toggleTaskExpand('" + bodyId + "', '" + arrowId + "')");
		}
		out.endBeginTag();
		{
			// Status icon
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-task-icon");
			out.endBeginTag();
			out.writeText(getTaskIcon(task.getStatus()));
			out.endTag(SPAN);

			// Description
			String headerText = getTaskHeaderDescription(task);
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-task-description");
			out.endBeginTag();
			out.writeText(headerText);
			out.endTag(SPAN);

			// Duration
			if (task.getEndTime() != null) {
				long durationMs = java.time.Duration.between(task.getStartTime(), task.getEndTime()).toMillis();
				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, "chat-task-time");
				out.endBeginTag();
				out.writeText(formatDuration(durationMs));
				out.endTag(SPAN);
			}

			// Expand arrow (visible for completed/error tasks via CSS)
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-task-expand");
			out.writeAttribute(ID_ATTR, taskDivId + "-arrow");
			out.endBeginTag();
			out.writeText(isFinished ? "\u25B6" : ""); // ▶ collapsed indicator
			out.endTag(SPAN);
		}
		out.endTag(DIV);
	}

	private void writeTaskBody(DisplayContext context, TagWriter out, ChatTask task, String taskDivId)
			throws IOException {
		boolean isFinished =
			task.getStatus() == ChatTask.TaskStatus.COMPLETED || task.getStatus() == ChatTask.TaskStatus.ERROR;

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-task-body");
		out.writeAttribute(ID_ATTR, taskDivId + "-body");
		if (isFinished) {
			out.writeAttribute("style", "display: none");
		}
		out.endBeginTag();
		{
			// Timeline: group tool calls under their parent agent calls.
			// Tool calls that follow an agent_call belong to that agent;
			// tool calls before any agent_call are top-level (orchestrator's own).
			List<ChatTimelineEvent> events = task.getTimelineEvents();
			int i = 0;
			while (i < events.size()) {
				ChatTimelineEvent ev = events.get(i);
				if (ev.getKind() == ChatTimelineEvent.Kind.AGENT_CALL) {
					// Collect subsequent tool calls that belong to this agent
					List<ChatTimelineEvent> childTools = new java.util.ArrayList<>();
					List<String> childToolIds = new java.util.ArrayList<>();
					int j = i + 1;
					while (j < events.size()
						&& events.get(j).getKind() == ChatTimelineEvent.Kind.TOOL_CALL) {
						childTools.add(events.get(j));
						childToolIds.add("e" + j);
						j++;
					}
					writeAgentCall(out, ev, taskDivId + "-agent-" + i, "e" + i, childTools, childToolIds);
					i = j;
				} else if (ev.getKind() == ChatTimelineEvent.Kind.TOOL_CALL && ev.getToolCall() != null) {
					writeToolCall(out, ev.getToolCall(), "e" + i);
					i++;
				} else {
					i++;
				}
			}

			// Error message if failed
			if (task.getStatus() == ChatTask.TaskStatus.ERROR && task.getErrorMessage() != null) {
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, "chat-task-error");
				out.endBeginTag();
				String errText = task.getErrorMessage();
				out.writeText("Error: " + (errText != null && !errText.isEmpty() ? errText : "An unexpected error occurred"));
				out.endTag(DIV);
			}
		}
		out.endTag(DIV);
	}

	private void writeAgentCall(TagWriter out, ChatTimelineEvent ev, String agentCallId, String eventId,
			List<ChatTimelineEvent> childTools, List<String> childToolIds) throws IOException {
		String bodyId = agentCallId + "-body";
		String arrowId = agentCallId + "-arrow";

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-agent-call");
		out.writeAttribute("data-event-id", eventId);
		out.endBeginTag();
		{
			// Clickable header: label + expand arrow
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-agent-call-header");
			out.writeAttribute(ONCLICK_ATTR, "toggleToolExpand('" + bodyId + "', '" + arrowId + "')");
			out.endBeginTag();
			{
				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, "chat-agent-call-label");
				out.endBeginTag();
				out.writeText(ev.getFromAgent() + " \u2192 " + ev.getToAgent());
				out.endTag(SPAN);
				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, "chat-agent-call-expand");
				out.writeAttribute(ID_ATTR, arrowId);
				out.endBeginTag();
				out.writeText("\u25BE"); // ▾ expanded by default
				out.endTag(SPAN);
			}
			out.endTag(DIV);

			// Collapsible body (expanded by default so nested tools are visible)
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-agent-call-body");
			out.writeAttribute(ID_ATTR, bodyId);
			out.endBeginTag();
			{
				if (ev.getInputSummary() != null && !ev.getInputSummary().isEmpty()) {
					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, "chat-agent-call-input-label");
					out.endBeginTag();
					out.writeText("Input:");
					out.endTag(DIV);
					out.beginBeginTag(PRE);
					out.writeAttribute(CLASS_ATTR, "chat-agent-call-input");
					out.endBeginTag();
					out.writeText(ev.getInputSummary());
					out.endTag(PRE);
				}

				// Nested tool calls from this agent
				if (!childTools.isEmpty()) {
					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, "chat-agent-call-tools");
					out.endBeginTag();
					for (int t = 0; t < childTools.size(); t++) {
						ChatTimelineEvent toolEv = childTools.get(t);
						if (toolEv.getToolCall() != null) {
							writeToolCall(out, toolEv.getToolCall(), childToolIds.get(t));
						}
					}
					out.endTag(DIV);
				}

				if (ev.getResultSummary() != null && !ev.getResultSummary().isEmpty()) {
					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, "chat-agent-call-result-label");
					out.endBeginTag();
					out.writeText("Result:");
					out.endTag(DIV);
					out.beginBeginTag(PRE);
					out.writeAttribute(CLASS_ATTR, "chat-agent-call-result");
					out.endBeginTag();
					out.writeText(ev.getResultSummary());
					out.endTag(PRE);
				}
			}
			out.endTag(DIV);
		}
		out.endTag(DIV);
	}

	private void writeToolCall(TagWriter out, ChatToolCall toolCall, String eventId) throws IOException {
		String toolDivId = "tool-" + toolCall.getId();

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-tool-call");
		out.writeAttribute("data-event-id", eventId);
		out.writeAttribute("data-status", toolCall.getStatus().name().toLowerCase());
		out.endBeginTag();
		{
			writeToolHeader(out, toolCall, toolDivId);
			writeToolResult(out, toolCall, toolDivId);
		}
		out.endTag(DIV);
	}

	private void writeToolHeader(TagWriter out, ChatToolCall toolCall, String toolDivId) throws IOException {
		String arrowId = toolDivId + "-arrow";
		String resultId = toolDivId + "-result";

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-tool-header");
		out.writeAttribute(ONCLICK_ATTR, "toggleToolExpand('" + resultId + "', '" + arrowId + "')");
		out.endBeginTag();
		{
			// Tool name
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-tool-name");
			out.endBeginTag();
			out.writeText(toolCall.getToolName());
			out.endTag(SPAN);

			// Full arguments (no truncation)
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-tool-args");
			out.endBeginTag();
			out.writeText(formatArgsFull(toolCall.getArgs()));
			out.endTag(SPAN);

			// Expand arrow
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-tool-expand");
			out.writeAttribute(ID_ATTR, arrowId);
			out.endBeginTag();
			out.writeText("\u203A"); // › collapsed chevron
			out.endTag(SPAN);
		}
		out.endTag(DIV);
	}

	private void writeToolResult(TagWriter out, ChatToolCall toolCall, String toolDivId) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-tool-result");
		out.writeAttribute(ID_ATTR, toolDivId + "-result");
		out.writeAttribute("style", "display: none");
		out.endBeginTag();
		{
			if (toolCall.getResultJson() != null) {
				String prettyJson = JsonPrettyPrint.prettyPrint(toolCall.getResultJson());
				out.beginBeginTag(PRE);
				out.endBeginTag();
				{
					out.beginBeginTag(CODE);
					out.writeAttribute(CLASS_ATTR, "language-json");
					out.endBeginTag();
					out.writeText(prettyJson);
					out.endTag(CODE);
				}
				out.endTag(PRE);
			}
		}
		out.endTag(DIV);
	}

	private String getTaskIcon(ChatTask.TaskStatus status) {
		switch (status) {
			case COMPLETED:
				return "\u2713";
			case ERROR:
				return "\u2717";
			case IN_PROGRESS:
				return "\u23F3";
			case PENDING:
				return "\u25CB";
			default:
				return "\u25CB";
		}
	}

	/** Interval in seconds after which the in-progress placeholder message rotates. */
	private static final int PLACEHOLDER_ROTATE_SECONDS = 25;

	private static String getTaskHeaderDescription(ChatTask task) {
		switch (task.getStatus()) {
			case COMPLETED:
				return "Completed";
			case ERROR:
				return "Failed";
			case IN_PROGRESS: {
				long elapsedSeconds = java.time.Duration.between(task.getStartTime(), java.time.Instant.now()).getSeconds();
				int index = (int) (elapsedSeconds / PLACEHOLDER_ROTATE_SECONDS) % ChatProcessor.CHAT_TASK_PLACEHOLDERS.length;
				return ChatProcessor.CHAT_TASK_PLACEHOLDERS[index];
			}
			default:
				return task.getDescription();
		}
	}

	private String formatDuration(long ms) {
		return String.format("%.1fs", ms / 1000.0);
	}

	private String formatArgsFull(Map<String, Object> args) {
		if (args == null || args.isEmpty()) {
			return "()";
		}
		StringBuilder sb = new StringBuilder("(");
		boolean first = true;
		for (Map.Entry<String, Object> entry : args.entrySet()) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(entry.getKey()).append("=");
			Object value = entry.getValue();
			if (value instanceof String) {
				String str = (String) value;
				sb.append("\"").append(str.replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
			} else {
				sb.append(value);
			}
			first = false;
		}
		sb.append(")");
		return sb.toString();
	}

	private void writeInputSection(TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-input-section");
		out.endBeginTag();
		{
			// Attachment preview strip (hidden when empty, populated by JS)
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-attachment-preview");
			out.writeAttribute(ID_ATTR, getID() + "-attachment-preview");
			out.endBeginTag();
			out.endTag(DIV);

			// Input row: attach + text input + send
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-input-row");
			out.endBeginTag();
			{
				// Hidden file input
				out.beginBeginTag(INPUT);
				out.writeAttribute(TYPE_ATTR, "file");
				out.writeAttribute(ID_ATTR, getID() + "-file-input");
				out.writeAttribute(CLASS_ATTR, "chat-file-input");
				out.writeAttribute("accept", "image/*,.pdf");
				out.writeAttribute("multiple", "multiple");
				out.writeAttribute("style", "display:none");
				out.endEmptyTag();

				// Attach button (paperclip)
				out.beginBeginTag(BUTTON);
				out.writeAttribute(ID_ATTR, getID() + "-attach-btn");
				out.writeAttribute(TYPE_ATTR, "button");
				out.writeAttribute(CLASS_ATTR, "chat-attach-button");
				out.writeAttribute("title", "Attach files");
				out.endBeginTag();
				writePaperclipSvg(out);
				out.endTag(BUTTON);

				// Auto-approve toggle
				out.beginBeginTag(BUTTON);
				out.writeAttribute(TYPE_ATTR, "button");
				out.writeAttribute(CLASS_ATTR, "approval-mode-indicator");
				out.writeAttribute("title", "Auto-approve is OFF \u2014 click to enable");
				out.endBeginTag();
				out.writeText("\u2713"); // ✓
				out.endTag(BUTTON);

				// Text input
				out.beginBeginTag(INPUT);
				out.writeAttribute(TYPE_ATTR, "text");
				out.writeAttribute(ID_ATTR, getInputId());
				out.writeAttribute(NAME_ATTR, "message");
				out.writeAttribute(CLASS_ATTR, "chat-input");
				out.writeAttribute(PLACEHOLDER_ATTR, "Type a message...");
				out.endEmptyTag();

				// Send button
				out.beginBeginTag(BUTTON);
				out.writeAttribute(ID_ATTR, getButtonId());
				out.writeAttribute(TYPE_ATTR, "button");
				out.writeAttribute(CLASS_ATTR, "chat-button");
				out.endBeginTag();
				out.writeText("\u2191");
				out.endTag(BUTTON);
			}
			out.endTag(DIV);
		}
		out.endTag(DIV);
	}

	private void writePaperclipSvg(TagWriter out) throws IOException {
		out.beginBeginTag("svg");
		out.writeAttribute("xmlns", "http://www.w3.org/2000/svg");
		out.writeAttribute("width", "20");
		out.writeAttribute("height", "20");
		out.writeAttribute("viewBox", "0 0 24 24");
		out.writeAttribute("fill", "none");
		out.writeAttribute("stroke", "currentColor");
		out.writeAttribute("stroke-width", "2");
		out.writeAttribute("stroke-linecap", "round");
		out.writeAttribute("stroke-linejoin", "round");
		out.endBeginTag();
		{
			out.beginBeginTag("path");
			out.writeAttribute("d",
				"M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48");
			out.endEmptyTag();
		}
		out.endTag("svg");
	}

	private String getInputId() {
		return getID() + "-input";
	}

	private String getButtonId() {
		return getID() + "-button";
	}


	/**
	 * Command that sends a chat message (TL AJAX fallback).
	 * Primary send path is the JSON API via {@code chat-client.js}.
	 */
	private static class SendMessage extends ControlCommand {

		static final ControlCommand INSTANCE = new SendMessage();

		static final String COMMAND_ID = "sendMessage";

		private static final String TEXT_PARAM = "text";

		SendMessage() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {
			ChatControl chatControl = (ChatControl) control;
			String text = (String) arguments.get(TEXT_PARAM);

			if (text != null) {
				String trimmedText = text.trim();
				if (!trimmedText.isEmpty()) {
						String storedName = chatControl._taskTracker.getUserName();
					String userName = storedName != null ? storedName : "Anonymous";
					// No UIExecutor polling: the client uses SSE for real-time updates.
					ChatProcessor.SendResult result = ChatProcessor.sendMessage(
						chatControl._taskTracker, userName, trimmedText, null,
						() -> chatControl.requestRepaint(), null);
					if (result.getTaskId() != null) {
						chatControl._processingState = chatControl._taskTracker.getAnyActiveProcessingState();
						chatControl.requestRepaint();
					} else if (!result.isBusy()) {
						chatControl._chat.addMessage(new ChatMessage("System", "Failed to start AI processing"));
						chatControl.requestRepaint();
					} else {
						chatControl.requestRepaint();
					}
				}
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.text("chat.sendMessage");
		}
	}

}
