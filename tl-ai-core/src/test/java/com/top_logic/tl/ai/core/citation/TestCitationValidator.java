/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.time.Instant;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.tool.api.ReadSource;

/**
 * What an answer's links become: a footnote for every source the run read, plain text for every
 * identifier or address it did not (#617).
 *
 * <p>
 * The answer points at its sources itself, with a link, and calls no tool for it. Whether a link
 * holds is the reading list's answer - what the tools of the run registered - and nothing the model
 * writes can add to that list.
 * </p>
 */
public class TestCitationValidator extends TestCase {

	private static final String EVENT_A = "GenericObject:11047";

	private static final String EVENT_B = "GenericObject:11048";

	private static final String INVOICE = "GenericObject:20001";

	private static final String PAGE = "https://www.example.org/sommerfest";

	private static final String PAGE_TEXT = "# Sommerfest 2026\n\nDas Sommerfest findet am 4. Juli im Stadtpark statt."
		+ " Der Eintritt ist frei.";

	/** Three objects and one web page, as read tools register them. */
	private static ReadingList threeObjectsAndAPage() {
		ReadingList list = new ReadingList();
		list.add(ReadSource.of("object", EVENT_A, "Sommerfest"));
		list.add(ReadSource.of("object", EVENT_B, "Herbstmarkt"));
		list.add(ReadSource.of("object", INVOICE, "Rechnung 2026-0142"));
		list.add(new ReadSource("web", PAGE, "Sommerfest 2026", PAGE, PAGE_TEXT, "/archived-page/abc",
			Instant.parse("2026-10-10T08:00:00Z")));
		return list;
	}

	/**
	 * The acceptance case of #617: an answer resting on three objects and a web page cites them with
	 * links alone, and shows exactly these four sources, in the order the reader meets them.
	 */
	public void testFourLinksBecomeFourFootnotes() {
		String answer = "[Sommerfest](tl:" + EVENT_A + ") und [Herbstmarkt](tl:" + EVENT_B + ") sind geplant;"
			+ " die [Rechnung](tl:" + INVOICE + ") ist bezahlt. Das Fest ist [am 4. Juli](" + PAGE + ").";

		CitationValidator.Result result = CitationValidator.validate(answer, threeObjectsAndAPage(), List.of());

		assertEquals("Sommerfest[1] und Herbstmarkt[2] sind geplant; die Rechnung[3] ist bezahlt."
			+ " Das Fest ist am 4. Juli[4].", result.text());
		List<Citation> citations = result.citations();
		assertEquals(List.of(EVENT_A, EVENT_B, INVOICE, PAGE), citations.stream().map(Citation::id).toList());
		assertEquals(List.of(1, 2, 3, 4), citations.stream().map(Citation::index).toList());
		assertEquals("Sommerfest", citations.get(0).label());
		Citation page = citations.get(3);
		assertEquals("web", page.kind());
		assertEquals("The footnote opens the page.", PAGE, page.url());
		assertEquals("And offers the copy kept when it was read.", "/archived-page/abc", page.archiveUrl());
		assertEquals("2026-10-10T08:00:00Z", page.archivedAt());
		assertTrue("The popover shows what the page says: " + page.excerpt(),
			page.excerpt().contains("Das Sommerfest findet am 4. Juli im Stadtpark statt."));
	}

	/** A source cited twice is one footnote. */
	public void testTheSameSourceKeepsItsNumber() {
		String answer = "[Sommerfest](tl:" + EVENT_A + ") beginnt um 14 Uhr. [Das Fest](tl:" + EVENT_A + ") ist ausgebucht.";

		CitationValidator.Result result = CitationValidator.validate(answer, threeObjectsAndAPage(), List.of());

		assertEquals("Sommerfest[1] beginnt um 14 Uhr. Das Fest[1] ist ausgebucht.", result.text());
		assertEquals(1, result.citations().size());
	}

	/** An identifier no tool of the run showed is no source: the link goes, the words stay. */
	public void testAnInventedIdLosesItsLink() {
		String answer = "[Sommerfest](tl:" + EVENT_A + ") und [Winterball](tl:GenericObject:99999).";

		CitationValidator.Result result = CitationValidator.validate(answer, threeObjectsAndAPage(), List.of());

		assertEquals("Sommerfest[1] und Winterball.", result.text());
		assertEquals(List.of(EVENT_A), result.citations().stream().map(Citation::id).toList());
	}

	/** A model that forgets the {@code tl:} scheme still cites; a {@code mailto:} link is no marker. */
	public void testAnObjectLinkWithoutItsSchemeCites() {
		String answer = "[Sommerfest](" + EVENT_A + ") - Fragen an [das Team](mailto:team@example.org).";

		CitationValidator.Result result = CitationValidator.validate(answer, threeObjectsAndAPage(), List.of());

		assertEquals("Sommerfest[1] - Fragen an [das Team](mailto:team@example.org).", result.text());
	}

	/** An address the run never saw is invented: the link goes, the words stay. */
	public void testAnInventedAddressLosesItsLink() {
		String answer = "Laut [Tagesschau](https://www.tagesschau.de/invented-article) ist es warm.";

		CitationValidator.Result result = CitationValidator.validate(answer, threeObjectsAndAPage(), List.of());

		assertEquals("Laut Tagesschau ist es warm.", result.text());
		assertTrue(result.citations().isEmpty());
	}

	/**
	 * An address a tool answer named without anybody reading the page - a search hit - is not a
	 * source, and not an invention either: it stays the ordinary link it was.
	 */
	public void testAnAddressSeenButNotReadStaysALink() {
		String hit = "https://news.example.org/hit";
		String answer = "Siehe auch [diesen Artikel](" + hit + ").";

		CitationValidator.Result result = CitationValidator.validate(answer, threeObjectsAndAPage(),
			List.of("<untrusted_search_results>1. Hit - " + hit + "</untrusted_search_results>"));

		assertEquals(answer, result.text());
		assertTrue(result.citations().isEmpty());
	}

	/** The address the model asked for counts when a redirect led to another one. */
	public void testTheRequestedAddressOfARedirectedPageCites() {
		ReadingList list = new ReadingList();
		list.add(new ReadSource("web", PAGE, "Sommerfest 2026", PAGE, PAGE_TEXT, "", null), "http://example.org/fest");

		CitationValidator.Result result =
			CitationValidator.validate("[Das Fest](http://example.org/fest/) ist im Juli.", list, List.of());

		assertEquals("Das Fest[1] ist im Juli.", result.text());
		assertEquals(PAGE, result.citations().get(0).id());
	}

	/** A link in code is code, and an image is an image. */
	public void testCodeAndImagesAreLeftAlone() {
		String answer = "`[x](tl:" + EVENT_A + ")` und ![Bild](" + PAGE + ") und [Sommerfest](tl:" + EVENT_A + ")";

		CitationValidator.Result result = CitationValidator.validate(answer, threeObjectsAndAPage(), List.of());

		assertEquals("`[x](tl:" + EVENT_A + ")` und ![Bild](" + PAGE + ") und Sommerfest[1]", result.text());
	}

	/**
	 * A bracketed number the model wrote itself cannot be told from a footnote by the reader's
	 * renderer, so in an answer that cites it is written as character references; an answer that
	 * cites nothing is not touched at all.
	 */
	public void testOwnBracketNumbersAreDefusedOnlyInACitingAnswer() {
		CitationValidator.Result citing = CitationValidator.validate(
			"werte[1] ist leer, [Sommerfest](tl:" + EVENT_A + ") nicht.", threeObjectsAndAPage(), List.of());
		assertEquals("werte&#91;1&#93; ist leer, Sommerfest[1] nicht.", citing.text());

		CitationValidator.Result plain =
			CitationValidator.validate("werte[1] ist leer.", threeObjectsAndAPage(), List.of());
		assertEquals("werte[1] ist leer.", plain.text());
	}

	/**
	 * The link title names the sentence the statement rests on. It is shown only when the page
	 * really says it.
	 */
	public void testAPassageInTheLinkTitleIsCheckedAgainstThePage() {
		String said = "Das Sommerfest findet am 4. Juli im Stadtpark statt.";
		CitationValidator.Result kept = CitationValidator.validate(
			"Es ist [im Stadtpark](" + PAGE + " \"" + said + "\").", threeObjectsAndAPage(), List.of());
		assertEquals("Es ist im Stadtpark[1].", kept.text());
		assertEquals(said, kept.citations().get(0).passage());

		CitationValidator.Result invented = CitationValidator.validate(
			"Es ist [am See](" + PAGE + " \"Das Sommerfest findet am See statt.\").", threeObjectsAndAPage(),
			List.of());
		assertEquals("The source still holds: ", "Es ist am See[1].", invented.text());
		assertEquals("but the sentence the page does not say is not shown as its words", "",
			invented.citations().get(0).passage());
	}

	/**
	 * Every text of the turn is rewritten with one numbering, the footnotes are attached to the text
	 * they occur in, and the reading list is recorded on the turn whether it was cited or not.
	 */
	public void testApplyWritesFootnotesAndTheReadingListOntoTheTurn() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u", Instant.now(), "Was ist geplant?")));
		turn.addAssistantPart(new Part.TextPart("t1", Instant.now(), "[Herbstmarkt](tl:" + EVENT_B + ") ist geplant."));
		turn.addAssistantPart(new Part.TextPart("t2", Instant.now(),
			"[Sommerfest](tl:" + EVENT_A + ") auch, wie [Herbstmarkt](tl:" + EVENT_B + ")."));

		CitationValidator.Result result = CitationValidator.apply(log, "ignored", threeObjectsAndAPage());

		List<Part> parts = turn.assistantParts();
		Part.TextPart first = (Part.TextPart) parts.get(0);
		Part.TextPart second = (Part.TextPart) parts.get(1);
		assertEquals("Herbstmarkt[1] ist geplant.", first.text());
		assertEquals("Sommerfest[2] auch, wie Herbstmarkt[1].", second.text());
		assertEquals(List.of(EVENT_B), first.citations().stream().map(Citation::id).toList());
		assertEquals(List.of(EVENT_B, EVENT_A), second.citations().stream().map(Citation::id).toList());
		assertEquals(2, result.citations().size());
		assertEquals("Everything read is kept with the transcript, cited or not.",
			List.of(EVENT_A, EVENT_B, INVOICE, PAGE), turn.sources().stream().map(ReadSource::id).toList());
		assertEquals("The read text is not.", "", turn.sources().get(3).text());
	}

	/**
	 * An application cites its own way: it declares a syntax in {@link CitationKinds}, and its
	 * markers hold exactly as links do - a code its tools registered becomes a footnote, an invented
	 * one disappears.
	 */
	public void testAnApplicationsOwnMarkerSyntax() throws Exception {
		CitationKinds.Config config = TypedConfiguration.newConfigItem(CitationKinds.Config.class);
		config.getMarkerSyntaxes().add(TypedConfiguration.createConfigItemForImplementationClass(RecordCodeMarkers.class));
		CitationKinds kinds = new CitationKinds(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		ReadingList list = new ReadingList();
		list.add(ReadSource.of("statement", "FACT-001", "Budget ist freigegeben"));
		list.add(ReadSource.of("decision", "DEC-002", "Start im Mai"));

		CitationValidator.Result result = CitationValidator.validate(
			"Das Projekt startet im Mai (FACT-001, DEC-002). Der Termin steht (DEC-999).", list, List.of(),
			kinds.markerSyntaxes());

		assertEquals("Das Projekt startet im Mai[1][2]. Der Termin steht.", result.text());
		assertEquals(List.of("FACT-001", "DEC-002"), result.citations().stream().map(Citation::id).toList());
		assertEquals("decision", result.citations().get(1).kind());
	}

	/** The configuration of an application's syntax is resolved through the type index. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestCitationValidator.class), TypeIndex.Module.INSTANCE));
	}
}
