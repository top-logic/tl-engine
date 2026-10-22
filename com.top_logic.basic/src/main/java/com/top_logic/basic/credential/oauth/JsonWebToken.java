/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.json.JSON;

/**
 * Creation of signed JSON web tokens (RFC 7519) in compact serialization (RFC 7515 §7.1).
 */
public final class JsonWebToken {

	private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

	private JsonWebToken() {
		// Static utility.
	}

	/**
	 * Creates a signed JSON web token.
	 *
	 * @param algorithm
	 *        The signature algorithm.
	 * @param key
	 *        The private key to sign with.
	 * @param keyId
	 *        The identifier of the key for the header {@link OAuthConstants#HEADER_KEY_ID}, or
	 *        <code>null</code>.
	 * @param claims
	 *        The claims of the token, values as supported by {@link JSON}.
	 * @return The token: <code>base64url(header).base64url(claims).base64url(signature)</code>.
	 */
	public static String sign(JwsAlgorithm algorithm, PrivateKey key, String keyId, Map<String, ?> claims) {
		Map<String, Object> header = new LinkedHashMap<>();
		header.put(OAuthConstants.HEADER_ALGORITHM, algorithm.name());
		header.put(OAuthConstants.HEADER_TYPE, OAuthConstants.TYPE_JWT);
		if (!StringServices.isEmpty(keyId)) {
			header.put(OAuthConstants.HEADER_KEY_ID, keyId);
		}
		String signingInput = encode(JSON.toString(header)) + "." + encode(JSON.toString(claims));
		byte[] signature = algorithm.sign(key, signingInput.getBytes(StandardCharsets.US_ASCII));
		return signingInput + "." + BASE64_URL.encodeToString(signature);
	}

	private static String encode(String json) {
		return BASE64_URL.encodeToString(json.getBytes(StandardCharsets.UTF_8));
	}

}
