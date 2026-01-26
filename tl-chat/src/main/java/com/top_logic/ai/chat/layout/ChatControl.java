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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.agents.ModelExplorer;
import com.top_logic.ai.service.agents.TLScriptAgent;
import com.top_logic.ai.service.agents.UMLSpecificationAgent;
import com.top_logic.ai.service.scripting.Converters;
import com.top_logic.ai.service.scripting.OpenAIScriptFunctions;
import com.top_logic.ai.service.tools.ToolCallTracker;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.help.ModelExplorerTool;
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

	/** List to hold notifier messages for progress updates */
	private final List<ChatNotifier> _notifiers = new ArrayList<>();

	/** Shared state for background processing (null when not processing) */
	private AIProcessingState _processingState = null;

	/**
	 * Creates a new {@link ChatControl}.
	 */
	public ChatControl(Chat chat) {
		super(COMMANDS);
		_chat = chat;
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
		// Cancel any ongoing AI processing
		if (_processingState != null) {
			_processingState.cancel();
			_processingState = null;
		}
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
			// Render regular chat messages
			for (ChatMessage msg : _chat.getMessages()) {
				writeMessage(context, out, msg);
			}

			// Render notifier messages
			for (ChatNotifier notifier : _notifiers) {
				writeNotifierMessage(out, notifier);
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

	private void writeNotifierMessage(TagWriter out, ChatNotifier notifier) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-notifier");
		out.endBeginTag();
		{
			// Icon and source
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-notifier-source");
			out.endBeginTag();
			out.writeText(notifier.getIcon() + " " + notifier.getSource());
			out.endTag(SPAN);

			out.writeText(" ");

			// Timestamp
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-notifier-time");
			out.endBeginTag();
			out.writeText("(" + TIME_FORMAT.format(notifier.getTimestamp().atZone(ZoneId.systemDefault())) + ")");
			out.endTag(SPAN);

			out.writeText(": ");

			// Message text with type-specific styling
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, getNotifierTextClass(notifier.getType()));
			out.endBeginTag();
			out.writeText(notifier.getMessage());
			out.endTag(SPAN);
		}
		out.endTag(DIV);
	}

	private String getNotifierTextClass(ChatNotifier.NotificationType type) {
		switch (type) {
			case INFO:
				return "chat-notifier-text-info";
			case PROGRESS:
				return "chat-notifier-text-progress";
			case SUCCESS:
				return "chat-notifier-text-success";
			case ERROR:
				return "chat-notifier-text-error";
			default:
				return "chat-notifier-text-info";
		}
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
	 * Executes a regular chat request in the background thread with progress tracking.
	 */
	private void executeRegularChat(String userMessage) throws Exception {
		_processingState.addProgressNotifier(
			new ChatNotifier("Processing chat request...", ChatNotifier.NotificationType.PROGRESS, "AI"));

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

		_processingState.addProgressNotifier(
			new ChatNotifier("Calling OpenAI API...", ChatNotifier.NotificationType.PROGRESS, "AI"));

		// Call OpenAI API (this is the blocking operation)
		String aiResponse = OpenAIScriptFunctions.chat(messages, null, null, null);

		// Add result to pending messages
		_processingState.addPendingMessage(new ChatMessage("AI", aiResponse));
	}

	/**
	 * Executes a model creation command in the background thread with progress tracking.
	 */
	private void executeModelCreation(String command) throws Exception {
		String requirements = extractBusinessRequirements(command);

		_processingState.addProgressNotifier(
			new ChatNotifier("Starting UML specification workflow...", ChatNotifier.NotificationType.PROGRESS, "Workflow"));

		Logger.info("ChatControl: Starting workflow execution in thread: " + Thread.currentThread().getName(),
			ChatControl.class);

		// Execute the workflow
		String result = UMLSpecificationAgent.execute(requirements);

		_processingState.addPendingMessage(new ChatMessage("Agent", result));
	}

	/**
	 * Executes a model exploration command in the background thread with progress tracking.
	 */
	private void executeModelExploration(String command) throws Exception {
		String explorationQuestion = extractExplorationQuestion(command);

		_processingState.addProgressNotifier(
			new ChatNotifier("Initializing model exploration agent...", ChatNotifier.NotificationType.PROGRESS, "Agent"));

		// Set up tool tracking
		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);

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

			_processingState.addProgressNotifier(
				new ChatNotifier("Building ModelExplorer agent with tools...", ChatNotifier.NotificationType.PROGRESS, "Agent"));

			// Build the ModelExplorer agent
			ModelExplorer explorer = AgenticServices.agentBuilder(ModelExplorer.class)
				.chatModel(explorationModel)
				.tools(browserTool, instanceTool, analysisTool, searchTool, annotationTool, definitionTool)
				.build();

			_processingState.addProgressNotifier(
				new ChatNotifier("Executing exploration...", ChatNotifier.NotificationType.PROGRESS, "ModelExplorer"));

			// Execute the exploration
			String result = explorer.explore(explorationQuestion);

			// Add tool calls as a system message if there were any
			if (!tracker.getToolCalls().isEmpty()) {
				String toolCallsMessage = formatToolCalls(tracker);
				_processingState.addPendingMessage(new ChatMessage("System", toolCallsMessage));
			}

			// Add final result
			_processingState.addPendingMessage(new ChatMessage("Agent", result));

		} finally {
			// Always clear the tracker
			ToolCallTracking.setTracker(null);
		}
	}

	/**
	 * Executes a TLScript reference command in the background thread with progress tracking.
	 */
	private void executeTLScript(String command) throws Exception {
		String question = extractTLScriptQuestion(command);

		_processingState.addProgressNotifier(
			new ChatNotifier("Initializing TLScript agent...", ChatNotifier.NotificationType.PROGRESS, "Agent"));

		// Set up tool tracking
		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);

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

			// Create TLScript reference tool and ModelExplorer tool
			TLScriptReferenceTool tlscriptTool = new TLScriptReferenceTool();
			ModelExplorerTool modelExplorerTool = new ModelExplorerTool();

			_processingState.addProgressNotifier(
				new ChatNotifier("Building TLScriptAgent with tools...", ChatNotifier.NotificationType.PROGRESS, "Agent"));

			// Build the TLScriptAgent
			TLScriptAgent agent = AgenticServices.agentBuilder(TLScriptAgent.class)
				.chatModel(tlscriptModel)
				.tools(tlscriptTool, modelExplorerTool)
				.build();

			_processingState.addProgressNotifier(
				new ChatNotifier("Executing TLScript query...", ChatNotifier.NotificationType.PROGRESS, "TLScriptAgent"));

			// Execute the agent
			String result = agent.answer(question);

			// Add tool calls as a system message if there were any
			if (!tracker.getToolCalls().isEmpty()) {
				String toolCallsMessage = formatToolCalls(tracker);
				_processingState.addPendingMessage(new ChatMessage("System", toolCallsMessage));
			}

			// Add final result
			_processingState.addPendingMessage(new ChatMessage("Agent", result));

		} finally {
			// Always clear the tracker
			ToolCallTracking.setTracker(null);
		}
	}

	/**
	 * Polls for AI processing updates and updates the UI.
	 * Called periodically by the UI executor.
	 */
	private void pollForUpdates() {
		AIProcessingState state = _processingState;

		if (state == null) {
			return;
		}

		boolean hasUpdates = false;

		// Check for new progress notifiers
		List<ChatNotifier> newNotifiers = state.consumeProgressNotifiers();
		if (!newNotifiers.isEmpty()) {
			_notifiers.clear(); // Replace old notifiers
			_notifiers.addAll(newNotifiers);
			hasUpdates = true;
		}

		// Check for new messages
		List<ChatMessage> newMessages = state.consumePendingMessages();
		if (!newMessages.isEmpty()) {
			for (ChatMessage msg : newMessages) {
				_chat.addMessage(msg);
			}
			hasUpdates = true;
		}

		// Check for completion
		if (!state.isProcessing()) {
			// Cancel polling task
			if (state._pollingTask != null) {
				state._pollingTask.cancel(false);
				state._pollingTask = null;
			}

			// Clear notifiers on completion
			_notifiers.clear();

			// Handle errors
			Throwable error = state.getError();
			if (error != null) {
				String errorMsg = "Error: " + error.getMessage();
				if (error instanceof InterruptedException) {
					errorMsg = "Processing was cancelled";
				}
				_chat.addMessage(new ChatMessage("AI", errorMsg));
				hasUpdates = true;
			}

			// Clean up state
			_processingState = null;
		}

		if (hasUpdates) {
			requestRepaint();
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
			// Run in system interaction context for AIService access
			try {
				ThreadContextManager.inSystemInteraction(ChatControl.class, () -> {
					try {
						switch (_operationType) {
							case "chat":
								executeRegularChat(_userMessage);
								break;
							case "model-creation":
								executeModelCreation(_userMessage);
								break;
							case "explore-model":
								executeModelExploration(_userMessage);
								break;
							case "tlscript":
								executeTLScript(_userMessage);
								break;
							default:
								throw new RuntimeException("Unknown operation type: " + _operationType);
						}

						_processingState.completeSuccessfully();
						return null; // ComputationEx2 requires a return value

					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						_processingState.completeWithError(ex);
						return null;
					} catch (Exception ex) {
						Logger.error("AI processing failed: " + ex.getMessage(), ex, ChatControl.class);
						_processingState.completeWithError(ex);
						return null;
					}
				});
			} catch (Exception ex) {
				Logger.error("Failed to run background task: " + ex.getMessage(), ex, ChatControl.class);
				_processingState.completeWithError(ex);
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

		// Progress tracking with agent and tool information
		private final List<ChatNotifier> _progressNotifiers = new ArrayList<>();

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
				_progressNotifiers.clear();
			}
		}

		public void addProgressNotifier(ChatNotifier notifier) {
			synchronized (_lock) {
				_progressNotifiers.add(notifier);
			}
		}

		public List<ChatNotifier> consumeProgressNotifiers() {
			synchronized (_lock) {
				List<ChatNotifier> result = new ArrayList<>(_progressNotifiers);
				_progressNotifiers.clear();
				return result;
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

					// Determine operation type
					String operationType;
					if (chatControl.isModelCreationCommand(trimmedText)) {
						operationType = "model-creation";
					} else if (chatControl.isModelExplorationCommand(trimmedText)) {
						operationType = "explore-model";
					} else if (chatControl.isTLScriptCommand(trimmedText)) {
						operationType = "tlscript";
					} else {
						operationType = "chat";
					}

					// Initialize processing state
					chatControl._processingState = new AIProcessingState();
					chatControl._processingState.startProcessing();

					// Submit background task
					AIBackgroundTask task = chatControl.new AIBackgroundTask(trimmedText, operationType);
					Future<?> future = BACKGROUND_EXECUTOR.submit(task);
					chatControl._processingState._backgroundTask = future;

					// Schedule UI executor polling
					try {
						MainLayout mainLayout = commandContext.getLayoutContext().getMainLayout();
						ScheduledExecutorService uiExecutor = mainLayout.getWindowScope().getUIExecutor();

						ScheduledFuture<?> pollingTask = uiExecutor.scheduleWithFixedDelay(
							new Runnable() {
								@Override
								public void run() {
									// Poll for updates in UI thread
									chatControl.pollForUpdates();
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
