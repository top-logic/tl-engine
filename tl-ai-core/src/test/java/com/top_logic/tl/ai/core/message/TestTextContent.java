/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import junit.framework.TestCase;

/**
 * Tests for {@link TextContent}.
 */
public class TestTextContent extends TestCase {

	/** TextContent stores text. */
	public void testTextContent() {
		TextContent content = new TextContent("hello");
		assertEquals("hello", content.text());
	}

	/** TextContent is a ContentPart. */
	public void testIsContentPart() {
		ContentPart part = new TextContent("hello");
		assertNotNull(part);
		assertTrue(part instanceof TextContent);
	}

	/** Two TextContent with same text are equal. */
	public void testEquality() {
		TextContent a = new TextContent("hello");
		TextContent b = new TextContent("hello");
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	/** TextContent with different text are not equal. */
	public void testInequality() {
		TextContent a = new TextContent("hello");
		TextContent b = new TextContent("world");
		assertFalse(a.equals(b));
	}
}
