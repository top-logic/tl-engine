/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.approval.InteractionTimeoutService;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.event.DiagnosticLoggingListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInbox;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInboxBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentNews;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResultDelivery;
import com.top_logic.tl.ai.core.binding.BindingSnapshot;
import com.top_logic.tl.ai.core.citation.PageArchive;
import com.top_logic.tl.ai.core.citation.SeenUrls;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.cost.RunCosts;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.InteractionRecorder;
import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.memory.CompactionStrategy;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.SummarizingStrategy;
import com.top_logic.tl.ai.core.memory.TokenEstimator;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.observe.RunObservers;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.MessageHistorySanitizer;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.health.ModelAvailability;
import com.top_logic.tl.ai.core.provider.health.ModelHealthMark;
import com.top_logic.tl.ai.core.provider.health.ModelOptions;
import com.top_logic.tl.ai.core.provider.health.ModelTarget;
import com.top_logic.tl.ai.core.security.PreCommitGuard;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolExecutions;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.memory.MemoryConsent;
import com.top_logic.tl.ai.core.tools.memory.MemoryConsentBinding;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ProgressReporter;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;

/**
 * Core agent loop: calls the LLM, executes tool calls, feeds results back, repeats.
 *
 * <p>Blocks the calling thread for the entire run. Progress is reported via
 * {@link AgentEventListener} callbacks on the same thread.</p>
 *
 * <p>One iteration:</p>
 * <ol>
 *   <li>Build {@link ProviderRequest} from conversation + config</li>
 *   <li>Call {@link LLMProvider#stream} — streams via internal handler</li>
 *   <li>Assemble {@link AssistantMessage}, add to conversation</li>
 *   <li>Extract {@link ToolCall}s — if none, stop</li>
 *   <li>Security check via {@link SecurityContext#hasToolPermission}</li>
 *   <li>Partition into concurrent/serial batches, execute</li>
 *   <li>Truncate oversized results, add to conversation</li>
 *   <li>Loop</li>
 * </ol>
 *
 * @see AgentParams
 * @see AgentResult
 * @see AgentEventListener
 */
public class AgentLoop {

	/** Default maximum characters per tool result before truncation. */
	public static final int DEFAULT_MAX_RESULT_CHARS = AgentTuning.MAX_TOOL_RESULT_CHARS;

	/**
	 * What the run is currently working with.
	 *
	 * <p>
	 * Not final because a run may move to another model when the one it was configured with cannot
	 * answer - see {@link #callRouted}. Reset to {@link #_configuredParams} at the start of every
	 * run, so a loop that is used again starts where it was configured, not where its last run
	 * ended up.
	 * </p>
	 */
	private volatile AgentParams _config;

	/** The provider serving {@link #_config}; switched with it. */
	private volatile LLMProvider _provider;

	/** What this loop was built with, whatever a failover moved the run to. */
	private final AgentParams _configuredParams;

	/** @see #_configuredParams */
	private final LLMProvider _configuredProvider;

	/**
	 * The models this run may use, in order; index 0 is the one it was configured with.
	 *
	 * <p>
	 * A list of one - the default, because the {@code <failover>} section is empty unless an
	 * installation writes one - means nothing about this run changes.
	 * </p>
	 */
	private volatile List<ModelTarget> _failoverChain = List.of();

	/** The entry of {@link #_failoverChain} the run is currently trying. */
	private volatile int _chainIndex;

	/** The entry of {@link #_failoverChain} {@link #_config} and {@link #_provider} belong to. */
	private volatile int _activeIndex;

	/** The configured provider entry serving {@link #_config}, or <code>null</code>. */
	private volatile String _activeProviderName;

	/**
	 * Every model this run passed over, one line each, in the order it passed them.
	 *
	 * <p>
	 * Kept for the whole run rather than per switch: each
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ModelSwitched} names what was skipped
	 * on the way to it, and a run that ran out of chain names all of them in the error it fails
	 * with - which is the whole point of a chain that otherwise continues somewhere else without
	 * saying so.
	 * </p>
	 */
	private final List<String> _skipped = new CopyOnWriteArrayList<>();

	/**
	 * How many entries of {@link #_skipped} a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ModelSwitched} has already named.
	 */
	private volatile int _announcedSkips;

	private final ToolRegistry _registry;

	private final int _maxResultChars;

	private final ConversationMemory _memory;

	private final Set<String> _sessionApprovedTools = ConcurrentHashMap.newKeySet();

	/** Tool call IDs denied in the current turn; cleared at the start of each turn's tool execution. */
	private final Set<String> _turnDeniedCallIds = ConcurrentHashMap.newKeySet();

	/**
	 * Tool call IDs of the current turn whose
	 * {@link com.top_logic.tl.ai.core.message.ToolState.Failed} result is synthetic — the tool
	 * never executed (permission denied, approval rejected, duplicate blocked, unknown tool). These
	 * failures must not trigger the end-of-turn rollback: they cannot have corrupted any data, and
	 * rolling back would silently discard writes of tools that succeeded in the same turn.
	 */
	private final Set<String> _turnBlockedCallIds = ConcurrentHashMap.newKeySet();

	/**
	 * Whether a call of the current turn has run. Until one has, the turn may still move its view of
	 * the database to the newest commit - see {@link #catchUpWithOtherCommits()}; from then on the
	 * calls of the turn read one state.
	 */
	private volatile boolean _turnCallRan;

	/**
	 * Signature ({@code name + "\0" + arguments}) of the most recent tool call, for
	 * consecutive-duplicate detection. Reset whenever a different tool call is made.
	 */
	private String _lastToolCallSignature;

	/** Consecutive repeats of {@link #_lastToolCallSignature}. */
	private int _lastToolCallRepeats;

	/**
	 * Signature of the tool call before {@link #_lastToolCallSignature}, for period-2
	 * oscillation detection (e.g. a model stuck alternating A, B, A, B, ... — each call
	 * differs from its immediate predecessor, so {@link #_lastToolCallRepeats} alone never
	 * catches it).
	 */
	private String _secondLastToolCallSignature;

	/** Consecutive A/B alternations matching {@link #_secondLastToolCallSignature}. */
	private int _alternationRepeats;

	/**
	 * Whether the request being built is the run's last one: the tools stay in the request, so that
	 * the cached prefix stays the same, but the model may call none of them.
	 */
	private boolean _finalTurn;

	/** Maximum consecutive times the same tool+args combination may be called before being blocked. */
	private static final int MAX_DUPLICATE_TOOL_CALLS = AgentTuning.MAX_DUPLICATE_TOOL_CALLS;

	/**
	 * Estimator behind {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ContextComposition}.
	 *
	 * <p>
	 * Stateless, and deliberately the same heuristic {@link ConversationMemory} budgets with, so
	 * the breakdown a UI shows and the number that decides compaction cannot disagree about what a
	 * message costs.
	 * </p>
	 */
	private static final TokenEstimator CONTEXT_ESTIMATOR = new ByteBasedTokenEstimator();

	/**
	 * Creates a new agent loop with default result truncation.
	 *
	 * @param config   loop configuration
	 * @param provider LLM provider for streaming responses
	 * @param registry tool registry for tool lookup and execution
	 */
	public AgentLoop(AgentParams config, LLMProvider provider, ToolRegistry registry) {
		this(config, provider, registry, DEFAULT_MAX_RESULT_CHARS);
	}

	/**
	 * Creates a new agent loop with configurable result truncation.
	 *
	 * @param config         loop configuration
	 * @param provider       LLM provider for streaming responses
	 * @param registry       tool registry for tool lookup and execution
	 * @param maxResultChars maximum characters per tool result before truncation
	 */
	public AgentLoop(AgentParams config, LLMProvider provider, ToolRegistry registry,
			int maxResultChars) {
		_config = Objects.requireNonNull(config, "Config must not be null.");
		_provider = Objects.requireNonNull(provider, "Provider must not be null.");
		_configuredParams = _config;
		_configuredProvider = _provider;
		_registry = Objects.requireNonNull(registry, "Registry must not be null.");
		if (maxResultChars <= 0) {
			throw new IllegalArgumentException("maxResultChars must be positive.");
		}
		_maxResultChars = maxResultChars;
		_memory = null;
	}

	/**
	 * Creates a new agent loop with memory-aware context management.
	 *
	 * @param config   loop configuration
	 * @param provider LLM provider for streaming responses
	 * @param registry tool registry for tool lookup and execution
	 * @param memory   conversation memory with token tracking and compaction
	 */
	public AgentLoop(AgentParams config, LLMProvider provider, ToolRegistry registry,
			ConversationMemory memory) {
		this(config, provider, registry, memory, DEFAULT_MAX_RESULT_CHARS);
	}

	/**
	 * Creates a new agent loop with memory-aware context management and configurable result
	 * truncation.
	 *
	 * @param config           loop configuration
	 * @param provider         LLM provider for streaming responses
	 * @param registry         tool registry for tool lookup and execution
	 * @param memory           conversation memory with token tracking and compaction
	 * @param maxResultChars   maximum characters per tool result before truncation
	 */
	public AgentLoop(AgentParams config, LLMProvider provider, ToolRegistry registry,
			ConversationMemory memory, int maxResultChars) {
		_config = Objects.requireNonNull(config, "Config must not be null.");
		_provider = Objects.requireNonNull(provider, "Provider must not be null.");
		_configuredParams = _config;
		_configuredProvider = _provider;
		_registry = Objects.requireNonNull(registry, "Registry must not be null.");
		_memory = Objects.requireNonNull(memory, "Memory must not be null.");
		if (maxResultChars <= 0) {
			throw new IllegalArgumentException("maxResultChars must be positive.");
		}
		_maxResultChars = maxResultChars;
	}

	private static Instant now() {
		return Instant.now();
	}

	private static void emit(AgentEventListener listener, AgentEvent event) {
		listener.onEvent(event);
	}

	/**
	 * Appends any completed-but-undelivered subagent results to the conversation.
	 *
	 * <p>Called at the start of every turn, i.e. after the previous turn's tool results were
	 * appended and before the next provider request is built.</p>
	 */
	private void injectBetweenTurns(Conversation conversation, ToolContext context,
			AgentEventListener listener, String runId, int turn) {
		injectSubagentResults(conversation, context);
		injectInboxMessages(conversation, listener, runId, turn);
		recordGivenAddresses(conversation, context.readingList().seenUrls());
	}

	/**
	 * Records the web addresses that the run's user or lead wrote, for the URL provenance rule.
	 *
	 * <p>
	 * Every message in the user's role is read once, at the start of the turn after it arrived - the
	 * user's message, a background subagent's task, a message its lead or the user sent it. What the
	 * framework writes into that role is not read: a subagent's report repeats what the subagent read,
	 * and a summary of compacted history is the model's own text, and neither is an address somebody
	 * gave this run.
	 * </p>
	 *
	 * @see SeenUrls
	 */
	static void recordGivenAddresses(Conversation conversation, SeenUrls seen) {
		for (Message message : conversation.messages()) {
			if (message instanceof UserMessage user && seen.firstLook(user.id()) && givenByUserOrLead(user)) {
				for (ContentPart part : user.content()) {
					if (part instanceof TextContent text) {
						seen.addAllIn(text.text());
					}
				}
			}
		}
	}

	private static boolean givenByUserOrLead(UserMessage message) {
		// A summary restored from a compaction sidecar written before messages recorded their origin
		// reads back as the user's; its text still tells it apart.
		return message.origin() != UserMessage.Origin.FRAMEWORK && !SummarizingStrategy.isSummary(message);
	}

	/**
	 * Opening line of what a background subagent's model is shown when its lead sent it messages.
	 *
	 * <p>
	 * The messages arrive in the user's role, for the reason {@link #injectSubagentResults} gives, but
	 * the user did not write them: the agent that started this run did, and the run is its worker.
	 * </p>
	 */
	static final String LEAD_MESSAGE_NOTICE = "[Message from the lead agent] The agent that started you"
		+ " sends the following while you work. It is not the user. Take it into account for the rest"
		+ " of your task:";

	/**
	 * Opening line of what a background subagent's model is shown when the user wrote to it directly.
	 *
	 * <p>
	 * The user role alone would not say it: the run's first message is the lead's task, also in the
	 * user role. This line says that these words are the user's own.
	 * </p>
	 */
	static final String USER_MESSAGE_NOTICE = "[Message from the user] The user you are working for writes"
		+ " to you directly while you work. Take it into account for the rest of your task:";

	/**
	 * Hands a background subagent what its lead and the user sent it since its last turn, before the
	 * next request is built.
	 *
	 * <p>
	 * Only a run that reads an {@link SubagentInbox} - a background subagent, whose thread the spawner
	 * binds it on - gets anything here. The lead's messages become one user message, in the order they
	 * were sent, announced as a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.LeadMessageDelivered} event. The user's
	 * become one user message after it - the user has the last word - announced as a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.UserMessageDelivered} event, which tells
	 * the chat which of its messages were read. The events are what record the messages in the
	 * subagent's own log.
	 * </p>
	 */
	static void injectInboxMessages(Conversation conversation, AgentEventListener listener, String runId,
			int turn) {
		SubagentInbox inbox = SubagentInboxBinding.getInbox();
		if (inbox == null) {
			return;
		}
		List<SubagentInbox.Message> messages = inbox.drain();
		List<SubagentInbox.Message> fromLead = new ArrayList<>();
		List<SubagentInbox.Message> fromUser = new ArrayList<>();
		for (SubagentInbox.Message message : messages) {
			(message.sender() == SubagentInbox.Sender.USER ? fromUser : fromLead).add(message);
		}
		if (!fromLead.isEmpty()) {
			String text = inboxText(LEAD_MESSAGE_NOTICE, fromLead);
			conversation.add(userMessage(text, UserMessage.Origin.AGENT));
			emit(listener, new AgentEvent.LeadMessageDelivered(runId, turn, now(), text));
		}
		if (!fromUser.isEmpty()) {
			String text = inboxText(USER_MESSAGE_NOTICE, fromUser);
			conversation.add(userMessage(text, UserMessage.Origin.USER));
			emit(listener, new AgentEvent.UserMessageDelivered(runId, turn, now(), text, List.copyOf(fromUser)));
		}
	}

	/**
	 * Whether a background subagent that is about to end has messages it has not read, and therefore
	 * takes one more turn, in which {@link #injectInboxMessages} hands them over.
	 *
	 * <p>
	 * When none waits, its inbox is closed in the same step, so a message sent while the subagent wrote
	 * its answer is either read in the next turn or refused - never accepted and then left unread. A run
	 * without an inbox - every run but a background subagent's - ends as before.
	 * </p>
	 */
	private static boolean messagesArrivedDuringLastTurn(String runId, int turn) {
		SubagentInbox inbox = SubagentInboxBinding.getInbox();
		if (inbox == null || inbox.closeIfEmpty()) {
			return false;
		}
		Logger.info("Run " + runId + " turn " + turn + ": messages arrived while the subagent wrote its answer;"
			+ " it takes one more turn to read them.", AgentLoop.class);
		return true;
	}

	private static String inboxText(String notice, List<SubagentInbox.Message> messages) {
		StringBuilder text = new StringBuilder(notice);
		for (SubagentInbox.Message message : messages) {
			text.append("\n\n").append(message.text());
		}
		return text.toString();
	}

	private static UserMessage userMessage(String text, UserMessage.Origin origin) {
		return new UserMessage(UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(text)),
			origin);
	}

	/**
	 * Delivers the results of every completed-but-undelivered subagent of the thread-bound registry
	 * to the model, as the last message before the next provider request is built.
	 *
	 * <p>
	 * The delivery is a {@link UserMessage}, and that is the whole point of #359: this used to be a
	 * mid-conversation {@link com.top_logic.tl.ai.core.message.SystemMessage}, which <em>no</em>
	 * provider's request builder transmits, so the parent was never told its subagent had finished
	 * while the transcript and the token estimate both claimed it had been. A user message is the
	 * one shape every builder sends, and it is the shape {@code SummarizingStrategy} and the
	 * rollback notice already use to put non-conversational text in front of the model.
	 * {@link SubagentResultDelivery#NOTICE} says in the text what the role no longer can: the user
	 * did not write this.
	 * </p>
	 *
	 * <p>
	 * A batch that arrives together is one message - see
	 * {@link SubagentResultDelivery#deliveryText(List)}. Nothing is injected when no subagent
	 * finished, so an ordinary turn is untouched. A result that becomes ready while no run is in
	 * progress simply waits in the registry: the next run's first turn picks it up, which is the
	 * turn that follows the user's next message.
	 * </p>
	 *
	 * <p>
	 * The same text is recorded on the current turn of the bound {@code ConversationLog} as a
	 * {@link com.top_logic.tl.ai.core.session.Part.SystemEventType#SUBAGENT_RESULT} part. That is
	 * what makes it visible in the transcript, and what lets {@code ConversationLogReplay} put the
	 * very same message back into a reloaded session - as a log part rather than a user part, so
	 * the chat does not draw it as a user bubble.
	 * </p>
	 *
	 * <p>
	 * Per-turn context (#438) has since landed, and this delivery stayed a message of its own:
	 * {@link #ensureTurnContext(Conversation)} prepends its block to the newest {@link UserMessage}
	 * instead of merging the two, and it runs later in the same turn than this does. On a turn that
	 * opens with a delivery - a subagent of an earlier run of the session finishing - the newest
	 * user message is therefore this one, and the block sits in front of the delivery text. Either
	 * way nothing here touches what the user wrote.
	 * </p>
	 */
	static void injectSubagentResults(Conversation conversation, ToolContext context) {
		injectSubagentNews(conversation, context);
	}

	/**
	 * Delivers what the subagents of the bound registry have to say, as
	 * {@link #injectSubagentResults(Conversation, ToolContext)} does, and says who said what.
	 *
	 * <p>
	 * Public for the chat's wake: a lead turn started because a subagent finished delivers before its
	 * run, so that the run does not start without the news it was started for, and names in the chat
	 * who reported back.
	 * </p>
	 *
	 * @return The news delivered: the ended subagents in the order they were started, then the asking
	 *         ones; empty when nothing was delivered.
	 */
	public static List<SubagentNews> injectSubagentNews(Conversation conversation, ToolContext context) {
		SubagentJobRegistry registry = SubagentRegistryBinding.getRegistry();
		if (registry == null) {
			return List.of();
		}
		List<SubagentJob> finished = registry.takeUndeliveredResults();
		// A subagent's question to this run travels the same way: it is news from a worker, it waits
		// in the same registry, and it is answered with reply_to_subagent.
		List<SubagentJob> asking = registry.takeUndeliveredQuestions();
		if (finished.isEmpty() && asking.isEmpty()) {
			return List.of();
		}
		if (context != null) {
			// The results are this run's from now on, and so is what they rest on.
			for (SubagentJob job : finished) {
				context.readingList().addAll(job.readingList());
			}
		}
		String text = SubagentResultDelivery.deliveryText(finished, asking);
		conversation.add(new UserMessage(
			UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(text)),
			UserMessage.Origin.FRAMEWORK));

		ConversationLogListener log = ConversationLogListenerBinding.getListener();
		if (log != null) {
			log.recordSubagentResultsDelivered(text);
		}
		List<SubagentNews> news = new ArrayList<>();
		for (SubagentJob job : finished) {
			news.add(SubagentNews.ended(job));
		}
		for (SubagentJob job : asking) {
			news.add(SubagentNews.asking(job));
		}
		return news;
	}

	/**
	 * Why a reply that called no tool is not the answer of the run, or <code>null</code> if it is.
	 *
	 * <p>
	 * A reply without a tool call used to end every run as {@link StopReason#COMPLETE}, whatever it
	 * held. A model that thinks and then stops - a local model, most of all - left the user a
	 * finished turn with nothing in it (#677). Text that was cut off at the output limit is not an
	 * answer the model chose to end either.
	 * </p>
	 *
	 * @return {@link StopReason#NO_ANSWER} for a reply without any text (only thinking, or nothing),
	 *         {@link StopReason#OUTPUT_LIMIT} for text cut off at the output limit.
	 */
	static StopReason unfinished(AssistantMessage reply) {
		boolean saysSomething = reply.content().stream()
			.anyMatch(part -> part instanceof TextContent text && !text.text().isBlank());
		if (!saysSomething) {
			return StopReason.NO_ANSWER;
		}
		if (FINISH_MAX_TOKENS.equals(reply.finishReason())) {
			return StopReason.OUTPUT_LIMIT;
		}
		return null;
	}

	/**
	 * Asks the model to go on after a reply that held no text and called no tool.
	 *
	 * <p>
	 * Recorded on the current turn of the bound {@code ConversationLog} like a subagent delivery, as
	 * a {@link com.top_logic.tl.ai.core.session.Part.SystemEventType#CONTINUATION_REQUESTED} part, so
	 * that a reloaded session puts the same message back between the two replies instead of merging
	 * them into one.
	 * </p>
	 */
	private static void askToContinue(Conversation conversation, AssistantMessage reply, String runId,
			int turn) {
		String text = FINISH_MAX_TOKENS.equals(reply.finishReason())
			? CONTINUE_NOTICE_OUTPUT_LIMIT
			: CONTINUE_NOTICE;
		Logger.info("Run " + runId + " turn " + turn + ": the reply held no text and no tool call"
			+ " (finish reason '" + reply.finishReason() + "'); asking the model to continue.",
			AgentLoop.class);
		conversation.add(new UserMessage(
			UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(text)),
			UserMessage.Origin.FRAMEWORK));

		ConversationLogListener log = ConversationLogListenerBinding.getListener();
		if (log != null) {
			log.recordContinuationRequested(text);
		}
	}

	/**
	 * Run the agent loop.
	 *
	 * <p>Blocks until the LLM completes without tool calls, the turn limit is reached,
	 * an unrecoverable error occurs, or the thread is interrupted.</p>
	 *
	 * @param conversation the conversation to operate on (messages are added during the run)
	 * @param context      execution context for the current user (security, transaction)
	 * @param listener     receives progress events
	 * @return the result describing why the loop stopped
	 */
	public AgentResult run(Conversation conversation, ToolContext context, AgentEventListener listener) {
		return run(conversation, (ConversationMemory) null, context, listener);
	}

	/**
	 * Run the agent loop on the given conversation, compacting it when it outgrows this loop's
	 * model.
	 *
	 * <p>
	 * For a run that owns its conversation but has no session memory to bring - a subagent, above
	 * all. Such a run used to go without any compaction: a worker whose tool results outgrew the
	 * model's window was refused by the provider and ended {@link StopReason#CONTEXT_OVERFLOW}. Here
	 * it gets a {@link ConversationMemory} over its conversation, sized to the context window of
	 * this loop's model, and with it the same checks a chat run has - the proactive one at the start
	 * of every turn and the reactive retry when a provider reports an overflow anyway.
	 * </p>
	 *
	 * @param conversation the conversation to operate on (messages are added and may be compacted)
	 * @param compaction   how the conversation is shortened when it no longer fits
	 * @param context      execution context for the current user (security, transaction)
	 * @param listener     receives progress events
	 * @return the result describing why the loop stopped
	 */
	public AgentResult run(Conversation conversation, CompactionStrategy compaction, ToolContext context,
			AgentEventListener listener) {
		Objects.requireNonNull(compaction, "Compaction must not be null.");
		ConversationMemory memory = new ConversationMemory(conversation, new ByteBasedTokenEstimator(),
			compaction, _config.contextWindowTokens(), ConversationMemory.DEFAULT_RESERVED_TOKENS);
		return run(conversation, memory, context, listener);
	}

	private AgentResult run(Conversation conversation, ConversationMemory memory, ToolContext context,
			AgentEventListener listener) {
		Objects.requireNonNull(conversation, "Conversation must not be null.");
		Objects.requireNonNull(context, "Context must not be null.");
		Objects.requireNonNull(listener, "Listener must not be null.");

		beginRun();

		ToolContext runContext = securedContext(context);

		_sessionApprovedTools.addAll(context.sessionApprovedTools());

		AgentEventListener serialized = AgentEventListener.serializing(
			new DiagnosticLoggingListener(RunObservers.observe(context, listener)));
		String runId = UUID.randomUUID().toString();
		// Publish the run's identity to its tools before anything of the run happens, and as plain
		// data: the persistent AgentExecution row is written later, by the execution store's writer
		// thread, and a tool's view of the database need not include it (Gitea #405). Done here
		// rather than in the ExecutionRecorder so that an unrecorded run - a test, a run without a
		// session - tells its tools which run they are in just the same.
		context.executionHandle().setRunId(runId);
		context.executionHandle().setModel(_config.model(), null);
		emit(serialized, new AgentEvent.RunStarted(runId, 0, now(), _config.model()));
		emitEnvironment(serialized, runId);
		emitTurnContext(serialized, runId);

		try (MemoryConsentBinding.Binding consent =
			MemoryConsentBinding.bindIfAbsent(MemoryConsent.forNewestTurn(conversation));
			// This run's token on its thread, for the waits of code that is handed no context; a
			// subagent's worker thread carries its lead's binding otherwise.
			RunCancellationBinding.Binding cancellation = RunCancellationBinding.bind(context.cancellationToken())) {
			return memory == null
				? runLoop(conversation, runContext, serialized, runId)
				: runWithMemory(memory, runContext, serialized, runId);
		} catch (RuntimeException e) {
			// Guarantee a terminal event so event consumers (e.g. SSE clients) are
			// never left waiting for a run that already died.
			Logger.error("Agent run " + runId + " failed unexpectedly: " + e.getMessage(), e, AgentLoop.class);
			emit(serialized, new AgentEvent.RunCompleted(runId, 0, now(), StopReason.ERROR, TokenUsage.EMPTY,
				RunCosts.zero()));
			throw e;
		} finally {
			// The archived copy of a document read in parts is extended once, here, rather than per
			// part; see PageArchive.
			PageArchive.flush(context.readingList());
		}
	}

	/**
	 * The context this run executes with: the caller's, bound to this loop's model.
	 *
	 * <p>
	 * What the run may do is decided by the context the caller built, which names the acting
	 * principal. A child run inherits it unchanged - see {@link ToolContext#forDelegation} and
	 * {@link ToolContext#forDetachedSubagent} - so no agent definition can widen its own reach.
	 * </p>
	 */
	private ToolContext securedContext(ToolContext context) {
		return context.withModel(_config.model());
	}

	/**
	 * Which provider's dialect this run's history is in, or <code>null</code> if that is not known.
	 *
	 * <p>
	 * Unknown is the safe answer: nothing provider-specific is replayed then. It is the answer
	 * whenever the model is not configured (a test wiring its own provider, for one) or resolves to
	 * more than one provider.
	 * </p>
	 */
	private String providerId() {
		if (!LLMProviderService.Module.INSTANCE.isActive()) {
			return null;
		}
		return LLMProviderService.getInstance().getProviderId(_config.model(), _activeProviderName);
	}

	/**
	 * Undoes what a failed run added to the conversation.
	 *
	 * <p>
	 * A run that dies in the provider leaves a half-written turn behind: an assistant message
	 * whose tool calls never got a result, or a tool result whose answer never came. Replayed on
	 * the next send, such a fragment is rejected by the provider as well, and the chat stays dead
	 * however often the user tries. Rolling back to where the run started leaves the history the
	 * user's next message can build on: everything that completed, plus the message that triggered
	 * the failed run - the chat adds that one before the run begins, so the user does not have to
	 * type it again.
	 * </p>
	 *
	 * @param resumePoint
	 *        the last message present when the run started, or <code>null</code> if the
	 *        conversation was empty then.
	 */
	private static void rollbackFailedRun(Conversation conversation, Message resumePoint) {
		List<Message> current = conversation.messages();
		int keep = 0;
		if (resumePoint != null) {
			keep = -1;
			for (int n = current.size() - 1; n >= 0; n--) {
				if (current.get(n) == resumePoint) {
					keep = n + 1;
					break;
				}
			}
			if (keep < 0) {
				// Compaction rewrote the history this run started from; what belongs to the run
				// can no longer be told apart from what was summarized into it.
				return;
			}
		}
		if (keep < current.size()) {
			conversation.replaceRange(keep, current.size(), List.of());
		}
	}

	private AgentResult runLoop(Conversation conversation, ToolContext context,
			AgentEventListener listener, String runId) {
		TokenUsage totalUsage = TokenUsage.EMPTY;
		Money totalCost = RunCosts.zero();
		int maxTurns = _config.maxTurns();
		List<Message> before = conversation.messages();
		Message resumePoint = before.isEmpty() ? null : before.get(before.size() - 1);
		int continuations = 0;

		for (int turn = 1; turn <= maxTurns; turn++) {
			if (Thread.interrupted() || context.cancellationToken().isCancelled()) {
				StopReason stopReason = StopReason.ABORTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn - 1, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn - 1, totalUsage);
			}

			emit(listener, new AgentEvent.TurnStarted(runId, turn, now()));

			injectBetweenTurns(conversation, context, listener, runId, turn);

			boolean lastTurn = announceFinalTurn(conversation, turn, maxTurns, listener, runId);

			int currentTurn = turn;
			TurnReply reply = requestReply(conversation, context, listener, runId, turn, lastTurn, totalUsage,
				totalCost, () -> callProvider(conversation, context, listener, runId, currentTurn), null);
			totalUsage = reply.totalUsage();
			totalCost = reply.totalCost();
			if (reply.failure() != null) {
				rollbackFailedRun(conversation, resumePoint);
				return endOnProviderFailure(reply.failure(), context, listener, runId, turn, totalUsage, totalCost);
			}
			AssistantMessage assistantMsg = reply.message();

			if (lastTurn) {
				return endAtLimit(assistantMsg, listener, runId, turn, totalUsage, totalCost);
			}

			if (!_config.responseValidator().test(assistantMsg)) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.REJECTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			List<ToolCall> toolCalls = assistantMsg.content().stream()
				.filter(ToolCall.class::isInstance)
				.map(ToolCall.class::cast)
				.toList();

			if (toolCalls.isEmpty()) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason unfinished = unfinished(assistantMsg);
				if (unfinished == StopReason.NO_ANSWER && continuations < MAX_EMPTY_REPLY_CONTINUATIONS
					&& turn < maxTurns) {
					continuations++;
					askToContinue(conversation, assistantMsg, runId, turn);
					continue;
				}
				if (turn < maxTurns && messagesArrivedDuringLastTurn(runId, turn)) {
					continue;
				}
				StopReason stopReason = unfinished != null ? unfinished : StopReason.COMPLETE;
				if (unfinished != null) {
					Logger.info("Run " + runId + " ends without a complete answer (" + stopReason
						+ "): the last reply had finish reason '" + assistantMsg.finishReason() + "'.",
						AgentLoop.class);
				}
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn, totalUsage);
			}
			continuations = 0;

			if (!executeToolCalls(toolCalls, context, conversation, listener, runId, turn)) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.ABORTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));

			// Between turns, never inside one: a turn that has begun is finished and its tool results
			// are written, so the history the next run continues from is whole. The run was admitted,
			// so it always gets at least this one turn.
			if (context.budgetAllowance().exhausted()) {
				StopReason stopReason = StopReason.BUDGET_EXHAUSTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn, totalUsage);
			}
		}

		StopReason stopReason = StopReason.MAX_TURNS;
		emit(listener, new AgentEvent.RunCompleted(runId, maxTurns, now(), stopReason, totalUsage, totalCost));
		return new AgentResult(stopReason, maxTurns, totalUsage);
	}

	/**
	 * Run the agent loop with an empty (unrestricted) context.
	 *
	 * <p>Convenience method for tests and trusted internal use.</p>
	 */
	public AgentResult run(Conversation conversation, AgentEventListener listener) {
		return run(conversation, ToolContext.unrestricted(), listener);
	}

	/**
	 * Run the agent loop using the configured {@link ConversationMemory}.
	 *
	 * <p>Uses the memory's conversation for message history. At the start of each turn - before
	 * the request is built - checks whether the conversation still fits the budget and compacts it
	 * if it does not. After each LLM response, updates the token baseline.</p>
	 *
	 * @param context  execution context for the current user
	 * @param listener receives progress events
	 * @return the result describing why the loop stopped
	 * @throws IllegalStateException if this loop was not created with a ConversationMemory
	 */
	public AgentResult run(ToolContext context, AgentEventListener listener) {
		if (_memory == null) {
			throw new IllegalStateException(
				"This AgentLoop was not created with a ConversationMemory. "
				+ "Use the constructor that accepts ConversationMemory, "
				+ "or call run(Conversation, ToolContext, AgentEventListener) instead.");
		}
		Objects.requireNonNull(context, "Context must not be null.");
		Objects.requireNonNull(listener, "Listener must not be null.");

		beginRun();

		ToolContext runContext = securedContext(context);

		_sessionApprovedTools.addAll(context.sessionApprovedTools());

		AgentEventListener serialized = AgentEventListener.serializing(
			new DiagnosticLoggingListener(RunObservers.observe(context, listener)));
		String runId = UUID.randomUUID().toString();
		emit(serialized, new AgentEvent.RunStarted(runId, 0, now(), _config.model()));
		emitEnvironment(serialized, runId);
		emitTurnContext(serialized, runId);

		try (MemoryConsentBinding.Binding consent =
			MemoryConsentBinding.bindIfAbsent(MemoryConsent.forNewestTurn(_memory.conversation()));
			// This run's token on its thread, for the waits of code that is handed no context; a
			// subagent's worker thread carries its lead's binding otherwise.
			RunCancellationBinding.Binding cancellation = RunCancellationBinding.bind(context.cancellationToken())) {
			return runWithMemory(_memory, runContext, serialized, runId);
		} catch (RuntimeException e) {
			// Guarantee a terminal event so event consumers (e.g. SSE clients) are
			// never left waiting for a run that already died.
			Logger.error("Agent run " + runId + " failed unexpectedly: " + e.getMessage(), e, AgentLoop.class);
			emit(serialized, new AgentEvent.RunCompleted(runId, 0, now(), StopReason.ERROR, TokenUsage.EMPTY,
				RunCosts.zero()));
			throw e;
		} finally {
			// The archived copy of a document read in parts is extended once, here, rather than per
			// part; see PageArchive.
			PageArchive.flush(context.readingList());
		}
	}

	/**
	 * What {@link #requestReply} brought: the turn's reply, or the provider failure that ends the run.
	 *
	 * <p>
	 * It carries the run's totals rather than this turn's alone, so that every request is added to
	 * them one by one, in the order the loop always added them.
	 * </p>
	 *
	 * @param message
	 *        The reply, already in the conversation; <code>null</code> on a failure.
	 * @param totalUsage
	 *        The tokens of the run so far, this turn's requests included.
	 * @param totalCost
	 *        The cost of the run so far, this turn's requests included.
	 * @param failure
	 *        The provider failure that ends the run, or <code>null</code>.
	 */
	private record TurnReply(AssistantMessage message, TokenUsage totalUsage, Money totalCost,
			ProviderException failure) {
		// Nothing but the components.
	}

	/**
	 * Requests the turn's reply, adds it to the conversation, and prices it and books it on the run's
	 * budget.
	 *
	 * <p>
	 * On the run's last turn the reply loses the tool calls it may still hold, and a reply without
	 * any text is asked for once more - see {@link #askForFinalAnswer}. When that second request
	 * fails, the run ends at its limit with the reply it has.
	 * </p>
	 *
	 * @param lastTurn
	 *        Whether {@link #announceFinalTurn} made this turn the run's last.
	 * @param request
	 *        The provider call of the loop the turn runs in.
	 * @param afterReply
	 *        Called with each reply once it is booked, or <code>null</code>.
	 */
	private TurnReply requestReply(Conversation conversation, ToolContext context, AgentEventListener listener,
			String runId, int turn, boolean lastTurn, TokenUsage totalUsage, Money totalCost,
			ProviderCall request, Consumer<AssistantMessage> afterReply) {
		AssistantMessage assistantMsg = null;
		UserMessage answerRequest = null;
		while (true) {
			try {
				assistantMsg = callRouted(request, context, listener, runId, turn);
			} catch (ProviderException e) {
				if (answerRequest != null && stopReasonFor(e, context) == StopReason.ERROR) {
					// The run ends with the reply it has, as it did before it asked once more.
					withdrawFinalAnswerRequest(conversation, answerRequest, e, runId);
					break;
				}
				return new TurnReply(null, totalUsage, totalCost, e);
			} finally {
				_finalTurn = false;
			}

			if (lastTurn) {
				assistantMsg = withoutToolCalls(assistantMsg, runId);
			}
			conversation.add(assistantMsg);
			totalUsage = totalUsage.add(assistantMsg.usage());
			// Priced per request and with the model that answered it: a failover moves the run to
			// another price list, and a tiered price depends on this request's prompt alone.
			Money turnCost = RunCosts.costOf(_config.model(), assistantMsg.usage());
			totalCost = totalCost.plus(turnCost);
			context.budgetAllowance().record(assistantMsg.usage(), turnCost);
			if (afterReply != null) {
				afterReply.accept(assistantMsg);
			}

			if (!lastTurn || answerRequest != null || unfinished(assistantMsg) != StopReason.NO_ANSWER) {
				break;
			}
			answerRequest = askForFinalAnswer(conversation, runId, turn);
			_finalTurn = true;
		}
		return new TurnReply(assistantMsg, totalUsage, totalCost, null);
	}

	/**
	 * Ends a run at its turn limit with the reply of the last turn {@link #announceFinalTurn} allowed
	 * it.
	 */
	private static AgentResult endAtLimit(AssistantMessage reply, AgentEventListener listener, String runId,
			int turn, TokenUsage totalUsage, Money totalCost) {
		StopReason limit = StopReason.MAX_TURNS;
		emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), reply.usage(), reply.content()));
		Logger.info("Run " + runId + " ends at its limit (" + limit + ") after a last turn without tools.",
			AgentLoop.class);
		emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), limit, totalUsage, totalCost));
		return new AgentResult(limit, turn, totalUsage);
	}

	/**
	 * Ends the run on the exception its provider call ended with.
	 *
	 * <p>
	 * A run the user cancelled is not a failed one, whatever the provider made of it: cancelling
	 * closes the response stream, and the provider reports the closed stream as an
	 * {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind#UNKNOWN} "Stream cancelled.".
	 * Such a run emits no {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ProviderError} and
	 * carries no error on its result - it ends {@link StopReason#ABORTED}, which is how every
	 * consumer already tells a cancel apart. Passed on, the provider's sentence was drawn in the
	 * chat as "the model reported an error" and persisted into the transcript as one (#549).
	 * </p>
	 */
	private AgentResult endOnProviderFailure(ProviderException e, ToolContext context,
			AgentEventListener listener, String runId, int turn, TokenUsage totalUsage, Money totalCost) {
		StopReason stopReason = stopReasonFor(e, context);
		boolean cancelled = stopReason == StopReason.ABORTED;
		if (!cancelled) {
			emit(listener, new AgentEvent.ProviderError(runId, turn, now(), e.kind().name(), e.getMessage()));
		}
		emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), TokenUsage.EMPTY, List.of()));
		emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage, totalCost));
		// The kind and the message travel on the result, not only in the event stream: an entry
		// point without a listener of its own (the TL-Script functions) must still be able to tell
		// a failed run from an empty answer, and say why it failed.
		return cancelled
			? new AgentResult(stopReason, turn, totalUsage)
			: new AgentResult(stopReason, turn, totalUsage, e.kind().name(), e.getMessage());
	}


	/**
	 * Classify a provider failure: a run cancelled by the user is {@link StopReason#ABORTED}
	 * regardless of the provider error that surfaced it, context overflow is
	 * {@link StopReason#CONTEXT_OVERFLOW}, everything else is {@link StopReason#ERROR}.
	 */
	private static StopReason stopReasonFor(ProviderException e, ToolContext context) {
		if (context.cancellationToken().isCancelled() || Thread.currentThread().isInterrupted()) {
			return StopReason.ABORTED;
		}
		return e.kind() == ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED
			? StopReason.CONTEXT_OVERFLOW
			: StopReason.ERROR;
	}

	/** Maximum reactive compaction retries on context_window_exceeded before giving up. */
	private static final int MAX_REACTIVE_COMPACT_RETRIES = AgentTuning.MAX_REACTIVE_COMPACT_RETRIES;

	/** How often in a row the model is asked to go on after a reply that said nothing. */
	private static final int MAX_EMPTY_REPLY_CONTINUATIONS = AgentTuning.MAX_EMPTY_REPLY_CONTINUATIONS;

	/** The finish reason every provider reports for a reply cut off at the output limit. */
	private static final String FINISH_MAX_TOKENS = "max_tokens";

	/**
	 * What the model is told after a reply that held no text and called no tool.
	 *
	 * <p>
	 * A user message, for the reason {@link #injectSubagentResults} gives: no provider transmits a
	 * system message in the middle of a conversation. The prefix says in the text what the role
	 * cannot - the user did not write this.
	 * </p>
	 */
	static final String CONTINUE_NOTICE = "[Framework notice] Your last reply contained no text for"
		+ " the user and no tool call, so the user has received no answer yet. Continue the task now:"
		+ " call the tool you need next, or write your answer to the user.";

	/** {@link #CONTINUE_NOTICE} for a reply that was cut off at the output limit before it said anything. */
	static final String CONTINUE_NOTICE_OUTPUT_LIMIT = "[Framework notice] Your last reply reached the"
		+ " output limit before it contained any text for the user or a tool call, so the user has"
		+ " received no answer yet. Keep your reasoning short and continue the task now: call the tool"
		+ " you need next, or write your answer to the user.";

	/**
	 * What the model is told before the last turn of a run that reached its turn limit; the number
	 * is the limit.
	 */
	static final String FINAL_TURN_NOTICE_TURNS = "[Framework notice] This run has reached its limit of"
		+ " %d turns. You cannot call any tool any more. Write your final answer now from what you"
		+ " have: what you found, and what is still open and why. If another agent started you, this"
		+ " answer is your report to it.";

	/** Base backoff delay in milliseconds for transient retries. Doubles each attempt. */
	private static final long BASE_BACKOFF_MS = AgentTuning.BASE_BACKOFF_MS;

	/** Sleep slice for backoff waits, so cancellation is noticed promptly. */
	private static final long BACKOFF_POLL_MS = AgentTuning.BACKOFF_POLL_MS;

	private AgentResult runWithMemory(ConversationMemory memory, ToolContext context,
			AgentEventListener listener, String runId) {
		Conversation conversation = memory.conversation();
		TokenUsage totalUsage = TokenUsage.EMPTY;
		Money totalCost = RunCosts.zero();
		int maxTurns = _config.maxTurns();
		int continuations = 0;

		for (int turn = 1; turn <= maxTurns; turn++) {
			if (Thread.interrupted() || context.cancellationToken().isCancelled()) {
				StopReason stopReason = StopReason.ABORTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn - 1, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn - 1, totalUsage);
			}

			emit(listener, new AgentEvent.TurnStarted(runId, turn, now()));

			injectBetweenTurns(conversation, context, listener, runId, turn);

			compactIfOverBudget(memory, listener, runId, turn);

			boolean lastTurn = announceFinalTurn(conversation, turn, maxTurns, listener, runId);

			int currentTurn = turn;
			TurnReply reply = requestReply(conversation, context, listener, runId, turn, lastTurn, totalUsage,
				totalCost,
				() -> callProviderWithReactiveCompaction(conversation, context, memory, listener, runId, currentTurn),
				answer -> memory.updateTokenBaseline(answer.usage()));
			totalUsage = reply.totalUsage();
			totalCost = reply.totalCost();
			if (reply.failure() != null) {
				return endOnProviderFailure(reply.failure(), context, listener, runId, turn, totalUsage, totalCost);
			}
			AssistantMessage assistantMsg = reply.message();

			if (lastTurn) {
				return endAtLimit(assistantMsg, listener, runId, turn, totalUsage, totalCost);
			}

			if (!_config.responseValidator().test(assistantMsg)) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.REJECTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			List<ToolCall> toolCalls = assistantMsg.content().stream()
				.filter(ToolCall.class::isInstance)
				.map(ToolCall.class::cast)
				.toList();

			if (toolCalls.isEmpty()) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason unfinished = unfinished(assistantMsg);
				if (unfinished == StopReason.NO_ANSWER && continuations < MAX_EMPTY_REPLY_CONTINUATIONS
					&& turn < maxTurns) {
					continuations++;
					askToContinue(conversation, assistantMsg, runId, turn);
					continue;
				}
				if (turn < maxTurns && messagesArrivedDuringLastTurn(runId, turn)) {
					continue;
				}
				StopReason stopReason = unfinished != null ? unfinished : StopReason.COMPLETE;
				if (unfinished != null) {
					Logger.info("Run " + runId + " ends without a complete answer (" + stopReason
						+ "): the last reply had finish reason '" + assistantMsg.finishReason() + "'.",
						AgentLoop.class);
				}
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn, totalUsage);
			}
			continuations = 0;

			if (!executeToolCalls(toolCalls, context, conversation, listener, runId, turn)) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.ABORTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));

			// Between turns, never inside one: a turn that has begun is finished and its tool results
			// are written, so the history the next run continues from is whole. The run was admitted,
			// so it always gets at least this one turn.
			if (context.budgetAllowance().exhausted()) {
				StopReason stopReason = StopReason.BUDGET_EXHAUSTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage, totalCost));
				return new AgentResult(stopReason, turn, totalUsage);
			}
		}

		StopReason stopReason = StopReason.MAX_TURNS;
		emit(listener, new AgentEvent.RunCompleted(runId, maxTurns, now(), stopReason, totalUsage, totalCost));
		return new AgentResult(stopReason, maxTurns, totalUsage);
	}

	/**
	 * Compacts the conversation when it no longer fits the budget, before the turn's request is
	 * built.
	 *
	 * <p>
	 * Called at the top of every turn, after {@link #injectBetweenTurns} and before the provider
	 * call, which is the one order in which a conversation that is already over budget cannot spend
	 * a round trip on a request that cannot be sent (Gitea #367). The check used to sit at the end
	 * of a turn, so the first request of an over-budget conversation - a reloaded session whose
	 * compaction sidecar did not match, most of all - was sent, refused, and only then compacted.
	 * </p>
	 *
	 * <p>
	 * The same position also answers the other half of that question: a turn's tool results are
	 * appended by {@link #executeToolCalls} and are therefore measured here, at the start of the
	 * next turn, which is the next moment that history is actually used. Checking between the tool
	 * batches of one turn would buy nothing - no request is built there - and would run a compaction
	 * over a half-finished turn, where an assistant message's tool calls and their results are not
	 * yet all appended and can therefore not be kept together.
	 * </p>
	 *
	 * <p>
	 * A run therefore no longer compacts after its last turn: what that pass used to prepare is
	 * prepared by the first turn of the next run instead, which is where it stops a request rather
	 * than a summarizer call from being spent on a conversation nobody continues. A provider that
	 * reports an overflow the estimate did not see is still caught by
	 * {@link #callProviderWithReactiveCompaction}.
	 * </p>
	 */
	private void compactIfOverBudget(ConversationMemory memory, AgentEventListener listener,
			String runId, int turn) {
		if (!memory.shouldCompact()) {
			return;
		}
		int tokensBefore = memory.estimateTokens();
		emit(listener, new AgentEvent.CompactionStarted(runId, turn, now(), tokensBefore, "proactive"));
		// Pass the same label used above, so CompactionResult.trigger() actually matches what
		// CompactionStarted already reported - memory.compact()'s "auto" default is never read by
		// anything that persists or displays it.
		CompactionResult result = memory.compact("proactive");
		_registry.historyCompacted();
		emit(listener, new AgentEvent.CompactionCompleted(runId, turn, now(), result));
	}

	/**
	 * Call provider with reactive compaction: if the API returns
	 * {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind#CONTEXT_WINDOW_EXCEEDED},
	 * compact and retry.
	 */
	private AssistantMessage callProviderWithReactiveCompaction(
			Conversation conversation, ToolContext context, ConversationMemory memory,
			AgentEventListener listener, String runId, int turn) throws ProviderException {
		for (int attempt = 0; attempt <= MAX_REACTIVE_COMPACT_RETRIES; attempt++) {
			try {
				return callProvider(conversation, context, listener, runId, turn);
			} catch (ProviderException e) {
				if (e.kind() != ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED
						|| attempt == MAX_REACTIVE_COMPACT_RETRIES) {
					throw e;
				}
				int tokensBefore = memory.estimateTokens();
				emit(listener, new AgentEvent.CompactionStarted(runId, turn, now(), tokensBefore, "reactive"));
				CompactionResult result = memory.compact("reactive");
				_registry.historyCompacted();
				emit(listener, new AgentEvent.CompactionCompleted(runId, turn, now(), result));
			}
		}
		throw new IllegalStateException("Unreachable");
	}

	/**
	 * @return {@code true} if all tool calls were executed, {@code false} if interrupted
	 */
	private boolean executeToolCalls(List<ToolCall> toolCalls, ToolContext context,
			Conversation conversation, AgentEventListener listener, String runId, int turn) {

		_turnDeniedCallIds.clear();
		_turnBlockedCallIds.clear();
		_turnCallRan = false;

		// Before anything of the turn reads - the checks of its calls included - so that the turn
		// sees what others committed since the run last looked.
		catchUpWithOtherCommits();

		List<List<ToolCall>> batches = partitionBatches(toolCalls, context);
		int messagesBefore = conversation.size();

		for (List<ToolCall> batch : batches) {
			boolean interrupted = Thread.interrupted();
			if (interrupted || context.cancellationToken().isCancelled()) {
				// The rollback may touch the database, so it runs without the interrupt: one that
				// reaches a file read closes H2's database file for the whole application.
				rollbackTransaction(context, listener, runId, turn, "Interrupted");
				if (interrupted) {
					Thread.currentThread().interrupt();
				}
				return false;
			}

			if (batch.size() == 1) {
				executeSingle(batch.get(0), context, conversation, listener, runId, turn);
			} else {
				executeConcurrent(batch, context, conversation, listener, runId, turn);
			}
		}

		// End-of-turn: commit unless a tool actually executed and failed. Synthetic
		// failures (denied, duplicate-blocked, unknown tool) never ran, so they cannot
		// have corrupted data — rolling back for them would discard unrelated writes
		// that succeeded in the same turn. Failures marked ToolState.Failed.synthetic()
		// (e.g. unresolvable arguments) are equally synthetic but only known once
		// ToolCatalog has already produced the result, so they are recognized here
		// instead of via _turnBlockedCallIds.
		boolean anyExecutionFailed = conversation.messages().subList(messagesBefore, conversation.size())
			.stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.filter(tr -> !_turnBlockedCallIds.contains(tr.toolCallId()))
			.anyMatch(tr -> tr.state() instanceof ToolState.Failed failed && !failed.synthetic());

		if (anyExecutionFailed) {
			rollbackTurn(context, conversation, listener, runId, turn, "Tool execution failed",
				"Because a tool call in this turn failed, ALL data changes made by tools in this turn"
					+ " were rolled back and NOT persisted — including changes reported as successful."
					+ " Re-apply any changes that are still needed.");
		} else {
			commitTransaction(context, conversation, listener, runId, turn);
		}

		return true;
	}

	/**
	 * Rolls the whole turn back and corrects the picture the model was left with.
	 *
	 * <p>
	 * The one rollback route for "everything this turn wrote is gone": a failed tool call, a
	 * {@link com.top_logic.tl.ai.core.security.PreCommitGuard} violation and a commit that itself
	 * failed all go through here, so all-or-nothing means the same thing in every case and the model
	 * is told in the same shape. Leaving the commit-failure path out was what let an agent report a
	 * created object, by the identifier its tool had returned, that no commit ever persisted.
	 * </p>
	 *
	 * @param reason
	 *        The reason for the
	 *        {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.TransactionRolledBack} event.
	 * @param notice
	 *        What to tell the model, without the {@code [Framework notice]} prefix. Only added when
	 *        a transaction was actually active - with nothing rolled back there is nothing to
	 *        correct.
	 */
	private void rollbackTurn(ToolContext context, Conversation conversation, AgentEventListener listener,
			String runId, int turn, String reason, String notice) {
		boolean hadTransaction = context.transaction().isActive();
		rollbackTransaction(context, listener, runId, turn, reason);
		if (hadTransaction) {
			// The model was already told (via the individual tool results) that some
			// tools succeeded. Correct that picture: their writes are gone now.
			conversation.add(new UserMessage(
				UUID.randomUUID().toString(), now(),
				List.of(new TextContent("[Framework notice] " + notice)), UserMessage.Origin.FRAMEWORK));
		}
	}

	/**
	 * Moves the run's view of the database to the newest commit, if nothing on this thread depends on
	 * the view it has (Gitea #647).
	 *
	 * <p>
	 * A run reads the database through the interaction of the thread it runs on, and an interaction
	 * reads at the revision it read first - for a chat run that is before its first turn, because the
	 * chat's worker enters the window's sub-session once for the whole run. Left alone, the run does
	 * not see what anybody else commits while it runs: the helpers a tool started, a person saving in
	 * the browser, a scheduled job. A write to an object such a commit changed is then a write on an
	 * outdated version, which the knowledge base refuses when the turn commits, and the turn is rolled
	 * back for a conflict that never was one.
	 * </p>
	 *
	 * <p>
	 * So a turn reads the database as it is when its calls run: the view is moved before the first call
	 * of the turn is checked, and once more after an approval that call waited for, because a person
	 * may have saved something meanwhile. From the first call that runs on, the calls of the turn read
	 * one state. A commit made after that point that changed an object the turn writes is a real
	 * conflict, and the turn's commit fails on it as before. What a tool's own helpers commit while it
	 * waits for them is the tool's to catch up with - see
	 * {@link com.top_logic.tl.ai.tool.api.ToolExecutionContext#catchUpWithCommits()}.
	 * </p>
	 *
	 * <p>
	 * Only while the thread holds no knowledge-base context. Between two turns the loop has nothing
	 * open, which makes this the same kind of point as the begin of a request, where
	 * {@code ChatServlet} moves the view. A run started inside a transaction of its caller - a script
	 * in a command that opened one, a delegated run inside a turn that has written - keeps the caller's
	 * view: moving it would merge other threads' commits into a transaction that is not the run's.
	 * </p>
	 *
	 * <p>
	 * The sub-session moves with the view, as it does on every commit of the run. A chat run shares it
	 * with the window's requests; a request that is running at that moment keeps the view it has.
	 * </p>
	 */
	private static void catchUpWithOtherCommits() {
		if (ThreadContextManager.getSubSession() == null || !PersistencyLayer.Module.INSTANCE.isActive()) {
			// No interaction, or no knowledge base: nothing reads at an old revision.
			return;
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		if (!(kb instanceof DBKnowledgeBase db) || db.getCurrentDBContext() != null) {
			return;
		}
		try {
			HistoryUtils.updateSessionRevision();
		} catch (RuntimeException ex) {
			// Nothing is open, so there is nothing to conflict with. Should moving fail anyway, the
			// turn reads what the run read so far rather than not running.
			Logger.warn("Could not move the run's view of the database to the newest commit; the turn"
				+ " reads the state the run read so far.", ex, AgentLoop.class);
		}
	}

	/**
	 * Ensure a transaction is active before executing a write tool.
	 * Begins a transaction on first write. Locking is handled by individual
	 * tools via {@link TransactionScope#acquireLock(String, com.top_logic.model.TLObject...)}.
	 *
	 * <p>
	 * Only a tool that writes state this application's transaction covers - see
	 * {@link ToolMode#writesApplicationState()} - opens one. A
	 * {@link ToolMode#READ} tool has nothing to commit, and a {@link ToolMode#EXTERNAL} one has
	 * nothing this transaction could commit or undo: the remote system commits by itself. Opening a
	 * transaction for it would create one that only ever commits an empty change set, while
	 * suggesting in the turn's events that the call could be taken back.
	 * </p>
	 *
	 * @param callMode what the call changes, see {@link ToolCatalog#callMode(Tool, ToolCall)}; may be
	 *                 {@code null} for unknown tools
	 * @param context  the current execution context
	 * @param listener receives transaction events
	 * @param runId    the current run ID
	 * @param turn     the current turn number
	 */
	private void ensureTransaction(ToolMode callMode, ToolContext context,
			AgentEventListener listener, String runId, int turn) {
		TransactionScope scope = context.transaction();
		if (scope.isNoOp() || scope.isActive()) {
			return;
		}

		if (callMode == null || !callMode.writesApplicationState()) {
			return;
		}

		scope.begin();
		emit(listener, new AgentEvent.TransactionBegan(runId, turn, now()));
	}

	/** */
	private void commitTransaction(ToolContext context, Conversation conversation,
			AgentEventListener listener, String runId, int turn) {
		TransactionScope scope = context.transaction();
		if (!scope.isActive()) {
			// No write tool ran this turn: nothing to commit and nothing to guard.
			return;
		}
		if (!preCommitGuardPasses(context, conversation, listener, runId, turn)) {
			return;
		}
		Revision rev;
		try {
			scope.commit();
			rev = scope.commitRevision();
		} catch (RuntimeException e) {
			// Every failed commit, whatever its type. TransactionScope.commit() wraps what the
			// engine threw into a TransactionException; catching only that was still one gate too
			// narrow for a failure the engine adds later, and a commit failure that escapes this
			// method kills the run and is announced to the browser as a provider error - the model
			// blamed for the application's refusal (issue #410).
			//
			// Only scope.commit() is inside: an event listener that throws must not be able to
			// make this method roll a committed turn back and tell the model its writes are gone.
			Logger.error("Agent run " + runId + " turn " + turn
				+ ": the transaction of the turn could not be committed.", e, AgentLoop.class);
			String reason = rollbackReason(e);
			rollbackTurn(context, conversation, listener, runId, turn, reason,
				"The data changes of this turn could NOT be saved and were rolled back: "
					+ asSentence(reason)
					+ " This includes every change a tool of this turn reported as successful -"
					+ " those results are void, and any identifier they named does not exist."
					+ " Tell the user that the change was not saved, and do not claim otherwise."
					+ " The cause is a database failure, not a mistake of yours, so do not retry"
					+ " the identical change.");
			return;
		}
		long revNum = rev != null ? rev.getCommitNumber() : -1;
		emit(listener, new AgentEvent.TransactionCommitted(runId, turn, now(), revNum));
	}

	/**
	 * The last gate before the turn's changes are persisted: may the run's principal make every
	 * change this turn accumulated?
	 *
	 * <p>
	 * The layer that catches tools which performed no check of their own - remote MCP tools,
	 * TL-Script tools authored in the running application, and any Java tool whose author forgot. On a
	 * violation the whole turn is rolled back through {@link #rollbackTurn}, the same route a failed
	 * tool call takes; each violation is audited by the guard itself.
	 * </p>
	 *
	 * <p>
	 * Costs nothing for a run that enforces no model access: the guard is asked whether it enforces
	 * <em>before</em> the change set is read from the transaction.
	 * </p>
	 *
	 * @return Whether the commit may proceed.
	 */
	private boolean preCommitGuardPasses(ToolContext context, Conversation conversation,
			AgentEventListener listener, String runId, int turn) {
		SecurityContext security = context.security();
		PreCommitGuard guard = PreCommitGuard.forRun();
		if (!guard.enforces()) {
			return true;
		}
		List<PreCommitGuard.Violation> violations = guard.check(security, context.transaction().changes());
		if (violations.isEmpty()) {
			return true;
		}
		rollbackTurn(context, conversation, listener, runId, turn, "Pre-commit security check failed",
			"The data changes of this turn were rolled back and NOT persisted, because your account is"
				+ " not permitted to make all of them: " + PreCommitGuard.describe(violations)
				+ ". The rollback is all-or-nothing, so the permitted changes of this turn are gone as"
				+ " well. Do not retry the same change; ask the user for help or continue without it.");
		return false;
	}

	/**
	 * The reason to report for a rollback caused by a failed commit.
	 *
	 * <p>
	 * Only the reason: the event says a rollback happened, and the chat prefixes it once more with
	 * "Rolled back: ". Adding {@link TransactionScope#COMMIT_FAILED_PREFIX} here as well is what
	 * produced the "Rolled back: Commit failed: Commit failed: ..." a real session reported.
	 * </p>
	 *
	 * <p>
	 * A failure that is not a {@link TransactionException} carries no reason a reader could act on -
	 * it is a defect of the engine or of a listener, not a refusal the model can work around - so it
	 * is named by its type. The stack trace is in the log line this reason accompanies.
	 * </p>
	 */
	private static String rollbackReason(RuntimeException failure) {
		String message = failure.getMessage();
		if (message != null && message.startsWith(TransactionScope.COMMIT_FAILED_PREFIX)) {
			return message.substring(TransactionScope.COMMIT_FAILED_PREFIX.length());
		}
		if (StringServices.isEmpty(message)) {
			return failure.getClass().getSimpleName();
		}
		return failure instanceof TransactionException ? message
			: failure.getClass().getSimpleName() + ": " + message;
	}

	/**
	 * The reason as a sentence, so the notice built around it does not run two sentences together.
	 *
	 * <p>
	 * A commit failure reason is whatever the engine produced: {@code "Database operation failed."}
	 * ends in a full stop, a nested rollback's reason may not.
	 * </p>
	 */
	private static String asSentence(String reason) {
		if (StringServices.isEmpty(reason)) {
			return "The database reported no reason.";
		}
		char last = reason.charAt(reason.length() - 1);
		return last == '.' || last == '!' || last == '?' ? reason : reason + ".";
	}

	/** */
	private void rollbackTransaction(ToolContext context, AgentEventListener listener, String runId, int turn,
			String reason) {
		TransactionScope scope = context.transaction();
		if (!scope.isActive()) {
			return;
		}
		scope.rollback();
		emit(listener, new AgentEvent.TransactionRolledBack(runId, turn, now(), reason));
	}

	/**
	 * Partition tool calls into batches: consecutive concurrency-safe tools are grouped
	 * together, non-safe tools get their own batch.
	 *
	 * <p>A tool is considered safe for concurrent execution when it reports itself as
	 * concurrency-safe AND will not require interactive approval (which would block
	 * the thread waiting for user input).</p>
	 */
	List<List<ToolCall>> partitionBatches(List<ToolCall> toolCalls, ToolContext context) {
		List<List<ToolCall>> batches = new ArrayList<>();
		List<ToolCall> currentBatch = null;
		boolean currentBatchConcurrent = false;

		for (ToolCall tc : toolCalls) {
			boolean safe = isConcurrencySafe(tc, context);

			if (currentBatch == null) {
				currentBatch = new ArrayList<>();
				currentBatch.add(tc);
				currentBatchConcurrent = safe;
			} else if (safe && currentBatchConcurrent) {
				currentBatch.add(tc);
			} else {
				batches.add(currentBatch);
				currentBatch = new ArrayList<>();
				currentBatch.add(tc);
				currentBatchConcurrent = safe;
			}
		}
		if (currentBatch != null) {
			batches.add(currentBatch);
		}
		return batches;
	}

	boolean isConcurrencySafe(ToolCall tc, ToolContext context) {
		Tool tool = _registry.getTool(tc.name());
		if (tool == null) {
			return false;
		}
		if (tool.mode() != ToolMode.READ) {
			// Only READ tools share concurrent batches. USER_SCOPED / DATA_WRITE / STRUCTURAL
			// tools trigger ensureTransaction, which opens a KB transaction in the worker's
			// own (interaction-scoped) DBContext; that transaction is auto-rolled-back when
			// the worker's interaction unbinds, leaving the shared TransactionScope holding
			// a zombie TX that the agent's end-of-turn commit then fails on with
			// "Invalid nesting of transactions". EXTERNAL opens no transaction and so escapes
			// that hazard, but it is excluded for its own reason: every EXTERNAL call is put in
			// front of a human, and a batch is precisely the shape in which nobody is asked.
			return false;
		}
		if (!tool.isConcurrencySafe()) {
			return false;
		}
		// Session-approved tools don't need interactive approval.
		if (_sessionApprovedTools.contains(tool.name())) {
			return true;
		}
		// Check if the security context auto-approves this tool.
		PermissionDecision decision = context.security().checkApproval(tool, tc);
		return decision instanceof PermissionDecision.Allowed;
	}

	private void executeSingle(ToolCall toolCall, ToolContext context,
			Conversation conversation, AgentEventListener listener, String runId, int turn) {

		ToolResultMessage duplicateResult = checkDuplicateToolCall(toolCall);
		if (duplicateResult != null) {
			conversation.add(duplicateResult);
			reportResult(toolCall, duplicateResult, listener, runId, turn, _turnDeniedCallIds);
			return;
		}

		ToolResultMessage result = executeWithSecurityCheck(toolCall, context, listener, runId, turn);
		conversation.add(result);
		reportResult(toolCall, result, listener, runId, turn, _turnDeniedCallIds);
	}

	/**
	 * Checks if this tool call is a consecutive duplicate of the previous call, or part of a
	 * two-call oscillation (A, B, A, B, ...), with the same name and arguments each time.
	 * Returns a synthetic error result if either pattern has repeated too many times,
	 * {@code null} otherwise.
	 *
	 * <p>Allows up to {@link #MAX_DUPLICATE_TOOL_CALLS} consecutive identical calls, and up to
	 * {@link #MAX_DUPLICATE_TOOL_CALLS} full A/B cycles (to account for legitimate retries and
	 * genuine compare-two-things back-and-forth). Beyond that, the call is blocked with a
	 * message directing the LLM to use the result it already received. Both counters reset as
	 * soon as a call breaks the pattern, so legitimate later re-reads (e.g. verify after an
	 * edit, or a third distinct comparison) stay possible.</p>
	 *
	 * <p>Only called from the loop thread — duplicate detection for concurrent batches
	 * happens before the batch is dispatched to worker threads.</p>
	 */
	private ToolResultMessage checkDuplicateToolCall(ToolCall toolCall) {
		String signature = toolCall.name() + "\0" + toolCall.arguments();
		if (signature.equals(_lastToolCallSignature)) {
			_lastToolCallRepeats++;
		} else {
			_alternationRepeats = signature.equals(_secondLastToolCallSignature) ? _alternationRepeats + 1 : 1;
			_secondLastToolCallSignature = _lastToolCallSignature;
			_lastToolCallSignature = signature;
			_lastToolCallRepeats = 1;
		}

		if (_lastToolCallRepeats > MAX_DUPLICATE_TOOL_CALLS) {
			_turnBlockedCallIds.add(toolCall.id());
			return errorResult(toolCall,
				"DUPLICATE CALL BLOCKED: You have already called '" + toolCall.name()
				+ "' with these exact arguments " + (_lastToolCallRepeats - 1) + " time(s) in a row. "
				+ "The result is already in your conversation. "
				+ "Use the result you already received instead of calling again. "
				+ "If you need different information, call the tool with DIFFERENT arguments.");
		}
		if (_alternationRepeats > MAX_DUPLICATE_TOOL_CALLS) {
			_turnBlockedCallIds.add(toolCall.id());
			return errorResult(toolCall,
				"DUPLICATE CALL BLOCKED: You are alternating between '" + toolCall.name()
				+ "' and a previous call without making progress. "
				+ "Both results are already in your conversation. "
				+ "Use the results you already received instead of calling again. "
				+ "If you need different information, call the tool with DIFFERENT arguments.");
		}

		return null;
	}

	/**
	 * Shared worker pool for concurrent READ-tool batches. Cached daemon threads are reused
	 * across batches and runs, and reaped after idling — avoids creating a fresh pool per batch.
	 */
	private static final ExecutorService TOOL_WORKERS = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "agent-tool-worker");
		thread.setDaemon(true);
		return thread;
	});

	private void executeConcurrent(List<ToolCall> batch, ToolContext context,
			Conversation conversation, AgentEventListener listener, String runId, int turn) {

		// Capture thread-local bindings and the caller's sub-session from the calling thread:
		// worker threads do not inherit thread-locals, so each must re-establish them.
		BindingSnapshot bindings = BindingSnapshot.capture();
		SubSessionContext userSession = ThreadContextManager.getSubSession();

		List<Future<ToolResultMessage>> futures = new ArrayList<>();

		for (ToolCall tc : batch) {
			// Duplicate detection runs on the loop thread, in call order, before dispatch —
			// same behaviour as for serial calls.
			ToolResultMessage duplicateResult = checkDuplicateToolCall(tc);
			if (duplicateResult != null) {
				futures.add(CompletableFuture.completedFuture(duplicateResult));
				continue;
			}

			ComputationEx2<ToolResultMessage, RuntimeException, RuntimeException> job = () -> {
				try (BindingSnapshot.Established established = bindings.establish()) {
					return executeWithSecurityCheck(tc, context, listener, runId, turn);
				}
			};
			// Propagate the caller's sub-session (e.g. the chat user's) onto the worker thread
			// so parallel tools run in the same context as sequential ones; fall back to a
			// system interaction only when the caller had no bound context.
			//
			// Submitted directly to the ExecutorService (not via CompletableFuture.supplyAsync)
			// so the returned Future is backed by FutureTask: its cancel(false) takes a job that
			// has not started off the queue, so it never runs.
			futures.add(TOOL_WORKERS.submit(
				() -> userSession != null
					? ThreadContextManager.inContext(userSession, job)
					: ThreadContext.inSystemContext(AgentLoop.class, job)));
		}

		List<ToolResultMessage> results = collectBatchResults(batch, futures, context);

		for (int i = 0; i < batch.size(); i++) {
			conversation.add(results.get(i));
			reportResult(batch.get(i), results.get(i), listener, runId, turn, _turnDeniedCallIds);
		}
	}

	/**
	 * Await batch results while staying responsive to interrupts and cancellation.
	 *
	 * <p>{@link CompletableFuture#join()} waits uninterruptibly, which would make the run
	 * un-cancellable while a batch is in flight. Uses timed polling instead; once cancelled
	 * or interrupted, remaining futures are cancelled and reported as failed, which triggers
	 * the end-of-turn rollback. Polls every not-yet-done future once per tick instead of
	 * exhausting one future's timeout before moving to the next, so cancellation is noticed
	 * within one poll slice regardless of the batch size.</p>
	 *
	 * <p>A job that is already running is given up, not interrupted: a batch holds only reading
	 * tools, and a reading tool is likely inside the knowledge base, where an interrupt during a
	 * file read closes H2's database file for the whole application. The job sees the cancelled
	 * token in its own waits ({@link RunCancellationBinding#whileBlocked}) and its result is
	 * dropped.</p>
	 */
	private static List<ToolResultMessage> collectBatchResults(List<ToolCall> batch,
			List<Future<ToolResultMessage>> futures, ToolContext context) {
		ToolResultMessage[] results = new ToolResultMessage[futures.size()];
		boolean abort = false;
		int remaining = futures.size();
		while (remaining > 0) {
			if (!abort && (context.cancellationToken().isCancelled() || Thread.interrupted())) {
				abort = true;
			}
			for (int i = 0; i < futures.size(); i++) {
				if (results[i] != null) {
					continue;
				}
				Future<ToolResultMessage> future = futures.get(i);
				if (abort) {
					future.cancel(false);
					results[i] = errorResult(batch.get(i), "Cancelled.");
					remaining--;
					continue;
				}
				try {
					results[i] = future.get(200, TimeUnit.MILLISECONDS);
					remaining--;
				} catch (TimeoutException e) {
					// Still running — loop and re-check cancellation.
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					abort = true;
					future.cancel(false);
					results[i] = errorResult(batch.get(i), "Cancelled.");
					remaining--;
				} catch (CancellationException e) {
					results[i] = errorResult(batch.get(i), "Cancelled.");
					remaining--;
				} catch (ExecutionException e) {
					Throwable cause = e.getCause() != null ? e.getCause() : e;
					results[i] = errorResult(batch.get(i),
						"Concurrent execution failed: " + cause.getMessage());
					remaining--;
				}
			}
		}
		return Arrays.asList(results);
	}

	/**
	 * Marks a tool call as denied: its failure result is synthetic (the tool never ran), so it is
	 * excluded from failure events (a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ToolCallDenied} was already emitted)
	 * and from the end-of-turn rollback decision.
	 */
	private void markDenied(String toolCallId) {
		_turnDeniedCallIds.add(toolCallId);
		_turnBlockedCallIds.add(toolCallId);
	}

	private ToolResultMessage executeWithSecurityCheck(ToolCall toolCall, ToolContext context,
			AgentEventListener listener, String runId, int turn) {

		if (context.cancellationToken().isCancelled()) {
			// A concurrent batch's worker may not start running this job until well after
			// the loop thread already gave up on it (thread-pool queueing, or another job in
			// the same batch is slow). Bail out before emitting any lifecycle event or
			// touching the tool. This covers jobs that haven't started yet; a job already
			// mid-execution is given up by collectBatchResults and ends by itself, its tool
			// cooperatively checking this token.
			return errorResult(toolCall, "Cancelled.");
		}

		SecurityContext security = context.security();

		// Step 1: Permission + mode gate
		Tool tool = _registry.getTool(toolCall.name());

		if (tool != null) {
			ToolPermissionFilter filter = new ToolPermissionFilter(security);
			if (!filter.test(tool)) {
				String reason = filter.denialReason(tool);
				emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), reason));
				markDenied(toolCall.id());
				return errorResult(toolCall, reason);
			}
		}
		// A name the registry does not resolve is deliberately not run through the permission
		// check: it belongs to no tool group, so the check would report a hallucinated or
		// misspelled name as a permission problem and swallow the "Available:" hint the model
		// needs to correct itself. Such a name cannot execute either way - the registry refuses
		// it below with a message that says so and lists what this user does have.

		// What this call changes: the tool's mode, or READ for its dry run. Approval, the turn's
		// transaction and the conversation log all go by this one answer.
		ToolMode callMode = tool != null ? ToolCatalog.callMode(tool, toolCall) : null;

		// Step 1b: The tool's own refusal of this call, asked before anybody is asked to approve it:
		// an approval prompt for a call that cannot succeed asks the user for nothing. A tool that
		// cannot act in this run at all was not offered; a call that names it anyway is answered the
		// same way, with the tool's reason.
		if (tool != null) {
			String refusal = unavailableOf(tool, toolCall, context);
			if (refusal == null) {
				refusal = refusalOf(tool, toolCall, context);
			}
			if (refusal != null) {
				emit(listener, new AgentEvent.ToolCallStarted(runId, turn, now(),
					toolCall.id(), toolCall.name(), toolCall.arguments(), callMode));
				// The tool never ran: a synthetic failure, which leaves the turn's other writes standing.
				_turnBlockedCallIds.add(toolCall.id());
				return errorResult(toolCall, refusal);
			}
		}

		// Step 2: Approval (only for registered tools with metadata)
		if (tool != null) {
			// Approval check
			// Check session memory first — skip approval if already approved for session
			if (_sessionApprovedTools.contains(tool.name())) {
				// Auto-approved for this session: emit a resolved event so listeners
				// still see an approval signal on repeat invocations, even though no
				// user interaction happens.
				emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
					toolCall.id(), UUID.randomUUID().toString(),
					ApprovalOutcome.APPROVED_FOR_SESSION));
			} else {
				PermissionDecision decision = security.checkApproval(tool, toolCall);
				if (decision instanceof PermissionDecision.Denied denied) {
					String reason = denied.reason();
					emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), reason));
					markDenied(toolCall.id());
					return errorResult(toolCall, reason);
				} else if (decision instanceof PermissionDecision.NeedsApproval needsApproval) {
					Map<String, Object> parsedArgs;
					try {
						parsedArgs = ToolCatalog.parseJson(toolCall.arguments());
					} catch (IOException e) {
						parsedArgs = Map.of();
					}
					String approvalRequestId = UUID.randomUUID().toString();
					ApprovalRequest request = new ApprovalRequest(
						approvalRequestId,
						tool.name(), toolCall.id(),
						parsedArgs, callMode,
						needsApproval.displayReason(),
						InteractionTimeoutService.approvalTimeout());
					String interactionId;
					ApprovalResult approvalResult;
					// The wait is open before the request is announced: whoever hears of it may answer
					// at once, and an answer that finds no wait registered is lost (#944).
					try (ApprovalCallback.ApprovalWait wait = context.approval().open(request)) {
						emit(listener, new AgentEvent.ApprovalRequired(runId, turn, now(),
							toolCall.id(), approvalRequestId, toolCall.name(),
							toolCall.arguments(), request.displayReason(), callMode, request.timeout()));
						// Durable record of the pending approval, carrying the tool call's actual
						// argument JSON (not the possibly-empty parsed copy shown above).
						interactionId = InteractionRecorder.begin(
							context.executionHandle(),
							AgentExecutionStore.InteractionType.APPROVAL, approvalRequestId,
							tool.name(), needsApproval.displayReason(), toolCall.arguments());
						approvalResult = wait.await();
					}
					if (approvalResult instanceof ApprovalResult.Approved) {
						InteractionRecorder.resolve(interactionId,
							AgentExecutionStore.InteractionState.RESOLVED, ApprovalOutcome.APPROVED.name());
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.APPROVED));
						// proceed to execution
					} else if (approvalResult instanceof ApprovalResult.ApprovedForSession) {
						_sessionApprovedTools.add(tool.name());
						InteractionRecorder.resolve(interactionId,
							AgentExecutionStore.InteractionState.RESOLVED,
							ApprovalOutcome.APPROVED_FOR_SESSION.name());
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.APPROVED_FOR_SESSION));
						// proceed to execution
					} else if (approvalResult instanceof ApprovalResult.Rejected rejected) {
						String feedback = rejected.userComment().isBlank()
							? "User denied tool '" + toolCall.name() + "'."
							: "User denied tool '" + toolCall.name() + "': "
								+ rejected.userComment();
						InteractionRecorder.resolve(interactionId,
							AgentExecutionStore.InteractionState.RESOLVED,
							rejected.userComment().isBlank() ? ApprovalOutcome.REJECTED.name()
								: ApprovalOutcome.REJECTED.name() + ": " + rejected.userComment());
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.REJECTED));
						emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), feedback));
						markDenied(toolCall.id());
						return errorResult(toolCall, feedback);
					} else if (approvalResult instanceof ApprovalResult.TimedOut) {
						String feedback = "Approval timed out for tool '" + toolCall.name() + "'.";
						InteractionRecorder.resolve(interactionId,
							AgentExecutionStore.InteractionState.TIMED_OUT, null);
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.TIMED_OUT));
						emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), feedback));
						markDenied(toolCall.id());
						return errorResult(toolCall, feedback);
					}
					if (!_turnCallRan) {
						// Approved. Whoever approved may have taken a while and saved something
						// meanwhile, in the very object this call is to change - and no call of the
						// turn has read anything yet.
						catchUpWithOtherCommits();
					}
				}
				// PermissionDecision.Allowed: proceed to execution
			}
		}

		// All security and approval gates passed — signal that the call is about to run.
		emit(listener, new AgentEvent.ToolCallStarted(runId, turn, now(),
			toolCall.id(), toolCall.name(), toolCall.arguments(), callMode));

		// Ensure transaction is open before executing a write tool.
		// Done here (after all security gates) so denied tools never open a transaction.
		ensureTransaction(callMode, context, listener, runId, turn);

		ProgressReporter progressReporter = message ->
			emit(listener, new AgentEvent.ToolProgress(runId, turn, now(),
				toolCall.id(), toolCall.name(), message));
		ToolContext execContext = context.withToolCallId(toolCall.id())
			.withProgressReporter(progressReporter);

		if (tool == null) {
			// Unknown tool: the registry fails the call without executing anything, so the
			// failure is synthetic and must not trigger the end-of-turn rollback.
			_turnBlockedCallIds.add(toolCall.id());
		} else {
			// From here on the calls of the turn read one state.
			_turnCallRan = true;
		}

		ToolResultMessage result = _registry.execute(toolCall, execContext);
		return truncateIfNeeded(result, listener, runId, turn);
	}

	/**
	 * Why the called tool cannot act in this run, or <code>null</code> when it can.
	 *
	 * <p>
	 * The same question the schema filter asked before the request, asked again for the call: the
	 * model can name a tool it was not offered, from an earlier turn or from a text that mentions it.
	 * A tool whose answer throws is not run - it could not say whether it can act.
	 * </p>
	 */
	private static String unavailableOf(Tool tool, ToolCall toolCall, ToolContext context) {
		try {
			return tool.unavailableReason(context);
		} catch (RuntimeException ex) {
			Logger.warn("Tool '" + toolCall.name() + "' failed to say whether it can act, so it is not run.", ex,
				AgentLoop.class);
			return "Tool '" + toolCall.name() + "' could not say whether it can act in this run: " + ex.getMessage();
		}
	}

	/**
	 * Why the tool refuses the given call before it runs, or {@code null}.
	 *
	 * <p>
	 * A check that fails refuses the call: nothing has run yet, and a call its own tool could not
	 * check is not one to put in front of the user for approval.
	 * </p>
	 *
	 * @see Tool#refusal(Map, com.top_logic.tl.ai.tool.api.ToolExecutionContext)
	 */
	private static String refusalOf(Tool tool, ToolCall toolCall, ToolContext context) {
		Map<String, Object> arguments;
		try {
			arguments = ToolCatalog.parseJson(toolCall.arguments());
		} catch (IOException | RuntimeException ex) {
			// Not the check's to answer: the execution reports arguments that do not parse.
			return null;
		}
		try {
			return ToolExecutions.refusal(tool, arguments, context.withToolCallId(toolCall.id()));
		} catch (RuntimeException ex) {
			Logger.warn("Tool '" + toolCall.name() + "' failed to check a call, which is therefore refused.", ex,
				AgentLoop.class);
			return "Tool '" + toolCall.name() + "' could not check this call: " + ex.getMessage();
		}
	}

	/**
	 * Bounds a tool answer by the installation's character budget, whatever its outcome.
	 *
	 * <p>
	 * A failure costs the same as a success: both stay in the conversation for every later request
	 * of the run, so both are cut to the budget and both report it with the same
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ToolCallResultTruncated} event. What a
	 * failure does <em>not</em> have is a per-tool budget of its own:
	 * {@link com.top_logic.tl.ai.tool.api.ToolResult#failure(String)} carries no
	 * {@code truncationHint}, and the failures that matter most here - a thrown exception, a remote
	 * MCP error body, an exception out of a TL-Script body - never pass a {@code ToolResult} at
	 * all. The installation default is therefore the only budget for a failure, deliberately: a
	 * tool must not be able to exempt its own error path from the ceiling the way a browser verb
	 * exempts a rendered page.
	 * </p>
	 *
	 * <p>
	 * Truncation keeps the head and the tail and names the gap between them
	 * ({@link #truncateToBudget}); the notice says only how much was dropped, so it never reads as
	 * an outcome and a cut failure still reads as a failure. The bits beside the text - the failed
	 * flag the providers send as {@code is_error} and the {@code synthetic} bit the end-of-turn
	 * rollback asks - travel unchanged.
	 * </p>
	 */
	private ToolResultMessage truncateIfNeeded(ToolResultMessage result,
			AgentEventListener listener, String runId, int turn) {
		if (result.state() instanceof ToolState.Completed completed) {
			return truncateCompleted(result, completed, listener, runId, turn);
		}
		if (result.state() instanceof ToolState.Failed failed) {
			return truncateFailed(result, failed, listener, runId, turn);
		}
		return result;
	}

	private ToolResultMessage truncateCompleted(ToolResultMessage result, ToolState.Completed completed,
			AgentEventListener listener, String runId, int turn) {
		String text = completed.result();
		int limit = completed.truncationHint() > 0 ? completed.truncationHint() : _maxResultChars;
		if (text.length() <= limit) {
			return result;
		}

		String truncated = truncateToBudget(text, limit);

		emit(listener, new AgentEvent.ToolCallResultTruncated(runId, turn, now(),
			result.toolCallId(), result.toolName(), text.length(), truncated.length()));

		return new ToolResultMessage(
			result.id(), result.timestamp(), result.toolCallId(), result.toolName(),
			new ToolState.Completed(truncated, completed.startTime(), completed.endTime(),
				false, completed.summary(), -1, truncateTextParts(completed.content(), limit)));
	}

	private ToolResultMessage truncateFailed(ToolResultMessage result, ToolState.Failed failed,
			AgentEventListener listener, String runId, int turn) {
		String text = failed.error();
		if (text.length() <= _maxResultChars) {
			return result;
		}

		String truncated = truncateToBudget(text, _maxResultChars);

		emit(listener, new AgentEvent.ToolCallResultTruncated(runId, turn, now(),
			result.toolCallId(), result.toolName(), text.length(), truncated.length()));

		return new ToolResultMessage(
			result.id(), result.timestamp(), result.toolCallId(), result.toolName(),
			new ToolState.Failed(truncated, failed.startTime(), failed.endTime(), failed.synthetic()));
	}

	/**
	 * Applies the same character budget to the text parts of a multimodal tool result.
	 *
	 * <p>Images and documents pass through untouched: a character budget does not describe them,
	 * and dropping them would silently cost the model the very content the tool was called for.</p>
	 */
	private static List<ContentPart> truncateTextParts(List<ContentPart> content, int limit) {
		if (content.isEmpty()) {
			return content;
		}
		List<ContentPart> result = new ArrayList<>(content.size());
		for (ContentPart part : content) {
			if (part instanceof TextContent text && text.text().length() > limit) {
				result.add(new TextContent(truncateToBudget(text.text(), limit)));
			} else {
				result.add(part);
			}
		}
		return result;
	}

	/** Keeps the first and last half of the budget, reporting how much was dropped in between. */
	private static String truncateToBudget(String text, int limit) {
		int halfBudget = limit / 2;
		String head = text.substring(0, halfBudget);
		String tail = text.substring(text.length() - halfBudget);
		int droppedChars = text.length() - head.length() - tail.length();
		return head + "\n\n[... " + droppedChars + " chars truncated ...]\n\n" + tail;
	}

	private static ToolResultMessage errorResult(ToolCall toolCall, String error) {
		Instant now = Instant.now();
		return new ToolResultMessage(
			UUID.randomUUID().toString(), now,
			toolCall.id(), toolCall.name(),
			new ToolState.Failed(error, now, now));
	}

	private static void reportResult(ToolCall toolCall, ToolResultMessage result,
			AgentEventListener listener, String runId, int turnNumber,
			Set<String> deniedCallIds) {
		if (deniedCallIds.contains(toolCall.id())) {
			return;
		}
		if (result.state() instanceof ToolState.Failed failed) {
			Duration d = Duration.between(failed.startTime(), failed.endTime());
			emit(listener, new AgentEvent.ToolCallFailed(
				runId, turnNumber, now(), toolCall.id(), toolCall.name(), failed.error(), d));
		} else if (result.state() instanceof ToolState.Completed completed) {
			Duration d = Duration.between(completed.startTime(), completed.endTime());
			emit(listener, new AgentEvent.ToolCallCompleted(
				runId, turnNumber, now(), toolCall.id(), toolCall.name(), completed.result(), d));
		}
	}

	/**
	 * Call the LLM provider with automatic retry for transient errors.
	 *
	 * <p>
	 * Retries on {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind#RATE_LIMITED} and
	 * {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind#OVERLOADED} with exponential
	 * backoff, never sooner than the provider's {@code Retry-After} - see {@link #retryWait}. The
	 * backoff sleeps in small slices so a user cancellation ends the wait promptly.
	 * </p>
	 *
	 * <p>
	 * A rate-limited run on its last model waits for up to {@link #rateLimitWaitMs()} in total: the
	 * model is usually held by another request of the same key, and giving up after a few seconds
	 * lost the turn - and with it the answer the user had just given to a question card - to a
	 * request that would have been served a minute later (#678). Everything else retries
	 * {@link AgentParams#maxRetries()} times, as it always did: a run with a fallback model ahead in
	 * its failover chain moves on to it instead of waiting, and an overloaded model is also what a
	 * plain server error is classified as, which no wait mends. {@link AgentParams#maxRetries()} of
	 * {@code 0} switches retrying off in every case.
	 * </p>
	 *
	 * <p>
	 * Every wait is announced as an {@link AgentEvent.RetryScheduled}, which is how the chat tells
	 * its user that the agent is waiting rather than stuck.
	 * </p>
	 */
	private AssistantMessage callProvider(Conversation conversation, ToolContext context,
			AgentEventListener listener, String runId, int turn) throws ProviderException {
		int maxRetries = Math.max(0, _config.maxRetries());
		boolean fallbackAhead = _chainIndex < _failoverChain.size() - 1;
		long waitedMs = 0;
		for (int attempt = 0;; attempt++) {
			try {
				return callProviderOnce(conversation, context, listener, runId, turn);
			} catch (ProviderException e) {
				boolean retryable = e.kind() == ProviderException.Kind.RATE_LIMITED
					|| e.kind() == ProviderException.Kind.OVERLOADED;
				if (!retryable || maxRetries == 0) {
					throw e;
				}
				boolean waitOut = e.kind() == ProviderException.Kind.RATE_LIMITED && !fallbackAhead;
				if (!waitOut && attempt >= maxRetries) {
					throw e;
				}
				long windowMs = waitOut ? rateLimitWaitMs() : Long.MAX_VALUE;
				long waitMs = retryWait(attempt, e.retryAfter(), waitedMs, windowMs);
				if (waitMs <= 0) {
					throw e;
				}
				waitedMs += waitMs;

				emit(listener, new AgentEvent.RetryScheduled(
					runId, turn, now(), e.kind().name(), attempt + 1, waitMs));

				backoff(waitMs, context, e);
			}
		}
	}

	/**
	 * How long to wait before the given retry, or {@code 0} when the wait is used up.
	 *
	 * <p>
	 * The wait doubles with every attempt, starting at the provider's {@code Retry-After} when it
	 * sent one and at {@link #BASE_BACKOFF_MS} otherwise, up to {@link AgentTuning#MAX_BACKOFF_MS}.
	 * It is never shorter than the provider's {@code Retry-After}: asking earlier only earns another
	 * refusal. The last wait is cut to what is left of the window.
	 * </p>
	 *
	 * @param attempt
	 *        How many retries were made before this one, {@code 0} for the first.
	 * @param retryAfter
	 *        What the provider asked for, or {@code null}.
	 * @param waitedMs
	 *        How long the call has waited so far.
	 * @param windowMs
	 *        How long the call may wait in total.
	 */
	static long retryWait(int attempt, Duration retryAfter, long waitedMs, long windowMs) {
		long remaining = windowMs - waitedMs;
		if (remaining <= 0) {
			return 0;
		}
		long asked = retryAfter == null ? 0 : Math.max(0, retryAfter.toMillis());
		long backoff = asked > 0 ? asked : BASE_BACKOFF_MS;
		for (int n = 0; n < attempt && backoff < AgentTuning.MAX_BACKOFF_MS; n++) {
			backoff *= 2;
		}
		long waitMs = Math.max(Math.min(backoff, AgentTuning.MAX_BACKOFF_MS), asked);
		return Math.min(waitMs, remaining);
	}

	/**
	 * How long a run on its last model waits in total for a model that is rate limited.
	 *
	 * <p>
	 * The installation's {@code rate-limit-wait-seconds} of {@code <failover>}, or
	 * {@link AgentTuning#DEFAULT_RATE_LIMIT_WAIT_SECONDS} for a loop that runs without the service.
	 * </p>
	 */
	private static long rateLimitWaitMs() {
		LLMProviderService service = LLMProviderService.Module.INSTANCE.isActive() ? LLMProviderService.getInstance() : null;
		int seconds = service != null ? service.rateLimitWaitSeconds()
			: AgentTuning.DEFAULT_RATE_LIMIT_WAIT_SECONDS;
		return Math.max(0, seconds) * 1000L;
	}

	// ---- Cross-provider failover (Gitea #344) ----

	/**
	 * Makes the coming turn the run's last one if the run has reached a limit, and tells the model.
	 *
	 * <p>
	 * A run at its last allowed turn gets one more request,
	 * with the tools still listed but none callable, and a notice that says so - a user message, for
	 * the reason {@link #injectSubagentResults} gives, announced as
	 * {@link AgentEvent.FinalTurnRequested}, which the run's own log records so that a reloaded
	 * session replays it. A run limited to one turn has no turn to spare for this and ends as it
	 * always did.
	 * </p>
	 *
	 * @return Whether the coming turn is the run's last, ending it at {@link StopReason#MAX_TURNS}.
	 */
	private boolean announceFinalTurn(Conversation conversation, int turn, int maxTurns,
			AgentEventListener listener, String runId) {
		if (turn != maxTurns || maxTurns <= 1) {
			return false;
		}
		String notice = String.format(FINAL_TURN_NOTICE_TURNS, maxTurns);
		Logger.info("Run " + runId + " turn " + turn + " is its last (" + StopReason.MAX_TURNS
			+ "): no tool may be called.", AgentLoop.class);
		conversation.add(new UserMessage(
			UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(notice)),
			UserMessage.Origin.FRAMEWORK));
		emit(listener, new AgentEvent.FinalTurnRequested(runId, turn, now(), notice));
		_finalTurn = true;
		return true;
	}

	/**
	 * What the model is told when the last turn of a run that reached its limit brought no text - a
	 * provider that ignores the request not to call tools answered with tool calls only.
	 */
	static final String FINAL_ANSWER_NOTICE = "[Framework notice] Your last reply contained no text: it"
		+ " called tools, and no tool can be called any more, so none of them ran. Write your final answer"
		+ " now, as text, from what you have: what you found, and what is still open and why.";

	/**
	 * Asks once more for the answer of a run that reached its limit, after a last reply without text.
	 *
	 * <p>
	 * The request goes out as the last one did, with the tools listed and none callable. Recorded like
	 * {@link #askToContinue}, so that a reloaded session puts the notice back between the two replies.
	 * </p>
	 *
	 * @return The notice added to the conversation.
	 */
	private static UserMessage askForFinalAnswer(Conversation conversation, String runId, int turn) {
		Logger.info("Run " + runId + " turn " + turn + ": the last reply at the run's limit held no text;"
			+ " asking once more for the answer.", AgentLoop.class);
		UserMessage notice = new UserMessage(
			UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(FINAL_ANSWER_NOTICE)),
			UserMessage.Origin.FRAMEWORK);
		conversation.add(notice);
		ConversationLogListener log = ConversationLogListenerBinding.getListener();
		if (log != null) {
			log.recordContinuationRequested(FINAL_ANSWER_NOTICE);
		}
		return notice;
	}

	/**
	 * Takes back the request of {@link #askForFinalAnswer} when it failed.
	 *
	 * <p>
	 * The request was one more try; its failure does not turn a run that reached its limit into a
	 * failed one. The notice is removed, so that the conversation ends with the run's last reply, as it
	 * did before.
	 * </p>
	 *
	 * @param request
	 *        The notice {@link #askForFinalAnswer} added. Recognised by its id, which a compaction that
	 *        rewrites the message keeps.
	 */
	private static void withdrawFinalAnswerRequest(Conversation conversation, UserMessage request,
			ProviderException e, String runId) {
		Logger.info("Run " + runId + ": asking once more for the answer failed (" + e.getMessage()
			+ "); the run ends at its limit with its last reply.", AgentLoop.class);
		List<Message> messages = conversation.messages();
		int last = messages.size() - 1;
		if (last >= 0 && messages.get(last).id().equals(request.id())) {
			conversation.replaceRange(last, last + 1, List.of());
		}
	}

	/**
	 * The last reply of a run without the tool calls it may still hold - a provider that ignores
	 * the request not to call any. Nothing runs them, and a call without a result would break the
	 * next request of the conversation.
	 */
	private static AssistantMessage withoutToolCalls(AssistantMessage reply, String runId) {
		List<ContentPart> kept = reply.content().stream().filter(part -> !(part instanceof ToolCall)).toList();
		if (kept.size() == reply.content().size()) {
			return reply;
		}
		Logger.info("Run " + runId + ": the last reply called " + (reply.content().size() - kept.size())
			+ " tool(s) although none may be called; they are dropped.", AgentLoop.class);
		return new AssistantMessage(reply.id(), reply.timestamp(), kept, reply.usage(), reply.finishReason());
	}

	/**
	 * Puts the run back on the model it was configured with, and works out where it may go from
	 * there.
	 *
	 * <p>
	 * Called at the start of every run, because a loop is allowed to serve more than one: a run
	 * that ended on a fallback must not hand its successor that fallback as its starting point.
	 * The successor may of course be routed there again straight away - the mark that sent the
	 * first run over is still held - but that decision is taken by {@link #callRouted} with the
	 * health state of the moment, not inherited.
	 * </p>
	 */
	private void beginRun() {
		_finalTurn = false;
		_config = _configuredParams;
		_provider = _configuredProvider;
		_chainIndex = 0;
		_activeIndex = 0;
		_activeProviderName = null;
		_skipped.clear();
		_announcedSkips = 0;
		_failoverChain = resolveFailoverChain();
	}

	/**
	 * The models this run may use, in order.
	 *
	 * <p>
	 * A run whose model is not configured in this application - a test wiring its own provider, a
	 * loop built by hand - has a chain of one and never fails over, because nothing knows how to
	 * build a provider for anything else.
	 * </p>
	 */
	private List<ModelTarget> resolveFailoverChain() {
		// The instance rather than the module: a service that has started is one that can route and
		// one whose health state is the installation's, however it was started. Everything else in
		// this class asks the module, because the answer there is "is this application configured",
		// not "is there a service".
		LLMProviderService service = LLMProviderService.getInstance();
		List<ModelTarget> chain = service == null ? List.of() : service.failoverChain(_config.model(), null);
		return chain.isEmpty() ? List.of(new ModelTarget(_config.model(), null)) : chain;
	}

	/**
	 * Calls the provider, moving the run to the next healthy model of its chain when the one it is
	 * on cannot answer.
	 *
	 * <p>
	 * The switch lasts for the <strong>rest of the run</strong>, not for the turn: every provider
	 * caches by prefix, so a run that alternated between two models would pay the full prompt again
	 * on every turn and never read a cache. Once a run has moved, it stays moved; the next run
	 * starts at the configured model again and is routed by the marks that are then in force.
	 * </p>
	 *
	 * <p>
	 * A model is passed over for two reasons, and both are recorded: {@link ModelAvailability}
	 * already holds a mark against it, or the call to it just failed with a kind that says
	 * something about the model. A failure that says nothing about the model - a malformed request,
	 * a context overflow - is rethrown untouched: the next model would fail on it in exactly the
	 * same way, and failing over would only hide it.
	 * </p>
	 *
	 * <p>
	 * A run with no configured fallback does not enter any of this: {@code call} is invoked once
	 * and its exception travels out, which is what the loop did before there were chains.
	 * </p>
	 *
	 * @param call
	 *        Makes one attempt with whatever model is currently active - the retry-and-compact
	 *        stack the run loop would otherwise have called directly.
	 */
	private AssistantMessage callRouted(ProviderCall call, ToolContext context,
			AgentEventListener listener, String runId, int turn) throws ProviderException {
		List<ModelTarget> chain = _failoverChain;
		if (chain.size() <= 1) {
			return call.call();
		}
		ModelAvailability health = LLMProviderService.getInstance().modelHealth();
		String turnToken = ModelAvailability.turnToken(runId, turn);
		ProviderException pending = null;
		while (_chainIndex < chain.size()) {
			ModelTarget target = chain.get(_chainIndex);
			if (!health.claim(target.model(), turnToken)) {
				ModelHealthMark mark = health.mark(target.model());
				skip(target, mark == null ? "marked unavailable" : mark.reason(), runId);
				continue;
			}
			if (_chainIndex != _activeIndex && !activate(target, context, listener, runId, turn)) {
				continue;
			}
			try {
				return call.call();
			} catch (ProviderException e) {
				if (context.cancellationToken().isCancelled() || Thread.currentThread().isInterrupted()) {
					throw e;
				}
				if (ModelAvailability.causeOf(e.kind()) == null) {
					throw e;
				}
				health.reportFailure(target.model(), e.kind(), e.getMessage(), turnToken);
				pending = e;
				skip(target, e.kind().name()
					+ (e.getMessage() == null || e.getMessage().isBlank() ? "" : ": " + e.getMessage()), runId);
			}
		}
		if (pending != null) {
			// The last model that was actually called decides how the run reads: a run that died
			// of a rate limit says so, and only the message names the models it went through.
			String detail = pending.getMessage() == null ? "" : pending.getMessage() + " ";
			throw new ProviderException(pending.kind(), pending.statusCode(),
				detail + exhausted(chain), pending.retryAfter(), pending.requestId(),
				pending.getCause());
		}
		throw new ProviderException(ProviderException.Kind.UNKNOWN, -1, exhausted(chain));
	}

	/** Why no model of the chain was left to try. */
	private String exhausted(List<ModelTarget> chain) {
		return "No model of the failover chain of '" + chain.get(0).label()
			+ "' could serve this run: " + String.join("; ", _skipped) + ".";
	}

	/**
	 * Notes that the given model was passed over, and moves the run one entry further down its
	 * chain.
	 */
	private void skip(ModelTarget target, String reason, String runId) {
		_skipped.add(target.label() + " - " + reason);
		Logger.warn("Run " + runId + " passes over model '" + target.label() + "': " + reason,
			AgentLoop.class);
		_chainIndex++;
	}

	/**
	 * Moves the run onto the given model: its provider, its limits and the sampling options its
	 * provider understands.
	 *
	 * <p>
	 * The tool schemas and the response format need nothing done to them here - they are
	 * provider-neutral, and the target's own request builder renders them in its own dialect, which
	 * is what makes a chain across vendors work at all. What does not survive is the conversation
	 * memory's budget: it was sized for the configured model and is owned by the caller, so a run
	 * that fails over to a smaller context window relies on the reactive compaction in
	 * {@link #callProviderWithReactiveCompaction} to notice.
	 * </p>
	 *
	 * @return <code>false</code> when no provider could be built for the target, which is a
	 *         configuration fault of that chain entry alone; the entry is then skipped like any
	 *         other unusable one.
	 */
	private boolean activate(ModelTarget target, ToolContext context, AgentEventListener listener,
			String runId, int turn) {
		LLMProviderService service = LLMProviderService.getInstance();
		LLMProvider provider;
		try {
			provider = service.buildProvider(target.model(), target.provider());
		} catch (RuntimeException ex) {
			skip(target, "no provider could be built: " + ex.getMessage(), runId);
			return false;
		}
		String from = _failoverChain.get(_activeIndex).label();
		String providerId = service.getProviderId(target.model(), target.provider());
		ModelInfo info = service.getModel(target.model());
		int maxOutput = _config.maxOutputTokens();
		int contextWindow = _config.contextWindowTokens();
		if (info != null) {
			// The lower of the two: an installation that asked for fewer tokens meant it, and a
			// target that allows fewer cannot be asked for more.
			maxOutput = Math.min(maxOutput, info.limits().maxOutput());
			contextWindow = Math.min(contextWindow, info.limits().contextWindow());
		}
		_config = _config.withModel(target.model(), maxOutput, contextWindow,
			ModelOptions.forTarget(_config.modelOptions(), providerId, info));
		_provider = provider;
		_activeProviderName = target.provider();
		_activeIndex = _chainIndex;
		context.executionHandle().setModel(target.model(), providerId);

		List<String> announce = _skipped.subList(_announcedSkips, _skipped.size());
		String reason = announce.isEmpty() ? "the previous model was not available"
			: String.join("; ", announce);
		_announcedSkips = _skipped.size();
		Logger.info("Run " + runId + " continues on model '" + target.label() + "' instead of '"
			+ from + "': " + reason, AgentLoop.class);
		emit(listener, new AgentEvent.ModelSwitched(runId, turn, now(), from, target.label(), reason));
		return true;
	}

	/** One attempt at the provider, with whatever model the run is currently on. */
	@FunctionalInterface
	private interface ProviderCall {

		/** Makes the attempt. */
		AssistantMessage call() throws ProviderException;
	}

	/**
	 * Sleep for the given backoff duration in {@link #BACKOFF_POLL_MS} slices, re-checking
	 * cancellation between slices. When the run is cancelled or the thread interrupted,
	 * gives up retrying by rethrowing the pending transient error — the run loop then maps
	 * it to {@link StopReason#ABORTED} via {@link #stopReasonFor}.
	 */
	private static void backoff(long waitMs, ToolContext context, ProviderException pending)
			throws ProviderException {
		if (context.cancellationToken().sleepCancellably(waitMs, BACKOFF_POLL_MS)) {
			throw pending;
		}
	}

	/**
	 * Call the LLM provider and collect the response.
	 *
	 * <p>Filters tool schemas dynamically based on the security context: tools the
	 * user lacks permission for, and STRUCTURAL tools in PRODUCTION mode, are excluded
	 * from the request so the LLM never sees capabilities it cannot use. So are tools that
	 * cannot act in this run at all - see {@link ToolPermissionFilter#offeredIn(ToolContext)}.</p>
	 */
	private AssistantMessage callProviderOnce(Conversation conversation, ToolContext context,
			AgentEventListener listener, String runId, int turn) throws ProviderException {
		ensureTurnContext(conversation);

		Predicate<Tool> toolFilter = ToolPermissionFilter.offeredIn(context);
		List<ToolSchema> filteredTools = _registry.toolSchemas(toolFilter);
		// Where the tool array stops being the same bytes on every request of this conversation: a
		// loaded skill's tools follow behind it, and a provider that places cache breakpoints puts
		// one here rather than paying for the whole array again after a loadSkill (#346).
		int stableToolCount = Math.min(_registry.stableToolCount(toolFilter), filteredTools.size());
		List<String> loadedSkills = _registry.loadedSkills();

		if (Logger.isDebugEnabled(AgentLoop.class)) {
			List<String> toolNames = filteredTools.stream().map(ToolSchema::name).toList();
			Logger.debug("run=" + runId + " turn=" + turn + " PROVIDER_REQUEST"
				+ " tools=" + filteredTools.size() + " messages=" + conversation.messages().size()
				+ " toolNames=" + toolNames,
				AgentLoop.class);
		}

		String systemPrompt = systemPrompt();
		// What this provider may actually be given: a thinking block another provider minted, or one
		// whose signature never arrived, is dropped here rather than rejected by the API - a request
		// refused over a replayed block fails every later one too, because the block stays in memory.
		List<Message> history = MessageHistorySanitizer.forProvider(conversation.messages(), providerId());
		emit(listener, contextComposition(runId, turn, systemPrompt, filteredTools, history, loadedSkills));

		ProviderRequest request = new ProviderRequest(
			_config.model(),
			systemPrompt,
			history,
			filteredTools,
			_config.maxOutputTokens(),
			_config.modelOptions(),
			_config.responseFormat(),
			_finalTurn ? ToolChoice.NONE : _config.toolChoice(),
			Duration.ofSeconds(_config.timeoutSeconds()),
			stableToolCount);

		ResponseCollector collector = new ResponseCollector(listener, runId, turn, context.cancellationToken());
		long startNanos = System.nanoTime();
		try {
			_provider.stream(request, collector);
		} finally {
			if (Logger.isDebugEnabled(AgentLoop.class)) {
				// Provider call latency is operational signal that is not persisted (the KB stores
				// only the run's token totals, not per-turn timing). Logged even on failure so a
				// slow-then-erroring provider is visible. Correlated by runId/turn.
				Logger.debug("run=" + runId + " turn=" + turn + " PROVIDER_MS ms="
					+ (System.nanoTime() - startNanos) / 1_000_000L, AgentLoop.class);
			}
		}
		return collector.toAssistantMessage();
	}

	/**
	 * What the prompt about to be sent is made of, so the chat UI can attribute the context window.
	 *
	 * <p>
	 * Estimated with the same {@link TokenEstimator} the compaction budget uses. A tool schema costs
	 * its name and description as prose plus its JSON Schema as JSON, which is how the providers
	 * serialise it.
	 * </p>
	 *
	 * <p>
	 * What the tools answered is counted twice over: once inside the conversation, where it is part
	 * of what the model reads, and once on its own, so a reader can see how much of their context a
	 * single large tool result took.
	 * </p>
	 */
	private static AgentEvent.ContextComposition contextComposition(String runId, int turn,
			String systemPrompt, List<ToolSchema> tools, List<Message> messages,
			List<String> loadedSkills) {
		TokenEstimator estimator = CONTEXT_ESTIMATOR;
		int toolTokens = 0;
		for (ToolSchema tool : tools) {
			toolTokens += estimator.estimateText(tool.name())
				+ estimator.estimateText(tool.description())
				+ estimator.estimateJson(tool.inputSchemaJson());
		}
		int toolResultTokens = 0;
		for (Message message : messages) {
			if (message instanceof ToolResultMessage) {
				toolResultTokens += estimator.estimateTokens(message);
			}
		}
		return new AgentEvent.ContextComposition(runId, turn, now(),
			estimator.estimateText(systemPrompt), toolTokens,
			estimator.estimateTokens(messages), toolResultTokens, tools.size(), loadedSkills);
	}

	/**
	 * Reports the environment block this run's system prompt carries, for the run record.
	 *
	 * <p>
	 * Nothing is emitted for a run that composed none - every caller that builds an
	 * {@link AgentParams} directly rather than through {@code AgentFactory}.
	 * </p>
	 */
	private void emitEnvironment(AgentEventListener listener, String runId) {
		String environment = _config.environment();
		if (environment == null || environment.isBlank()) {
			return;
		}
		emit(listener, new AgentEvent.EnvironmentComposed(runId, 0, now(), environment));
	}

	/**
	 * Reports the per-turn context block this run's turn carries, so the transcript can show it
	 * beside the user's text.
	 *
	 * <p>
	 * Nothing is emitted for a run whose host stated no per-turn fact, which is every installation
	 * that has not written a {@code <fact stable="false">}.
	 * </p>
	 */
	private void emitTurnContext(AgentEventListener listener, String runId) {
		String turnContext = _config.turnContext();
		if (turnContext == null || turnContext.isBlank()) {
			return;
		}
		emit(listener, new AgentEvent.TurnContextComposed(runId, 0, now(), turnContext));
	}

	/**
	 * Makes sure the turn's context block is in the history the next request carries, exactly once.
	 *
	 * <p>
	 * Called from {@link #callProviderOnce} rather than from the run's start, because that is the
	 * one point a request is built: it therefore holds after a compaction too, including the
	 * reactive one that happens between two attempts of the same turn. Requirement of the ticket:
	 * <em>after a summarizing compaction the newest context block must still reach the model</em>,
	 * and a compaction that shrank the preserve zone to nothing would otherwise have dropped it
	 * together with the user message that carried it.
	 * </p>
	 *
	 * <p>
	 * The block goes into the conversation, not into the request alone, so that it stays where it
	 * was put: a history rewritten per request would invalidate the providers' prompt caches on
	 * every turn, which is the very thing that keeps this block out of the system prompt.
	 * </p>
	 *
	 * <p>
	 * It is prepended to the newest {@link UserMessage} rather than inserted as a message of its
	 * own, because a lone message between an assistant's tool calls and their results is rejected by
	 * the provider APIs. Only when a compaction left no user message at all - which a truncating
	 * one does - is the block appended as a message, at the very end, where no such pair can be
	 * broken.
	 * </p>
	 */
	private void ensureTurnContext(Conversation conversation) {
		String block = _config.turnContext();
		if (block == null || block.isBlank()) {
			return;
		}
		List<Message> messages = conversation.messages();
		int newestUser = -1;
		for (int i = messages.size() - 1; i >= 0; i--) {
			if (!(messages.get(i) instanceof UserMessage user)) {
				continue;
			}
			if (carries(user, block)) {
				// Already there - the ordinary case from the second iteration of a turn onwards.
				return;
			}
			if (newestUser < 0) {
				newestUser = i;
			}
		}
		if (newestUser < 0) {
			conversation.add(new UserMessage(UUID.randomUUID().toString(), now(),
				List.of(new TextContent(block)), UserMessage.Origin.FRAMEWORK));
			return;
		}
		UserMessage target = (UserMessage) messages.get(newestUser);
		List<ContentPart> content = new ArrayList<>(target.content().size() + 1);
		content.add(new TextContent(block));
		content.addAll(target.content());
		conversation.replaceRange(newestUser, newestUser + 1,
			List.of(new UserMessage(target.id(), target.timestamp(), content, target.origin())));
	}

	/** Whether the given message already carries the block as one of its content parts. */
	private static boolean carries(UserMessage message, String block) {
		for (ContentPart part : message.content()) {
			if (part instanceof TextContent text && block.equals(text.text())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The system prompt of every request: the run's configured prompt, followed by the standing rule
	 * that tells the model what an {@code <untrusted_*>} block is.
	 *
	 * <p>
	 * Composed here, rather than where a prompt is assembled, because here it cannot be forgotten.
	 * {@link AgentFactory#buildConfig} is one of several places that builds an {@link AgentParams} -
	 * TL-Script's {@code ai.chat} is another, and so is every caller that constructs a loop directly -
	 * while this is the one point at which a system prompt reaches a provider. It is also the loop whose
	 * tool executions emit the tags, so the rule and the tags it explains cannot ship apart.
	 * </p>
	 *
	 * <p>
	 * Recomposed per turn rather than cached in a field: the result is a pure function of the
	 * configuration, so nothing accumulates, and the string is a few hundred characters against a
	 * request that carries the whole conversation.
	 * </p>
	 */
	private String systemPrompt() {
		return UntrustedEnvelope.withStandingRule(_config.systemPrompt());
	}

	/**
	 * Internal handler that bridges {@link StreamHandler} events to {@link AgentEventListener}
	 * callbacks and accumulates data to build an {@link AssistantMessage}.
	 */
	private static class ResponseCollector implements StreamHandler {

		private final AgentEventListener _listener;

		private final String _runId;

		private final int _turnNumber;

		private final CancellationToken _cancellation;

		private String _id;

		private String _finishReason;

		private TokenUsage _usage;

		private List<ContentPart> _content;

		/**
		 * Stream-level error received via
		 * {@link com.top_logic.tl.ai.core.provider.StreamEvent.Error}, if any.
		 */
		private ProviderException _streamError;

		ResponseCollector(AgentEventListener listener, String runId, int turnNumber,
				CancellationToken cancellation) {
			_listener = listener;
			_runId = runId;
			_turnNumber = turnNumber;
			_cancellation = cancellation;
		}

		@Override
		public CancellationToken cancellation() {
			return _cancellation;
		}

		@Override
		public void onEvent(StreamEvent event) {
			if (event instanceof StreamEvent.TextDelta delta) {
				_listener.onEvent(new AgentEvent.TextDelta(_runId, _turnNumber, Instant.now(), delta.text()));
			} else if (event instanceof StreamEvent.ThinkingDelta delta) {
				_listener.onEvent(new AgentEvent.ThinkingDelta(_runId, _turnNumber, Instant.now(), delta.text()));
			} else if (event instanceof StreamEvent.MessageComplete complete) {
				_id = complete.id();
				_finishReason = complete.finishReason();
				_usage = complete.usage();
				_content = complete.content();
			} else if (event instanceof StreamEvent.Error error) {
				// Store for re-throw in toAssistantMessage(); onEvent cannot throw checked exceptions.
				_streamError = new ProviderException(error.kind(), 0, error.message());
			}
			// ToolCallStart, ToolCallDelta, and lifecycle *Start/*End events:
			// accumulated in MessageComplete or not needed here.
		}

		/**
		 * Build the {@link AssistantMessage} from the accumulated stream events.
		 *
		 * @throws ProviderException
		 *         if the stream delivered a
		 *         {@link com.top_logic.tl.ai.core.provider.StreamEvent.Error} event
		 * @throws IllegalStateException
		 *         if no {@link com.top_logic.tl.ai.core.provider.StreamEvent.MessageComplete} was
		 *         received
		 */
		AssistantMessage toAssistantMessage() throws ProviderException {
			if (_streamError != null) {
				throw _streamError;
			}
			if (_content == null) {
				throw new IllegalStateException("No MessageComplete event received.");
			}
			return new AssistantMessage(
				_id != null ? _id : UUID.randomUUID().toString(),
				Instant.now(),
				_content,
				_usage,
				_finishReason != null ? _finishReason : "unknown");
		}
	}
}
