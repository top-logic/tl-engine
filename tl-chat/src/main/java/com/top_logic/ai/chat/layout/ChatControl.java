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
import com.top_logic.util.TLContext;

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
		out.endBeginTag();
		{
			writeMessages(context, out);
			writeInputSection(out);
		}
		out.endTag(DIV);

		// Add JavaScript for task/tool expand/collapse
		writeTaskToggleScript(out);

		// Load chat client via inline script so it runs when fragment is AJAX-injected
		// (browsers do not execute <script src="..."> in injected HTML)
		String contextPath = context.getContextPath();
		out.beginScript();
		out.append("(function(){var s=document.createElement('script');s.src='");
		out.append(contextPath);
		out.append("/script/chat-client.js';s.type='text/javascript';(document.head||document.documentElement).appendChild(s);})();");
		out.endScript();
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
				: _taskTracker.getCurrentProcessingState();
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
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-message");
		out.writeAttribute("data-message-id", "m" + messageIndex);
		out.endBeginTag();
		{
			// User name
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-user");
			out.endBeginTag();
			out.writeText(msg.getUser());
			out.endTag(SPAN);

			out.writeText(" ");

			// Timestamp
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-time");
			out.endBeginTag();
			out.writeText("(" + TIME_FORMAT.format(msg.getTimestamp().atZone(ZoneId.systemDefault())) + ")");
			out.endTag(SPAN);

			out.writeText(": ");

			// Message text
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-text");
			out.endBeginTag();
			Converters.markdownToHtml(msg.getText()).write(context, out);
			out.endTag(SPAN);
		}
		out.endTag(DIV);
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
		String arrowId = taskDivId + "-arrow";
		String bodyId = taskDivId + "-body";

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-task-header");
		out.writeAttribute(ONCLICK_ATTR, "toggleTaskExpand('" + bodyId + "', '" + arrowId + "')");
		out.endBeginTag();
		{
			// Status icon
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-task-icon");
			out.endBeginTag();
			out.writeText(getTaskIcon(task.getStatus()));
			out.endTag(SPAN);

			// Description: funny placeholder while in progress, "Completed"/"Failed" when done
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

			// Expand arrow
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-task-expand");
			out.writeAttribute(ID_ATTR, arrowId);
			out.endBeginTag();
			out.writeText("\u25BC");
			out.endTag(SPAN);
		}
		out.endTag(DIV);
	}

	private void writeTaskBody(DisplayContext context, TagWriter out, ChatTask task, String taskDivId)
			throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-task-body");
		out.writeAttribute(ID_ATTR, taskDivId + "-body");
		out.endBeginTag();
		{
			// Timeline: agent calls and tool calls in order
			List<ChatTimelineEvent> events = task.getTimelineEvents();
			for (int i = 0; i < events.size(); i++) {
				ChatTimelineEvent ev = events.get(i);
				String eventId = "e" + i;
				if (ev.getKind() == ChatTimelineEvent.Kind.TOOL_CALL && ev.getToolCall() != null) {
					writeToolCall(out, ev.getToolCall(), eventId);
				} else if (ev.getKind() == ChatTimelineEvent.Kind.AGENT_CALL) {
					writeAgentCall(out, ev, taskDivId + "-agent-" + i, eventId);
				}
			}

			// Error message if failed
			if (task.getStatus() == ChatTask.TaskStatus.ERROR && task.getErrorMessage() != null) {
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, "chat-task-error");
				out.endBeginTag();
				out.writeText("Error: " + task.getErrorMessage());
				out.endTag(DIV);
			}
		}
		out.endTag(DIV);
	}

	private void writeAgentCall(TagWriter out, ChatTimelineEvent ev, String agentCallId, String eventId)
			throws IOException {
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
				out.writeText("\u25B6");
				out.endTag(SPAN);
			}
			out.endTag(DIV);

			// Collapsible body: full input and result (no truncation)
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-agent-call-body");
			out.writeAttribute(ID_ATTR, bodyId);
			out.writeAttribute("style", "display: none");
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
			out.writeText("\u25B6");
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
		// Render clean HTML; chat-client.js handles sending via JSON API and SSE.
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-input-section");
		out.endBeginTag();
		{
			out.beginBeginTag(INPUT);
			out.writeAttribute(TYPE_ATTR, "text");
			out.writeAttribute(ID_ATTR, getInputId());
			out.writeAttribute(NAME_ATTR, "message");
			out.writeAttribute(CLASS_ATTR, "chat-input");
			out.writeAttribute(PLACEHOLDER_ATTR, "Type a message...");
			out.endEmptyTag();

			out.beginBeginTag(BUTTON);
			out.writeAttribute(ID_ATTR, getButtonId());
			out.writeAttribute(TYPE_ATTR, "button");
			out.writeAttribute(CLASS_ATTR, "chat-button");
			out.endBeginTag();
			out.writeText("Send");
			out.endTag(BUTTON);
		}
		out.endTag(DIV);
	}

	private String getInputId() {
		return getID() + "-input";
	}

	private String getButtonId() {
		return getID() + "-button";
	}

	private void writeTaskToggleScript(TagWriter out) throws IOException {
		out.beginScript();
		out.append("function toggleTaskExpand(bodyId, arrowId) {");
		out.append("  var body = document.getElementById(bodyId);");
		out.append("  var arrow = document.getElementById(arrowId);");
		out.append("  if (body && arrow) {");
		out.append("    if (body.style.display === 'none' || body.style.display === '') {");
		out.append("      body.style.display = 'block';");
		out.append("      arrow.textContent = '\u25BC';");
		out.append("    } else {");
		out.append("      body.style.display = 'none';");
		out.append("      arrow.textContent = '\u25B6';");
		out.append("    }");
		out.append("  }");
		out.append("}");
		out.append("function toggleToolExpand(resultId, arrowId) {");
		out.append("  var result = document.getElementById(resultId);");
		out.append("  var arrow = document.getElementById(arrowId);");
		out.append("  if (result && arrow) {");
		out.append("    if (result.style.display === 'none' || result.style.display === '') {");
		out.append("      result.style.display = 'block';");
		out.append("      arrow.textContent = '\u25BC';");
		out.append("    } else {");
		out.append("      result.style.display = 'none';");
		out.append("      arrow.textContent = '\u25B6';");
		out.append("    }");
		out.append("  }");
		out.append("}");
		out.endScript();
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
					String userName = getCurrentUserName();
					// No UIExecutor polling: the client uses SSE for real-time updates.
					ChatProcessor.SendResult result = ChatProcessor.sendMessage(
						chatControl._taskTracker, userName, trimmedText,
						() -> chatControl.requestRepaint(), null);
					if (result.getTaskId() != null) {
						chatControl._processingState = chatControl._taskTracker.getCurrentProcessingState();
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

		private String getCurrentUserName() {
			try {
				TLContext context = TLContext.getContext();
				if (context != null && context.getPerson() != null) {
					return context.getPerson().getName();
				}
			} catch (Exception e) {
				// Fallback if person not available
			}
			return "Anonymous";
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.text("chat.sendMessage");
		}
	}

}
