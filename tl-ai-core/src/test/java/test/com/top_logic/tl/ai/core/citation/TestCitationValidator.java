/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tl.ai.core.citation;

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
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Drops invented citations, keeps evidence-backed ones, and rewrites markers.
 */
public class TestCitationValidator extends TestCase {

	/** A declaration whose id is missing from this turn's other tool results is dropped. */
	public void testInventedIdDropped() {
		Result result = CitationValidator.validate(
			"Alpha [1]. Beta [2].",
			List.of(
				decl(1, "invented", "https://no.example"),
				decl(2, "https://ok.example/a", "https://ok.example/a")),
			List.of("fetched https://ok.example/a with title A"));
		assertEquals("Alpha . Beta [1].", result.text());
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

	/** {@link CitationValidator#apply} rewrites the last assistant {@link Part.TextPart}. */
	public void testApplyRewritesLastText() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		Instant now = Instant.parse("2026-04-01T10:00:00Z");
		turn.addAssistantPart(Part.ToolCallPart.started("t1", now, "web-fetch.webFetch", "{}", ToolMode.READ)
			.completed("body https://ok.example/a", Duration.ZERO));
		turn.addAssistantPart(Part.ToolCallPart.started("c1", now, "cite.cite",
			"{\"kind\":\"web\",\"id\":\"https://ok.example/a\",\"label\":\"A\",\"url\":\"https://ok.example/a\"}",
			ToolMode.READ).completed("1", Duration.ZERO));
		turn.addAssistantPart(Part.ToolCallPart.started("c2", now, "cite.cite",
			"{\"kind\":\"web\",\"id\":\"invented\",\"label\":\"No\"}", ToolMode.READ)
			.completed("2", Duration.ZERO));
		turn.addAssistantPart(new Part.TextPart("txt", now, "Kept [1] dropped [2]."));

		Result result = CitationValidator.apply(log, "Kept [1] dropped [2].");
		assertEquals("Kept [1] dropped .", result.text());
		Part.TextPart stored = (Part.TextPart) turn.findPart("txt");
		assertEquals(result.text(), stored.text());
		assertEquals(1, stored.citations().size());
		assertEquals("https://ok.example/a", stored.citations().get(0).id());
	}

	private static Declaration decl(int index, String id, String url) {
		return new Declaration(index, "cite.cite", "c" + index, "web", id, "label-" + index, url);
	}
}
