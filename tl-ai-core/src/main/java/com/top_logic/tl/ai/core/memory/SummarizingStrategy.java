/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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
 * <p>The split point is driven by the token budget, not by a message count: messages are
 * taken into the preserve zone from the newest backwards while their estimated cost stays
 * within {@code targetTokens * recentTokenFraction}. Two rules bound that walk:</p>
 * <ul>
 *   <li>{@code minPreserve} is a <em>floor</em> - that many recent messages are always
 *       preserved even when the budget would keep fewer, so the model never loses the
 *       exchange it is supposed to continue.</li>
 *   <li>The budget is a <em>hard limit</em> that outranks the floor: if the
 *       floor-mandated preserve zone alone does not fit {@code targetTokens}, its oldest
 *       messages are moved into the summarize zone until it does - down to an empty
 *       preserve zone, where the whole conversation becomes one summary. Honoring the
 *       floor there would return a conversation that cannot be sent at all, so every
 *       following request would fail with a context overflow.</li>
 * </ul>
 *
 * <p>The result can therefore still land marginally above {@code targetTokens} in one
 * case: when the floor holds a preserve zone that fits {@code targetTokens} on its own but
 * leaves no room for the summary. That is the fixed point described on
 * {@link #isCompactionOutputOnly(List)}, and it is reached without further summarizer
 * calls. With the default {@link #DEFAULT_RECENT_TOKEN_FRACTION} it takes a preserve zone
 * of more than half the budget - i.e. a floor forced against the budget - to get there at
 * all.</p>
 *
 * <p>The split never separates an {@link AssistantMessage} carrying tool calls from its
 * {@link ToolResultMessage}s — a preserve zone starting with tool results whose calls
 * were summarized away is rejected by the provider APIs.</p>
 *
 * <p><b>{@link SystemMessage}s are summarized like any other message</b> and nothing is
 * preserved verbatim. An agent's standing instructions travel in
 * {@link ProviderRequest#systemPrompt()}, never in the message list, and no provider
 * request builder renders a mid-conversation {@link SystemMessage} into a request at all
 * (Anthropic and Bedrock skip it explicitly; the OpenAI and chat-completions builders
 * have no branch for it). A {@link SystemMessage} in the history is therefore purely our
 * own bookkeeping - in practice a sub-agent result injected between turns - that costs
 * estimated tokens without reaching the model. Re-emitting that region verbatim, as this
 * strategy used to, let it grow without bound and made compaction unable to converge.</p>
 *
 * <p>Falls back to {@link TruncatingStrategy} if the LLM call fails, and when a pass
 * would spend a summarizer call on nothing but the output of an earlier pass (see
 * {@link #isCompactionOutputOnly(List)}).</p>
 */
public class SummarizingStrategy implements CompactionStrategy {

	/** Default minimum messages to preserve. */
	static final int DEFAULT_MIN_PRESERVE = 4;

	/**
	 * Default fraction of the token budget that the preserve zone may occupy.
	 *
	 * <p>Half the budget is kept for recent messages verbatim; the other half has to
	 * absorb three things. First, the summary this compaction adds - its size is bounded
	 * by the summarizer's own {@code maxOutputTokens}, normally a small fraction of a real
	 * context budget (2048 against ~95K usable of a 128K window), so this is generous.
	 * Second, the system prompt and tool schemas, which are part of the real request and
	 * count against the model's context window but are invisible to the
	 * {@link TokenEstimator}, which only ever sees the message list. Third, headroom for
	 * the turns that follow: compaction triggers at the budget, so a pass that returned
	 * exactly at the budget would trigger the next one again immediately.</p>
	 *
	 * @see #SummarizingStrategy(LLMProvider, String, int, int, double)
	 */
	static final double DEFAULT_RECENT_TOKEN_FRACTION = 0.5;

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

	private final double _recentTokenFraction;

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
		this(provider, model, maxOutputTokens, minPreserve, DEFAULT_RECENT_TOKEN_FRACTION);
	}

	/**
	 * @param provider            LLM provider for the summarization call
	 * @param model               model to use for summarization
	 * @param maxOutputTokens     maximum output tokens for the summary
	 * @param minPreserve         minimum recent messages to preserve
	 * @param recentTokenFraction fraction of {@code targetTokens} the preserve zone may
	 *                            occupy, in {@code (0, 1]}, see
	 *                            {@link #DEFAULT_RECENT_TOKEN_FRACTION}
	 */
	public SummarizingStrategy(LLMProvider provider, String model, int maxOutputTokens,
			int minPreserve, double recentTokenFraction) {
		_provider = Objects.requireNonNull(provider, "Provider must not be null.");
		_model = Objects.requireNonNull(model, "Model must not be null.");
		_maxOutputTokens = maxOutputTokens;
		if (minPreserve < 1) {
			throw new IllegalArgumentException("minPreserve must be at least 1.");
		}
		if (!(recentTokenFraction > 0.0) || recentTokenFraction > 1.0) {
			throw new IllegalArgumentException("recentTokenFraction must be in (0, 1].");
		}
		_minPreserve = minPreserve;
		_recentTokenFraction = recentTokenFraction;
	}

	@Override
	public CompactionResult compact(List<Message> messages, int targetTokens,
			TokenEstimator estimator, int tokensBefore) {
		if (tokensBefore <= targetTokens) {
			return unchanged(messages, tokensBefore);
		}

		int splitIndex = findSplitIndex(messages, targetTokens, estimator);
		List<Message> summarizeZone = messages.subList(0, splitIndex);
		List<Message> preserveZone = messages.subList(splitIndex, messages.size());

		if (summarizeZone.isEmpty()) {
			// The whole message list fits the recent-token budget, yet tokensBefore says we
			// are over: the excess is in the system prompt and tool schemas that the caller's
			// figure includes and the estimator cannot see. No split of this message list can
			// free it, so report no progress rather than spending a summarizer call.
			return unchanged(messages, tokensBefore);
		}
		if (isCompactionOutputOnly(summarizeZone)) {
			// Summarizing a summary again costs a call to trade one paraphrase for another:
			// it frees nothing (that is the observed non-convergence - three passes, three
			// calls, zero tokens freed) and loses detail every round. Truncation gets under
			// budget for free, at the price of dropping the earlier summary outright, which
			// is the honest trade when the budget cannot hold the preserve zone and a
			// summary at the same time.
			Logger.info("Summarization compaction would re-summarize its own output;"
				+ " truncating instead.", SummarizingStrategy.class);
			return new TruncatingStrategy(_minPreserve)
				.compact(messages, targetTokens, estimator, tokensBefore);
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
		replacement.add(new UserMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent(SUMMARY_PREFIX + summaryText))));
		replacement.addAll(preserveZone);

		// Unlike tokensBefore, this is always the raw estimator heuristic over just the
		// replacement messages — there is no baseline for a message list that has never
		// been sent to the API yet. tokensBefore - tokensAfter is therefore a lower bound
		// on tokens actually freed, not an exact figure, whenever tokensBefore came from a
		// caller's more accurate estimate (e.g. ConversationMemory's API baseline). The
		// budget-driven split above compares against targetTokens in that same raw
		// estimator currency, so tokensAfter is the number it actually bounds; a
		// tokensBefore that includes the system prompt and tool schemas stays above it.
		int tokensAfter = estimator.estimateTokens(replacement);
		return new CompactionResult(replacement, summarizeZone.size(),
			tokensBefore, tokensAfter, "auto");
	}

	private static CompactionResult unchanged(List<Message> messages, int tokensBefore) {
		return new CompactionResult(messages, 0, tokensBefore, tokensBefore, "auto");
	}

	/**
	 * Splits the conversation so that the preserve zone fits the token budget.
	 *
	 * <p>Walks back from the newest message, accumulating {@link TokenEstimator} cost,
	 * until the recent-token budget ({@code targetTokens * recentTokenFraction}, see
	 * {@link #DEFAULT_RECENT_TOKEN_FRACTION}) is reached; {@code minPreserve} is a floor on
	 * that walk and {@code targetTokens} is a hard limit that outranks the floor. Both are
	 * documented on the class.</p>
	 *
	 * <p>Two adjustments keep the preserve zone from starting with an orphaned tool result,
	 * in the direction the respective step is already moving:</p>
	 * <ul>
	 *   <li>After the backward walk the boundary moves <em>back</em> onto the assistant
	 *       message that requested the results, keeping the pair together in the preserve
	 *       zone. This relies on {@link com.top_logic.tl.ai.core.agent.AgentLoop} always
	 *       appending a tool call's results immediately after the assistant message that
	 *       requested them, with nothing interleaved - walking back while the candidate
	 *       boundary is itself a {@link ToolResultMessage} is therefore guaranteed to land
	 *       exactly on that assistant message.</li>
	 *   <li>When the hard limit then has to shrink the preserve zone, the boundary moves
	 *       <em>forward</em> past the whole run of tool results instead. Their assistant
	 *       message is older than the boundary and hence already in the summarize zone, so
	 *       the pair stays together there and is summarized rather than orphaned. Moving
	 *       back would undo the very shrinking that step is doing.</li>
	 * </ul>
	 *
	 * <p>{@link TruncatingStrategy} enforces the same invariant differently — by tracking
	 * dropped tool-call IDs and stripping any now-orphaned result out of what it keeps —
	 * because it is fine losing that result outright. Here it is not: both adjustments keep
	 * the assistant message and its results on the same side of the boundary, so nothing is
	 * silently dropped without at least being summarized.</p>
	 */
	private int findSplitIndex(List<Message> messages, int targetTokens, TokenEstimator estimator) {
		int keepRecentTokens = Math.max(1, (int) Math.round(targetTokens * _recentTokenFraction));

		int splitIndex = messages.size();
		int preservedTokens = 0;
		for (int i = messages.size() - 1; i >= 0; i--) {
			int cost = estimator.estimateTokens(messages.get(i));
			boolean belowFloor = messages.size() - splitIndex < _minPreserve;
			if (!belowFloor && preservedTokens + cost > keepRecentTokens) {
				break;
			}
			preservedTokens += cost;
			splitIndex = i;
		}

		while (splitIndex > 0 && splitIndex < messages.size()
				&& messages.get(splitIndex) instanceof ToolResultMessage) {
			splitIndex--;
			preservedTokens += estimator.estimateTokens(messages.get(splitIndex));
		}

		while (splitIndex < messages.size() && preservedTokens > targetTokens) {
			preservedTokens -= estimator.estimateTokens(messages.get(splitIndex));
			splitIndex++;
			while (splitIndex < messages.size()
					&& messages.get(splitIndex) instanceof ToolResultMessage) {
				preservedTokens -= estimator.estimateTokens(messages.get(splitIndex));
				splitIndex++;
			}
		}
		return splitIndex;
	}

	/**
	 * Whether the given summarize zone holds nothing but what an earlier compaction pass
	 * produced: {@link CompactionMarker}s and summary {@link UserMessage}s.
	 *
	 * <p>This is the fixed point of repeated compaction. It is reached when the preserve
	 * zone cannot shrink any further - {@code minPreserve} holds it at the floor and the
	 * hard budget limit has nothing left to take away - so every further pass would see the
	 * same split and re-summarize its own previous summary.</p>
	 */
	private static boolean isCompactionOutputOnly(List<Message> summarizeZone) {
		for (Message msg : summarizeZone) {
			if (msg instanceof CompactionMarker) {
				continue;
			}
			if (msg instanceof UserMessage user && startsWithSummaryPrefix(user)) {
				continue;
			}
			return false;
		}
		return true;
	}

	private static boolean startsWithSummaryPrefix(UserMessage msg) {
		for (ContentPart part : msg.content()) {
			if (part instanceof TextContent text) {
				return text.text().startsWith(SUMMARY_PREFIX);
			}
		}
		return false;
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
			} else if (msg instanceof SystemMessage sys) {
				result = systemMessageToUserMessage(sys);
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

	/**
	 * Renders a {@link SystemMessage} as user text for the summarizer request.
	 *
	 * <p>Passing it through as a {@link SystemMessage} would lose it: the request builders
	 * drop mid-conversation system messages, so a summarize zone made of sub-agent results
	 * would reach the summarizer as an empty message list. The content is capped like a
	 * tool result - a sub-agent report is exactly the kind of unbounded text that would
	 * otherwise overflow the summarizer's own context window and force the truncation
	 * fallback, which is the one path that still cannot compact these messages away.</p>
	 */
	private UserMessage systemMessageToUserMessage(SystemMessage msg) {
		String content = msg.content();
		if (content.length() > MAX_TOOL_RESULT_CHARS) {
			content = content.substring(0, MAX_TOOL_RESULT_CHARS)
				+ "\n[truncated: " + content.length() + " chars total]";
		}
		return new UserMessage(msg.id(), msg.timestamp(),
			List.of(new TextContent("[System note: " + content + "]")));
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
