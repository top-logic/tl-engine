/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static java.nio.charset.StandardCharsets.*;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.InvalidEncryptedValueException;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.EncodingConfigurationValueProvider;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;

/**
 * Test for {@link ConfigurationEncryption}.
 */
@SuppressWarnings("javadoc")
public class TestConfigurationEncryption extends TestCase {

	/**
	 * Plain text of {@link #UNAUTHENTICATED_VALUE}.
	 */
	static final String UNAUTHENTICATED_PLAINTEXT = "Old secret äöü €";

	/**
	 * {@link #UNAUTHENTICATED_PLAINTEXT} in the unauthenticated format without prefix, as created by
	 * {@link com.top_logic.basic.Base64Encoder#newBase64AESEncoder(String)} with the configuration
	 * passphrase.
	 */
	static final String UNAUTHENTICATED_VALUE = "dntKC27wX4k4edJb6D7q-Lew0FNrViSNINU4Eq00M2ADFTfD_g";

	private static final String PROPERTY = "secret-property";

	public void testRoundTrip() {
		assertRoundTrip("Some secret text");
		assertRoundTrip("Pässwört € 中文 🔑");
	}

	private static void assertRoundTrip(String plaintext) {
		String encrypted = ConfigurationEncryption.encrypt(plaintext);
		assertTrue(encrypted.startsWith(ConfigurationEncryption.AUTHENTICATED_PREFIX));
		assertTrue(ConfigurationEncryption.isAuthenticated(encrypted));
		assertEquals(plaintext, ConfigurationEncryption.decrypt(encrypted));
	}

	public void testEncryptionRandomized() {
		assertFalse(ConfigurationEncryption.encrypt("secret").equals(ConfigurationEncryption.encrypt("secret")));
	}

	public void testUnauthenticatedFormatReadable() {
		assertFalse(ConfigurationEncryption.isAuthenticated(UNAUTHENTICATED_VALUE));
		assertEquals(UNAUTHENTICATED_PLAINTEXT, ConfigurationEncryption.decrypt(UNAUTHENTICATED_VALUE));
	}

	public void testUnauthenticatedFormatWarning() throws Exception {
		List<LogEntry> warnings = collectWarnings(() -> {
			assertEquals(UNAUTHENTICATED_PLAINTEXT, provider().getValue(PROPERTY, UNAUTHENTICATED_VALUE));
		});
		assertEquals(1, warnings.size());
		String message = warnings.get(0).getMessage();
		assertTrue("Property not reported: " + message, message.contains(PROPERTY));
		assertFalse("Plain text reported: " + message, message.contains(UNAUTHENTICATED_PLAINTEXT));
		assertFalse("Value reported: " + message, message.contains(UNAUTHENTICATED_VALUE));
	}

	public void testAuthenticatedFormatNoWarning() throws Exception {
		String encrypted = ConfigurationEncryption.encrypt("secret");
		List<LogEntry> warnings = collectWarnings(() -> {
			assertEquals("secret", provider().getValue(PROPERTY, encrypted));
		});
		assertEquals(0, warnings.size());
	}

	public void testUnencryptedPrefix() throws ConfigurationException {
		assertEquals(" Bla blub ", provider().getValue(PROPERTY, "unencrypted: Bla blub "));
	}

	public void testRejectLongPlaintextPassword() {
		assertRejected("ThisIsAPlainTextPassword2026");
		assertRejected("PlainTextPassword12345");
		assertRejected("PlainSecret");
	}

	public void testRejectModifiedAuthenticatedValue() {
		String encrypted = ConfigurationEncryption.encrypt("Some secret text");
		int index = ConfigurationEncryption.AUTHENTICATED_PREFIX.length() + 10;
		char flipped = encrypted.charAt(index) == 'A' ? 'B' : 'A';
		assertRejected(encrypted.substring(0, index) + flipped + encrypted.substring(index + 1));
	}

	public void testRejectTruncatedAuthenticatedValue() {
		String encrypted = ConfigurationEncryption.encrypt("Some secret text");
		for (int cut = 1; cut <= 5; cut++) {
			assertRejected(encrypted.substring(0, encrypted.length() - cut));
		}
		assertRejected(ConfigurationEncryption.AUTHENTICATED_PREFIX);
	}

	public void testRejectPrefixedPlaintext() {
		assertRejected(ConfigurationEncryption.AUTHENTICATED_PREFIX + "PlainSecret");
		assertRejected(ConfigurationEncryption.AUTHENTICATED_PREFIX + "A plain secret!");
	}

	public void testRejectNonBase64UnauthenticatedValue() {
		assertRejected(UNAUTHENTICATED_VALUE + "!");
		assertRejected("#" + UNAUTHENTICATED_VALUE);
		assertRejected(UNAUTHENTICATED_VALUE.substring(0, 20) + " " + UNAUTHENTICATED_VALUE.substring(20));
	}

	public void testRejectUnauthenticatedValueNotUTF8() {
		byte[] cipherText = Base64.getUrlDecoder().decode(UNAUTHENTICATED_VALUE);
		byte[] plainText = UNAUTHENTICATED_PLAINTEXT.getBytes(UTF_8);
		// The cipher text consists of a 16 byte IV followed by the plain text XOR the key stream.
		int ivLength = cipherText.length - plainText.length;
		cipherText[ivLength] ^= (byte) (plainText[0] ^ 0xFF);
		assertRejected(Base64.getUrlEncoder().withoutPadding().encodeToString(cipherText));
	}

	private static void assertRejected(String value) {
		try {
			ConfigurationEncryption.decrypt(value);
			fail("Invalid encrypted value must be rejected.");
		} catch (InvalidEncryptedValueException ex) {
			assertNotReported(value, ex);
		}
		try {
			provider().getValue(PROPERTY, value);
			fail("Invalid encrypted value must be rejected.");
		} catch (ConfigurationException ex) {
			assertNotReported(value, ex);
			String report = report(ex);
			assertTrue("Property not reported: " + report, report.contains(PROPERTY));
		}
	}

	private static void assertNotReported(String value, Throwable ex) {
		String report = report(ex);
		String body = value.startsWith(ConfigurationEncryption.AUTHENTICATED_PREFIX)
			? value.substring(ConfigurationEncryption.AUTHENTICATED_PREFIX.length())
			: value;
		if (body.isEmpty()) {
			return;
		}
		assertFalse("Value reported: " + report, report.contains(body));
	}

	private static String report(Throwable ex) {
		StringBuilder report = new StringBuilder();
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			report.append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append('\n');
		}
		return report.toString();
	}

	private static EncodingConfigurationValueProvider<String> provider() {
		return EncodingConfigurationValueProvider.ensureEncodedConfigurationValue(StringValueProvider.INSTANCE);
	}

	/**
	 * Runs the given action and returns the warnings logged meanwhile.
	 */
	static List<LogEntry> collectWarnings(ThrowingRunnable action) throws Exception {
		CollectingLogListener listener = new CollectingLogListener(Set.of(Level.WARN), true);
		try {
			action.run();
		} finally {
			listener.deactivate();
		}
		return listener.getLogEntries();
	}

	/**
	 * Action for {@link TestConfigurationEncryption#collectWarnings(ThrowingRunnable)}.
	 */
	interface ThrowingRunnable {
		void run() throws Exception;
	}

}
