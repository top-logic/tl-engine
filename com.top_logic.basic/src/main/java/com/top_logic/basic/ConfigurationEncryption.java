/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.top_logic.basic.encryption.AuthenticatedEncryption;
import com.top_logic.basic.encryption.InvalidCipherTextException;
import com.top_logic.basic.encryption.SymmetricEncryption;

/**
 * Helper class to encrypt and decrypt configuration values.
 *
 * <p>
 * {@link #encrypt(String)} produces the authenticated format: the prefix {@value #AUTHENTICATED_PREFIX}
 * followed by the URL-safe Base64 encoding (without padding) of the {@link AuthenticatedEncryption
 * AES/GCM encryption} of the UTF-8 encoded value. Decryption verifies the integrity of such a
 * value, so a value that was modified or was never encrypted is rejected.
 * </p>
 *
 * <p>
 * {@link #decrypt(String)} also reads the unauthenticated format without prefix: the Base64
 * encoding of the {@link SymmetricEncryption AES/OFB encryption} of the UTF-8 encoded value. This
 * format cannot detect modifications, so a value in this format is only checked for being Base64
 * and decrypting to a non-empty, valid UTF-8 text. {@link #isAuthenticated(String)} tells the
 * formats apart, so that callers can ask for a value to be encrypted again.
 * </p>
 *
 * <p>
 * Both formats use the same key derived from a fixed passphrase. This protects configured secrets
 * only from casual readers of a configuration file, not from an attacker with access to the
 * application.
 * </p>
 *
 * @since 5.7.4
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigurationEncryption {

	/**
	 * Prefix of a value in the authenticated format produced by {@link #encrypt(String)}.
	 */
	public static final String AUTHENTICATED_PREFIX = "{gcm}";

	/**
	 * Algorithm of the configuration encryption key.
	 */
	private static final String KEY_ALGORITHM = "AES";

	/**
	 * Length in bytes of the configuration encryption key.
	 */
	private static final int KEY_LENGTH = 16;

	/**
	 * Characters of a value in the unauthenticated format: the standard and the URL-safe Base64
	 * alphabet with padding.
	 */
	private static final Pattern UNAUTHENTICATED_FORMAT = Pattern.compile("[A-Za-z0-9+/_=-]*");

	private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

	private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();

	private static final AuthenticatedEncryption AUTHENTICATED_ENCRYPTION;

	private static final SymmetricEncryption UNAUTHENTICATED_ENCRYPTION;

	static {
		// BEWARE: if the passphrase is changed, all projects using this feature must be informed so
		// that they can change values which are stored encrypted.
		String passphrase = "This is the passphrase for XMLProperties";
		SecretKey key = new SecretKeySpec(
			Arrays.copyOf(passphrase.getBytes(StandardCharsets.UTF_8), KEY_LENGTH), KEY_ALGORITHM);
		SecureRandom random = new SecureRandom();
		AUTHENTICATED_ENCRYPTION = new AuthenticatedEncryption(random, key);
		UNAUTHENTICATED_ENCRYPTION = new SymmetricEncryption(random, key);
	}

	/**
	 * Whether the given encrypted value is in the authenticated format produced by
	 * {@link #encrypt(String)}.
	 *
	 * <p>
	 * A value for which this method returns <code>false</code> is either invalid or in the
	 * unauthenticated format without prefix, which {@link #decrypt(String)} still reads, but which
	 * should be encrypted again.
	 * </p>
	 */
	public static boolean isAuthenticated(String encryptedValue) {
		return encryptedValue.startsWith(AUTHENTICATED_PREFIX);
	}

	/**
	 * Decrypts the given value.
	 *
	 * @param encryptedValue
	 *        A value in the authenticated format produced by {@link #encrypt(String)}, or in the
	 *        unauthenticated format without prefix.
	 *
	 * @return The decrypted value.
	 *
	 * @throws InvalidEncryptedValueException
	 *         If the given value is no valid encrypted value. The exception does not reveal the
	 *         given value.
	 *
	 * @see #encrypt(String)
	 * @see #isAuthenticated(String)
	 */
	public static String decrypt(String encryptedValue) throws InvalidEncryptedValueException {
		if (isAuthenticated(encryptedValue)) {
			return decryptAuthenticated(encryptedValue.substring(AUTHENTICATED_PREFIX.length()));
		} else {
			return decryptUnauthenticated(encryptedValue);
		}
	}

	private static String decryptAuthenticated(String base64) {
		byte[] cipherText;
		try {
			cipherText = BASE64_DECODER.decode(base64);
		} catch (IllegalArgumentException ex) {
			// The cause is dropped, since its message may contain characters of the value.
			throw new InvalidEncryptedValueException("Encrypted value is not Base64 encoded.");
		}
		if (!BASE64_ENCODER.encodeToString(cipherText).equals(base64)) {
			throw new InvalidEncryptedValueException("Encrypted value is not canonically Base64 encoded.");
		}
		byte[] plainText;
		try {
			plainText = AUTHENTICATED_ENCRYPTION.decrypt(cipherText);
		} catch (InvalidCipherTextException ex) {
			throw new InvalidEncryptedValueException("Encrypted value is not authentic.", ex);
		}
		return decodeUTF8(plainText);
	}

	private static String decryptUnauthenticated(String base64) {
		if (!UNAUTHENTICATED_FORMAT.matcher(base64).matches()) {
			throw new InvalidEncryptedValueException("Encrypted value is not Base64 encoded.");
		}
		byte[] cipherText = org.apache.commons.codec.binary.Base64.decodeBase64(base64);
		byte[] plainText;
		try {
			plainText = UNAUTHENTICATED_ENCRYPTION.decrypt(cipherText);
		} catch (IllegalArgumentException ex) {
			throw new InvalidEncryptedValueException("Encrypted value too short.");
		}
		if (plainText.length == 0) {
			throw new InvalidEncryptedValueException("Encrypted value too short.");
		}
		return decodeUTF8(plainText);
	}

	private static String decodeUTF8(byte[] plainText) {
		try {
			return StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.decode(ByteBuffer.wrap(plainText))
				.toString();
		} catch (CharacterCodingException ex) {
			// The cause is dropped, since it may describe the decrypted bytes.
			throw new InvalidEncryptedValueException("Encrypted value does not decrypt to UTF-8 text.");
		}
	}

	/**
	 * Encrypts the given value in the authenticated format.
	 *
	 * <p>
	 * The result consists of {@link #AUTHENTICATED_PREFIX} followed by URL-safe Base64 characters
	 * without padding, so it can be written into any text file. Each call uses a fresh random
	 * initialization vector, so encrypting the same value twice gives different results.
	 * </p>
	 *
	 * @return The encrypted value.
	 *
	 * @see #decrypt(String)
	 */
	public static String encrypt(String plaintext) {
		byte[] cipherText = AUTHENTICATED_ENCRYPTION.encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
		return AUTHENTICATED_PREFIX + BASE64_ENCODER.encodeToString(cipherText);
	}

}
