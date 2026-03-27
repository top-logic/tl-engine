/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;
import java.util.Map;

/**
 * Static utility that serializes conversation data into XML format for the review agent.
 *
 * <p>
 * Provides two serialization modes: {@link #serializeLastTurn(String, String, ChatTask)} for
 * auto-review after each agent turn (includes tool timeline), and {@link #serializeFullChat(Chat)}
 * for manual review of the entire conversation (messages only, no tool timelines).
 * </p>
 */
public class ConversationSerializer {

	/** Maximum length for individual result strings in the XML output. */
	private static final int MAX_RESULT_LENGTH = 2000;

	/** Maximum total length of the serialized XML. */
	private static final int MAX_TOTAL_LENGTH = 50_000;

	private ConversationSerializer() {
		// Static utility class
	}

	/**
	 * Serializes the last conversation turn into XML for auto-review.
	 *
	 * @param userMessage
	 *        The user's message text.
	 * @param agentResponse
	 *        The agent's response text.
	 * @param task
	 *        The completed {@link ChatTask} containing timeline events; may be {@code null}.
	 * @return An XML string wrapped in {@code <review-request>}.
	 */
	public static String serializeLastTurn(String userMessage, String agentResponse, ChatTask task) {
		StringBuilder sb = new StringBuilder();
		sb.append("<review-request>\n");

		sb.append("  <user-messages>\n");
		sb.append("    ").append(escapeXml(userMessage)).append("\n");
		sb.append("  </user-messages>\n");

		sb.append("  <agent-responses>\n");
		sb.append("    ").append(escapeXml(agentResponse)).append("\n");
		sb.append("  </agent-responses>\n");

		if (task != null) {
			appendTimeline(sb, task.getTimelineEvents());
		}

		sb.append("</review-request>");
		return truncateTotal(sb.toString());
	}

	/**
	 * Serializes the entire chat conversation into XML for manual review.
	 *
	 * <p>
	 * Only user messages and agent/system responses are included. Tool call timelines are omitted
	 * to keep the serialized output concise.
	 * </p>
	 *
	 * @param chat
	 *        The {@link Chat} containing all entries.
	 * @return An XML string wrapped in {@code <review-request>}.
	 */
	public static String serializeFullChat(Chat chat) {
		StringBuilder userMessages = new StringBuilder();
		StringBuilder agentResponses = new StringBuilder();

		for (ChatEntry entry : chat.getEntries()) {
			if (userMessages.length() + agentResponses.length() > MAX_TOTAL_LENGTH - 200) {
				break;
			}
			if (entry instanceof ChatEntry.MessageEntry me) {
				ChatMessage msg = me.message();
				String user = msg.getUser();
				if ("Agent".equals(user) || "AI".equals(user) || "System".equals(user)) {
					agentResponses.append("    ").append(escapeXml(msg.getText())).append("\n");
				} else {
					userMessages.append("    ").append(escapeXml(msg.getText())).append("\n");
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<review-request>\n");
		sb.append("  <user-messages>\n").append(userMessages).append("  </user-messages>\n");
		sb.append("  <agent-responses>\n").append(agentResponses).append("  </agent-responses>\n");
		sb.append("</review-request>");
		return truncateTotal(sb.toString());
	}

	/**
	 * Appends the tool timeline section for the given events.
	 */
	private static void appendTimeline(StringBuilder sb, List<ChatTimelineEvent> events) {
		if (events == null || events.isEmpty()) {
			return;
		}

		sb.append("  <tool-timeline>\n");
		for (ChatTimelineEvent event : events) {
			if (event.getKind() == ChatTimelineEvent.Kind.TOOL_CALL) {
				appendToolCall(sb, event.getToolCall());
			} else if (event.getKind() == ChatTimelineEvent.Kind.AGENT_CALL) {
				appendAgentCall(sb, event);
			}
		}
		sb.append("  </tool-timeline>\n");
	}

	/**
	 * Appends a single tool call element.
	 */
	private static void appendToolCall(StringBuilder sb, ChatToolCall toolCall) {
		if (toolCall == null) {
			return;
		}

		sb.append("    <tool-call name=\"").append(escapeXml(toolCall.getToolName())).append("\">\n");

		Map<String, Object> args = toolCall.getArgs();
		if (args != null && !args.isEmpty()) {
			sb.append("      <args>");
			sb.append(escapeXml(truncateResult(args.toString())));
			sb.append("</args>\n");
		}

		String result = toolCall.getResultJson();
		if (result != null) {
			sb.append("      <result>");
			sb.append(escapeXml(truncateResult(result)));
			sb.append("</result>\n");
		}

		sb.append("    </tool-call>\n");
	}

	/**
	 * Appends a single agent call element.
	 */
	private static void appendAgentCall(StringBuilder sb, ChatTimelineEvent event) {
		sb.append("    <agent-call from=\"").append(escapeXml(safe(event.getFromAgent())));
		sb.append("\" to=\"").append(escapeXml(safe(event.getToAgent()))).append("\">\n");

		String input = event.getInputSummary();
		if (input != null && !input.isEmpty()) {
			sb.append("      <input>").append(escapeXml(truncateResult(input))).append("</input>\n");
		}

		String result = event.getResultSummary();
		if (result != null && !result.isEmpty()) {
			sb.append("      <result>").append(escapeXml(truncateResult(result))).append("</result>\n");
		}

		sb.append("    </agent-call>\n");
	}

	/**
	 * Truncates an individual result string to {@link #MAX_RESULT_LENGTH}.
	 */
	private static String truncateResult(String s) {
		if (s == null) {
			return "";
		}
		if (s.length() <= MAX_RESULT_LENGTH) {
			return s;
		}
		return s.substring(0, MAX_RESULT_LENGTH - 3) + "...";
	}

	/**
	 * Truncates the total XML output to {@link #MAX_TOTAL_LENGTH}.
	 */
	private static String truncateTotal(String xml) {
		if (xml.length() <= MAX_TOTAL_LENGTH) {
			return xml;
		}
		// Find the last complete line boundary before the limit
		int cutoff = xml.lastIndexOf('\n', MAX_TOTAL_LENGTH - 25);
		if (cutoff <= 0) {
			cutoff = MAX_TOTAL_LENGTH - 25;
		}
		return xml.substring(0, cutoff) + "\n<!-- truncated -->\n</review-request>";
	}

	/**
	 * Escapes XML special characters in text content.
	 */
	private static String escapeXml(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;");
	}

	/**
	 * Returns the string, or empty string if null.
	 */
	private static String safe(String s) {
		return s != null ? s : "";
	}
}
