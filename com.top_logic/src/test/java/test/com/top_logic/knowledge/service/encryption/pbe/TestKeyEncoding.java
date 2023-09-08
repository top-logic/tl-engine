/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.encryption.pbe;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.TestCase;

/**
 * Test case that shows how to make a key from {@link Key#getEncoded()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestKeyEncoding extends TestCase {

	private static final String ALGORITHM = "DESede";

	private static final String ENCODING = "utf-8";

	private static final String PLAIN_TEXT = "plain text input";

	public void testEncodeDecode() throws Exception {
		SecretKey key = generateKey();

		byte[] cipherText = encrypt(key, PLAIN_TEXT);

		byte[] encodedKey = encodeKey(key);

		// Transmit encoded key and ciper text to receiver.

		SecretKey decodedKey = decodeKey(encodedKey);

		String recoveredPlainText = decrypt(decodedKey, cipherText);

		assertEquals(PLAIN_TEXT, recoveredPlainText);
	}

	private SecretKey generateKey() throws NoSuchAlgorithmException {
		SecretKey key = KeyGenerator.getInstance(ALGORITHM).generateKey();
		return key;
	}

	private byte[] encrypt(SecretKey key, String plainText) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

		Cipher encryption = Cipher.getInstance(ALGORITHM);
		encryption.init(Cipher.ENCRYPT_MODE, key);
	
		byte[] cipherText = encryption.doFinal(plainText.getBytes(ENCODING));
		return cipherText;
	}

	private byte[] encodeKey(SecretKey key) {
		byte[] encodedKey = key.getEncoded();
		return encodedKey;
	}

	private SecretKey decodeKey(byte[] encodedKey) {
		SecretKeySpec keySpec = new SecretKeySpec(encodedKey, ALGORITHM);
		return keySpec;
	}

	private String decrypt(SecretKey decodedKey, byte[] cipherText) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException,
			BadPaddingException {

		Cipher decryption = Cipher.getInstance(ALGORITHM);
		decryption.init(Cipher.DECRYPT_MODE, decodedKey);

		String recoveredPlainText = new String(decryption.doFinal(cipherText), ENCODING);
		return recoveredPlainText;
	}

}
