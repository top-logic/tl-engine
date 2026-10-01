/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.Logger;

/**
 * {@link HashingInputStream} checking the content against an expected {@link #SHA_256} hash and
 * size.
 *
 * <p>
 * When the end of the content is reached, the hash and the number of bytes read are compared with
 * the expected values. A mismatch is logged and fails the read that reached the end with an
 * {@link IOException}. A stream closed before its end is not checked.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class VerifyingInputStream extends HashingInputStream {

	private final String _expectedHash;

	private final long _expectedSize;

	private final String _description;

	/**
	 * Creates a {@link VerifyingInputStream}.
	 *
	 * @param in
	 *        The stream to read from.
	 * @param expectedHash
	 *        The expected {@link #SHA_256} hash of the content as hex string. Case is ignored.
	 * @param expectedSize
	 *        The expected number of bytes of the content, or <code>-1</code> if only the hash is
	 *        checked.
	 * @param description
	 *        Description of the content for the error message.
	 */
	public VerifyingInputStream(InputStream in, String expectedHash, long expectedSize, String description) {
		super(in, newDigest(SHA_256));
		_expectedHash = expectedHash;
		_expectedSize = expectedSize;
		_description = description;
	}

	@Override
	protected void onEnd() throws IOException {
		if (_expectedSize >= 0 && getCount() != _expectedSize) {
			fail("Integrity check failed for " + _description + ": Expected " + _expectedSize + " bytes, read "
				+ getCount() + " bytes.");
		}
		if (!getHash().equalsIgnoreCase(_expectedHash)) {
			fail("Integrity check failed for " + _description + ": Expected hash " + _expectedHash + ", found "
				+ getHash() + ".");
		}
	}

	private static void fail(String message) throws IOException {
		IOException error = new IOException(message);
		Logger.error(message, error, VerifyingInputStream.class);
		throw error;
	}

}
