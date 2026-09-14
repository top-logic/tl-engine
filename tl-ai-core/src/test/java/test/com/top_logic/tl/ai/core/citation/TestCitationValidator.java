/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tl.ai.core.citation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.citation.CitationValidator;
import com.top_logic.tl.ai.core.citation.CitationValidator.Declaration;
import com.top_logic.tl.ai.core.citation.CitationValidator.Result;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Drops invented citations, keeps evidence-backed ones, rewrites markers - and leaves every other
 * bracketed number in the answer alone.
 */
public class TestCitationValidator extends TestCase {

	/**
	 * The transcript of a real run, written by the product and copied from the review's artifacts.
	 *
	 * <p>
	 * It is the fixture on purpose: the name a cite call carries is decided by the runtime, not by
	 * the test, and a hand-typed {@code "cite.cite"} is what let the validator ship blind to every
	 * declaration it was supposed to read.
	 * </p>
	 */
	private static final String RECORDED_RUN = "recorded-cite-run.json";

	/** A declaration whose id is missing from this turn's other tool results is dropped. */
	public void testInventedIdDropped() {
		Result result = CitationValidator.validate(
			"Alpha [1]. Beta [2].",
			List.of(
				decl(1, "invented", "https://no.example"),
				decl(2, "https://ok.example/a", "https://ok.example/a")),
			List.of("fetched https://ok.example/a with title A"));
		assertEquals("Alpha. Beta [1].", result.text());
		assertEquals(1, result.citations().size());
		assertEquals("https://ok.example/a", result.citations().get(0).id());
		assertEquals(1, result.citations().get(0).index());
	}

	/** A declaration whose id appears in another tool result is kept. */
	public void testValidKept() {
		Result result = CitationValidator.validate(
			"Fact [1].",
			List.of(decl(1, "INV-12", "")),
			List.of("invoice INV-12 amount 12.00"));
		assertEquals("Fact [1].", result.text());
		assertEquals(List.of("INV-12"), result.citations().stream().map(Citation::id).toList());
	}

	/** Surviving markers are rewritten in order of first appearance. */
	public void testRenumbering() {
		Result result = CitationValidator.validate(
			"Second [2] then first [1] then second again [2].",
			List.of(
				decl(1, "https://one.example", "https://one.example"),
				decl(2, "https://two.example", "https://two.example")),
			List.of("one https://one.example two https://two.example"));
		assertEquals("Second [1] then first [2] then second again [1].", result.text());
		assertEquals("https://two.example", result.citations().get(0).id());
		assertEquals("https://one.example", result.citations().get(1).id());
	}

	/** An answer with no citations is left unchanged. */
	public void testNoneAtAll() {
		Result result = CitationValidator.validate("Just a sentence.", List.of(), List.of("unused"));
		assertEquals("Just a sentence.", result.text());
		assertTrue(result.citations().isEmpty());
	}

	/**
	 * A turn that declared nothing keeps every bracket the model wrote.
	 *
	 * <p>
	 * The regression this pins deleted array indices and footnote references out of every answer in
	 * the product, whether or not the citation feature was in use.
	 * </p>
	 */
	public void testUndeclaredMarkersSurvive() {
		String answer = "Zeile A: werte[0] und werte[1] und werte[2].\nZeile B: siehe Fussnote [1] und [2].";
		Result result = CitationValidator.validate(answer, List.of(), List.of("evidence"));
		assertEquals(answer, result.text());
	}

	/** A marker the model wrote but no cite call claimed stays, even when the turn did cite. */
	public void testUnclaimedMarkerSurvivesBesideACitation() {
		Result result = CitationValidator.validate(
			"Fact [1] and the array werte[7].",
			List.of(decl(1, "INV-12", "")),
			List.of("invoice INV-12"));
		assertEquals("Fact [1] and the array werte[7].", result.text());
	}

	/** Nothing inside an inline code span or a fenced block is rewritten. */
	public void testCodeIsNeverRewritten() {
		String answer = "Kept [1].\n"
			+ "Inline `werte[1]` and `werte[2]`.\n"
			+ "```java\n"
			+ "int a = werte[1];\n"
			+ "int b = werte[2];\n"
			+ "```\n"
			+ "Tail [2].";
		Result result = CitationValidator.validate(answer,
			List.of(
				decl(1, "https://ok.example/a", "https://ok.example/a"),
				decl(2, "invented", "")),
			List.of("body of https://ok.example/a"));
		assertEquals("Kept [1].\n"
			+ "Inline `werte[1]` and `werte[2]`.\n"
			+ "```java\n"
			+ "int a = werte[1];\n"
			+ "int b = werte[2];\n"
			+ "```\n"
			+ "Tail.", result.text());
		assertEquals(1, result.citations().size());
	}

	/** Stripping a marker takes the space in front of it with it. */
	public void testStrippingLeavesNoStraySpace() {
		Result result = CitationValidator.validate("Alpha [1]. Beta [1] gamma.",
			List.of(decl(1, "invented", "")), List.of("nothing matching"));
		assertEquals("Alpha. Beta gamma.", result.text());
		assertTrue(result.citations().isEmpty());
	}

	/** A short id is not evidenced by a longer number that merely contains it. */
	public void testEvidenceMatchesWholeTokens() {
		Result result = CitationValidator.validate("Fact [1].",
			List.of(decl(1, "7", "")), List.of("total 700 EUR, 17 items"));
		assertEquals("Fact.", result.text());
		assertTrue(result.citations().isEmpty());

		Result hit = CitationValidator.validate("Fact [1].",
			List.of(decl(1, "7", "")), List.of("invoice id 7, total 700 EUR"));
		assertEquals("Fact [1].", hit.text());
		assertEquals(1, hit.citations().size());
	}

	/** Two cite calls that claim the same marker do not overwrite each other silently. */
	public void testCollidingClaimsKeepTheFirst() {
		Result result = CitationValidator.validate("Only [1].",
			List.of(
				new Declaration(1, "cite", "call-a", "web", "https://a.example", "A", "https://a.example"),
				new Declaration(1, "cite", "call-b", "web", "https://b.example", "B", "https://b.example")),
			List.of("https://a.example and https://b.example"));
		assertEquals("Only [1].", result.text());
		assertEquals(1, result.citations().size());
		assertEquals("https://a.example", result.citations().get(0).id());
	}

	/** {@link CitationValidator#apply} rewrites every assistant {@link Part.TextPart} of the turn. */
	public void testApplyRewritesEveryText() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		Instant now = Instant.parse("2026-04-01T10:00:00Z");
		turn.addAssistantPart(new Part.TextPart("t0", now, "Vorab [2] schon erwaehnt."));
		turn.addAssistantPart(Part.ToolCallPart.started("t1", now, "webFetch", "{}", ToolMode.READ)
			.completed("body https://ok.example/a", Duration.ZERO));
		turn.addAssistantPart(Part.ToolCallPart.started("c1", now, "cite",
			"{\"kind\":\"web\",\"id\":\"https://ok.example/a\",\"label\":\"A\",\"url\":\"https://ok.example/a\"}",
			ToolMode.READ).completed("1", Duration.ZERO));
		turn.addAssistantPart(Part.ToolCallPart.started("c2", now, "cite",
			"{\"kind\":\"web\",\"id\":\"invented\",\"label\":\"No\"}", ToolMode.READ)
			.completed("2", Duration.ZERO));
		turn.addAssistantPart(new Part.TextPart("txt", now, "Kept [1] dropped [2]."));

		Result result = CitationValidator.apply(log, "Kept [1] dropped [2].");
		assertEquals("Kept [1] dropped.", result.text());

		Part.TextPart first = (Part.TextPart) turn.findPart("t0");
		assertEquals("Vorab schon erwaehnt.", first.text());
		assertTrue(first.citations().isEmpty());

		Part.TextPart stored = (Part.TextPart) turn.findPart("txt");
		assertEquals(result.text(), stored.text());
		assertEquals(1, stored.citations().size());
		assertEquals("https://ok.example/a", stored.citations().get(0).id());
	}

	/**
	 * The recorded run's cite calls are recognized, so the citation reaches the answer.
	 *
	 * <p>
	 * The transcript names the tool {@code cite}, which is what the model calls and what the log
	 * records. A validator that matched only the configured {@code cite.cite} saw no declaration at
	 * all, kept no citation and deleted the marker.
	 * </p>
	 */
	public void testRecordedRunIsRecognized() throws IOException {
		ConversationLog log = ConversationLogSerializer.fromJson(recordedRun());
		Turn turn = log.turns().get(0);
		assertEquals("The fixture must keep the run's cite calls.", 2, citeCalls(turn));

		Result result = CitationValidator.apply(log,
			"Seite 1 sagt das eine [1], Seite 2 das andere [2].");
		assertEquals(1, result.citations().size());
		assertEquals("http://quotes.toscrape.com/page/2/", result.citations().get(0).id());
		assertEquals("web", result.citations().get(0).kind());
		// The recorded run is the one with the thread-local counter: both calls returned "1", so
		// only one marker was ever claimed and the second is prose the validator must not touch.
		assertEquals("Seite 1 sagt das eine [1], Seite 2 das andere [2].", result.text());
	}

	/** Both URLs of the recorded run are evidenced by the run's own {@code webFetch} results. */
	public void testRecordedRunEvidenceIsFound() throws IOException {
		ConversationLog log = ConversationLogSerializer.fromJson(recordedRun());
		Turn turn = log.turns().get(0);
		List<String> evidence = turn.assistantParts().stream()
			.filter(Part.ToolCallPart.class::isInstance)
			.map(Part.ToolCallPart.class::cast)
			.filter(call -> "webFetch".equals(call.toolName()))
			.map(Part.ToolCallPart::result)
			.toList();
		assertEquals(2, evidence.size());

		for (String url : List.of("http://quotes.toscrape.com/page/1/", "http://quotes.toscrape.com/page/2/")) {
			Result result = CitationValidator.validate("Fact [1].",
				List.of(new Declaration(1, "cite", "c", "web", url, "label", url)), evidence);
			assertEquals("Fact [1].", result.text());
			assertEquals(url, result.citations().get(0).id());
		}
	}

	private static int citeCalls(Turn turn) {
		return (int) turn.assistantParts().stream()
			.filter(Part.ToolCallPart.class::isInstance)
			.map(Part.ToolCallPart.class::cast)
			.filter(call -> "cite".equals(call.toolName()))
			.count();
	}

	private static String recordedRun() throws IOException {
		try (InputStream in = TestCitationValidator.class.getResourceAsStream(RECORDED_RUN)) {
			assertNotNull("Missing test fixture " + RECORDED_RUN, in);
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static Declaration decl(int index, String id, String url) {
		return new Declaration(index, "cite", "c" + index, "web", id, "label-" + index, url);
	}
}
