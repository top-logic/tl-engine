/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Signature algorithm of a JSON web signature (RFC 7518 §3.1).
 *
 * <p>
 * The constant names are the algorithm names of the header {@link OAuthConstants#HEADER_ALGORITHM}.
 * </p>
 */
@Label("JWS algorithm")
public enum JwsAlgorithm {

	/**
	 * RSASSA-PKCS1-v1_5 with SHA-256, signed with an RSA key.
	 */
	@Label("RS256")
	RS256("RSA", "SHA256withRSA", 0),

	/**
	 * ECDSA with the curve P-256 and SHA-256.
	 *
	 * <p>
	 * The signature is the concatenation of the values R and S of 32 bytes each (RFC 7518 §3.4), the
	 * format the JDK produces as <code>SHA256withECDSAinP1363Format</code>.
	 * </p>
	 */
	@Label("ES256")
	ES256("EC", "SHA256withECDSAinP1363Format", 256);

	private static final String PEM_BEGIN = "-----BEGIN PRIVATE KEY-----";

	private static final String PEM_END = "-----END PRIVATE KEY-----";

	private final String _keyAlgorithm;

	private final String _signatureAlgorithm;

	private final int _curveSize;

	JwsAlgorithm(String keyAlgorithm, String signatureAlgorithm, int curveSize) {
		_keyAlgorithm = keyAlgorithm;
		_signatureAlgorithm = signatureAlgorithm;
		_curveSize = curveSize;
	}

	/**
	 * The JCA name of the key algorithm, e.g. <code>RSA</code>.
	 */
	public String getKeyAlgorithm() {
		return _keyAlgorithm;
	}

	/**
	 * The JCA name of the signature algorithm, producing the signature format of a JSON web
	 * signature.
	 */
	public String getSignatureAlgorithm() {
		return _signatureAlgorithm;
	}

	/**
	 * Reads a private key for this algorithm in PKCS#8 PEM format (<code>-----BEGIN PRIVATE
	 * KEY-----</code>, as written by <code>openssl genpkey</code>).
	 *
	 * @param pem
	 *        The PEM text.
	 * @return The private key.
	 *
	 * @throws I18NRuntimeException
	 *         If the text is not a private key for this algorithm. The message does not contain
	 *         the key.
	 */
	public PrivateKey parsePrivateKey(String pem) {
		int begin = pem.indexOf(PEM_BEGIN);
		int end = pem.indexOf(PEM_END);
		if (begin < 0 || end < begin) {
			throw invalidKey();
		}
		String base64 = pem.substring(begin + PEM_BEGIN.length(), end);
		PrivateKey key;
		try {
			byte[] der = Base64.getMimeDecoder().decode(base64);
			key = KeyFactory.getInstance(_keyAlgorithm).generatePrivate(new PKCS8EncodedKeySpec(der));
		} catch (IllegalArgumentException | GeneralSecurityException ex) {
			// The cause is not reported, its message could quote parts of the key.
			throw invalidKey();
		}
		if (_curveSize > 0) {
			if (!(key instanceof ECPrivateKey)
				|| ((ECPrivateKey) key).getParams().getCurve().getField().getFieldSize() != _curveSize) {
				throw invalidKey();
			}
		}
		return key;
	}

	/**
	 * Signs the given data.
	 *
	 * @param key
	 *        The private key, as returned by {@link #parsePrivateKey(String)}.
	 * @param data
	 *        The data to sign.
	 * @return The signature in the format of a JSON web signature.
	 */
	public byte[] sign(PrivateKey key, byte[] data) {
		try {
			Signature signature = Signature.getInstance(_signatureAlgorithm);
			signature.initSign(key);
			signature.update(data);
			return signature.sign();
		} catch (GeneralSecurityException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SIGNING_FAILED__ALGORITHM.fill(name()), ex);
		}
	}

	private I18NRuntimeException invalidKey() {
		return new I18NRuntimeException(I18NConstants.ERROR_INVALID_PRIVATE_KEY__ALGORITHM.fill(name()));
	}

}
