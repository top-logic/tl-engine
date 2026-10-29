/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.admin.system;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.security.util.CryptSupport;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.admin.system.EncryptionFormat;
import com.top_logic.layout.view.admin.system.I18NConstants;
import com.top_logic.layout.view.admin.system.SecretFunctions;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of {@link SecretFunctions#encrypt(String, EncryptionFormat)} and
 * {@link SecretFunctions#decrypt(String, EncryptionFormat)}.
 */
@SuppressWarnings("javadoc")
public class TestSecretFunctionsEncryption extends TestCase {

	private static final String PLAIN = "S3cret äöü €";

	public void testRoundTripEveryFormat() {
		for (EncryptionFormat format : EncryptionFormat.values()) {
			String encrypted = SecretFunctions.encrypt(PLAIN, format);
			assertFalse(format.name(), encrypted.contains(PLAIN));
			assertEquals(format.name(), PLAIN, SecretFunctions.decrypt(encrypted, format));
		}
	}

	public void testConfigurationFormatIsWhatTheReaderAccepts() {
		String encrypted = SecretFunctions.encrypt(PLAIN, EncryptionFormat.CONFIGURATION);
		assertTrue(ConfigurationEncryption.isAuthenticated(encrypted));
		assertEquals(PLAIN, ConfigurationEncryption.decrypt(encrypted));
	}

	public void testCryptSupportFormatIsWhatTheReaderAccepts() {
		String encrypted = SecretFunctions.encrypt(PLAIN, EncryptionFormat.CRYPT_SUPPORT);
		assertEquals(PLAIN, CryptSupport.getInstance().decodeString(encrypted));
	}

	public void testDecryptIgnoresSurroundingWhiteSpace() {
		for (EncryptionFormat format : EncryptionFormat.values()) {
			String encrypted = SecretFunctions.encrypt(PLAIN, format);
			assertEquals(format.name(), PLAIN, SecretFunctions.decrypt("  " + encrypted + "\n", format));
		}
	}

	public void testDecryptInvalid() {
		for (EncryptionFormat format : EncryptionFormat.values()) {
			assertNotEncrypted(format, "garbage");
			assertNotEncrypted(format, "{gcm}garbage");
			assertNotEncrypted(format, "0123456789abcdef");
		}
	}

	public void testDecryptInOtherFormat() {
		assertNotEncrypted(EncryptionFormat.CRYPT_SUPPORT,
			SecretFunctions.encrypt(PLAIN, EncryptionFormat.CONFIGURATION));
		assertNotEncrypted(EncryptionFormat.CONFIGURATION,
			SecretFunctions.encrypt(PLAIN, EncryptionFormat.CRYPT_SUPPORT));
	}

	public void testModifiedCryptSupportValue() {
		String encrypted = SecretFunctions.encrypt(PLAIN, EncryptionFormat.CRYPT_SUPPORT);
		char last = encrypted.charAt(encrypted.length() - 1);
		String modified = encrypted.substring(0, encrypted.length() - 1) + (last == '0' ? '1' : '0');
		assertNotEncrypted(EncryptionFormat.CRYPT_SUPPORT, modified);
	}

	public void testMissingInput() {
		assertError(I18NConstants.ERROR_NO_VALUE, () -> SecretFunctions.encrypt("", EncryptionFormat.CONFIGURATION));
		assertError(I18NConstants.ERROR_NO_VALUE, () -> SecretFunctions.decrypt(" ", EncryptionFormat.CONFIGURATION));
		assertError(I18NConstants.ERROR_NO_ENCRYPTION_FORMAT, () -> SecretFunctions.encrypt(PLAIN, null));
		assertError(I18NConstants.ERROR_NO_ENCRYPTION_FORMAT, () -> SecretFunctions.decrypt(PLAIN, null));
	}

	private static void assertNotEncrypted(EncryptionFormat format, String value) {
		try {
			SecretFunctions.decrypt(value, format);
			fail("Decrypted invalid value in format " + format + ": " + value);
		} catch (TopLogicException ex) {
			ResKey key = ex.getErrorKey();
			assertEquals(I18NConstants.ERROR_NOT_ENCRYPTED__FORMAT.getKey(), key.plain().getKey());
			assertEquals(ResKey.forEnum(format), key.arguments()[0]);
			assertNull("The cause may reveal the value.", ex.getCause());
		}
	}

	private static void assertError(ResKey expectedKey, Runnable call) {
		try {
			call.run();
			fail("Expected error " + expectedKey);
		} catch (TopLogicException ex) {
			assertEquals(expectedKey.getKey(), ex.getErrorKey().getKey());
		}
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(TestSecretFunctionsEncryption.class, CryptSupport.Module.INSTANCE));
	}

}
