/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import junit.framework.TestCase;

/**
 * Which name an empty chat greets its user with (#597).
 */
public class TestWelcomeName extends TestCase {

	/** The contact's first name is the name, whatever the full name starts with. */
	public void testFirstNameWins() {
		assertEquals("Anna", WelcomeName.of("Anna", "root"));
	}

	/**
	 * A first name of several words is kept whole: "Anna Maria" is one person's first name, not a
	 * first name and something else.
	 */
	public void testFirstNameIsNotSplit() {
		assertEquals("Anna Maria", WelcomeName.of("  Anna Maria ", "root"));
	}

	/** No contact, or a contact without a first name: the login name, as before. */
	public void testLoginNameWithoutFirstName() {
		assertEquals("root", WelcomeName.of(null, "root"));
		assertEquals("root", WelcomeName.of("", "root"));
		assertEquals("root", WelcomeName.of("   ", "root"));
	}

	/**
	 * Punctuation that ends a name is dropped, because the greeting puts its own full stop behind
	 * it: "Hallo Anna,." is what a comma typed into a contact form used to produce.
	 */
	public void testTrailingPunctuationDropped() {
		assertEquals("Anna", WelcomeName.of("Anna,", "root"));
		assertEquals("Anna", WelcomeName.of("Anna. ", "root"));
		assertEquals("Anna", WelcomeName.of("Anna ;:", "root"));
	}

	/** A first name that is nothing but punctuation is no first name. */
	public void testPunctuationOnlyFallsBack() {
		assertEquals("root", WelcomeName.of(",", "root"));
	}

	/**
	 * Punctuation inside a name belongs to it. Only the last stop of "J.R." goes, and the greeting's
	 * own full stop puts it back: "Hallo J.R."
	 */
	public void testInnerPunctuationKept() {
		assertEquals("J.R", WelcomeName.of("J.R.", "root"));
		assertEquals("Jean-Luc", WelcomeName.of("Jean-Luc", "root"));
	}

	/** Neither source says anything: the empty name, which the greeting renders as "Hallo.". */
	public void testNothingToGreetWith() {
		assertEquals("", WelcomeName.of(null, null));
	}

}
