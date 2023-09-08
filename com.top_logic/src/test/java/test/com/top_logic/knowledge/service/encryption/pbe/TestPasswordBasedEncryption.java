/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.encryption.pbe;

import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.knowledge.service.encryption.SecurityService;
import com.top_logic.knowledge.service.encryption.pbe.EncryptionResult;
import com.top_logic.knowledge.service.encryption.pbe.InvalidPasswordException;
import com.top_logic.knowledge.service.encryption.pbe.PasswordBasedEncryption;

/**
 * Test case for {@link PasswordBasedEncryption}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see "http://javaboutique.internet.com/resources/books/JavaSec/javasec2_4.html"
 */
public class TestPasswordBasedEncryption extends TestCase {

	private static final String ALGORITHM = "PBEWithSHAAndTwofish-CBC";

	private static final int SALT_LENGTH = 8;

	private static int ITERATIONS = 1000;


	public void testEncryptDecrypt() throws Exception {
		char[] password = ("123" + ((char) 12345) + "567").toCharArray();
		byte[] plainText = "Hello world!".getBytes();

		EncryptionResult encryption = createEngine(password).encrypt(plainText);

		byte[] recoveredPlainText = createEngine(password).decrypt(encryption);

		assertTrue(Arrays.equals(plainText, recoveredPlainText));
	}

	public void testEncodedEncryptDecrypt() throws Exception {
		// Note: Password cannot be longer than 7 characters without the "unrestricted policy" file
		// installed in the runtime.
		char[] password = ("123" + ((char) 12345) + "567").toCharArray();
		String plainText = "Hello world!";
		String cipherText = encrypt(password, plainText);

		String recoveredText = decrypt(password, cipherText);
		assertEquals(plainText, recoveredText);
	}

	public void testWrongPassword() throws InvalidPasswordException {
		char[] password = "1234568".toCharArray();
		String plainText = "Hello world!";

		String cipherText = encrypt(password, plainText);

		char[] wrongPassword = "Test".toCharArray();
		try {
			String someText = decrypt(wrongPassword, cipherText);
			assertFalse(plainText.equals(someText));
		} catch (BadPaddingException ex) {
			// Possible outcome with wrong password.
		}
	}

	private String encrypt(char[] password, String plainText) throws InvalidPasswordException {
		return createEngine(password).encryptEncoded(plainText.getBytes());
	}

	private static PasswordBasedEncryption createEngine(char[] password) throws InvalidPasswordException {
		return new PasswordBasedEncryption(new Random(), ALGORITHM, SALT_LENGTH, ITERATIONS, password);
	}

	private String decrypt(char[] password, String encodedSaltAndCipherText) throws BadPaddingException,
			InvalidPasswordException {
		try {
			byte[] plainText = createEngine(password).decryptEncoded(encodedSaltAndCipherText);
			return new String(plainText);
		} catch (IllegalBlockSizeException ex) {
			throw fail(ex);
		}
	}

	private static AssertionFailedError fail(Throwable ex) {
		return (AssertionFailedError) new AssertionFailedError("Crypto API failed.").initCause(ex);
	}

	public static Test suite() {
		Test test = new TestSuite(TestPasswordBasedEncryption.class);
		test = ServiceTestSetup.createSetup(test, SecurityService.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}

}
