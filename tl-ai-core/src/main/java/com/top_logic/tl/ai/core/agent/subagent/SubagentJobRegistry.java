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
import java.util.concurrent.atomic.AtomicInteger;

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

	/** */
	public SubagentJobRegistry() {
		this(DEFAULT_MAX_CONCURRENT);
	}

	/** */
	public SubagentJobRegistry(int maxConcurrent) {
		_maxConcurrent = maxConcurrent;
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

	private List<SubagentJob> ordered() {
		List<SubagentJob> all = new ArrayList<>(_jobs.values());
		all.sort((x, y) -> x.subagentId().compareTo(y.subagentId()));
		return all;
	}
}
