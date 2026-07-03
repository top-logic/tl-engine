/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link CompactionResult}.
 */
public class TestCompactionResult extends TestCase {

	/** Negative messagesSummarized is rejected. */
	public void testRejectsNegativeMessagesSummarized() {
		try {
			new CompactionResult(List.of(), -1, 0, 0, "auto");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	/** Negative tokensBefore is rejected. */
	public void testRejectsNegativeTokensBefore() {
		try {
			new CompactionResult(List.of(), 0, -1, 0, "auto");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	/** Negative tokensAfter is rejected. */
	public void testRejectsNegativeTokensAfter() {
		try {
			new CompactionResult(List.of(), 0, 0, -1, "auto");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	/** Zero values are valid (e.g. an empty conversation). */
	public void testAllowsZero() {
		CompactionResult result = new CompactionResult(List.of(), 0, 0, 0, "auto");
		assertEquals(0, result.messagesSummarized());
		assertEquals(0, result.tokensBefore());
		assertEquals(0, result.tokensAfter());
	}
}
