/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;

/**
 * Turns the rows of a matrix into the two numbers that may be quoted, and prints them beside what
 * they cost.
 *
 * <h2>Why two numbers</h2>
 *
 * <p>
 * <b>pass@1</b> is the share of individual runs that passed: what a user gets on one attempt.
 * <b>pass^k</b> is the share of cells in which <em>all</em> k repeats passed: what a user gets if
 * the same task has to work every time, which is what an agent inside a business application is
 * actually asked for. They differ most exactly where it matters - a model that passes two runs out
 * of three scores 0.67 and 0, and the second number is the honest one for an unattended workflow.
 * </p>
 *
 * <h2>Why cost and latency are in the same table</h2>
 *
 * <p>
 * Because a ranking on accuracy alone is not a decision. An arm that is three points better and
 * four times the price is not better; the published agent-benchmark literature is unanimous that
 * accuracy without cost produces recommendations nobody can act on. Tokens and milliseconds are
 * therefore printed in the same row as the pass rates, never in an appendix.
 * </p>
 *
 * <h2>What may not be read out of it</h2>
 *
 * <p>
 * A small difference. With a handful of scenarios this report can show that an arm is unusable with
 * our harness; it cannot rank two arms that both mostly work. The README says how many scenarios
 * that would take, and the number is far larger than any set anyone will hand-write.
 * </p>
 */
public class EvalReport {

	/**
	 * One cell of the matrix: what one arm did with one scenario, over all its repeats.
	 *
	 * @param scenarioId
	 *        The scenario this cell reports on, as the eval file names it.
	 * @param arm
	 *        The arm this cell reports on, as the eval file names it.
	 * @param model
	 *        The model the arm was run with.
	 * @param runs
	 *        How many runs actually happened; 0 for an arm that could not be started.
	 * @param passes
	 *        How many of them passed.
	 * @param couldNotRun
	 *        Whether the arm was never started, with {@link #reason()} saying why.
	 * @param reason
	 *        Why the arm could not be started, or <code>null</code> when it ran.
	 * @param meanTurns
	 *        Mean number of agent turns over the runs that happened.
	 * @param meanInputTokens
	 *        Mean input tokens over the runs that happened.
	 * @param meanOutputTokens
	 *        Mean output tokens over the runs that happened.
	 * @param meanDurationMs
	 *        Mean wall-clock duration in milliseconds over the runs that happened.
	 */
	public record Cell(
			String scenarioId,
			String arm,
			String model,
			int runs,
			int passes,
			boolean couldNotRun,
			String reason,
			double meanTurns,
			long meanInputTokens,
			long meanOutputTokens,
			long meanDurationMs) {

		/** The share of runs that passed, or 0 when the arm never ran. */
		public double passAt1() {
			return runs == 0 ? 0.0 : (double) passes / runs;
		}

		/** Whether every repeat passed - the cell's contribution to pass^k. */
		public boolean passedAll() {
			return runs > 0 && passes == runs;
		}

		/** "k/n", the form every cell must be reported in. */
		public String kOfN() {
			return couldNotRun ? "-" : passes + "/" + runs;
		}
	}

	/**
	 * What one arm did over the whole scenario set.
	 *
	 * @param arm
	 *        The arm this summary reports on, as the eval file names it.
	 * @param model
	 *        The model the arm was run with.
	 * @param runs
	 *        How many runs happened, over all cells that ran.
	 * @param passes
	 *        How many of them passed.
	 * @param cells
	 *        How many cells ran at all - the denominator of pass^k.
	 * @param cellsAllPassed
	 *        How many of those cells passed every repeat - the numerator of pass^k.
	 * @param cellsNotRun
	 *        How many cells never ran, and are therefore in no rate.
	 * @param meanInputTokens
	 *        Mean input tokens per run, over the cells that ran.
	 * @param meanOutputTokens
	 *        Mean output tokens per run, over the cells that ran.
	 * @param meanDurationMs
	 *        Mean wall-clock duration in milliseconds per run, over the cells that ran.
	 */
	public record ArmSummary(
			String arm,
			String model,
			int runs,
			int passes,
			int cells,
			int cellsAllPassed,
			int cellsNotRun,
			long meanInputTokens,
			long meanOutputTokens,
			long meanDurationMs) {

		/** Share of runs that passed. */
		public double passAt1() {
			return runs == 0 ? 0.0 : (double) passes / runs;
		}

		/** Share of cells in which every repeat passed. */
		public double passHatK() {
			return cells == 0 ? 0.0 : (double) cellsAllPassed / cells;
		}
	}

	private EvalReport() {
		// Static utility.
	}

	/** Groups the rows into cells, in the order the rows arrived. */
	public static List<Cell> cells(List<RunRecord> rows) {
		Map<String, List<RunRecord>> grouped = new LinkedHashMap<>();
		for (RunRecord row : rows) {
			grouped.computeIfAbsent(row.scenarioId() + "\u0000" + row.arm(), key -> new ArrayList<>())
				.add(row);
		}
		List<Cell> result = new ArrayList<>();
		for (List<RunRecord> group : grouped.values()) {
			result.add(cell(group));
		}
		return result;
	}

	private static Cell cell(List<RunRecord> group) {
		RunRecord first = group.get(0);
		List<RunRecord> ran = group.stream().filter(r -> r.outcome().ran()).toList();
		int passes = (int) ran.stream().filter(r -> r.outcome().passed()).count();
		String reason = ran.isEmpty() ? first.error() : null;
		return new Cell(first.scenarioId(), first.arm(), first.model(), ran.size(), passes,
			ran.isEmpty(), reason,
			mean(ran.stream().mapToLong(RunRecord::turns).sum(), ran.size()) / 1.0,
			meanLong(ran.stream().mapToLong(RunRecord::inputTokens).sum(), ran.size()),
			meanLong(ran.stream().mapToLong(RunRecord::outputTokens).sum(), ran.size()),
			meanLong(ran.stream().mapToLong(RunRecord::durationMs).sum(), ran.size()));
	}

	/** Summarizes every arm over all cells, in the order the arms first appear. */
	public static List<ArmSummary> arms(List<RunRecord> rows) {
		Map<String, List<Cell>> byArm = new LinkedHashMap<>();
		for (Cell cell : cells(rows)) {
			byArm.computeIfAbsent(cell.arm(), key -> new ArrayList<>()).add(cell);
		}
		List<ArmSummary> result = new ArrayList<>();
		for (Map.Entry<String, List<Cell>> entry : byArm.entrySet()) {
			List<Cell> cells = entry.getValue();
			List<Cell> ran = cells.stream().filter(c -> !c.couldNotRun()).toList();
			int runs = ran.stream().mapToInt(Cell::runs).sum();
			int passes = ran.stream().mapToInt(Cell::passes).sum();
			int allPassed = (int) ran.stream().filter(Cell::passedAll).count();
			result.add(new ArmSummary(entry.getKey(), cells.get(0).model(), runs, passes,
				ran.size(), allPassed, cells.size() - ran.size(),
				meanLong(ran.stream().mapToLong(Cell::meanInputTokens).sum(), ran.size()),
				meanLong(ran.stream().mapToLong(Cell::meanOutputTokens).sum(), ran.size()),
				meanLong(ran.stream().mapToLong(Cell::meanDurationMs).sum(), ran.size())));
		}
		return result;
	}

	/** The whole report as text, for the build log or a report file. */
	public static String render(List<RunRecord> rows) {
		StringBuilder out = new StringBuilder();
		out.append("Per cell (k of n runs passed)\n");
		out.append(String.format(Locale.ROOT, "%-30s %-14s %-8s %6s %8s %8s %9s%n",
			"scenario", "arm", "k/n", "turns", "in-tok", "out-tok", "ms"));
		for (Cell cell : cells(rows)) {
			out.append(String.format(Locale.ROOT, "%-30s %-14s %-8s %6.1f %8d %8d %9d%n",
				cell.scenarioId(), cell.arm(), cell.kOfN(), cell.meanTurns(),
				cell.meanInputTokens(), cell.meanOutputTokens(), cell.meanDurationMs()));
			if (cell.couldNotRun()) {
				out.append("    could not run: ").append(cell.reason()).append('\n');
			}
		}
		out.append("\nPer arm\n");
		out.append(String.format(Locale.ROOT, "%-14s %-30s %8s %8s %8s %8s %9s%n",
			"arm", "model", "pass@1", "pass^k", "in-tok", "out-tok", "ms"));
		for (ArmSummary arm : arms(rows)) {
			// An arm that never ran prints a dash, not 0.00: a rate of zero reads as "this model
			// failed everything", which is the one thing the run did not establish.
			out.append(String.format(Locale.ROOT, "%-14s %-30s %8s %8s %8d %8d %9d%n",
				arm.arm(), arm.model(), rate(arm.cells(), arm.passAt1()),
				rate(arm.cells(), arm.passHatK()),
				arm.meanInputTokens(), arm.meanOutputTokens(), arm.meanDurationMs()));
			if (arm.cellsNotRun() > 0) {
				out.append("    ").append(arm.cellsNotRun())
					.append(" scenario(s) could not run on this arm and are excluded from both rates.\n");
			}
		}
		out.append("\npass@1 is the share of single runs that passed; pass^k the share of scenarios"
			+ " in which every repeat passed.\nWith few scenarios these numbers show an unusable"
			+ " arm, not a ranking between two that work.\n");
		return out.toString();
	}

	/** A rate, or a dash when no run stands behind it. */
	private static String rate(int cells, double value) {
		return cells == 0 ? "-" : String.format(Locale.ROOT, "%.2f", value);
	}

	private static double mean(long total, int count) {
		return count == 0 ? 0.0 : (double) total / count;
	}

	private static long meanLong(long total, int count) {
		return count == 0 ? 0L : total / count;
	}
}
