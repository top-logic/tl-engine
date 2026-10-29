/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.encryption;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Utility for authenticated symmetric encryption.
 * 
 * <p>
 * The plain text is encrypted in GCM mode of the key's block cipher (e.g. AES). The cipher text
 * consists of a random initialization vector of {@link #IV_LENGTH} bytes followed by the encrypted
 * data and an authentication tag of {@link #TAG_LENGTH_BITS} bits. Decryption verifies the tag, so
 * a cipher text that was modified, produced with another key, or is no cipher text at all is
 * rejected with an {@link InvalidCipherTextException} instead of being decrypted to arbitrary
 * bytes.
 * </p>
 * 
 * <p>
 * In contrast to {@link SymmetricEncryption}, the cipher text is not streamable: the whole cipher
 * text must be available to verify its integrity before any plain text is released.
 * </p>
 */
public class AuthenticatedEncryption {

	/**
	 * Mode and padding of the authenticated cipher.
	 */
	private static final String MODE_AND_PADDING = "/GCM/NoPadding";

	/**
	 * Length in bytes of the random initialization vector prefixed to each cipher text.
	 */
	public static final int IV_LENGTH = 12;

	/**
	 * Length in bits of the authentication tag appended to each cipher text.
	 */
	public static final int TAG_LENGTH_BITS = 128;

	/**
	 * Length in bytes of the authentication tag.
	 */
	public static final int TAG_LENGTH = TAG_LENGTH_BITS / Byte.SIZE;

	private final SecretKey _key;

	private final SecureRandom _random;

	/**
	 * Creates a {@link AuthenticatedEncryption}.
	 *
	 * @param random
	 *        The source for random initialization vectors.
	 * @param key
	 *        The encryption key.
	 */
	public AuthenticatedEncryption(SecureRandom random, SecretKey key) {
		_key = key;
		_random = random;
	}

	/**
	 * The exact cipher text size (including initialization vector and authentication tag) for the
	 * given plain text size.
	 */
	public long getCipherTextSize(long plainTextSize) {
		return IV_LENGTH + plainTextSize + TAG_LENGTH;
	}

	/**
	 * Encrypts the given plain text.
	 * 
	 * @return The initialization vector followed by the encrypted data and the authentication tag.
	 */
	public byte[] encrypt(byte[] plainText) {
		byte[] iv = new byte[IV_LENGTH];
		_random.nextBytes(iv);

		Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, iv);
		byte[] result = Arrays.copyOf(iv, IV_LENGTH + cipher.getOutputSize(plainText.length));
		try {
			cipher.doFinal(plainText, 0, plainText.length, result, IV_LENGTH);
		} catch (GeneralSecurityException ex) {
			throw error("Encryption failed.", ex);
		}
		return result;
	}

	/**
	 * Decrypts the given cipher text and verifies its integrity.
	 *
	 * @param cipherText
	 *        A cipher text created by {@link #encrypt(byte[])} with the same key.
	 * @return The plain text.
	 * @throws InvalidCipherTextException
	 *         If the given data is too short, was modified, or was not encrypted with this key.
	 */
	public byte[] decrypt(byte[] cipherText) throws InvalidCipherTextException {
		if (cipherText.length < IV_LENGTH + TAG_LENGTH) {
			throw new InvalidCipherTextException("Cipher text too short.");
		}
		Cipher cipher = getCipher(Cipher.DECRYPT_MODE, Arrays.copyOf(cipherText, IV_LENGTH));
		try {
			return cipher.doFinal(cipherText, IV_LENGTH, cipherText.length - IV_LENGTH);
		} catch (AEADBadTagException ex) {
			throw new InvalidCipherTextException("Cipher text not authentic.", ex);
		} catch (IllegalBlockSizeException | BadPaddingException ex) {
			throw new InvalidCipherTextException("Invalid cipher text.", ex);
		}
	}

	private Cipher getCipher(int mode, byte[] iv) {
		try {
			Cipher cipher = Cipher.getInstance(_key.getAlgorithm() + MODE_AND_PADDING);
			cipher.init(mode, _key, new GCMParameterSpec(TAG_LENGTH_BITS, iv), _random);
			return cipher;
		} catch (InvalidKeyException ex) {
			throw errorInvalidKey(ex);
		} catch (NoSuchAlgorithmException ex) {
			throw errorUnsupportedEncryptionAlgorithm(ex);
		} catch (NoSuchPaddingException ex) {
			throw errorUnsupportedPaddingAlgorithm(ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw errorInvalidAlgorithmParameters(ex);
		}
	}

	private AssertionError errorUnsupportedEncryptionAlgorithm(NoSuchAlgorithmException ex) {
		return error("Unsupported encryption algorithm.", ex);
	}

	private AssertionError errorUnsupportedPaddingAlgorithm(NoSuchPaddingException ex) {
		return error("Unsupported padding algorithm.", ex);
	}

	private AssertionError errorInvalidAlgorithmParameters(InvalidAlgorithmParameterException ex) {
		return error("Invalid algorithm parameters.", ex);
	}

	private AssertionError errorInvalidKey(InvalidKeyException ex) {
		return error("Invalid encryption key.", ex);
	}

	private AssertionError error(String message, Throwable cause) {
		return new AssertionError(message, cause);
	}

}
