/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;

/**
 * Chat control that renders messages and an input section.
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
		super.internalDetach();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		String contextPath = context.getContextPath();
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.writeAttribute("data-tl-chat", "v1");
		// Sub-session registry key so the JSON API can bind requests to this application window.
		out.writeAttribute("data-window-id", _taskTracker.getWindowId());
		out.writeAttribute("data-cancel-url", contextPath + "/chat/cancel");
		out.writeAttribute("data-respond-url", contextPath + "/chat/respond");
		out.writeAttribute("data-approval-mode-url", contextPath + "/chat/approval-mode");
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
				writeTaskPanel(out);
				writeInputSection(out);
			}
			out.endTag(DIV);

			// History panel (right side, collapsible)
			writeHistoryPanel(out);
		}
		out.endTag(DIV);

		// Load chat client via inline script so it runs when fragment is AJAX-injected
		out.beginScript();
		out.append("(function(){var s=document.createElement('script');s.src='");
		out.append(contextPath);
		out.append("/script/chat-client.js';s.type='text/javascript';(document.head||document.documentElement).appendChild(s);})();");
		out.endScript();
	}

	private void writeHistoryPanel(TagWriter out) {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-history-panel");
		out.endBeginTag();
		{
			// Sidebar header with toggle and new chat
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "chat-sidebar-header");
			out.endBeginTag();
			{
				// Toggle button (hamburger -> X animation via CSS)
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

	private void writeToggleBar(TagWriter out) {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "chat-toggle-bar");
		out.endBeginTag();
		out.endTag(SPAN);
	}

	private void writeWelcomeScreen(TagWriter out) {
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

	private void writePromptChip(TagWriter out, String text) {
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
			int messageIndex = 0;
			for (ChatMessage msg : _chat.getMessages()) {
				writeMessage(context, out, msg, messageIndex);
				messageIndex++;
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

	private void writeTaskPanel(TagWriter out) {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "chat-task-panel");
		out.endBeginTag();
		out.endTag(DIV);
	}

	private void writeInputSection(TagWriter out) {
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
				out.writeText("\u2713"); // checkmark
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

	private void writePaperclipSvg(TagWriter out) {
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
					ChatProcessor.SendResult result = ChatProcessor.sendMessage(
						chatControl._taskTracker, TLContextManager.getSubSession(), userName, trimmedText, null);
					if (result.getTaskId() != null) {
						chatControl._taskTracker.getAnyActiveProcessingState();
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
