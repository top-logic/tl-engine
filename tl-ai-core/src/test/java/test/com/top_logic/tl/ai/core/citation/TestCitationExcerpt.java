/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tl.ai.core.citation;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.citation.CitationExcerpt;

/**
 * What the marker's popover shows: the source in the reader's words, never the tool's transport.
 *
 * <p>
 * The producer is tested on its own because the answer it gives is the whole of what a reader sees
 * of a source. Every case here is a tool result shape the product really produces - JSON with
 * Markdown inside it, a page in the untrusted-content envelope, a line of plain text.
 * </p>
 */
public class TestCitationExcerpt extends TestCase {

	/** A host whose tools answer JSON with Markdown inside, as BOS/Consulting's do. */
	private static final String SECTIONS = "{\"sections\":[{\"id\":\"sec-3\",\"title\":\"Versand\","
		+ "\"body\":\"## Versand\\n\\nDie **Versandkosten** trägt der Besteller, sofern nichts anderes"
		+ " vereinbart ist.\\n\\nEine [Ausnahme](https://example.org/awb) gilt für Teillieferungen.\"}]}";

	/** The section's own prose, as the source spells it. */
	private static final String PASSAGE =
		"Die **Versandkosten** trägt der Besteller, sofern nichts anderes vereinbart ist.";

	/**
	 * A prose source cited without a passage shows the source's prose, not the JSON it arrived in.
	 *
	 * <p>
	 * This is #418: the fallback cut a window of the raw result by character count, so a consultant
	 * read {@code "body": "Die **Versand**…\\n\\n"} in a popover two lines high.
	 * </p>
	 */
	public void testProseSourceShowsItsProse() {
		String excerpt = CitationExcerpt.ofSource("sec-3", "Versand", false, List.of(SECTIONS));

		assertReadable(excerpt);
		assertTrue("The source's own words: " + excerpt,
			excerpt.contains("Versandkosten trägt der Besteller"));
		assertTrue("A Markdown link becomes its text: " + excerpt, excerpt.contains("Eine Ausnahme gilt"));
	}

	/** The same result under a kind declared {@code passage="false"} lists its fields instead. */
	public void testStructuredKindShowsFields() {
		String excerpt = CitationExcerpt.ofSource("sec-3", "Versand", true, List.of(SECTIONS));

		assertReadable(excerpt);
		assertTrue("One field per line: " + excerpt, excerpt.contains("id: sec-3"));
		assertTrue(excerpt.contains("title: Versand"));
	}

	/** An object with no prose in it is shown as its fields, whatever its kind was declared. */
	public void testObjectWithoutProseShowsFields() {
		String result = "{\"id\":\"INV-12\",\"description\":\"Reise Berlin\",\"amount\":\"184.90\","
			+ "\"lines\":[\"Bahn\",\"Hotel\"],\"audit\":{\"by\":\"ada\"}}";
		String excerpt = CitationExcerpt.ofSource("INV-12", "Beleg", false, List.of(result));

		assertReadable(excerpt);
		assertEquals("id: INV-12\ndescription: Reise Berlin\namount: 184.90\nlines: Bahn, Hotel", excerpt);
	}

	/**
	 * A payload quoted inside its envelope is unwrapped, not shown with its own braces.
	 *
	 * <p>
	 * This is what the TL-Script tool returns: an {@code ExecutionResult} whose {@code result} is
	 * the script's value as a string, and a script that read a record returns that record's JSON.
	 * </p>
	 */
	public void testAQuotedPayloadIsUnwrapped() {
		String result = "{\"success\":true,\"result\":\"{\\\"id\\\":\\\"INV-12\\\","
			+ "\\\"description\\\":\\\"Reise **Berlin**\\\"}\"}";
		String excerpt = CitationExcerpt.ofSource("INV-12", "Beleg", true, List.of(result));

		assertReadable(excerpt);
		assertEquals("id: INV-12\ndescription: Reise Berlin", excerpt);
	}

	/**
	 * The untrusted-content envelope is transport too, and a JSON result inside one is still JSON.
	 *
	 * <p>
	 * This is the shape the browser walk produced: {@code <untrusted_model_data>} around the
	 * {@code ExecutionResult} around the record. A producer that only looked at the first character
	 * saw a {@code <}, decided the result was not JSON, and put the whole envelope in the popover.
	 * </p>
	 */
	public void testAnEnvelopedJsonResultIsStillJson() {
		String result = "<untrusted_model_data source=\"TL-Script result\">\n"
			+ "{\n  \"success\": true,\n"
			+ "  \"result\": \"{\\\"id\\\":\\\"INV-12\\\",\\\"titel\\\":\\\"Reise Berlin\\\"}\",\n"
			+ "  \"resultType\": \"string\"\n}\n"
			+ "</untrusted_model_data>";
		String excerpt = CitationExcerpt.ofSource("INV-12", "Beleg", true, List.of(result));

		assertReadable(excerpt);
		assertFalse("The envelope is transport, not source: " + excerpt, excerpt.contains("untrusted"));
		assertEquals("id: INV-12\ntitel: Reise Berlin", excerpt);
	}

	/** The cited record wins over the envelope that carries it. */
	public void testTheCitedRecordIsFoundAmongSiblings() {
		String result = "{\"hits\":2,\"items\":["
			+ "{\"id\":\"INV-11\",\"description\":\"Reise Hamburg\"},"
			+ "{\"id\":\"INV-12\",\"description\":\"Reise Berlin\"}]}";
		String excerpt = CitationExcerpt.ofSource("INV-12", "Beleg", true, List.of(result));

		assertEquals("id: INV-12\ndescription: Reise Berlin", excerpt);
	}

	/** A text source is cut on sentence boundaries and says where it was cut. */
	public void testTextSourceIsCutOnSentences() {
		String page = "<untrusted_page_content source=\"http://x/\">\n# [Quotes](http://x/)\n\n"
			+ "Ein erster Satz, der nichts zur Sache sagt. Die Seite trägt die Kennung PAGE-7 mitten"
			+ " darin. Dann folgt ein dritter Satz, und ein vierter, damit das Fenster irgendwo"
			+ " vernünftig geschnitten werden muss statt mitten im Wort. "
			+ "Und weiter und weiter und weiter, bis der Rand überschritten ist und der Schnitt"
			+ " im Ergebnis auch wirklich zu sehen ist, spätestens jetzt endlich.\n"
			+ "</untrusted_page_content>";
		String excerpt = CitationExcerpt.ofSource("PAGE-7", "Quotes", false, List.of(page));

		assertReadable(excerpt);
		assertFalse("The envelope is transport, not source: " + excerpt, excerpt.contains("untrusted"));
		assertTrue("What was cited must be in it: " + excerpt, excerpt.contains("Kennung PAGE-7 mitten"));
		assertTrue("A cut is marked: " + excerpt, excerpt.endsWith("…"));
		assertTrue("Cut after a sentence, not inside a word: " + excerpt,
			excerpt.contains("statt mitten im Wort. …"));
	}

	/**
	 * A record that only a text result named - a query's {@code label [type:...] [id:...]} lines -
	 * has no fields to show, and a window of those lines is the transport, not the record.
	 */
	public void testARecordNamedOnlyInTextShowsNothingRatherThanTheTransport() {
		String result = "<untrusted_model_data source=\"query\">\n"
			+ "KI im Mittelstand [type:demo.events:Event] [id:GenericObject:2808]\n"
			+ "Teamworkshop [type:demo.events:Event] [id:GenericObject:2977]\n</untrusted_model_data>";

		assertEquals("", CitationExcerpt.ofSource("GenericObject:2808", "KI im Mittelstand", true, List.of(result)));
	}

	/** Found nowhere, the popover still names the source rather than staying blank. */
	public void testUnknownIdFallsBackToTheLabel() {
		String excerpt = CitationExcerpt.ofSource("INV-99", "Der **Beleg**", false,
			List.of("{\"id\":\"INV-12\"}"));

		assertEquals("Der Beleg", excerpt);
	}

	/** The passage's context is cut out of the string value that holds it, not out of the JSON. */
	public void testPassageContextComesFromItsOwnField() {
		String excerpt = CitationExcerpt.around(PASSAGE, List.of(SECTIONS));

		assertReadable(excerpt);
		assertTrue("The passage must be findable in the excerpt, or nothing is highlighted: " + excerpt,
			excerpt.contains(CitationExcerpt.plain(PASSAGE)));
		assertTrue("And it must be shown in its context: " + excerpt, excerpt.contains("Teillieferungen"));
	}

	/** A passage the evidence does not carry is still shown readable rather than not at all. */
	public void testPassageWithoutEvidenceIsRenderedAnyway() {
		assertEquals("Die Versandkosten trägt der Besteller, sofern nichts anderes vereinbart ist.",
			CitationExcerpt.around(PASSAGE, List.of("etwas ganz anderes")));
	}

	/** Markdown and JSON escapes are rendered, not shown. */
	public void testPlainRendersRatherThanShows() {
		assertEquals("H bold and em and code and a link and \"q\"",
			CitationExcerpt.plain("## H\\n\\n- **bold** and *em* and `code` and [a link](http://x) and \\\"q\\\""));
	}

	/** Nothing a reader would have to decode. */
	private static void assertReadable(String excerpt) {
		assertFalse("A JSON escape reached the popover: " + excerpt, excerpt.contains("\\n"));
		assertFalse("A JSON escape reached the popover: " + excerpt, excerpt.contains("\\\""));
		assertFalse("JSON punctuation reached the popover: " + excerpt, excerpt.contains("\":"));
		assertFalse("Markdown syntax reached the popover: " + excerpt, excerpt.contains("**"));
		assertFalse("A Markdown heading reached the popover: " + excerpt, excerpt.contains("##"));
		assertFalse("A Markdown link reached the popover: " + excerpt, excerpt.contains("]("));
	}
}
