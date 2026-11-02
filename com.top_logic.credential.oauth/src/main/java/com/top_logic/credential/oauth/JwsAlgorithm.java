/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyType;

import com.top_logic.basic.config.annotation.Label;

/**
 * Signature algorithm of a client assertion (RFC 7518 §3.1).
 *
 * <p>
 * The constant names are the algorithm names of the JSON web signature.
 * </p>
 *
 * @implNote The assertion is signed by the Nimbus JOSE library with the corresponding
 *           {@link JWSAlgorithm}.
 */
@Label("JWS algorithm")
public enum JwsAlgorithm {

	/**
	 * RSASSA-PKCS1-v1_5 with SHA-256, signed with an RSA key.
	 */
	@Label("RS256")
	RS256(JWSAlgorithm.RS256),

	/**
	 * RSASSA-PKCS1-v1_5 with SHA-384, signed with an RSA key.
	 */
	@Label("RS384")
	RS384(JWSAlgorithm.RS384),

	/**
	 * RSASSA-PKCS1-v1_5 with SHA-512, signed with an RSA key.
	 */
	@Label("RS512")
	RS512(JWSAlgorithm.RS512),

	/**
	 * RSASSA-PSS with SHA-256, signed with an RSA key.
	 */
	@Label("PS256")
	PS256(JWSAlgorithm.PS256),

	/**
	 * RSASSA-PSS with SHA-384, signed with an RSA key.
	 */
	@Label("PS384")
	PS384(JWSAlgorithm.PS384),

	/**
	 * RSASSA-PSS with SHA-512, signed with an RSA key.
	 */
	@Label("PS512")
	PS512(JWSAlgorithm.PS512),

	/**
	 * ECDSA with the curve P-256 and SHA-256.
	 */
	@Label("ES256")
	ES256(JWSAlgorithm.ES256),

	/**
	 * ECDSA with the curve P-384 and SHA-384.
	 */
	@Label("ES384")
	ES384(JWSAlgorithm.ES384),

	/**
	 * ECDSA with the curve P-521 and SHA-512.
	 */
	@Label("ES512")
	ES512(JWSAlgorithm.ES512);

	private final JWSAlgorithm _algorithm;

	JwsAlgorithm(JWSAlgorithm algorithm) {
		_algorithm = algorithm;
	}

	/**
	 * The algorithm of the Nimbus JOSE library.
	 */
	public JWSAlgorithm getAlgorithm() {
		return _algorithm;
	}

	/**
	 * The type of the key signing with this algorithm.
	 */
	public KeyType getKeyType() {
		return KeyType.forAlgorithm(_algorithm);
	}

}
