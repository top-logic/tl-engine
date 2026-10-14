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
import java.util.concurrent.CopyOnWriteArrayList;
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

	/** How every subagent id starts; the number of the subagent in its conversation follows. */
	private static final String ID_PREFIX = "sa-";

	/** Default maximum concurrently-running subagents per session. */
	public static final int DEFAULT_MAX_CONCURRENT = 4;

	private final int _maxConcurrent;

	private final Map<String, SubagentJob> _jobs = new ConcurrentHashMap<>();

	/**
	 * The number of the last subagent id handed out - shared by every registry of one conversation, see
	 * {@link #SubagentJobRegistry(int, AtomicInteger)}.
	 */
	private final AtomicInteger _ids;

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

	private final List<Listener> _listeners = new CopyOnWriteArrayList<>();

	/** Counts the questions asked to the lead, so that a waiting lead notices a new one. */
	private int _questionsAsked;

	/** */
	public SubagentJobRegistry() {
		this(DEFAULT_MAX_CONCURRENT);
	}

	/** */
	public SubagentJobRegistry(int maxConcurrent) {
		this(maxConcurrent, new AtomicInteger());
	}

	/**
	 * Creates a registry that numbers its subagents with the given counter.
	 *
	 * @param ids
	 *        The number of the last subagent id handed out. The registries of one conversation - one
	 *        per window, and the one of a run without a window - share it, so that no two subagents of
	 *        a conversation have the same id: a card, a transcript link and the lead's own history name
	 *        a subagent by it.
	 */
	public SubagentJobRegistry(int maxConcurrent, AtomicInteger ids) {
		_maxConcurrent = maxConcurrent;
		_ids = ids;
	}

	/**
	 * Makes sure the next subagent id is beyond the given number.
	 *
	 * <p>
	 * For ids handed out before this registry existed: the lead's log of a conversation that outlived
	 * its registries - a server restart, a registry forgotten while idle - names its earlier subagents,
	 * and a new one must not take one of their ids.
	 * </p>
	 *
	 * @param number
	 *        The highest number of a subagent id already in use; see {@link #numberOf(String)}.
	 */
	public void countOnFrom(int number) {
		_ids.accumulateAndGet(number, Math::max);
	}

	/**
	 * Told when a job's work starts, when it ends, and when it asks the lead a question.
	 *
	 * <p>
	 * For what has to know about a job while it runs and outlive the turn that started it - in the
	 * chat, the register that lets any window of the account find and answer what a subagent asks, and
	 * the waker that starts a lead turn when there is news. Called on the job's own thread, so an
	 * implementation must be quick and must not throw.
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

		/** The job asked the lead a question with {@code ask_lead} and waits for the answer. */
		default void questionAsked(SubagentJob job) {
			// Nothing by default.
		}
	}

	/**
	 * Adds a listener that is told about the jobs that start from now on and about questions to the
	 * lead.
	 */
	public void addListener(Listener listener) {
		_listeners.add(listener);
	}

	/** Removes a listener added before; a no-op if it was not. */
	public void removeListener(Listener listener) {
		_listeners.remove(listener);
	}

	/** Maximum concurrently-running subagents. */
	public int maxConcurrent() {
		return _maxConcurrent;
	}

	/** Registers a new QUEUED job and returns it. */
	public SubagentJob register(String targetAgentName, String taskBrief, CancellationToken token) {
		String id = ID_PREFIX + _ids.incrementAndGet();
		SubagentJob job = new SubagentJob(id, targetAgentName, taskBrief, token);
		job.setQuestionSignal(() -> questionAsked(job));
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
	private void questionAsked(SubagentJob job) {
		synchronized (_completionLock) {
			_questionsAsked++;
			_completionLock.notifyAll();
		}
		for (Listener listener : _listeners) {
			listener.questionAsked(job);
		}
	}

	/**
	 * Whether a job spawned now would wait in the queue rather than start: this registry or the
	 * installation is at its cap, or others wait already. A prediction for what to tell the user at
	 * the spawn; the job starts when it starts.
	 */
	public synchronized boolean wouldQueue() {
		return !_queue.isEmpty() || _started >= _maxConcurrent || SubagentSlots.isFull();
	}

	/**
	 * What waits for a lead turn: the finished jobs whose result no lead turn has been shown, and
	 * the questions to the lead no lead turn has been shown and nobody has answered.
	 *
	 * <p>
	 * A cancelled job is not counted: somebody stopped it, so it brings no news anybody waits for.
	 * Its result is still delivered with the next lead turn.
	 * </p>
	 *
	 * @param finished
	 *        Number of results waiting, of jobs that ended done or failed.
	 * @param asking
	 *        Number of questions waiting.
	 */
	public record News(int finished, int asking) {

		/** Whether nothing waits. */
		public boolean isEmpty() {
			return finished == 0 && asking == 0;
		}
	}

	/**
	 * Whether nobody needs this registry any more: no job is queued or running, and nothing waits for a
	 * lead turn - not even a cancelled job's result.
	 */
	public boolean isIdle() {
		synchronized (this) {
			if (!_queue.isEmpty() || _started > 0) {
				return false;
			}
		}
		for (SubagentJob job : _jobs.values()) {
			if (!job.status().isTerminal() || !job.isDelivered()) {
				return false;
			}
			LeadQuestion asked = job.leadQuestion();
			if (asked != null && !asked.answer().isDone()) {
				return false;
			}
		}
		return true;
	}

	/** What waits for a lead turn now; see {@link News}. */
	public News news() {
		int finished = 0;
		int asking = 0;
		for (SubagentJob job : _jobs.values()) {
			if (job.status().isTerminal() && job.status() != SubagentJobStatus.CANCELLED
				&& !job.isDelivered()) {
				finished++;
			}
			LeadQuestion asked = job.leadQuestion();
			if (asked != null && !asked.isDelivered() && !asked.answer().isDone()) {
				asking++;
			}
		}
		return new News(finished, asking);
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
		int number = numberOf(job.subagentId());
		return number == 0 ? Integer.MAX_VALUE : number;
	}

	/**
	 * The number in a subagent id this class hands out ({@code "sa-7"} is 7), or 0 for any other
	 * string.
	 */
	public static int numberOf(String subagentId) {
		if (subagentId == null || !subagentId.startsWith(ID_PREFIX)) {
			return 0;
		}
		try {
			return Math.max(0, Integer.parseInt(subagentId.substring(ID_PREFIX.length())));
		} catch (NumberFormatException ex) {
			return 0;
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
	 * installation frees a slot this registry asked for. Once the registry is {@link #shutdown() shut
	 * down}, nothing starts any more, and every queued job ends cancelled.
	 * </p>
	 */
	void dispatch() {
		List<Pending> starting = new ArrayList<>();
		List<Pending> dropped = new ArrayList<>();
		synchronized (this) {
			boolean shutDown = _executor.isShutdown();
			for (Iterator<Pending> it = _queue.iterator(); it.hasNext();) {
				Pending pending = it.next();
				if (shutDown || pending.job().cancellationToken().isCancelled()) {
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
				// Shut down meanwhile: the job will not run; give its slots back and end it.
				ended();
				endNotStarted(pending);
			}
		}
	}

	/**
	 * Tells who listens now that the job started. A method of its own, so that nothing of the
	 * listener list stays in the frame of {@link #run(Pending)} while the job works.
	 */
	private void notifyStarted(SubagentJob job) {
		for (Listener listener : _listeners) {
			listener.started(job);
		}
	}

	/**
	 * Tells who listens now - not who listened when the job started - that it ended: a chat window
	 * that reloaded the chat meanwhile has to hear it, and one that has ended must not be held for as
	 * long as the job works.
	 */
	private void notifyFinished(SubagentJob job) {
		for (Listener listener : _listeners) {
			listener.finished(job);
		}
	}

	private void run(Pending pending) {
		SubagentJob job = pending.job();
		try {
			job.markRunning();
			notifyStarted(job);
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
			// Before the job reads as ended: a message sent from then on is refused, and one that arrived
			// when no turn was left to read it is named in the result, not lost.
			job.setUnreadMessages(job.inbox().closeAndDrain());
			job.markTerminal(terminal, result);
			notifyFinished(job);
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
		job.setUnreadMessages(job.inbox().closeAndDrain());
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
	 * before it is answered, and another one's question must not wait behind a long wait. For the same
	 * reason the wait does not start at all while one of the given jobs waits for the lead to answer a
	 * question the lead was shown before: the lead and the subagent would wait for each other until the
	 * timeout.
	 * </p>
	 */
	public List<SubagentResult> awaitBlocking(List<String> ids, long timeoutMs) {
		return awaitBlocking(ids, timeoutMs, CancellationToken.UNCANCELLABLE);
	}

	/**
	 * {@link #awaitBlocking(List, long)} that also ends when the waiting run is cancelled.
	 *
	 * <p>
	 * The cancellation wakes the wait; it does not interrupt the waiting thread, which belongs to an
	 * agent run and may go on to read the database.
	 * </p>
	 *
	 * @param cancellation
	 *        The waiting run's token.
	 */
	public List<SubagentResult> awaitBlocking(List<String> ids, long timeoutMs, CancellationToken cancellation) {
		long deadline = System.currentTimeMillis() + timeoutMs;
		try (CancellationToken.Registration wake = cancellation.onCancel(this::signalCompletion)) {
			synchronized (_completionLock) {
				int questions = _questionsAsked;
				while (!cancellation.isCancelled() && !allTerminal(ids) && _questionsAsked == questions
						&& !hasUndeliveredQuestion() && !waitsForLead(ids) && System.currentTimeMillis() < deadline) {
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

	/** Whether one of the given jobs waits for the lead to answer its question. */
	private boolean waitsForLead(List<String> ids) {
		for (String id : ids) {
			SubagentJob job = get(id);
			if (job != null && job.waitingFor() == SubagentJob.Waiting.LEAD_ANSWER) {
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

	/**
	 * Shuts down the background executor (call on session teardown).
	 *
	 * <p>
	 * Running jobs are cancelled through their tokens and end by themselves; their threads are not
	 * interrupted, because a subagent may be reading the database and an interrupt during a read
	 * closes H2's file for the whole application. Queued jobs, and jobs spawned afterwards, never
	 * start: they end {@link SubagentJobStatus#CANCELLED} at once, so that nobody waits for them.
	 * </p>
	 */
	public void shutdown() {
		SubagentSlots.forget(this);
		_executor.shutdown();
		cancelAll();
		dispatch();
	}
}
