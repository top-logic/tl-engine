/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Random bearer secrets that are recognisable by a prefix and stored only as their hash.
 *
 * <p>
 * A token is a prefix followed by {@value #SECRET_LENGTH} characters drawn uniformly from
 * {@code [0-9A-Za-z]} by a {@link SecureRandom} - {@code 43 * log2(62)}, about 256 bits of
 * randomness. The prefix names what the token is for: a secret scanner can match a leaked one by
 * pattern, a person can tell it from a password, and a receiver can refuse a token of another kind
 * without a database lookup. Base 62 rather than base 64, so that a double click selects the whole
 * token.
 * </p>
 *
 * <p>
 * What is stored to find a token again is its {@link #hash(String) SHA-256}, never the token. An
 * unsalted fast hash is right here and would be wrong for a password: the secret is 256 random
 * bits, not something a person chose, so there is no dictionary to run against the hash, and the
 * lookup by hash is a single comparison or indexed query.
 * </p>
 *
 * @see AccessTokens
 */
public final class SecretTokens {

	/** Number of random characters after the prefix. */
	public static final int SECRET_LENGTH = 43;

	private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static final String HASH_ALGORITHM = "SHA-256";

	private static final SecureRandom RANDOM = new SecureRandom();

	private SecretTokens() {
		// Static utility.
	}

	/**
	 * A new random token.
	 *
	 * @param prefix
	 *        What the token starts with, e.g. {@value AccessTokens#PREFIX}.
	 */
	public static String newToken(String prefix) {
		StringBuilder result = new StringBuilder(prefix.length() + SECRET_LENGTH).append(prefix);
		for (int n = 0; n < SECRET_LENGTH; n++) {
			result.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}
		return result.toString();
	}

	/**
	 * Whether {@code candidate} looks like a token {@link #newToken(String) made} with the given
	 * prefix: the prefix and {@value #SECRET_LENGTH} characters of the alphabet.
	 */
	public static boolean hasFormat(String prefix, String candidate) {
		if (candidate == null || candidate.length() != prefix.length() + SECRET_LENGTH
			|| !candidate.startsWith(prefix)) {
			return false;
		}
		for (int n = prefix.length(); n < candidate.length(); n++) {
			if (ALPHABET.indexOf(candidate.charAt(n)) < 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * What is stored to find {@code token} again: the lower-case hex SHA-256 of its UTF-8 bytes.
	 */
	public static String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			// Every Java platform is required to provide SHA-256.
			throw new IllegalStateException(ex);
		}
	}

}
