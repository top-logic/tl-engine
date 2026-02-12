/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.io.IOException;
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

import com.top_logic.ai.service.agents.OrchestratorAgent;
import com.top_logic.ai.service.scripting.Converters;
import com.top_logic.ai.service.tools.ToolCallTracker;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.TraceEntry;
import com.top_logic.ai.service.tools.util.JsonPrettyPrint;
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
			// Render entries in order: user message → task trace → agent response
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

	/** Formats duration in seconds for the overall task trace. */
	private String formatDuration(long ms) {
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

	private void writeInputSection(TagWriter out) throws IOException {
		String controlId = getID();
		String inputId = getInputId();
		// Only use TL path when script has not inited (data-tl-chat-inited); else script handles send → incremental
		String sendOnClick = "var root=document.getElementById('" + controlId + "');"
			+ "if(root&&root.getAttribute('data-tl-chat-inited')==='true')return;"
			+ "var inp=document.getElementById('" + inputId + "');var t=inp?inp.value.trim():'';"
			+ "if(t){if(typeof services!=='undefined'&&services.ajax){services.ajax.execute('dispatchControlCommand',"
			+ "{controlCommand:'sendMessage',controlID:'" + controlId + "',text:t});}inp.value='';}";
		String sendOnKeypress = "if(event.key==='Enter'){event.preventDefault();"
			+ "var root=document.getElementById('" + controlId + "');"
			+ "if(root&&root.getAttribute('data-tl-chat-inited')==='true')return;"
			+ "var inp=document.getElementById('" + inputId + "');var t=inp?inp.value.trim():'';"
			+ "if(t){if(typeof services!=='undefined'&&services.ajax){services.ajax.execute('dispatchControlCommand',"
			+ "{controlCommand:'sendMessage',controlID:'" + controlId + "',text:t});}inp.value='';}}";

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-input-section");
		out.endBeginTag();
		{
			out.beginBeginTag(INPUT);
			out.writeAttribute(TYPE_ATTR, "text");
			out.writeAttribute(ID_ATTR, inputId);
			out.writeAttribute(NAME_ATTR, "message");
			out.writeAttribute(CLASS_ATTR, "chat-input");
			out.writeAttribute(PLACEHOLDER_ATTR, "Type a message...");
			out.writeAttribute("onkeypress", sendOnKeypress);
			out.endEmptyTag();

			out.beginBeginTag(BUTTON);
			out.writeAttribute(ID_ATTR, getButtonId());
			out.writeAttribute(TYPE_ATTR, "button");
			out.writeAttribute(CLASS_ATTR, "chat-button");
			out.writeAttribute(ONCLICK_ATTR, sendOnClick);
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
	 * Executes user message through OrchestratorAgent (single chat entrypoint).
	 * Sets trace so AgentCall and ToolCall events are visible in the UI timeline.
	 * Uses state from tracker; taskId must already be set on state (by sendMessage / ChatSendService).
	 */
	private static void executeOrchestrator(TaskProgressTracker taskTracker, AIProcessingState state,
			String userMessage) throws Exception {
		if (state == null) {
			return;
		}
		String taskId = state.getCurrentTaskId();
		if (taskId == null) {
			return;
		}

		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);
		state.setToolCallTracker(tracker);

		try {
			Logger.info("ChatControl: Starting Orchestrator in thread: " + Thread.currentThread().getName(),
				ChatControl.class);

			String result = OrchestratorAgent.chat(userMessage);

			state.addPendingMessage(new ChatMessage("Agent", result));
			taskTracker.completeTask(taskId);

		} catch (Exception ex) {
			taskTracker.failTask(taskId, ex.getMessage());
			throw ex;
		} finally {
			ToolCallTracking.setTracker(null);
			state.setToolCallTracker(null);
		}
	}

	/**
	 * Applies poll updates for the given state to chat and taskTracker. Clears tracker and
	 * cancels polling when processing is done. Used by both the control and the scheduled
	 * polling runnable (so updates continue after navigation away).
	 *
	 * <p>
	 * Public so {@link com.top_logic.ai.chat.api.ChatServlet} and
	 * {@link com.top_logic.ai.chat.api.ChatSSEHandler} can call it to sync timeline events
	 * from ToolCallTracker to TaskProgressTracker before returning updates.
	 * </p>
	 */
	public static boolean applyPollUpdates(AIProcessingState state, Chat chat,
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
	private static class AIBackgroundTask implements Runnable {
		private final TaskProgressTracker _taskTracker;
		private final String _userMessage;

		AIBackgroundTask(TaskProgressTracker taskTracker, String userMessage) {
			_taskTracker = taskTracker;
			_userMessage = userMessage;
		}

		@Override
		public void run() {
			AIProcessingState state = (AIProcessingState) _taskTracker.getCurrentProcessingState();
			if (state == null) {
				return;
			}
			try {
				ThreadContextManager.inSystemInteraction(ChatControl.class, () -> {
					try {
						executeOrchestrator(_taskTracker, state, _userMessage);
						state.completeSuccessfully();
						return null;
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
	 *
	 * <p>
	 * Public so {@link com.top_logic.ai.chat.api.ChatServlet} can access it for syncing
	 * timeline events.
	 * </p>
	 */
	public static class AIProcessingState {
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
	 * Shared send logic: adds user message, starts task, submits background orchestrator, optionally
	 * schedules UI polling. Used by SendMessage (TL UI) and ChatServlet (JSON API).
	 *
	 * @param tracker
	 *        Task progress tracker for this session.
	 * @param userName
	 *        Display name of the user sending the message.
	 * @param userMessage
	 *        The message text.
	 * @param repaintRequester
	 *        Called when polling has updates (e.g. requestRepaint); may be null for API-only.
	 * @param uiExecutor
	 *        Top Logic UI executor for server-side polling; if null, no polling is scheduled (client polls).
	 * @return The send result (task ID, or busy/error); never null.
	 */
	public static SendResult sendMessage(TaskProgressTracker tracker, String userName, String userMessage,
			Runnable repaintRequester, ScheduledExecutorService uiExecutor) {
		if (isProcessing(tracker)) {
			tracker.getChat().addMessage(new ChatMessage("System", "Please wait for the current response to complete."));
			if (repaintRequester != null) {
				repaintRequester.run();
			}
			return SendResult.busy();
		}

		ChatMessage message = new ChatMessage(userName, userMessage);
		tracker.getChat().addMessage(message);

		AIProcessingState state = new AIProcessingState();
		state.startProcessing();
		String taskId = tracker.startTask(randomChatTaskPlaceholder());
		state.setCurrentTaskId(taskId);
		tracker.setCurrentProcessingState(state);
		if (repaintRequester != null) {
			tracker.setRepaintRequester(repaintRequester);
		}

		Future<?> future = BACKGROUND_EXECUTOR.submit(new AIBackgroundTask(tracker, userMessage));
		state._backgroundTask = future;

		if (uiExecutor != null) {
			try {
				ScheduledFuture<?> pollingTask = uiExecutor.scheduleWithFixedDelay(
					() -> {
						AIProcessingState s = (AIProcessingState) tracker.getCurrentProcessingState();
						if (s == null) {
							return;
						}
						boolean hasUpdates = applyPollUpdates(s, tracker.getChat(), tracker);
						if (hasUpdates) {
							tracker.requestRepaintIfNeeded();
						}
					},
					POLL_INTERVAL_MS,
					POLL_INTERVAL_MS,
					TimeUnit.MILLISECONDS);
				state._pollingTask = pollingTask;
			} catch (Exception ex) {
				Logger.error("Failed to schedule UI polling: " + ex.getMessage(), ex, ChatControl.class);
				future.cancel(true);
				tracker.clearCurrentProcessingState();
				return SendResult.error();
			}
		}
		return SendResult.ok(taskId);
	}

	/** Result of a send attempt: success (taskId), busy (already processing), or error. */
	public static final class SendResult {
		private final String _taskId;
		private final boolean _busy;

		private SendResult(String taskId, boolean busy) {
			_taskId = taskId;
			_busy = busy;
		}

		public static SendResult ok(String taskId) {
			return new SendResult(taskId, false);
		}

		public static SendResult busy() {
			return new SendResult(null, true);
		}

		public static SendResult error() {
			return new SendResult(null, false);
		}

		public String getTaskId() {
			return _taskId;
		}

		public boolean isBusy() {
			return _busy;
		}
	}

	/**
	 * Returns whether an AI task is currently in progress for the given tracker.
	 * Used by the JSON API to build the updates response.
	 */
	public static boolean isProcessing(TaskProgressTracker tracker) {
		Object state = tracker.getCurrentProcessingState();
		return state instanceof AIProcessingState s && s.isProcessing();
	}

	/**
	 * Returns the current task ID for the given tracker, or null if none.
	 * Used by the JSON API to build the updates response.
	 */
	public static String getCurrentTaskId(TaskProgressTracker tracker) {
		Object state = tracker.getCurrentProcessingState();
		return state instanceof AIProcessingState s ? s.getCurrentTaskId() : null;
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
					ScheduledExecutorService uiExecutor;
					try {
						MainLayout mainLayout = commandContext.getLayoutContext().getMainLayout();
						uiExecutor = mainLayout.getWindowScope().getUIExecutor();
					} catch (Exception ex) {
						uiExecutor = null;
					}
					SendResult result = sendMessage(chatControl._taskTracker, userName, trimmedText,
						() -> chatControl.requestRepaint(), uiExecutor);
					if (result.getTaskId() != null) {
						chatControl._processingState = (AIProcessingState) chatControl._taskTracker.getCurrentProcessingState();
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
