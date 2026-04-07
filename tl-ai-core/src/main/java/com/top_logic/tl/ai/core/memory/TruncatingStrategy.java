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
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;

/**
 * Compaction strategy that drops oldest messages to fit within the token budget.
 *
 * <p>Maintains the tool call/result pairing invariant: if an {@link AssistantMessage}
 * containing {@link ToolCall}s is dropped, the corresponding {@link ToolResultMessage}s
 * are also dropped (and vice versa).</p>
 *
 * <p>Always preserves at least {@code minPreserve} recent messages.</p>
 */
public class TruncatingStrategy implements CompactionStrategy {

	/** Default minimum messages to preserve. */
	public static final int DEFAULT_MIN_PRESERVE = 4;

	private final int _minPreserve;

	/** */
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
			TokenEstimator estimator) {
		int tokensBefore = estimator.estimateTokens(messages);

		if (tokensBefore <= targetTokens) {
			return new CompactionResult(messages, 0, tokensBefore, tokensBefore, "auto");
		}

		int preserveFrom = Math.max(0, messages.size() - _minPreserve);

		Set<String> droppedToolCallIds = new HashSet<>();
		int dropCount = 0;

		for (int i = 0; i < preserveFrom; i++) {
			Message msg = messages.get(i);
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
		replacement.addAll(kept);

		int tokensAfter = estimator.estimateTokens(replacement);
		return new CompactionResult(replacement, dropCount, tokensBefore, tokensAfter, "auto");
	}
}
