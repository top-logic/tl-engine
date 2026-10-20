/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The eval's raw output: one line per run, in the order the matrix ran them.
 *
 * <p>
 * Raw on purpose. The aggregate anybody reads is {@link EvalReport}, but an aggregate cannot be
 * argued with, and the first thing anyone doubting a number wants is the individual runs. So this
 * file keeps every run, including the ones that were never started, and the report is computed from
 * it rather than beside it.
 * </p>
 *
 * <h2>The columns</h2>
 *
 * <pre>
 * scenario        the scenario id
 * arm             the arm name
 * model           the model the arm ran on
 * agent           the agent definition the arm ran
 * repeat          1-based repeat within the cell
 * outcome         PASS | FAIL | ERROR | COULD_NOT_RUN
 * turns           agent-loop iterations
 * inputTokens     prompt tokens INCLUDING the cached share
 * cacheReadTokens the cached share of inputTokens
 * outputTokens    completion tokens, thinking excluded
 * durationMs      wall clock of the run
 * stopReason      the loop's stop reason, or NOT_RUN
 * toolCalls       every tool dispatched, in order, separated by '|'
 * failedGraders   the labels of the expectations that did not hold, separated by '|'
 * error           the exception or the reason the arm was not started
 * </pre>
 *
 * <p>
 * {@code inputTokens} is cache-inclusive because that is the figure a context window and a price
 * are measured against, and {@code cacheReadTokens} stands beside it because prompt caching
 * otherwise makes one route look cheaper than another for the same work.
 * </p>
 */
public class EvalCsv {

	/**
	 * The header line, and with it the column contract.
	 *
	 * <p>
	 * Joined at class initialisation, not a compile-time constant: a constant is copied into every
	 * class that reads it, so a test compiled before a column was added kept the old header and failed
	 * against a build that had recompiled only this class (#795).
	 * </p>
	 */
	public static final String HEADER = String.join(",",
		"scenario", "arm", "model", "agent", "repeat", "outcome", "turns", "inputTokens", "cacheReadTokens",
		"outputTokens", "durationMs", "stopReason", "toolCalls", "failedGraders", "error", "subagents",
		"subagentInputTokens", "subagentCacheReadTokens", "subagentOutputTokens", "subagentToolCalls");

	private EvalCsv() {
		// Static utility.
	}

	/** Renders the rows, header included, with a trailing newline. */
	public static String render(List<RunRecord> rows) {
		StringBuilder out = new StringBuilder(HEADER).append('\n');
		for (RunRecord row : rows) {
			out.append(line(row)).append('\n');
		}
		return out.toString();
	}

	/** Writes {@link #render(List)} to the given file, creating missing directories. */
	public static void write(Path file, List<RunRecord> rows) {
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(file, render(rows), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new UncheckedIOException("Cannot write the eval CSV to '" + file + "'.", ex);
		}
	}

	private static String line(RunRecord row) {
		return String.join(",",
			quote(row.scenarioId()),
			quote(row.arm()),
			quote(row.model()),
			quote(row.agent()),
			Integer.toString(row.repeat()),
			row.outcome().name(),
			Integer.toString(row.turns()),
			Integer.toString(row.inputTokens()),
			Integer.toString(row.cacheReadTokens()),
			Integer.toString(row.outputTokens()),
			Long.toString(row.durationMs()),
			quote(row.stopReason()),
			quote(String.join("|", row.toolCalls())),
			quote(String.join("|", row.failedGraders())),
			quote(row.error() == null ? "" : row.error()),
			Integer.toString(row.subagents().runs()),
			Integer.toString(row.subagents().inputTokens()),
			Integer.toString(row.subagents().cacheReadTokens()),
			Integer.toString(row.subagents().outputTokens()),
			Integer.toString(row.subagents().toolCalls()));
	}

	/**
	 * Quotes a field the way RFC 4180 asks: always in double quotes, with embedded quotes doubled,
	 * and with newlines folded to spaces.
	 *
	 * <p>
	 * Always quoting, rather than only when needed, keeps the rule small enough to be obviously
	 * right. Newlines are folded rather than quoted because a grader detail can carry an exception
	 * message, and a CSV whose rows are not lines is the kind of file that reads fine in a
	 * spreadsheet and breaks every command-line tool.
	 * </p>
	 */
	static String quote(String value) {
		String text = value == null ? "" : value;
		return '"' + text.replace("\"", "\"\"").replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ') + '"';
	}
}
