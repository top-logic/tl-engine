/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tl.ai.core.citation;

import java.time.Duration;
import java.time.Instant;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.citation.CitationMarkers;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * The marker numbering belongs to the conversation, not to the single run.
 *
 * @see CitationMarkers#resumeFrom(ConversationLog)
 */
public class TestCitationMarkers extends TestCase {

	private static final Instant NOW = Instant.parse("2026-04-01T10:00:00Z");

	/** A conversation that has cited nothing numbers from 1. */
	public void testFreshConversationStartsAtOne() {
		assertEquals(1, CitationMarkers.nextFreeMarker(new ConversationLog()));
		assertEquals(1, CitationMarkers.nextFreeMarker(null));

		CitationMarkers markers = new CitationMarkers();
		markers.resumeFrom(new ConversationLog());
		assertEquals(1, markers.next());
	}

	/**
	 * The second run of a conversation continues where the first stopped.
	 *
	 * <p>
	 * Handing out {@code 1} again is what made citing stop working after the first answer (#495):
	 * the model reads its own earlier answer back, sees {@code [1]} taken, and writes a number no
	 * cite call of this turn claimed.
	 * </p>
	 */
	public void testNumberingContinuesAfterAnEarlierTurn() {
		ConversationLog log = new ConversationLog();
		Turn first = log.startTurn();
		first.addAssistantPart(citeCall("c1", "1"));
		first.addAssistantPart(citeCall("c2", "2"));
		first.addAssistantPart(new Part.TextPart("t1", NOW, "Alpha [1] und Beta [2]."));
		log.completeTurn();

		assertEquals(3, CitationMarkers.nextFreeMarker(log));

		CitationMarkers markers = new CitationMarkers();
		markers.resumeFrom(log);
		assertEquals(3, markers.next());
		assertEquals(4, markers.next());
	}

	/** Only the cite calls decide; a number in the answer's text does not. */
	public void testOnlyCiteCallsCount() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.addAssistantPart(Part.ToolCallPart.started("w1", NOW, "webFetch", "{}", ToolMode.READ)
			.completed("42", Duration.ZERO));
		turn.addAssistantPart(new Part.TextPart("t1", NOW, "Kein Zitat, aber werte[9]."));
		log.completeTurn();

		assertEquals(1, CitationMarkers.nextFreeMarker(log));
	}

	/** A cite call that answered an error rather than a number does not move the counter. */
	public void testNonNumericResultIsIgnored() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.addAssistantPart(citeCall("c1", "Error: id is required."));
		turn.addAssistantPart(citeCall("c2", "5"));
		log.completeTurn();

		assertEquals(6, CitationMarkers.nextFreeMarker(log));
	}

	/** The open turn counts too: a second cite call of the same turn must not repeat the first. */
	public void testTheOpenTurnCountsAsWell() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.addAssistantPart(citeCall("c1", "7"));

		assertEquals(8, CitationMarkers.nextFreeMarker(log));
	}

	private static Part.ToolCallPart citeCall(String id, String result) {
		return Part.ToolCallPart.started(id, NOW, "cite",
			"{\"kind\":\"web\",\"id\":\"https://a.example\",\"label\":\"A\"}", ToolMode.READ)
			.completed(result, Duration.ZERO);
	}
}
