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

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;
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
 * and the resulting summary replaces the original messages. The summary is emitted as a
 * {@link UserMessage}: mid-conversation {@link SystemMessage}s are not representable in
 * the provider APIs (Anthropic and Bedrock only support a top-level system prompt), so a
 * system-role summary would be invisible to the model.</p>
 *
 * <p>The split never separates an {@link AssistantMessage} carrying tool calls from its
 * {@link ToolResultMessage}s — a preserve zone starting with tool results whose calls
 * were summarized away is rejected by the provider APIs.</p>
 *
 * <p>Falls back to {@link TruncatingStrategy} if the LLM call fails.</p>
 */
public class SummarizingStrategy implements CompactionStrategy {

	/** Default minimum messages to preserve. */
	static final int DEFAULT_MIN_PRESERVE = 4;

	/** Preamble prepended to the summary so the model knows it replaces compacted history. */
	static final String SUMMARY_PREFIX =
		"[Summary of the earlier conversation — the original messages were compacted to free context]\n\n";

	/** Maximum characters per tool result sent to the summarizer. */
	static final int MAX_TOOL_RESULT_CHARS = 2000;

	static final String SUMMARIZATION_PROMPT =
		"You are a conversation summarizer. Analyze the conversation and produce a structured summary.\n\n"
		+ "Preserve ALL of the following in your summary:\n"
		+ "1. Primary request and user's intent\n"
		+ "2. Key technical context and decisions made\n"
		+ "3. Model types, objects, and resources referenced\n"
		+ "4. Tool calls made and their outcomes\n"
		+ "5. Errors encountered and how they were resolved\n"
		+ "6. Work completed so far\n"
		+ "7. Pending tasks and next steps\n\n"
		+ "Be precise. Include type names, object identifiers, tool names, and specific values.\n"
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
			TokenEstimator estimator, int tokensBefore) {
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
			// Covers both legitimate transient failures (network, rate limit, a summarize
			// zone too large for the summarizer's own context window) and genuine bugs
			// (e.g. prepareForSummarization's "unexpected message type" for a Message
			// subtype this class doesn't handle yet) — both must still degrade to
			// truncation rather than lose the turn, but only a log line tells them apart
			// after the fact.
			Logger.warn("Summarization compaction failed, falling back to truncation: "
				+ e.getMessage(), e, SummarizingStrategy.class);
			return new TruncatingStrategy(_minPreserve)
				.compact(messages, targetTokens, estimator, tokensBefore);
		}
		if (summaryText.isBlank()) {
			// The LLM can return an empty assistant message when the provider API
			// encounters tool_use/tool_result blocks without matching tool definitions
			// (which is how the Anthropic API behaves for summarizer calls that include
			// assistant turns with tool calls). prepareForSummarization now converts those
			// to plain text, so this guard is a last-resort safety net.
			Logger.warn("Summarization returned empty response, falling back to truncation.",
				SummarizingStrategy.class);
			return new TruncatingStrategy(_minPreserve)
				.compact(messages, targetTokens, estimator, tokensBefore);
		}

		List<Message> replacement = new ArrayList<>();
		Instant now = Instant.now();
		replacement.add(new CompactionMarker(
			UUID.randomUUID().toString(), now,
			summarizeZone.size(), tokensBefore, "auto"));
		replacement.addAll(preservedSystemMessages);
		replacement.add(new UserMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent(SUMMARY_PREFIX + summaryText))));
		replacement.addAll(preserveZone);

		// Unlike tokensBefore, this is always the raw estimator heuristic over just the
		// replacement messages — there is no baseline for a message list that has never
		// been sent to the API yet. tokensBefore - tokensAfter is therefore a lower bound
		// on tokens actually freed, not an exact figure, whenever tokensBefore came from a
		// caller's more accurate estimate (e.g. ConversationMemory's API baseline).
		int tokensAfter = estimator.estimateTokens(replacement);
		return new CompactionResult(replacement, summarizeZone.size(),
			tokensBefore, tokensAfter, "auto");
	}

	/**
	 * Adjusts the split boundary so the preserve zone never starts with an orphaned tool
	 * result. Relies on {@link com.top_logic.tl.ai.core.agent.AgentLoop} always appending a
	 * tool call's results immediately after the assistant message that requested them, with
	 * nothing interleaved — walking back while the candidate boundary is itself a
	 * {@link ToolResultMessage} is therefore guaranteed to land exactly on that assistant
	 * message.
	 *
	 * <p>{@link TruncatingStrategy} enforces the same invariant differently — by tracking
	 * dropped tool-call IDs and stripping any now-orphaned result out of what it keeps —
	 * because it is fine losing that result outright. Here it is not: moving the boundary
	 * back instead keeps the assistant message and its results together on the preserve
	 * side, so nothing is silently dropped without at least being summarized.</p>
	 */
	private int findSplitIndex(List<Message> messages) {
		int splitIndex = Math.max(0, messages.size() - _minPreserve);
		while (splitIndex > 0 && messages.get(splitIndex) instanceof ToolResultMessage) {
			splitIndex--;
		}
		return splitIndex;
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
				result = flattenAssistantForSummarization(a);
			} else if (msg instanceof ToolResultMessage tr) {
				// Convert to a plain UserMessage so the summarizer request never contains
				// tool_result blocks that reference tool_use IDs no longer present in the
				// request — Anthropic returns an empty response in that situation.
				result = toolResultToUserMessage(tr);
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

	private AssistantMessage flattenAssistantForSummarization(AssistantMessage msg) {
		List<ContentPart> flattened = new ArrayList<>();
		boolean changed = false;
		for (ContentPart part : msg.content()) {
			if (part instanceof ImageContent) {
				flattened.add(new TextContent("[image]"));
				changed = true;
			} else if (part instanceof ThinkingContent) {
				// Drop: thinking blocks are internal reasoning, not conversation content.
				changed = true;
			} else if (part instanceof ToolCall tc) {
				// Convert to text so the summarizer request never contains tool_use blocks
				// without matching tool definitions — Anthropic returns an empty assistant
				// message in that situation, causing silent context loss.
				String args = tc.arguments();
				if (args != null && args.length() > MAX_TOOL_RESULT_CHARS) {
					args = args.substring(0, MAX_TOOL_RESULT_CHARS)
						+ "...[truncated]";
				}
				flattened.add(new TextContent("[Called tool: " + tc.name()
					+ "(" + (args != null ? args : "") + ")]"));
				changed = true;
			} else {
				flattened.add(part);
			}
		}
		if (!changed) {
			return msg;
		}
		return new AssistantMessage(msg.id(), msg.timestamp(), flattened,
			msg.usage(), msg.finishReason());
	}

	private UserMessage toolResultToUserMessage(ToolResultMessage msg) {
		String text;
		if (msg.state() instanceof ToolState.Failed failed) {
			String error = failed.error();
			if (error.length() > MAX_TOOL_RESULT_CHARS) {
				error = error.substring(0, MAX_TOOL_RESULT_CHARS)
					+ "\n[truncated: " + error.length() + " chars total]";
			}
			text = "[Tool error: " + msg.toolName() + " → " + error + "]";
		} else if (msg.state() instanceof ToolState.Completed completed) {
			String resultText = completed.summary() != null ? completed.summary() : completed.result();
			if (resultText.length() > MAX_TOOL_RESULT_CHARS) {
				resultText = resultText.substring(0, MAX_TOOL_RESULT_CHARS)
					+ "\n[truncated: " + resultText.length() + " chars total]";
			}
			text = "[Tool result: " + msg.toolName() + " → " + resultText + "]";
		} else {
			text = "[Tool result: " + msg.toolName() + " (status unknown)]";
		}
		return new UserMessage(msg.id(), msg.timestamp(), List.of(new TextContent(text)));
	}
}
