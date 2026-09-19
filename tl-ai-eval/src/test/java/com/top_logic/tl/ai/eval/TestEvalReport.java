/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import junit.framework.TestCase;

/**
 * The arithmetic of the report, and the two ways it could lie.
 *
 * <p>
 * A model that was never asked must not lower anybody's pass rate, and a cell must never be reported
 * as a single pass or fail when it was run three times. Both are properties of the aggregation, so
 * they are asserted here rather than read off a matrix.
 * </p>
 */
public class TestEvalReport extends TestCase {

	/** Two of three repeats passed: pass@1 is 2/3 and pass^k is 0. */
	public void testACellIsReportedAsKOfN() {
		List<RunRecord> rows = List.of(
			row("s1", "a", RunOutcome.PASS, 1),
			row("s1", "a", RunOutcome.FAIL, 2),
			row("s1", "a", RunOutcome.PASS, 3));

		EvalReport.Cell cell = EvalReport.cells(rows).get(0);
		assertEquals("2/3", cell.kOfN());
		assertEquals(3, cell.runs());
		assertEquals(2, cell.passes());
		assertFalse("Two of three is not 'all of them'.", cell.passedAll());

		EvalReport.ArmSummary arm = EvalReport.arms(rows).get(0);
		assertEquals(2.0 / 3.0, arm.passAt1(), 0.0001);
		assertEquals("The only cell did not pass every repeat.", 0.0, arm.passHatK(), 0.0001);
	}

	/**
	 * pass@1 and pass^k differ exactly where it matters.
	 *
	 * <p>
	 * Two scenarios, one passed three times out of three and one twice out of three: four of six
	 * single runs passed, but only one of the two scenarios is reliable. A report showing only the
	 * first number would call this arm two-thirds good at an unattended workflow it fails half the
	 * time.
	 * </p>
	 */
	public void testPassHatKIsStricterThanPassAt1() {
		List<RunRecord> rows = List.of(
			row("s1", "a", RunOutcome.PASS, 1),
			row("s1", "a", RunOutcome.PASS, 2),
			row("s1", "a", RunOutcome.PASS, 3),
			row("s2", "a", RunOutcome.PASS, 1),
			row("s2", "a", RunOutcome.FAIL, 2),
			row("s2", "a", RunOutcome.FAIL, 3));

		EvalReport.ArmSummary arm = EvalReport.arms(rows).get(0);
		assertEquals(4.0 / 6.0, arm.passAt1(), 0.0001);
		assertEquals(1.0 / 2.0, arm.passHatK(), 0.0001);
	}

	/**
	 * An arm that could not be started is excluded from both rates rather than counted as failing.
	 *
	 * <p>
	 * Counting it as a failure would put a number on a question nobody put to the model, and would
	 * make every arm in the same table look better by comparison.
	 * </p>
	 */
	public void testCouldNotRunIsNotAFailure() {
		EvalConfig.Arm arm = Arms.of("ghost", "Eval_Ghost", "no-such-model");
		List<RunRecord> rows = List.of(
			row("s1", "a", RunOutcome.PASS, 1),
			RunRecord.couldNotRun("s1", arm, 1, "Model cannot call tools."));

		EvalReport.Cell skipped = EvalReport.cells(rows).get(1);
		assertTrue(skipped.couldNotRun());
		assertEquals("A cell that never ran has no k of n to report.", "-", skipped.kOfN());
		assertEquals("Model cannot call tools.", skipped.reason());

		EvalReport.ArmSummary ghost = EvalReport.arms(rows).get(1);
		assertEquals(0, ghost.cells());
		assertEquals(1, ghost.cellsNotRun());
		assertEquals("An arm that never ran has a pass rate of nothing, not of zero out of one.",
			0.0, ghost.passAt1(), 0.0001);

		String text = EvalReport.render(rows);
		assertTrue("The report has to say why an arm did not run: " + text,
			text.contains("could not run: Model cannot call tools."));
	}

	/** Cost and latency stand in the same table as the pass rates, never in an appendix. */
	public void testTheReportPrintsCostAndLatencyBesideAccuracy() {
		String text = EvalReport.render(List.of(row("s1", "a", RunOutcome.PASS, 1)));

		assertTrue(text, text.contains("pass@1"));
		assertTrue(text, text.contains("pass^k"));
		assertTrue("Tokens belong next to the pass rate: " + text, text.contains("in-tok"));
		assertTrue("So does wall clock: " + text, text.contains("ms"));
		assertTrue("And the reader has to be told what the numbers cannot do: " + text,
			text.contains("not a ranking"));
	}

	static RunRecord row(String scenario, String arm, RunOutcome outcome, int repeat) {
		return new RunRecord(scenario, arm, "model-" + arm, "Agent_" + arm, repeat, outcome,
			2, 1000, 0, 100, 250L, "COMPLETE", List.of("read"),
			List.of(new Verdict("check", outcome.passed(), "detail")), "answer", null);
	}
}
