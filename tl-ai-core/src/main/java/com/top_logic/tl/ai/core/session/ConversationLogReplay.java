/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * Rebuilds an LLM {@link Conversation} from a persisted {@link ConversationLog}.
 *
 * <p>Used when a session is loaded from the database to reconstruct the message
 * history that the LLM needs for context continuity.</p>
 */
public class ConversationLogReplay {

	/**
	 * Replays all completed turns from the log into the conversation.
	 *
	 * <p>User text parts become {@link UserMessage}s. Assistant text, thinking,
	 * and tool call parts are accumulated and flushed as {@link AssistantMessage}s.
	 * Completed and failed tool calls produce {@link ToolResultMessage}s. Parts
	 * that are not relevant to LLM context (approvals, system events, compaction
	 * markers, etc.) are skipped.</p>
	 */
	public static void rebuildMemory(ConversationLog log, Conversation conversation) {
		for (Turn turn : log.turns()) {
			replayTurn(turn, conversation);
		}
	}

	private static void replayTurn(Turn turn, Conversation conversation) {
		// 1. Replay user parts.
		for (Part userPart : turn.userParts()) {
			if (userPart instanceof Part.TextPart tp) {
				conversation.add(new UserMessage(tp.id(), tp.timestamp(),
					List.of(new TextContent(tp.text()))));
			}
		}

		// 2. Process assistant parts — accumulate content, flush on tool calls.
		List<ContentPart> currentContent = new ArrayList<>();

		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.TextPart tp) {
				currentContent.add(new TextContent(tp.text()));
			} else if (part instanceof Part.ThinkingPart tp) {
				currentContent.add(new ThinkingContent(tp.text()));
			} else if (part instanceof Part.ToolCallPart tc) {
				if (tc.status() == Part.ToolCallStatus.RUNNING) {
					// Incomplete tool call — skip gracefully.
					continue;
				}

				// Add tool call to current content, then flush as assistant message.
				currentContent.add(new ToolCall(tc.id(), tc.toolName(), tc.argsJson()));
				flushAssistantMessage(conversation, currentContent, turn);
				currentContent = new ArrayList<>();

				// Add tool result.
				addToolResult(conversation, tc);
			}
			// Skip: ApprovalPart, InteractionPart, SystemPart, CompactionPart,
			//        TransactionPart, ErrorPart, DelegationPart
		}

		// 3. Flush remaining content as final assistant message.
		flushAssistantMessage(conversation, currentContent, turn);
	}

	private static void flushAssistantMessage(Conversation conversation,
			List<ContentPart> content, Turn turn) {
		if (content.isEmpty()) {
			return;
		}
		conversation.add(new AssistantMessage(
			UUID.randomUUID().toString(), Instant.now(),
			List.copyOf(content), turn.cumulativeUsage(), "stop"));
	}

	private static void addToolResult(Conversation conversation, Part.ToolCallPart tc) {
		Instant now = Instant.now();
		ToolState state;

		if (tc.status() == Part.ToolCallStatus.COMPLETED) {
			state = new ToolState.Completed(
				tc.result() != null ? tc.result() : "", now, now, false);
		} else if (tc.status() == Part.ToolCallStatus.FAILED) {
			state = new ToolState.Failed(
				tc.errorMessage() != null ? tc.errorMessage() : "Unknown error", now, now);
		} else if (tc.status() == Part.ToolCallStatus.DENIED) {
			String reason = tc.denyReason() != null ? tc.denyReason() : "";
			state = new ToolState.Failed(
				"Tool call denied by user: " + reason, now, now);
		} else {
			return;
		}

		conversation.add(new ToolResultMessage(
			UUID.randomUUID().toString(), now, tc.id(), tc.toolName(), state));
	}
}
