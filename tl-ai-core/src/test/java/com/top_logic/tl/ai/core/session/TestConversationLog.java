/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link ConversationLog}.
 */
public class TestConversationLog extends TestCase {

	/** Log starts empty. */
	public void testEmpty() {
		ConversationLog log = new ConversationLog();
		assertTrue(log.turns().isEmpty());
		assertNull(log.currentTurn());
		assertEquals(0, log.turnCount());
	}

	/** Starting a turn creates a new Turn and notifies observer. */
	public void testStartTurn() {
		ConversationLog log = new ConversationLog();
		List<Turn> observed = new ArrayList<>();
		log.addObserver(new ConversationLog.Observer() {
			@Override
			public void onTurnStarted(Turn turn) { observed.add(turn); }
			@Override
			public void onTurnCompleted(Turn turn) {
				/** */
			}
			@Override
			public void onPartAdded(Turn turn, Part part) {
				/** */
			}
			@Override
			public void onPartUpdated(Turn turn, Part part) {
				/** */
			}
		});

		Turn turn = log.startTurn();
		assertNotNull(turn);
		assertEquals(1, log.turns().size());
		assertEquals(1, turn.turnNumber());
		assertSame(turn, log.currentTurn());
		assertEquals(1, observed.size());
	}

	/** Completing a turn notifies observer and clears current. */
	public void testCompleteTurn() {
		ConversationLog log = new ConversationLog();
		boolean[] completed = {false};
		log.addObserver(new ConversationLog.Observer() {
			@Override
			public void onTurnStarted(Turn turn) {
				/** */
			}
			@Override
			public void onTurnCompleted(Turn turn) { completed[0] = true; }
			@Override
			public void onPartAdded(Turn turn, Part part) {
				/** */
			}
			@Override
			public void onPartUpdated(Turn turn, Part part) {
				/** */
			}
		});

		log.startTurn();
		log.completeTurn();
		assertTrue(completed[0]);
		assertNull(log.currentTurn());
		assertEquals(1, log.turns().size());
	}

	/** Second turn gets turnNumber 2. */
	public void testTurnNumbering() {
		ConversationLog log = new ConversationLog();
		Turn t1 = log.startTurn();
		assertEquals(1, t1.turnNumber());
		log.completeTurn();

		Turn t2 = log.startTurn();
		assertEquals(2, t2.turnNumber());
		log.completeTurn();
		assertEquals(2, log.turnCount());
	}

	/** Parts added to current turn are forwarded to log observers. */
	public void testPartForwarding() {
		ConversationLog log = new ConversationLog();
		List<Part> observedParts = new ArrayList<>();
		log.addObserver(new ConversationLog.Observer() {
			@Override
			public void onTurnStarted(Turn turn) {
				/** */
			}
			@Override
			public void onTurnCompleted(Turn turn) {
				/** */
			}
			@Override
			public void onPartAdded(Turn turn, Part part) { observedParts.add(part); }
			@Override
			public void onPartUpdated(Turn turn, Part part) {
				/** */
			}
		});

		Turn turn = log.startTurn();
		Part.TextPart text = new Part.TextPart("p1", Instant.now(), "Hello");
		turn.addAssistantPart(text);

		assertEquals(1, observedParts.size());
		assertSame(text, observedParts.get(0));
	}
}
