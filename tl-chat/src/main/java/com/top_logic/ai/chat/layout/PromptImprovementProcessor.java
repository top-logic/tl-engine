/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.top_logic.ai.chat.layout.TaskProgressTracker.PromptImprovementState;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.TLContext;

/**
 * Runs prompt improvement independently from the main chat conversation.
 */
public final class PromptImprovementProcessor {

	/** Maximum accepted draft length in UTF-16 characters. */
	public static final int MAX_DRAFT_LENGTH = 20_000;

	/** The prompt was improved successfully. */
	public static final String STATUS_COMPLETED = "completed";

	/** The prompt-improvement request was cancelled. */
	public static final String STATUS_CANCELLED = "cancelled";

	/** A newer prompt-improvement request replaced this request. */
	public static final String STATUS_STALE = "stale";

	/** Longest first line still treated as a lead-in rather than as part of the prompt. */
	private static final int MAX_LEAD_IN_CHARS = 80;

	/** How often wrapper removal is retried; a lead-in plus a fence needs two passes. */
	private static final int MAX_WRAPPER_PASSES = 3;

	/**
	 * Longest draft still considered "very short", below which a rewrite may not balloon.
	 */
	private static final int SHORT_DRAFT_CHARS = 40;

	/** How much longer than a very short draft its rewrite may be. */
	private static final int MAX_SHORT_GROWTH = 3;

	/**
	 * Length a rewrite of a very short draft may always reach, whatever the ratio says.
	 *
	 * <p>
	 * Three words turned into one clear sentence is the point of the feature and can easily be five
	 * times as long; the ratio alone would refuse it. What the ratio is there to catch is an essay,
	 * and an essay is longer than this either way.
	 * </p>
	 */
	private static final int SHORT_REWRITE_CEILING = 120;

	/**
	 * How many words a draft needs before the whole {@link #SHORT_REWRITE_CEILING} is granted.
	 *
	 * <p>
	 * The ceiling exists so a handful of words may become one clear sentence. A single word cannot
	 * be clarified, only replaced: there is nothing in it to make precise, so anything the improver
	 * adds it invented. The allowance therefore grows with the draft - a word at a time - and only a
	 * draft of this many words gets the full ceiling.
	 * </p>
	 */
	private static final int WORDS_FOR_FULL_CEILING = 4;

	/**
	 * How many words a draft needs before a faithful rewrite may outgrow its share of the ceiling.
	 *
	 * <p>
	 * One word names a subject and asks nothing, so there is no instruction in it that a rewrite
	 * could be faithful to: "everything the draft said survives" is true of any sentence built
	 * around that word, which is exactly how {@code "Belege"} became "evaluate the receipts and
	 * summarize the results". From two words on the draft says something, and a rewrite can be held
	 * to it.
	 * </p>
	 */
	private static final int MIN_WORDS_TO_CLARIFY = 2;

	/**
	 * How much of a word has to reappear for it to count as still being there.
	 *
	 * <p>
	 * A word is matched by stem, not literally: German inflects and splits, so {@code "auswerten"}
	 * comes back as "Werte ... aus" with nothing of it left whole. Either form may be the longer
	 * one, so containment is tested both ways - with this floor on the shorter side, so that a
	 * particle cannot vouch for a word it has nothing to do with.
	 * </p>
	 */
	private static final int STEM_CHARS = 4;

	/** A sentence end followed by more text. */
	private static final Pattern SENTENCE_BREAK = Pattern.compile("[.!?]\\s");

	/** Runs of anything that is not part of a word. */
	private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{N}]+");

	/** Shortest token still carrying meaning of its own; below this a word is grammar. */
	private static final int CONTENT_WORD_CHARS = 3;

	/**
	 * Longest a faithful rewrite of a very short draft may be.
	 *
	 * <p>
	 * Tighter than {@link #SHORT_REWRITE_CEILING}, which is the bound for drafts that already say a
	 * lot. A clarification of two or three words is one short sentence; past this the rewrite is
	 * carrying requirements the draft never gave, however carefully it repeats the words it was
	 * given - {@code "offene rechnungen"} came back with a supplier column and an overdue filter,
	 * with both of its own words intact, in a single sentence of 119 characters.
	 * </p>
	 */
	private static final int FAITHFUL_REWRITE_CEILING = 90;

	/**
	 * Phrases that only occur when the reply is about the prompt instead of being one.
	 *
	 * <p>
	 * English and German, lower case, matched anywhere. A genuine prompt can of course contain
	 * "your" or "ich" - what it does not contain is these pairings, which belong to an assistant
	 * addressing its user.
	 * </p>
	 */
	private static final String[] META_PHRASES = {
		"your message", "your prompt", "your draft", "your request",
		"could you", "can you provide", "please provide", "please specify", "please share",
		"i can help", "i'd be happy", "i would be happy", "it looks like", "it seems like",
		"here is the", "here's the", "improved prompt", "improved version",
		"deine nachricht", "deine anfrage", "dein prompt", "ihre nachricht", "ihre anfrage",
		"ihr prompt", "bitte gib an", "bitte geben sie an", "bitte teile mit",
		"kannst du mir", "k\u00f6nnen sie mir", "ich kann dir", "ich kann ihnen",
		"verbesserter prompt", "hier ist der",
	};

	/** Quotation-mark pairs stripped when they enclose the whole answer. */
	private static final String[] QUOTE_PAIRS = { "\"\"", "\u201c\u201d", "\u00ab\u00bb", "\u201e\u201c", "''" };

	private PromptImprovementProcessor() {
		// Static utility.
	}

	/**
	 * Improves the given draft with the agent configured on the tracker.
	 *
	 * <p>
	 * The run gets a fresh {@link Conversation}, ephemeral environment state and no
	 * {@link com.top_logic.tl.ai.core.execution.model.AgentSession}. It therefore cannot append to
	 * the chat transcript or memory, emit chat SSE events, register a main processing state, or
	 * create an agent execution.
	 * </p>
	 *
	 * @throws PromptImprovementException
	 *         If the request or the administrator configuration is invalid.
	 */
	public static Result improve(TaskProgressTracker tracker, String requestId, String draft) {
		return improve(tracker, requestId, draft,
			name -> AgentDefinitionQuery.getInstance().findAgentByName(name) != null,
			PromptImprovementProcessor::runAgent);
	}

	static Result improve(TaskProgressTracker tracker, String requestId, String draft,
			Predicate<String> agentExists, AgentInvocation invocation) {
		String normalizedRequestId = requireRequestId(requestId);
		String normalizedDraft = requireDraft(draft);
		String agentName = configuredAgent(tracker);
		if (!agentExists.test(agentName)) {
			throw new PromptImprovementException(Failure.MISSING_AGENT,
				"Configured prompt-improver agent '" + agentName + "' does not exist.");
		}

		CancellationToken cancellationToken = new CancellationToken();
		PromptImprovementState state =
			new PromptImprovementState(normalizedRequestId, cancellationToken);
		PromptImprovementState previous = tracker.replacePromptImprovementState(state);
		if (previous != null) {
			previous.getCancellationToken().cancel();
		}

		try {
			String improved = invocation.run(agentName, normalizedDraft, cancellationToken);
			if (tracker.getPromptImprovementState() != state) {
				return new Result(normalizedRequestId, "", STATUS_STALE);
			}
			if (cancellationToken.isCancelled()) {
				return new Result(normalizedRequestId, "", STATUS_CANCELLED);
			}
			if (improved == null || improved.isBlank()) {
				throw new IllegalStateException(
					"Prompt-improver agent '" + agentName + "' returned no improved prompt.");
			}
			String cleaned = stripWrapper(improved);
			if (cleaned.isEmpty()) {
				throw new IllegalStateException(
					"Prompt-improver agent '" + agentName + "' returned only a wrapper.");
			}
			return new Result(normalizedRequestId, keepOrFallBack(normalizedDraft, cleaned),
				STATUS_COMPLETED);
		} catch (CancellationException ex) {
			return cancelledOrStale(tracker, state);
		} catch (RuntimeException ex) {
			if (cancellationToken.isCancelled()) {
				return cancelledOrStale(tracker, state);
			}
			throw ex;
		} finally {
			tracker.clearPromptImprovementState(state);
		}
	}

	/**
	 * Returns the rewrite, or the draft when the rewrite is not one.
	 *
	 * <p>
	 * A prompt improver has exactly one failure mode that matters: instead of rewriting the draft it
	 * starts talking to the user - asking what they meant, explaining what it would need, offering
	 * to help. Pasting that into the input box replaces the user's question with a machine's request
	 * for clarification, which is worse than doing nothing. Three shapes are refused, and refusing
	 * means handing back the draft unchanged so the UI can say "no changes":
	 * </p>
	 *
	 * <ul>
	 * <li>a reply that addresses the user - a first- or second-person meta phrase such as "your
	 * prompt", "could you", "I can";</li>
	 * <li>a question the draft did not ask, which is what a request for clarification looks like;</li>
	 * <li>a reply several times longer than a very short draft, which is an essay grown from three
	 * words rather than a rewrite of them - and the shorter the draft, the less room its rewrite
	 * gets, because a one-word draft has nothing in it to clarify.</li>
	 * </ul>
	 *
	 * <p>
	 * The checks are deliberately blunt. A false refusal costs the user nothing - they keep the
	 * text they wrote - while a false acceptance costs them their prompt.
	 * </p>
	 */
	static String keepOrFallBack(String draft, String improved) {
		if (improved.equalsIgnoreCase(draft)) {
			return draft;
		}
		String lower = improved.toLowerCase(Locale.ROOT);
		for (String phrase : META_PHRASES) {
			if (lower.contains(phrase)) {
				return draft;
			}
		}
		if (improved.indexOf('?') >= 0 && draft.indexOf('?') < 0) {
			return draft;
		}
		if (draft.length() <= SHORT_DRAFT_CHARS
			&& improved.length() > Math.max(draft.length() * MAX_SHORT_GROWTH, shortRewriteCeiling(draft))
			&& !saysTheSameThing(draft, improved)) {
			return draft;
		}
		return improved;
	}

	/**
	 * Whether the rewrite is one sentence in which everything the draft said is still there.
	 *
	 * <p>
	 * The per-word allowance is what keeps a single word from becoming an instruction, but it also
	 * suppressed {@code "belege auswerten"} - a real request, whose rewrite "Werte die vorhandenen
	 * Belege aus und fasse die Ergebnisse zusammen." the first review recorded as good. Two words
	 * buying a 60-character ceiling is an accident of counting words, not a judgement about the
	 * rewrite. So a rewrite that keeps every content word of the draft and says it in a single
	 * sentence is let through on its own terms, no matter how many words the draft had - bounded
	 * only by {@link #FAITHFUL_REWRITE_CEILING}, which is what separates a clarifying sentence from
	 * a briefing.
	 * </p>
	 */
	private static boolean saysTheSameThing(String draft, String improved) {
		if (wordCount(draft) < MIN_WORDS_TO_CLARIFY) {
			return false;
		}
		if (improved.length() > FAITHFUL_REWRITE_CEILING) {
			return false;
		}
		if (SENTENCE_BREAK.matcher(improved.trim().replaceAll("[.!?\\s]+$", "")).find()) {
			return false;
		}
		List<String> rewritten = words(improved);
		for (String word : words(draft)) {
			if (word.length() < CONTENT_WORD_CHARS) {
				continue;
			}
			if (!survives(word, rewritten)) {
				return false;
			}
		}
		return true;
	}

	/** Whether a word of the draft is still recognisable somewhere in the rewrite. */
	private static boolean survives(String word, List<String> rewritten) {
		for (String other : rewritten) {
			if (other.length() >= STEM_CHARS && word.contains(other)) {
				return true;
			}
			if (word.length() >= STEM_CHARS && other.contains(word)) {
				return true;
			}
		}
		return false;
	}

	/** The words of a text, lower cased, with the punctuation between them dropped. */
	private static List<String> words(String text) {
		return NON_WORD.splitAsStream(text.toLowerCase(Locale.ROOT))
			.filter(word -> !word.isEmpty())
			.toList();
	}

	/**
	 * The absolute length a rewrite of this draft may reach, whatever the ratio allows.
	 *
	 * <p>
	 * Proportional to how much the draft actually says: a quarter of the ceiling per word, up to
	 * the full {@link #SHORT_REWRITE_CEILING} from {@link #WORDS_FOR_FULL_CEILING} words on. This is
	 * what keeps {@code "Belege"} from coming back as {@code "Werte die vorhandenen Belege aus und
	 * fasse die Ergebnisse zusammen."} - an instruction the user never gave.
	 * </p>
	 */
	private static int shortRewriteCeiling(String draft) {
		int words = wordCount(draft);
		if (words >= WORDS_FOR_FULL_CEILING) {
			return SHORT_REWRITE_CEILING;
		}
		return Math.max(1, words) * SHORT_REWRITE_CEILING / WORDS_FOR_FULL_CEILING;
	}

	/** How many words the draft is made of; anything between whitespace counts as one. */
	private static int wordCount(String draft) {
		String trimmed = draft.trim();
		return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
	}

	/**
	 * Removes the wrapper a model may add around a rewritten prompt, whatever the system prompt
	 * forbids.
	 *
	 * <p>
	 * Three shapes are removed, each only where it is unambiguous: a lead-in line that announces the
	 * result ("Here is the improved prompt:"), a markdown fence around the <em>whole</em> answer, and
	 * one pair of quotation marks around the whole answer. Every rule is deliberately narrow, because
	 * a rewritten prompt may legitimately contain a fenced code block, a colon-terminated line, or
	 * quoted text - only a construct that encloses everything is a wrapper.
	 * </p>
	 */
	static String stripWrapper(String text) {
		if (text == null) {
			return "";
		}
		String result = text.strip();
		// Repeated, because a model that adds a lead-in often also fences what follows.
		for (int pass = 0; pass < MAX_WRAPPER_PASSES; pass++) {
			String before = result;
			result = stripLeadIn(result);
			result = stripFence(result);
			result = stripQuotes(result);
			if (result.equals(before)) {
				break;
			}
		}
		return result;
	}

	/**
	 * Drops a first line that only announces the result.
	 *
	 * <p>
	 * Recognised by shape rather than by wording, so it holds in any language: a short line that ends
	 * in a colon and is followed by a blank line and further content. A prompt whose own first line
	 * ends in a colon (a heading the user wrote) is kept, because the rewritten prompt would then
	 * have no blank line separating it.
	 * </p>
	 */
	private static String stripLeadIn(String text) {
		int firstBreak = text.indexOf('\n');
		if (firstBreak < 0) {
			return text;
		}
		String firstLine = text.substring(0, firstBreak).strip();
		if (firstLine.length() > MAX_LEAD_IN_CHARS || !firstLine.endsWith(":")) {
			return text;
		}
		String rest = text.substring(firstBreak + 1);
		if (!rest.startsWith("\n") && !rest.startsWith("\r\n")) {
			return text;
		}
		String body = rest.strip();
		return body.isEmpty() ? text : body;
	}

	/** Unwraps a markdown fence that encloses the whole answer. */
	private static String stripFence(String text) {
		if (!text.startsWith("```") || !text.endsWith("```") || text.length() < 7) {
			return text;
		}
		// A single opening and a single closing fence; anything else is content with its own fences.
		if (countFenceLines(text) != 2) {
			return text;
		}
		int firstBreak = text.indexOf('\n');
		if (firstBreak < 0) {
			return text;
		}
		// An empty body is returned as such: the caller rejects an answer that was only a wrapper,
		// which is better than handing the user back the fence as if it were a prompt.
		return text.substring(firstBreak + 1, text.lastIndexOf("```")).strip();
	}

	private static int countFenceLines(String text) {
		int count = 0;
		for (String line : text.split("\n", -1)) {
			if (line.strip().startsWith("```")) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Removes one pair of quotation marks around the whole answer, but only when that pair is the
	 * only one present - otherwise the quotes belong to the prompt.
	 */
	private static String stripQuotes(String text) {
		for (String pair : QUOTE_PAIRS) {
			char open = pair.charAt(0);
			char close = pair.charAt(1);
			if (text.length() < 2 || text.charAt(0) != open || text.charAt(text.length() - 1) != close) {
				continue;
			}
			// The pair must be the only one in the answer; otherwise the quotes are the prompt's own.
			// A symmetric pair (") is one character used twice, an asymmetric one two characters once.
			boolean onlyPair = open == close
				? count(text, open) == 2
				: count(text, open) == 1 && count(text, close) == 1;
			if (!onlyPair) {
				continue;
			}
			return text.substring(1, text.length() - 1).strip();
		}
		return text;
	}

	private static int count(String text, char c) {
		int count = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == c) {
				count++;
			}
		}
		return count;
	}

	private static Result cancelledOrStale(TaskProgressTracker tracker, PromptImprovementState state) {
		String status = tracker.getPromptImprovementState() == state ? STATUS_CANCELLED : STATUS_STALE;
		return new Result(state.getRequestId(), "", status);
	}

	private static String requireRequestId(String requestId) {
		if (requestId == null || requestId.isBlank()) {
			throw new PromptImprovementException(Failure.INVALID_REQUEST,
				"Missing or empty 'requestId'.");
		}
		return requestId.trim();
	}

	private static String requireDraft(String draft) {
		if (draft == null || draft.isBlank()) {
			throw new PromptImprovementException(Failure.INVALID_REQUEST,
				"Missing or empty 'draft'.");
		}
		if (draft.length() > MAX_DRAFT_LENGTH) {
			throw new PromptImprovementException(Failure.INVALID_REQUEST,
				"Draft is too long (max " + MAX_DRAFT_LENGTH + " characters).");
		}
		return draft.strip();
	}

	private static String configuredAgent(TaskProgressTracker tracker) {
		String agentName = tracker.getPromptImproverAgentName();
		if (agentName == null || agentName.isBlank()) {
			throw new PromptImprovementException(Failure.DISABLED,
				"Prompt improvement is not enabled for this chat.");
		}
		return agentName.trim();
	}

	private static String runAgent(String agentName, String draft, CancellationToken cancellationToken) {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent(draft))));

		ConversationLog ephemeralLog = new ConversationLog();
		ConversationLogListener logListener = new ConversationLogListener(ephemeralLog);
		TaskStore taskStore = new TaskStore();
		UserInteractionStore interactionStore = new UserInteractionStore();
		try (TransactionScope transaction = new TransactionScope(cancellationToken);
				AgentEnvironment environment = AgentEnvironment.open(logListener, taskStore,
					interactionStore, Collections.synchronizedSet(new LinkedHashSet<>()),
					cancellationToken)) {
			// withoutAgentChoice: the user did not pick this agent and cannot pick another. The agent
			// allowlist decides which persona answers them, and the improver never answers - it
			// rewrites the draft they are about to send. Gating it would make an administrator grant
			// an agent the picker deliberately hides, and the button would break for every restricted
			// role with nothing on screen to say why. Everything else about this run stays decided.
			ToolContext context = new ToolContext(
				RunSecurity.contextFor(
					new RunPrincipal(TLContext.currentUser(), RunEntryPoint.INTERNAL, false))
					.withoutAgentChoice(),
				ApprovalCallback.AUTO_APPROVE, transaction, cancellationToken);
			return AgentRunner.run(agentName, conversation, null, context, environment.logListener(),
				null, ephemeralLog, null, null).response();
		}
	}

	/**
	 * Outcome of one prompt-improvement request.
	 *
	 * @param requestId
	 *        Correlation ID supplied by the browser.
	 * @param improvedText
	 *        Improved prompt, or an empty string when no result must be applied.
	 * @param status
	 *        One of {@link #STATUS_COMPLETED}, {@link #STATUS_CANCELLED}, or
	 *        {@link #STATUS_STALE}.
	 */
	public record Result(String requestId, String improvedText, String status) {
		// Pure data.
	}

	@FunctionalInterface
	interface AgentInvocation {
		String run(String agentName, String draft, CancellationToken cancellationToken);
	}

	/** Request or configuration failure category. */
	public enum Failure {
		/** The browser request is malformed. */
		INVALID_REQUEST,

		/** Prompt improvement is not configured. */
		DISABLED,

		/** The configured agent does not exist. */
		MISSING_AGENT
	}

	/**
	 * Reports a rejected prompt-improvement request.
	 */
	public static final class PromptImprovementException extends RuntimeException {

		private final Failure _failure;

		PromptImprovementException(Failure failure, String message) {
			super(message);
			_failure = failure;
		}

		/** Returns the failure category. */
		public Failure getFailure() {
			return _failure;
		}
	}
}
