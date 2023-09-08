/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.nio.charset.Charset;
import java.security.SecureRandom;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.encryption.SymmetricEncryption;

/**
 * The class {@link Base64Encoder} encrypts / decrypts strings andencodes / decodes it Base64.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class Base64Encoder {

	private static final Charset CHARSET_UTF8 = StringServices.CHARSET_UTF_8;

	private final SymmetricEncryption _encrypter;

	/**
	 * Creates a new {@link Base64Encoder} with the given key.
	 */
	public Base64Encoder(byte[] key) {
		_encrypter = newEncryption(key);
	}
	
	/**
	 * Service method to compute bytes to use {@link Base64Encoder#Base64Encoder(byte[])} from a
	 * potential {@link String} Constructor
	 */
	protected static byte[] toBytes(String key) {
		return key.getBytes(CHARSET_UTF8);
	}

	private SymmetricEncryption newEncryption(byte[] key) {
		return new SymmetricEncryption(newSecureRandom(), newKey(key));
	}

	private SecureRandom newSecureRandom() {
		return new SecureRandom();
	}

	/**
	 * Creates the actual {@link SecretKey} used to create the {@link SymmetricEncryption}, which is
	 * used in {@link #encode(String)} and {@link #decode(String)}.
	 * 
	 * @param key
	 *        The key given to {@link Base64Encoder#Base64Encoder(byte[])}.
	 */
	protected abstract SecretKey newKey(byte[] key);

	/**
	 * Decodes the given value formerly encoded by {@link #encode(String)}.
	 * 
	 * @param encodedValue
	 *        The encoded value.
	 * 
	 * @return The decoded value.
	 * 
	 * @see #encode(String)
	 */
	public String decode(String encodedValue) {
		byte[] decrypted = decode(encodedValue.getBytes(CHARSET_UTF8));
		return new String(decrypted, CHARSET_UTF8);
	}

	/**
	 * Encodes the given value for later decoding using {@link #decode(String)}.
	 * 
	 * The returned string is URL-safe that means: (see comment for the method
	 * encodeBase64URLSafeString in {@link Base64}:
	 * 
	 * <p>
	 * <tt>
	 * Encodes binary data using a URL-safe variation of the base64 algorithm but does not chunk the
	 * output. The url-safe variation emits - and _ instead of + and / characters. Note: no padding
	 * is added.
	 * </tt>
	 * </p>
	 * 
	 * @param value
	 *        The value to encode.
	 * 
	 * @return The decoded value.
	 * 
	 * @see #decode(String)
	 */
	public String encode(String value) {
		byte[] base64Encoded = encode(value.getBytes(CHARSET_UTF8));
		return new String(base64Encoded, CHARSET_UTF8);
	}

	/**
	 * Decodes the given value formerly encoded by {@link #encode(byte[])}.
	 * 
	 * @param encodedValue
	 *        The encoded value.
	 * 
	 * @return The decoded value.
	 * 
	 * @see #encode(byte[])
	 */
	private byte[] decode(byte[] encodedValue) {
		byte[] base64Decoded = Base64.decodeBase64(encodedValue);
		return _encrypter.decrypt(base64Decoded);
	}

	/**
	 * Encodes the given value for later decoding using {@link #decode(byte[])}.
	 * 
	 * The returned bytes are URL-safe that means: (see comment for the method
	 * encodeBase64URLSafeString in {@link Base64}:
	 * 
	 * <p>
	 * <tt>
	 * Encodes binary data using a URL-safe variation of the base64 algorithm but does not chunk the
	 * output. The url-safe variation emits - and _ instead of + and / characters. Note: no padding
	 * is added.
	 * </tt>
	 * </p>
	 * 
	 * @param value
	 *        The value to encode.
	 * 
	 * @return The decoded value.
	 * 
	 * @see #decode(byte[])
	 */
	private byte[] encode(byte[] value) {
		byte[] encrypted = _encrypter.encrypt(value);
		return Base64.encodeBase64URLSafe(encrypted);
	}

	/**
	 * Creates a {@link Base64Encoder} which uses the algorithm "AES" for symmetric encryption.
	 */
	public static final Base64Encoder newBase64AESEncoder(String key) {
		return new Base64AESEncoder(toBytes(key));
	}

}

