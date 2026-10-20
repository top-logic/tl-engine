/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;

/**
 * Tests for the cap on an account's preference text.
 *
 * <p>
 * The text is sent with every single request of that account, which is why it is refused where it
 * is entered rather than truncated where it is used.
 * </p>
 */
public class TestPreferencesLengthConstraint extends TestCase {

	/** A text at the cap is still allowed; the cap is a length, not a bound to stay under. */
	public void testATextAtTheCapIsAllowed() {
		assertNull(PreferencesLengthConstraint.refusal("x".repeat(1000), 1000));
	}

	/** One character more is refused, and the message names both numbers. */
	public void testALongerTextIsRefused() {
		ResKey refusal = PreferencesLengthConstraint.refusal("x".repeat(1001), 1000);

		assertNotNull("A text above the cap must be refused.", refusal);
		String message = refusal.toString();
		assertTrue(message, message.contains("1001"));
		assertTrue(message, message.contains("1000"));
	}

	/** Nothing to check. */
	public void testNoTextIsNoRefusal() {
		assertNull(PreferencesLengthConstraint.refusal(null, 1000));
		assertNull(PreferencesLengthConstraint.refusal("", 1000));
	}

	/**
	 * A cap of zero or less is no cap.
	 *
	 * <p>
	 * An installation that wants no limit says so by clearing the number, not by typing one nobody
	 * could reach.
	 * </p>
	 */
	public void testAClearedCapAllowsAnything() {
		assertNull(PreferencesLengthConstraint.refusal("x".repeat(100_000), 0));
		assertNull(PreferencesLengthConstraint.refusal("x".repeat(100_000), -1));
	}

	/** The shipped default, for an application that configures none. */
	public void testTheShippedDefaultIsAThousand() {
		assertEquals(1000, EnvironmentSettings.DEFAULT_MAX_PREFERENCES_LENGTH);
		assertEquals(1000, EnvironmentSettings.maxPreferencesLength());
	}

	/** With no configuration at all the preference texts are sent - the switch defaults to on. */
	public void testPreferencesAreOnByDefault() {
		assertTrue(EnvironmentSettings.userPreferencesEnabled());
	}

	/** No account, no preference text - a scheduled run acts for nobody in particular. */
	public void testARunWithoutAnAccountHasNoPreferences() {
		assertNull(UserPreferences.of(null));
	}
}
