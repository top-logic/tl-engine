/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * {@link InputStream} computing a message digest and the number of bytes of the content read
 * through it.
 *
 * <p>
 * Every byte delivered by this stream is part of the digest, also bytes passed over with
 * {@link #skip(long)}. Marking is not supported, since a reset would hash bytes twice.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HashingInputStream extends FilterInputStream {

	/**
	 * The digest algorithm used by {@link #sha256(InputStream)}.
	 */
	public static final String SHA_256 = "SHA-256";

	private static final int SKIP_BUFFER_SIZE = 8192;

	private final MessageDigest _digest;

	private long _count;

	private boolean _eof;

	private String _hash;

	/**
	 * Creates a {@link HashingInputStream}.
	 *
	 * @param in
	 *        The stream to read from.
	 * @param digest
	 *        The digest to update with the content.
	 */
	public HashingInputStream(InputStream in, MessageDigest digest) {
		super(in);
		_digest = digest;
	}

	/**
	 * Creates a {@link HashingInputStream} computing the {@link #SHA_256} digest of the given
	 * content.
	 */
	public static HashingInputStream sha256(InputStream in) {
		return new HashingInputStream(in, newDigest(SHA_256));
	}

	/**
	 * Creates a {@link MessageDigest} for an algorithm every Java platform supports.
	 *
	 * @param algorithm
	 *        The name of the algorithm, e.g. {@link #SHA_256}.
	 */
	public static MessageDigest newDigest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Digest algorithm '" + algorithm + "' not available.", ex);
		}
	}

	@Override
	public int read() throws IOException {
		int result = super.read();
		if (result < 0) {
			reachedEnd();
		} else {
			_digest.update((byte) result);
			_count++;
		}
		return result;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int result = super.read(buffer, offset, length);
		if (result < 0) {
			reachedEnd();
		} else if (result > 0) {
			_digest.update(buffer, offset, result);
			_count += result;
		}
		return result;
	}

	@Override
	public long skip(long n) throws IOException {
		if (n <= 0) {
			return 0;
		}
		byte[] buffer = new byte[(int) Math.min(SKIP_BUFFER_SIZE, n)];
		long skipped = 0;
		while (skipped < n) {
			int direct = read(buffer, 0, (int) Math.min(buffer.length, n - skipped));
			if (direct < 0) {
				break;
			}
			skipped += direct;
		}
		return skipped;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void mark(int readlimit) {
		// Not supported.
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("Mark not supported.");
	}

	private void reachedEnd() throws IOException {
		if (_eof) {
			return;
		}
		_eof = true;
		onEnd();
	}

	/**
	 * Called once, when the end of the content has been reached.
	 *
	 * <p>
	 * At this time, {@link #getCount()} and {@link #getHash()} describe the complete content.
	 * </p>
	 *
	 * @throws IOException
	 *         To report the read that reached the end as failed.
	 */
	protected void onEnd() throws IOException {
		// Hook for subclasses.
	}

	/**
	 * Whether the end of the content has been reached.
	 */
	public boolean isEnd() {
		return _eof;
	}

	/**
	 * The number of bytes read so far.
	 */
	public long getCount() {
		return _count;
	}

	/**
	 * The digest of the bytes read so far as lower case hex string.
	 *
	 * <p>
	 * The digest is completed when this method is called first; it must only be called when the
	 * content has been read completely.
	 * </p>
	 */
	public String getHash() {
		if (_hash == null) {
			_hash = HexFormat.of().formatHex(_digest.digest());
		}
		return _hash;
	}

}
