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

import com.top_logic.ai.service.agents.ModelExplorer;
import com.top_logic.ai.service.agents.UMLSpecificationAgent;
import com.top_logic.ai.service.scripting.OpenAIScriptFunctions;
import com.top_logic.ai.service.tools.AnnotationDefinitionExplorerTool;
import com.top_logic.ai.service.tools.InstanceBrowserTool;
import com.top_logic.ai.service.tools.ModelAnnotationBrowserTool;
import com.top_logic.ai.service.tools.ModelBrowserTool;
import com.top_logic.ai.service.tools.ModelSearchTool;
import com.top_logic.ai.service.tools.TypeAnalysisTool;
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
import dev.langchain4j.model.mistralai.MistralAiChatModel;

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
			writeMessages(out);
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

	private void writeMessages(TagWriter out) throws IOException {
		String messagesId = getID() + "-messages";
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-messages");
		out.writeAttribute(ID_ATTR, messagesId);
		out.writeAttribute(STYLE_ATTR,
			"height: 400px; overflow-y: auto; border: 1px solid #ccc; padding: 10px; margin-bottom: 10px; background-color: #f9f9f9;");
		out.endBeginTag();
		{
			// Render regular chat messages
			for (ChatMessage msg : _chat.getMessages()) {
				writeMessage(out, msg);
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

	private void writeMessage(TagWriter out, ChatMessage msg) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-message");
		out.writeAttribute(STYLE_ATTR, "margin-bottom: 8px;");
		out.endBeginTag();
		{
			// User name
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-user");
			out.writeAttribute(STYLE_ATTR, "font-weight: bold; color: #0066cc;");
			out.endBeginTag();
			out.writeText(msg.getUser());
			out.endTag(SPAN);

			out.writeText(" ");

			// Timestamp
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-time");
			out.writeAttribute(STYLE_ATTR, "color: #666; font-size: 0.9em;");
			out.endBeginTag();
			out.writeText("(" + TIME_FORMAT.format(msg.getTimestamp().atZone(ZoneId.systemDefault())) + ")");
			out.endTag(SPAN);

			out.writeText(": ");

			// Message text
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "chat-text");
			out.endBeginTag();
			out.writeText(msg.getText());
			out.endTag(SPAN);
		}
		out.endTag(DIV);
	}

	private void writeNotifierMessage(TagWriter out, ChatNotifier notifier) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-notifier");
		out.writeAttribute(STYLE_ATTR,
			"margin-bottom: 4px; padding: 6px 10px; background-color: #f0f0f0; border-radius: 4px; font-size: 0.9em; color: #666; border-left: 3px solid #ccc;");
		out.endBeginTag();
		{
			// Icon and source
			out.beginBeginTag(SPAN);
			out.writeAttribute(STYLE_ATTR, "font-weight: normal; color: #888;");
			out.endBeginTag();
			out.writeText(notifier.getIcon() + " " + notifier.getSource());
			out.endTag(SPAN);

			out.writeText(" ");

			// Timestamp
			out.beginBeginTag(SPAN);
			out.writeAttribute(STYLE_ATTR, "color: #999; font-size: 0.8em;");
			out.endBeginTag();
			out.writeText("(" + TIME_FORMAT.format(notifier.getTimestamp().atZone(ZoneId.systemDefault())) + ")");
			out.endTag(SPAN);

			out.writeText(": ");

			// Message text with type-specific styling
			out.beginBeginTag(SPAN);
			out.writeAttribute(STYLE_ATTR, getNotifierTextStyle(notifier.getType()));
			out.endBeginTag();
			out.writeText(notifier.getMessage());
			out.endTag(SPAN);
		}
		out.endTag(DIV);
	}

	private String getNotifierTextStyle(ChatNotifier.NotificationType type) {
		switch (type) {
			case INFO:
				return "color: #555;";
			case PROGRESS:
				return "color: #2980b9; font-style: italic;";
			case SUCCESS:
				return "color: #27ae60; font-weight: 500;";
			case ERROR:
				return "color: #e74c3c; font-weight: 500;";
			default:
				return "color: #666;";
		}
	}

	private void writeInputSection(TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-input-section");
		out.writeAttribute(STYLE_ATTR, "display: flex; gap: 10px;");
		out.endBeginTag();
		{
			// Text input
			out.beginBeginTag(INPUT);
			out.writeAttribute(TYPE_ATTR, "text");
			out.writeAttribute(ID_ATTR, getInputId());
			out.writeAttribute(NAME_ATTR, "message");
			out.writeAttribute(PLACEHOLDER_ATTR, "Type a message...");
			out.writeAttribute(STYLE_ATTR, "flex: 1; padding: 8px;");
			out.writeAttribute(ONKEYPRESS_ATTR,
				"if(event.keyCode == 13 || event.key == 'Enter') { " + getSendCommandJS() + " return false; }");
			out.endEmptyTag();

			// Send button
			out.beginBeginTag(BUTTON);
			out.writeAttribute(ID_ATTR, getButtonId());
			out.writeAttribute(TYPE_ATTR, "button");
			out.writeAttribute(STYLE_ATTR, "padding: 8px 20px; white-space: nowrap;");
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
	 * Handles a model creation command synchronously.
	 */
	private void handleModelCreationCommand(String command, String userName) {
		// Clear any previous notifiers
		_notifiers.clear();

		String businessRequirements = extractBusinessRequirements(command);

		try {
			System.out.println("ChatControl: Starting workflow execution in thread: " + Thread.currentThread().getName());

			// Execute the workflow synchronously
			String result = UMLSpecificationAgent.execute(businessRequirements);

			// Add final result as Agent message
			_chat.addMessage(new ChatMessage("Agent", result));
			requestRepaint();

		} catch (Exception e) {
			System.err.println("ChatControl: Workflow execution failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace();

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
			System.out.println("ChatControl: Starting model exploration in thread: " + Thread.currentThread().getName());

			// Create the ModelExplorer agent
			ChatModel explorationModel = MistralAiChatModel.builder()
				.apiKey("IeHf1m5VQVCx0ZK3oQUYJC1hyOXVFjGn")
				.baseUrl("https://api.mistral.ai/v1")
				.modelName("mistral-large-latest")
				.logRequests(false)
				.logResponses(true)
				.build();

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
			System.err.println("ChatControl: Model exploration failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace();

			// Handle exploration failure
			_chat.addMessage(new ChatMessage("Agent", "❌ Model exploration failed: " + e.getMessage()));
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
			String aiResponse = OpenAIScriptFunctions.chat(messages, null, null);

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
