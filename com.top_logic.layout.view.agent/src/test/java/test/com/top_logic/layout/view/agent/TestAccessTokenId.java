/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.agent;

import junit.framework.TestCase;

import com.top_logic.layout.view.agent.AccessTokens;

/**
 * Test of {@link AccessTokens#tokenId(String)}: the id of the token a plain secret names, also when
 * the id or the secret contains the separator, which belongs to the Base64url alphabet they are
 * encoded in.
 */
public class TestAccessTokenId extends TestCase {

	/**
	 * An id without separator.
	 */
	public void testPlainId() {
		assertEquals("jUlPmLM1ZQU",
			AccessTokens.tokenId("tlagt_jUlPmLM1ZQU_hP5yFCHcCVN5kGPZiuYG9SxV9Jzlm-mIp1qwR2XsLXQ"));
	}

	/**
	 * An id containing the separator is read by its length, not up to the first separator.
	 */
	public void testIdContainingSeparator() {
		assertEquals("oK_VBE-Ay70",
			AccessTokens.tokenId("tlagt_oK_VBE-Ay70_PrXgoE3o7RLFUSPLTiiWxkl6HvO4qO7Mek-uXkyLXVA"));
	}

	/**
	 * A secret containing the separator does not affect the id.
	 */
	public void testSecretContainingSeparator() {
		assertEquals("abcdefghijk", AccessTokens.tokenId("tlagt_abcdefghijk_a_b_c"));
	}

	/**
	 * Credentials not having the form of an access token name no token.
	 */
	public void testMalformed() {
		assertNull("Other prefix.", AccessTokens.tokenId("other_abcdefghijk_secret"));
		assertNull("Id too short.", AccessTokens.tokenId("tlagt_abc_secret"));
		assertNull("No separator after the id.", AccessTokens.tokenId("tlagt_abcdefghijkXsecret"));
		assertNull("No secret.", AccessTokens.tokenId("tlagt_abcdefghijk_"));
		assertNull("Nothing after the prefix.", AccessTokens.tokenId("tlagt_"));
	}

}
