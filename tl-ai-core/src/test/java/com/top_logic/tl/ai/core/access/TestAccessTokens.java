/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import junit.framework.TestCase;

/**
 * The format of a personal access token, and what is kept of it.
 *
 * <p>
 * That a token resolves to its owner, and that a revoked or expired one is refused, needs real
 * accounts and is asserted in {@code tl-ai-demo}'s {@code SecDemoMcpEndpointTest}.
 * </p>
 */
public class TestAccessTokens extends TestCase {

	/** A new token is the prefix and the secret, and is recognised as a token. */
	public void testANewTokenHasTheFormat() {
		String token = AccessTokens.newToken();

		assertTrue(token, token.startsWith("tlpat_"));
		assertEquals(token, "tlpat_".length() + 43, token.length());
		assertTrue(token, token.substring("tlpat_".length()).matches("[0-9A-Za-z]+"));
		assertTrue(AccessTokens.hasFormat(token));
	}

	/**
	 * What is not a token is told apart without a lookup - which is also what lets another bearer
	 * scheme configured beside this one take the request.
	 */
	public void testWhatIsNotATokenIsRecognised() {
		String token = AccessTokens.newToken();

		assertFalse("Another prefix.", AccessTokens.hasFormat("ghp_" + token.substring(6)));
		assertFalse("Too short.", AccessTokens.hasFormat(token.substring(0, token.length() - 1)));
		assertFalse("Too long.", AccessTokens.hasFormat(token + "A"));
		assertFalse("A character outside the alphabet.",
			AccessTokens.hasFormat(token.substring(0, token.length() - 1) + "-"));
		assertFalse("An OAuth access token, a JWT.",
			AccessTokens.hasFormat("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhbm5hIn0.c2lnbmF0dXJl"));
		assertFalse(AccessTokens.hasFormat(null));
	}

	/** The stored hash is the hex SHA-256 of the token: the FIPS 180-2 test vector "abc". */
	public void testTheHashIsSha256() {
		assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
			AccessTokens.hash("abc"));
	}

	/** The hint keeps the prefix and four characters of the secret, and no more. */
	public void testTheHintKeepsFourCharactersOfTheSecret() {
		String token = AccessTokens.newToken();

		assertEquals(token.substring(0, "tlpat_".length() + 4) + "…", AccessTokens.hint(token));
	}

}
