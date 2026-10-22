/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.memory;

import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * That a memory is kept only where the user agreed to it in the turn that is running.
 *
 * <p>
 * This is the check the {@code remember} tool asks, and it is the reason the tool cannot be talked
 * into keeping something: it is built from what the <em>user</em> wrote and answered, never from
 * what the model reports about it.
 * </p>
 */
public class TestMemoryConsent extends TestCase {

	/** Nothing about memory was said, so nothing may be kept. */
	public void testOrdinaryTurnGrantsNothing() {
		assertFalse(new MemoryConsent("Wie hoch ist die Rechnung vom August?").granted());
		assertFalse(new MemoryConsent("Summarize the last three invoices.").granted());
		assertFalse(new MemoryConsent("").granted());
		assertFalse(new MemoryConsent(null).granted());
	}

	/** The user asked for it in their own words, in either shipped language. */
	public void testUserAskedInTheirOwnWords() {
		assertTrue(new MemoryConsent("Merke dir, dass ich kurze Antworten mag.").granted());
		assertTrue(new MemoryConsent("Bitte merken: ich arbeite in der Buchhaltung.").granted());
		assertTrue(new MemoryConsent("Remember that I prefer short answers.").granted());
		assertTrue(new MemoryConsent("Keep in mind I work in accounting.").granted());
	}

	/**
	 * A word inside another word is not that word.
	 *
	 * <p>
	 * "anmerkenswert" carries the letters of "merken" and says nothing about memory; a match on
	 * substrings would turn it into a grant.
	 * </p>
	 */
	public void testAWordInsideAnotherWordGrantsNothing() {
		assertFalse(new MemoryConsent("Das ist anmerkenswert.").granted());
		assertFalse(new MemoryConsent("Remembrance day is a holiday.").granted());
	}

	/** The words are matched whatever the case and whatever the diacritics. */
	public void testCaseAndDiacriticsAreIgnored() {
		assertTrue(new MemoryConsent("MERKE DIR das bitte.").granted());
		assertTrue(new MemoryConsent("Vergiss nicht, dass ich früh Feierabend mache.").granted());
	}

	/** The agent asked whether it should remember, and the user agreed. */
	public void testAgreedToTheQuestion() {
		MemoryConsent consent = new MemoryConsent("Danke, das war hilfreich.");
		assertFalse("Nothing has been asked yet.", consent.granted());

		consent.recordAnswer("Soll ich mir merken, dass Sie kurze Antworten bevorzugen?", "Ja");
		assertTrue(consent.granted());
	}

	/** The user declined, so nothing may be kept. */
	public void testDeclinedTheQuestion() {
		MemoryConsent consent = new MemoryConsent("Danke.");
		consent.recordAnswer("Soll ich mir merken, dass Sie kurze Antworten bevorzugen?", "Nein");
		assertFalse(consent.granted());
	}

	/**
	 * A negation wins over anything in the same answer that would otherwise read as agreement.
	 *
	 * <p>
	 * "Nein, das ist in Ordnung" carries "in ordnung", which is an agreement word. Refusing is the
	 * safe direction, so the ambiguous answer goes that way.
	 * </p>
	 */
	public void testANegationWinsOverAnAgreementWord() {
		MemoryConsent consent = new MemoryConsent("Danke.");
		consent.recordAnswer("Soll ich mir das merken?", "Nein, das ist in Ordnung.");
		assertFalse(consent.granted());

		MemoryConsent english = new MemoryConsent("Thanks.");
		english.recordAnswer("Shall I remember that?", "No, that is fine.");
		assertFalse(english.granted());
	}

	/** A "yes" to some other question grants nothing: the question has to be about remembering. */
	public void testAgreementToAnotherQuestionGrantsNothing() {
		MemoryConsent consent = new MemoryConsent("Zeig mir die Rechnungen.");
		consent.recordAnswer("Soll ich die Rechnung stornieren?", "Ja, bitte");
		assertFalse(consent.granted());
	}

	/** An answer that says nothing either way is not an agreement. */
	public void testAnEmptyAnswerGrantsNothing() {
		MemoryConsent consent = new MemoryConsent("Danke.");
		consent.recordAnswer("Soll ich mir das merken?", "");
		consent.recordAnswer("Soll ich mir das merken?", null);
		consent.recordAnswer("Soll ich mir das merken?", "Hm.");
		assertFalse(consent.granted());
	}

	/** The record is built from the newest user message, and only from its text. */
	public void testReadsTheNewestUserMessage() {
		Conversation conversation = new Conversation();
		conversation.add(user("Merke dir, dass ich Kaffee mag."));
		conversation.add(assistant("Gemerkt."));
		conversation.add(user("Und wie hoch ist die Rechnung?"));

		MemoryConsent consent = MemoryConsent.forNewestTurn(conversation);
		assertEquals("Und wie hoch ist die Rechnung?", consent.userText());
		assertFalse("The request was in an earlier turn, not in this one.", consent.granted());
	}

	/** An attachment is not a sentence: only the text parts are read. */
	public void testReadsOnlyTheTextParts() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("m1", Instant.now(), List.of(
			new TextContent("Merke dir das bitte."),
			new ImageContent("data:image/png;base64,AAAA", "image/png"))));

		assertTrue(MemoryConsent.forNewestTurn(conversation).granted());
	}

	/** A conversation with nothing in it is a turn nobody agreed in. */
	public void testEmptyConversation() {
		assertNull(MemoryConsent.forNewestTurn(new Conversation()).userText());
		assertNull(MemoryConsent.forNewestTurn(null).userText());
		assertFalse(MemoryConsent.forNewestTurn(new Conversation()).granted());
	}

	private static UserMessage user(String text) {
		return new UserMessage("u-" + text.hashCode(), Instant.now(), List.of(new TextContent(text)));
	}

	private static AssistantMessage assistant(String text) {
		return new AssistantMessage("a-" + text.hashCode(), Instant.now(),
			List.of(new TextContent(text)), TokenUsage.EMPTY, "stop");
	}
}
