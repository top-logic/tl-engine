/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;

/**
 * Compaction strategy that drops oldest messages to fit within the token budget.
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>{@link SystemMessage}s are never dropped — they carry context the LLM needs
 *       to stay on track.</li>
 *   <li>Tool call/result pairing: if an {@link AssistantMessage} containing
 *       {@link ToolCall}s is dropped, the corresponding {@link ToolResultMessage}s
 *       are also dropped (and vice versa).</li>
 *   <li>At least {@code minPreserve} recent non-system messages are kept.</li>
 * </ul>
 */
public class TruncatingStrategy implements CompactionStrategy {

	/** Default minimum messages to preserve. */
	public static final int DEFAULT_MIN_PRESERVE = 4;

	private final int _minPreserve;

	/** Creates a {@link TruncatingStrategy}. */
	public TruncatingStrategy() {
		this(DEFAULT_MIN_PRESERVE);
	}

	/**
	 * @param minPreserve minimum number of recent messages to always preserve
	 */
	public TruncatingStrategy(int minPreserve) {
		if (minPreserve < 1) {
			throw new IllegalArgumentException("minPreserve must be at least 1.");
		}
		_minPreserve = minPreserve;
	}

	@Override
	public CompactionResult compact(List<Message> messages, int targetTokens,
			TokenEstimator estimator, int tokensBefore) {
		if (tokensBefore <= targetTokens) {
			return new CompactionResult(messages, 0, tokensBefore, tokensBefore, "auto");
		}

		int preserveFrom = Math.max(0, messages.size() - _minPreserve);

		// Enforces "never orphan a tool result" by tracking dropped tool-call IDs and
		// stripping any now-orphaned result out of the kept side afterward (below), rather
		// than moving preserveFrom back like SummarizingStrategy.findSplitIndex does. That
		// difference is intentional: dropping the orphaned result along with its tool call
		// is consistent with this strategy's whole purpose (things are meant to be dropped
		// here), whereas SummarizingStrategy must not silently drop a result without at
		// least summarizing it, so it keeps the pair together instead.
		Set<String> droppedToolCallIds = new HashSet<>();
		List<Message> preservedSystemMessages = new ArrayList<>();
		int dropCount = 0;

		for (int i = 0; i < preserveFrom; i++) {
			Message msg = messages.get(i);
			if (msg instanceof SystemMessage) {
				preservedSystemMessages.add(msg);
				continue;
			}
			if (msg instanceof AssistantMessage a) {
				for (ContentPart part : a.content()) {
					if (part instanceof ToolCall tc) {
						droppedToolCallIds.add(tc.id());
					}
				}
			}
			if (msg instanceof ToolResultMessage tr) {
				droppedToolCallIds.add(tr.toolCallId());
			}
			dropCount++;
		}

		// Build kept list, removing orphaned tool results
		List<Message> kept = new ArrayList<>();
		for (int i = preserveFrom; i < messages.size(); i++) {
			Message msg = messages.get(i);
			if (msg instanceof ToolResultMessage tr && droppedToolCallIds.contains(tr.toolCallId())) {
				dropCount++;
				continue;
			}
			kept.add(msg);
		}

		List<Message> replacement = new ArrayList<>();
		if (dropCount > 0) {
			replacement.add(new CompactionMarker(
				UUID.randomUUID().toString(), Instant.now(),
				dropCount, tokensBefore, "auto"));
		}
		replacement.addAll(preservedSystemMessages);
		replacement.addAll(kept);

		int tokensAfter = estimator.estimateTokens(replacement);
		return new CompactionResult(replacement, dropCount, tokensBefore, tokensAfter, "auto");
	}
}
