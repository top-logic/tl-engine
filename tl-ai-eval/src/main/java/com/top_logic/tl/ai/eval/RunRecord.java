/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

/**
 * One row of the eval: what one arm did with one scenario on one repeat, and how it was graded.
 *
 * @param scenarioId
 *        The {@link EvalScenario#getId() scenario}.
 * @param arm
 *        The {@link EvalConfig.Arm#getName() arm name}.
 * @param model
 *        The model the arm ran on.
 * @param agent
 *        The agent definition the arm ran.
 * @param repeat
 *        1-based repeat index within the cell.
 * @param outcome
 *        The graded result.
 * @param turns
 *        Agent-loop iterations the run used.
 * @param inputTokens
 *        Prompt tokens <em>including</em> the cached share, which is the figure to compare against a
 *        context window and the one a price is charged on. {@code cacheReadTokens} says how much of
 *        it was cached.
 * @param cacheReadTokens
 *        The cached share of {@code inputTokens}. Reported separately because prompt caching
 *        distorts a cost comparison badly: an arm whose route caches the system prompt looks cheaper
 *        than one whose route does not, for the same work.
 * @param outputTokens
 *        Completion tokens, thinking excluded.
 * @param durationMs
 *        Wall-clock time of the run.
 * @param stopReason
 *        The loop's {@code StopReason}, or {@code "NOT_RUN"} for an arm that never started.
 * @param toolCalls
 *        Every tool the run dispatched, in call order, by qualified name.
 * @param verdicts
 *        One entry per expectation, in the scenario's order. Empty exactly when the arm never ran.
 * @param response
 *        The final assistant text, kept for reading a surprising row rather than for grading.
 * @param error
 *        Why the run failed or could not be started, or {@code null}.
 * @param subagents
 *        What the background subagents the run started spent; the token columns before count the
 *        run's own requests only.
 */
public record RunRecord(
		String scenarioId,
		String arm,
		String model,
		String agent,
		int repeat,
		RunOutcome outcome,
		int turns,
		int inputTokens,
		int cacheReadTokens,
		int outputTokens,
		long durationMs,
		String stopReason,
		List<String> toolCalls,
		List<Verdict> verdicts,
		String response,
		String error,
		SubagentTotals subagents) {

	/** A row of a run whose subagents are not counted. */
	public RunRecord(String scenarioId, String arm, String model, String agent, int repeat, RunOutcome outcome,
			int turns, int inputTokens, int cacheReadTokens, int outputTokens, long durationMs, String stopReason,
			List<String> toolCalls, List<Verdict> verdicts, String response, String error) {
		this(scenarioId, arm, model, agent, repeat, outcome, turns, inputTokens, cacheReadTokens, outputTokens,
			durationMs, stopReason, toolCalls, verdicts, response, error, SubagentTotals.NONE);
	}

	/** The stop reason recorded for an arm that was never started. */
	public static final String NOT_RUN = "NOT_RUN";

	/** A row for an arm the harness refused to start, with the reason. */
	public static RunRecord couldNotRun(String scenarioId, EvalConfig.Arm arm, int repeat, String reason) {
		return new RunRecord(scenarioId, arm.getName(), arm.getModel(), arm.getAgent(), repeat,
			RunOutcome.COULD_NOT_RUN, 0, 0, 0, 0, 0L, NOT_RUN, List.of(), List.of(), "", reason);
	}

	/** The labels of the expectations that did not hold, in scenario order. */
	public List<String> failedGraders() {
		return verdicts().stream().filter(v -> !v.passed()).map(Verdict::label).toList();
	}
}
