/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Compaction strategy that calls an LLM to summarize older messages.
 *
 * <p>Splits the conversation into a summarize zone (older messages) and a preserve zone
 * (recent messages). The summarize zone is sent to the LLM with a structured prompt,
 * and the resulting summary replaces the original messages.</p>
 *
 * <p>Falls back to {@link TruncatingStrategy} if the LLM call fails.</p>
 */
public class SummarizingStrategy implements CompactionStrategy {

	/** Default minimum messages to preserve. */
	static final int DEFAULT_MIN_PRESERVE = 4;

	/** Maximum characters per tool result sent to the summarizer. */
	static final int MAX_TOOL_RESULT_CHARS = 2000;

	static final String SUMMARIZATION_PROMPT =
		"You are a conversation summarizer. Analyze the conversation and produce a structured summary.\n\n"
		+ "Preserve ALL of the following in your summary:\n"
		+ "1. Primary request and user's intent\n"
		+ "2. Key technical context and decisions made\n"
		+ "3. Files, resources, and code sections referenced (include key snippets)\n"
		+ "4. Errors encountered and how they were resolved\n"
		+ "5. Work completed so far\n"
		+ "6. Pending tasks and next steps\n"
		+ "7. Current working state\n\n"
		+ "Be precise. Include file names, function names, and specific values.\n"
		+ "Do not include meta-commentary about the summarization process.";

	private final LLMProvider _provider;

	private final String _model;

	private final int _maxOutputTokens;

	private final int _minPreserve;

	/**
	 * @param provider       LLM provider for the summarization call
	 * @param model          model to use for summarization
	 * @param maxOutputTokens maximum output tokens for the summary
	 */
	public SummarizingStrategy(LLMProvider provider, String model, int maxOutputTokens) {
		this(provider, model, maxOutputTokens, DEFAULT_MIN_PRESERVE);
	}

	/**
	 * @param provider        LLM provider for the summarization call
	 * @param model           model to use for summarization
	 * @param maxOutputTokens maximum output tokens for the summary
	 * @param minPreserve     minimum recent messages to preserve
	 */
	public SummarizingStrategy(LLMProvider provider, String model, int maxOutputTokens,
			int minPreserve) {
		_provider = Objects.requireNonNull(provider, "Provider must not be null.");
		_model = Objects.requireNonNull(model, "Model must not be null.");
		_maxOutputTokens = maxOutputTokens;
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

		int splitIndex = findSplitIndex(messages);
		List<Message> summarizeZone = new ArrayList<>();
		List<Message> preservedSystemMessages = new ArrayList<>();
		for (int i = 0; i < splitIndex; i++) {
			Message msg = messages.get(i);
			if (msg instanceof SystemMessage) {
				preservedSystemMessages.add(msg);
			} else {
				summarizeZone.add(msg);
			}
		}
		List<Message> preserveZone = messages.subList(splitIndex, messages.size());

		if (summarizeZone.isEmpty()) {
			return new CompactionResult(messages, 0, tokensBefore, tokensBefore, "auto");
		}

		String summaryText;
		try {
			summaryText = callSummarizer(summarizeZone);
		} catch (Exception e) {
			return new TruncatingStrategy(_minPreserve)
				.compact(messages, targetTokens, estimator);
		}

		List<Message> replacement = new ArrayList<>();
		Instant now = Instant.now();
		replacement.add(new CompactionMarker(
			UUID.randomUUID().toString(), now,
			summarizeZone.size(), tokensBefore, "auto"));
		replacement.addAll(preservedSystemMessages);
		replacement.add(new SystemMessage(
			UUID.randomUUID().toString(), now, summaryText));
		replacement.addAll(preserveZone);

		int tokensAfter = estimator.estimateTokens(replacement);
		return new CompactionResult(replacement, summarizeZone.size(),
			tokensBefore, tokensAfter, "auto");
	}

	private int findSplitIndex(List<Message> messages) {
		return Math.max(0, messages.size() - _minPreserve);
	}

	private String callSummarizer(List<Message> messagesToSummarize)
			throws com.top_logic.tl.ai.core.provider.ProviderException {
		List<Message> prepared = prepareForSummarization(messagesToSummarize);

		ProviderRequest request = new ProviderRequest(
			_model, SUMMARIZATION_PROMPT, prepared, List.of(), _maxOutputTokens, Map.of());

		StringBuilder summary = new StringBuilder();
		_provider.stream(request, event -> {
			if (event instanceof StreamEvent.TextDelta delta) {
				summary.append(delta.text());
			}
		});

		return summary.toString();
	}

	private List<Message> prepareForSummarization(List<Message> messages) {
		List<Message> prepared = new ArrayList<>();
		for (Message msg : messages) {
			Message result;
			if (msg instanceof UserMessage u) {
				result = stripImages(u);
			} else if (msg instanceof AssistantMessage a) {
				result = stripImagesFromAssistant(a);
			} else if (msg instanceof ToolResultMessage tr) {
				result = truncateToolResult(tr);
			} else if (msg instanceof SystemMessage) {
				result = msg;
			} else if (msg instanceof CompactionMarker) {
				result = msg;
			} else {
				throw new IllegalArgumentException("Unexpected message type: " + msg.getClass());
			}
			prepared.add(result);
		}
		return prepared;
	}

	private UserMessage stripImages(UserMessage msg) {
		List<ContentPart> stripped = new ArrayList<>();
		for (ContentPart part : msg.content()) {
			if (part instanceof ImageContent) {
				stripped.add(new TextContent("[image]"));
			} else {
				stripped.add(part);
			}
		}
		return new UserMessage(msg.id(), msg.timestamp(), stripped);
	}

	private AssistantMessage stripImagesFromAssistant(AssistantMessage msg) {
		List<ContentPart> stripped = new ArrayList<>();
		boolean changed = false;
		for (ContentPart part : msg.content()) {
			if (part instanceof ImageContent) {
				stripped.add(new TextContent("[image]"));
				changed = true;
			} else if (part instanceof ThinkingContent) {
				changed = true;
			} else {
				stripped.add(part);
			}
		}
		if (!changed) {
			return msg;
		}
		return new AssistantMessage(msg.id(), msg.timestamp(), stripped,
			msg.usage(), msg.finishReason());
	}

	private ToolResultMessage truncateToolResult(ToolResultMessage msg) {
		if (!(msg.state() instanceof ToolState.Completed completed)) {
			return msg;
		}
		String result = completed.result();
		if (result.length() <= MAX_TOOL_RESULT_CHARS) {
			return msg;
		}
		String truncated = result.substring(0, MAX_TOOL_RESULT_CHARS)
			+ "\n[truncated: " + result.length() + " chars total]";
		return new ToolResultMessage(msg.id(), msg.timestamp(), msg.toolCallId(), msg.toolName(),
			new ToolState.Completed(truncated, completed.startTime(), completed.endTime(),
				completed.compacted()));
	}
}
