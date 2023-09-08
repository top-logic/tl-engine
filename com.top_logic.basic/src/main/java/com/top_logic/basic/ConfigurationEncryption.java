/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import org.apache.commons.codec.binary.Base64;

/**
 * Helper class to decode / encode configuration values.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigurationEncryption {

	private static final Base64Encoder ENCODER;
	static {
		// BEWARE: if the passphrase is changed, all projects using this feature must be informed so
		// that they can change values which are stored encrypted.
		String passphrase = "This is the passphrase for XMLProperties";
		ENCODER = Base64Encoder.newBase64AESEncoder(passphrase);
	}

	/**
	 * Decodes the given value. It is assumed, that the value is a base64 encoded String.
	 * 
	 * @param encodedValue
	 *        The encoded base64 string
	 * 
	 * @return The decoded String.
	 * 
	 * @see #encrypt(String)
	 */
	public static String decrypt(String encodedValue) {
		return ENCODER.decode(encodedValue);
	}

	/**
	 * returns a String which can be written in a text file, i.e. without special chars (base64) The
	 * returned String is URL-safe String, that means: (see comment for the method
	 * encodeBase64URLSafeString in {@link Base64}:
	 * 
	 * Encodes binary data using a URL-safe variation of the base64 algorithm but does not chunk the
	 * output. The url-safe variation emits - and _ instead of + and / characters. Note: no padding
	 * is added.
	 * 
	 * @return the encrypted and base 64 coded version of plaintext
	 * 
	 * @see #decrypt(String)
	 */
	public static String encrypt(String plaintext) {
		return ENCODER.encode(plaintext);
	}

}

