/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.pbe;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.UnreachableAssertion;

/**
 * Utility that makes password based encryption and decryption from JCE easily usable.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PasswordBasedEncryption {

	private final String _algorithm;

	private final int _saltLength;

	private final int _iterations;

	private final SecretKey _key;

	private Random _random;

	/**
	 * Creates a {@link PasswordBasedEncryption}.
	 * 
	 * @param secureRandom
	 *        The random source for creating salt bytes.
	 * @param algorithm
	 *        The PBE algorithm.
	 * @param saltLength
	 *        The number of salt bytes to use.
	 * @param iterations
	 *        The number of iteration (more to make brute force attacks harder).
	 * @param password
	 *        The password to encrypt with.
	 * @throws IllegalArgumentException
	 *         If the algorithms are not supported
	 * @throws InvalidPasswordException
	 *         If the PBE algorithm cannot handle the password.
	 */
	public PasswordBasedEncryption(Random secureRandom, String algorithm, int saltLength, int iterations,
			char[] password) throws IllegalArgumentException, InvalidPasswordException {
		_random = secureRandom;

		_algorithm = algorithm;
		_saltLength = saltLength;
		_iterations = iterations;
		try {
			_key = createKey(password);

			// Check potential error conditions in the arguments.
			internalCreateEncryption(createSalt());
		} catch (NoSuchAlgorithmException ex) {
			throw fail("Algorithm not supported.", ex);
		} catch (InvalidKeySpecException ex) {
			throw fail("Invalid password.", ex);
		} catch (InvalidKeyException ex) {
			throw (InvalidPasswordException) new InvalidPasswordException().initCause(ex);
		} catch (NoSuchPaddingException ex) {
			throw fail("Padding not supported.", ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw fail("Invalid algorithm.", ex);
		}
	}

	/**
	 * Encrypts the given plain text and returns a base 64 encoding of the salt and cipher text.
	 * 
	 * @see #decryptEncoded(String)
	 */
	public String encryptEncoded(byte[] plaintext) {
		return encode(encrypt(plaintext));
	}

	/**
	 * Encrypts the given plain text.
	 */
	public EncryptionResult encrypt(byte[] plaintext) {
		try {
			byte[] salt = createSalt();
			Cipher cipher = createEncryption(salt);

			byte[] ciphertext = encrypt(cipher, plaintext);

			return new EncryptionResult(salt, ciphertext);
		} catch (IllegalBlockSizeException ex) {
			throw unreachableInEncryption(ex);
		} catch (BadPaddingException ex) {
			throw unreachableInEncryption(ex);
		}
	}

	private Cipher createEncryption(byte[] salt) {
		try {
			return internalCreateEncryption(salt);
		} catch (InvalidKeyException ex) {
			throw unreachableCheckedInConstructor(ex);
		} catch (NoSuchAlgorithmException ex) {
			throw unreachableCheckedInConstructor(ex);
		} catch (NoSuchPaddingException ex) {
			throw unreachableCheckedInConstructor(ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw unreachableCheckedInConstructor(ex);
		}
	}

	private Cipher internalCreateEncryption(byte[] salt) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException {
		return internalCreateCipher(salt, Cipher.ENCRYPT_MODE);
	}

	private byte[] createSalt() {
		byte[] salt = new byte[_saltLength];
		_random.nextBytes(salt);
		return salt;
	}

	private SecretKey createKey(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Create the PBEKeySpec with the given password
		PBEKeySpec keySpec = new PBEKeySpec(password);

		// Get a SecretKeyFactory for PBEWithSHAAndTwofish
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(_algorithm);

		return keyFactory.generateSecret(keySpec);
	}

	private static byte[] encrypt(Cipher cipher, byte[] plaintext) throws IllegalBlockSizeException,
			BadPaddingException {
		byte[] ciphertext = cipher.doFinal(plaintext);
		return ciphertext;
	}

	/**
	 * Decrypts an encoded cipher text produced by {@link #encryptEncoded(byte[])}
	 * 
	 * @return The recovered plain text.
	 */
	public byte[] decryptEncoded(String encodedSaltAndCipherText) throws IllegalBlockSizeException, BadPaddingException {
		return decrypt(decode(encodedSaltAndCipherText));
	}

	/**
	 * Decrypts the given cipher text.
	 * 
	 * @return The recovered plain text.
	 */
	public byte[] decrypt(EncryptionResult encryption) throws IllegalBlockSizeException, BadPaddingException {
		return decrypt(encryption.getSalt(), encryption.getCipherText());
	}

	private byte[] decrypt(byte[] salt, byte[] ciphertext) throws IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = createDecryption(salt);

		return decrypt(cipher, ciphertext);
	}

	private Cipher createDecryption(byte[] salt) {
		try {
			return internalCreateCipher(salt, Cipher.DECRYPT_MODE);
		} catch (InvalidKeyException ex) {
			throw unreachableCheckedInConstructor(ex);
		} catch (NoSuchAlgorithmException ex) {
			throw unreachableCheckedInConstructor(ex);
		} catch (NoSuchPaddingException ex) {
			throw unreachableCheckedInConstructor(ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw unreachableCheckedInConstructor(ex);
		}
	}

	private Cipher internalCreateCipher(byte[] salt, int mode) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, _iterations);
		Cipher cipher = Cipher.getInstance(_algorithm);
		cipher.init(mode, _key, paramSpec);
		return cipher;
	}

	private static byte[] decrypt(Cipher cipher, byte[] ciphertext) throws IllegalBlockSizeException,
			BadPaddingException {
		return cipher.doFinal(ciphertext);
	}

	private static IllegalArgumentException fail(String message, Throwable ex) {
		return new IllegalArgumentException(message, ex);
	}

	private UnreachableAssertion unreachableCheckedInConstructor(Throwable ex) {
		return new UnreachableAssertion("Condition checked in constructor.", ex);
	}

	private UnreachableAssertion unreachableInEncryption(Throwable ex) {
		return new UnreachableAssertion("Can only happen in decryption.", ex);
	}

	private String encode(EncryptionResult result) {
		byte[] saltAndCipherText = ArrayUtil.join(result.getSalt(), result.getCipherText());
		return new Base64().encodeToString(saltAndCipherText);
	}

	private EncryptionResult decode(String encodedSaltAndCipherText) {
		byte[] saltAndCipherText = new Base64().decode(encodedSaltAndCipherText);
		byte[] salt = ArrayUtil.copy(saltAndCipherText, 0, _saltLength);
		byte[] cipherText = ArrayUtil.copy(saltAndCipherText, _saltLength, saltAndCipherText.length - _saltLength);
		return new EncryptionResult(salt, cipherText);
	}

}
