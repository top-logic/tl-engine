/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

import junit.framework.TestCase;

import com.top_logic.ai.a2a.protocol.TaskState;

/**
 * Test case for {@link ContextBook}: which remote conversation a call talks in.
 */
public class TestContextBook extends TestCase {

	public void testAnIncomingContextIsTheDefaultOfItsConversation() {
		ContextBook book = new ContextBook();
		book.bind("session-1", "ctx-from-beta");
		assertEquals("A call back lands in the caller's context.", "ctx-from-beta",
			book.choose("session-1", "beta", null, null, "Frage").contextId());
	}

	public void testNewConversationsGetUniqueLabelsAndAreFoundAgain() {
		ContextBook book = new ContextBook();
		ContextBook.Choice first = book.choose("c", "gamma", "new", "design", "x");
		book.record("c", "gamma", first.contextId(), first.label(), "t1", TaskState.COMPLETED);
		ContextBook.Choice second = book.choose("c", "gamma", "new:design", null, "y");
		assertEquals("design 2", second.label());
		book.record("c", "gamma", second.contextId(), second.label(), "t2", TaskState.WORKING);

		assertEquals(first.contextId(), book.choose("c", "gamma", "DESIGN", null, "z").contextId());
		assertEquals(second.contextId(), book.choose("c", "gamma", "continue", null, "z").contextId());
		assertEquals("Without a topic, the first words label it.", "Bitte prüfe die Frames",
			book.choose("c", "gamma", "new", null, "Bitte prüfe die Frames von SCR-01").label());
		assertEquals(2, book.conversations("c").size());
		assertEquals("design 2", book.conversations("c").get(0).label());
	}
}
