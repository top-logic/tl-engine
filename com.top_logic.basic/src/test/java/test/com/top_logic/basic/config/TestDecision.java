/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.Decision.*;
import junit.framework.TestCase;

import com.top_logic.basic.config.Decision;

/**
 * Test for {@link Decision}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDecision extends TestCase {

	public void testToBoolean() {
		assertTrue(DEFAULT.toBoolean(true));
		assertFalse(DEFAULT.toBoolean(false));
		assertTrue(TRUE.toBoolean(true));
		assertTrue(TRUE.toBoolean(false));
		assertFalse(FALSE.toBoolean(true));
		assertFalse(FALSE.toBoolean(false));
	}

	public void testNot() {
		assertEquals(DEFAULT, not(DEFAULT));
		assertEquals(TRUE, not(FALSE));
		assertEquals(FALSE, not(TRUE));
	}

	public void testAnd() {
		assertEquals(DEFAULT, DEFAULT.and(DEFAULT));
		assertEquals(TRUE, DEFAULT.and(TRUE));
		assertEquals(FALSE, DEFAULT.and(FALSE));

		assertEquals(TRUE, TRUE.and(DEFAULT));
		assertEquals(TRUE, TRUE.and(TRUE));
		assertEquals(FALSE, TRUE.and(FALSE));

		assertEquals(FALSE, FALSE.and(DEFAULT));
		assertEquals(FALSE, FALSE.and(TRUE));
		assertEquals(FALSE, FALSE.and(FALSE));
	}

	public void testOr() {
		assertEquals(DEFAULT, DEFAULT.or(DEFAULT));
		assertEquals(TRUE, DEFAULT.or(TRUE));
		assertEquals(FALSE, DEFAULT.or(FALSE));

		assertEquals(TRUE, TRUE.or(DEFAULT));
		assertEquals(TRUE, TRUE.or(TRUE));
		assertEquals(TRUE, TRUE.or(FALSE));

		assertEquals(FALSE, FALSE.or(DEFAULT));
		assertEquals(TRUE, FALSE.or(TRUE));
		assertEquals(FALSE, FALSE.or(FALSE));
	}

}

