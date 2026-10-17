/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.encryption;

import static java.nio.charset.StandardCharsets.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import junit.framework.TestCase;

import com.top_logic.basic.encryption.AuthenticatedEncryption;
import com.top_logic.basic.encryption.InvalidCipherTextException;

/**
 * Test for {@link AuthenticatedEncryption}.
 */
@SuppressWarnings("javadoc")
public class TestAuthenticatedEncryption extends TestCase {

	private static final String ALGORITHM = "AES";

	private AuthenticatedEncryption _encryption;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_encryption = newEncryption();
	}

	private static AuthenticatedEncryption newEncryption() throws NoSuchAlgorithmException {
		SecretKey key = KeyGenerator.getInstance(ALGORITHM).generateKey();
		return new AuthenticatedEncryption(new SecureRandom(), key);
	}

	public void testRoundTrip() throws InvalidCipherTextException {
		assertRoundTrip("Some secret text".getBytes(UTF_8));
	}

	public void testRoundTripEmpty() throws InvalidCipherTextException {
		assertRoundTrip(new byte[0]);
	}

	public void testRoundTripNonAscii() throws InvalidCipherTextException {
		assertRoundTrip("Pässwört € 中文 🔑".getBytes(UTF_8));
	}

	public void testRoundTripLarge() throws InvalidCipherTextException {
		byte[] plainText = new byte[10000];
		new Random(47).nextBytes(plainText);
		assertRoundTrip(plainText);
	}

	private void assertRoundTrip(byte[] plainText) throws InvalidCipherTextException {
		byte[] cipherText = _encryption.encrypt(plainText);
		assertEquals(_encryption.getCipherTextSize(plainText.length), cipherText.length);
		assertTrue(Arrays.equals(plainText, _encryption.decrypt(cipherText)));
	}

	public void testEncryptionRandomized() {
		byte[] plainText = "Some secret text".getBytes(UTF_8);
		byte[] first = _encryption.encrypt(plainText);
		byte[] second = _encryption.encrypt(plainText);
		assertFalse("Same plain text must be encrypted differently.", Arrays.equals(first, second));
		assertFalse("Initialization vectors must differ.", Arrays.equals(
			Arrays.copyOf(first, AuthenticatedEncryption.IV_LENGTH),
			Arrays.copyOf(second, AuthenticatedEncryption.IV_LENGTH)));
	}

	public void testDecryptWithOtherKey() throws NoSuchAlgorithmException {
		byte[] cipherText = _encryption.encrypt("Some secret text".getBytes(UTF_8));
		assertRejected(newEncryption(), cipherText);
	}

	public void testFlippedBitInIV() {
		assertFlippedBitRejected(0);
	}

	public void testFlippedBitInCipherText() {
		assertFlippedBitRejected(AuthenticatedEncryption.IV_LENGTH + 2);
	}

	public void testFlippedBitInTag() {
		assertFlippedBitRejected(-1);
	}

	public void testFlippedBitInEveryPosition() {
		byte[] cipherText = _encryption.encrypt("Secret".getBytes(UTF_8));
		for (int index = 0; index < cipherText.length; index++) {
			for (int bit = 0; bit < Byte.SIZE; bit++) {
				byte[] modified = cipherText.clone();
				modified[index] ^= 1 << bit;
				assertRejected(_encryption, modified);
			}
		}
	}

	private void assertFlippedBitRejected(int position) {
		byte[] cipherText = _encryption.encrypt("Some secret text".getBytes(UTF_8));
		int index = position < 0 ? cipherText.length + position : position;
		cipherText[index] ^= 0x01;
		assertRejected(_encryption, cipherText);
	}

	public void testTruncated() {
		byte[] cipherText = _encryption.encrypt("Some secret text".getBytes(UTF_8));
		for (int length = 0; length < cipherText.length; length++) {
			assertRejected(_encryption, Arrays.copyOf(cipherText, length));
		}
	}

	public void testShorterThanIV() {
		assertRejected(_encryption, new byte[0]);
		assertRejected(_encryption, new byte[AuthenticatedEncryption.IV_LENGTH - 1]);
	}

	public void testAppended() {
		byte[] cipherText = _encryption.encrypt("Some secret text".getBytes(UTF_8));
		assertRejected(_encryption, Arrays.copyOf(cipherText, cipherText.length + 1));
	}

	public void testRandomBytes() {
		Random random = new Random(42);
		for (int length = 0; length < 100; length++) {
			byte[] data = new byte[length];
			random.nextBytes(data);
			assertRejected(_encryption, data);
		}
	}

	public void testPlainTextPassword() {
		assertRejected(_encryption, "PlainSecret".getBytes(UTF_8));
		assertRejected(_encryption, "A rather long plain text password value".getBytes(UTF_8));
	}

	public void testMessageDoesNotContainInput() {
		String secret = "A rather long plain text password value";
		try {
			_encryption.decrypt(secret.getBytes(UTF_8));
			fail("Plain text must not be decryptable.");
		} catch (InvalidCipherTextException ex) {
			for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
				String message = String.valueOf(cause.getMessage());
				assertFalse("Input reported: " + message, message.contains(secret));
			}
		}
	}

	private static void assertRejected(AuthenticatedEncryption encryption, byte[] cipherText) {
		try {
			encryption.decrypt(cipherText);
			fail("Invalid cipher text of length " + cipherText.length + " must be rejected.");
		} catch (InvalidCipherTextException ex) {
			// Expected.
		}
	}

}
