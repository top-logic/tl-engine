/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import junit.framework.TestCase;

import com.top_logic.basic.encryption.SymmetricEncryption;

/**
 * Test for {@link SymmetricEncryption}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestSymmetricEncryption extends TestCase {

	private SymmetricEncryption _symmetricEncryption;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String encryptionAlgorithm = "DESede";
		SecretKey secret = KeyGenerator.getInstance(encryptionAlgorithm).generateKey();
		SecureRandom random = new SecureRandom();
		_symmetricEncryption = new SymmetricEncryption(random, secret);
	}

	public void testEncryptDecryptByte() {
		Random random = new Random(47);
		byte[] plainText = new byte[10000];
		random.nextBytes(plainText);

		byte[] encrypted = _symmetricEncryption.encrypt(plainText);
		assertFalse("Enryption must produce at least different content.", Arrays.equals(plainText, encrypted));
		byte[] decrypted = _symmetricEncryption.decrypt(encrypted);
		assertTrue(Arrays.equals(decrypted, plainText));
	}

	public void testEncryptDecryptStream() throws IOException {
		Random random = new Random(47);
		byte[] plainText = new byte[10000];
		random.nextBytes(plainText);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStream encrypted = _symmetricEncryption.encrypt(out);
		encrypted.write(plainText, 0, plainText.length);
		assertFalse("Enryption must produce at least different content.", Arrays.equals(plainText, out.toByteArray()));
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputStream decrypted = _symmetricEncryption.decrypt(in);
		byte[] decryptedText = new byte[10000];
		int offset = 0;
		while (true) {
			int readBytes = decrypted.read(decryptedText, offset, decryptedText.length - offset);
			if (readBytes == -1) {
				break;
			}
			offset += readBytes;
		}
		assertTrue(Arrays.equals(decryptedText, plainText));
	}

}

