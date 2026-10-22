/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.core.agent.StopReason;

/**
 * The half of the grading that needs no knowledge base: which tools were called, and how a
 * TL-Script result is compared with a written value.
 *
 * <p>
 * The {@link KbQuery} half is exercised against a real knowledge base in
 * {@code TestEvalHarness}, because an expression grader tested against a stub would only prove that
 * the stub answers.
 * </p>
 */
public class TestEvalGraders extends TestCase {

	/** Every required tool present, no forbidden one: the expectation holds. */
	public void testRequiredPresentAndForbiddenAbsent() throws Exception {
		Verdict verdict = grade("required='a.read' forbidden='a.write'", List.of("read", "list"));

		assertTrue(verdict.detail(), verdict.passed());
	}

	/** The failure names the tools that were missing, not merely that something was. */
	public void testMissingRequiredToolIsNamed() throws Exception {
		Verdict verdict = grade("required='a.read, a.count'", List.of("read"));

		assertFalse(verdict.passed());
		assertTrue("The detail has to name what was missing: " + verdict.detail(),
			verdict.detail().contains("count"));
	}

	/** A forbidden tool that was called fails, and the detail says which. */
	public void testForbiddenToolIsReported() throws Exception {
		Verdict verdict = grade("required='a.read' forbidden='a.write'", List.of("read", "write"));

		assertFalse(verdict.passed());
		assertTrue("The detail has to name the forbidden call: " + verdict.detail(),
			verdict.detail().contains("write"));
	}

	/**
	 * A bound on the number of calls counts every call, the required ones included, and says how
	 * many there were (#616).
	 */
	public void testMoreCallsThanTheBoundFail() throws Exception {
		assertTrue(grade("required='a.present' max-calls='2'", List.of("outlineModel", "present")).passed());

		Verdict verdict = grade("required='a.present' max-calls='2'", List.of("listInstances", "getInstance", "present"));

		assertFalse(verdict.passed());
		assertTrue("The detail has to say how many: " + verdict.detail(),
			verdict.detail().contains("made 3 tool calls, at most 2"));
	}

	/** Order is not checked: the same calls in the other order still pass. */
	public void testOrderDoesNotMatter() throws Exception {
		assertTrue(grade("required='a.one, a.two'", List.of("two", "one")).passed());
	}

	/**
	 * A scenario writes the qualified name; a run records the bare one the model used.
	 *
	 * <p>
	 * The one that would silently be wrong: a scenario saying {@code script-exec.changeData}
	 * while the transcript says {@code changeData} must not report "never called".
	 * </p>
	 */
	public void testQualifiedNameMatchesTheBareCall() {
		assertTrue(Graders.wasCalled("script-exec.changeData", List.of("changeData")));
		assertTrue(Graders.wasCalled("script-exec__changeData", List.of("changeData")));
		assertTrue(Graders.wasCalled("changeData", List.of("changeData")));
		assertFalse(Graders.wasCalled("script-exec.changeData", List.of("listInstances")));
	}

	/** An expectation that names nothing fails rather than passing on a technicality. */
	public void testExpectationNamingNothingFails() throws Exception {
		Verdict verdict = grade("", List.of("read"));

		assertFalse(verdict.passed());
		assertTrue(verdict.detail(), verdict.detail().contains("asserts nothing"));
	}

	/** The label of the expectation is what a failed cell is named by. */
	public void testLabelReachesTheVerdict() throws Exception {
		assertEquals("read-only", grade("label='read-only' required='a.read'", List.of()).label());
	}

	/**
	 * {@code count(...)} yields a whole {@code Double}, and nobody writing a scenario would think to
	 * write {@code 4.0}.
	 */
	public void testWholeNumbersRenderWithoutADecimalPoint() {
		assertEquals("4", Graders.render(Double.valueOf(4.0)));
		assertEquals("4", Graders.render(Integer.valueOf(4)));
		assertEquals("0", Graders.render(Double.valueOf(0.0)));
		assertEquals("4.5", Graders.render(Double.valueOf(4.5)));
		assertEquals("null", Graders.render(null));
		assertEquals("done", Graders.render("done"));
	}

	/** A run that did not finish fails, whatever its data looks like. */
	public void testUnfinishedRunFails() {
		assertTrue(EvalRunner.completionVerdict(StopReason.COMPLETE, null).passed());
		assertFalse(EvalRunner.completionVerdict(StopReason.MAX_TURNS, null).passed());
		assertFalse(EvalRunner.completionVerdict(null, null).passed());

		Verdict threw = EvalRunner.completionVerdict(StopReason.COMPLETE, "Boom: no provider");
		assertFalse("A run that threw cannot pass, however the data ended up.", threw.passed());
		assertTrue(threw.detail(), threw.detail().contains("Boom"));
	}

	private static Verdict grade(String attributes, List<String> called) throws ConfigurationException {
		ToolSequence expectation = (ToolSequence) TypedConfiguration.parse(ToolSequence.TAG_NAME,
			ToolSequence.class,
			CharacterContents.newContent("<" + ToolSequence.TAG_NAME + " " + attributes + "/>"));
		return Graders.grade(expectation, called);
	}
}
