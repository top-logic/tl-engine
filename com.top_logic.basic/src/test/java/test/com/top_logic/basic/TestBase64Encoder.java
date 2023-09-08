/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.TestCase;

import com.top_logic.basic.Base64Encoder;

/**
 * Abstract test class for all {@link Base64Encoder}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TestBase64Encoder extends TestCase {

	public void testEncryptDecrypt() {
		Base64Encoder encoder = newBase64Encoder("My funny key");

		encodeDecode(encoder, "value To Encode.");
	}

	/**
	 * Creates the {@link Base64Encoder} for test.
	 * 
	 * @param key
	 *        The key of the {@link Base64Encoder}.
	 */
	protected abstract Base64Encoder newBase64Encoder(String key);

	public void testEncryptDecryptSpecialChars() {
		Base64Encoder encoder = newBase64Encoder("My funny key");

		encodeDecode(encoder, "value To Encod%&§$_#+~*.:,;!^-e.");
	}

	public void testEncryptDecryptLongValue() {
		Base64Encoder encoder = newBase64Encoder("My funny key");

		StringBuilder b = new StringBuilder();
		int i = 0;
		while (i++ < 2000) {
			b.append((char) i);
		}
		encodeDecode(encoder, b.toString());
	}

	private void encodeDecode(Base64Encoder encoder, String value) {
		String encoded = encoder.encode(value);
		assertEquals("Decoding of encoding must be identity.", value, encoder.decode(encoded));
	}

	public void testStableDecoding() {
		Base64Encoder encoder = newBase64Encoder("My funny key");

		String encoded = encoder.encode("value To Encode.");

		String decoded = encoder.decode(encoded);
		assertEquals("Repeated decoding must be stable.", decoded, encoder.decode(encoded));
	}

	public void testStableDecoding2() {
		String encoded = newBase64Encoder("My funny key").encode("value To Encode.");

		String decoded1 = newBase64Encoder("My funny key").decode(encoded);
		String decoded2 = newBase64Encoder("My funny key").decode(encoded);
		assertEquals("Repeated decoding must be stable.", decoded1, decoded2);
	}

	public void testSpecialChars() {
		String key = "My funny key";
		String encoded = newBase64Encoder(key).encode("value To Encode.");

		String decoded1 = newBase64Encoder(key).decode(encoded);
		String decoded2 = newBase64Encoder(new String(key)).decode(encoded);
		assertEquals("Repeated decoding must be stable.", decoded1, decoded2);
	}

}

