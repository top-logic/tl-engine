/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.session.ConversationLogListener;

/**
 * The chat runs currently in flight, so that any window of the owning account can reach one - not
 * only the window that started it.
 *
 * <p>
 * A run hangs on the {@link ChatProcessor.AIProcessingState} of one browser window's
 * {@link TaskProgressTracker}, and both things a user does to a running run reach it from there.
 * Without this register they reach it only from that one window:
 * </p>
 *
 * <ul>
 * <li><em>Answering</em> what the run asks: a run that asks for an approval, or asks the user a
 * question, parks its thread on a future in the {@link UserInteractionStore} of its state. A second
 * window, a reloaded page whose window is a new one, or the same account signing in again could see
 * the pending request - {@code GET /chat/interactions} reads it from the persisted
 * {@code InteractionRequest} rows and is account-scoped - but had no way to answer it, and the run
 * stayed parked until its timeout (issue #393). {@link #find(Person, String)} finds it by the
 * request id.</li>
 * <li><em>Stopping</em> it: the same window barrier made the stop button of a second tab, or of the
 * page after a reload, silently do nothing, and the run went on spending the account's tokens until
 * it finished by itself (issue #465). {@link #findBySession(Person, String)} finds it by the chat
 * the user is watching.</li>
 * </ul>
 *
 * <p>
 * The register is process-wide and keyed by the run's task id, and every entry names the
 * <em>account</em> the run belongs to. That account is the whole access rule: both lookups answer
 * only for that account, so another account naming the same request or the same chat is told
 * exactly what it is told about one that does not exist - nothing.
 * </p>
 *
 * <p>
 * Entries live for the duration of one run: {@link ChatProcessor} registers a run when it creates
 * its interaction store and drops it in the same {@code finally} that unbinds the store, so a
 * finished, failed or cancelled run leaves nothing behind. Nothing here keeps a window alive - the
 * run's own background task already holds its tracker while it is working.
 * </p>
 *
 * <p>
 * <b>Background subagents</b> are registered the same way, but for as long as each one works rather
 * than for the turn that started it: that turn ends while the subagent goes on, and a question or an
 * approval it raises afterwards waits in the {@link SubagentJob#interactionStore() job's own store}.
 * {@link #subagentListener} registers a job when its work starts, and drops it when it ends - with
 * {@link #forgetEndedOf} also when the window that registered it has stopped listening meanwhile.
 * {@link #findWaiting(Person, String)} finds a request in either kind of store, and
 * {@link #subagentStores(Person, String)} lists the stores of the subagents working for a chat.
 * </p>
 *
 * <p>
 * Every open chat window asks {@link #findBySession} and {@link #subagentStores} on each flush of its
 * stream, many times a second, idle or not. Both registers are therefore also indexed by account, so
 * that such a question reads only the asking account's entries rather than those of every user of
 * the installation, and writes nothing.
 * </p>
 */
public final class PendingInteractionRegistry {

	/**
	 * One run that may be waiting for a human.
	 *
	 * @param taskId
	 *        The run's task id; the key this entry is registered under.
	 * @param responder
	 *        The account whose windows may answer this run's requests. Never <code>null</code>.
	 * @param tracker
	 *        The tracker of the window the run was started in.
	 * @param state
	 *        The run's processing state, holding the {@link UserInteractionStore} the answer
	 *        completes, the conversation log the decision is recorded in and the run's chat, where
	 *        "approve for this session" is recorded - not in the chat of whichever window happens
	 *        to answer.
	 */
	public record PendingRun(String taskId, Person responder, TaskProgressTracker tracker,
			AIProcessingState state) {
		// Plain data.
	}

	/**
	 * One background subagent that may be waiting for a human.
	 *
	 * @param job
	 *        The subagent's job, holding the store its questions and approvals wait in.
	 * @param responder
	 *        The account whose windows may answer it. Never <code>null</code>.
	 * @param tracker
	 *        The tracker of the window whose run started it, which holds the chat "approve for this
	 *        session" is recorded in. <code>null</code> once that window has ended: the subagent
	 *        stays answerable from the account's other windows, but no longer holds the window's
	 *        state in memory.
	 * @param sessionId
	 *        The chat the run that started it belongs to; may be <code>null</code>.
	 */
	public record PendingSubagent(SubagentJob job, Person responder, TaskProgressTracker tracker,
			String sessionId) {
		// Plain data.
	}

	/**
	 * Where a request is waiting, found by {@link PendingInteractionRegistry#findWaiting}.
	 *
	 * @param responder
	 *        The account the request is addressed to.
	 * @param tracker
	 *        The window the asking run was started in; <code>null</code> when that window has ended.
	 * @param chat
	 *        The chat the session-approved tools are recorded in; <code>null</code> when no window
	 *        holds it any more.
	 * @param store
	 *        The store the request waits in; resolving it there unparks the asking thread.
	 * @param logListener
	 *        The conversation log the decision is recorded in, or <code>null</code> when no run of
	 *        the chat is recording - a subagent that asks after its lead's turn has ended.
	 */
	public record Waiting(Person responder, TaskProgressTracker tracker, ChatState chat,
			UserInteractionStore store, ConversationLogListener logListener) {
		// Plain data.
	}

	private static final Map<String, PendingRun> RUNS = new ConcurrentHashMap<>();

	/** The task ids in {@link #RUNS}, by {@link PendingRun#responder()}. */
	private static final Map<Person, Set<String>> RUNS_BY_RESPONDER = new ConcurrentHashMap<>();

	private static final Map<SubagentJob, PendingSubagent> SUBAGENTS = new ConcurrentHashMap<>();

	/** The jobs in {@link #SUBAGENTS}, by {@link PendingSubagent#responder()}. */
	private static final Map<Person, Set<SubagentJob>> SUBAGENTS_BY_RESPONDER = new ConcurrentHashMap<>();

	/**
	 * The subagent registries {@link #ENDED} listens to.
	 *
	 * <p>
	 * Weak, because a registry is forgotten once nobody listens to it and nothing works in it any
	 * more; this set must not keep it.
	 * </p>
	 */
	private static final Set<SubagentJobRegistry> FORGETTING =
		Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	/**
	 * Drops a job when it ends.
	 *
	 * <p>
	 * Apart from the listener of the window that registered the job, because that one is removed
	 * when the window stops listening to the registry - it holds the window - while the job works on.
	 * A job's end is told only to who listens at that moment, so without this listener, which holds
	 * nothing and therefore may stay, an ended job would remain registered.
	 * </p>
	 */
	private static final SubagentJobRegistry.Listener ENDED = new SubagentJobRegistry.Listener() {
		@Override
		public void finished(SubagentJob job) {
			removeSubagent(job);
		}
	};

	private PendingInteractionRegistry() {
		// Static registry.
	}

	/**
	 * Announces a run that the given account may answer and stop from any of its windows.
	 *
	 * <p>
	 * A no-op when the responder is unknown: a run nobody is named as the owner of must not become
	 * answerable and stoppable by everybody. Such a run keeps the behaviour it had before this
	 * register existed - only the window it runs in reaches it.
	 * </p>
	 *
	 * @param taskId
	 *        The run's task id; must not be <code>null</code>.
	 * @param responder
	 *        The account the run is waiting on; may be <code>null</code>.
	 * @param tracker
	 *        The tracker of the window the run was started in.
	 * @param state
	 *        The run's processing state.
	 */
	public static void register(String taskId, Person responder, TaskProgressTracker tracker,
			AIProcessingState state) {
		if (taskId == null || responder == null || tracker == null || state == null) {
			if (taskId != null && responder == null) {
				Logger.info("Run " + taskId + " has no responder; its approvals and questions can be "
					+ "answered only in the window that started it.", PendingInteractionRegistry.class);
			}
			return;
		}
		PendingRun before = RUNS.put(taskId, new PendingRun(taskId, responder, tracker, state));
		if (before != null && !before.responder().equals(responder)) {
			removeFromIndex(RUNS_BY_RESPONDER, before.responder(), taskId);
		}
		addToIndex(RUNS_BY_RESPONDER, responder, taskId);
	}

	/**
	 * Drops the run registered under the given task id; a no-op if there is none.
	 *
	 * @param taskId
	 *        The task id passed to {@link #register}; may be <code>null</code>.
	 */
	public static void unregister(String taskId) {
		if (taskId != null) {
			PendingRun run = RUNS.remove(taskId);
			if (run != null) {
				removeFromIndex(RUNS_BY_RESPONDER, run.responder(), taskId);
			}
		}
	}

	/**
	 * The run waiting for the given request, if it is waiting for this account.
	 *
	 * <p>
	 * Does not take the request: the caller resolves it through the returned run's
	 * {@link UserInteractionStore}, after recording what the user decided.
	 * </p>
	 *
	 * @param responder
	 *        The account that wants to answer; may be <code>null</code>, which finds nothing.
	 * @param requestId
	 *        The correlation id of the approval or question; may be <code>null</code>, which finds
	 *        nothing.
	 * @return The run parked on that request, or <code>null</code> when no run of this account is
	 *         waiting for it - including when the request belongs to somebody else.
	 */
	public static PendingRun find(Person responder, String requestId) {
		if (responder == null || requestId == null) {
			return null;
		}
		for (PendingRun run : runsOf(responder)) {
			UserInteractionStore store = run.state().getUserInteractionStore();
			if (store != null && store.isPending(requestId)) {
				return run;
			}
		}
		return null;
	}

	/**
	 * Where the given request is waiting, if it is waiting for this account: in the store of a run,
	 * or in the store of a background subagent.
	 *
	 * <p>
	 * Does not take the request, like {@link #find(Person, String)}.
	 * </p>
	 *
	 * @param responder
	 *        The account that wants to answer; may be <code>null</code>, which finds nothing.
	 * @param requestId
	 *        The correlation id of the approval or question; may be <code>null</code>, which finds
	 *        nothing.
	 * @return Where the request waits, or <code>null</code> when nothing of this account waits for
	 *         it - including when the request belongs to somebody else.
	 */
	public static Waiting findWaiting(Person responder, String requestId) {
		PendingRun run = find(responder, requestId);
		if (run != null) {
			return new Waiting(run.responder(), run.tracker(), run.state().getChatState(),
				run.state().getUserInteractionStore(), run.state().getLogListener());
		}
		if (responder == null || requestId == null) {
			return null;
		}
		for (PendingSubagent subagent : subagentsOf(responder)) {
			UserInteractionStore store = subagent.job().interactionStore();
			if (store.isPending(requestId)) {
				TaskProgressTracker window = subagent.tracker();
				ChatState chat =
					window == null ? null : window.getSessionManager().getChatState(subagent.sessionId());
				return new Waiting(subagent.responder(), window, chat, store, null);
			}
		}
		return null;
	}

	/**
	 * Makes sure that a job of the given registry is dropped from this register when it ends, whoever
	 * listens to the registry by then.
	 *
	 * <p>
	 * To be called before a {@link #subagentListener} is added to the registry, so that no job is
	 * registered whose end nothing here hears. Idempotent.
	 * </p>
	 *
	 * @param registry
	 *        The registry whose jobs a {@link #subagentListener} is about to register.
	 */
	public static void forgetEndedOf(SubagentJobRegistry registry) {
		if (FORGETTING.add(registry)) {
			registry.addListener(ENDED);
		}
	}

	/**
	 * Registers each job of a chat's subagent registry while it works, so that what it asks can be
	 * shown and answered after the turn that started it has ended.
	 *
	 * <p>
	 * A listener that does nothing when the responder is unknown, for the reason
	 * {@link #register} gives.
	 * </p>
	 *
	 * @param responder
	 *        The account the subagents work for; may be <code>null</code>.
	 * @param tracker
	 *        The window whose run starts them.
	 * @param sessionId
	 *        The chat the run belongs to.
	 */
	public static SubagentJobRegistry.Listener subagentListener(Person responder,
			TaskProgressTracker tracker, String sessionId) {
		if (responder == null || tracker == null) {
			return SubagentJobRegistry.Listener.NONE;
		}
		return new SubagentJobRegistry.Listener() {
			@Override
			public void started(SubagentJob job) {
				SUBAGENTS.put(job, new PendingSubagent(job, responder, tracker, sessionId));
				addToIndex(SUBAGENTS_BY_RESPONDER, responder, job);
			}

			@Override
			public void finished(SubagentJob job) {
				removeSubagent(job);
			}
		};
	}

	/**
	 * The given window has ended: the subagents its runs started stay answerable from the account's
	 * other windows, but no longer refer to it.
	 */
	public static void windowClosed(TaskProgressTracker tracker) {
		SUBAGENTS.replaceAll((job, subagent) -> subagent.tracker() == tracker
			? new PendingSubagent(job, subagent.responder(), null, subagent.sessionId())
			: subagent);
	}

	/**
	 * The stores of the subagents working for the given chat of this account, whichever window
	 * started them.
	 *
	 * @param owner
	 *        The account; may be <code>null</code>, which finds nothing.
	 * @param sessionId
	 *        The chat; may be <code>null</code>, which finds nothing.
	 */
	public static List<UserInteractionStore> subagentStores(Person owner, String sessionId) {
		if (owner == null || sessionId == null) {
			return List.of();
		}
		List<UserInteractionStore> stores = null;
		for (PendingSubagent subagent : subagentsOf(owner)) {
			if (sessionId.equals(subagent.sessionId())) {
				if (stores == null) {
					stores = new ArrayList<>();
				}
				stores.add(subagent.job().interactionStore());
			}
		}
		return stores == null ? List.of() : stores;
	}

	/**
	 * The copies of the given chat that this account's runs and subagents decide by, whichever
	 * window started them.
	 *
	 * <p>
	 * What {@code POST /chat/approval-mode} brings in line besides the copies the windows hold: a run
	 * reads the switch of its own chat at every approval (#943), and the run may have been started
	 * in a window other than the one the switch is changed in.
	 * </p>
	 *
	 * @param owner
	 *        The account; may be <code>null</code>, which finds nothing.
	 * @param sessionId
	 *        The chat; may be <code>null</code>, which finds nothing.
	 */
	public static Set<ChatState> chatsOf(Person owner, String sessionId) {
		if (owner == null || sessionId == null) {
			return Set.of();
		}
		Set<ChatState> chats = Collections.newSetFromMap(new IdentityHashMap<>());
		for (PendingRun run : runsOf(owner)) {
			ChatState chat = run.state().getChatState();
			if (chat != null && sessionId.equals(run.state().getSessionId())) {
				chats.add(chat);
			}
		}
		for (PendingSubagent subagent : subagentsOf(owner)) {
			TaskProgressTracker window = subagent.tracker();
			if (window != null && sessionId.equals(subagent.sessionId())) {
				ChatState chat = window.getSessionManager().getChatState(sessionId);
				if (chat != null) {
					chats.add(chat);
				}
			}
		}
		return chats;
	}

	/**
	 * The run this account has in the given chat, whichever window started it.
	 *
	 * <p>
	 * What {@code POST /chat/cancel} stops. The chat is what the user is looking at when they press
	 * stop, and it is the account's own chat by the time this is asked - the window it runs in is
	 * not something the user can be expected to have kept.
	 * </p>
	 *
	 * <p>
	 * A chat runs one run at a time, so the first match is the answer; a state that has stopped
	 * processing but is not yet unregistered is taken only when nothing live matches, so a dying run
	 * cannot shadow the live one the user means.
	 * </p>
	 *
	 * @param owner
	 *        The account that wants to stop the run; may be <code>null</code>, which finds nothing.
	 * @param sessionId
	 *        The id of the chat the run belongs to; may be <code>null</code>, which finds nothing.
	 * @return The run of that chat, or <code>null</code> when this account has none there -
	 *         including when the chat is somebody else's.
	 */
	public static PendingRun findBySession(Person owner, String sessionId) {
		if (owner == null || sessionId == null) {
			return null;
		}
		PendingRun ending = null;
		for (PendingRun run : runsOf(owner)) {
			if (!sessionId.equals(run.state().getSessionId())) {
				continue;
			}
			if (run.state().isProcessing()) {
				return run;
			}
			if (ending == null) {
				ending = run;
			}
		}
		return ending;
	}

	/** The runs registered for the given account. */
	private static List<PendingRun> runsOf(Person responder) {
		Set<String> taskIds = RUNS_BY_RESPONDER.get(responder);
		if (taskIds == null) {
			return List.of();
		}
		List<PendingRun> runs = new ArrayList<>(taskIds.size());
		for (String taskId : taskIds) {
			PendingRun run = RUNS.get(taskId);
			if (run != null && responder.equals(run.responder())) {
				runs.add(run);
			}
		}
		return runs;
	}

	/**
	 * The subagents registered for the given account that have not ended.
	 *
	 * <p>
	 * One that has ended is left out even before its end is heard: its status turns terminal just
	 * before its listeners are told.
	 * </p>
	 */
	private static List<PendingSubagent> subagentsOf(Person responder) {
		Set<SubagentJob> jobs = SUBAGENTS_BY_RESPONDER.get(responder);
		if (jobs == null) {
			return List.of();
		}
		List<PendingSubagent> subagents = new ArrayList<>(jobs.size());
		for (SubagentJob job : jobs) {
			PendingSubagent subagent = SUBAGENTS.get(job);
			if (subagent != null && !job.status().isTerminal()) {
				subagents.add(subagent);
			}
		}
		return subagents;
	}

	private static void removeSubagent(SubagentJob job) {
		PendingSubagent subagent = SUBAGENTS.remove(job);
		if (subagent != null) {
			removeFromIndex(SUBAGENTS_BY_RESPONDER, subagent.responder(), job);
		}
	}

	/**
	 * Adds the key under the given account, atomically with {@link #removeFromIndex} dropping an
	 * emptied set, so that a concurrent removal cannot swallow the addition.
	 */
	private static <K> void addToIndex(Map<Person, Set<K>> index, Person responder, K key) {
		index.compute(responder, (account, keys) -> {
			Set<K> result = keys == null ? ConcurrentHashMap.newKeySet() : keys;
			result.add(key);
			return result;
		});
	}

	private static <K> void removeFromIndex(Map<Person, Set<K>> index, Person responder, K key) {
		index.computeIfPresent(responder, (account, keys) -> {
			keys.remove(key);
			return keys.isEmpty() ? null : keys;
		});
	}

	/** How many subagents are currently registered. For tests and diagnostics. */
	public static int subagentCount() {
		return SUBAGENTS.size();
	}

	/** How many runs are currently registered. For tests and diagnostics. */
	public static int size() {
		return RUNS.size();
	}
}
