/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.core.session.Citation;

/**
 * The graders of a research answer that need no model: {@code <citations>}, and how a judge's rubric
 * and reply are read (#755).
 */
public class TestResearchGraders extends TestCase {

	private static final String QUESTION = "Where did Geoffrey Hinton earn his doctorate?";

	private static final Citation EDINBURGH =
		new Citation(1, "webFetch", "call-1", "web", "https://www.ed.ac.uk/hinton", "Hinton at Edinburgh",
			"https://www.ed.ac.uk/hinton");

	private static final Citation NOBEL =
		new Citation(2, "webFetch", "call-2", "web", "https://nobelprize.org/2024", "Nobel 2024",
			"https://nobelprize.org/2024");

	private static final Citation NOBEL_AGAIN =
		new Citation(3, "webFetch", "call-3", "web", "https://www.nobelprize.org/2024/facts", "Facts",
			"https://www.nobelprize.org/2024/facts");

	/** An answer that cites read pages and links nothing else passes. */
	public void testReadSourcesPass() throws Exception {
		Verdict verdict = Graders.gradeCitations(citations(2, 2, 0), answer(
			"He studied at [Edinburgh](https://www.ed.ac.uk/hinton/) and won the [prize](https://nobelprize.org/2024).",
			EDINBURGH, NOBEL));

		assertTrue(verdict.detail(), verdict.passed());
		assertEquals("2 sources from 2 hosts, 0 unread links", verdict.detail());
	}

	/** A link to a page no run read fails, and names the address. */
	public void testAnUnreadLinkFails() throws Exception {
		Verdict verdict = Graders.gradeCitations(citations(1, 0, 0), answer(
			"He studied at [Edinburgh](https://www.ed.ac.uk/hinton), see also "
				+ "[his CV](https://invented.example/cv \"quoted\").",
			EDINBURGH));

		assertFalse(verdict.passed());
		assertTrue(verdict.detail(), verdict.detail().contains("https://invented.example/cv"));
	}

	/** An answer without a source fails the default of one source. */
	public void testNoSourceFails() throws Exception {
		Verdict verdict = Graders.gradeCitations(citations(1, 0, 0), answer("Edinburgh, 1978."));

		assertFalse(verdict.passed());
		assertTrue(verdict.detail(), verdict.detail().contains("cites 0 sources, at least 1 required"));
	}

	/** Two pages of one site are one host, with or without {@code www.}. */
	public void testOneSiteIsOneHost() throws Exception {
		Verdict verdict = Graders.gradeCitations(citations(2, 2, 0), answer("[a](x) [b](y)", NOBEL, NOBEL_AGAIN));

		assertFalse(verdict.passed());
		assertTrue(verdict.detail(), verdict.detail().contains("cites 1 hosts [nobelprize.org], at least 2 required"));
	}

	/** A rubric is one criterion per line; list markers and blank lines are dropped. */
	public void testRubricCriteria() {
		assertEquals(List.of("Names Edinburgh.", "Names 1978.", "Names Hinton."),
			Judge.criteria("\n\t- Names Edinburgh.\n\n  * Names 1978.\nNames Hinton.\n"));
	}

	/** The judge's reply is read with text around the JSON, and unknown fields are skipped. */
	public void testJudgeReplyIsRead() throws Exception {
		List<Judge.Criterion> result = Judge.parse("Here you go:\n{\"criteria\": [{\"met\": true, \"reason\": \"ok\", "
			+ "\"score\": 1}, {\"reason\": \"wrong year\", \"met\": false}], \"note\": \"x\"}\nDone.");

		assertEquals(List.of(new Judge.Criterion(true, "ok"), new Judge.Criterion(false, "wrong year")), result);
	}

	/** A reply that is not the expected JSON is refused rather than read as anything. */
	public void testUnreadableJudgeReply() {
		for (String reply : List.of("Everything is met.", "{\"criteria\": [{\"reason\": \"no verdict\"}]}")) {
			try {
				Judge.parse(reply);
				fail("Read as a verdict: " + reply);
			} catch (java.io.IOException expected) {
				// The judge expectation fails with this reason.
			}
		}
	}

	/** Without a judge model a judge expectation fails, saying why - it never passes unasked. */
	public void testNoJudgeFails() throws Exception {
		JudgeExpectation expectation = (JudgeExpectation) TypedConfiguration.parse(JudgeExpectation.TAG_NAME,
			JudgeExpectation.class, CharacterContents.newContent(
				"<judge label='correct'><rubric>- Names Edinburgh.</rubric></judge>", "judge.xml"));

		Verdict verdict = Judge.NONE.grade(expectation, answer("Edinburgh."));

		assertFalse(verdict.passed());
		assertTrue(verdict.detail(), verdict.detail().contains("names no judge model"));
	}

	/** An eval file with a judge expectation must name the judge. */
	public void testAJudgeWithoutModelIsRefused() throws Exception {
		try {
			EvalConfigs.load(CharacterContents.newContent("<eval><arms><arm name='a' agent='x' model='m'/></arms>"
				+ "<scenarios><scenario id='s' prompt='p' oracle='o'><expectations>"
				+ "<judge><rubric>- Names it.</rubric></judge>"
				+ "</expectations></scenario></scenarios></eval>", "eval.xml"));
			fail("A judge nobody names would fail every run for a reason that has nothing to do with the model.");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("names no judge-model"));
		}
	}

	private static CitationExpectation citations(int minSources, int minDomains, int maxUnread) throws Exception {
		return (CitationExpectation) TypedConfiguration.parse(CitationExpectation.TAG_NAME,
			CitationExpectation.class, CharacterContents.newContent("<citations min-sources='" + minSources
				+ "' min-domains='" + minDomains + "' max-unread-links='" + maxUnread + "'/>", "citations.xml"));
	}

	private static RunAnswer answer(String raw, Citation... citations) {
		return new RunAnswer(QUESTION, raw, raw, List.of(citations));
	}
}
