/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Per-session registry of asynchronous subagent jobs. Tracks each {@link SubagentJob}
 * by id, enforces a concurrency cap - its own and the installation's - and hands undelivered results
 * back to the parent.
 *
 * <p>This class is the single source of truth for subagent state within one chat session.</p>
 */
public class SubagentJobRegistry {

	/** Default maximum concurrently-running subagents per session. */
	public static final int DEFAULT_MAX_CONCURRENT = 4;

	private final int _maxConcurrent;

	private final Map<String, SubagentJob> _jobs = new ConcurrentHashMap<>();

	private final AtomicInteger _counter = new AtomicInteger();

	private final ExecutorService _executor =
		Executors.newCachedThreadPool(r -> {
			Thread t = new Thread(r, "tl-ai-subagent");
			t.setDaemon(true);
			return t;
		});

	private final Object _completionLock = new Object();

	/** Jobs registered for a start and not started yet, oldest first; guarded by {@code this}. */
	private final Deque<Pending> _queue = new ArrayDeque<>();

	/** Jobs started and not ended; guarded by {@code this}. */
	private int _started;

	private record Pending(SubagentJob job, SubagentRun run, Runnable notStarted) {
		// Plain data.
	}

	private volatile Listener _listener = Listener.NONE;

	/** Counts the questions asked to the lead, so that a waiting lead notices a new one. */
	private int _questionsAsked;

	/** */
	public SubagentJobRegistry() {
		this(DEFAULT_MAX_CONCURRENT);
	}

	/** */
	public SubagentJobRegistry(int maxConcurrent) {
		_maxConcurrent = maxConcurrent;
	}

	/**
	 * Told when a job's work starts and when it ends.
	 *
	 * <p>
	 * For what has to know about a job while it runs and outlive the turn that started it - in the
	 * chat, the register that lets any window of the account find and answer what a subagent asks.
	 * Called on the job's own thread, so an implementation must be quick and must not throw.
	 * </p>
	 */
	public interface Listener {

		/** Hears nothing. */
		Listener NONE = new Listener() {
			// Default methods only.
		};

		/** The job's work is about to run; its status is {@link SubagentJobStatus#RUNNING}. */
		default void started(SubagentJob job) {
			// Nothing by default.
		}

		/**
		 * The job's work has ended, whichever way; its status is terminal. Called exactly once for
		 * every {@link #started(SubagentJob)}.
		 */
		default void finished(SubagentJob job) {
			// Nothing by default.
		}
	}

	/**
	 * Sets what is told about the jobs that start from now on.
	 *
	 * @param listener
	 *        The listener; <code>null</code> for none.
	 */
	public void setListener(Listener listener) {
		_listener = listener == null ? Listener.NONE : listener;
	}

	/** Maximum concurrently-running subagents. */
	public int maxConcurrent() {
		return _maxConcurrent;
	}

	/** Registers a new QUEUED job and returns it. */
	public SubagentJob register(String targetAgentName, String taskBrief, CancellationToken token) {
		String id = "sa-" + _counter.incrementAndGet();
		SubagentJob job = new SubagentJob(id, targetAgentName, taskBrief, token);
		job.setQuestionSignal(this::questionAsked);
		_jobs.put(id, job);
		return job;
	}

	/** The job with the given id, or {@code null}. */
	public SubagentJob get(String id) {
		return _jobs.get(id);
	}

	/** All jobs currently in any of the given states, in registration order. */
	public List<SubagentJob> byStatus(SubagentJobStatus... states) {
		List<SubagentJob> result = new ArrayList<>();
		for (SubagentJob job : ordered()) {
			for (SubagentJobStatus s : states) {
				if (job.status() == s) {
					result.add(job);
					break;
				}
			}
		}
		return result;
	}

	/** Number of jobs currently RUNNING. */
	public int runningCount() {
		return byStatus(SubagentJobStatus.RUNNING).size();
	}

	/** Whether a new job may start now without exceeding this registry's cap. */
	public synchronized boolean canStart() {
		return _started < _maxConcurrent;
	}

	/**
	 * Where the given job stands in this registry's queue: {@code 1} for the next to start,
	 * {@code 0} when it is not waiting.
	 */
	public synchronized int queuePosition(SubagentJob job) {
		int position = 1;
		for (Pending pending : _queue) {
			if (pending.job() == job) {
				return position;
			}
			position++;
		}
		return 0;
	}

	/**
	 * Cancels every job that is not yet terminal by firing its {@link SubagentJob#cancellationToken()
	 * cancellation token}. Used when the parent/orchestrator run is cancelled so its still-running
	 * subagents terminate instead of hanging (including any parked on a user question, whose
	 * ask-user wait the token's listener unblocks).
	 *
	 * @return the number of jobs whose token was cancelled
	 */
	public int cancelAll() {
		int cancelled = 0;
		for (SubagentJob job : byStatus(SubagentJobStatus.QUEUED, SubagentJobStatus.RUNNING)) {
			job.cancellationToken().cancel();
			cancelled++;
		}
		return cancelled;
	}

	/**
	 * Returns all jobs that are terminal and not yet delivered, marking each delivered.
	 * Used by the parent loop's top-of-turn injection.
	 */
	public List<SubagentJob> takeUndeliveredResults() {
		List<SubagentJob> result = new ArrayList<>();
		for (SubagentJob job : ordered()) {
			if (job.status().isTerminal() && !job.isDelivered()) {
				job.markDelivered();
				result.add(job);
			}
		}
		return result;
	}

	/**
	 * The questions to the lead no lead turn has been shown yet, in registration order of the asking
	 * jobs, each marked as shown.
	 */
	public List<SubagentJob> takeUndeliveredQuestions() {
		List<SubagentJob> asking = new ArrayList<>();
		for (SubagentJob job : ordered()) {
			LeadQuestion asked = job.leadQuestion();
			if (asked != null && !asked.answer().isDone() && asked.markDelivered()) {
				asking.add(job);
			}
		}
		return asking;
	}

	/** Wakes a lead that waits in {@link #awaitBlocking}, because a subagent asked it something. */
	private void questionAsked() {
		synchronized (_completionLock) {
			_questionsAsked++;
			_completionLock.notifyAll();
		}
	}

	/**
	 * Returns the results of the given jobs that are already terminal, marking them delivered.
	 * Blocking-until-complete behavior is layered on in {@link #awaitBlocking} (later step);
	 * this base form returns only the currently-terminal subset.
	 */
	public List<SubagentResult> await(List<String> ids) {
		List<SubagentResult> results = new ArrayList<>();
		for (String id : ids) {
			SubagentJob job = _jobs.get(id);
			if (job != null && job.status().isTerminal()) {
				job.markDelivered();
				results.add(job.result());
			}
		}
		return results;
	}

	/**
	 * Every job in registration order.
	 *
	 * <p>
	 * Ordered by the counter in the id rather than by the id as a string: {@code "sa-10"} sorts
	 * before {@code "sa-2"} lexicographically, which would silently reorder results the moment a
	 * session passes ten subagents - and result delivery to the parent is defined to follow the
	 * order the parent started them in.
	 * </p>
	 */
	private List<SubagentJob> ordered() {
		List<SubagentJob> all = new ArrayList<>(_jobs.values());
		all.sort((x, y) -> Integer.compare(sequenceOf(x), sequenceOf(y)));
		return all;
	}

	/**
	 * The registration counter encoded in a job's id, or {@link Integer#MAX_VALUE} for an id
	 * this registry did not mint (which therefore sorts last rather than throwing).
	 */
	private static int sequenceOf(SubagentJob job) {
		String id = job.subagentId();
		int separator = id.lastIndexOf('-');
		if (separator < 0) {
			return Integer.MAX_VALUE;
		}
		try {
			return Integer.parseInt(id.substring(separator + 1));
		} catch (NumberFormatException ex) {
			return Integer.MAX_VALUE;
		}
	}

	/** Work performed for a subagent job on a background thread. */
	@FunctionalInterface
	public interface SubagentRun {

		/** Runs the subagent and returns its result envelope. */
		SubagentResult run(SubagentJob job) throws Exception;
	}

	/**
	 * Queues the given job's work for the background executor.
	 *
	 * @see #spawn(SubagentJob, SubagentRun, Runnable)
	 */
	public void spawn(SubagentJob job, SubagentRun run) {
		spawn(job, run, () -> {
			// Nobody to tell.
		});
	}

	/**
	 * Queues the given job's work, and starts it as soon as this registry and the installation have
	 * room for it.
	 *
	 * <p>
	 * At most {@link #maxConcurrent()} jobs of this registry run at once, and at most
	 * {@link com.top_logic.tl.ai.core.limits.RunLimitSettings#subagentWorkers()} over the whole
	 * installation, when that is set. A job beyond either stays {@link SubagentJobStatus#QUEUED} and
	 * starts, in registration order, when one ends - it waits rather than being refused, because the
	 * lead that started it expects its result. Started, it is marked RUNNING, then DONE, FAILED or
	 * CANCELLED; waiters in {@link #awaitBlocking} are signalled.
	 * </p>
	 *
	 * <p>
	 * A job cancelled while it is still queued never runs its work: it ends CANCELLED at once, and
	 * {@code notStarted} is told, for whatever the spawner has to record about a job that ended
	 * without running.
	 * </p>
	 *
	 * @param notStarted
	 *        Told when the job is cancelled before it started.
	 */
	public void spawn(SubagentJob job, SubagentRun run, Runnable notStarted) {
		synchronized (this) {
			_queue.addLast(new Pending(job, run, notStarted));
		}
		job.cancellationToken().onCancel(this::dispatch);
		dispatch();
	}

	/**
	 * Starts what may start now and ends what was cancelled while queued.
	 *
	 * <p>
	 * Called when a job is queued, when one ends, when a queued one is cancelled, and when the
	 * installation frees a slot this registry asked for.
	 * </p>
	 */
	void dispatch() {
		List<Pending> starting = new ArrayList<>();
		List<Pending> dropped = new ArrayList<>();
		synchronized (this) {
			for (Iterator<Pending> it = _queue.iterator(); it.hasNext();) {
				Pending pending = it.next();
				if (pending.job().cancellationToken().isCancelled()) {
					it.remove();
					dropped.add(pending);
				}
			}
			while (!_queue.isEmpty() && _started < _maxConcurrent && !_executor.isShutdown()
				&& SubagentSlots.tryAcquire(this)) {
				starting.add(_queue.removeFirst());
				_started++;
			}
		}
		for (Pending pending : dropped) {
			endNotStarted(pending);
		}
		for (Pending pending : starting) {
			try {
				_executor.submit(() -> run(pending));
			} catch (java.util.concurrent.RejectedExecutionException ex) {
				// Shut down meanwhile: the job will not run; give its slots back.
				ended();
			}
		}
	}

	private void run(Pending pending) {
		SubagentJob job = pending.job();
		try {
			job.markRunning();
			Listener listener = _listener;
			listener.started(job);
			SubagentJobStatus terminal;
			SubagentResult result;
			try {
				result = pending.run().run(job);
				terminal = SubagentJobStatus.DONE;
			} catch (java.util.concurrent.CancellationException cancelled) {
				terminal = SubagentJobStatus.CANCELLED;
				result = cancelledResult(job);
			} catch (Exception e) {
				terminal = SubagentJobStatus.FAILED;
				result = new SubagentResult("ERROR: " + e.getMessage(), TokenUsage.EMPTY, 0L, 0, StopReason.ERROR);
			}
			// Before the job reads as ended: a message sent from then on is refused, not left unread.
			job.inbox().close();
			job.markTerminal(terminal, result);
			listener.finished(job);
			signalCompletion();
		} finally {
			ended();
		}
	}

	/** A slot of this registry and of the installation is free again; start what waits. */
	private void ended() {
		synchronized (this) {
			_started--;
		}
		SubagentSlots.release();
		dispatch();
	}

	private void endNotStarted(Pending pending) {
		SubagentJob job = pending.job();
		job.inbox().close();
		job.markTerminal(SubagentJobStatus.CANCELLED, cancelledResult(job));
		pending.notStarted().run();
		signalCompletion();
	}

	private static SubagentResult cancelledResult(SubagentJob job) {
		String reason = job.cancelReason();
		return new SubagentResult(
			reason == null || reason.isBlank() ? "Cancelled." : "Cancelled by the lead agent: " + reason,
			TokenUsage.EMPTY, 0L, 0, StopReason.ABORTED);
	}

	private void signalCompletion() {
		synchronized (_completionLock) {
			_completionLock.notifyAll();
		}
	}

	/**
	 * Blocks until every given job is terminal, a subagent asks the lead a question, or the timeout
	 * elapses, then returns the results of those that completed (marking them delivered). Jobs still
	 * running then are simply omitted.
	 *
	 * <p>
	 * A question ends the wait whichever subagent asks it: a subagent the lead waits for cannot finish
	 * before it is answered, and another one's question must not wait behind a long wait.
	 * </p>
	 */
	public List<SubagentResult> awaitBlocking(List<String> ids, long timeoutMs) {
		long deadline = System.currentTimeMillis() + timeoutMs;
		synchronized (_completionLock) {
			int questions = _questionsAsked;
			while (!allTerminal(ids) && _questionsAsked == questions && !hasUndeliveredQuestion()
					&& System.currentTimeMillis() < deadline) {
				long wait = deadline - System.currentTimeMillis();
				if (wait <= 0) {
					break;
				}
				try {
					_completionLock.wait(wait);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		return await(ids);
	}

	private boolean hasUndeliveredQuestion() {
		for (SubagentJob job : _jobs.values()) {
			LeadQuestion asked = job.leadQuestion();
			if (asked != null && !asked.isDelivered() && !asked.answer().isDone()) {
				return true;
			}
		}
		return false;
	}

	private boolean allTerminal(List<String> ids) {
		for (String id : ids) {
			SubagentJob job = get(id);
			if (job == null || !job.status().isTerminal()) {
				return false;
			}
		}
		return true;
	}

	/** Shuts down the background executor (call on session teardown); queued jobs never start. */
	public void shutdown() {
		SubagentSlots.forget(this);
		_executor.shutdownNow();
	}
}
