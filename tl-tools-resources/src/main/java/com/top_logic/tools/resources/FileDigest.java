/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tools.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Digests of file contents.
 */
public class FileDigest {

	private static final String SHA_256 = "SHA-256";

	private static final int BUFFER_SIZE = 8192;

	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	/**
	 * Computes the SHA-256 digest of the contents of the given file.
	 *
	 * @param file
	 *        The file to read.
	 * @return The digest as lower-case hexadecimal string.
	 * @throws IOException
	 *         If reading the file fails.
	 */
	public static String sha256Hex(File file) throws IOException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(SHA_256);
		} catch (NoSuchAlgorithmException ex) {
			throw new IOException("Digest algorithm is not available: " + SHA_256, ex);
		}

		byte[] buffer = new byte[BUFFER_SIZE];
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			int direct;
			while ((direct = in.read(buffer)) >= 0) {
				digest.update(buffer, 0, direct);
			}
		}

		return toHex(digest.digest());
	}

	private static String toHex(byte[] bytes) {
		StringBuilder result = new StringBuilder(2 * bytes.length);
		for (byte value : bytes) {
			result.append(HEX_DIGITS[(value >> 4) & 0x0F]);
			result.append(HEX_DIGITS[value & 0x0F]);
		}
		return result.toString();
	}

}
