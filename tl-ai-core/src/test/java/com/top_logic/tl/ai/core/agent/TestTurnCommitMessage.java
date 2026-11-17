/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;

/**
 * Tests for {@link TurnCommitMessage}, without a running application: messages are compared as
 * keys, not as resolved text.
 *
 * @see TestTurnCommitMessageInATurn
 */
public class TestTurnCommitMessage extends TestCase {

	private static final ResKey CREATED = ResKey.text("Created A");

	private static final ResKey DELETED = ResKey.text("Deleted B");

	/** The first non-blank paragraph, its white space collapsed. */
	public void testFirstParagraph() {
		assertEquals("I will rename the ticket now.",
			TurnCommitMessage.summary("\n  \n I will   rename\nthe ticket\tnow.\n\n Then I check it."));
	}

	/** A paragraph break may contain white space, and a line break alone is none. */
	public void testParagraphBreaks() {
		assertEquals("one two", TurnCommitMessage.summary("one\ntwo\r\n \t\r\nthree"));
		assertEquals("one two", TurnCommitMessage.summary("one\r\ntwo\r\rthree"));
	}

	/** No text is no summary. */
	public void testBlankTextHasNoSummary() {
		assertNull(TurnCommitMessage.summary(null));
		assertNull(TurnCommitMessage.summary(""));
		assertNull(TurnCommitMessage.summary(" \n\n \t"));
	}

	/** A text at the limit stays as it is. */
	public void testTextAtTheLimitIsKept() {
		String text = "x".repeat(TurnCommitMessage.MAX_TEXT_LENGTH);
		assertEquals(text, TurnCommitMessage.summary(text));
	}

	/** A longer text is cut after the last word that fits, and marked. */
	public void testLongTextIsCutAtAWordBoundary() {
		String text = "word ".repeat(100);
		String summary = TurnCommitMessage.summary(text);
		assertTrue(summary, summary.endsWith("word" + TurnCommitMessage.ELLIPSIS));
		assertTrue(summary.length() <= TurnCommitMessage.MAX_TEXT_LENGTH);
		assertTrue("No room for another word: " + summary.length(),
			summary.length() > TurnCommitMessage.MAX_TEXT_LENGTH - "word ".length() - 1);
	}

	/** A single word longer than the limit is cut inside it. */
	public void testOneLongWordIsCutInside() {
		String summary = TurnCommitMessage.summary("y".repeat(500));
		assertEquals("y".repeat(TurnCommitMessage.MAX_TEXT_LENGTH - 1) + TurnCommitMessage.ELLIPSIS, summary);
	}

	/** The model's text wins over the tools' messages and is prefixed with the agent's name. */
	public void testModelTextWithAgent() {
		assertEquals(I18NConstants.AGENT_CHANGE__AGENT_DESCRIPTION.fill("Planner", ResKey.text("Renaming it.")),
			TurnCommitMessage.of(" Planner ", "Renaming it.\n\nMore.", List.of(CREATED)));
	}

	/** Without an agent name, the description stands alone. */
	public void testNoAgentName() {
		assertEquals(ResKey.text("Renaming it."), TurnCommitMessage.of(null, "Renaming it.", List.of()));
		assertEquals(CREATED, TurnCommitMessage.of("", null, List.of(CREATED)));
	}

	/** The tools' messages, each once, in call order. */
	public void testToolMessagesDistinctInOrder() {
		assertEquals(I18NConstants.CHANGES__FIRST_NEXT.fill(CREATED, DELETED),
			TurnCommitMessage.of(null, " ", Arrays.asList(CREATED, DELETED, CREATED, null)));
	}

	/** Three messages nest from the left. */
	public void testThreeToolMessages() {
		ResKey third = ResKey.text("Moved C");
		assertEquals(
			I18NConstants.CHANGES__FIRST_NEXT.fill(I18NConstants.CHANGES__FIRST_NEXT.fill(CREATED, DELETED), third),
			TurnCommitMessage.of(null, null, List.of(CREATED, DELETED, third)));
	}

	/** Nothing said by anybody: the neutral message. */
	public void testNothingSaid() {
		assertEquals(I18NConstants.AGENT_CHANGE, TurnCommitMessage.of(null, null, List.of()));
		assertEquals(I18NConstants.AGENT_CHANGE__AGENT_DESCRIPTION.fill("Planner", I18NConstants.AGENT_CHANGE),
			TurnCommitMessage.of("Planner", null, null));
	}
}
