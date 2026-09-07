/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
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

	private final List<Message> _messages = new ArrayList<>();

	private final List<ContentPart> _currentContent = new ArrayList<>();

	private final String _stopPartId;

	private boolean _stopped;

	private ConversationLogReplay(String stopPartId) {
		_stopPartId = stopPartId;
	}

	/**
	 * Replays all completed turns from the log into the conversation.
	 *
	 * <p>User text parts become {@link UserMessage}s. Assistant text, thinking,
	 * and tool call parts are accumulated and flushed as {@link AssistantMessage}s.
	 * Completed, failed, denied and rolled-back tool calls produce
	 * {@link ToolResultMessage}s. Parts that are not relevant to LLM context
	 * (approvals, system events, transactions, etc.) are skipped.</p>
	 *
	 * <p>A {@link Part.CompactionPart} carrying a {@link CompactionSidecar} that still
	 * {@link CompactionSidecar#matches(List) matches} the history replayed up to it replaces
	 * that history with the messages the compaction produced - which is what keeps a
	 * reloaded session at its compacted size instead of the size it was compacted to escape.
	 * A sidecar that no longer matches, or that is missing because the log predates it, is
	 * ignored and the full history is replayed.</p>
	 */
	public static void rebuildMemory(ConversationLog log, Conversation conversation) {
		List<Message> messages = replay(log, null);
		// Appended as one range rather than message by message: the conversation copies its
		// whole list on every add.
		conversation.replaceRange(conversation.size(), conversation.size(), messages);
	}

	/**
	 * The message history a part sees: everything recorded in the log before it, replayed by
	 * the rules of {@link #rebuildMemory(ConversationLog, Conversation)}.
	 *
	 * <p>This is what a compaction's {@link CompactionSidecar} is fingerprinted against. It
	 * is computed from the log rather than from the live conversation on purpose: the reader
	 * has nothing but the log, so the writer must fingerprint exactly what the reader will
	 * reconstruct.</p>
	 *
	 * @param partId
	 *        The part to stop before. An unknown id yields the whole history.
	 */
	public static List<Message> replayedHistoryBefore(ConversationLog log, String partId) {
		return replay(log, partId);
	}

	private static List<Message> replay(ConversationLog log, String stopPartId) {
		ConversationLogReplay replay = new ConversationLogReplay(stopPartId);
		for (Turn turn : log.turns()) {
			replay.replayTurn(turn);
			if (replay._stopped) {
				break;
			}
		}
		return replay._messages;
	}

	private void replayTurn(Turn turn) {
		// 1. Replay user parts.
		for (Part userPart : turn.userParts()) {
			if (stopAt(userPart, turn)) {
				return;
			}
			if (userPart instanceof Part.TextPart tp) {
				_messages.add(new UserMessage(tp.id(), tp.timestamp(),
					List.of(new TextContent(tp.text()))));
			}
		}

		// 2. Process assistant parts — accumulate content, flush on tool calls.
		for (Part part : turn.assistantParts()) {
			if (stopAt(part, turn)) {
				return;
			}
			if (part instanceof Part.TextPart tp) {
				_currentContent.add(new TextContent(tp.text()));
			} else if (part instanceof Part.ThinkingPart tp) {
				_currentContent.add(new ThinkingContent(tp.text(), tp.continuityToken()));
			} else if (part instanceof Part.ToolCallPart tc) {
				if (tc.status() == Part.ToolCallStatus.RUNNING) {
					// Incomplete tool call — skip gracefully.
					continue;
				}

				// Add tool call to current content, then flush as assistant message.
				// Arguments must be non-null, parseable JSON: no-argument tool calls are
				// recorded with empty args, which the persist/load round-trip turns into
				// null. Default those back to an empty JSON object.
				String argsJson = tc.argsJson();
				if (argsJson == null || argsJson.isBlank()) {
					argsJson = "{}";
				}
				_currentContent.add(new ToolCall(tc.id(), tc.toolName(), argsJson));
				flushAssistantMessage(turn);

				// Add tool result.
				addToolResult(tc);
			} else if (part instanceof Part.CompactionPart cp) {
				// A compaction is a message boundary: whatever it replaced ends here, so
				// pending content is flushed before the sidecar is weighed - both here and
				// in replayedHistoryBefore, which is what makes the two projections equal.
				flushAssistantMessage(turn);
				applySidecar(cp);
			}
			// Skip: ApprovalPart, InteractionPart, SystemPart, TransactionPart, ErrorPart,
			//        DelegationPart
		}

		// 3. Flush remaining content as final assistant message.
		flushAssistantMessage(turn);
	}

	/**
	 * Whether the replay ends before the given part, flushing what it has if so.
	 */
	private boolean stopAt(Part part, Turn turn) {
		if (!part.id().equals(_stopPartId)) {
			return false;
		}
		flushAssistantMessage(turn);
		_stopped = true;
		return true;
	}

	private void applySidecar(Part.CompactionPart part) {
		CompactionSidecar sidecar = part.sidecar();
		if (sidecar == null || !sidecar.matches(_messages)) {
			return;
		}
		_messages.clear();
		_messages.addAll(sidecar.replacementMessages());
	}

	private void flushAssistantMessage(Turn turn) {
		if (_currentContent.isEmpty()) {
			return;
		}
		_messages.add(new AssistantMessage(
			UUID.randomUUID().toString(), Instant.now(),
			List.copyOf(_currentContent), turn.cumulativeUsage(), "stop"));
		_currentContent.clear();
	}

	private void addToolResult(Part.ToolCallPart tc) {
		Instant now = Instant.now();
		ToolState state;

		if (tc.status() == Part.ToolCallStatus.COMPLETED) {
			// The log persists a tool result as text, so a replayed result is text-only: an image a
			// tool showed live is not resent on reload, its caption remains. Deliberate — the
			// alternative is storing multi-megabyte payloads in every session log.
			state = new ToolState.Completed(
				tc.result() != null ? tc.result() : "", now, now, false);
		} else if (tc.status() == Part.ToolCallStatus.FAILED) {
			state = new ToolState.Failed(
				tc.errorMessage() != null ? tc.errorMessage() : "Unknown error", now, now);
		} else if (tc.status() == Part.ToolCallStatus.ROLLED_BACK) {
			// The tool ran and reported success, but the turn's transaction never persisted it.
			// Replaying it as a failure is what keeps the correction the model was given live from
			// being lost on a page reload - the alternative replays a green write for a change that
			// does not exist and lets the model cite it again.
			String reason = tc.errorMessage() != null ? tc.errorMessage() : "Unknown reason";
			state = new ToolState.Failed(
				"The data changes of this turn were rolled back and NOT persisted: " + reason
					+ " Anything this call reported as changed does not exist. If it also returned"
					+ " data you still need, read it again.",
				now, now);
		} else if (tc.status() == Part.ToolCallStatus.DENIED) {
			String reason = tc.denyReason() != null ? tc.denyReason() : "";
			state = new ToolState.Failed(
				"Tool call denied by user: " + reason, now, now);
		} else {
			return;
		}

		_messages.add(new ToolResultMessage(
			UUID.randomUUID().toString(), now, tc.id(), tc.toolName(), state));
	}
}
