/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.accesscontrol;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.base.accesscontrol.ExternalIdentity;

/**
 * Test for the value type {@link ExternalIdentity}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestExternalIdentity extends TestCase {

	private static final String CLIENT = "test-client";

	private static final String SUBJECT = "subject-4711";

	private static final String NAME = "someone";

	private static final String EMAIL_CLAIM = "email";

	private static final String EMAIL = "someone@example.com";

	/**
	 * The identity keeps the claims it was created with, even when the given map changes later,
	 * and does not let them be changed.
	 */
	public void testClaimsAreCopied() {
		Map<String, Object> claims = new HashMap<>();
		claims.put(EMAIL_CLAIM, EMAIL);
		ExternalIdentity identity = new ExternalIdentity(CLIENT, SUBJECT, NAME, claims);
		claims.clear();

		assertEquals(EMAIL, identity.getClaim(EMAIL_CLAIM));
		assertEquals(Map.of(EMAIL_CLAIM, EMAIL), identity.getClaims());
		try {
			identity.getClaims().remove(EMAIL_CLAIM);
			fail("The claims of an identity must not be modifiable.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}

	/**
	 * Without claims, an identity has an empty claim map.
	 */
	public void testNoClaims() {
		ExternalIdentity identity = new ExternalIdentity(CLIENT, SUBJECT, NAME, null);

		assertTrue(identity.getClaims().isEmpty());
		assertNull(identity.getClaim(EMAIL_CLAIM));
	}

	/**
	 * Identities with equal parts are equal.
	 */
	public void testEquality() {
		ExternalIdentity identity = new ExternalIdentity(CLIENT, SUBJECT, NAME, Map.of(EMAIL_CLAIM, EMAIL));

		assertEquals(identity, new ExternalIdentity(CLIENT, SUBJECT, NAME, Map.of(EMAIL_CLAIM, EMAIL)));
		assertEquals(identity.hashCode(),
			new ExternalIdentity(CLIENT, SUBJECT, NAME, Map.of(EMAIL_CLAIM, EMAIL)).hashCode());
		assertFalse(identity.equals(new ExternalIdentity(CLIENT, "other-subject", NAME, Map.of(EMAIL_CLAIM, EMAIL))));
	}

	/**
	 * The string representation names the claims but does not disclose their values.
	 */
	public void testToStringHidesClaimValues() {
		String text = new ExternalIdentity(CLIENT, SUBJECT, NAME, Map.of(EMAIL_CLAIM, EMAIL)).toString();

		assertTrue(text.contains(SUBJECT));
		assertTrue(text.contains(EMAIL_CLAIM));
		assertFalse("Claim values must not end up in a log.", text.contains(EMAIL));
	}

}
