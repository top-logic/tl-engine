/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.background;

import java.util.List;

import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatMessage;
import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.ai.service.agents.background.context.ChatContext;
import com.top_logic.ai.service.agents.background.input.ChatContextInput;
import com.top_logic.ai.service.agents.background.input.InputAdapter;

/**
 * Input adapter that serializes chat conversation data for background agents.
 *
 * <p>
 * Lives in {@code tl-chat} because it depends on the concrete chat domain types ({@link Chat},
 * {@link ChatMessage}).
 * </p>
 */
public class ChatContextAdapter implements InputAdapter {

	/** Maximum total length of the serialized XML. */
	private static final int MAX_TOTAL_LENGTH = 50_000;

	/** Maximum length for individual text values. */
	private static final int MAX_TEXT_LENGTH = 2000;

	private final ChatContextInput.Content _content;

	/**
	 * Creates a new {@link ChatContextAdapter}.
	 *
	 * @param config
	 *        The chat context input configuration specifying the content scope.
	 */
	public ChatContextAdapter(ChatContextInput config) {
		_content = config.getContent();
	}

	@Override
	public String gatherInput(BackgroundContext context) {
		if (!(context instanceof ChatContext chatCtx)) {
			throw new IllegalArgumentException(
				"ChatContextAdapter requires a ChatContext, got: " + context.getClass().getName());
		}
		if (_content == ChatContextInput.Content.LAST_TURN) {
			return serializeLastTurn(chatCtx.getUserMessage(), chatCtx.getAgentResponse());
		} else {
			Object chat = chatCtx.getChat();
			if (!(chat instanceof Chat)) {
				throw new IllegalArgumentException(
					"Expected Chat but got: " + (chat != null ? chat.getClass().getName() : "null"));
			}
			return serializeFullChat((Chat) chat);
		}
	}

	/**
	 * Serializes the last conversation turn into XML for review.
	 */
	private static String serializeLastTurn(String userMessage, String agentResponse) {
		StringBuilder sb = new StringBuilder();
		sb.append("<review-request>\n");
		sb.append("  <user-messages>\n");
		sb.append("    ").append(escapeXml(userMessage)).append("\n");
		sb.append("  </user-messages>\n");
		sb.append("  <agent-responses>\n");
		sb.append("    ").append(escapeXml(agentResponse)).append("\n");
		sb.append("  </agent-responses>\n");
		sb.append("</review-request>");
		return truncateTotal(sb.toString());
	}

	/**
	 * Serializes the full chat conversation into XML for review.
	 */
	private static String serializeFullChat(Chat chat) {
		StringBuilder userMessages = new StringBuilder();
		StringBuilder agentResponses = new StringBuilder();

		List<ChatMessage> messages = chat.getMessages();
		for (ChatMessage msg : messages) {
			if (userMessages.length() + agentResponses.length() > MAX_TOTAL_LENGTH - 200) {
				break;
			}
			String user = msg.getUser();
			String text = truncateText(msg.getText());
			if ("Agent".equals(user) || "AI".equals(user) || "System".equals(user)) {
				agentResponses.append("    ").append(escapeXml(text)).append("\n");
			} else {
				userMessages.append("    ").append(escapeXml(text)).append("\n");
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<review-request>\n");
		sb.append("  <user-messages>\n").append(userMessages).append("  </user-messages>\n");
		sb.append("  <agent-responses>\n").append(agentResponses).append("  </agent-responses>\n");
		sb.append("</review-request>");
		return truncateTotal(sb.toString());
	}

	/** */
	private static String truncateText(String s) {
		if (s == null) {
			return "";
		}
		if (s.length() <= MAX_TEXT_LENGTH) {
			return s;
		}
		return s.substring(0, MAX_TEXT_LENGTH - 3) + "...";
	}

	/** */
	private static String truncateTotal(String xml) {
		if (xml.length() <= MAX_TOTAL_LENGTH) {
			return xml;
		}
		int cutoff = xml.lastIndexOf('\n', MAX_TOTAL_LENGTH - 25);
		if (cutoff <= 0) {
			cutoff = MAX_TOTAL_LENGTH - 25;
		}
		return xml.substring(0, cutoff) + "\n<!-- truncated -->\n</review-request>";
	}

	/** */
	private static String escapeXml(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;");
	}
}
