/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.List;
import java.util.function.Predicate;

import junit.framework.TestCase;

/**
 * Tests {@link HiddenElements#keep(java.util.Collection, java.util.Collection, Predicate)}.
 *
 * <p>
 * The elements are plain texts: the merge does not look at what an element is. A text that starts
 * with {@code "hidden"} is one that the writer does not see.
 * </p>
 */
public class TestHiddenElements extends TestCase {

	private static final Predicate<Object> SEEN = element -> !((String) element).startsWith("hidden");

	/** Each hidden element goes back to its stored index, between the written elements. */
	public void testSeveralHiddenElementsKeepTheirPlaces() {
		assertEquals(List.of("c", "hidden1", "a", "hidden2"),
			HiddenElements.keep(List.of("a", "hidden1", "b", "hidden2", "c"), List.of("c", "a"), SEEN));
	}

	/** A written value that is shorter than a stored index takes the hidden element at its end. */
	public void testAShorterWrittenValueTakesTheHiddenElementsAtItsEnd() {
		List<String> stored = List.of("a", "b", "c", "hidden1", "hidden2");

		assertEquals(List.of("hidden1", "hidden2"), HiddenElements.keep(stored, List.of(), SEEN));
		assertEquals(List.of("x", "hidden1", "hidden2"), HiddenElements.keep(stored, List.of("x"), SEEN));
	}

	/** Without a hidden element, the written value is the value to store. */
	public void testWithoutAHiddenElementTheWrittenValueStays() {
		assertEquals(List.of("b", "x"), HiddenElements.keep(List.of("a", "b"), List.of("b", "x"), SEEN));
	}

	/** A hidden element that the written value holds already is not added a second time. */
	public void testAHiddenElementThatIsWrittenIsNotAddedAgain() {
		assertEquals(List.of("hidden", "a"),
			HiddenElements.keep(List.of("a", "hidden"), List.of("hidden", "a"), SEEN));
	}

	/** In a value that holds a hidden element twice, each copy counts. */
	public void testEachCopyOfAHiddenElementCounts() {
		List<String> stored = List.of("hidden", "a", "hidden");

		assertEquals(List.of("hidden", "a", "hidden"), HiddenElements.keep(stored, List.of("a"), SEEN));
		assertEquals(List.of("hidden", "a", "hidden"), HiddenElements.keep(stored, List.of("hidden", "a"), SEEN));
	}
}
