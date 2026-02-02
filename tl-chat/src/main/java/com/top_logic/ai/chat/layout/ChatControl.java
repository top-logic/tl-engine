/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.agents.ModelExplorer;
import com.top_logic.ai.service.agents.OrchestratorAgent;
import com.top_logic.ai.service.agents.TLScriptAgent;
import com.top_logic.ai.service.agents.UMLSpecificationAgent;
import com.top_logic.ai.service.scripting.Converters;
import com.top_logic.ai.service.scripting.OpenAIScriptFunctions;
import com.top_logic.ai.service.tools.ToolCallTracker;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.TraceEntry;
import com.top_logic.ai.service.tools.util.JsonPrettyPrint;
import com.top_logic.ai.service.tools.help.RequestModelExplorationTool;
import com.top_logic.ai.service.tools.help.TLScriptReferenceTool;
import com.top_logic.ai.service.tools.model.AnnotationDefinitionExplorerTool;
import com.top_logic.ai.service.tools.model.InstanceBrowserTool;
import com.top_logic.ai.service.tools.model.ModelAnnotationBrowserTool;
import com.top_logic.ai.service.tools.model.ModelBrowserTool;
import com.top_logic.ai.service.tools.model.ModelSearchTool;
import com.top_logic.ai.service.tools.model.TypeAnalysisTool;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContext;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;

/**
 * A minimal chat control displaying messages and allowing user input.
 *
 * @author jhu
 */
public class ChatControl extends AbstractControlBase {

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(SendMessage.INSTANCE, PollAIProgress.INSTANCE);

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	/** Polling interval for UI updates (milliseconds) */
	private static final long POLL_INTERVAL_MS = 500;

	/** Funny placeholder descriptions for in-progress chat tasks (Cursor/Claude style). */
	private static final String[] CHAT_TASK_PLACEHOLDERS = {
		"Flabbergasting...",
		"Contemplating...",
		"Warming up neurons...",
		"Consulting the oracle...",
		"Assembling thoughts...",
		"Polishing the answer...",
		"Thinking really hard...",
		"Consulting ancient scrolls...",
		"Loading infinite wisdom...",
		"Brewing ideas...",
		"Reticulating splines...",
		"Herding cats...",
		"Convincing the AI...",
		"Spinning up...",
	};

	private static String randomChatTaskPlaceholder() {
		return CHAT_TASK_PLACEHOLDERS[ThreadLocalRandom.current().nextInt(CHAT_TASK_PLACEHOLDERS.length)];
	}

	// #region agent log
	private static final String DEBUG_LOG_PATH = "/home/jhu/git/tl-ai/.cursor/debug.log";

	private static void debugLog(String hypothesisId, String location, String message, String dataStr) {
		try {
			String dataEsc = dataStr == null ? "null" : "\"" + dataStr.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
			String msgEsc = message.replace("\\", "\\\\").replace("\"", "\\\"");
			String json = "{\"hypothesisId\":\"" + hypothesisId + "\",\"location\":\"" + location + "\",\"message\":\"" + msgEsc + "\",\"data\":" + dataEsc + ",\"timestamp\":" + System.currentTimeMillis() + "}\n";
			try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(DEBUG_LOG_PATH, true), StandardCharsets.UTF_8)) {
				w.write(json);
			}
		} catch (Throwable ignored) {
		}
	}
	// #endregion

	/** Background thread pool for AI operations */
	private static final ExecutorService BACKGROUND_EXECUTOR =
		Executors.newCachedThreadPool(new ThreadFactory() {
			private final AtomicInteger _counter = new AtomicInteger();

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName("ChatControl-AI-Worker-" + _counter.incrementAndGet());
				thread.setDaemon(true);
				return thread;
			}
		});

	private final Chat _chat;

	/** Shared state for background processing (null when not processing) */
	private AIProcessingState _processingState = null;

	/** Session-scoped task progress tracker */
	private final TaskProgressTracker _taskTracker;

	/**
	 * Creates a new {@link ChatControl}.
	 */
	public ChatControl(Chat chat, TaskProgressTracker taskTracker) {
		super(COMMANDS);
		_chat = chat;
		_taskTracker = taskTracker;
		// #region agent log
		debugLog("B", "ChatControl.<init>", "ChatControl created", "entries=" + chat.getEntries().size());
		// #endregion
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
		// #region agent log
		boolean had = _processingState != null;
		debugLog("A", "ChatControl.internalDetach", "internalDetach called", "hadProcessingState=" + had);
		// #endregion
		// Do NOT cancel processing on detach: state lives in tracker so navigation away and back
		// still shows in-progress task and agent answer when it arrives.
		_processingState = null;
		super.internalDetach();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			writeMessages(context, out);
			writeInputSection(out);
		}
		out.endTag(DIV);

		// Add JavaScript for task/tool expand/collapse
		writeTaskToggleScript(out);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		com.top_logic.mig.html.HTMLUtil.appendCSSClass(out, "chat-control");
	}

	private void writeMessages(DisplayContext context, TagWriter out) throws IOException {
		// #region agent log
		int entriesCount = _chat.getEntries().size();
		boolean hasState = _processingState != null;
		debugLog("C", "ChatControl.writeMessages", "writeMessages", "hasProcessingState=" + hasState + " entriesCount=" + entriesCount);
		// #endregion
		String messagesId = getID() + "-messages";
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-messages");
		out.writeAttribute(ID_ATTR, messagesId);
		out.endBeginTag();
		{
			// Render entries in order: user message → task trace → agent response
			for (ChatEntry entry : _chat.getEntries()) {
				if (entry instanceof ChatEntry.MessageEntry me) {
					writeMessage(context, out, me.message());
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
			// Use state from this control or from tracker so returning to the chat view shows it.
			// Only show here when task is still IN_PROGRESS; when COMPLETED/ERROR it's already
			// in entries as a snapshot, so skip to avoid rendering the same task twice.
			AIProcessingState state = _processingState != null ? _processingState
				: (AIProcessingState) _taskTracker.getCurrentProcessingState();
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

	private void writeMessage(DisplayContext context, TagWriter out, ChatMessage msg) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-message");
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
			out.writeText("▼");
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
				if (ev.getKind() == ChatTimelineEvent.Kind.TOOL_CALL && ev.getToolCall() != null) {
					writeToolCall(out, ev.getToolCall());
				} else if (ev.getKind() == ChatTimelineEvent.Kind.AGENT_CALL) {
					writeAgentCall(out, ev, taskDivId + "-agent-" + i);
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

	private void writeAgentCall(TagWriter out, ChatTimelineEvent ev, String agentCallId) throws IOException {
		String bodyId = agentCallId + "-body";
		String arrowId = agentCallId + "-arrow";

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-agent-call");
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
				out.writeText("▶");
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

	private static String truncateForDisplay(String s, int maxLen) {
		if (s == null) {
			return "";
		}
		return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
	}

	private static ChatTimelineEvent toChatTimelineEvent(TraceEntry entry, ToolCallTracker tracker) {
		if (entry instanceof TraceEntry.ToolCallTraceEntry tc) {
			String resultJson = tracker.getRawResultJsons().get(tc.resultRef());
			ChatToolCall chatToolCall = new ChatToolCall(tc.toolName(), tc.args(), resultJson);
			return ChatTimelineEvent.toolCall(chatToolCall);
		}
		if (entry instanceof TraceEntry.AgentCallTraceEntry ac) {
			return ChatTimelineEvent.agentCall(ac.fromAgent(), ac.toAgent(), ac.inputSummary(), ac.resultSummary());
		}
		if (entry instanceof TraceEntry.MutableAgentCallTraceEntry mac) {
			return ChatTimelineEvent.agentCallWithDynamicResult(mac.fromAgent(), mac.toAgent(), mac.inputSummary(),
				mac::resultSummary);
		}
		return null;
	}

	private void writeToolCall(TagWriter out, ChatToolCall toolCall) throws IOException {
		String toolDivId = "tool-" + toolCall.getId();

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-tool-call");
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

			// Duration
			if (toolCall.getEndTime() != null) {
				long durationMs = java.time.Duration.between(toolCall.getStartTime(), toolCall.getEndTime()).toMillis();
				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, "chat-tool-time");
				out.endBeginTag();
				out.writeText(formatDuration(durationMs));
				out.endTag(SPAN);
			}

			// Expand arrow (▶ when collapsed, ▼ when expanded)
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-tool-expand");
			out.writeAttribute(ID_ATTR, arrowId);
			out.endBeginTag();
			out.writeText("▶");
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
				return "✓";
			case ERROR:
				return "✗";
			case IN_PROGRESS:
				return "⏳";
			case PENDING:
				return "○";
			default:
				return "○";
		}
	}

	/** Interval in seconds after which the in-progress placeholder message rotates. */
	private static final int PLACEHOLDER_ROTATE_SECONDS = 25;

	/**
	 * Returns the header text for a task: "Completed" or "Failed" when done; while in progress,
	 * a funny placeholder that rotates every {@value #PLACEHOLDER_ROTATE_SECONDS} seconds.
	 */
	private static String getTaskHeaderDescription(ChatTask task) {
		switch (task.getStatus()) {
			case COMPLETED:
				return "Completed";
			case ERROR:
				return "Failed";
			case IN_PROGRESS: {
				long elapsedSeconds = java.time.Duration.between(task.getStartTime(), java.time.Instant.now()).getSeconds();
				int index = (int) (elapsedSeconds / PLACEHOLDER_ROTATE_SECONDS) % CHAT_TASK_PLACEHOLDERS.length;
				return CHAT_TASK_PLACEHOLDERS[index];
			}
			default:
				return task.getDescription();
		}
	}

	private String formatDuration(long ms) {
		if (ms < 1000) {
			return ms + "ms";
		}
		return String.format("%.1fs", ms / 1000.0);
	}

	/** Full arguments string for display (no truncation). */
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

	/**
	 * Formats tool calls for display in the chat.
	 *
	 * @param tracker
	 *        The tool tracker containing recorded tool calls.
	 * @return A formatted string listing all tool calls with their raw JSON results.
	 */
	private String formatToolCalls(ToolCallTracker tracker) {
		StringBuilder sb = new StringBuilder();
		sb.append("## Tool Calls\n\n");

		for (ToolCallTracker.ToolCall call : tracker.getToolCalls()) {
			sb.append("- **").append(call.getTool()).append("**");
			if (!call.getArgs().isEmpty()) {
				sb.append("(");
				boolean first = true;
				for (Map.Entry<String, Object> arg : call.getArgs().entrySet()) {
					if (!first) {
						sb.append(", ");
					}
					sb.append(arg.getKey()).append("=");
					Object value = arg.getValue();
					if (value instanceof String) {
						String str = (String) value;
						if (str.length() > 50) {
							sb.append("\"").append(str.substring(0, 47)).append("...\"");
						} else {
							sb.append("\"").append(str).append("\"");
						}
					} else {
						sb.append(value);
					}
					first = false;
				}
				sb.append(")");
			}
			sb.append(" -> [").append(call.getResultRef()).append("]\n\n");

			// Add the raw JSON result
			String rawJson = tracker.getRawResultJsons().get(call.getResultRef());
			if (rawJson != null) {
				sb.append("  ```json\n  ").append(rawJson.replace("\n", "\n  ")).append("\n  ```\n\n");
			}
		}

		return sb.toString();
	}

	private void writeInputSection(TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-input-section");
		out.endBeginTag();
		{
			// Text input
			out.beginBeginTag(INPUT);
			out.writeAttribute(TYPE_ATTR, "text");
			out.writeAttribute(ID_ATTR, getInputId());
			out.writeAttribute(NAME_ATTR, "message");
			out.writeAttribute(CLASS_ATTR, "chat-input");
			out.writeAttribute(PLACEHOLDER_ATTR, "Type a message...");
			out.writeAttribute(ONKEYPRESS_ATTR,
				"if(event.keyCode == 13 || event.key == 'Enter') { " + getSendCommandJS() + " return false; }");
			out.endEmptyTag();

			// Send button
			out.beginBeginTag(BUTTON);
			out.writeAttribute(ID_ATTR, getButtonId());
			out.writeAttribute(TYPE_ATTR, "button");
			out.writeAttribute(CLASS_ATTR, "chat-button");
			out.writeAttribute(ONCLICK_ATTR, getSendCommandJS());
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

	private String getSendCommandJS() {
		return "var input = document.getElementById('" + getInputId() + "');" +
			"var text = input.value.trim();" +
			"if (text) {" +
			"  services.ajax.execute('dispatchControlCommand', {" +
			"    controlID: '" + getID() + "'," +
			"    controlCommand: '" + SendMessage.COMMAND_ID + "'," +
			"    text: text" +
			"  });" +
			"  input.value = '';" +
			"}" +
			"return false;";
	}

	private void writeTaskToggleScript(TagWriter out) throws IOException {
		out.beginScript();
		out.append("function toggleTaskExpand(bodyId, arrowId) {");
		out.append("  var body = document.getElementById(bodyId);");
		out.append("  var arrow = document.getElementById(arrowId);");
		out.append("  if (body && arrow) {");
		out.append("    if (body.style.display === 'none' || body.style.display === '') {");
		out.append("      body.style.display = 'block';");
		out.append("      arrow.textContent = '▼';");
		out.append("    } else {");
		out.append("      body.style.display = 'none';");
		out.append("      arrow.textContent = '▶';");
		out.append("    }");
		out.append("  }");
		out.append("}");
		out.append("function toggleToolExpand(resultId, arrowId) {");
		out.append("  var result = document.getElementById(resultId);");
		out.append("  var arrow = document.getElementById(arrowId);");
		out.append("  if (result && arrow) {");
		out.append("    if (result.style.display === 'none' || result.style.display === '') {");
		out.append("      result.style.display = 'block';");
		out.append("      arrow.textContent = '▼';");
		out.append("    } else {");
		out.append("      result.style.display = 'none';");
		out.append("      arrow.textContent = '▶';");
		out.append("    }");
		out.append("  }");
		out.append("}");
		out.endScript();
	}

	/**
	 * Checks if the given text is a model creation command.
	 */
	private boolean isModelCreationCommand(String text) {
		return text != null && text.startsWith("/model-creation");
	}

	/**
	 * Extracts business requirements from a model creation command.
	 */
	private String extractBusinessRequirements(String command) {
		return command.substring("/model-creation".length()).trim();
	}

	/**
	 * Checks if the given text is a model exploration command.
	 */
	private boolean isModelExplorationCommand(String text) {
		return text != null && text.startsWith("/explore-model");
	}

	/**
	 * Extracts the exploration question from a model exploration command.
	 */
	private String extractExplorationQuestion(String command) {
		return command.substring("/explore-model".length()).trim();
	}

	/**
	 * Checks if the given text is a TLScript reference command.
	 */
	private boolean isTLScriptCommand(String text) {
		return text != null && text.startsWith("/tlscript");
	}

	/**
	 * Extracts the question from a TLScript reference command.
	 */
	private String extractTLScriptQuestion(String command) {
		return command.substring("/tlscript".length()).trim();
	}

	/**
	 * Executes user message through OrchestratorAgent (single chat entrypoint).
	 * Sets trace so AgentCall and ToolCall events are visible in the UI timeline.
	 * Uses state from tracker so the background task keeps working after navigation (control detach).
	 */
	private void executeOrchestrator(String userMessage) throws Exception {
		AIProcessingState state = _processingState != null ? _processingState
			: (AIProcessingState) _taskTracker.getCurrentProcessingState();
		if (state == null) {
			return;
		}
		String taskDescription = randomChatTaskPlaceholder();
		String taskId = _taskTracker.startTask(taskDescription);
		state.setCurrentTaskId(taskId);

		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);
		state.setToolCallTracker(tracker);

		try {
			Logger.info("ChatControl: Starting Orchestrator in thread: " + Thread.currentThread().getName(),
				ChatControl.class);

			String result = OrchestratorAgent.chat(userMessage);

			state.addPendingMessage(new ChatMessage("Agent", result));
			_taskTracker.completeTask(taskId);

		} catch (Exception ex) {
			_taskTracker.failTask(taskId, ex.getMessage());
			throw ex;
		} finally {
			ToolCallTracking.setTracker(null);
			state.setToolCallTracker(null);
		}
	}

	/**
	 * Executes a regular chat request in the background thread with progress tracking.
	 * @deprecated Use {@link #executeOrchestrator(String)}; Orchestrator is the single entrypoint.
	 */
	@Deprecated
	private void executeRegularChat(String userMessage) throws Exception {
		// Start task
		String taskDescription = "Chat: " +
			(userMessage.length() > 50 ? userMessage.substring(0, 47) + "..." : userMessage);
		String taskId = _taskTracker.startTask(taskDescription);
		_processingState.setCurrentTaskId(taskId);

		try {
			// Convert message history to OpenAI format
			List<Object> messages = new ArrayList<>();
			for (ChatMessage msg : _chat.getMessages()) {
				// Skip the current user message as it's already been added
				if (msg.getText().equals(userMessage) && !"AI".equals(msg.getUser())) {
					continue;
				}

				Map<String, Object> messageMap = Map.of(
					"role", "AI".equals(msg.getUser()) ? "assistant" : "user",
					"content", msg.getText()
				);
				messages.add(messageMap);
			}

			// Add the current user message
			messages.add(Map.of("role", "user", "content", userMessage));

			// Call OpenAI API (this is the blocking operation)
			String aiResponse = OpenAIScriptFunctions.chat(messages, null, null, null);

			// Add result to pending messages
			_processingState.addPendingMessage(new ChatMessage("AI", aiResponse));

			// Complete task successfully
			_taskTracker.completeTask(taskId);

		} catch (Exception ex) {
			// Fail the task with error
			_taskTracker.failTask(taskId, ex.getMessage());
			throw ex;
		} finally {
			_processingState.setCurrentTaskId(null);
		}
	}

	/**
	 * Executes a model creation command in the background thread with progress tracking.
	 */
	private void executeModelCreation(String command) throws Exception {
		String requirements = extractBusinessRequirements(command);

		// Start task
		String taskDescription = "Model creation: " +
			(requirements.length() > 50 ? requirements.substring(0, 47) + "..." : requirements);
		String taskId = _taskTracker.startTask(taskDescription);
		_processingState.setCurrentTaskId(taskId);

		try {
			Logger.info("ChatControl: Starting workflow execution in thread: " + Thread.currentThread().getName(),
				ChatControl.class);

			// Execute the workflow
			String result = UMLSpecificationAgent.execute(requirements);

			_processingState.addPendingMessage(new ChatMessage("Agent", result));

			// Complete task successfully
			_taskTracker.completeTask(taskId);

		} catch (Exception ex) {
			// Fail the task with error
			_taskTracker.failTask(taskId, ex.getMessage());
			throw ex;
		} finally {
			_processingState.setCurrentTaskId(null);
		}
	}

	/**
	 * Executes a model exploration command in the background thread with progress tracking.
	 */
	private void executeModelExploration(String command) throws Exception {
		String explorationQuestion = extractExplorationQuestion(command);

		// Start task
		String taskDescription = "Exploring: " +
			(explorationQuestion.length() > 50 ? explorationQuestion.substring(0, 47) + "..." : explorationQuestion);
		String taskId = _taskTracker.startTask(taskDescription);
		_processingState.setCurrentTaskId(taskId);

		try {
			// Set up tool tracking
			ToolCallTracker tracker = new ToolCallTracker();
			ToolCallTracking.setTracker(tracker);

			// Store tracker in processing state for real-time polling
			_processingState.setToolCallTracker(tracker);

			try {
				Logger.info("ChatControl: Starting model exploration in thread: " + Thread.currentThread().getName(),
					ChatControl.class);

				// Get the model from AIService
				AIService aiService = AIService.getInstance();
				if (aiService == null) {
					throw new RuntimeException("AIService is not available. Please ensure the service is started.");
				}

				// Use mistral-large-latest model
				ChatModel explorationModel = aiService.getChatModel("mistral-large-latest");

				// Create internal tool instances
				ModelBrowserTool browserTool = new ModelBrowserTool();
				InstanceBrowserTool instanceTool = new InstanceBrowserTool();
				TypeAnalysisTool analysisTool = new TypeAnalysisTool();
				ModelSearchTool searchTool = new ModelSearchTool();
				ModelAnnotationBrowserTool annotationTool = new ModelAnnotationBrowserTool();
				AnnotationDefinitionExplorerTool definitionTool = new AnnotationDefinitionExplorerTool();

				// Build the ModelExplorer agent
				ModelExplorer explorer = AgenticServices.agentBuilder(ModelExplorer.class)
					.chatModel(explorationModel)
					.tools(browserTool, instanceTool, analysisTool, searchTool, annotationTool, definitionTool)
					.build();

				// Execute the exploration (tool calls will be detected by polling)
				String result = explorer.explore(explorationQuestion);

				// Add final result
				_processingState.addPendingMessage(new ChatMessage("Agent", result));

				// Complete task successfully
				_taskTracker.completeTask(taskId);

			} finally {
				// Always clear the tracker
				ToolCallTracking.setTracker(null);
			}

		} catch (Exception ex) {
			// Fail the task with error
			_taskTracker.failTask(taskId, ex.getMessage());
			throw ex;
		} finally {
			_processingState.setCurrentTaskId(null);
			_processingState.setToolCallTracker(null);
		}
	}

	/**
	 * Executes a TLScript reference command in the background thread with progress tracking.
	 */
	private void executeTLScript(String command) throws Exception {
		String question = extractTLScriptQuestion(command);

		// Start task
		String taskDescription = "TLScript: " +
			(question.length() > 50 ? question.substring(0, 47) + "..." : question);
		String taskId = _taskTracker.startTask(taskDescription);
		_processingState.setCurrentTaskId(taskId);

		try {
			// Set up tool tracking
			ToolCallTracker tracker = new ToolCallTracker();
			ToolCallTracking.setTracker(tracker);

			// Store tracker in processing state for real-time polling
			_processingState.setToolCallTracker(tracker);

			try {
				Logger.info("ChatControl: Starting TLScript reference in thread: " + Thread.currentThread().getName(),
					ChatControl.class);

				// Get the model from AIService
				AIService aiService = AIService.getInstance();
				if (aiService == null) {
					throw new RuntimeException("AIService is not available. Please ensure the service is started.");
				}

				// Use mistral-large-latest model
				ChatModel tlscriptModel = aiService.getChatModel("mistral-large-latest");

				// Create TLScript reference tool and Request Model Exploration tool (structured handoff)
				TLScriptReferenceTool tlscriptTool = new TLScriptReferenceTool();
				RequestModelExplorationTool requestExplorationTool = new RequestModelExplorationTool();

				// Build the TLScriptAgent
				TLScriptAgent agent = AgenticServices.agentBuilder(TLScriptAgent.class)
					.chatModel(tlscriptModel)
					.tools(tlscriptTool, requestExplorationTool)
					.build();

				// Execute the agent (tool calls will be detected by polling)
				String result = agent.answer(question);

				// Add final result
				_processingState.addPendingMessage(new ChatMessage("Agent", result));

				// Complete task successfully
				_taskTracker.completeTask(taskId);

			} finally {
				// Always clear the tracker
				ToolCallTracking.setTracker(null);
			}

		} catch (Exception ex) {
			// Fail the task with error
			_taskTracker.failTask(taskId, ex.getMessage());
			throw ex;
		} finally {
			_processingState.setCurrentTaskId(null);
			_processingState.setToolCallTracker(null);
		}
	}

	/**
	 * Applies poll updates for the given state to chat and taskTracker. Clears tracker and
	 * cancels polling when processing is done. Used by both the control and the scheduled
	 * polling runnable (so updates continue after navigation away).
	 */
	private static boolean applyPollUpdates(AIProcessingState state, Chat chat,
			TaskProgressTracker taskTracker) {
		if (state == null) {
			return false;
		}
		boolean hasUpdates = false;
		String currentTaskId = state.getCurrentTaskId();

		// Check for new messages (agent response when task completes)
		List<ChatMessage> newMessages = state.consumePendingMessages();
		if (!newMessages.isEmpty()) {
			if (currentTaskId != null) {
				ChatTask task = taskTracker.getTask(currentTaskId);
				chat.addTaskSnapshot(task);
			}
			for (ChatMessage msg : newMessages) {
				chat.addMessage(msg);
			}
			hasUpdates = true;
		}

		// Check for new timeline events (tool calls and agent calls) in real-time
		ToolCallTracker tracker = state.getToolCallTracker();
		if (currentTaskId != null && tracker != null) {
			List<TraceEntry> timeline = tracker.getTimeline();
			int currentCount = timeline.size();
			int lastSeen = state.getLastSeenToolCallCount();
			if (currentCount > lastSeen) {
				for (int i = lastSeen; i < currentCount; i++) {
					TraceEntry entry = timeline.get(i);
					ChatTimelineEvent chatEv = toChatTimelineEvent(entry, tracker);
					if (chatEv != null) {
						taskTracker.addTimelineEvent(currentTaskId, chatEv);
					}
				}
				state.setLastSeenToolCallCount(currentCount);
				hasUpdates = true;
			}
		}

		// Check for completion
		if (!state.isProcessing()) {
			if (state._pollingTask != null) {
				state._pollingTask.cancel(false);
				state._pollingTask = null;
			}
			Throwable error = state.getError();
			if (error != null) {
				if (currentTaskId != null) {
					ChatTask task = taskTracker.getTask(currentTaskId);
					chat.addTaskSnapshot(task);
				}
				String errorMsg = "Error: " + error.getMessage();
				if (error instanceof InterruptedException) {
					errorMsg = "Processing was cancelled";
				}
				chat.addMessage(new ChatMessage("AI", errorMsg));
				hasUpdates = true;
			}
			taskTracker.clearCurrentProcessingState();
		}
		return hasUpdates;
	}

	/**
	 * Polls for AI processing updates and updates the UI. Uses state from this control or from
	 * tracker so polling continues to apply updates after navigation away and back.
	 */
	private void pollForUpdates() {
		AIProcessingState state = _processingState != null ? _processingState
			: (AIProcessingState) _taskTracker.getCurrentProcessingState();

		if (state == null) {
			// #region agent log
			debugLog("E", "ChatControl.pollForUpdates", "pollForUpdates state null early return", null);
			// #endregion
			return;
		}

		boolean hasUpdates = applyPollUpdates(state, _chat, _taskTracker);
		if (hasUpdates) {
			requestRepaint();
		}
		if (_taskTracker.getCurrentProcessingState() == null) {
			_processingState = null;
		}
	}

	/**
	 * Background task for AI processing with detailed progress tracking.
	 */
	private class AIBackgroundTask implements Runnable {
		private final String _userMessage;
		private final String _operationType;

		public AIBackgroundTask(String userMessage, String operationType) {
			_userMessage = userMessage;
			_operationType = operationType;
		}

		@Override
		public void run() {
			// Use state from tracker so we keep working after user navigates away (control detach)
			AIProcessingState state = (AIProcessingState) _taskTracker.getCurrentProcessingState();
			if (state == null) {
				return;
			}
			// Run in system interaction context for AIService access
			try {
				ThreadContextManager.inSystemInteraction(ChatControl.class, () -> {
					try {
						switch (_operationType) {
							case "orchestrator":
								executeOrchestrator(_userMessage);
								break;
							default:
								// Fallback for legacy operation types: route through Orchestrator
								executeOrchestrator(_userMessage);
								break;
						}

						state.completeSuccessfully();
						return null; // ComputationEx2 requires a return value

					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						state.completeWithError(ex);
						return null;
					} catch (Exception ex) {
						Logger.error("AI processing failed: " + ex.getMessage(), ex, ChatControl.class);
						state.completeWithError(ex);
						return null;
					}
				});
			} catch (Exception ex) {
				Logger.error("Failed to run background task: " + ex.getMessage(), ex, ChatControl.class);
				state.completeWithError(ex);
			}
		}
	}

	/**
	 * Thread-safe container for AI processing state shared between background and UI threads.
	 */
	private static class AIProcessingState {
		private final Object _lock = new Object();

		// Processing status
		private volatile boolean _processing = false;
		private volatile boolean _completed = false;
		private volatile Throwable _error = null;

		// Current task ID for progress tracking
		private volatile String _currentTaskId = null;

		// Tool call tracking for real-time updates
		private volatile ToolCallTracker _toolCallTracker = null;
		private final AtomicInteger _lastSeenToolCallCount = new AtomicInteger(0);

		// Result accumulation
		private final List<ChatMessage> _pendingMessages = new ArrayList<>();

		// Lifecycle management
		volatile Future<?> _backgroundTask = null;
		volatile ScheduledFuture<?> _pollingTask = null;

		public boolean isProcessing() {
			synchronized (_lock) {
				return _processing && !_completed;
			}
		}

		public void startProcessing() {
			synchronized (_lock) {
				_processing = true;
				_completed = false;
				_error = null;
				_pendingMessages.clear();
			}
		}

		public void addPendingMessage(ChatMessage message) {
			synchronized (_lock) {
				_pendingMessages.add(message);
			}
		}

		public List<ChatMessage> consumePendingMessages() {
			synchronized (_lock) {
				List<ChatMessage> result = new ArrayList<>(_pendingMessages);
				_pendingMessages.clear();
				return result;
			}
		}

		public void completeSuccessfully() {
			synchronized (_lock) {
				_processing = false;
				_completed = true;
			}
		}

		public void completeWithError(Throwable error) {
			synchronized (_lock) {
				_processing = false;
				_completed = true;
				_error = error;
			}
		}

		public void cancel() {
			synchronized (_lock) {
				if (_backgroundTask != null) {
					_backgroundTask.cancel(true);
				}
				if (_pollingTask != null) {
					_pollingTask.cancel(false);
				}
			}
		}

		public Throwable getError() {
			return _error;
		}

		public void setCurrentTaskId(String taskId) {
			_currentTaskId = taskId;
		}

		public String getCurrentTaskId() {
			return _currentTaskId;
		}

		public void setToolCallTracker(ToolCallTracker tracker) {
			_toolCallTracker = tracker;
			_lastSeenToolCallCount.set(0);
		}

		public ToolCallTracker getToolCallTracker() {
			return _toolCallTracker;
		}

		public int getLastSeenToolCallCount() {
			return _lastSeenToolCallCount.get();
		}

		public void setLastSeenToolCallCount(int count) {
			_lastSeenToolCallCount.set(count);
		}
	}

	/**
	 * Command that sends a chat message.
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
					ChatMessage message = new ChatMessage(userName, trimmedText);
					chatControl._chat.addMessage(message);

					// Show user message immediately
					chatControl.requestRepaint();

					// Single entrypoint: all messages go through OrchestratorAgent (step 7)
					String operationType = "orchestrator";

					// Initialize processing state (on control and in tracker so it survives navigation)
					chatControl._processingState = new AIProcessingState();
					chatControl._processingState.startProcessing();
					chatControl._taskTracker.setCurrentProcessingState(chatControl._processingState);
					chatControl._taskTracker.setRepaintRequester(() -> chatControl.requestRepaint());
					// #region agent log
					debugLog("D", "ChatControl.SendMessage", "SendMessage started processing", "operationType=" + operationType);
					// #endregion

					// Submit background task
					AIBackgroundTask task = chatControl.new AIBackgroundTask(trimmedText, operationType);
					Future<?> future = BACKGROUND_EXECUTOR.submit(task);
					chatControl._processingState._backgroundTask = future;

					// Schedule UI executor polling (uses tracker so updates apply after navigation)
					try {
						MainLayout mainLayout = commandContext.getLayoutContext().getMainLayout();
						ScheduledExecutorService uiExecutor = mainLayout.getWindowScope().getUIExecutor();
						TaskProgressTracker tracker = chatControl._taskTracker;

						ScheduledFuture<?> pollingTask = uiExecutor.scheduleWithFixedDelay(
							new Runnable() {
								@Override
								public void run() {
									AIProcessingState state =
										(AIProcessingState) tracker.getCurrentProcessingState();
									if (state == null) {
										return;
									}
									boolean hasUpdates =
										applyPollUpdates(state, tracker.getChat(), tracker);
									if (hasUpdates) {
										tracker.requestRepaintIfNeeded();
									}
								}
							},
							POLL_INTERVAL_MS,
							POLL_INTERVAL_MS,
							TimeUnit.MILLISECONDS
						);

						chatControl._processingState._pollingTask = pollingTask;

					} catch (Exception ex) {
						Logger.error("Failed to schedule UI polling: " + ex.getMessage(), ex, ChatControl.class);
						// Fallback: cancel background task
						future.cancel(true);
						chatControl._processingState = null;
						chatControl._chat.addMessage(new ChatMessage("System", "Failed to start AI processing"));
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

	/**
	 * Command that polls for AI processing updates using TopLogic's UI Executor.
	 */
	private static class PollAIProgress extends ControlCommand {

		static final ControlCommand INSTANCE = new PollAIProgress();

		static final String COMMAND_ID = "pollAIProgress";

		PollAIProgress() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			ChatControl chatControl = (ChatControl) control;
			chatControl.pollForUpdates();
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.text("chat.pollAIProgress");
		}
	}

}
