/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.RunCancellationBinding;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.AgentTask;
import com.top_logic.tl.ai.core.execution.model.InteractionRequest;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn;
import com.top_logic.util.model.ModelService;

/**
 * Service that persists and retrieves the <code>tl.ai.execution</code> model:
 * {@link AgentSession}, {@link AgentExecution}, {@link InteractionRequest} and {@link AgentTask}.
 *
 * <p>
 * This is the single write path for agent-run persistence. Any entry point that runs an agent
 * (chat, TL-Script, delegation, background jobs) records its run through this service, so all
 * executions are uniformly queryable regardless of how they were started.
 * </p>
 *
 * <p>
 * All mutating methods run on a dedicated writer thread inside a fresh system interaction with
 * their own {@link Transaction}. Callers therefore need neither a transaction nor a committable
 * KB context — recording works from any caller context, including read-only ones such as
 * TL-Script evaluation during rendering (whose {@code ImmutableDBContext} cannot commit).
 * Queries run on the calling thread and require a bound TopLogic context.
 * </p>
 *
 * <p>
 * <b>Invariant:</b> nothing reachable from a {@code KnowledgeBase} {@code UpdateListener} (or any other
 * synchronous commit hook) may call a mutating method of this service — the caller blocks on the
 * single writer thread, whose commit synchronously notifies those listeners, so such a call
 * closes a deadlock cycle.
 * </p>
 *
 * <p>
 * Callers do <em>not</em> automatically see their own writes: writer commits happen in a
 * separate session, and a subsession's stable view only advances at sanctioned points. The chat
 * servlet advances the session revision at request begin, which covers all HTTP read-back paths.
 * </p>
 */
@ServiceDependencies({
	ModelService.Module.class,
})
public class AgentExecutionStore extends ConfiguredManagedClass<AgentExecutionStore.Config<?>> {

	/**
	 * How an {@link AgentSession} was started. Mirrors the <code>tl.ai.execution:SessionKind</code>
	 * enumeration.
	 */
	public enum SessionKind {
		/** Interactive chat with a person as owner. */
		CHAT,
		/** Background job without an interactive surface. */
		BACKGROUND,
		/** Run triggered by a scheduler. */
		SCHEDULED,
		/** Run started from a TL-Script function. */
		SCRIPT;
	}

	/**
	 * Lifecycle state of an {@link AgentExecution}. Mirrors the
	 * <code>tl.ai.execution:ExecutionStatus</code> enumeration.
	 */
	public enum ExecutionStatus {
		/** The agent loop is currently running (or crashed without reaching a terminal state). */
		RUNNING,
		/** The run finished normally. */
		COMPLETE,
		/** The run failed. */
		ERROR,
		/** The run was cancelled/aborted. */
		CANCELLED,
		/** The run was rejected by an approval decision. */
		REJECTED;
	}

	/**
	 * Kind of human-in-the-loop request. Mirrors the <code>tl.ai.execution:InteractionType</code>
	 * enumeration.
	 */
	public enum InteractionType {
		/** Tool-call approval. */
		APPROVAL,
		/** Free-text question. */
		QUESTION,
		/** Selection from options. */
		CHOICE;
	}

	/**
	 * Lifecycle state of an {@link InteractionRequest}. Mirrors the
	 * <code>tl.ai.execution:InteractionState</code> enumeration.
	 */
	public enum InteractionState {
		/** Waiting for a response. */
		PENDING,
		/** Answered by the responder. */
		RESOLVED,
		/** No response within the allowed time. */
		TIMED_OUT,
		/** Withdrawn, e.g. because the run was aborted. */
		CANCELLED;
	}

	/**
	 * Configuration interface for {@link AgentExecutionStore}.
	 */
	public interface Config<I extends AgentExecutionStore> extends ConfiguredManagedClass.Config<I> {
		// No configurable properties for v1.
	}

	/**
	 * Notified after the writer thread committed a change to the recorded agent data.
	 *
	 * <p>
	 * The seam a live user interface needs: a run writes from a background thread, so nothing in a
	 * browser window learns of the change by itself. A listener is called on the thread that asked
	 * for the write, once the writer thread has committed it, so it must return quickly and must
	 * not write through this service in turn - it hands the work to its own scheduler.
	 * </p>
	 */
	public interface ChangeListener {

		/**
		 * Called after a committed change.
		 *
		 * @param owner
		 *        The account whose data changed, or <code>null</code> when the change belongs to no
		 *        particular account.
		 */
		void onAgentDataChanged(Person owner);
	}

	/**
	 * Maximum seconds a caller waits for its write; recording is best-effort, so a stalled
	 * commit degrades the caller instead of hanging it (and everything serialized behind it).
	 */
	private static final long WRITE_TIMEOUT_SECONDS = 30;

	/** Queue depth above which a warning is logged — a growing queue means a stalled commit. */
	private static final int QUEUE_WARN_THRESHOLD = 25;

	private ThreadPoolExecutor _writer;

	private volatile Thread _writerThread;

	private final CopyOnWriteArrayList<ChangeListener> _changeListeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates an {@link AgentExecutionStore}.
	 */
	public AgentExecutionStore(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_writer = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>(), runnable -> {
				Thread thread = new Thread(runnable, "agent-execution-store");
				thread.setDaemon(true);
				_writerThread = thread;
				return thread;
			});
		cancelOrphanedPendingInteractions();
	}

	/**
	 * Cancels every interaction request still marked {@link InteractionState#PENDING} at startup.
	 *
	 * <p>
	 * No agent run survives a process restart, so a request left pending — e.g. the server was
	 * stopped or the provider service was reconfigured while a user approval was open — is
	 * orphaned: nothing will ever answer it. Left in place it would be re-surfaced to the user on
	 * every visit to that chat and block the composer. Best-effort: a failure here must never
	 * prevent startup.
	 * </p>
	 */
	private void cancelOrphanedPendingInteractions() {
		try {
			int cancelled = write(() -> {
				try (Transaction tx = beginTx("Cancel orphaned pending interaction requests")) {
					TLClassifier pending = interactionStateClassifier(InteractionState.PENDING);
					TLClassifier cancelledState = interactionStateClassifier(InteractionState.CANCELLED);
					Date now = new Date();
					int count = 0;
					for (TLObject obj : allInstances(TlAiExecutionFactory.getInteractionRequestType())) {
						InteractionRequest request = (InteractionRequest) obj;
						if (pending.equals(request.getState())) {
							request.setState(cancelledState);
							request.setResolvedAt(now);
							count++;
						}
					}
					tx.commit();
					return count;
				}
			});
			if (cancelled > 0) {
				Logger.info("Cancelled " + cancelled
					+ " orphaned pending interaction request(s) left by a previous run at startup.",
					AgentExecutionStore.class);
			}
		} catch (RuntimeException ex) {
			Logger.warn("Could not cancel orphaned pending interaction requests at startup: "
				+ ex.getMessage(), ex, AgentExecutionStore.class);
		}
	}

	@Override
	protected void shutDown() {
		if (_writer != null) {
			_writer.shutdown();
			try {
				if (!_writer.awaitTermination(10, TimeUnit.SECONDS)) {
					_writer.shutdownNow();
				}
			} catch (InterruptedException ex) {
				_writer.shutdownNow();
				Thread.currentThread().interrupt();
			}
			_writer = null;
			_writerThread = null;
		}
		super.shutDown();
	}

	/**
	 * Runs the given mutation on the dedicated writer thread inside a fresh system interaction.
	 *
	 * <p>
	 * A fresh interaction always carries a committable KB context, so writes succeed regardless
	 * of the caller's context — including read-only ones (e.g. TL-Script evaluation during
	 * rendering) where a transaction begun on the calling thread could never commit. It also
	 * keeps run recording independent of any business transaction the caller may have open:
	 * a rolled-back caller transaction cannot take the recording down with it.
	 * </p>
	 *
	 * <p>
	 * Cancelling an agent run interrupts its thread, which ends this wait. That is expected and
	 * reported as a {@link CancelledWriteException} so that callers can trace it instead of
	 * logging it as a failure; an interrupt with any other cause stays a plain
	 * {@link RuntimeException}. Either way the mutation is already queued and still commits on the
	 * writer thread - only the wait for its result is given up.
	 * </p>
	 */
	private <T> T write(Supplier<T> mutation) {
		if (Thread.currentThread() == _writerThread) {
			// Defensive: no mutation currently calls another mutator, so this branch is
			// unreachable. If future code makes it reachable, running directly avoids a
			// self-submit deadlock (single writer thread waiting on its own queue).
			return mutation.get();
		}
		ThreadPoolExecutor writer = _writer;
		if (writer == null) {
			throw new IllegalStateException("AgentExecutionStore is not started.");
		}
		int queued = writer.getQueue().size();
		if (queued > QUEUE_WARN_THRESHOLD) {
			Logger.warn("Agent-data writer queue depth is " + queued
				+ " — a commit is stalled or writes outpace the writer.", AgentExecutionStore.class);
		}
		try {
			return writer.submit(
				() -> ThreadContextManager.inSystemInteraction(AgentExecutionStore.class, mutation::get))
				.get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			// Restore the flag before leaving: the interrupt belongs to whoever set it, and code
			// further up the stack (the agent loop's abort checks, later blocking calls) still has
			// to see it.
			Thread.currentThread().interrupt();
			if (RunCancellationBinding.isRunCancelled()) {
				// Expected: the user cancelled this run, which interrupts its thread. The mutation
				// is queued and still commits on the writer thread; only this wait ends early.
				throw new CancelledWriteException(
					"Stopped waiting for the agent-data write: the run was cancelled.", ex);
			}
			throw new RuntimeException("Interrupted while writing agent execution data.", ex);
		} catch (TimeoutException ex) {
			throw new RuntimeException("Timed out after " + WRITE_TIMEOUT_SECONDS
				+ "s waiting for the agent-data writer; the write may still complete.", ex);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof RuntimeException runtime) {
				throw runtime;
			}
			throw new RuntimeException("Failed to write agent execution data: " + cause.getMessage(), cause);
		}
	}

	/**
	 * Registers a listener that is told about every committed change.
	 *
	 * @see ChangeListener
	 */
	public void addChangeListener(ChangeListener listener) {
		if (listener != null) {
			_changeListeners.addIfAbsent(listener);
		}
	}

	/**
	 * Removes a listener registered with {@link #addChangeListener(ChangeListener)}.
	 */
	public void removeChangeListener(ChangeListener listener) {
		_changeListeners.remove(listener);
	}

	/**
	 * Tells the registered listeners that data of {@code owner} changed. Best-effort: a failing
	 * listener is logged and never disturbs the write that caused the notification.
	 */
	private void notifyChanged(Person owner) {
		for (ChangeListener listener : _changeListeners) {
			try {
				listener.onAgentDataChanged(owner);
			} catch (RuntimeException ex) {
				Logger.warn("Agent-data change listener failed: " + ex.getMessage(), ex,
					AgentExecutionStore.class);
			}
		}
	}

	/**
	 * The singleton service instance.
	 */
	public static AgentExecutionStore getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	// ---- AgentSession ----

	/**
	 * Creates a new {@link AgentSession}.
	 *
	 * @param kind
	 *        How the session was started; must not be <code>null</code>.
	 * @param owner
	 *        The owning person; may be <code>null</code> for system-initiated sessions.
	 * @param title
	 *        Display title; may be <code>null</code>.
	 */
	public AgentSession createSession(SessionKind kind, Person owner, String title) {
		AgentSession created = write(() -> {
		try (Transaction tx = beginTx("Create agent session")) {
			AgentSession session = TlAiExecutionFactory.getInstance().createAgentSession();
			session.setKind(kindClassifier(kind));
			if (owner != null) {
				session.setOwner(owner);
			}
			session.setTitle(title);
			Date now = new Date();
			session.setCreatedAt(now);
			session.setLastUpdated(now);
			tx.commit();
			return session;
		}
		});
		// The owner as the caller named it, never read back from the new wrapper: this thread's view
		// of the database predates the row the writer thread just created.
		notifyChanged(owner);
		return created;
	}

	/**
	 * Overwrites the timestamps of a session with the ones it originally had.
	 *
	 * <p>
	 * Needed when a session is imported from an external source: the regular writes
	 * ({@link #saveMetadata(AgentSession, String)}, {@link #finishExecution}, ...) stamp
	 * {@code lastUpdated} with the current time, so the original timestamps have to be restored
	 * afterwards to keep the session in the right place of the history.
	 * </p>
	 *
	 * @param createdAt
	 *        The creation time to store; <code>null</code> keeps the current value.
	 * @param lastUpdated
	 *        The last-modification time to store; <code>null</code> keeps the current value.
	 */
	public void setSessionTimestamps(AgentSession session, Date createdAt, Date lastUpdated) {
		write(() -> {
			try (Transaction tx = beginTx("Adjust agent session timestamps")) {
				if (createdAt != null) {
					session.setCreatedAt(createdAt);
				}
				if (lastUpdated != null) {
					session.setLastUpdated(lastUpdated);
				}
				tx.commit();
			}
			return null;
		});
	}

	/**
	 * Stores the (compacted) conversation memory snapshot of the session.
	 */
	public void saveMemory(AgentSession session, String memoryJson) {
		write(() -> {
			try (Transaction tx = beginTx("Save agent session memory")) {
				session.setMemory(memoryJson);
				session.setLastUpdated(new Date());
				tx.commit();
			}
			return null;
		});
	}

	/**
	 * Stores the session metadata bag (approved tools, approval mode, loaded skills, ...).
	 */
	public void saveMetadata(AgentSession session, String metadataJson) {
		notifyChanged(write(() -> {
			try (Transaction tx = beginTx("Save agent session metadata")) {
				session.setMetadata(metadataJson);
				session.setLastUpdated(new Date());
				tx.commit();
			}
			return session.getOwner();
		}));
	}

	/**
	 * Updates the session title.
	 */
	public void updateTitle(AgentSession session, String title) {
		notifyChanged(write(() -> {
			try (Transaction tx = beginTx("Update agent session title")) {
				session.setTitle(title);
				session.setLastUpdated(new Date());
				tx.commit();
			}
			return session.getOwner();
		}));
	}

	/**
	 * Looks up an {@link AgentSession} by its persistent object id (as produced by
	 * {@link KBUtils#getWrappedObjectName(TLObject)}), or <code>null</code> if none matches.
	 *
	 * <p>
	 * Used to resolve the string handle returned by an async run
	 * ({@code aiAgentChatAsync}) back to its session on a later polling request. Scans the session
	 * instances; polling is infrequent, so a linear scan is acceptable and avoids assumptions about
	 * the id's type-key encoding.
	 * </p>
	 */
	public AgentSession findSessionById(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			return null;
		}
		for (TLObject obj : allInstances(TlAiExecutionFactory.getAgentSessionType())) {
			AgentSession session = (AgentSession) obj;
			if (sessionId.equals(KBUtils.getWrappedObjectName(session).toString())) {
				return session;
			}
		}
		return null;
	}

	/**
	 * All sessions of the given kind owned by the given person, newest first.
	 *
	 * @param kind
	 *        Filter by session kind; <code>null</code> for all kinds.
	 */
	public List<AgentSession> sessionsForOwner(Person owner, SessionKind kind) {
		List<AgentSession> result = new ArrayList<>();
		if (owner == null) {
			return result;
		}
		TLClassifier kindFilter = kind != null ? kindClassifier(kind) : null;
		for (TLObject obj : allInstances(TlAiExecutionFactory.getAgentSessionType())) {
			AgentSession session = (AgentSession) obj;
			if (!owner.equals(session.getOwner())) {
				continue;
			}
			if (kindFilter != null && !kindFilter.equals(session.getKind())) {
				continue;
			}
			result.add(session);
		}
		result.sort(Comparator.comparing(AgentSession::getLastUpdated,
			Comparator.nullsLast(Comparator.reverseOrder())));
		return result;
	}

	// ---- AgentExecution ----

	/**
	 * Records the start of an agent-loop invocation with status {@link ExecutionStatus#RUNNING}.
	 *
	 * <p>
	 * A row that stays {@link ExecutionStatus#RUNNING} without an end date marks a run that
	 * crashed mid-flight.
	 * </p>
	 *
	 * @param session
	 *        The session this run belongs to; must not be <code>null</code>.
	 * @param runId
	 *        The agent loop's run id; must not be <code>null</code>.
	 * @param agentName
	 *        Name of the agent handling the run; must not be <code>null</code>.
	 * @param model
	 *        The LLM model name; may be <code>null</code>.
	 * @param provider
	 *        The LLM provider id; may be <code>null</code>.
	 * @param parent
	 *        The delegating execution for sub-agent runs; <code>null</code> for top-level runs.
	 */
	public AgentExecution beginExecution(AgentSession session, String runId, String agentName,
			String model, String provider, AgentExecution parent) {
		return write(() -> {
		try (Transaction tx = beginTx("Begin agent execution " + runId)) {
			AgentExecution execution = TlAiExecutionFactory.getInstance().createAgentExecution();
			execution.setSession(session);
			execution.setRunId(runId);
			execution.setAgentName(agentName);
			execution.setModel(model);
			execution.setProvider(provider);
			execution.setStatus(statusClassifier(ExecutionStatus.RUNNING));
			execution.setStartedAt(new Date());
			if (parent != null) {
				execution.setParentExecution(parent);
			}
			session.setLastUpdated(new Date());
			tx.commit();
			return execution;
		}
		});
	}

	/**
	 * Records the terminal state of an agent-loop invocation without a final answer.
	 *
	 * @see #finishExecution(AgentExecution, ExecutionStatus, StopReason, String, TokenUsage,
	 *      String, String)
	 */
	public void finishExecution(AgentExecution execution, ExecutionStatus status,
			StopReason stopReason, String errorMessage, TokenUsage usage, String transcript) {
		finishExecution(execution, status, stopReason, errorMessage, usage, transcript, null);
	}

	/**
	 * Records the terminal state of an agent-loop invocation.
	 *
	 * @param status
	 *        Terminal status; must not be <code>null</code>.
	 * @param stopReason
	 *        Why the loop stopped; may be <code>null</code> if unknown.
	 * @param errorMessage
	 *        Failure details for {@link ExecutionStatus#ERROR} runs; may be <code>null</code>.
	 * @param usage
	 *        Total token usage of this run (excluding child executions); may be
	 *        <code>null</code>.
	 * @param transcript
	 *        The serialized per-run conversation-log transcript; may be <code>null</code>.
	 * @param response
	 *        The agent's final answer text (the same value the synchronous entry points return);
	 *        may be <code>null</code> when the run produced no answer (e.g. it failed).
	 *
	 * <p>
	 * Interaction requests of this execution still pending at run end (e.g. an approval nobody
	 * answered before the run was cancelled) are marked {@link InteractionState#CANCELLED}.
	 * </p>
	 */
	public void finishExecution(AgentExecution execution, ExecutionStatus status,
			StopReason stopReason, String errorMessage, TokenUsage usage, String transcript,
			String response) {
		write(() -> {
		try (Transaction tx = beginTx("Finish agent execution " + execution.getRunId())) {
			cancelPendingInteractions(execution);
			execution.setStatus(statusClassifier(status));
			execution.setStopReason(stopReason != null ? stopReason.name() : null);
			execution.setErrorMessage(errorMessage);
			if (usage != null) {
				execution.setInputTokens((long) usage.inputTokens());
				execution.setOutputTokens((long) usage.outputTokens());
				execution.setThinkingTokens((long) usage.thinkingTokens());
				execution.setCacheReadTokens((long) usage.cacheReadTokens());
				execution.setCacheWriteTokens((long) usage.cacheWriteTokens());
			}
			execution.setTranscript(transcript);
			execution.setResponse(response);
			execution.setEndedAt(new Date());
			AgentSession session = execution.getSession();
			session.setLastUpdated(new Date());
			tx.commit();
		}
		return null;
		});
	}

	/**
	 * Requests cancellation of a running agent execution by setting and committing its persisted
	 * {@link AgentExecution#getCancelRequested() cancel marker}.
	 *
	 * <p>
	 * This is the cluster-safe half of async cancellation: the request is a normal KB write, so it
	 * works from any node - the node that receives the cancel need not be the node running the job.
	 * The node actually running the job observes the committed marker at HEAD (see
	 * {@link #isCancelRequested(AgentExecution)}) and cancels its in-memory
	 * {@link com.top_logic.tl.ai.tool.api.CancellationToken}, which the agent loop already honors.
	 * </p>
	 *
	 * <p>
	 * A no-op for a run that has already reached a terminal state: there is nothing left to cancel.
	 * The current status is (re-)read at HEAD on the writer thread so a concurrently finishing run
	 * is seen correctly regardless of the caller's pinned revision.
	 * </p>
	 *
	 * @param execution
	 *        The execution to cancel; must not be <code>null</code>.
	 * @return <code>true</code> if a still-running execution was newly signalled to cancel;
	 *         <code>false</code> if it was already terminal (or already signalled).
	 */
	public boolean requestCancel(AgentExecution execution) {
		return write(() -> {
			if (isTerminal(execution) || execution.getCancelRequested()) {
				return Boolean.FALSE;
			}
			try (Transaction tx = beginTx("Request cancel of agent execution " + execution.getRunId())) {
				execution.setCancelRequested(true);
				tx.commit();
			}
			return Boolean.TRUE;
		});
	}

	/**
	 * Whether the given execution's persisted cancel marker is set, read at KB HEAD.
	 *
	 * <p>
	 * Read on the writer thread inside a fresh system interaction so the marker committed by a
	 * (possibly remote) {@link #requestCancel(AgentExecution)} is visible even though the caller -
	 * typically the agent-loop thread - reads at an older pinned request revision.
	 * </p>
	 */
	public boolean isCancelRequested(AgentExecution execution) {
		return write(() -> Boolean.valueOf(execution.getCancelRequested())).booleanValue();
	}

	/**
	 * Whether the execution's status (read in the current interaction) is a terminal one.
	 */
	private boolean isTerminal(AgentExecution execution) {
		TLClassifier status = execution.getStatus();
		return status != null && !ExecutionStatus.RUNNING.name().equals(status.getName());
	}

	/**
	 * Looks up an execution by its run id, or <code>null</code> if none exists.
	 */
	public AgentExecution findExecutionByRunId(String runId) {
		if (runId == null || runId.isEmpty()) {
			return null;
		}
		for (TLObject obj : allInstances(TlAiExecutionFactory.getAgentExecutionType())) {
			AgentExecution execution = (AgentExecution) obj;
			if (runId.equals(execution.getRunId())) {
				return execution;
			}
		}
		return null;
	}

	/**
	 * All executions of the given session, oldest first.
	 */
	public List<AgentExecution> executionsOf(AgentSession session) {
		List<AgentExecution> result = new ArrayList<>();
		for (TLObject obj : allInstances(TlAiExecutionFactory.getAgentExecutionType())) {
			AgentExecution execution = (AgentExecution) obj;
			if (session.equals(execution.getSession())) {
				result.add(execution);
			}
		}
		result.sort(Comparator.comparing(AgentExecution::getStartedAt,
			Comparator.nullsLast(Comparator.naturalOrder())));
		return result;
	}

	/**
	 * Drops the tail of a session's recorded conversation.
	 *
	 * <p>
	 * Used when the user regenerates an answer or edits an earlier message: what followed was said
	 * in reply to a turn that is going away, so it cannot stay. Runs are walked in start order and
	 * their transcripts rewritten to the turns that still fit within {@code keepTurns}; a run left
	 * with nothing is deleted outright, together with the interaction requests recorded under it,
	 * so nothing dangles.
	 * </p>
	 *
	 * @param session
	 *        the session to truncate; {@code null} is a no-op
	 * @param keepTurns
	 *        how many turns to keep, counted from the start of the session
	 * @return how many turns were removed
	 */
	public int truncateConversation(AgentSession session, int keepTurns) {
		if (session == null || keepTurns < 0) {
			return 0;
		}
		return write(() -> {
		try (Transaction tx = beginTx("Truncate agent session")) {
			int seen = 0;
			int removed = 0;
			List<AgentExecution> doomed = new ArrayList<>();
			for (AgentExecution execution : executionsOf(session)) {
				if (execution.getParentExecution() != null) {
					// A sub-agent run is not part of the chat stream; it goes with its parent.
					continue;
				}
				String transcript = execution.getTranscript();
				if (transcript == null || transcript.isEmpty()) {
					continue;
				}
				PersistedConversationV2 conversation;
				try {
					conversation = PersistedConversationV2
						.readPersistedConversationV2(new JsonReader(new StringR(transcript)));
				} catch (IOException | RuntimeException ex) {
					Logger.warn("Skipping unreadable transcript of run " + execution.getRunId()
						+ " while truncating: " + ex.getMessage(), AgentExecutionStore.class);
					continue;
				}
				List<PersistedTurn> turns = conversation.getTurns();
				int keepHere = Math.max(0, Math.min(turns.size(), keepTurns - seen));
				seen += turns.size();
				if (keepHere == turns.size()) {
					continue;
				}
				removed += turns.size() - keepHere;
				if (keepHere == 0) {
					doomed.add(execution);
					continue;
				}
				execution.setTranscript(writeTurns(turns.subList(0, keepHere)));
			}
			if (!doomed.isEmpty()) {
				deleteExecutions(doomed);
			}
			if (removed > 0) {
				session.setLastUpdated(new Date());
			}
			tx.commit();
			return Integer.valueOf(removed);
		}
		}).intValue();
	}

	/** Deletes runs together with everything recorded under them. */
	private void deleteExecutions(List<AgentExecution> executions) {
		Set<AgentExecution> family = new LinkedHashSet<>(executions);
		for (TLObject obj : allInstances(TlAiExecutionFactory.getAgentExecutionType())) {
			AgentExecution child = (AgentExecution) obj;
			if (family.contains(child.getParentExecution())) {
				family.add(child);
			}
		}
		for (TLObject obj : allInstances(TlAiExecutionFactory.getInteractionRequestType())) {
			InteractionRequest request = (InteractionRequest) obj;
			if (family.contains(request.getExecution())) {
				request.tDelete();
			}
		}
		for (AgentExecution execution : family) {
			execution.tDelete();
		}
	}

	private static String writeTurns(List<PersistedTurn> turns) {
		PersistedConversationV2 kept = PersistedConversationV2.create();
		for (PersistedTurn turn : turns) {
			kept.addTurn(turn);
		}
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			kept.writeTo(json);
		} catch (IOException ex) {
			throw new RuntimeException("Cannot serialize the kept turns.", ex);
		}
		return buffer.toString();
	}

	/**
	 * Deletes the given session with everything recorded under it: tasks, the interaction
	 * requests of its executions, the executions themselves, and finally the session.
	 */
	public void deleteSession(AgentSession session) {
		if (session == null) {
			return;
		}
		notifyChanged(write(() -> {
		try (Transaction tx = beginTx("Delete agent session")) {
			// Read before the delete: afterwards the wrapper has no owner to report any more.
			Person owner = session.getOwner();
			List<AgentExecution> executions = executionsOf(session);
			for (TLObject obj : allInstances(TlAiExecutionFactory.getInteractionRequestType())) {
				InteractionRequest request = (InteractionRequest) obj;
				if (executions.contains(request.getExecution())) {
					request.tDelete();
				}
			}
			for (TLObject obj : allInstances(TlAiExecutionFactory.getAgentTaskType())) {
				AgentTask task = (AgentTask) obj;
				if (session.equals(task.getSession())) {
					task.tDelete();
				}
			}
			for (AgentExecution execution : executions) {
				execution.tDelete();
			}
			session.tDelete();
			tx.commit();
			return owner;
		}
		}));
	}

	/**
	 * Total token usage of the given execution including all (transitive) child executions.
	 *
	 * <p>
	 * This is the "true cost" of a delegating run: the parent's own tokens plus everything its
	 * sub-agents consumed.
	 * </p>
	 */
	public TokenUsage totalUsageWithChildren(AgentExecution root) {
		List<AgentExecution> all = new ArrayList<>();
		for (TLObject obj : allInstances(TlAiExecutionFactory.getAgentExecutionType())) {
			all.add((AgentExecution) obj);
		}
		TokenUsage sum = usageOf(root);
		List<AgentExecution> frontier = new ArrayList<>(List.of(root));
		while (!frontier.isEmpty()) {
			List<AgentExecution> next = new ArrayList<>();
			for (AgentExecution execution : all) {
				if (frontier.contains(execution.getParentExecution())) {
					sum = sum.add(usageOf(execution));
					next.add(execution);
				}
			}
			frontier = next;
		}
		return sum;
	}

	// ---- InteractionRequest ----

	/**
	 * Records a pending human-in-the-loop request.
	 *
	 * @param execution
	 *        The execution waiting for the response; must not be <code>null</code>.
	 * @param type
	 *        The interaction kind; must not be <code>null</code>.
	 * @param requestId
	 *        Correlation id used by the surface presenting the request; must not be
	 *        <code>null</code>.
	 * @param header
	 *        Short label (e.g. "Approval"); may be <code>null</code>.
	 * @param prompt
	 *        The question or approval text presented to the responder; may be <code>null</code>.
	 * @param payload
	 *        Structured request data, e.g. the tool name and the actual argument JSON of an
	 *        approval; may be <code>null</code>.
	 */
	public InteractionRequest createInteraction(AgentExecution execution, InteractionType type,
			String requestId, String header, String prompt, String payload) {
		return write(() -> {
		try (Transaction tx = beginTx("Create interaction request " + requestId)) {
			InteractionRequest request = TlAiExecutionFactory.getInstance().createInteractionRequest();
			request.setExecution(execution);
			request.setType(interactionTypeClassifier(type));
			request.setState(interactionStateClassifier(InteractionState.PENDING));
			request.setRequestId(requestId);
			request.setHeader(header);
			request.setPrompt(prompt);
			request.setPayload(payload);
			// The session owner is the person expected to respond (for chat sessions the
			// chatting user); a session without an owner leaves the request open to any user.
			// Resolved here on the writer thread, whose fresh system interaction sees KB HEAD and
			// thus the just-recorded execution/session — unlike the calling agent thread, whose
			// revision view is intentionally pinned to before the execution was recorded.
			AgentSession session = execution.getSession();
			Person responder = session == null ? null : session.getOwner();
			if (responder != null) {
				request.setResponder(responder);
			}
			request.setCreatedAt(new Date());
			tx.commit();
			return request;
		}
		});
	}

	/**
	 * Records the outcome of a human-in-the-loop request, located by its {@code requestId}.
	 *
	 * <p>
	 * The row is looked up and updated entirely on the writer thread, whose fresh system
	 * interaction sees KB HEAD. Callers therefore pass the stable {@code requestId} string
	 * (captured when the request was created) rather than a persistent {@link InteractionRequest}
	 * wrapper: the calling agent/tool thread's KB view is pinned to a revision that predates the
	 * row's creation, so dereferencing such a wrapper on that thread throws "created after access
	 * revision N". A request id that matches no persisted row (best-effort recording never
	 * committed it, or it was already removed) is a silent no-op.
	 * </p>
	 *
	 * @param requestId
	 *        Correlation id of the request to resolve; must not be <code>null</code>.
	 * @param state
	 *        Terminal state; must not be {@link InteractionState#PENDING}.
	 * @param resolution
	 *        The response data (decision, answer text, selected options); may be
	 *        <code>null</code>.
	 */
	public void resolveInteraction(String requestId, InteractionState state, String resolution) {
		if (state == InteractionState.PENDING) {
			throw new IllegalArgumentException("Resolution state must be terminal, not PENDING.");
		}
		write(() -> {
			InteractionRequest request = findInteractionByRequestId(requestId);
			if (request == null) {
				// Row was never persisted (best-effort) or is already gone: nothing to resolve.
				return null;
			}
			try (Transaction tx = beginTx("Resolve interaction request " + requestId)) {
				request.setState(interactionStateClassifier(state));
				request.setResolution(resolution);
				request.setResolvedAt(new Date());
				tx.commit();
			}
			return null;
		});
	}

	/**
	 * Finds the pending/resolved interaction row with the given correlation id, or
	 * <code>null</code> if none exists. Must run on the writer thread (KB HEAD) so a row committed
	 * after the caller's pinned revision is still found.
	 */
	private InteractionRequest findInteractionByRequestId(String requestId) {
		if (requestId == null) {
			return null;
		}
		for (TLObject obj : allInstances(TlAiExecutionFactory.getInteractionRequestType())) {
			InteractionRequest request = (InteractionRequest) obj;
			if (requestId.equals(request.getRequestId())) {
				return request;
			}
		}
		return null;
	}

	/**
	 * All pending interaction requests addressed to the given person, oldest first.
	 *
	 * <p>
	 * Includes requests without a dedicated responder (any user may respond to those).
	 * </p>
	 */
	public List<InteractionRequest> pendingInteractionsFor(Person responder) {
		TLClassifier pending = interactionStateClassifier(InteractionState.PENDING);
		List<InteractionRequest> result = new ArrayList<>();
		for (TLObject obj : allInstances(TlAiExecutionFactory.getInteractionRequestType())) {
			InteractionRequest request = (InteractionRequest) obj;
			if (!pending.equals(request.getState())) {
				continue;
			}
			Person requestResponder = request.getResponder();
			if (requestResponder == null || requestResponder.equals(responder)) {
				result.add(request);
			}
		}
		result.sort(Comparator.comparing(InteractionRequest::getCreatedAt,
			Comparator.nullsLast(Comparator.naturalOrder())));
		return result;
	}

	// ---- AgentTask ----

	/**
	 * The wanted state of one task of a session's todo plan - what a
	 * {@link #syncTasks(AgentSession, List, AgentExecution) sync} makes the model say.
	 *
	 * <p>
	 * Plain data, so the caller can build it on an agent thread without touching the
	 * {@link com.top_logic.knowledge.service.KnowledgeBase}: the whole reconciliation - reading the
	 * existing rows and writing the difference - happens on the writer thread, which sees HEAD.
	 * </p>
	 *
	 * @param taskId
	 *        The identity of the task within its session; the key the sync matches rows on.
	 * @param subject
	 *        Brief title, or <code>null</code>.
	 * @param content
	 *        The detailed description, or <code>null</code>.
	 * @param status
	 *        The task's status word, or <code>null</code>.
	 * @param ordinal
	 *        The task's one-based position in the plan.
	 */
	public record TaskState(String taskId, String subject, String content, String status, int ordinal) {
		// A transport record; see the type comment.
	}

	/**
	 * Makes the session's persistent todo plan say exactly what {@code desired} says: missing tasks
	 * are created, differing ones updated, and tasks that are no longer wanted deleted - all in one
	 * transaction.
	 *
	 * <p>
	 * The caller passes plain {@link TaskState} data rather than persistent wrappers: rows are looked
	 * up, compared and written on the writer thread, so no wrapper is ever dereferenced on the
	 * (revision-pinned) agent thread that asked for the sync. Matching is by
	 * {@link TaskState#taskId()} within the session, which makes the call idempotent - repeating it
	 * writes nothing.
	 * </p>
	 *
	 * @param session
	 *        The session whose plan is synchronized; <code>null</code> is a no-op.
	 * @param desired
	 *        The tasks the plan should consist of, in plan order; empty clears it.
	 * @param touchedBy
	 *        The execution to attribute created and changed rows to; may be <code>null</code>.
	 * @return Whether anything was actually written.
	 */
	public boolean syncTasks(AgentSession session, List<TaskState> desired, AgentExecution touchedBy) {
		if (session == null) {
			return false;
		}
		List<TaskState> wanted = desired == null ? List.of() : desired;
		// Read on the writer thread with the rest: the caller may be an agent thread whose view of
		// the database predates the session row.
		Person[] owner = { null };
		boolean changed = write(() -> {
			try (Transaction tx = beginTx("Sync agent tasks")) {
				if (!reconcileTasks(session, wanted, touchedBy)) {
					// Nothing to say: committing an empty transaction would only burn a revision.
					return Boolean.FALSE;
				}
				owner[0] = session.getOwner();
				tx.commit();
				return Boolean.TRUE;
			}
		}).booleanValue();
		if (changed) {
			notifyChanged(owner[0]);
		}
		return changed;
	}

	/**
	 * Writes the difference between the session's existing task rows and {@code desired} inside the
	 * caller's transaction; answers whether anything changed.
	 */
	private boolean reconcileTasks(AgentSession session, List<TaskState> desired,
			AgentExecution touchedBy) {
		Map<String, AgentTask> existing = new LinkedHashMap<>();
		List<AgentTask> obsolete = new ArrayList<>();
		for (AgentTask row : tasksOf(session)) {
			if (existing.putIfAbsent(row.getTaskId(), row) != null) {
				// One row per task id is the invariant; a duplicate is a leftover and goes.
				obsolete.add(row);
			}
		}

		boolean changed = false;
		for (TaskState state : desired) {
			AgentTask row = existing.remove(state.taskId());
			if (row == null) {
				row = TlAiExecutionFactory.getInstance().createAgentTask();
				row.setSession(session);
				row.setTaskId(state.taskId());
				row.setSubject(state.subject());
				row.setContent(state.content());
				row.setStatus(state.status());
				row.setOrdinal(Integer.valueOf(state.ordinal()));
				row.setLastTouchedByExecution(touchedBy);
				changed = true;
				continue;
			}
			changed |= applyTaskState(row, state, touchedBy);
		}

		obsolete.addAll(existing.values());
		for (AgentTask row : obsolete) {
			row.tDelete();
			changed = true;
		}
		return changed;
	}

	/** Applies {@code state} to an existing row, answering whether it changed anything. */
	private static boolean applyTaskState(AgentTask row, TaskState state, AgentExecution touchedBy) {
		boolean changed = false;
		if (!Objects.equals(row.getSubject(), state.subject())) {
			row.setSubject(state.subject());
			changed = true;
		}
		if (!Objects.equals(row.getContent(), state.content())) {
			row.setContent(state.content());
			changed = true;
		}
		if (!Objects.equals(row.getStatus(), state.status())) {
			row.setStatus(state.status());
			changed = true;
		}
		Integer ordinal = Integer.valueOf(state.ordinal());
		if (!ordinal.equals(row.getOrdinal())) {
			row.setOrdinal(ordinal);
			changed = true;
		}
		if (touchedBy != null && changed && !touchedBy.equals(row.getLastTouchedByExecution())) {
			row.setLastTouchedByExecution(touchedBy);
		}
		return changed;
	}

	/**
	 * All tasks of the given session, in plan order.
	 */
	public List<AgentTask> tasksOf(AgentSession session) {
		List<AgentTask> result = new ArrayList<>();
		for (TLObject obj : allInstances(TlAiExecutionFactory.getAgentTaskType())) {
			AgentTask task = (AgentTask) obj;
			if (session.equals(task.getSession())) {
				result.add(task);
			}
		}
		result.sort(Comparator.comparing(AgentTask::getOrdinal,
			Comparator.nullsLast(Comparator.naturalOrder())));
		return result;
	}

	// ---- Helpers ----

	/**
	 * Marks all still-pending interaction requests of the given execution as cancelled. Runs
	 * inside the caller's transaction.
	 */
	private void cancelPendingInteractions(AgentExecution execution) {
		TLClassifier pending = interactionStateClassifier(InteractionState.PENDING);
		Date now = new Date();
		for (TLObject obj : allInstances(TlAiExecutionFactory.getInteractionRequestType())) {
			InteractionRequest request = (InteractionRequest) obj;
			if (execution.equals(request.getExecution()) && pending.equals(request.getState())) {
				request.setState(interactionStateClassifier(InteractionState.CANCELLED));
				request.setResolvedAt(now);
			}
		}
	}

	private static Transaction beginTx(String message) {
		return PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text(message));
	}

	private static List<TLObject> allInstances(com.top_logic.model.TLClass type) {
		return MetaElementUtil.getAllInstancesOf(type, TLObject.class);
	}

	private static TokenUsage usageOf(AgentExecution execution) {
		return new TokenUsage(
			intValue(execution.getInputTokens()),
			intValue(execution.getOutputTokens()),
			intValue(execution.getThinkingTokens()),
			intValue(execution.getCacheReadTokens()),
			intValue(execution.getCacheWriteTokens()));
	}

	private static int intValue(Long value) {
		return value != null ? value.intValue() : 0;
	}

	private static TLClassifier kindClassifier(SessionKind kind) {
		switch (kind) {
			case CHAT:
				return TlAiExecutionFactory.getCHATSessionKindClassifier();
			case BACKGROUND:
				return TlAiExecutionFactory.getBACKGROUNDSessionKindClassifier();
			case SCHEDULED:
				return TlAiExecutionFactory.getSCHEDULEDSessionKindClassifier();
			case SCRIPT:
				return TlAiExecutionFactory.getSCRIPTSessionKindClassifier();
		}
		throw new IllegalArgumentException("Unknown session kind: " + kind);
	}

	private static TLClassifier statusClassifier(ExecutionStatus status) {
		switch (status) {
			case RUNNING:
				return TlAiExecutionFactory.getRUNNINGExecutionStatusClassifier();
			case COMPLETE:
				return TlAiExecutionFactory.getCOMPLETEExecutionStatusClassifier();
			case ERROR:
				return TlAiExecutionFactory.getERRORExecutionStatusClassifier();
			case CANCELLED:
				return TlAiExecutionFactory.getCANCELLEDExecutionStatusClassifier();
			case REJECTED:
				return TlAiExecutionFactory.getREJECTEDExecutionStatusClassifier();
		}
		throw new IllegalArgumentException("Unknown execution status: " + status);
	}

	private static TLClassifier interactionTypeClassifier(InteractionType type) {
		switch (type) {
			case APPROVAL:
				return TlAiExecutionFactory.getAPPROVALInteractionTypeClassifier();
			case QUESTION:
				return TlAiExecutionFactory.getQUESTIONInteractionTypeClassifier();
			case CHOICE:
				return TlAiExecutionFactory.getCHOICEInteractionTypeClassifier();
		}
		throw new IllegalArgumentException("Unknown interaction type: " + type);
	}

	private static TLClassifier interactionStateClassifier(InteractionState state) {
		switch (state) {
			case PENDING:
				return TlAiExecutionFactory.getPENDINGInteractionStateClassifier();
			case RESOLVED:
				return TlAiExecutionFactory.getRESOLVEDInteractionStateClassifier();
			case TIMED_OUT:
				return TlAiExecutionFactory.getTIMEDOUTInteractionStateClassifier();
			case CANCELLED:
				return TlAiExecutionFactory.getCANCELLEDInteractionStateClassifier();
		}
		throw new IllegalArgumentException("Unknown interaction state: " + state);
	}

	/**
	 * Module for {@link AgentExecutionStore}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentExecutionStore> {

		/** Singleton. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<AgentExecutionStore> getImplementation() {
			return AgentExecutionStore.class;
		}
	}
}
