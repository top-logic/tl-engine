/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.layout.chat;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.agents.ModelExplorer;
import com.top_logic.ai.service.agents.TLScriptAgent;
import com.top_logic.ai.service.agents.UMLSpecificationAgent;
import com.top_logic.ai.service.scripting.Converters;
import com.top_logic.ai.service.scripting.OpenAIScriptFunctions;
import com.top_logic.ai.service.tools.model.AnnotationDefinitionExplorerTool;
import com.top_logic.ai.service.tools.model.InstanceBrowserTool;
import com.top_logic.ai.service.tools.model.ModelAnnotationBrowserTool;
import com.top_logic.ai.service.tools.model.ModelBrowserTool;
import com.top_logic.ai.service.tools.model.ModelSearchTool;
import com.top_logic.ai.service.tools.model.TypeAnalysisTool;
import com.top_logic.ai.service.tools.help.ModelExplorerTool;
import com.top_logic.ai.service.tools.help.TLScriptReferenceTool;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
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
		createCommandMap(SendMessage.INSTANCE, AIResponse.INSTANCE);

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final Chat _chat;

	private boolean _scheduleAIResponse = false;

	/** List to hold notifier messages for progress updates */
	private final List<ChatNotifier> _notifiers = new ArrayList<>();

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
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			writeMessages(context, out);
			writeInputSection(out);
		}
		out.endTag(DIV);

		if (_scheduleAIResponse) {
			_scheduleAIResponse = false;
			out.beginScript();
			out.append("setTimeout(function() {");
			out.append("services.ajax.execute('dispatchControlCommand', {");
			out.append("controlID: '");
			out.append(getID());
			out.append("',");
			out.append("controlCommand: '");
			out.append(AIResponse.COMMAND_ID);
			out.append("',");
			out.append("userMessage: ''"); // Will be populated from command
			out.append("});");
			out.append("}, 100);"); // Short delay to allow UI update
			out.endScript();
		}
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
		return text != null && text.trim().startsWith("/model-creation");
	}

	/**
	 * Extracts business requirements from a model creation command.
	 */
	private String extractBusinessRequirements(String command) {
		return command.trim().substring("/model-creation".length()).trim();
	}

	/**
	 * Checks if the given text is a model exploration command.
	 */
	private boolean isModelExplorationCommand(String text) {
		return text != null && text.trim().startsWith("/explore-model");
	}

	/**
	 * Extracts the exploration question from a model exploration command.
	 */
	private String extractExplorationQuestion(String command) {
		return command.trim().substring("/explore-model".length()).trim();
	}

	/**
	 * Checks if the given text is a TLScript reference command.
	 */
	private boolean isTLScriptCommand(String text) {
		return text != null && text.trim().startsWith("/tlscript");
	}

	/**
	 * Extracts the question from a TLScript reference command.
	 */
	private String extractTLScriptQuestion(String command) {
		return command.trim().substring("/tlscript".length()).trim();
	}

	/**
	 * Handles a model creation command synchronously.
	 */
	private void handleModelCreationCommand(String command, String userName) {
		// Clear any previous notifiers
		_notifiers.clear();

		String businessRequirements = extractBusinessRequirements(command);

		try {
			Logger.info("ChatControl: Starting workflow execution in thread: " + Thread.currentThread().getName(),
				ChatControl.class);
			// Execute the workflow synchronously
			String result = UMLSpecificationAgent.execute(businessRequirements);
			// Add final result as Agent message
			_chat.addMessage(new ChatMessage("Agent", result));
			requestRepaint();

		} catch (Exception e) {
			Logger.error(
				"ChatControl: Workflow execution failed: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
				ChatControl.class);
			// Handle workflow failure
			_chat.addMessage(new ChatMessage("Agent", "❌ Workflow execution failed: " + e.getMessage()));
			requestRepaint();
		}
	}

	/**
	 * Handles a model exploration command synchronously.
	 */
	private void handleModelExplorationCommand(String command, String userName) {
		// Clear any previous notifiers
		_notifiers.clear();

		String explorationQuestion = extractExplorationQuestion(command);

		try {
			Logger.info("ChatControl: Starting model exploration in thread: " + Thread.currentThread().getName(),
				ChatControl.class);
			// Get the model from AIService (uses environment variable configuration)
			AIService aiService = AIService.getInstance();
			if (aiService == null) {
				throw new RuntimeException("AIService is not available. Please ensure the service is started.");
			}

			// Use mistral-large-latest model (configured in tl-ai-service)
			ChatModel explorationModel = aiService.getChatModel("mistral-large-latest");

			// Create internal tool instances (no HTTP overhead)
			ModelBrowserTool browserTool = new ModelBrowserTool();
			InstanceBrowserTool instanceTool = new InstanceBrowserTool();
			TypeAnalysisTool analysisTool = new TypeAnalysisTool();
			ModelSearchTool searchTool = new ModelSearchTool();
			ModelAnnotationBrowserTool annotationTool = new ModelAnnotationBrowserTool();
			AnnotationDefinitionExplorerTool definitionTool = new AnnotationDefinitionExplorerTool();

			// Build the ModelExplorer agent with direct tools
			ModelExplorer explorer = AgenticServices.agentBuilder(ModelExplorer.class)
				.chatModel(explorationModel)
				.tools(browserTool, instanceTool, analysisTool, searchTool, annotationTool, definitionTool)
				.build();

			// Execute the exploration
			String result = explorer.explore(explorationQuestion);

			// Add final result as Agent message
			_chat.addMessage(new ChatMessage("Agent", result));
			requestRepaint();

		} catch (Exception e) {
			Logger.error(
				"ChatControl: Model exploration failed: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
				ChatControl.class);
			// Handle exploration failure
			_chat.addMessage(new ChatMessage("Agent", "❌ Model exploration failed: " + e.getMessage()));
			requestRepaint();
		}
	}

	/**
	 * Handles a TLScript reference command synchronously.
	 */
	private void handleTLScriptCommand(String command, String userName) {
		// Clear any previous notifiers
		_notifiers.clear();

		String question = extractTLScriptQuestion(command);

		try {
			Logger.info("ChatControl: Starting TLScript reference in thread: " + Thread.currentThread().getName(),
				ChatControl.class);
			// Get the model from AIService (uses environment variable configuration)
			AIService aiService = AIService.getInstance();
			if (aiService == null) {
				throw new RuntimeException("AIService is not available. Please ensure the service is started.");
			}

			// Use mistral-large-latest model (configured in tl-ai-service)
			ChatModel tlscriptModel = aiService.getChatModel("mistral-large-latest");

			// Create TLScript reference tool and ModelExplorer tool
			TLScriptReferenceTool tlscriptTool = new TLScriptReferenceTool();
			ModelExplorerTool modelExplorerTool = new ModelExplorerTool();

			// Build the TLScriptAgent with both tools
			TLScriptAgent agent = AgenticServices.agentBuilder(TLScriptAgent.class)
				.chatModel(tlscriptModel)
				.tools(tlscriptTool, modelExplorerTool)
				.build();

			// Execute the agent
			String result = agent.answer(question);

			// Add final result as Agent message
			_chat.addMessage(new ChatMessage("Agent", result));
			requestRepaint();

		} catch (Exception e) {
			Logger.error("ChatControl: TLScript reference failed: " + e.getClass().getSimpleName() + ": " + e.getMessage(), ChatControl.class);
			// Handle failure
			_chat.addMessage(new ChatMessage("Agent", "❌ TLScript reference failed: " + e.getMessage()));
			requestRepaint();
		}
	}

	/**
	 * Adds a notifier message and triggers UI repaint.
	 */
	private void addNotifierMessage(ChatNotifier notifier) {
		_notifiers.add(notifier);
		requestRepaint();
	}

	/**
	 * Handles AI response processing for user messages.
	 */
	private void handleAIResponse(String userMessage) {
		// Add processing notifier
		addNotifierMessage(new ChatNotifier("Processing your request...",
			ChatNotifier.NotificationType.PROGRESS, "AI"));

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

		try {
			// Call OpenAI API
			String aiResponse = OpenAIScriptFunctions.chat(messages, null, null, null);

			// Clear processing notifier and add AI response
			_notifiers.clear();
			_chat.addMessage(new ChatMessage("AI", aiResponse));
		} catch (Exception e) {
			// Clear processing notifier and add error message
			_notifiers.clear();
			_chat.addMessage(new ChatMessage("AI", "Sorry, I encountered an error: " + e.getMessage()));
		}
		requestRepaint();
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

			if (text != null && !text.trim().isEmpty()) {
				String userName = getCurrentUserName();
				ChatMessage message = new ChatMessage(userName, text.trim());
				chatControl._chat.addMessage(message);

				// Show user message immediately
				chatControl.requestRepaint();

				// Check if this is a model creation command
				if (chatControl.isModelCreationCommand(text.trim())) {
					// Handle model creation workflow
					chatControl.handleModelCreationCommand(text.trim(), userName);
				} else if (chatControl.isModelExplorationCommand(text.trim())) {
					// Handle model exploration workflow
					chatControl.handleModelExplorationCommand(text.trim(), userName);
				} else if (chatControl.isTLScriptCommand(text.trim())) {
					// Handle TLScript reference workflow
					chatControl.handleTLScriptCommand(text.trim(), userName);
				} else {
					// Regular chat - schedule AI response via JavaScript
					chatControl._scheduleAIResponse = true;
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
	 * Command that sends an AI response.
	 */
	private static class AIResponse extends ControlCommand {

		static final ControlCommand INSTANCE = new AIResponse();

		static final String COMMAND_ID = "aiResponse";

		AIResponse() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {
			ChatControl chatControl = (ChatControl) control;
			// Get the last user message from chat since JavaScript can't easily pass it
			String lastUserMessage = getLastUserMessage(chatControl);

			chatControl.handleAIResponse(lastUserMessage);

			return HandlerResult.DEFAULT_RESULT;
		}

		private String getLastUserMessage(ChatControl chatControl) {
			List<ChatMessage> messages = chatControl._chat.getMessages();
			for (int i = messages.size() - 1; i >= 0; i--) {
				ChatMessage msg = messages.get(i);
				if (!"AI".equals(msg.getUser())) {
					return msg.getText();
				}
			}
			return "";
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.text("chat.aiResponse");
		}
	}

}
