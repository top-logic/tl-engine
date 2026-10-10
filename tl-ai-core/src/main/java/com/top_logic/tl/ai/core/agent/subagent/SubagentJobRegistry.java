/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayList;
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
 * by id, enforces a concurrency cap, and hands undelivered results back to the parent.
 *
 * <p>The background spawn machinery is added in a later step; this class is the single
 * source of truth for subagent state within one chat session.</p>
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

	private volatile Listener _listener = Listener.NONE;

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

	/** Whether a new job may start now without exceeding the cap. */
	public boolean canStart() {
		return runningCount() < _maxConcurrent;
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
	 * Submits the given job's work to the background executor. The job is marked RUNNING,
	 * then DONE/FAILED/CANCELLED on completion; waiters in {@link #awaitBlocking} are signaled.
	 */
	public void spawn(SubagentJob job, SubagentRun run) {
		_executor.submit(() -> {
			job.markRunning();
			Listener listener = _listener;
			listener.started(job);
			SubagentJobStatus terminal;
			SubagentResult result;
			try {
				result = run.run(job);
				terminal = SubagentJobStatus.DONE;
			} catch (java.util.concurrent.CancellationException cancelled) {
				terminal = SubagentJobStatus.CANCELLED;
				result = new SubagentResult("Cancelled.", TokenUsage.EMPTY, 0L, 0, StopReason.ABORTED);
			} catch (Exception e) {
				terminal = SubagentJobStatus.FAILED;
				result = new SubagentResult("ERROR: " + e.getMessage(), TokenUsage.EMPTY, 0L, 0, StopReason.ERROR);
			}
			job.markTerminal(terminal, result);
			listener.finished(job);
			synchronized (_completionLock) {
				_completionLock.notifyAll();
			}
		});
	}

	/**
	 * Blocks until every given job is terminal or the timeout elapses, then returns the
	 * results of those that completed (marking them delivered). Jobs still running at timeout
	 * are simply omitted.
	 */
	public List<SubagentResult> awaitBlocking(List<String> ids, long timeoutMs) {
		long deadline = System.currentTimeMillis() + timeoutMs;
		synchronized (_completionLock) {
			while (!allTerminal(ids) && System.currentTimeMillis() < deadline) {
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

	private boolean allTerminal(List<String> ids) {
		for (String id : ids) {
			SubagentJob job = get(id);
			if (job == null || !job.status().isTerminal()) {
				return false;
			}
		}
		return true;
	}

	/** Shuts down the background executor (call on session teardown). */
	public void shutdown() {
		_executor.shutdownNow();
	}
}
