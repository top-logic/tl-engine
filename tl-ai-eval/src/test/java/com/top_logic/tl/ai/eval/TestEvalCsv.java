/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import junit.framework.TestCase;

/**
 * That the raw output of an eval is a file other tools can read.
 *
 * <p>
 * The CSV is the artifact anybody doubting an aggregate goes back to, so its shape is a contract:
 * one line per run, the columns in a fixed order, and nothing in a field that can break the line.
 * </p>
 */
public class TestEvalCsv extends TestCase {

	/** The header names every column the report is computed from, in order. */
	public void testHeaderIsTheColumnContract() {
		assertEquals("scenario,arm,model,agent,repeat,outcome,turns,inputTokens,cacheReadTokens,"
			+ "outputTokens,durationMs,stopReason,toolCalls,failedGraders,error", EvalCsv.HEADER);
	}

	/** One run is one line, with its values where the header says they are. */
	public void testOneRunIsOneLine() {
		String csv = EvalCsv.render(List.of(TestEvalReport.row("s1", "a", RunOutcome.PASS, 2)));
		String[] lines = csv.split("\n");

		assertEquals(2, lines.length);
		assertEquals(EvalCsv.HEADER, lines[0]);
		assertEquals("\"s1\",\"a\",\"model-a\",\"Agent_a\",2,PASS,2,1000,0,100,250,\"COMPLETE\","
			+ "\"read\",\"\",\"\"", lines[1]);
	}

	/** A failed run names the expectations that did not hold, so the CSV explains itself. */
	public void testFailedGradersAreNamed() {
		RunRecord row = new RunRecord("s1", "a", "m", "Agent", 1, RunOutcome.FAIL, 3, 10, 0, 5, 7L,
			"COMPLETE", List.of("read", "write"),
			List.of(new Verdict("run-completed", true, "fine"),
				new Verdict("ticket-exists", false, "expected '1' but produced '0'"),
				new Verdict("read-only", false, "called the forbidden [write]")),
			"answer", null);

		assertEquals(List.of("ticket-exists", "read-only"), row.failedGraders());
		assertTrue(EvalCsv.render(List.of(row)).contains("\"ticket-exists|read-only\""));
	}

	/**
	 * A field with a comma, a quote or a newline in it does not break the line.
	 *
	 * <p>
	 * Not hypothetical: a grader's detail carries an exception message, and a TL-Script expression
	 * carries commas and quotes by nature.
	 * </p>
	 */
	public void testFieldsThatWouldBreakTheFile() {
		assertEquals("\"a,b\"", EvalCsv.quote("a,b"));
		assertEquals("\"say \"\"no\"\"\"", EvalCsv.quote("say \"no\""));
		assertEquals("\"one two\"", EvalCsv.quote("one\ntwo"));
		assertEquals("\"one two\"", EvalCsv.quote("one\r\ntwo"));
		assertEquals("\"\"", EvalCsv.quote(null));
	}

	/** An arm that never ran is in the file too, with its reason. */
	public void testCouldNotRunIsRecorded() {
		RunRecord row = RunRecord.couldNotRun("s1",
			Arms.of("ghost", "Eval_Ghost", "no-such-model"), 1, "No such model.");
		String line = EvalCsv.render(List.of(row)).split("\n")[1];

		assertTrue(line, line.contains("COULD_NOT_RUN"));
		assertTrue(line, line.contains("NOT_RUN"));
		assertTrue("The reason belongs in the file, not only in the log: " + line,
			line.contains("No such model."));
	}
}
